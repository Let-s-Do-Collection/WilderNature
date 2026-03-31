package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedWolfEntity extends Wolf {
    private static final EntityDataAccessor<Boolean> LEAPING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int ATTACK_DURATION = 40;

    public final AnimationState attackState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    private int lastTargetTick;

    public RedWolfEntity(EntityType<? extends Wolf> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896D)
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return SoundRegistry.RED_WOLF_AGGRO.get();
        } else if (this.random.nextInt(3) == 0) {
            return this.isTame() && this.getHealth() < 10.0F ? SoundEvents.WOLF_WHINE : SoundEvents.WOLF_PANT;
        } else {
            return SoundRegistry.RED_WOLF_AMBIENT.get();
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BEEF)
                || stack.is(Items.PORKCHOP)
                || stack.is(Items.MUTTON)
                || stack.is(Items.CHICKEN)
                || stack.is(Items.RABBIT);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5D) {
            @Override
            protected boolean shouldPanic() {
                return this.mob.isFreezing() || this.mob.isOnFire();
            }
        });
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5D, 1.5D) {
            @Override
            public boolean canUse() {
                if (super.canUse() && this.toAvoid instanceof Llama llamaEntity) {
                    return !RedWolfEntity.this.isTame() && this.avoidLlama(llamaEntity);
                }
                return false;
            }

            private boolean avoidLlama(Llama llamaEntity) {
                return llamaEntity.getStrength() >= RedWolfEntity.this.random.nextInt(5);
            }

            @Override
            public void start() {
                RedWolfEntity.this.setTarget(null);
                super.start();
            }

            @Override
            public void tick() {
                RedWolfEntity.this.setTarget(null);
                super.tick();
            }
        });
        this.goalSelector.addGoal(5, new AnimationAttackGoal<>(this, 1.0D, true, ATTACK_DURATION, 4, this::setAttacking));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0D, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.lastTargetTick++;
            if (this.getTarget() != null) {
                this.lastTargetTick = 0;
            }
            this.setSneaking(this.lastTargetTick < 10);
        }

        if (this.level().isClientSide()) {
            this.attackState.animateWhen(this.isAttacking(), this.tickCount);
            this.sitAnimationState.animateWhen(this.isSitting(), this.tickCount);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEAPING, false);
        builder.define(ATTACKING, false);
        builder.define(SITTING, false);
    }

    private void setSneaking(boolean sneaking) {
        this.entityData.set(LEAPING, sneaking);
    }

    public boolean isSneaking() {
        return this.entityData.get(LEAPING);
    }

    @Nullable
    @Override
    public RedWolfEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.RED_WOLF.get().create(serverLevel);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.RED_WOLF_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.RED_WOLF_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }

    @Override
    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 16 : super.getMaxHeadXRot();
    }

    @Override
    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        }
        return this.isTame() ? (0.55F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * (float) Math.PI : 0.62831855F;
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isSitting() {
        return this.entityData.get(SITTING);
    }

    public void setSitting(boolean sitting) {
        this.entityData.set(SITTING, sitting);
    }

    @Override
    public boolean isOrderedToSit() {
        return this.isSitting();
    }

    @Override
    public void setOrderedToSit(boolean sitting) {
        super.setOrderedToSit(sitting);
        this.setSitting(sitting);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", this.isSitting());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        boolean sitting = compound.getBoolean("Sitting");
        this.setOrderedToSit(sitting);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.level().isClientSide()) {
            boolean shouldConsume = this.isOwnedBy(player) || this.isTame();
            return shouldConsume ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        if (this.isOwnedBy(player)) {
            this.setSitting(!this.isSitting());
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }
}