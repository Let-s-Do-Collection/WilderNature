package net.satisfy.wildernature.core.entity.animal.defensive;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CassowaryEntity extends Animal {
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(CassowaryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALERT = SynchedEntityData.defineId(CassowaryEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> THREATENING = SynchedEntityData.defineId(CassowaryEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_DURATION = 27;
    private static final int ATTACK_HIT_TICK = 12;
    private static final int ALERT_DURATION = 24;
    private static final int THREATENING_DURATION = 28;
    private static final int CALM_DOWN_DURATION = 60;
    private static final int ALERT_PARTICLE_INTERVAL = 8;
    private static final int BABY_PROTECT_CACHE_DURATION = 20;
    private static final int THREAT_SOUND_COOLDOWN = 32;
    private static final double TERRITORIAL_RANGE = 6.0D;
    private static final double BABY_PROTECT_RANGE = 11.0D;
    private static final double THREATENING_MOVE_RANGE = 4.0D;
    private static final double CHASE_BREAK_RANGE = 18.0D;

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState alertAnimationState = new AnimationState();
    public final AnimationState threateningAnimationState = new AnimationState();

    private int attackAnimationTicks;
    private int alertTicks;
    private int threateningTicks;
    private int calmDownTicks;
    private int babyProtectCacheTicks;
    private int threatSoundCooldownTicks;
    private boolean nearbyBabyCached;
    private boolean attackHitPending;

    public CassowaryEntity(EntityType<? extends CassowaryEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 16.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.75D)
                .add(Attributes.ATTACK_SPEED, 1.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.attackAnimationTicks > 0) {
                this.attackAnimationTicks--;

                if (this.attackHitPending && this.attackAnimationTicks == ATTACK_DURATION - ATTACK_HIT_TICK) {
                    this.attackHitPending = false;
                    Entity currentTarget = this.getTarget();
                    if (currentTarget != null && currentTarget.isAlive() && this.distanceToSqr(currentTarget) <= 6.25D) {
                        this.doHurtTarget(currentTarget);
                    }
                }

                if (this.attackAnimationTicks <= 0) {
                    this.setAttacking(false);
                    this.attackHitPending = false;
                }
            }

            this.setAttacking(this.attackAnimationTicks > 0);
            this.updateThreatBehavior();
        } else {
            this.setupAnimationStates();
        }
    }

    private void updateThreatBehavior() {
        if (this.calmDownTicks > 0) {
            this.calmDownTicks--;
        }

        if (this.babyProtectCacheTicks > 0) {
            this.babyProtectCacheTicks--;
        } else {
            this.computeNearbyBabyCassowary();
            this.babyProtectCacheTicks = BABY_PROTECT_CACHE_DURATION;
        }

        if (this.threatSoundCooldownTicks > 0) {
            this.threatSoundCooldownTicks--;
        }

        Player currentTargetPlayer = this.getTarget() instanceof Player player ? player : null;
        if (currentTargetPlayer != null) {
            if (!this.isValidTargetPlayer(currentTargetPlayer) || this.distanceToSqr(currentTargetPlayer) > CHASE_BREAK_RANGE * CHASE_BREAK_RANGE) {
                this.setTarget(null);
                this.stopThreatStates();
                this.calmDownTicks = CALM_DOWN_DURATION;
                return;
            }

            this.stopThreatStates();
            return;
        }

        if (this.calmDownTicks > 0) {
            this.stopThreatStates();
            return;
        }

        Player nearbyThreatPlayer = this.findNearestThreatPlayer();
        if (nearbyThreatPlayer == null) {
            this.stopThreatStates();
            return;
        }

        if (this.alertTicks <= 0 && this.threateningTicks <= 0) {
            this.alertTicks = ALERT_DURATION;
            this.setAlert(true);
            this.setThreatening(false);
            this.getNavigation().stop();
            this.getLookControl().setLookAt(nearbyThreatPlayer, 30.0F, 30.0F);
            this.spawnAlertParticle();
            this.playThreatWarningSound();
            return;
        }

        if (this.alertTicks > 0) {
            this.alertTicks--;
            this.setAlert(true);
            this.setThreatening(false);
            this.getNavigation().stop();
            this.getLookControl().setLookAt(nearbyThreatPlayer, 30.0F, 30.0F);

            if (this.tickCount % ALERT_PARTICLE_INTERVAL == 0) {
                this.spawnAlertParticle();
            }

            if (this.alertTicks <= 0) {
                this.threateningTicks = THREATENING_DURATION;
                this.setAlert(false);
                this.setThreatening(true);
                this.playThreatWarningSound();
            }
            return;
        }

        this.threateningTicks--;
        this.setAlert(false);
        this.setThreatening(true);

        if (this.tickCount % 5 == 0) {
            this.getLookControl().setLookAt(nearbyThreatPlayer, 30.0F, 30.0F);
        }

        if (this.tickCount % ALERT_PARTICLE_INTERVAL == 0) {
            this.spawnAlertParticle();
        }

        if (this.threatSoundCooldownTicks <= 0) {
            this.playThreatWarningSound();
        }

        if (this.distanceToSqr(nearbyThreatPlayer) > THREATENING_MOVE_RANGE * THREATENING_MOVE_RANGE) {
            this.getNavigation().moveTo(nearbyThreatPlayer, 0.9D);
        } else {
            this.getNavigation().stop();
        }

        if (this.threateningTicks <= 0) {
            this.setThreatening(false);
            this.setTarget(nearbyThreatPlayer);
        }
    }

    private void playThreatWarningSound() {
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.CASSOWARY_AMBIENT.get(), SoundSource.HOSTILE, 1.1F, 0.7F + this.random.nextFloat() * 0.1F);
        this.threatSoundCooldownTicks = THREAT_SOUND_COOLDOWN;
    }

    private void computeNearbyBabyCassowary() {
        this.nearbyBabyCached = false;
        List<CassowaryEntity> cassowaryList = this.level().getEntitiesOfClass(CassowaryEntity.class, this.getBoundingBox().inflate(8.0D));
        for (CassowaryEntity cassowaryEntity : cassowaryList) {
            if (cassowaryEntity != this && cassowaryEntity.isBaby()) {
                this.nearbyBabyCached = true;
                return;
            }
        }
    }

    @Nullable
    private Player findNearestThreatPlayer() {
        double detectionRange = this.nearbyBabyCached ? BABY_PROTECT_RANGE : TERRITORIAL_RANGE;
        Player nearestPlayer = this.level().getNearestPlayer(this, detectionRange);
        if (!this.isValidTargetPlayer(nearestPlayer)) {
            return null;
        }
        return nearestPlayer;
    }

    private boolean isValidTargetPlayer(@Nullable Player player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    private void stopThreatStates() {
        this.alertTicks = 0;
        this.threateningTicks = 0;
        this.setAlert(false);
        this.setThreatening(false);
    }

    private void spawnAlertParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), this.getX(), this.getY() + this.getBbHeight() + 0.25D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isAttacking() && !this.isAlert() && !this.isThreatening();

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.alertAnimationState.animateWhen(this.isAlert(), this.tickCount);
        this.threateningAnimationState.animateWhen(this.isThreatening(), this.tickCount);
        this.attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float walkSpeed = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(walkSpeed, 0.2F);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAlert() {
        return this.entityData.get(ALERT);
    }

    public void setAlert(boolean alert) {
        this.entityData.set(ALERT, alert);
    }

    public boolean isThreatening() {
        return this.entityData.get(THREATENING);
    }

    public void setThreatening(boolean threatening) {
        this.entityData.set(THREATENING, threatening);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ALERT, false);
        builder.define(THREATENING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, true) {
            @Override
            public boolean canUse() {
                return CassowaryEntity.this.level().getDifficulty() != Difficulty.PEACEFUL && CassowaryEntity.this.attackAnimationTicks <= 0 && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return CassowaryEntity.this.level().getDifficulty() != Difficulty.PEACEFUL && super.canContinueToUse();
            }

            @Override
            protected void checkAndPerformAttack(net.minecraft.world.entity.LivingEntity livingEntity) {
                if (this.canPerformAttack(livingEntity) && CassowaryEntity.this.attackAnimationTicks <= 0) {
                    this.resetAttackCooldown();
                    CassowaryEntity.this.attackAnimationTicks = ATTACK_DURATION;
                    CassowaryEntity.this.attackHitPending = true;
                    CassowaryEntity.this.setAttacking(true);
                }
            }
        });
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !CassowaryEntity.this.isAlert() && !CassowaryEntity.this.isThreatening() && CassowaryEntity.this.getTarget() == null && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return CassowaryEntity.this.level().getDifficulty() != Difficulty.PEACEFUL && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return CassowaryEntity.this.level().getDifficulty() != Difficulty.PEACEFUL && super.canContinueToUse();
            }
        });
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean success = super.doHurtTarget(entity);

        if (success && entity instanceof Player player) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
        }

        return success;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        boolean wasHurt = super.hurt(damageSource, damageAmount);
        if (wasHurt) {
            Entity sourceEntity = damageSource.getEntity();
            if (sourceEntity instanceof Player player && this.isValidTargetPlayer(player)) {
                this.stopThreatStates();
                this.setTarget(player);
                this.calmDownTicks = 0;
            }
        }
        return wasHurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.CASSOWARY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.CASSOWARY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.CASSOWARY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    @Nullable
    public CassowaryEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.CASSOWARY.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT_SEEDS) || stack.is(Items.MELON_SEEDS) || stack.is(Items.PUMPKIN_SEEDS) || stack.is(Items.BEETROOT_SEEDS);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Attacking", this.isAttacking());
        compound.putBoolean("Alert", this.isAlert());
        compound.putBoolean("Threatening", this.isThreatening());
        compound.putInt("AttackAnimationTicks", this.attackAnimationTicks);
        compound.putInt("AlertTicks", this.alertTicks);
        compound.putInt("ThreateningTicks", this.threateningTicks);
        compound.putInt("CalmDownTicks", this.calmDownTicks);
        compound.putInt("BabyProtectCacheTicks", this.babyProtectCacheTicks);
        compound.putBoolean("NearbyBabyCached", this.nearbyBabyCached);
        compound.putInt("ThreatSoundCooldownTicks", this.threatSoundCooldownTicks);
        compound.putBoolean("AttackHitPending", this.attackHitPending);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setAttacking(compound.getBoolean("Attacking"));
        this.setAlert(compound.getBoolean("Alert"));
        this.setThreatening(compound.getBoolean("Threatening"));
        this.attackAnimationTicks = compound.getInt("AttackAnimationTicks");
        this.alertTicks = compound.getInt("AlertTicks");
        this.threateningTicks = compound.getInt("ThreateningTicks");
        this.calmDownTicks = compound.getInt("CalmDownTicks");
        this.babyProtectCacheTicks = compound.getInt("BabyProtectCacheTicks");
        this.nearbyBabyCached = compound.getBoolean("NearbyBabyCached");
        this.threatSoundCooldownTicks = compound.getInt("ThreatSoundCooldownTicks");
        this.attackHitPending = compound.getBoolean("AttackHitPending");
        this.setAttacking(this.attackAnimationTicks > 0);
    }
}