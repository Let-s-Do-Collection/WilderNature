package net.satisfy.wildernature.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
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
import net.satisfy.wildernature.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.entity.ai.EntityWithAttackAnimation;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TurkeyEntity extends Chicken implements EntityWithAttackAnimation {
    private static final Ingredient FOOD_ITEMS;
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(TurkeyEntity.class, EntityDataSerializers.BOOLEAN);
    public AnimationState attackAnimationState = new AnimationState();

    static {
        FOOD_ITEMS = Ingredient.of(
                Items.WHEAT_SEEDS,
                Items.MELON_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.BEETROOT_SEEDS,
                Items.BREAD
        );
    }

    public float flap;
    public float flapSpeed;
    public float oFlapSpeed;
    public float oFlap;
    public float flapping = 1.0F;
    public boolean isPelicanJockey;
    private float nextFlap = 1.0F;


    public TurkeyEntity(EntityType<? extends TurkeyEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        attackAnimationState.animateWhen(this.entityData.get(ATTACKING), this.tickCount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
    }


    @Override
    public LivingEntity getTarget_() {
        return getTarget();
    }

    @Override
    public double getMeleeAttackRangeSqr_(LivingEntity target) {
        return this.distanceToSqr(target);
    }

    public void setAttacking_(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public Vec3 getPosition_(int i) {
        return super.getPosition(i);
    }

    @Override
    public void doHurtTarget_(LivingEntity targetEntity) {
        super.doHurtTarget(targetEntity);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).add(Attributes.MOVEMENT_SPEED, 0.24)
                .add(Attributes.ATTACK_DAMAGE, 1.25);
    }

    @Override
    protected void registerGoals() {
        int i = 0;
        this.goalSelector.addGoal(++i, new AnimationAttackGoal(this, 1.0D, true, (int) (ServerAnimationDurations.turkey_attack * 20 + 2), 8));
        this.goalSelector.addGoal(++i, new FloatGoal(this));
        this.goalSelector.addGoal(++i, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(++i, new TemptGoal(this, 1.0, FOOD_ITEMS, false));
        this.goalSelector.addGoal(++i, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(++i, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(++i, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(++i, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        this.oFlap = this.flap;
        this.oFlapSpeed = this.flapSpeed;
        this.flapSpeed += (this.onGround() ? -1.0F : 3.0F) * 0.2F;
        this.flapSpeed = Mth.clamp(this.flapSpeed, 0.0F, 0.5F);
        if (!this.onGround() && this.flapping < 1.0F) {
            this.flapping = 1.0F;
        }

        this.flapping *= 0.9F;
        Vec3 vec3 = this.getDeltaMovement();
        if (!this.onGround() && vec3.y < 0.0) {
            this.setDeltaMovement(vec3.multiply(1.0, 0.6, 1.0));
        }

        this.flap += this.flapping * 2.0F;
    }

    protected boolean isFlapping() {
        return this.flyDist > this.nextFlap;
    }

    protected void onFlap() {
        this.nextFlap = this.flyDist + this.flapSpeed / 2.0F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.TURKEY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.TURKEY_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.TURKEY_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    @Nullable
    public TurkeyEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.TURKEY.get().create(serverLevel);
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
        float f = Mth.sin(this.yBodyRot * 0.017453292F);
        float g = Mth.cos(this.yBodyRot * 0.017453292F);
        double yOffset = -0.18;

        moveFunction.accept(entity, this.getX() + (double) (0.1F * f), this.getY(0.5) + entity.getVehicleAttachmentPoint(this).y + yOffset, this.getZ() - (double) (0.1F * g));

        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).yBodyRot = this.yBodyRot;
        }
    }


    public boolean isPelicanJockey() {
        return this.isPelicanJockey;
    }
}


