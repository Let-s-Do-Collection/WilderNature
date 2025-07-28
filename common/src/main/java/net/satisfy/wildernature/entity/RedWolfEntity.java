package net.satisfy.wildernature.entity;

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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.entity.ai.EntityWithAttackAnimation;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RedWolfEntity extends Wolf implements EntityWithAttackAnimation {
    public AnimationState attackState = new AnimationState();
    public AnimationState sitAnimationState = new AnimationState();
    int lastTargetTick;

    private static final EntityDataAccessor<Boolean> LEAPING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(RedWolfEntity.class, EntityDataSerializers.BOOLEAN);

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.30000001192092896)
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    public RedWolfEntity(EntityType<? extends Wolf> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public LivingEntity getTarget_() {
        return getTarget();
    }

    public double getMeleeAttackRangeSqr_(LivingEntity target) {
        return this.distanceToSqr(target);
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
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.5) {
            @Override
            protected boolean shouldPanic() {
                return this.mob.isFreezing() || this.mob.isOnFire();
            }
        });
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new AvoidEntityGoal<>(this, Llama.class, 24.0F, 1.5, 1.5) {
            @Override
            public boolean canUse() {
                if (super.canUse() && this.toAvoid instanceof Llama) {
                    return !RedWolfEntity.this.isTame() && this.avoidLlama(this.toAvoid);
                } else {
                    return false;
                }
            }

            private boolean avoidLlama(Llama llama) {
                return llama.getStrength() >= RedWolfEntity.this.random.nextInt(5);
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
        this.goalSelector.addGoal(5, new AnimationAttackGoal(this, 1.0, true, (int) (ServerAnimationDurations.red_wolf_attack * 20), 4));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, (new HurtByTargetGoal(this)).setAlertOthers());
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide()) {
            lastTargetTick++;
            if (getTarget() != null) {
                lastTargetTick = 0;
            }
            setSneaking(lastTargetTick < 10);
        }

        if (level().isClientSide()) {
            attackState.animateWhen(isAttacking(), this.tickCount);
            sitAnimationState.animateWhen(isSitting(), this.tickCount);
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        sitAnimationState.animateWhen(this.isSitting(), this.tickCount);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEAPING, false);
        builder.define(ATTACKING, false);
        builder.define(SITTING, false);
    }

    private void setSneaking(boolean b) {
        this.entityData.set(LEAPING, b);
    }

    public boolean isSneaking() {
        return this.entityData.get(LEAPING);
    }

    @Nullable
    public RedWolfEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.RED_WOLF.get().create(serverLevel);
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

    public int getMaxHeadXRot() {
        return this.isInSittingPose() ? 16 : super.getMaxHeadXRot();
    }

    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804F;
        } else {
            return this.isTame() ? (0.55F - (this.getMaxHealth() - this.getHealth()) * 0.02F) * (float) Math.PI : 0.62831855F;
        }
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    public void setAttacking_(boolean b) {
        this.entityData.set(ATTACKING, b);
    }

    @Override
    public Vec3 getPosition_(int i) {
        return super.getPosition(i);
    }

    @Override
    public void doHurtTarget_(LivingEntity targetEntity) {
        super.doHurtTarget(targetEntity);
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
        this.setSitting(sitting);
    }

    @Override
    @SuppressWarnings("unused")
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            boolean flag = this.isOwnedBy(player) || this.isTame();
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isOwnedBy(player)) {
                this.setSitting(!this.isSitting());
                return InteractionResult.SUCCESS;
            }
            return super.mobInteract(player, hand);
        }
    }
}
