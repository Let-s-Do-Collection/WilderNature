package net.satisfy.wildernature.core.entity.animal.neutral;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TurkeyEntity extends Chicken {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(
            Items.WHEAT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.BEETROOT_SEEDS,
            Items.BREAD
    );
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TurkeyEntity.class, EntityDataSerializers.BOOLEAN);
    public static final int TURKEY_ATTACK_TICKS = (int) (1.6F * 20) + 2;

    public AnimationState attackAnimationState = new AnimationState();
    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    public boolean isPelicanJockey;

    private int attackAnimationTicks;
    private int turkeyEggTime;
    private float nextFlap = 1.0F;

    public TurkeyEntity(EntityType<? extends TurkeyEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
        this.turkeyEggTime = this.random.nextInt(6000) + 6000;
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.ATTACK_DAMAGE, 1.25D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new FloatGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
        }

        this.setAttacking(this.attackAnimationTicks > 0);

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        boolean wasHurt = super.hurt(damageSource, damageAmount);

        if (wasHurt && !this.level().isClientSide()) {
            Entity sourceEntity = damageSource.getEntity();
            if (sourceEntity instanceof LivingEntity livingEntity) {
                this.setTarget(livingEntity);
            }
        }

        return wasHurt;
    }

    @Override
    public boolean doHurtTarget(Entity targetEntity) {
        boolean success = super.doHurtTarget(targetEntity);

        if (success) {
            this.attackAnimationTicks = TURKEY_ATTACK_TICKS;
            this.setAttacking(true);
        }

        return success;
    }

    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("AttackAnimationTicks", this.attackAnimationTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.attackAnimationTicks = compound.getInt("AttackAnimationTicks");
        this.setAttacking(this.attackAnimationTicks > 0);
    }

    @Override
    public void aiStep() {
        this.eggTime = Integer.MAX_VALUE;

        super.aiStep();

        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (this.onGround() ? -1.0F : 3.0F) * 0.2F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 0.5F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 movement = this.getDeltaMovement();
        if (!this.onGround() && movement.y < 0.0D) {
            this.setDeltaMovement(movement.multiply(1.0D, 0.6D, 1.0D));
        }

        this.flap += this.flapping * 2.0F;

        if (!this.level().isClientSide() && this.isAlive() && !this.isBaby() && --this.turkeyEggTime <= 0) {
            this.playSound(SoundEvents.CHICKEN_EGG, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
            this.spawnAtLocation(ObjectRegistry.TURKEY_EGG.get());
            this.turkeyEggTime = this.random.nextInt(6000) + 6000;
        }
    }

    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.TURKEY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.TURKEY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.TURKEY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    @Nullable
    public TurkeyEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.TURKEY.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override
    protected int getBaseExperienceReward() {
        return this.isPelicanJockey() ? 12 : super.getBaseExperienceReward();
    }

    @Override
    protected void positionRider(Entity entity, MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        float sineYaw = Mth.sin(this.yBodyRot * 0.017453292F);
        float cosineYaw = Mth.cos(this.yBodyRot * 0.017453292F);
        double verticalOffset = -0.18D;

        moveFunction.accept(
                entity,
                this.getX() + 0.1F * sineYaw,
                this.getY(0.5D) + entity.getVehicleAttachmentPoint(this).y + verticalOffset,
                this.getZ() - 0.1F * cosineYaw
        );

        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.yBodyRot = this.yBodyRot;
        }
    }

    public boolean isPelicanJockey() {
        return this.isPelicanJockey;
    }
}