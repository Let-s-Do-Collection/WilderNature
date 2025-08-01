package net.satisfy.wildernature.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
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

public class PelicanEntity extends Animal implements EntityWithAttackAnimation {
    private static final Ingredient FOOD_ITEMS;
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(PelicanEntity.class, EntityDataSerializers.BOOLEAN);
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        attackAnimationState.animateWhen(isAttacking(), this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
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

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
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

    public double getMeleeAttackRangeSqr_(LivingEntity entity) {
        return this.distanceToSqr(entity);
    }

    static {
        FOOD_ITEMS = Ingredient.of(Items.COD, Items.SALMON, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON);
    }

    public PelicanEntity(EntityType<? extends PelicanEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 8).add(Attributes.FOLLOW_RANGE, 24D).add(Attributes.MOVEMENT_SPEED, 0.22).add(Attributes.ATTACK_DAMAGE, 0.5f).add(Attributes.ATTACK_KNOCKBACK, 0D).add(Attributes.ATTACK_SPEED, 1F);
    }

    @Override
    protected void registerGoals() {
        int i = 0;
        this.goalSelector.addGoal(++i, new AnimationAttackGoal(this, 1.0D, true, (int) (ServerAnimationDurations.turkey_attack + 2), 8));
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
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.PELICAN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.PELICAN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.PELICAN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    @Nullable
    public PelicanEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.PELICAN.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }
}
