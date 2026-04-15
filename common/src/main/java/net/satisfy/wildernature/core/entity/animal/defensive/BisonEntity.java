package net.satisfy.wildernature.core.entity.animal.defensive;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.goal.animal.BisonGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BisonEntity extends Animal {
    private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ALERT = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SNORTING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CHARGING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GRAZING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> LAST_HURT_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.LONG);

    private static final int ATTACK_DURATION = 16;
    private static final int CHARGE_DURATION = 34;
    private static final int CHARGE_COOLDOWN_MIN = 100;
    private static final int CHARGE_COOLDOWN_MAX = 180;
    private static final int BABY_PROTECT_CACHE_DURATION = 20;
    private static final int GRAZE_DURATION_MIN = 60;
    private static final int GRAZE_DURATION_MAX = 120;
    private static final int SLEEP_START_DELAY = 140;
    private static final double HERD_PROTECT_RANGE = 8.0D;
    private static final double BABY_PROTECT_RANGE = 13.0D;

    public static final int ALERT_DURATION = 20;
    public static final int SNORT_DURATION = 18;
    public static final double CHARGE_SPEED = 1.55D;
    public static final int CALM_DOWN_DURATION = 200;
    public static final double CHARGE_BREAK_RANGE = 20.0D;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState alertAnimationState = new AnimationState();
    public final AnimationState snortAnimationState = new AnimationState();
    public final AnimationState grazeAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();

    private int attackAnimationTicks;
    private int alertTicks;
    private int snortTicks;
    private int chargeTicks;
    private int chargeCooldownTicks;
    private int calmDownTicks;
    private int babyProtectCacheTicks;
    private int grazeTicks;
    private int restTicks;
    private boolean nearbyBabyCached;

    public BisonEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.BISON_FOOD);
    }

    public static @NotNull AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.ATTACK_DAMAGE, 6.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.19D)
                .add(Attributes.ARMOR_TOUGHNESS, 1.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 3.2D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.45D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.attackAnimationTicks > 0) {
                this.attackAnimationTicks--;
            }

            this.setAttacking(this.attackAnimationTicks > 0);

            this.updateTemperState();
            this.updatePassiveState();

            if ((this.isSprinting() || this.isCharging()) && this.getDeltaMovement().horizontalDistanceSqr() > 0.003D) {
                this.spawnMovementParticles();
            }
        } else {
            this.setupAnimationStates();
        }
    }

    private void updateTemperState() {
        if (this.isPeacefulDifficulty()) {
            this.setTarget(null);
            this.setAngry(false);
            this.stopAlertStates();
            this.stopCharge();
            this.calmDownTicks = 0;
            return;
        }

        if (this.babyProtectCacheTicks > 0) {
            this.babyProtectCacheTicks--;
        } else {
            this.updateNearbyBabyCache();
            this.babyProtectCacheTicks = BABY_PROTECT_CACHE_DURATION;
        }

        if (this.chargeCooldownTicks > 0) {
            this.chargeCooldownTicks--;
        }

        if (this.calmDownTicks > 0) {
            this.calmDownTicks--;
        }

        if (this.alertTicks > 0) {
            this.alertTicks--;
        }

        if (this.snortTicks > 0) {
            this.snortTicks--;
        }

        if (this.chargeTicks > 0) {
            this.chargeTicks--;
        }

        this.setAlert(this.alertTicks > 0);
        this.setSnorting(this.snortTicks > 0);
        this.setCharging(this.chargeTicks > 0);

        if (this.isAngry() && this.getTarget() == null && this.calmDownTicks <= 0) {
            this.setAngry(false);
        }

        if (this.getTarget() instanceof Player player) {
            if (!this.isValidThreat(player) || this.distanceToSqr(player) > CHARGE_BREAK_RANGE * CHARGE_BREAK_RANGE) {
                this.setTarget(null);
                this.setAngry(false);
                this.setCharging(false);
                this.chargeTicks = 0;
            }
        }
    }

    public int getChargeTicks() {
        return this.chargeTicks;
    }

    public void setCalmDown(int calmDownTicks) {
        this.calmDownTicks = calmDownTicks;
    }

    public void stopAlertStates() {
        this.alertTicks = 0;
        this.snortTicks = 0;
        this.setAlert(false);
        this.setSnorting(false);
    }

    private void updatePassiveState() {
        if (this.grazeTicks > 0) {
            this.grazeTicks--;
        }

        if (this.isBusyState()) {
            this.grazeTicks = 0;
            this.restTicks = 0;
            this.setGrazing(false);
            this.setSleeping(false);
            return;
        }

        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;

        if (moving || !this.getNavigation().isDone()) {
            this.restTicks = 0;
            this.setSleeping(false);
            if (this.grazeTicks <= 0) {
                this.setGrazing(false);
            }
            return;
        }

        this.restTicks++;

        if (this.level().isNight()) {
            this.setGrazing(false);
            if (this.restTicks >= SLEEP_START_DELAY) {
                this.setSleeping(true);
            }
            return;
        }

        this.setSleeping(false);

        if (this.grazeTicks > 0) {
            this.setGrazing(true);
            return;
        }

        this.setGrazing(false);

        if (this.restTicks > 40 && this.random.nextInt(180) == 0) {
            this.grazeTicks = GRAZE_DURATION_MIN + this.random.nextInt(GRAZE_DURATION_MAX - GRAZE_DURATION_MIN + 1);
            this.setGrazing(true);
        }
    }

    private boolean isBusyState() {
        return this.isAngry() || this.isAlert() || this.isSnorting() || this.isCharging() || this.isAttacking() || this.getTarget() != null;
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isAttacking() && !this.isAlert() && !this.isSnorting() && !this.isGrazing() && !this.isSleeping();

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
        this.alertAnimationState.animateWhen(this.isAlert(), this.tickCount);
        this.snortAnimationState.animateWhen(this.isSnorting(), this.tickCount);
        this.grazeAnimationState.animateWhen(this.isGrazing(), this.tickCount);
        this.sleepAnimationState.animateWhen(this.isSleeping(), this.tickCount);
    }

    private void spawnMovementParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockState blockState = this.level().getBlockState(this.getOnPos());
        Vec3 deltaMovement = this.getDeltaMovement();
        double horizontalLength = deltaMovement.horizontalDistance();

        if (horizontalLength > 1.0E-4D) {
            double normalX = deltaMovement.x / horizontalLength;
            double normalZ = deltaMovement.z / horizontalLength;
            double particleX = this.getX() - normalX * 0.6D;
            double particleY = this.getY() + 0.1D;
            double particleZ = this.getZ() - normalZ * 0.6D;

            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), particleX, particleY, particleZ, 40, this.getBbWidth() * 0.4D, 0.05D, this.getBbWidth() * 0.4D, 0.12D);
            serverLevel.sendParticles(ParticleTypes.CLOUD, particleX, particleY + 0.05D, particleZ, 12, 0.25D, 0.01D, 0.25D, 0.01D);
        }
    }

    private void updateNearbyBabyCache() {
        this.nearbyBabyCached = false;
        List<BisonEntity> herd = this.level().getEntitiesOfClass(BisonEntity.class, this.getBoundingBox().inflate(10.0D), entity -> entity != this);
        for (BisonEntity bisonEntity : herd) {
            if (bisonEntity.isBaby()) {
                this.nearbyBabyCached = true;
                return;
            }
        }
    }

    public boolean isAlert() {
        return this.entityData.get(ALERT);
    }

    public void setAlert(boolean alert) {
        this.entityData.set(ALERT, alert);
    }

    public boolean isSnorting() {
        return this.entityData.get(SNORTING);
    }

    public void setSnorting(boolean snorting) {
        this.entityData.set(SNORTING, snorting);
    }

    public boolean isCharging() {
        return this.entityData.get(CHARGING);
    }

    public void setCharging(boolean charging) {
        this.entityData.set(CHARGING, charging);
    }

    public boolean isGrazing() {
        return this.entityData.get(GRAZING);
    }

    public void setGrazing(boolean grazing) {
        this.entityData.set(GRAZING, grazing);
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float walkSpeed;
        if (this.getPose() == Pose.STANDING && !this.isSleeping()) {
            walkSpeed = Math.min(partialTick * 6.0F, 1.0F);
        } else {
            walkSpeed = 0.0F;
        }
        this.walkAnimation.update(walkSpeed, 0.2F);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAngry() {
        return this.entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(ANGRY, angry);
        if (!angry) {
            this.alertTicks = 0;
            this.snortTicks = 0;
            this.chargeTicks = 0;
            this.setAlert(false);
            this.setSnorting(false);
            this.setCharging(false);
        }
    }

    public long getLastHurtTime() {
        return this.entityData.get(LAST_HURT_TIME);
    }

    public void setLastHurtTime(long gameTime) {
        this.entityData.set(LAST_HURT_TIME, gameTime);
    }

    public void startAlertPhase() {
        this.alertTicks = ALERT_DURATION;
        this.snortTicks = 0;
        this.setAlert(true);
        this.setSnorting(false);
        this.setGrazing(false);
        this.setSleeping(false);
        this.grazeTicks = 0;
        this.restTicks = 0;
    }

    public void startSnortPhase() {
        this.alertTicks = 0;
        this.snortTicks = SNORT_DURATION;
        this.setAlert(false);
        this.setSnorting(true);
    }

    public void startChargeCooldown() {
        this.chargeCooldownTicks = CHARGE_COOLDOWN_MIN + this.random.nextInt(CHARGE_COOLDOWN_MAX - CHARGE_COOLDOWN_MIN + 1);
    }

    public boolean canStartCharge() {
        return this.chargeCooldownTicks <= 0 && !this.isBaby();
    }

    public void startCharge() {
        this.chargeTicks = CHARGE_DURATION;
        this.setCharging(true);
        this.setSprinting(true);
        this.setGrazing(false);
        this.setSleeping(false);
    }

    public void stopCharge() {
        this.chargeTicks = 0;
        this.setCharging(false);
        this.setSprinting(false);
        this.startChargeCooldown();
    }

    public boolean hasNearbyBaby() {
        return this.nearbyBabyCached;
    }

    public boolean isValidThreat(@Nullable Player player) {
        return player != null && player.isAlive() && !player.isCreative() && !player.isSpectator();
    }

    public double getProtectRange() {
        return this.hasNearbyBaby() ? BABY_PROTECT_RANGE : HERD_PROTECT_RANGE;
    }

    @Nullable
    public Player findNearestThreatPlayer() {
        if (this.isPeacefulDifficulty()) {
            return null;
        }

        Player nearestPlayer = this.level().getNearestPlayer(this, this.getProtectRange());
        if (!this.isValidThreat(nearestPlayer)) {
            return null;
        }
        return nearestPlayer;
    }

    private boolean isPeacefulDifficulty() {
        return this.level().getDifficulty() == Difficulty.PEACEFUL;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ANGER_TIME, 0);
        builder.define(ANGRY, false);
        builder.define(ALERT, false);
        builder.define(SNORTING, false);
        builder.define(CHARGING, false);
        builder.define(GRAZING, false);
        builder.define(SLEEPING, false);
        builder.define(LAST_HURT_TIME, 0L);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BisonGoals.BisonPanicGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Wolf.class, 12.0F, 1.35D, 1.6D));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creeper.class, 10.0F, 1.35D, 1.6D));
        this.goalSelector.addGoal(1, new BisonGoals.BisonHerdGuardGoal(this) {
            @Override
            public boolean canUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(2, new BisonGoals.BisonChargeGoal(this) {
            @Override
            public boolean canUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.15D, true) {
            @Override
            public boolean canUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new BisonGoals.BisonHerdRunGoal(this));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT, Items.HAY_BLOCK), false));
        this.goalSelector.addGoal(7, new FollowParentGoal(this, 1.25D));
        this.goalSelector.addGoal(8, new BisonGoals.BisonHerdAwareStrollGoal(this, 1.0D, 20, 16.0F));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        HurtByTargetGoal hurtByTargetGoal = new HurtByTargetGoal(this) {
            @Override
            public boolean canUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canContinueToUse();
            }
        };
        hurtByTargetGoal.setAlertOthers();
        this.targetSelector.addGoal(1, hurtByTargetGoal);

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, livingEntity -> livingEntity instanceof Player player && BisonEntity.this.isValidThreat(player)) {
            @Override
            public boolean canUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !BisonEntity.this.isPeacefulDifficulty() && super.canContinueToUse();
            }
        });
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return EntityTypeRegistry.BISON.get().create(level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundEventRegistry.BISON_ANGRY.get() : SoundEventRegistry.BISON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.BISON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.BISON_DEATH.get();
    }

    @Override
    public int getMaxHeadYRot() {
        return 35;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        boolean wasHurt = super.hurt(damageSource, damageAmount);
        if (wasHurt && !this.level().isClientSide() && !this.isPeacefulDifficulty()) {
            this.setLastHurtTime(this.level().getGameTime());
            Entity sourceEntity = damageSource.getEntity();
            if (sourceEntity instanceof Player player && this.isValidThreat(player)) {
                this.setAngry(true);
                this.startAlertPhase();
                this.setTarget(player);
                this.calmDownTicks = CALM_DOWN_DURATION;
            }
        }
        return wasHurt;
    }

    @Override
    public boolean doHurtTarget(Entity targetEntity) {
        float attackDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean wasSuccessful = targetEntity.hurt(this.damageSources().mobAttack(this), attackDamage);

        if (wasSuccessful) {
            this.attackAnimationTicks = ATTACK_DURATION;
            this.setAttacking(true);

            if (targetEntity instanceof LivingEntity livingEntity) {
                Vec3 knockbackDirection = livingEntity.position().subtract(this.position());
                if (knockbackDirection.lengthSqr() > 1.0E-4D) {
                    Vec3 knockbackVector = knockbackDirection.normalize().scale(2.5D);
                    livingEntity.push(knockbackVector.x, 0.6D, knockbackVector.z);
                    livingEntity.hurtMarked = true;
                }
            }
        }

        return wasSuccessful;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Attacking", this.isAttacking());
        compound.putBoolean("Angry", this.isAngry());
        compound.putBoolean("Alert", this.isAlert());
        compound.putBoolean("Snorting", this.isSnorting());
        compound.putBoolean("Charging", this.isCharging());
        compound.putBoolean("Grazing", this.isGrazing());
        compound.putBoolean("Sleeping", this.isSleeping());
        compound.putLong("LastHurtTime", this.getLastHurtTime());
        compound.putInt("AttackAnimationTicks", this.attackAnimationTicks);
        compound.putInt("AlertTicks", this.alertTicks);
        compound.putInt("SnortTicks", this.snortTicks);
        compound.putInt("ChargeTicks", this.chargeTicks);
        compound.putInt("ChargeCooldownTicks", this.chargeCooldownTicks);
        compound.putInt("CalmDownTicks", this.calmDownTicks);
        compound.putInt("BabyProtectCacheTicks", this.babyProtectCacheTicks);
        compound.putBoolean("NearbyBabyCached", this.nearbyBabyCached);
        compound.putInt("GrazeTicks", this.grazeTicks);
        compound.putInt("RestTicks", this.restTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setAttacking(compound.getBoolean("Attacking"));
        this.setAngry(compound.getBoolean("Angry"));
        this.setAlert(compound.getBoolean("Alert"));
        this.setSnorting(compound.getBoolean("Snorting"));
        this.setCharging(compound.getBoolean("Charging"));
        this.setGrazing(compound.getBoolean("Grazing"));
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.setLastHurtTime(compound.getLong("LastHurtTime"));
        this.attackAnimationTicks = compound.getInt("AttackAnimationTicks");
        this.alertTicks = compound.getInt("AlertTicks");
        this.snortTicks = compound.getInt("SnortTicks");
        this.chargeTicks = compound.getInt("ChargeTicks");
        this.chargeCooldownTicks = compound.getInt("ChargeCooldownTicks");
        this.calmDownTicks = compound.getInt("CalmDownTicks");
        this.babyProtectCacheTicks = compound.getInt("BabyProtectCacheTicks");
        this.nearbyBabyCached = compound.getBoolean("NearbyBabyCached");
        this.grazeTicks = compound.getInt("GrazeTicks");
        this.restTicks = compound.getInt("RestTicks");
        this.setAttacking(this.attackAnimationTicks > 0);
    }
}