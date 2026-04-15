package net.satisfy.wildernature.core.entity.animal.defensive;

import java.util.List;
import java.util.UUID;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.goal.FollowParentAtDistanceGoal;
import net.satisfy.wildernature.core.entity.ai.goal.animal.ElephantGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ElephantEntity extends Animal {
    private static final int FLAG_THROWING = 0x00000001;
    private static final int FLAG_STAMPING = 0x00000010;
    private static final int FLAG_CHARGING = 0x00000100;
    private static final int FLAG_DRINKING = 0x00001000;
    private static final int FLAG_TRUMPETING = 0x00010000;

    public static final Ingredient FOOD_ITEMS = Ingredient.of(TagsRegistry.ELEPHANT_FOOD);

    public static final double HERD_RADIUS = 18.0D;
    public static final double HERD_RETURN_RADIUS = 10.0D;
    public static final double TERRITORY_BREAK_RADIUS = 38.0D;
    public static final double ALERT_RADIUS = 18.0D;
    public static final double BABY_PROTECT_RADIUS = 12.0D;

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
    private static final int DRINKING_DURATION = 60;
    private static final int DRINKING_COOLDOWN_MIN = 400;
    private static final int DRINKING_COOLDOWN_RANDOM = 200;
    private static final int DRINKING_PARTICLE_INTERVAL = 8;
    private static final int TRUMPETING_DURATION = 30;
    private static final int TRUST_MAX = 100;
    private static final int TRUST_FEED_GAIN = 8;
    private static final int TRUST_PROXIMITY_GAIN_INTERVAL = 100;
    private static final int TRUST_PROXIMITY_GAIN = 1;
    private static final double TRUST_PROXIMITY_RADIUS = 6.0D;
    private static final int TRUST_RIDE_THRESHOLD = 80;
    private static final int STUN_DURATION_TICKS = 30;
    private static final int THREAT_MEMORY_DURATION = 20 * 30;
    private static final int TRUST_TAKEOVER_THRESHOLD = 40;
    private static final int ATTACK_COOLDOWN_TICKS = 80;
    private static final int POST_CHARGE_ATTACK_DELAY_TICKS = 50;
    private static final double MELEE_ATTACK_RANGE_SQR = 4.0D;
    private static final double MAX_ATTACK_VERTICAL_DISTANCE = 1.5D;
    private static final double BABY_PROTECT_TRIGGER_RADIUS = 4.0D;

    private static final double CHARGE_SPEED = 0.38D;

    private static final EntityDataAccessor<Integer> DATA_FLAGS = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TRUST = SynchedEntityData.defineId(ElephantEntity.class, EntityDataSerializers.INT);

    public int throwTicks;
    public int stampTicks;
    public int chargeWindupTicks;
    public int chargeActiveTicks;
    public int chargeRecoveryTicks;
    public boolean throwImpactDone;
    public boolean stampImpactDone;
    public boolean chargeImpactDone;

    private int throwCooldownTicks;
    private int stampCooldownTicks;
    private int chargeCooldownTicks;
    private int drinkingTicks;
    private int drinkingCooldownTicks;
    private int drinkingParticleTicks;
    private int trumpetingTicks;
    private int herdSyncCooldown;
    private int trustProximityTimer;
    private int memorizedThreatTicks;
    private int attackCooldownTicks;
    private double chargeDirectionX;
    private double chargeDirectionZ;
    private BlockPos herdCenter;
    @Nullable
    private UUID trustedPlayerUUID;
    @Nullable
    private UUID memorizedThreatUUID;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState throwState = new AnimationState();
    public final AnimationState stampState = new AnimationState();
    public final AnimationState chargeState = new AnimationState();
    public final AnimationState drinkState = new AnimationState();
    public final AnimationState trumpetState = new AnimationState();

    public ElephantEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 60.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.17D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 28.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.STEP_HEIGHT, 1.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS, 0);
        builder.define(DATA_TRUST, 0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));

        this.goalSelector.addGoal(1, new ElephantGoals.ElephantChargeGoal(this));
        this.goalSelector.addGoal(2, new ElephantGoals.ElephantThrowGoal(this));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.0D, false) {
            @Override
            public boolean canUse() {
                return ElephantEntity.this.canUseCombatGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return ElephantEntity.this.canUseCombatGoal() && super.canContinueToUse();
            }

            @Override
            protected boolean canPerformAttack(LivingEntity target) {
                return this.isTimeToAttack() && this.mob.getSensing().hasLineOfSight(target) && ElephantEntity.this.canPerformDirectAttack(target);
            }
        });
        this.goalSelector.addGoal(4, new ElephantGoals.ElephantStampGoal(this));

        this.goalSelector.addGoal(5, new ElephantGoals.ReturnToHerdGoal(this, 0.9D));
        this.goalSelector.addGoal(6, new BreedGoal(this, 0.9D));
        this.goalSelector.addGoal(7, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(8, new FollowParentAtDistanceGoal(this, 0.75D));
        this.goalSelector.addGoal(9, new ElephantGoals.ElephantDrinkGoal(this));

        this.goalSelector.addGoal(10, new RandomStrollGoal(this, 0.65D, 80) {
            @Override
            public boolean canUse() {
                return ElephantEntity.this.canUseLandIdleGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return ElephantEntity.this.canUseLandIdleGoal() && super.canContinueToUse();
            }
        });

        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                super.start();
                if (ElephantEntity.this.getTarget() instanceof Player player) {
                    ElephantEntity.this.alertHerd(player);
                    ElephantEntity.this.triggerTrumpet();
                    ElephantEntity.this.memorizeThreat(player);
                }
            }
        });

        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, target -> target instanceof Player player && ElephantEntity.this.canTargetPlayer(player)) {
            @Override
            public boolean canUse() {
                return !ElephantEntity.this.isBaby() && !ElephantEntity.this.isBusy() && super.canUse();
            }

            @Override
            public void start() {
                super.start();
                if (this.target instanceof Player player) {
                    ElephantEntity.this.alertHerd(player);
                    ElephantEntity.this.triggerTrumpet();
                    ElephantEntity.this.memorizeThreat(player);
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
        this.drinkingTicks = 0;
        this.drinkingCooldownTicks = 0;
        this.drinkingParticleTicks = 0;
        this.trumpetingTicks = 0;
        this.herdSyncCooldown = this.random.nextInt(HERD_SYNC_INTERVAL);
        this.trustProximityTimer = 0;
        this.memorizedThreatTicks = 0;
        this.attackCooldownTicks = 0;
        this.throwImpactDone = false;
        this.stampImpactDone = false;
        this.chargeImpactDone = false;
        return output;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ElephantFlags", this.entityData.get(DATA_FLAGS));
        tag.putInt("Trust", this.getTrust());
        tag.putInt("ThrowTicks", this.throwTicks);
        tag.putInt("StampTicks", this.stampTicks);
        tag.putInt("ChargeWindupTicks", this.chargeWindupTicks);
        tag.putInt("ChargeActiveTicks", this.chargeActiveTicks);
        tag.putInt("ChargeRecoveryTicks", this.chargeRecoveryTicks);
        tag.putInt("ThrowCooldownTicks", this.throwCooldownTicks);
        tag.putInt("StampCooldownTicks", this.stampCooldownTicks);
        tag.putInt("ChargeCooldownTicks", this.chargeCooldownTicks);
        tag.putInt("DrinkingTicks", this.drinkingTicks);
        tag.putInt("DrinkingCooldownTicks", this.drinkingCooldownTicks);
        tag.putInt("DrinkingParticleTicks", this.drinkingParticleTicks);
        tag.putInt("TrumpetingTicks", this.trumpetingTicks);
        tag.putInt("MemorizedThreatTicks", this.memorizedThreatTicks);
        tag.putInt("AttackCooldownTicks", this.attackCooldownTicks);
        tag.putDouble("ChargeDirectionX", this.chargeDirectionX);
        tag.putDouble("ChargeDirectionZ", this.chargeDirectionZ);
        if (this.herdCenter != null) {
            tag.putInt("HerdCenterX", this.herdCenter.getX());
            tag.putInt("HerdCenterY", this.herdCenter.getY());
            tag.putInt("HerdCenterZ", this.herdCenter.getZ());
        }
        if (this.trustedPlayerUUID != null) {
            tag.putUUID("TrustedPlayer", this.trustedPlayerUUID);
        }
        if (this.memorizedThreatUUID != null) {
            tag.putUUID("MemorizedThreat", this.memorizedThreatUUID);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS, tag.getInt("ElephantFlags"));
        this.setTrust(tag.getInt("Trust"));
        this.throwTicks = tag.getInt("ThrowTicks");
        this.stampTicks = tag.getInt("StampTicks");
        this.chargeWindupTicks = tag.getInt("ChargeWindupTicks");
        this.chargeActiveTicks = tag.getInt("ChargeActiveTicks");
        this.chargeRecoveryTicks = tag.getInt("ChargeRecoveryTicks");
        this.throwCooldownTicks = tag.getInt("ThrowCooldownTicks");
        this.stampCooldownTicks = tag.getInt("StampCooldownTicks");
        this.chargeCooldownTicks = tag.getInt("ChargeCooldownTicks");
        this.drinkingTicks = tag.getInt("DrinkingTicks");
        this.drinkingCooldownTicks = tag.getInt("DrinkingCooldownTicks");
        this.drinkingParticleTicks = tag.getInt("DrinkingParticleTicks");
        this.trumpetingTicks = tag.getInt("TrumpetingTicks");
        this.memorizedThreatTicks = tag.getInt("MemorizedThreatTicks");
        this.attackCooldownTicks = tag.getInt("AttackCooldownTicks");
        this.chargeDirectionX = tag.getDouble("ChargeDirectionX");
        this.chargeDirectionZ = tag.getDouble("ChargeDirectionZ");
        if (tag.contains("HerdCenterX")) {
            this.herdCenter = new BlockPos(tag.getInt("HerdCenterX"), tag.getInt("HerdCenterY"), tag.getInt("HerdCenterZ"));
        } else {
            this.herdCenter = this.blockPosition();
        }
        if (tag.hasUUID("TrustedPlayer")) {
            this.trustedPlayerUUID = tag.getUUID("TrustedPlayer");
        }
        if (tag.hasUUID("MemorizedThreat")) {
            this.memorizedThreatUUID = tag.getUUID("MemorizedThreat");
        }
        this.throwImpactDone = false;
        this.stampImpactDone = false;
        this.chargeImpactDone = false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.ELEPHANT_FOOD);
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
        this.updateDrinkingParticles();
        this.syncHerdCenter();
        this.limitPursuitRange();
        this.updateBabySafety();
        this.updateTrustProximity();
        this.updateBabyProtection();
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
            if (this.chargeWindupTicks == 0) {
                this.chargeActiveTicks = CHARGE_ACTIVE_TICKS;
            }
        }

        if (this.chargeActiveTicks > 0) {
            this.chargeActiveTicks--;
            if (this.chargeActiveTicks == 0) {
                this.chargeRecoveryTicks = CHARGE_RECOVERY_TICKS;
            }
        }

        if (this.chargeRecoveryTicks > 0) {
            this.chargeRecoveryTicks--;
            if (this.chargeRecoveryTicks == 0) {
                this.setCharging(false);
                this.chargeImpactDone = false;
                this.attackCooldownTicks = Math.max(this.attackCooldownTicks, POST_CHARGE_ATTACK_DELAY_TICKS);
            }
        }

        if (this.drinkingTicks > 0) {
            this.drinkingTicks--;
            if (this.drinkingTicks == 0) {
                this.setDrinking(false);
            }
        }

        if (this.trumpetingTicks > 0) {
            this.trumpetingTicks--;
            if (this.trumpetingTicks == 0) {
                this.setTrumpeting(false);
            }
        }

        if (this.memorizedThreatTicks > 0) {
            this.memorizedThreatTicks--;
            if (this.memorizedThreatTicks == 0) {
                this.memorizedThreatUUID = null;
            }
        }

        if (this.throwCooldownTicks > 0) this.throwCooldownTicks--;
        if (this.stampCooldownTicks > 0) this.stampCooldownTicks--;
        if (this.chargeCooldownTicks > 0) this.chargeCooldownTicks--;
        if (this.drinkingCooldownTicks > 0) this.drinkingCooldownTicks--;
        if (this.drinkingParticleTicks > 0) this.drinkingParticleTicks--;
        if (this.herdSyncCooldown > 0) this.herdSyncCooldown--;
        if (this.trustProximityTimer > 0) this.trustProximityTimer--;
        if (this.attackCooldownTicks > 0) this.attackCooldownTicks--;
    }

    private void updateChargeMotion() {
        if (this.chargeActiveTicks <= 0) return;

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            Vec3 targetDirection = new Vec3(target.getX() - this.getX(), 0.0D, target.getZ() - this.getZ());
            if (targetDirection.lengthSqr() > 1.0E-6D) {
                Vec3 normalizedDirection = targetDirection.normalize();
                this.chargeDirectionX = normalizedDirection.x;
                this.chargeDirectionZ = normalizedDirection.z;
                float chargeYaw = (float) (Mth.atan2(normalizedDirection.z, normalizedDirection.x) * 180.0D / Math.PI) - 90.0F;
                this.setYRot(chargeYaw);
                this.yRotO = chargeYaw;
                this.yBodyRot = chargeYaw;
                this.yHeadRot = chargeYaw;
            }
        }

        Vec3 chargeMotion = new Vec3(this.chargeDirectionX * CHARGE_SPEED, this.getDeltaMovement().y, this.chargeDirectionZ * CHARGE_SPEED);
        this.setDeltaMovement(chargeMotion);
        this.move(MoverType.SELF, this.getDeltaMovement());

        if (this.level() instanceof ServerLevel serverLevel && this.tickCount % 3 == 0) {
            BlockPos belowPosition = this.blockPosition().below();
            BlockState groundState = this.level().getBlockState(belowPosition);
            if (!groundState.isAir()) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, groundState), this.getX(), this.getY() + 0.1D, this.getZ(), 12, 0.4D, 0.1D, 0.4D, 0.15D);
            }
        }

        if (!this.chargeImpactDone && target != null && target.isAlive() && this.canPerformDirectAttack(target)) {
            this.chargeImpactDone = true;
            target.hurt(this.damageSources().mobAttack(this), 14.0F);
            Vec3 knockbackDirection = new Vec3(this.chargeDirectionX, 0.0D, this.chargeDirectionZ);
            if (knockbackDirection.lengthSqr() > 1.0E-6D) {
                Vec3 normalizedDirection = knockbackDirection.normalize().scale(1.6D);
                target.push(normalizedDirection.x, 0.4D, normalizedDirection.z);
            }
            if (target instanceof Player player) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, STUN_DURATION_TICKS, 3, false, false));
            }
        }
    }

    private void updateDrinkingParticles() {
        if (!this.isDrinking()) return;
        if (this.drinkingParticleTicks > 0) return;
        this.drinkingParticleTicks = DRINKING_PARTICLE_INTERVAL;

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 1.2D, this.getZ(), 6, 0.3D, 0.1D, 0.3D, 0.05D);
        }
    }

    private void syncHerdCenter() {
        if (this.herdSyncCooldown > 0) return;
        this.herdSyncCooldown = HERD_SYNC_INTERVAL;

        List<ElephantEntity> nearbyElephants = this.level().getEntitiesOfClass(
                ElephantEntity.class,
                this.getBoundingBox().inflate(HERD_RADIUS, 6.0D, HERD_RADIUS),
                elephant -> elephant != this && elephant.isAlive()
        );

        if (nearbyElephants.isEmpty()) {
            if (this.herdCenter == null) {
                this.herdCenter = this.blockPosition();
            }
            return;
        }

        double centerX = this.getX();
        double centerY = this.getY();
        double centerZ = this.getZ();
        int elephantCount = 1;

        for (ElephantEntity elephantEntity : nearbyElephants) {
            centerX += elephantEntity.getX();
            centerY += elephantEntity.getY();
            centerZ += elephantEntity.getZ();
            elephantCount++;
        }

        this.herdCenter = BlockPos.containing(centerX / elephantCount, centerY / elephantCount, centerZ / elephantCount);
    }

    private void limitPursuitRange() {
        if (this.herdCenter == null) {
            this.herdCenter = this.blockPosition();
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null) return;

        if (!target.isAlive()
                || this.distanceToSqr(target) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS
                || target.distanceToSqr(Vec3.atCenterOf(this.herdCenter)) > TERRITORY_BREAK_RADIUS * TERRITORY_BREAK_RADIUS) {
            this.setTarget(null);
            this.getNavigation().stop();
            this.stopAllCombatStates();
        }
    }

    private void updateBabySafety() {
        if (!this.isBaby() || this.herdCenter == null || this.isBusy()) return;

        if (this.blockPosition().distSqr(this.herdCenter) > HERD_RETURN_RADIUS * HERD_RETURN_RADIUS) {
            this.getNavigation().moveTo(this.herdCenter.getX() + 0.5D, this.herdCenter.getY(), this.herdCenter.getZ() + 0.5D, 1.0D);
        }
    }

    private void updateTrustProximity() {
        if (this.trustProximityTimer > 0) return;
        this.trustProximityTimer = TRUST_PROXIMITY_GAIN_INTERVAL;

        if (this.getTarget() != null) return;
        if (this.getTrust() >= TRUST_RIDE_THRESHOLD) return;

        Player nearestPlayer = this.level().getNearestPlayer(this, TRUST_PROXIMITY_RADIUS);
        if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator()) return;

        if (this.trustedPlayerUUID == null || this.trustedPlayerUUID.equals(nearestPlayer.getUUID())) {
            this.trustedPlayerUUID = nearestPlayer.getUUID();
            this.setTrust(Math.min(TRUST_MAX, this.getTrust() + TRUST_PROXIMITY_GAIN));
            return;
        }

        if (this.getTrust() <= TRUST_TAKEOVER_THRESHOLD) {
            this.trustedPlayerUUID = nearestPlayer.getUUID();
            this.setTrust(TRUST_PROXIMITY_GAIN);
        }
    }

    private void updateBabyProtection() {
        if (this.isBaby() || this.getTarget() != null || this.isBusy()) return;

        List<ElephantEntity> nearbyBabies = this.level().getEntitiesOfClass(
                ElephantEntity.class,
                this.getBoundingBox().inflate(BABY_PROTECT_RADIUS),
                elephantEntity -> elephantEntity.isBaby() && elephantEntity != this
        );

        if (nearbyBabies.isEmpty()) return;

        Player nearestThreat = this.level().getNearestPlayer(this, BABY_PROTECT_TRIGGER_RADIUS);
        if (!this.isValidBabyProtectionThreat(nearestThreat)) return;

        this.setTarget(nearestThreat);
        this.alertHerd(nearestThreat);
        this.triggerTrumpet();
        this.memorizeThreat(nearestThreat);
    }

    private void updateAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isThrowing() && !this.isStamping() && !this.isCharging() && !this.isDrinking() && !this.isTrumpeting();

        this.throwState.animateWhen(this.isThrowing(), this.tickCount);
        this.stampState.animateWhen(this.isStamping(), this.tickCount);
        this.chargeState.animateWhen(this.isCharging() || this.chargeWindupTicks > 0, this.tickCount);
        this.drinkState.animateWhen(this.isDrinking(), this.tickCount);
        this.trumpetState.animateWhen(this.isTrumpeting(), this.tickCount);

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

    private void updateTrustParticles(boolean positive) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(positive ? ParticleTypeRegistry.TRUST_POSITIVE.get() : ParticleTypeRegistry.TRUST_NEGATIVE.get(), this.getX(), this.getY() + 3D, this.getZ(), 22, 0.35D, 0.25D, 0.35D, 0.0D);
    }

    public boolean canTargetPlayer(@Nullable Player player) {
        if (player == null || this.isBaby() || player.isCreative() || player.isSpectator()) return false;

        if (this.herdCenter == null) {
            this.herdCenter = this.blockPosition();
        }

        if (this.memorizedThreatUUID != null && this.memorizedThreatUUID.equals(player.getUUID()) && this.memorizedThreatTicks > 0) {
            return true;
        }

        return this.getLastHurtByMob() == player;
    }

    public void memorizeThreat(Player player) {
        this.memorizedThreatUUID = player.getUUID();
        this.memorizedThreatTicks = THREAT_MEMORY_DURATION;
    }

    public void alertHerd(Player player) {
        if (player == null || this.herdCenter == null) return;

        List<ElephantEntity> nearbyElephants = this.level().getEntitiesOfClass(
                ElephantEntity.class,
                this.getBoundingBox().inflate(ALERT_RADIUS, 6.0D, ALERT_RADIUS),
                elephantEntity -> elephantEntity != this && elephantEntity.isAlive() && !elephantEntity.isBaby()
        );

        for (ElephantEntity elephantEntity : nearbyElephants) {
            if (elephantEntity.canRespondToAlert(player)) {
                elephantEntity.setTarget(player);
                elephantEntity.triggerTrumpet();
                elephantEntity.memorizeThreat(player);
            }
        }
    }

    public void triggerTrumpet() {
        if (this.trumpetingTicks > 0) return;
        this.setTrumpeting(true);
        this.trumpetingTicks = TRUMPETING_DURATION;
    }

    private boolean canUseCombatGoal() {
        return !this.isBaby() && !this.isBusy() && this.getTarget() != null && this.getTarget().isAlive();
    }

    private boolean canUseLandIdleGoal() {
        return !this.isBusy() && this.getTarget() == null && this.herdCenter != null
                && this.blockPosition().distSqr(this.herdCenter) <= (HERD_RADIUS + 6.0D) * (HERD_RADIUS + 6.0D);
    }

    private boolean canPerformDirectAttack(LivingEntity target) {
        return this.attackCooldownTicks <= 0
                && target != null
                && target.isAlive()
                && this.distanceToSqr(target) <= MELEE_ATTACK_RANGE_SQR
                && Math.abs(target.getY() - this.getY()) <= MAX_ATTACK_VERTICAL_DISTANCE;
    }

    private boolean canRespondToAlert(Player player) {
        return player != null
                && !player.isCreative()
                && !player.isSpectator()
                && (this.canTargetPlayer(player) || this.isValidBabyProtectionThreat(player));
    }

    private boolean isValidBabyProtectionThreat(@Nullable Player player) {
        return player != null
                && !player.isCreative()
                && !player.isSpectator()
                && this.herdCenter != null
                && player.distanceToSqr(this) <= BABY_PROTECT_TRIGGER_RADIUS * BABY_PROTECT_TRIGGER_RADIUS
                && Math.abs(player.getY() - this.getY()) <= MAX_ATTACK_VERTICAL_DISTANCE
                && player.distanceToSqr(Vec3.atCenterOf(this.getHerdCenter())) <= BABY_PROTECT_RADIUS * BABY_PROTECT_RADIUS
                && this.getSensing().hasLineOfSight(player);
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

    private void setElephantFlag(int flag, boolean value) {
        int currentFlags = this.entityData.get(DATA_FLAGS);
        this.entityData.set(DATA_FLAGS, value ? currentFlags | flag : currentFlags & ~flag);
    }

    private boolean getElephantFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS) & flag) != 0;
    }

    public boolean isThrowing() {
        return this.getElephantFlag(FLAG_THROWING);
    }

    public void setThrowing(boolean value) {
        this.setElephantFlag(FLAG_THROWING, value);
    }

    public boolean isStamping() {
        return this.getElephantFlag(FLAG_STAMPING);
    }

    public void setStamping(boolean value) {
        this.setElephantFlag(FLAG_STAMPING, value);
    }

    public boolean isCharging() {
        return this.getElephantFlag(FLAG_CHARGING);
    }

    public void setCharging(boolean value) {
        this.setElephantFlag(FLAG_CHARGING, value);
    }

    public boolean isDrinking() {
        return this.getElephantFlag(FLAG_DRINKING);
    }

    public void setDrinking(boolean value) {
        this.setElephantFlag(FLAG_DRINKING, value);
    }

    public boolean isTrumpeting() {
        return this.getElephantFlag(FLAG_TRUMPETING);
    }

    public void setTrumpeting(boolean value) {
        this.setElephantFlag(FLAG_TRUMPETING, value);
    }

    public int getTrust() {
        return this.entityData.get(DATA_TRUST);
    }

    public void setTrust(int value) {
        this.entityData.set(DATA_TRUST, Mth.clamp(value, 0, TRUST_MAX));
    }

    public boolean isTrustedBy(Player player) {
        return this.trustedPlayerUUID != null && this.trustedPlayerUUID.equals(player.getUUID()) && this.getTrust() >= TRUST_RIDE_THRESHOLD;
    }

    public void startThrow() {
        this.setThrowing(true);
        this.throwTicks = ElephantGoals.THROW_DURATION;
        this.throwImpactDone = false;
        this.throwCooldownTicks = THROW_COOLDOWN_MIN + this.random.nextInt(THROW_COOLDOWN_RANDOM);
    }

    public void startStamp() {
        this.setStamping(true);
        this.stampTicks = ElephantGoals.STAMP_DURATION;
        this.stampImpactDone = false;
        this.stampCooldownTicks = STAMP_COOLDOWN_MIN + this.random.nextInt(STAMP_COOLDOWN_RANDOM);
    }

    public void startCharge(double directionX, double directionZ) {
        Vec3 direction = new Vec3(directionX, 0.0D, directionZ);
        if (direction.lengthSqr() < 1.0E-6D) return;

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

    public void startDrinking() {
        this.setDrinking(true);
        this.drinkingTicks = DRINKING_DURATION;
        this.drinkingCooldownTicks = DRINKING_COOLDOWN_MIN + this.random.nextInt(DRINKING_COOLDOWN_RANDOM);
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

    public boolean canStartDrinking() {
        return this.drinkingCooldownTicks <= 0 && !this.isBusy() && !this.isDrinking();
    }

    public boolean isBusy() {
        return this.isThrowing() || this.isStamping() || this.isCharging() || this.chargeWindupTicks > 0 || this.chargeRecoveryTicks > 0 || this.isDrinking();
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.isFood(itemStack)) {
            if (!this.level().isClientSide) {
                int previousTrust = this.getTrust();

                if (this.trustedPlayerUUID == null || this.getTrust() <= TRUST_TAKEOVER_THRESHOLD) {
                    this.trustedPlayerUUID = player.getUUID();
                }
                if (this.trustedPlayerUUID.equals(player.getUUID())) {
                    this.setTrust(Math.min(TRUST_MAX, this.getTrust() + TRUST_FEED_GAIN));
                }

                if (this.getTrust() > previousTrust) {
                    this.updateTrustParticles(true);
                }

                this.usePlayerItem(player, hand, itemStack);

                if (this.isBaby()) {
                    this.ageUp(getSpeedUpSecondsWhenFeeding(-this.getAge()), true);
                } else if (this.canFallInLove()) {
                    this.setInLove(player);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        if (!player.isSecondaryUseActive() && itemStack.isEmpty() && this.isTrustedBy(player) && !this.isBaby()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void travel(Vec3 travelVector) {
        LivingEntity controllingPassenger = this.getControllingPassenger();
        if (this.isAlive() && this.isVehicle() && controllingPassenger != null) {
            this.setYRot(controllingPassenger.getYRot());
            this.yRotO = this.getYRot();
            this.setXRot(controllingPassenger.getXRot());
            this.setRot(this.getYRot(), this.getXRot());
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;

            float strafeInput = 0.0F;
            float forwardInput = 1.0F;

            if (controllingPassenger instanceof Player player && player.isShiftKeyDown()) {
                forwardInput = 0.0F;
            }

            this.setSpeed((float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.9F);
            super.travel(new Vec3(strafeInput, travelVector.y, forwardInput));
            return;
        }

        if (this.isVehicle()) {
            super.travel(Vec3.ZERO);
            return;
        }

        super.travel(travelVector);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity firstPassenger = this.getFirstPassenger();
        if (firstPassenger instanceof Player player) {
            if (this.level().isClientSide) return player;
            if (this.isTrustedBy(player)) return player;
        }
        return null;
    }

    @Override
    protected @NotNull Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        Vec3 basePos = super.getPassengerAttachmentPoint(passenger, dimensions, partialTick);
        float rotation = -this.getYRot() * ((float) Math.PI / 180F);

        int passengerIndex = this.getPassengers().indexOf(passenger);
        Vec3 localOffset = switch (passengerIndex) {
            case 0 -> new Vec3(0.0, 0.3, 0.6);
            case 1 -> new Vec3(0.0, 0.3, -0.5);
            case 2 -> new Vec3(0.0, 0.3, 0.5);
            default -> new Vec3(0.0, 0.0, 0.0);
        };

        return basePos.add(localOffset.yRot(rotation));
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengers().size() < 3;
    }

    @Nullable
    @Override
    public ElephantEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.ELEPHANT.get().create(serverLevel);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hasHit = super.doHurtTarget(target);
        if (hasHit) {
            this.attackCooldownTicks = ATTACK_COOLDOWN_TICKS;
        }
        return hasHit;
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
            if (this.trustedPlayerUUID != null && this.trustedPlayerUUID.equals(player.getUUID()) && this.getTrust() > 0) {
                int reducedTrust = Math.max(0, this.getTrust() - TRUST_FEED_GAIN);
                if (reducedTrust < this.getTrust()) {
                    this.setTrust(reducedTrust);
                    this.updateTrustParticles(false);
                }
                if (this.getTrust() <= TRUST_TAKEOVER_THRESHOLD) {
                    this.trustedPlayerUUID = null;
                }
            }

            this.alertHerd(player);
            this.triggerTrumpet();
            this.memorizeThreat(player);
            if (!this.isBaby() && this.canTargetPlayer(player)) {
                this.setTarget(player);
            }
        }
        return wasHurt;
    }

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

    @Override
    public void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEventRegistry.ELEPHANT_STEP.get(), 0.15F, 1.0F);
    }

    @Override
    public float getVoicePitch() {
        return this.isBaby() ? 1.35F : 1.0F;
    }

    @Override
    protected void updateWalkAnimation(float movementAmount) {
        float value = this.getPose() == Pose.STANDING ? Math.min(movementAmount * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(value, 0.2F);
    }

    public BlockPos getHerdCenter() {
        return this.herdCenter == null ? this.blockPosition() : this.herdCenter;
    }
}