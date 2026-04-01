package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.DeerGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DeerEntity extends Animal {
    private static final int FLAG_RUNNING = 0x00000100;
    private static final int FLAG_EATING = 0x00010000;
    private static final int FLAG_LOOKING_AROUND = 0x00000010;

    private static final EntityDataAccessor<Integer> DATA_TYPE_ID;
    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID;
    private static final EntityDataAccessor<Boolean> DATA_WHITE;
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING;

    private static final int CALL_COOLDOWN_TICKS = 200;
    private static final int PANIC_DURATION_TICKS = 100;
    private static final int HERD_CHECK_INTERVAL_TICKS = 40;
    private static final int SLEEPING_PARTICLE_INTERVAL_TICKS = 14;
    private static final double CALL_RADIUS = 24.0D;
    private static final double AWARENESS_RADIUS = 16.0D;
    private static final double WAKE_UP_RADIUS = 8.0D;
    private static final double HERD_SEARCH_RADIUS = 12.0D;
    private static final double PANIC_SHARE_RADIUS = 20.0D;

    private static final double PANIC_NAVIGATION_SPEED = 1.95D;

    private int callCooldown = 0;
    private int alarmTicks = 0;
    @Nullable
    private Vec3 fleeFrom = null;

    private boolean isLeader;
    @Nullable
    private UUID herdLeaderUUID;
    private int herdCheckCooldown = 0;

    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;

    private float awareness;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState lookAroundState = new AnimationState();
    public final AnimationState eatingState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();

    public int globalCooldown = 0;

    static {
        DATA_TYPE_ID = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.INT);
        DATA_FLAGS_ID = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.INT);
        DATA_WHITE = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
        DATA_SLEEPING = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    }

    public DeerEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.27D).add(Attributes.MAX_HEALTH, 10.0D).add(Attributes.ATTACK_DAMAGE, 1.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE_ID, 0);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_WHITE, false);
        builder.define(DATA_SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        int goalPriority = 0;
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerAvoidEntityGoal<>(this, Villager.class));
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerAvoidEntityGoal<>(this, Pillager.class));
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerAvoidEntityGoal<>(this, Player.class));
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerSeekShelterGoal(this));
        this.goalSelector.addGoal(++goalPriority, new FloatGoal(this));
        this.goalSelector.addGoal(++goalPriority, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(++goalPriority, new TemptGoal(this, 1.2D, Ingredient.of(Items.SHORT_GRASS), false));
        this.goalSelector.addGoal(++goalPriority, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerFollowLeaderGoal(this));
        this.goalSelector.addGoal(++goalPriority, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DeerEntity.this.isSleeping() && !DeerEntity.this.isDeerRunning();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !DeerEntity.this.isSleeping() && !DeerEntity.this.isDeerRunning();
            }
        });
        this.goalSelector.addGoal(++goalPriority, new LookAtPlayerGoal(this, Player.class, 3.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && !DeerEntity.this.isSleeping();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !DeerEntity.this.isSleeping();
            }
        });
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerEatingGoal(this));
        this.goalSelector.addGoal(++goalPriority, new DeerGoals.DeerLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (this.random.nextFloat() < 0.01F) {
            this.entityData.set(DATA_WHITE, true);
        }
        this.isLeader = this.random.nextFloat() < 0.3F;
        return out;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("White", this.isWhite());
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putBoolean("IsLeader", this.isLeader);
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        tag.putInt("GlobalCooldown", this.globalCooldown);
        tag.putInt("CallCooldown", this.callCooldown);
        tag.putInt("AlarmTicks", this.alarmTicks);
        tag.putFloat("Awareness", this.awareness);
        if (this.herdLeaderUUID != null) {
            tag.putUUID("HerdLeaderUUID", this.herdLeaderUUID);
        }
        if (this.fleeFrom != null) {
            tag.putDouble("FleeFromX", this.fleeFrom.x);
            tag.putDouble("FleeFromY", this.fleeFrom.y);
            tag.putDouble("FleeFromZ", this.fleeFrom.z);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("White")) {
            this.entityData.set(DATA_WHITE, tag.getBoolean("White"));
        }
        if (tag.contains("Sleeping")) {
            this.entityData.set(DATA_SLEEPING, tag.getBoolean("Sleeping"));
        }
        this.isLeader = tag.getBoolean("IsLeader");
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks") ? tag.getInt("RequiredSleepPreparationTicks") : 100 + this.random.nextInt(120);
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.globalCooldown = tag.getInt("GlobalCooldown");
        this.callCooldown = tag.getInt("CallCooldown");
        this.alarmTicks = tag.getInt("AlarmTicks");
        this.awareness = tag.getFloat("Awareness");
        this.herdLeaderUUID = tag.hasUUID("HerdLeaderUUID") ? tag.getUUID("HerdLeaderUUID") : null;
        if (tag.contains("FleeFromX") && tag.contains("FleeFromY") && tag.contains("FleeFromZ")) {
            this.fleeFrom = new Vec3(tag.getDouble("FleeFromX"), tag.getDouble("FleeFromY"), tag.getDouble("FleeFromZ"));
        } else {
            this.fleeFrom = null;
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateAwareness();
            this.updateSleep();
            this.updateHerd();
        }

        this.updateAnimations();
        this.updatePanic();
    }

    private void updateAwareness() {
        if (this.isSleeping()) {
            this.awareness = 0.0F;
            return;
        }

        Player player = this.level().getNearestPlayer(this, AWARENESS_RADIUS);

        if (player == null || player.isCreative() || player.isSpectator()) {
            this.awareness = Math.max(0.0F, this.awareness - 1.5F);
            return;
        }

        this.awareness = Math.max(0.0F, this.awareness - 1.0F);

        float increase = 0.0F;

        if (!player.isCrouching()) {
            increase += 2.0F;
        } else {
            increase += 0.2F;
        }

        if (player.isSprinting() && this.tickCount % 10 == 0) {
            increase += 30.0F;
        }

        if (player.getMainHandItem().is(Items.BOW) || player.getOffhandItem().is(Items.BOW)) {
            increase += 10.0F;
        }

        if (player.getMainHandItem().is(Items.SHORT_GRASS) || player.getOffhandItem().is(Items.SHORT_GRASS)) {
            increase -= 4.0F;
        }

        this.awareness += increase;
        this.awareness = Math.max(0.0F, Math.min(100.0F, this.awareness));

        if (this.awareness > 40.0F && this.random.nextInt(25) == 0) {
            this.spawnAwarenessParticle();
        }

        if (this.awareness >= 100.0F) {
            this.spawnAlertParticle();
            this.triggerPanic(player.position());
            this.awareness = 0.0F;
        }
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        boolean isNight = !this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.002D;
        boolean isOnGrassBlock = this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK);

        if (isNight && !this.isDeerRunning() && isStill && this.sleepCooldownTicks <= 0 && isOnGrassBlock) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks && !this.isSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 25);
            }
        }

        if (this.isSleeping()) {
            if (this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
                Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
                if (player != null && !player.isCreative() && !player.isSpectator()) {
                    this.spawnAlertParticle();
                    this.triggerPanic(player.position());
                }
            }

            if (this.level() instanceof ServerLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                this.spawnSleepingParticle();
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void updateHerd() {
        if (this.herdCheckCooldown > 0) {
            this.herdCheckCooldown--;
            return;
        }
        this.herdCheckCooldown = HERD_CHECK_INTERVAL_TICKS;

        if (!this.isLeader) {
            if (this.herdLeaderUUID != null && this.getLeader() == null) {
                this.herdLeaderUUID = null;
            }

            if (this.herdLeaderUUID == null) {
                DeerEntity nearest = this.level().getNearestEntity(
                        this.level().getEntitiesOfClass(DeerEntity.class, this.getBoundingBox().inflate(HERD_SEARCH_RADIUS)),
                        TargetingConditions.forNonCombat(), this, this.getX(), this.getY(), this.getZ()
                );
                if (nearest != null && nearest != this && nearest.isLeader()) {
                    this.herdLeaderUUID = nearest.getUUID();
                }
            }
        }
    }

    private void updateAnimations() {
        if (this.globalCooldown > 0) {
            this.globalCooldown--;
        }

        this.eatingState.animateWhen(this.isEating(), this.tickCount);
        this.lookAroundState.animateWhen(this.isLookingAround(), this.tickCount);
        this.sleepState.animateWhen(this.isSleeping(), this.tickCount);

        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }
    }

    private void updatePanic() {
        if (this.callCooldown > 0) {
            this.callCooldown--;
        }

        if (this.alarmTicks > 0) {
            this.alarmTicks--;

            if (this.fleeFrom != null && !this.level().isClientSide) {
                Vec3 fleeDirection = this.position().subtract(this.fleeFrom);

                if (fleeDirection.lengthSqr() > 0.001D) {
                    Vec3 normalizedDirection = fleeDirection.normalize();
                    Vec3 targetPosition = this.position().add(normalizedDirection.scale(11.0D + this.random.nextDouble() * 7.0D)).add((this.random.nextDouble() - 0.5D) * 9.0D, 0.0D, (this.random.nextDouble() - 0.5D) * 9.0D);
                    this.getNavigation().moveTo(targetPosition.x, targetPosition.y, targetPosition.z, PANIC_NAVIGATION_SPEED);
                }
            }

            if (this.alarmTicks == 0) {
                this.stopRunningAnim();
                this.fleeFrom = null;
                this.awareness = 0.0F;
            }
        }
    }

    public void spawnAlertParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), this.getX(), this.getY() + this.getBbHeight() + 0.25D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnAwarenessParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.QUESTION.get(), this.getX(), this.getY() + this.getBbHeight() + 0.25D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void spawnSleepingParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D, this.getY() + this.getBbHeight() * 0.75D, this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private boolean hasWakeUpTriggerNearby() {
        Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (player == null) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        return !player.isCrouching();
    }

    public void triggerPanic(Vec3 threatPos) {
        this.stopEating();
        this.stopLookingAround();
        this.wakeUp();
        this.startRunningAnim();
        this.alarmTicks = PANIC_DURATION_TICKS;
        this.fleeFrom = threatPos;
        this.awareness = 0.0F;

        for (DeerEntity deer : this.level().getEntitiesOfClass(DeerEntity.class, this.getBoundingBox().inflate(PANIC_SHARE_RADIUS))) {
            if (deer == this) {
                continue;
            }

            deer.stopEating();
            deer.stopLookingAround();
            deer.wakeUp();
            deer.startRunningAnim();
            deer.alarmTicks = PANIC_DURATION_TICKS - 10 + deer.random.nextInt(30);
            deer.awareness = 0.0F;
            deer.fleeFrom = threatPos.add((deer.random.nextDouble() - 0.5D) * 7.0D, 0.0D, (deer.random.nextDouble() - 0.5D) * 7.0D);
        }
    }

    private void startSleeping() {
        this.stopEating();
        this.stopLookingAround();
        this.setSleeping(true);
        this.awareness = 0.0F;
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public boolean isWhite() {
        return this.entityData.get(DATA_WHITE);
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    private void wakeUp() {
        if (this.isSleeping()) {
            this.setSleeping(false);
            this.sleepCooldownTicks = 200 + this.random.nextInt(200);
        }
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    @Nullable
    public DeerEntity getLeader() {
        if (this.isLeader) {
            return this;
        }

        if (this.herdLeaderUUID == null) {
            return null;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.herdLeaderUUID);
            if (entity instanceof DeerEntity deer) {
                return deer;
            }
        }

        return null;
    }

    public boolean isLeader() {
        return this.isLeader;
    }

    public int getGlobalCooldown() {
        return this.globalCooldown;
    }

    public void setGlobalCooldown(int globalCooldown) {
        this.globalCooldown = globalCooldown;
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) | flag);
        } else {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) & ~flag);
        }
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS_ID) & flag) != 0;
    }

    public boolean isDeerRunning() {
        return getFlag(FLAG_RUNNING);
    }

    public void startRunningAnim() {
        setFlag(FLAG_RUNNING, true);
    }

    public void stopRunningAnim() {
        setFlag(FLAG_RUNNING, false);
    }

    public boolean isEating() {
        return getFlag(FLAG_EATING);
    }

    public void startEating() {
        setFlag(FLAG_EATING, true);
    }

    public void stopEating() {
        setFlag(FLAG_EATING, false);
    }

    public boolean isLookingAround() {
        return getFlag(FLAG_LOOKING_AROUND);
    }

    public void startLookingAround() {
        setFlag(FLAG_LOOKING_AROUND, true);
    }

    public void stopLookingAround() {
        setFlag(FLAG_LOOKING_AROUND, false);
    }

    public void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isDeerRunning() && !this.isEating() && !this.isLookingAround() && !this.isSleeping();

        if (idleAllowed) {
            this.idleState.startIfStopped(this.tickCount);
        } else {
            this.idleState.stop();
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SHORT_GRASS);
    }

    @Nullable
    @Override
    public DeerEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.DEER.get().create(serverLevel);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.DEER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.DEER_DEATH.get();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);

        if (!this.isWhite()) {
            return;
        }

        Entity killer = source.getEntity();
        if (killer instanceof Projectile projectile) {
            killer = projectile.getOwner();
        }

        if (killer instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6000, 1));
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity threat = damageSource.getEntity();
        this.broadcastCall(threat);
        return super.hurt(damageSource, amount);
    }

    private void broadcastCall(@Nullable Entity threat) {
        if (this.callCooldown > 0) {
            return;
        }

        if (this.level().isClientSide) {
            return;
        }

        this.callCooldown = CALL_COOLDOWN_TICKS;
        SoundEvent soundEvent = this.getCallSound();
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0F, 1.0F);

        for (DeerEntity deer : this.level().getEntitiesOfClass(DeerEntity.class, this.getBoundingBox().inflate(CALL_RADIUS))) {
            if (deer != this) {
                deer.onHeardCall(this, threat);
            }
        }
    }

    private void onHeardCall(DeerEntity source, @Nullable Entity threat) {
        if (threat instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }
        this.triggerPanic(threat != null ? threat.position() : source.position());
    }

    protected SoundEvent getCallSound() {
        return SoundRegistry.DEER_HURT.get();
    }
}