package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.satisfy.wildernature.core.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.core.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PelicanEntity extends Animal {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.COD, Items.SALMON, Items.PUFFERFISH, Items.COOKED_COD, Items.COOKED_SALMON);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(PelicanEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    public PelicanEntity(EntityType<? extends PelicanEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 0.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0D)
                .add(Attributes.ATTACK_SPEED, 1.0D);
    }

    @Override
    protected void registerGoals() {
        int goalPriority = 0;
        this.goalSelector.addGoal(++goalPriority, new AnimationAttackGoal<>(this, 1.0D, true, (int) (ServerAnimationDurations.turkey_attack + 2), 8, this::setAttacking));
        this.goalSelector.addGoal(++goalPriority, new FloatGoal(this));
        this.goalSelector.addGoal(++goalPriority, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(++goalPriority, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(++goalPriority, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(++goalPriority, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(0, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        boolean attacking = this.isAttacking();
        boolean idleAllowed = !moving && !attacking;

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.attackAnimationState.animateWhen(attacking, this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float walkSpeed;
        if (this.getPose() == Pose.STANDING) {
            walkSpeed = Math.min(partialTick * 6.0F, 1.0F);
        } else {
            walkSpeed = 0.0F;
        }

        this.walkAnimation.update(walkSpeed, 0.2F);
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
        return EntityTypeRegistry.PELICAN.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }
}