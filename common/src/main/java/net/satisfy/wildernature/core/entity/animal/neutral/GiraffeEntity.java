package net.satisfy.wildernature.core.entity.animal.neutral;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.goal.FollowParentAtDistanceGoal;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GiraffeEntity extends Animal {
    private static final int FLAG_EATING = 0x00000001;
    private static final int FLAG_RIDING_BURST = 0x00000100;
    private static final int FLAG_FATIGUED = 0x00001000;

    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ALERT = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(GiraffeEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int AWARENESS_CHECK_INTERVAL = 5;
    private static final int EATING_DURATION_MIN = 50;
    private static final int EATING_DURATION_RANDOM = 35;
    private static final int EATING_COOLDOWN_MIN = 4000;
    private static final int EATING_COOLDOWN_RANDOM = 3501;
    private static final int RIDE_COOLDOWN_MIN = 220;
    private static final int RIDE_COOLDOWN_RANDOM = 120;
    private static final int RIDE_DURATION_MIN = 300;
    private static final int FATIGUE_DURATION_MIN = 100;
    private static final int FATIGUE_DURATION_RANDOM = 61;
    private static final int ALERT_DURATION_TICKS = 100;
    private static final int SLEEP_PARTICLE_INTERVAL_TICKS = 14;
    private static final int SLEEP_COOLDOWN_MIN = 200;
    private static final int SLEEP_COOLDOWN_RANDOM = 200;
    private static final double AWARENESS_RADIUS = 20.0D;
    private static final double WAKE_UP_RADIUS = 9.0D;

    private int awarenessCheckCooldown;
    private int eatingTicks;
    private int eatingCooldownTicks;
    private int rideTicks;
    private int rideCooldownTicks;
    private int fatigueTicks;
    private int alertTicks;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;
    private float awareness;
    @Nullable
    private BlockPos eatingTargetPos;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState alertState = new AnimationState();
    public final AnimationState eatingState = new AnimationState();
    public final AnimationState runState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();

    public GiraffeEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.28D).add(Attributes.MAX_HEALTH, 24.0D).add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.FOLLOW_RANGE, 28.0D).add(Attributes.KNOCKBACK_RESISTANCE, 0.35D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_ALERT, false);
        builder.define(DATA_SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.45D) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, LionEntity.class, 18.0F, 1.1D, 1.45D) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(Items.SHORT_GRASS), false) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentAtDistanceGoal(this, 1.15D) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isRidingBurst() && !GiraffeEntity.this.isEating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isRidingBurst() && !GiraffeEntity.this.isEating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 10.0F) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !GiraffeEntity.this.isSleeping() && !GiraffeEntity.this.isEating() && super.canContinueToUse();
            }
        });
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.eatingTicks = 0;
        this.eatingCooldownTicks = 0;
        this.rideTicks = 0;
        this.rideCooldownTicks = 0;
        this.fatigueTicks = 0;
        this.alertTicks = 0;
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
        this.sleepCooldownTicks = 0;
        this.awareness = 0.0F;
        this.eatingTargetPos = null;
        this.stopFatigue();
        this.setSleeping(false);
        return out;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Flags", this.entityData.get(DATA_FLAGS_ID));
        tag.putBoolean("Alert", this.isAlert());
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putBoolean("Eating", this.isEating());
        tag.putBoolean("RidingBurst", this.isRidingBurst());
        tag.putBoolean("Fatigued", this.isFatigued());
        tag.putInt("EatingTicks", this.eatingTicks);
        tag.putInt("EatingCooldownTicks", this.eatingCooldownTicks);
        tag.putInt("RideTicks", this.rideTicks);
        tag.putInt("RideCooldownTicks", this.rideCooldownTicks);
        tag.putInt("FatigueTicks", this.fatigueTicks);
        tag.putInt("AlertTicks", this.alertTicks);
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        tag.putFloat("Awareness", this.awareness);
        if (this.eatingTargetPos != null) {
            tag.putInt("EatingTargetX", this.eatingTargetPos.getX());
            tag.putInt("EatingTargetY", this.eatingTargetPos.getY());
            tag.putInt("EatingTargetZ", this.eatingTargetPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS_ID, tag.getInt("Flags"));
        if (tag.contains("Alert")) this.entityData.set(DATA_ALERT, tag.getBoolean("Alert"));
        if (tag.contains("Sleeping")) this.entityData.set(DATA_SLEEPING, tag.getBoolean("Sleeping"));
        if (tag.contains("Eating")) this.setFlag(FLAG_EATING, tag.getBoolean("Eating"));
        if (tag.contains("RidingBurst")) this.setFlag(FLAG_RIDING_BURST, tag.getBoolean("RidingBurst"));
        if (tag.contains("Fatigued")) this.setFlag(FLAG_FATIGUED, tag.getBoolean("Fatigued"));
        this.eatingTicks = tag.getInt("EatingTicks");
        this.eatingCooldownTicks = tag.getInt("EatingCooldownTicks");
        this.rideTicks = tag.getInt("RideTicks");
        this.rideCooldownTicks = tag.getInt("RideCooldownTicks");
        this.fatigueTicks = tag.getInt("FatigueTicks");
        this.alertTicks = tag.getInt("AlertTicks");
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks") ? tag.getInt("RequiredSleepPreparationTicks") : 100 + this.random.nextInt(120);
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.awareness = tag.getFloat("Awareness");
        if (tag.contains("EatingTargetX") && tag.contains("EatingTargetY") && tag.contains("EatingTargetZ")) this.eatingTargetPos = new BlockPos(tag.getInt("EatingTargetX"), tag.getInt("EatingTargetY"), tag.getInt("EatingTargetZ"));
        else this.eatingTargetPos = null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateSleep();
            this.updateAwareness();
            this.updateEating();
            this.updateRideState();
            this.updateFatigue();
            this.updateAlertState();
            this.updateRidingBurstParticles();
            this.spawnEatingParticles();
        }

        this.updateAnimations();
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) this.sleepCooldownTicks--;

        boolean isNight = !this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.002D;
        boolean hasThreatsNearby = !this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(12.0D, 4.0D, 12.0D)).isEmpty();

        if (this.isSleeping()) {
            if (!isNight || this.isInWaterOrBubble() || this.isVehicle() || this.isAlert() || this.isEating() || this.isRidingBurst() || hasThreatsNearby || this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
                if (this.hasWakeUpTriggerNearby()) {
                    this.alertTicks = Math.max(this.alertTicks, ALERT_DURATION_TICKS);
                    this.entityData.set(DATA_ALERT, true);
                    this.spawnAlertParticle();
                }
            } else {
                this.getNavigation().stop();
                this.setDeltaMovement(Vec3.ZERO);
                if (this.level() instanceof ServerLevel && this.tickCount % SLEEP_PARTICLE_INTERVAL_TICKS == 0) this.spawnSleepingParticle();
            }
            return;
        }

        if (isNight && !this.isInWaterOrBubble() && !this.isVehicle() && !this.isAlert() && !this.isEating() && !this.isRidingBurst() && !this.isFatigued() && !hasThreatsNearby && this.sleepCooldownTicks <= 0 && isStill) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks) this.startSleeping();
        } else if (this.sleepPreparationTicks > 0) {
            this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 25);
        }
    }

    private boolean hasWakeUpTriggerNearby() {
        Player nearestPlayer = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (nearestPlayer == null) return false;
        if (nearestPlayer.isCreative() || nearestPlayer.isSpectator()) return false;
        return !nearestPlayer.isCrouching();
    }

    private void startSleeping() {
        this.stopEating();
        this.stopRidingBurst();
        this.setSleeping(true);
        this.awareness = 0.0F;
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void wakeUp() {
        if (this.isSleeping()) {
            this.setSleeping(false);
            this.sleepCooldownTicks = SLEEP_COOLDOWN_MIN + this.random.nextInt(SLEEP_COOLDOWN_RANDOM);
        }
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    private void updateAwareness() {
        if (this.isSleeping()) {
            this.awareness = 0.0F;
            return;
        }

        if (this.awarenessCheckCooldown > 0) {
            this.awarenessCheckCooldown--;
            return;
        }

        this.awarenessCheckCooldown = AWARENESS_CHECK_INTERVAL;

        Player nearestPlayer = this.level().getNearestPlayer(this, AWARENESS_RADIUS);
        if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator()) {
            this.awareness = Math.max(0.0F, this.awareness - 1.2F);
            return;
        }

        float increase = 0.0F;
        if (!nearestPlayer.isCrouching()) increase += 1.8F;
        else increase += 0.15F;
        if (nearestPlayer.isSprinting()) increase += 6.0F;
        if (nearestPlayer.getMainHandItem().is(Items.BOW) || nearestPlayer.getOffhandItem().is(Items.BOW)) increase += 12.0F;
        if (nearestPlayer.getMainHandItem().is(Items.SHORT_GRASS) || nearestPlayer.getOffhandItem().is(Items.SHORT_GRASS)) increase -= 3.0F;

        List<Monster> nearbyMonsters = this.level().getEntitiesOfClass(Monster.class, this.getBoundingBox().inflate(12.0D, 4.0D, 12.0D));
        if (!nearbyMonsters.isEmpty()) increase += 16.0F;
        if (this.isFatigued()) increase *= 0.35F;

        this.awareness = Mth.clamp(this.awareness + increase - 1.0F, 0.0F, 100.0F);

        if (this.awareness > 35.0F && this.random.nextInt(20) == 0) this.spawnAwarenessParticle();

        if (this.awareness >= 100.0F) {
            this.alertTicks = ALERT_DURATION_TICKS;
            this.entityData.set(DATA_ALERT, true);
            this.spawnAlertParticle();
            this.stopEating();
            this.wakeUp();
            this.awareness = 0.0F;
        }
    }

    private void updateEating() {
        if (this.eatingCooldownTicks > 0) this.eatingCooldownTicks--;

        if (this.isSleeping() || this.isAlert() || this.isVehicle() || this.isRidingBurst() || this.isFatigued()) {
            this.stopEating();
            return;
        }

        if (this.isEating()) {
            if (this.eatingTargetPos == null || !this.isValidEatingTarget(this.eatingTargetPos)) {
                this.stopEating();
                return;
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            this.getLookControl().setLookAt(this.eatingTargetPos.getX() + 0.5D, this.eatingTargetPos.getY() + 0.5D, this.eatingTargetPos.getZ() + 0.5D);

            if (this.eatingTicks > 0) this.eatingTicks--;

            if (this.eatingTicks == 10) this.finishEatingDrops();

            if (this.eatingTicks <= 0) {
                this.stopEating();
                this.eatingCooldownTicks = EATING_COOLDOWN_MIN + this.random.nextInt(EATING_COOLDOWN_RANDOM);
            }
            return;
        }

        if (this.eatingCooldownTicks > 0 || this.getDeltaMovement().horizontalDistanceSqr() > 0.002D || this.random.nextInt(90) != 0) return;

        BlockPos acaciaLeavesPos = this.findNearbyAcaciaLeaves();
        if (acaciaLeavesPos != null) this.startEating(acaciaLeavesPos);
    }

    private void spawnEatingParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isEating() || this.eatingTargetPos == null || this.tickCount % 6 != 0) return;

        double particleX = this.eatingTargetPos.getX() + 0.5D + (this.random.nextDouble() - 0.5D) * 0.35D;
        double particleY = this.eatingTargetPos.getY() + 0.55D + this.random.nextDouble() * 0.25D;
        double particleZ = this.eatingTargetPos.getZ() + 0.5D + (this.random.nextDouble() - 0.5D) * 0.35D;

        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level().getBlockState(this.eatingTargetPos)), particleX, particleY, particleZ, 5, 0.08D, 0.08D, 0.08D, 0.02D);
    }

    @Nullable
    private BlockPos findNearbyAcaciaLeaves() {
        BlockPos entityPos = this.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int yOffset = 1; yOffset <= 7; yOffset++) {
            for (int xOffset = -3; xOffset <= 3; xOffset++) {
                for (int zOffset = -3; zOffset <= 3; zOffset++) {
                    mutableBlockPos.set(entityPos.getX() + xOffset, entityPos.getY() + yOffset, entityPos.getZ() + zOffset);
                    if (this.isValidEatingTarget(mutableBlockPos)) return mutableBlockPos.immutable();
                }
            }
        }

        return null;
    }

    private boolean isValidEatingTarget(BlockPos blockPos) {
        return this.level().getBlockState(blockPos).is(Blocks.ACACIA_LEAVES);
    }

    private void startEating(BlockPos blockPos) {
        this.eatingTargetPos = blockPos;
        this.eatingTicks = EATING_DURATION_MIN + this.random.nextInt(EATING_DURATION_RANDOM);
        this.startEating();
    }

    private void finishEatingDrops() {
        if (!(this.level() instanceof ServerLevel) || this.eatingTargetPos == null || !this.isValidEatingTarget(this.eatingTargetPos)) return;
        if (this.random.nextFloat() < 0.5F) this.spawnAtLocation(new ItemStack(Items.STICK, 1 + this.random.nextInt(2)));
        if (this.random.nextFloat() < 0.2F) this.spawnAtLocation(new ItemStack(Items.ACACIA_SAPLING));
    }

    private void updateRideState() {
        if (this.rideCooldownTicks > 0) this.rideCooldownTicks--;

        if (!this.isVehicle()) {
            if (this.rideTicks > 0 || this.isRidingBurst()) {
                this.rideTicks = 0;
                this.stopRidingBurst();
                this.startFatigue();
            }
            return;
        }

        if (this.isSleeping()) this.wakeUp();

        if (this.rideTicks > 0) this.rideTicks--;

        if (this.rideTicks <= 0) {
            this.rideTicks = 0;
            if (this.isRidingBurst()) this.stopRidingBurst();
            this.ejectPassengers();
            this.startFatigue();
            this.rideCooldownTicks = RIDE_COOLDOWN_MIN + this.random.nextInt(RIDE_COOLDOWN_RANDOM);
            if (this.isAlert()) this.entityData.set(DATA_ALERT, true);
            return;
        }

        if (this.isAlert()) {
            this.stopRidingBurst();
            this.ejectPassengers();
            this.startFatigue();
            this.rideCooldownTicks = RIDE_COOLDOWN_MIN + this.random.nextInt(RIDE_COOLDOWN_RANDOM);
            this.alertTicks = Math.max(this.alertTicks, 40);
            this.entityData.set(DATA_ALERT, true);
        }
    }

    private void updateFatigue() {
        if (this.fatigueTicks > 0) this.fatigueTicks--;
        if (this.fatigueTicks <= 0 && this.isFatigued()) this.stopFatigue();
    }

    private void updateAlertState() {
        if (this.alertTicks > 0) this.alertTicks--;
        if (this.alertTicks <= 0) this.entityData.set(DATA_ALERT, false);
    }

    private void updateAnimations() {
        this.eatingState.animateWhen(this.isEating(), this.tickCount);
        this.runState.animateWhen(this.isRidingBurst(), this.tickCount);
        this.alertState.animateWhen(this.isAlert(), this.tickCount);
        this.sleepState.animateWhen(this.isSleeping(), this.tickCount);

        if (this.level().isClientSide) this.setupAnimationStates();
    }

    private void updateRidingBurstParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel) || !this.isRidingBurst() || this.getDeltaMovement().horizontalDistanceSqr() < 0.08D) return;

        BlockPos groundBlockPos = this.blockPosition().below();
        BlockState groundBlockState = this.level().getBlockState(groundBlockPos);
        if (groundBlockState.isAir()) return;

        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundBlockState), this.getX(), this.getY() + 0.1D, this.getZ(), 18, this.getBbWidth() * 0.35D, 0.08D, this.getBbWidth() * 0.35D, 0.12D);
    }

    private void spawnAlertParticle() {
        if (this.level() instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), this.getX(), this.getY() + this.getBbHeight() + 0.25D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void spawnAwarenessParticle() {
        if (this.level() instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypeRegistry.QUESTION.get(), this.getX(), this.getY() + this.getBbHeight() + 0.25D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    private void spawnSleepingParticle() {
        if (this.level() instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D, this.getY() + this.getBbHeight() * 0.75D, this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
    }

    public boolean isAlert() {
        return this.entityData.get(DATA_ALERT);
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    private void setFlag(int flag, boolean value) {
        if (value) this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) | flag);
        else this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) & ~flag);
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS_ID) & flag) != 0;
    }

    public boolean isEating() {
        return this.getFlag(FLAG_EATING);
    }

    public void startEating() {
        this.wakeUp();
        this.setFlag(FLAG_EATING, true);
    }

    public void stopEating() {
        this.setFlag(FLAG_EATING, false);
        this.eatingTicks = 0;
        this.eatingTargetPos = null;
    }

    public boolean isRidingBurst() {
        return this.getFlag(FLAG_RIDING_BURST);
    }

    public void startRidingBurst() {
        this.wakeUp();
        this.setFlag(FLAG_RIDING_BURST, true);
    }

    public void stopRidingBurst() {
        this.setFlag(FLAG_RIDING_BURST, false);
    }

    public boolean isFatigued() {
        return this.getFlag(FLAG_FATIGUED);
    }

    public void startFatigue() {
        this.fatigueTicks = FATIGUE_DURATION_MIN + this.random.nextInt(FATIGUE_DURATION_RANDOM);
        this.setFlag(FLAG_FATIGUED, true);
    }

    public void stopFatigue() {
        this.fatigueTicks = 0;
        this.setFlag(FLAG_FATIGUED, false);
    }

    public void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isSleeping() && !this.isAlert() && !this.isEating() && !this.isRidingBurst() && !this.isFatigued();

        if (idleAllowed) this.idleState.startIfStopped(this.tickCount);
        else this.idleState.stop();
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(TagsRegistry.GIRAFFE_FOOD);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;
        if (this.isFood(itemStack)) return super.mobInteract(player, hand);
        if (this.isSleeping()) this.wakeUp();
        if (this.isBaby() || this.isVehicle() || this.isAlert() || this.isEating() || this.rideCooldownTicks > 0) return super.mobInteract(player, hand);

        if (!this.level().isClientSide) {
            this.startRidingBurst();
            this.rideTicks = RIDE_DURATION_MIN;
            if (!player.startRiding(this, true)) {
                this.stopRidingBurst();
                this.rideTicks = 0;
                return InteractionResult.PASS;
            }
        }

        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !this.isBaby() && this.getPassengers().isEmpty();
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        float rotation = -this.getYRot() * ((float) Math.PI / 180F);
        Vec3 localOffset = new Vec3(0.0D, 2.25D, 0.45D);
        return new Vec3(this.getX(), this.getY(), this.getZ()).add(localOffset.yRot(rotation));
    }

    @Override
    public void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        if (!this.hasPassenger(passenger)) return;

        Vec3 passengerPosition = this.getPassengerAttachmentPoint(passenger, passenger.getDimensions(passenger.getPose()), 1.0F);
        moveFunction.accept(passenger, passengerPosition.x, passengerPosition.y, passengerPosition.z);
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isSleeping()) {
            this.getNavigation().stop();
            super.travel(Vec3.ZERO);
            return;
        }

        if (this.isAlive() && this.isVehicle() && this.getFirstPassenger() instanceof Player player) {
            this.setYRot(player.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(player.getXRot() * 0.35F);
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float sidewaysInput = player.xxa * 0.35F;
            float forwardInput = Math.max(player.zza, 0.0F);
            float speedModifier = 1.0F;
            if (this.isRidingBurst()) speedModifier = 2.15F;
            else if (this.isFatigued()) speedModifier = 0.6F;

            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * speedModifier);
            super.travel(new Vec3(sidewaysInput, travelVector.y, forwardInput));
            return;
        }

        super.travel(travelVector);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        this.wakeUp();
        boolean wasHurt = super.hurt(damageSource, amount);
        if (wasHurt) {
            this.alertTicks = ALERT_DURATION_TICKS;
            this.entityData.set(DATA_ALERT, true);
            this.stopEating();
            if (this.isVehicle()) {
                this.stopRidingBurst();
                this.ejectPassengers();
                this.startFatigue();
                this.rideCooldownTicks = RIDE_COOLDOWN_MIN + this.random.nextInt(RIDE_COOLDOWN_RANDOM);
            }
        }
        return wasHurt;
    }

    @Nullable
    @Override
    public GiraffeEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.GIRAFFE.get().create(serverLevel);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.GIRAFFE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.GIRAFFE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.GIRAFFE_DEATH.get();
    }

    @Override
    public float getVoicePitch() {
        return 1.2F + this.random.nextFloat() * 0.2F;
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }
}