package net.satisfy.wildernature.core.entity.animal.defensive;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElephantEntity extends Animal {
    private static final int FLAG_THROWING = 0x00000001;
    private static final int FLAG_STAMPING = 0x00000010;
    private static final int FLAG_CHARGING = 0x00000100;

    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.MELON_SLICE, Items.PUMPKIN, Items.APPLE, Items.SUGAR_CANE);

    private static final double HERD_RADIUS = 18.0D;
    private static final double HERD_RETURN_RADIUS = 10.0D;
    private static final double TERRITORY_RADIUS = 26.0D;
    private static final double TERRITORY_BREAK_RADIUS = 38.0D;
    private static final double ALERT_RADIUS = 18.0D;
    private static final double THROW_RANGE_MIN = 6.0D;
    private static final double THROW_RANGE_MAX = 14.0D;
    private static final double STAMP_RANGE = 3.4D;
    private static final double CHARGE_START_RANGE = 8.0D;
    private static final double CHARGE_MAX_RANGE = 20.0D;

    private static final int THROW_DURATION = 20;
    private static final int THROW_IMPACT_TICK = 12;
    private static final int STAMP_DURATION = 16;
    private static final int STAMP_IMPACT_TICK = 9;
    private static final int CHARGE_WINDUP_TICKS = 14;
    private static final int CHARGE_ACTIVE_TICKS = 18;
    private static final int CHARGE_RECOVERY_TICKS = 16;
    private static final int THROW_COOLDOWN_MIN = 60;
    private static final int THROW_COOLDOWN_RANDOM = 40;
    private static final int STAMP_COOLDOWN_MIN = 40;
    private static final int STAMP_COOLDOWN_RANDOM = 30;
    private static final int CHARGE_COOLDOWN_MIN = 90;
    private static final int CHARGE_COOLDOWN_RANDOM = 50;
    private static final int HERD_SYNC_INTERVAL = 20;

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_FLAGS = net.minecraft.network.syncher.SynchedEntityData.defineId(ElephantEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);

    private int throwTicks;
    private int stampTicks;
    private int chargeWindupTicks;
    private int chargeActiveTicks;
    private int chargeRecoveryTicks;
    private int throwCooldownTicks;
    private int stampCooldownTicks;
    private int chargeCooldownTicks;
    private int herdSyncCooldown;
    private boolean throwImpactDone;
    private boolean stampImpactDone;
    private boolean chargeImpactDone;
    private double chargeDirectionX;
    private double chargeDirectionZ;
    private BlockPos herdCenter;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState throwState = new AnimationState();
    public final AnimationState stampState = new AnimationState();
    public final AnimationState chargeState = new AnimationState();

    public ElephantEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 60.0D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.17D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 10.0D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE, 28.0D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(net.minecraft.world.entity.ai.attributes.Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new ElephantChargeGoal(this));
        this.goalSelector.addGoal(2, new ElephantThrowGoal(this));
        this.goalSelector.addGoal(3, new ElephantStampGoal(this));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            public boolean canUse() {
                return ElephantEntity.this.canUseCombatGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return ElephantEntity.this.canUseCombatGoal() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new ReturnToHerdGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new BreedGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(8, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.65D, 80) {
            @Override
            public boolean canUse() {
                return ElephantEntity.this.canUseLandIdleGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return ElephantEntity.this.canUseLandIdleGoal() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                super.start();
                if (ElephantEntity.this.getTarget() instanceof Player player) {
                    ElephantEntity.this.alertHerd(player);
                }
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, target -> target instanceof Player player && this.canTargetPlayer(player)) {
            @Override
            public boolean canUse() {
                return !ElephantEntity.this.isBaby() && !ElephantEntity.this.isBusy() && super.canUse();
            }

            @Override
            public void start() {
                super.start();
                if (this.target instanceof Player player) {
                    ElephantEntity.this.alertHerd(player);
                }
            }

            @Override
            public boolean canContinueToUse() {
                return this.target instanceof Player player && ElephantEntity.this.canTargetPlayer(player) && super.canContinueToUse();
            }
        });
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData output = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.herdCenter = this.blockPosition();
        this.throwTicks = 0;
        this.stampTicks = 0;
        this.chargeWindupTicks = 0;
        this.chargeActiveTicks = 0;
        this.chargeRecoveryTicks = 0;
        this.throwCooldownTicks = 0;
        this.stampCooldownTicks = 0;
        this.chargeCooldownTicks = 0;
        this.herdSyncCooldown = this.random.nextInt(HERD_SYNC_INTERVAL);
        this.throwImpactDone = false;
        this.stampImpactDone = false;
        this.chargeImpactDone = false;
        return output;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ElephantFlags", this.entityData.get(DATA_FLAGS));
        tag.putInt("ThrowTicks", this.throwTicks);
        tag.putInt("StampTicks", this.stampTicks);
        tag.putInt("ChargeWindupTicks", this.chargeWindupTicks);
        tag.putInt("ChargeActiveTicks", this.chargeActiveTicks);
        tag.putInt("ChargeRecoveryTicks", this.chargeRecoveryTicks);
        tag.putInt("ThrowCooldownTicks", this.throwCooldownTicks);
        tag.putInt("StampCooldownTicks", this.stampCooldownTicks);
        tag.putInt("ChargeCooldownTicks", this.chargeCooldownTicks);
        tag.putDouble("ChargeDirectionX", this.chargeDirectionX);
        tag.putDouble("ChargeDirectionZ", this.chargeDirectionZ);
        if (this.herdCenter != null) {
            tag.putInt("HerdCenterX", this.herdCenter.getX());
            tag.putInt("HerdCenterY", this.herdCenter.getY());
            tag.putInt("HerdCenterZ", this.herdCenter.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS, tag.getInt("ElephantFlags"));
        this.throwTicks = tag.getInt("ThrowTicks");
        this.stampTicks = tag.getInt("StampTicks");
        this.chargeWindupTicks = tag.getInt("ChargeWindupTicks");
        this.chargeActiveTicks = tag.getInt("ChargeActiveTicks");
        this.chargeRecoveryTicks = tag.getInt("ChargeRecoveryTicks");
        this.throwCooldownTicks = tag.getInt("ThrowCooldownTicks");
        this.stampCooldownTicks = tag.getInt("StampCooldownTicks");
        this.chargeCooldownTicks = tag.getInt("ChargeCooldownTicks");
        this.chargeDirectionX = tag.getDouble("ChargeDirectionX");
        this.chargeDirectionZ = tag.getDouble("ChargeDirectionZ");
        if (tag.contains("HerdCenterX") && tag.contains("HerdCenterY") && tag.contains("HerdCenterZ")) {
            this.herdCenter = new BlockPos(tag.getInt("HerdCenterX"), tag.getInt("HerdCenterY"), tag.getInt("HerdCenterZ"));
        } else {
            this.herdCenter = this.blockPosition();
        }
        this.throwImpactDone = false;
        this.stampImpactDone = false;
        this.chargeImpactDone = false;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateServerState();
        }

        this.updateAnimationStates();
    }

    private void updateServerState() {
        this.updateTimers();
        this.updateChargeMotion();
        this.syncHerdCenter();
        this.limitPursuitRange();
        this.updateBabySafety();
    }

    private void updateTimers() {
        if (this.throwTicks > 0) {
            this.throwTicks--;
            if (this.throwTicks == 0) {
                this.setThrowing(false);
                this.throwImpactDone = false;
            }
        }

        if (this.stampTicks > 0) {
            this.stampTicks--;
            if (this.stampTicks == 0) {
                this.setStamping(false);
                this.stampImpactDone = false;
            }
        }

        if (this.chargeWindupTicks > 0) {
            this.chargeWindupTicks--;
            if (this.chargeWindupTicks == 0 && this.chargeActiveTicks <= 0 && this.chargeRecoveryTicks <= 0) {
                this.chargeActiveTicks = CHARGE_ACTIVE_TICKS;
                this.setCharging(true);
            }
        }

        if (this.chargeRecoveryTicks > 0) {
            this.chargeRecoveryTicks--;
            if (this.chargeRecoveryTicks == 0) {
                this.setCharging(false);
                this.chargeImpactDone = false;
            }
        }

        if (this.throwCooldownTicks > 0) {
            this.throwCooldownTicks--;
        }
        if (this.stampCooldownTicks > 0) {
            this.stampCooldownTicks--;
        }
        if (this.chargeCooldownTicks > 0) {
            this.chargeCooldownTicks--;
        }
        if (this.herdSyncCooldown > 0) {
            this.herdSyncCooldown--;
        }
    }

    private void updateChargeMotion() {
        if (this.chargeActiveTicks <= 0) {
            return;
        }

        this.chargeActiveTicks--;

        Vec3 chargeMotion = new Vec3(this.chargeDirectionX * 0.62D, this.getDeltaMovement().y, this.chargeDirectionZ * 0.62D);
        this.setDeltaMovement(chargeMotion);
        this.move(MoverType.SELF, this.getDeltaMovement());

        LivingEntity target = this.getTarget();
        if (!this.chargeImpactDone && target != null && target.isAlive() && this.distanceToSqr(target) <= 9.0D) {
            this.chargeImpactDone = true;
            target.hurt(this.damageSources().mobAttack(this), 14.0F);
            Vec3 knockbackDirection = new Vec3(this.chargeDirectionX, 0.0D, this.chargeDirectionZ).normalize().scale(1.6D);
            target.push(knockbackDirection.x, 0.4D, knockbackDirection.z);
        }

        if (this.chargeActiveTicks <= 0) {
            this.chargeRecoveryTicks = CHARGE_RECOVERY_TICKS;
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void syncHerdCenter() {
        if (this.herdSyncCooldown > 0) {
            return;
        }

        this.herdSyncCooldown = HERD_SYNC_INTERVAL;

        List<ElephantEntity> nearbyElephants = this.level().getEntitiesOfClass(ElephantEntity.class, this.getBoundingBox().inflate(HERD_RADIUS, 6.0D, HERD_RADIUS), elephant -> elephant != this && elephant.isAlive());
        if (nearbyElephants.isEmpty()) {
            if (this.herdCenter == null) {
                this.herdCenter = this.blockPosition();
            }
            return;
        }

        double centerX = this.getX();
        double centerY = this.getY();
        double centerZ = this.getZ();
        int count = 1;

        for (ElephantEntity elephant : nearbyElephants) {
            centerX += elephant.getX();
            centerY += elephant.getY();
            centerZ += elephant.getZ();
            count++;
        }

        this.herdCenter = BlockPos.containing(centerX / count, centerY / count, centerZ / count);
    }

    private void limitPursuitRange() {
        if (this.herdCenter == null) {
            this.herdCenter = this.blockPosition();
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) {
            return;
        }

        if (!target.isAlive() || this.distanceToSqr(target) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS || target.distanceToSqr(Vec3.atCenterOf(this.herdCenter)) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.stopAllCombatStates();
        }
    }

    private void updateBabySafety() {
        if (!this.isBaby() || this.herdCenter == null || this.isBusy()) {
            return;
        }

        if (this.blockPosition().distSqr(this.herdCenter) > HERD_RETURN_RADIUS * HERD_RETURN_RADIUS) {
            this.getNavigation().moveTo(this.herdCenter.getX() + 0.5D, this.herdCenter.getY(), this.herdCenter.getZ() + 0.5D, 1.0D);
        }
    }

    private void updateAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isThrowing() && !this.isStamping() && !this.isCharging();

        this.throwState.animateWhen(this.isThrowing(), this.tickCount);
        this.stampState.animateWhen(this.isStamping(), this.tickCount);
        this.chargeState.animateWhen(this.isCharging() || this.chargeWindupTicks > 0, this.tickCount);

        if (idleAllowed) {
            this.idleState.startIfStopped(this.tickCount);
        } else {
            this.idleState.stop();
        }

        if (moving && !this.isThrowing() && !this.isStamping()) {
            this.walkState.startIfStopped(this.tickCount);
        } else {
            this.walkState.stop();
        }
    }

    public boolean canTargetPlayer(@Nullable Player player) {
        if (player == null || this.isBaby() || player.isCreative() || player.isSpectator()) {
            return false;
        }

        if (this.herdCenter == null) {
            this.herdCenter = this.blockPosition();
        }

        if (player.distanceToSqr(Vec3.atCenterOf(this.herdCenter)) > TERRITORY_RADIUS * TERRITORY_RADIUS) {
            return false;
        }

        return this.distanceToSqr(player) <= TERRITORY_RADIUS * TERRITORY_RADIUS || this.getLastHurtByMob() == player;
    }

    private void alertHerd(Player player) {
        if (player == null || this.herdCenter == null) {
            return;
        }

        List<ElephantEntity> nearbyElephants = this.level().getEntitiesOfClass(ElephantEntity.class, this.getBoundingBox().inflate(ALERT_RADIUS, 6.0D, ALERT_RADIUS), elephant -> elephant != this && elephant.isAlive() && !elephant.isBaby());
        for (ElephantEntity elephant : nearbyElephants) {
            if (elephant.canTargetPlayer(player)) {
                elephant.setTarget(player);
            }
        }
    }

    private boolean canUseCombatGoal() {
        return !this.isBaby() && !this.isBusy() && this.getTarget() != null && this.getTarget().isAlive();
    }

    private boolean canUseLandIdleGoal() {
        return !this.isBusy() && this.getTarget() == null && !this.isBaby() && this.herdCenter != null && this.blockPosition().distSqr(this.herdCenter) <= (HERD_RADIUS + 6.0D) * (HERD_RADIUS + 6.0D);
    }

    private void stopAllCombatStates() {
        this.setThrowing(false);
        this.setStamping(false);
        this.setCharging(false);
        this.throwTicks = 0;
        this.stampTicks = 0;
        this.chargeWindupTicks = 0;
        this.chargeActiveTicks = 0;
        this.chargeRecoveryTicks = 0;
        this.throwImpactDone = false;
        this.stampImpactDone = false;
        this.chargeImpactDone = false;
    }

    private void setFlag(int flag, boolean value) {
        int currentFlags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, value ? currentFlags | flag : currentFlags & ~flag);
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    public boolean isThrowing() {
        return this.getFlag(FLAG_THROWING);
    }

    public void startThrow() {
        this.setThrowing(true);
        this.throwTicks = THROW_DURATION;
        this.throwImpactDone = false;
        this.throwCooldownTicks = THROW_COOLDOWN_MIN + this.random.nextInt(THROW_COOLDOWN_RANDOM);
    }

    public void setThrowing(boolean value) {
        this.setFlag(FLAG_THROWING, value);
    }

    public boolean isStamping() {
        return this.getFlag(FLAG_STAMPING);
    }

    public void startStamp() {
        this.setStamping(true);
        this.stampTicks = STAMP_DURATION;
        this.stampImpactDone = false;
        this.stampCooldownTicks = STAMP_COOLDOWN_MIN + this.random.nextInt(STAMP_COOLDOWN_RANDOM);
    }

    public void setStamping(boolean value) {
        this.setFlag(FLAG_STAMPING, value);
    }

    public boolean isCharging() {
        return this.getFlag(FLAG_CHARGING);
    }

    public void startCharge(double directionX, double directionZ) {
        Vec3 direction = new Vec3(directionX, 0.0D, directionZ);
        if (direction.lengthSqr() < 1.0E-6D) {
            return;
        }

        Vec3 normalizedDirection = direction.normalize();
        this.chargeDirectionX = normalizedDirection.x;
        this.chargeDirectionZ = normalizedDirection.z;
        this.chargeWindupTicks = CHARGE_WINDUP_TICKS;
        this.chargeActiveTicks = 0;
        this.chargeRecoveryTicks = 0;
        this.chargeImpactDone = false;
        this.chargeCooldownTicks = CHARGE_COOLDOWN_MIN + this.random.nextInt(CHARGE_COOLDOWN_RANDOM);
        this.setCharging(true);
    }

    public void setCharging(boolean value) {
        this.setFlag(FLAG_CHARGING, value);
    }

    public boolean canStartThrow() {
        return this.throwCooldownTicks <= 0 && !this.isBusy();
    }

    public boolean canStartStamp() {
        return this.stampCooldownTicks <= 0 && !this.isBusy();
    }

    public boolean canStartCharge() {
        return this.chargeCooldownTicks <= 0 && !this.isBusy();
    }

    public boolean isBusy() {
        return this.isThrowing() || this.isStamping() || this.isCharging() || this.chargeWindupTicks > 0 || this.chargeRecoveryTicks > 0;
    }

    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.isFood(itemStack)) {
            return super.mobInteract(player, hand);
        }
        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public ElephantEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.ELEPHANT.get().create(serverLevel);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity directEntity = damageSource.getDirectEntity();

        if (directEntity instanceof AbstractArrow arrowEntity) {
            if (!this.level().isClientSide) {
                arrowEntity.setDeltaMovement(arrowEntity.getDeltaMovement().scale(-0.3D));
            }
            return false;
        }

        boolean wasHurt = super.hurt(damageSource, amount);
        if (wasHurt && this.getLastHurtByMob() instanceof Player player) {
            this.alertHerd(player);
            if (!this.isBaby() && this.canTargetPlayer(player)) {
                this.setTarget(player);
            }
        }
        return wasHurt;
    }
