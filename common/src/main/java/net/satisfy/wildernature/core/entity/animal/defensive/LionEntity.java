package net.satisfy.wildernature.core.entity.animal.defensive;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.goal.FollowParentAtDistanceGoal;
import net.satisfy.wildernature.core.entity.ai.goal.animal.LionGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LionEntity extends Animal {
    private static final int FLAG_SLEEPING = 0x01;
    private static final int FLAG_STALKING = 0x02;
    private static final int FLAG_POUNCING = 0x04;
    private static final int FLAG_WARNING = 0x08;
    private static final int FLAG_ROARING = 0x10;

    public static final double TERRITORY_BREAK_RADIUS = 38.0D;
    public static final double ALERT_RADIUS = 18.0D;
    public static final double AGGRO_PLAYER_RADIUS = 16.0D;
    public static final double BABY_PROTECT_RADIUS = 10.0D;
    public static final double BABY_PROTECT_TRIGGER = 6.0D;
    private static final double WAKE_UP_RADIUS = 8.0D;

    public static final int POUNCE_WINDUP_TICKS = 12;
    public static final int POUNCE_ACTIVE_TICKS = 6;
    public static final int POUNCE_RECOVERY_TICKS = 14;
    public static final int POUNCE_COOLDOWN_MIN = 80;
    public static final int POUNCE_COOLDOWN_RANDOM = 40;
    public static final float POUNCE_DAMAGE = 10.0F;

    public static final int WARN_DURATION_TICKS = 40;
    public static final int ROAR_DURATION_TICKS = 30;
    public static final int SLEEP_COOLDOWN_MIN = 400;
    public static final int SLEEP_COOLDOWN_RANDOM = 400;
    public static final int ATTACK_COOLDOWN_TICKS = 60;

    private static final EntityDataAccessor<Integer> DATA_FLAGS = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_MALE = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_THREAT = SynchedEntityData.defineId(LionEntity.class, EntityDataSerializers.INT);

    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();
    public final AnimationState stalkState = new AnimationState();
    public final AnimationState pounceState = new AnimationState();
    public final AnimationState warnState = new AnimationState();
    public final AnimationState roarState = new AnimationState();

    public int pounceWindupTicks;
    public int pounceActiveTicks;
    public int pounceRecoveryTicks;

    private boolean pounceLeapDone;
    private boolean pounceImpactDone;

    private int pounceCooldownTicks;
    private int warnTicks;
    private int roarTicks;
    private int sleepCooldownTicks;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepDurationTicks;
    private int attackCooldownTicks;

    @Nullable
    private BlockPos prideCenterPos;

    public LionEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(180);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, 0);
        builder.define(DATA_MALE, false);
        builder.define(DATA_THREAT, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new LionGoals.LionPounceGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            public boolean canUse() {
                return LionEntity.this.canUseCombatGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return LionEntity.this.canUseCombatGoal() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new LionGoals.LionWarnGoal(this));
        this.goalSelector.addGoal(4, new LionGoals.LionSleepGoal(this));
        this.goalSelector.addGoal(5, new LionGoals.LionStalkGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new LionGoals.ReturnToPrideCenterGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new FollowParentAtDistanceGoal(this, 0.8D));
        this.goalSelector.addGoal(9, new TemptGoal(this, 1.0D, Ingredient.of(Items.BEEF), false));
        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.65D, 120) {
            @Override
            public boolean canUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && LionEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !LionEntity.this.isSleeping() && !LionEntity.this.isBusy() && super.canContinueToUse();
            }
        });

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                super.start();
                if (LionEntity.this.getTarget() instanceof Player player) {
                    LionEntity.this.alertPride(player);
                    LionEntity.this.triggerRoar();
                }
            }
        });
        this.targetSelector.addGoal(2, new LionGoals.LionHuntGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateThreatLevel();
            this.updateTimers();
            this.updatePounceMotion();
            this.limitPursuitRange();
            this.updateBabyProtection();
            this.updateSleepState();
        }

        this.updateAnimationStates();
    }

    private void updateTimers() {
        if (this.pounceWindupTicks > 0) {
            this.pounceWindupTicks--;
            if (this.pounceWindupTicks == 0) {
                this.pounceActiveTicks = POUNCE_ACTIVE_TICKS;
                this.pounceLeapDone = false;
                this.pounceImpactDone = false;
            }
        }

        if (this.pounceActiveTicks > 0) {
            this.pounceActiveTicks--;
            if (this.pounceActiveTicks == 0) {
                this.pounceRecoveryTicks = POUNCE_RECOVERY_TICKS;
            }
        }

        if (this.pounceRecoveryTicks > 0) {
            this.pounceRecoveryTicks--;
            if (this.pounceRecoveryTicks == 0) {
                this.setPouncing(false);
                this.pounceLeapDone = false;
                this.pounceImpactDone = false;
            }
        }

        if (this.warnTicks > 0) {
            this.warnTicks--;
            if (this.warnTicks == 0) {
                this.setWarning(false);
            }
        }

        if (this.roarTicks > 0) {
            this.roarTicks--;
            if (this.roarTicks == 0) {
                this.setRoaring(false);
            }
        }

        if (this.pounceCooldownTicks > 0) {
            this.pounceCooldownTicks--;
        }

        if (this.attackCooldownTicks > 0) {
            this.attackCooldownTicks--;
        }

        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }
    }

    private void updatePounceMotion() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        if (this.pounceActiveTicks > 0 && !this.pounceLeapDone) {
            Vec3 jumpDirection = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            if (jumpDirection.lengthSqr() > 1.0E-4D) {
                Vec3 normalizedDirection = jumpDirection.normalize();
                this.setDeltaMovement(normalizedDirection.x * 1.15D, 0.42D, normalizedDirection.z * 1.15D);
            }
            this.pounceLeapDone = true;
        }

        if (this.pounceActiveTicks > 0 && !this.pounceImpactDone && this.distanceToSqr(target) <= 6.25D) {
            target.hurt(this.damageSources().mobAttack(this), POUNCE_DAMAGE);
            this.attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
            this.pounceImpactDone = true;
        }
    }

    public int getThreatLevel() {
        return this.entityData.get(DATA_THREAT);
    }

    public void setThreatLevel(int threatLevel) {
        this.entityData.set(DATA_THREAT, Mth.clamp(threatLevel, 0, 100));
    }

    private void updateThreatLevel() {
        Player player = this.level().getNearestPlayer(this, AGGRO_PLAYER_RADIUS);

        if (player == null || player.isCreative() || player.isSpectator()) {
            this.setThreatLevel(Math.max(0, this.getThreatLevel() - 1));
            return;
        }

        int increase = player.isSprinting() ? 3 : player.isCrouching() ? 1 : 2;
        this.setThreatLevel(Math.min(100, this.getThreatLevel() + increase));

        if (this.getThreatLevel() >= 75 && this.getTarget() == null) {
            this.setTarget(player);
            this.alertPride(player);
            this.triggerRoar();
        } else if (this.getThreatLevel() >= 50 && !this.isWarning()) {
            this.startWarning();
        }
    }

    private void limitPursuitRange() {
        if (this.prideCenterPos == null) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        if (!target.isAlive() || this.distanceToSqr(target) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS || target.distanceToSqr(Vec3.atCenterOf(this.prideCenterPos)) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.stopAllCombatStates();
        }
    }

    private void updateBabyProtection() {
        if (this.isBaby() || this.getTarget() != null || this.isBusy() || this.isSleeping()) {
            return;
        }

        List<LionEntity> nearbyBabies = this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(BABY_PROTECT_RADIUS), lion -> lion.isBaby() && lion != this);
        if (nearbyBabies.isEmpty()) {
            return;
        }

        Player threat = this.level().getNearestPlayer(this, BABY_PROTECT_TRIGGER);
        if (!this.isValidPlayerThreat(threat)) {
            return;
        }

        this.wakeUp();
        this.setTarget(threat);
        this.alertPride(threat);
        this.triggerRoar();
    }

    private void updateSleepState() {
        if (this.isSleeping()) {
            if (!this.level().isDay() || this.getTarget() != null || this.isBusy() || this.isStalking() || this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
                return;
            }

            if (this.sleepDurationTicks > 0) {
                this.sleepDurationTicks--;
            }

            if (this.sleepDurationTicks <= 0) {
                this.wakeUp();
                return;
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }

        if (this.getTarget() != null || this.isBusy() || this.isStalking() || !this.level().isDay() || this.isInWaterOrRain()) {
            this.sleepPreparationTicks = 0;
            return;
        }

        if (this.sleepCooldownTicks > 0) {
            return;
        }

        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.002D;
        if (isStill) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks) {
                this.startSleeping();
            }
        } else if (this.sleepPreparationTicks > 0) {
            this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 20);
        }
    }

    private boolean hasWakeUpTriggerNearby() {
        Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (player == null || player.isCreative() || player.isSpectator()) {
            return false;
        }
        return !player.isCrouching();
    }

    private void updateAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

        this.sleepState.animateWhen(this.isSleeping(), this.tickCount);
        this.pounceState.animateWhen(this.isPouncing() || this.pounceWindupTicks > 0, this.tickCount);
        this.warnState.animateWhen(this.isWarning(), this.tickCount);
        this.roarState.animateWhen(this.isRoaring(), this.tickCount);
        this.stalkState.animateWhen(this.isStalking() && moving, this.tickCount);

        if (!this.isSleeping() && !this.isPouncing() && !this.isWarning()) {
            if (moving && !this.isStalking()) {
                this.walkState.startIfStopped(this.tickCount);
                this.idleState.stop();
            } else if (!moving) {
                this.idleState.startIfStopped(this.tickCount);
                this.walkState.stop();
            } else {
                this.walkState.stop();
                this.idleState.stop();
            }
        } else {
            this.walkState.stop();
            this.idleState.stop();
        }
    }

    public void startPounce() {
        this.setPouncing(true);
        this.pounceWindupTicks = POUNCE_WINDUP_TICKS;
        this.pounceActiveTicks = 0;
        this.pounceRecoveryTicks = 0;
        this.pounceLeapDone = false;
        this.pounceImpactDone = false;
        this.pounceCooldownTicks = POUNCE_COOLDOWN_MIN + this.random.nextInt(POUNCE_COOLDOWN_RANDOM);
    }

    public void startSleeping() {
        this.setSleeping(true);
        this.sleepPreparationTicks = 0;
        this.sleepDurationTicks = 180 + this.random.nextInt(260);
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void wakeUp() {
        if (!this.isSleeping()) {
            return;
        }

        this.setSleeping(false);
        this.sleepDurationTicks = 0;
        this.sleepCooldownTicks = SLEEP_COOLDOWN_MIN + this.random.nextInt(SLEEP_COOLDOWN_RANDOM);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(180);
    }

    public void alertPride(Player player) {
        if (player == null) {
            return;
        }

        List<LionEntity> pride = this.level().getEntitiesOfClass(LionEntity.class, this.getBoundingBox().inflate(ALERT_RADIUS, 6.0D, ALERT_RADIUS), lion -> lion != this && lion.isAlive() && !lion.isBaby());
        for (LionEntity lion : pride) {
            if (lion.canTargetPlayer(player)) {
                lion.wakeUp();
                lion.setTarget(player);
                lion.triggerRoar();
            }
        }
    }

    public boolean canWarnAtPlayer(@Nullable Player player) {
        return player != null && !player.isCreative() && !player.isSpectator() && this.distanceToSqr(player) <= AGGRO_PLAYER_RADIUS * AGGRO_PLAYER_RADIUS && this.getLastHurtByMob() != player;
    }

    public boolean canTargetPlayer(Player player) {
        if (player == null || player.isCreative() || player.isSpectator()) {
            return false;
        }

        return this.distanceToSqr(player) <= AGGRO_PLAYER_RADIUS * AGGRO_PLAYER_RADIUS || this.getLastHurtByMob() == player;
    }

    private boolean isValidPlayerThreat(@Nullable Player player) {
        return player != null && !player.isCreative() && !player.isSpectator() && player.distanceToSqr(this) <= BABY_PROTECT_TRIGGER * BABY_PROTECT_TRIGGER;
    }

    public boolean canStartPounce() {
        return this.pounceCooldownTicks <= 0 && !this.isBusy();
    }

    public boolean canUseCombatGoal() {
        return !this.isBaby() && !this.isSleeping() && !this.isBusy() && this.getTarget() != null && this.getTarget().isAlive();
    }

    private void stopAllCombatStates() {
        this.setPouncing(false);
        this.setWarning(false);
        this.setStalking(false);
        this.pounceWindupTicks = 0;
        this.pounceActiveTicks = 0;
        this.pounceRecoveryTicks = 0;
        this.pounceLeapDone = false;
        this.pounceImpactDone = false;
    }

    public boolean isBusy() {
        return this.isPouncing() || this.pounceWindupTicks > 0 || this.pounceRecoveryTicks > 0 || this.isWarning();
    }

    public int getWarnTicks() {
        return this.warnTicks;
    }

    private void setFlag(int flag, boolean value) {
        int currentFlags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, value ? currentFlags | flag : currentFlags & ~flag);
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    public boolean isSleeping() {
        return this.getFlag(FLAG_SLEEPING);
    }

    public void setSleeping(boolean value) {
        this.setFlag(FLAG_SLEEPING, value);
    }

    public boolean isStalking() {
        return this.getFlag(FLAG_STALKING);
    }

    public void setStalking(boolean value) {
        this.setFlag(FLAG_STALKING, value);
    }

    public boolean isPouncing() {
        return this.getFlag(FLAG_POUNCING);
    }

    public void setPouncing(boolean value) {
        this.setFlag(FLAG_POUNCING, value);
    }

    public boolean isWarning() {
        return this.getFlag(FLAG_WARNING);
    }

    public void setWarning(boolean value) {
        this.setFlag(FLAG_WARNING, value);
    }

    public boolean isRoaring() {
        return this.getFlag(FLAG_ROARING);
    }

    public void setRoaring(boolean value) {
        this.setFlag(FLAG_ROARING, value);
    }

    public boolean isMale() {
        return this.entityData.get(DATA_MALE);
    }

    public void setMale(boolean value) {
        this.entityData.set(DATA_MALE, value);
    }

    @Nullable
    public BlockPos getPrideCenterPos() {
        return this.prideCenterPos;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            this.attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
            this.playSound(SoundEventRegistry.LION_ATTACK.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
        }
        return hit;
    }

    public void startWarning() {
        this.setWarning(true);
        this.warnTicks = WARN_DURATION_TICKS;
        this.playSound(SoundEventRegistry.LION_WARN.get(), 1.0F, 0.95F + this.random.nextFloat() * 0.1F);
    }

    public void triggerRoar() {
        if (this.roarTicks > 0) {
            return;
        }

        this.setRoaring(true);
        this.roarTicks = ROAR_DURATION_TICKS;
        this.playSound(SoundEventRegistry.LION_WARN.get(), 1.2F, 0.8F + this.random.nextFloat() * 0.1F);
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean wasHurt = super.hurt(source, amount);
        if (wasHurt) {
            this.wakeUp();
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player) {
                this.alertPride(player);
                this.triggerRoar();
                this.setTarget(player);
            }
        }
        return wasHurt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.LION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEventRegistry.LION_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.LION_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LionFlags", this.entityData.get(DATA_FLAGS));
        tag.putBoolean("IsMale", this.isMale());
        tag.putInt("PounceWindupTicks", this.pounceWindupTicks);
        tag.putInt("PounceActiveTicks", this.pounceActiveTicks);
        tag.putInt("PounceRecoveryTicks", this.pounceRecoveryTicks);
        tag.putInt("PounceCooldownTicks", this.pounceCooldownTicks);
        tag.putInt("WarnTicks", this.warnTicks);
        tag.putInt("RoarTicks", this.roarTicks);
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("SleepDurationTicks", this.sleepDurationTicks);
        tag.putInt("AttackCooldownTicks", this.attackCooldownTicks);
        if (this.prideCenterPos != null) {
            tag.putInt("PrideCenterX", this.prideCenterPos.getX());
            tag.putInt("PrideCenterY", this.prideCenterPos.getY());
            tag.putInt("PrideCenterZ", this.prideCenterPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS, tag.getInt("LionFlags"));
        this.setMale(tag.getBoolean("IsMale"));
        this.pounceWindupTicks = tag.getInt("PounceWindupTicks");
        this.pounceActiveTicks = tag.getInt("PounceActiveTicks");
        this.pounceRecoveryTicks = tag.getInt("PounceRecoveryTicks");
        this.pounceCooldownTicks = tag.getInt("PounceCooldownTicks");
        this.warnTicks = tag.getInt("WarnTicks");
        this.roarTicks = tag.getInt("RoarTicks");
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks") ? tag.getInt("RequiredSleepPreparationTicks") : 100 + this.random.nextInt(180);
        this.sleepDurationTicks = tag.getInt("SleepDurationTicks");
        this.attackCooldownTicks = tag.getInt("AttackCooldownTicks");
        if (tag.contains("PrideCenterX")) {
            this.prideCenterPos = new BlockPos(tag.getInt("PrideCenterX"), tag.getInt("PrideCenterY"), tag.getInt("PrideCenterZ"));
        } else {
            this.prideCenterPos = null;
        }
        this.pounceLeapDone = false;
        this.pounceImpactDone = false;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.prideCenterPos = this.blockPosition();
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(180);
        this.setMale(this.random.nextFloat() < 0.2F);
        return result;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.LION_FOOD);
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby() ? 1.35F : 1.0F;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return EntityTypeRegistry.LION.get().create(level);
    }
}