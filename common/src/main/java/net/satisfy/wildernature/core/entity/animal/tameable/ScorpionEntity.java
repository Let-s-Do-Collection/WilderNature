package net.satisfy.wildernature.core.entity.animal.tameable;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.satisfy.wildernature.core.entity.ai.goal.animal.ScorpionGoals;
import net.satisfy.wildernature.core.registry.MobEffectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScorpionEntity extends TamableAnimal {
    public static final int POISON_DURATION = 200;
    public static final double AGGRO_RADIUS = 4.0D;
    public static final int BURROW_COOLDOWN_MIN = 200;
    public static final int BURROW_COOLDOWN_MAX = 400;
    public static final int BURROW_DURATION_MIN = 100;
    public static final int BURROW_DURATION_MAX = 300;
    public static final int MAX_TRUST = 5;
    public static final int TRUST_COOLDOWN_TICKS = 200;
    public static final int BURROW_ANIMATION_TICKS = 14;
    public static final float BURROW_DEPTH = 0.92F;
    public static final int BURROW_STATE_NONE = 0;
    public static final int BURROW_STATE_ENTERING = 1;
    public static final int BURROW_STATE_BURIED = 2;
    public static final int BURROW_STATE_EXITING = 3;

    private static final EntityDataAccessor<Integer> DATA_BURROW_STATE = SynchedEntityData.defineId(ScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_BURROW_PROGRESS = SynchedEntityData.defineId(ScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRUST = SynchedEntityData.defineId(ScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRUST_COOLDOWN = SynchedEntityData.defineId(ScorpionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CALMED = SynchedEntityData.defineId(ScorpionEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState walkState = new AnimationState();
    public final AnimationState attackState = new AnimationState();
    public final AnimationState idleState = new AnimationState();
    public final AnimationState burrowState = new AnimationState();

    private int burrowCooldown;
    private int burrowDuration;
    private UUID trustedPlayerUuid;
    private int attackCooldown;
    private int retreatTicks;
    private double burrowOriginY;

    public ScorpionEntity(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BURROW_STATE, BURROW_STATE_NONE);
        builder.define(DATA_BURROW_PROGRESS, 0);
        builder.define(DATA_TRUST, 0);
        builder.define(DATA_TRUST_COOLDOWN, 0);
        builder.define(DATA_CALMED, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false) {
            @Override
            public boolean canUse() {
                return !ScorpionEntity.this.isBurrowBusy() && !ScorpionEntity.this.isOrderedToSit() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !ScorpionEntity.this.isBurrowBusy() && !ScorpionEntity.this.isOrderedToSit() && super.canContinueToUse();
            }

            @Override
            protected void checkAndPerformAttack(LivingEntity target) {
                if (this.isTimeToAttack() && this.mob.distanceToSqr(target) <= 1.0D) {
                    this.resetAttackCooldown();
                    this.mob.swing(InteractionHand.MAIN_HAND);
                    this.mob.doHurtTarget(target);
                }
            }
        });

        this.goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.0D, 6.0F, 2.0F) {
            @Override
            public boolean canUse() {
                return !ScorpionEntity.this.isBurrowBusy() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !ScorpionEntity.this.isBurrowBusy() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(4, new ScorpionGoals.ScorpionBurrowGoal(this));

        this.goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8D) {
            @Override
            public boolean canUse() {
                return !ScorpionEntity.this.isBurrowBusy() && !ScorpionEntity.this.isOrderedToSit() && super.canUse();
            }
        });

        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !ScorpionEntity.this.isBurrowBusy() && !ScorpionEntity.this.isOrderedToSit() && super.canUse();
            }
        });

        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !ScorpionEntity.this.isBurrowBusy() && !ScorpionEntity.this.isOrderedToSit() && super.canUse();
            }
        });

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(4, new ScorpionGoals.ScorpionProximityTargetGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.burrowCooldown > 0) {
                this.burrowCooldown--;
            }

            int trustCooldown = this.entityData.get(DATA_TRUST_COOLDOWN);
            if (trustCooldown > 0) {
                this.entityData.set(DATA_TRUST_COOLDOWN, trustCooldown - 1);
            }

            this.tickBurrowStateServer();
            this.tickCombatStateServer();
        }

        this.tickBurrowMotionClientAndServer();

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        this.walkState.animateWhen(moving && !this.isBurrowBusy(), this.tickCount);
        this.idleState.animateWhen(!moving && !this.isBurrowBusy(), this.tickCount);
        this.attackState.animateWhen(this.swinging && !this.isBurrowBusy(), this.tickCount);

        if (this.isBurrowBusy()) {
            this.burrowState.startIfStopped(this.tickCount);
        } else {
            this.burrowState.stop();
        }
    }

    private void tickCombatStateServer() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive() || this.isBurrowBusy()) {
            this.attackCooldown = 0;
            this.retreatTicks = 0;
            return;
        }

        double distanceToTarget = this.distanceToSqr(target);
        if (this.attackCooldown <= 0 && distanceToTarget <= 2.0D) {
            this.doHurtTarget(target);
            this.attackCooldown = 70;
            this.retreatTicks = 50;

            Vec3 retreatDirection = new Vec3(this.getX() - target.getX(), 0.0D, this.getZ() - target.getZ());
            if (retreatDirection.lengthSqr() > 1.0E-6D) {
                Vec3 normalizedDirection = retreatDirection.normalize().scale(0.8D);
                this.setDeltaMovement(normalizedDirection.x, 0.25D, normalizedDirection.z);
            }
            return;
        }

        if (this.retreatTicks > 0) {
            this.retreatTicks--;
            double deltaX = this.getX() - target.getX();
            double deltaZ = this.getZ() - target.getZ();
            this.getNavigation().moveTo(this.getX() + deltaX * 2.0D, this.getY(), this.getZ() + deltaZ * 2.0D, 1.3D);
            return;
        }

        if (this.attackCooldown > 0) {
            this.attackCooldown--;
        }

        if (this.attackCooldown <= 20) {
            this.getNavigation().moveTo(target, 1.1D);
        }

        if (this.attackCooldown > 30 && this.random.nextFloat() < 0.6F && this.canBurrow()) {
            this.burrow();
        }
    }

    private void tickBurrowStateServer() {
        int burrowState = this.entityData.get(DATA_BURROW_STATE);
        int burrowProgress = this.entityData.get(DATA_BURROW_PROGRESS);

        if (burrowState == BURROW_STATE_ENTERING) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setDeltaMovement(Vec3.ZERO);
            if (burrowProgress == 0) {
                this.spawnBurrowParticles(false);
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
            if (burrowProgress < BURROW_ANIMATION_TICKS) {
                this.entityData.set(DATA_BURROW_PROGRESS, burrowProgress + 1);
            } else {
                this.entityData.set(DATA_BURROW_STATE, BURROW_STATE_BURIED);
                this.burrowDuration = BURROW_DURATION_MIN + this.random.nextInt(BURROW_DURATION_MAX - BURROW_DURATION_MIN + 1);
            }
            return;
        }

        if (burrowState == BURROW_STATE_BURIED) {
            this.getNavigation().stop();
            this.setTarget(null);
            this.setDeltaMovement(Vec3.ZERO);
            if (this.burrowDuration > 0) {
                this.burrowDuration--;
            }
            if (this.burrowDuration <= 0) {
                this.unburrow();
            }
            return;
        }

        if (burrowState == BURROW_STATE_EXITING) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            if (burrowProgress == BURROW_ANIMATION_TICKS) {
                this.spawnBurrowParticles(true);
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
            if (burrowProgress > 0) {
                this.entityData.set(DATA_BURROW_PROGRESS, burrowProgress - 1);
            } else {
                this.entityData.set(DATA_BURROW_STATE, BURROW_STATE_NONE);
                this.burrowCooldown = BURROW_COOLDOWN_MIN + this.random.nextInt(BURROW_COOLDOWN_MAX - BURROW_COOLDOWN_MIN + 1);
                this.attackCooldown = 10;
                this.noPhysics = false;
                this.refreshDimensions();
                this.setPos(this.getX(), this.burrowOriginY, this.getZ());
            }
        }
    }

    private void tickBurrowMotionClientAndServer() {
        int burrowState = this.entityData.get(DATA_BURROW_STATE);
        int burrowProgress = this.entityData.get(DATA_BURROW_PROGRESS);

        if (burrowState == BURROW_STATE_NONE) {
            this.noPhysics = false;
            return;
        }

        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        this.setYRot(this.yBodyRot);
        this.setXRot(0.0F);

        double progressFactor = Math.min(1.0D, burrowProgress / (double) BURROW_ANIMATION_TICKS);
        double buriedY = this.burrowOriginY - BURROW_DEPTH * progressFactor;
        this.setPos(this.getX(), buriedY, this.getZ());
    }

    private void spawnBurrowParticles(boolean emerging) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;
        BlockPos blockPos = this.blockPosition().below();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.isAir()) blockState = this.level().getBlockState(this.blockPosition());
        double particleY = this.getY() + (emerging ? 0.15D : 0.05D);
        int count = emerging ? 48 : 24;
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), this.getX(), particleY, this.getZ(), count, 0.25D, 0.1D, 0.25D, 0.05D);
    }

    private boolean canBurrowInCurrentBlock() {
        BlockState blockState = this.level().getBlockState(this.blockPosition().below());
        return blockState.is(BlockTags.DIRT) || blockState.is(BlockTags.SAND);
    }

    public boolean isBurrowBusy() {
        return this.entityData.get(DATA_BURROW_STATE) != BURROW_STATE_NONE;
    }

    public boolean isBurrowed() {
        int burrowState = this.entityData.get(DATA_BURROW_STATE);
        return burrowState == BURROW_STATE_ENTERING || burrowState == BURROW_STATE_BURIED || burrowState == BURROW_STATE_EXITING;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && target instanceof LivingEntity livingEntity) {
            Holder<MobEffect> neurotoxin = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffectRegistry.NEUROTOXIN.get());
            MobEffectInstance existingEffect = livingEntity.getEffect(neurotoxin);
            int amplifier = existingEffect == null ? 0 : Math.min(2, existingEffect.getAmplifier() + 1);
            livingEntity.addEffect(new MobEffectInstance(neurotoxin, POISON_DURATION, amplifier));
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt) {
            this.unburrow();

            int trustLevel = this.entityData.get(DATA_TRUST);
            if (trustLevel > 0) {
                this.entityData.set(DATA_TRUST, Math.max(0, trustLevel - 2));
            }

            this.entityData.set(DATA_CALMED, false);

            if (source.getEntity() instanceof LivingEntity attacker && !this.isOwnedBy(attacker)) {
                this.setTarget(attacker);
            }
        }
        return wasHurt;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!this.isTame()) {
            if (this.canFinalTame(player) && itemStack.is(Items.FERMENTED_SPIDER_EYE)) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    this.finalizeTame(player);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (this.isFood(itemStack)) {
                if (!this.level().isClientSide()) {
                    if (this.tryIncreaseTrust(player)) {
                        if (!player.getAbilities().instabuild) {
                            itemStack.shrink(1);
                        }
                        this.level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            return super.mobInteract(player, hand);
        }

        if (this.isOwnedBy(player)) {
            if (player.isSecondaryUseActive()) {
                this.setOrderedToSit(!this.isOrderedToSit());
                this.jumping = false;
                this.getNavigation().stop();
                this.setTarget(null);
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    this.heal(4.0F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }
        }

        return super.mobInteract(player, hand);
    }

    public boolean tryIncreaseTrust(Player player) {
        if (this.entityData.get(DATA_TRUST_COOLDOWN) > 0) {
            return false;
        }
        if (!player.isShiftKeyDown()) {
            return false;
        }
        if (this.distanceTo(player) < 2.5D) {
            return false;
        }
        if (this.getTarget() != null) {
            return false;
        }

        if (this.trustedPlayerUuid == null) {
            this.trustedPlayerUuid = player.getUUID();
        }
        if (!this.trustedPlayerUuid.equals(player.getUUID())) {
            return false;
        }

        int trustLevel = this.entityData.get(DATA_TRUST);
        if (trustLevel < MAX_TRUST) {
            trustLevel++;
            this.entityData.set(DATA_TRUST, trustLevel);
            this.entityData.set(DATA_TRUST_COOLDOWN, TRUST_COOLDOWN_TICKS);
        }

        if (trustLevel >= MAX_TRUST) {
            this.entityData.set(DATA_CALMED, true);
            this.setTarget(null);
        }

        return true;
    }

    public boolean canFinalTame(Player player) {
        return this.isCalmed() && this.trustedPlayerUuid != null && this.trustedPlayerUuid.equals(player.getUUID());
    }

    public void finalizeTame(Player player) {
        this.tame(player);
        this.setOrderedToSit(true);
        this.getNavigation().stop();
        this.setTarget(null);
    }

    public void burrow() {
        if (!this.canBurrow()) {
            return;
        }

        this.burrowOriginY = this.getY();
        this.entityData.set(DATA_BURROW_PROGRESS, 0);
        this.entityData.set(DATA_BURROW_STATE, BURROW_STATE_ENTERING);
        this.burrowDuration = 0;
        this.getNavigation().stop();
        this.setTarget(null);
    }

    public void unburrow() {
        int burrowState = this.entityData.get(DATA_BURROW_STATE);
        if (burrowState == BURROW_STATE_NONE || burrowState == BURROW_STATE_EXITING) {
            return;
        }

        if (burrowState == BURROW_STATE_ENTERING && this.entityData.get(DATA_BURROW_PROGRESS) <= 0) {
            this.entityData.set(DATA_BURROW_STATE, BURROW_STATE_NONE);
            this.entityData.set(DATA_BURROW_PROGRESS, 0);
            this.noPhysics = false;
            this.refreshDimensions();
            this.setPos(this.getX(), this.burrowOriginY, this.getZ());
            return;
        }

        this.entityData.set(DATA_BURROW_STATE, BURROW_STATE_EXITING);
        if (this.entityData.get(DATA_BURROW_PROGRESS) <= 0) {
            this.entityData.set(DATA_BURROW_PROGRESS, BURROW_ANIMATION_TICKS);
        }
        this.burrowDuration = 0;
    }

    public boolean canBurrow() {
        return this.burrowCooldown <= 0 && !this.isBurrowBusy() && this.getTarget() == null && !this.isTame() && !this.isOrderedToSit() && this.onGround() && this.canBurrowInCurrentBlock();
    }

    public boolean isCalmed() {
        return this.entityData.get(DATA_CALMED);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (this.isCalmed() && target instanceof Player player) {
            return !player.getUUID().equals(this.trustedPlayerUuid);
        }
        return target != owner && !this.isOwnedBy(target);
    }

    @Override
    public boolean shouldTryTeleportToOwner() {
        return !this.isOrderedToSit() && !this.isBurrowBusy();
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SPIDER_EYE) || itemStack.is(Items.ROTTEN_FLESH);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SPIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    @Override
    public boolean isPushable() {
        return !this.isBurrowBusy();
    }

    @Override
    public boolean isPickable() {
        return !this.isBurrowBusy();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BurrowState", this.entityData.get(DATA_BURROW_STATE));
        tag.putInt("BurrowProgress", this.entityData.get(DATA_BURROW_PROGRESS));
        tag.putInt("BurrowCooldown", this.burrowCooldown);
        tag.putInt("BurrowDuration", this.burrowDuration);
        tag.putDouble("BurrowOriginY", this.burrowOriginY);
        tag.putInt("Trust", this.entityData.get(DATA_TRUST));
        tag.putInt("TrustCooldown", this.entityData.get(DATA_TRUST_COOLDOWN));
        tag.putBoolean("Calmed", this.entityData.get(DATA_CALMED));

        if (this.trustedPlayerUuid != null) {
            tag.putUUID("TrustedPlayer", this.trustedPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_BURROW_STATE, tag.getInt("BurrowState"));
        this.entityData.set(DATA_BURROW_PROGRESS, tag.getInt("BurrowProgress"));
        this.burrowCooldown = tag.getInt("BurrowCooldown");
        this.burrowDuration = tag.getInt("BurrowDuration");
        this.burrowOriginY = tag.contains("BurrowOriginY") ? tag.getDouble("BurrowOriginY") : this.getY();
        this.entityData.set(DATA_TRUST, tag.getInt("Trust"));
        this.entityData.set(DATA_TRUST_COOLDOWN, tag.getInt("TrustCooldown"));
        this.entityData.set(DATA_CALMED, tag.getBoolean("Calmed"));

        if (tag.hasUUID("TrustedPlayer")) {
            this.trustedPlayerUuid = tag.getUUID("TrustedPlayer");
        } else {
            this.trustedPlayerUuid = null;
        }
    }

    @Nullable
    @Override
    public ScorpionEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    public static boolean checkScorpionSpawnRules(EntityType<ScorpionEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos blockPos, RandomSource randomSource) {
        BlockState blockState = level.getBlockState(blockPos.below());
        return (blockState.is(BlockTags.SAND) || blockState.is(BlockTags.DIRT) || blockState.is(BlockTags.BASE_STONE_OVERWORLD)) && level.getRawBrightness(blockPos, 0) > 7 && checkMobSpawnRules(entityType, level, spawnType, blockPos, randomSource);
    }
}