/*
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.ELEPHANT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.ELEPHANT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.ELEPHANT_DEATH.get();
    }
*/
    public BlockPos getHerdCenter() {
        return this.herdCenter == null ? this.blockPosition() : this.herdCenter;
    }

    private static final class ReturnToHerdGoal extends Goal {
        private final ElephantEntity elephantEntity;
        private final double speedModifier;

        private ReturnToHerdGoal(ElephantEntity elephantEntity, double speedModifier) {
            this.elephantEntity = elephantEntity;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.elephantEntity.isBusy() || this.elephantEntity.getTarget() != null) {
                return false;
            }

            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            double maxDistance = this.elephantEntity.isBaby() ? HERD_RETURN_RADIUS : HERD_RADIUS;
            return this.elephantEntity.blockPosition().distSqr(herdCenter) > maxDistance * maxDistance;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.elephantEntity.isBusy() || this.elephantEntity.getTarget() != null) {
                return false;
            }

            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            double continueDistance = this.elephantEntity.isBaby() ? 9.0D : 16.0D;
            return this.elephantEntity.blockPosition().distSqr(herdCenter) > continueDistance * continueDistance && !this.elephantEntity.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            this.elephantEntity.getNavigation().moveTo(herdCenter.getX() + 0.5D, herdCenter.getY(), herdCenter.getZ() + 0.5D, this.speedModifier);
        }

        @Override
        public void tick() {
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            this.elephantEntity.getNavigation().moveTo(herdCenter.getX() + 0.5D, herdCenter.getY(), herdCenter.getZ() + 0.5D, this.speedModifier);
        }
    }

    private static final class ElephantThrowGoal extends Goal {
        private final ElephantEntity elephantEntity;

        private ElephantThrowGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null || !target.isAlive() || !this.elephantEntity.canStartThrow()) {
                return false;
            }

            double distanceToTarget = this.elephantEntity.distanceTo(target);
            return distanceToTarget >= THROW_RANGE_MIN && distanceToTarget <= THROW_RANGE_MAX;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.isThrowing() && this.elephantEntity.throwTicks > 0;
        }

        @Override
        public void start() {
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startThrow();
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) {
                return;
            }

            this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (!this.elephantEntity.throwImpactDone && this.elephantEntity.throwTicks <= THROW_DURATION - THROW_IMPACT_TICK) {
                this.elephantEntity.throwImpactDone = true;
                if (this.elephantEntity.hasLineOfSight(target) && this.elephantEntity.distanceTo(target) <= THROW_RANGE_MAX + 2.0D) {
                    target.hurt(this.elephantEntity.damageSources().mobAttack(this.elephantEntity), 8.0F);
                    Vec3 pushDirection = target.position().subtract(this.elephantEntity.position()).normalize().scale(1.0D);
                    target.push(pushDirection.x, 0.45D, pushDirection.z);
                }
            }
        }
    }

    private static final class ElephantStampGoal extends Goal {
        private final ElephantEntity elephantEntity;

        private ElephantStampGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            return target != null && target.isAlive() && this.elephantEntity.canStartStamp() && this.elephantEntity.distanceTo(target) <= STAMP_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.isStamping() && this.elephantEntity.stampTicks > 0;
        }

        @Override
        public void start() {
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startStamp();
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) {
                return;
            }

            this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (!this.elephantEntity.stampImpactDone && this.elephantEntity.stampTicks <= STAMP_DURATION - STAMP_IMPACT_TICK) {
                this.elephantEntity.stampImpactDone = true;
                List<LivingEntity> hitTargets = this.elephantEntity.level().getEntitiesOfClass(LivingEntity.class, this.elephantEntity.getBoundingBox().inflate(3.6D, 1.0D, 3.6D), entity -> entity != this.elephantEntity && entity.isAlive());
                for (LivingEntity hitTarget : hitTargets) {
                    hitTarget.hurt(this.elephantEntity.damageSources().mobAttack(this.elephantEntity), 11.0F);
                    Vec3 pushDirection = hitTarget.position().subtract(this.elephantEntity.position());
                    if (pushDirection.lengthSqr() > 1.0E-6D) {
                        Vec3 normalizedDirection = pushDirection.normalize().scale(0.9D);
                        hitTarget.push(normalizedDirection.x, 0.3D, normalizedDirection.z);
                    }
                }
            }
        }
    }

    private static final class ElephantChargeGoal extends Goal {
        private final ElephantEntity elephantEntity;

        private ElephantChargeGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null || !target.isAlive() || this.elephantEntity.isBaby() || !this.elephantEntity.canStartCharge()) {
                return false;
            }

            double distanceToTarget = this.elephantEntity.distanceTo(target);
            return distanceToTarget >= CHARGE_START_RANGE && distanceToTarget <= CHARGE_MAX_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.chargeWindupTicks > 0 || this.elephantEntity.chargeActiveTicks > 0 || this.elephantEntity.chargeRecoveryTicks > 0;
        }

        @Override
        public void start() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) {
                return;
            }

            Vec3 direction = target.position().subtract(this.elephantEntity.position());
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startCharge(direction.x, direction.z);
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target != null && this.elephantEntity.chargeWindupTicks > 0) {
                this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }
}