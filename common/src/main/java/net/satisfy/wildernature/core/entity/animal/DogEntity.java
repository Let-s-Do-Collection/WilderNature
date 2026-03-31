package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.satisfy.wildernature.core.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.core.entity.ai.RandomAction;
import net.satisfy.wildernature.core.entity.ai.RandomActionGoal;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class DogEntity extends TamableAnimal {
    public final AnimationState idleAnimationState = new AnimationState();
    public AnimationState howlingAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> HOWLING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);

    private static final double MOVEMENT_SPEED = 0.23;
    private static final double MAX_HEALTH = 12.0;
    private static final double ATTACK_DAMAGE = 3.0;
    private static final float SOUND_VOLUME = 0.3F;
    private static final int BITE_DURATION = 34;
    private static final int HOWL_DURATION = 70;

    public DogEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Nullable
    @Override
    public DogEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.DOG.get().create(serverLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new AnimationAttackGoal<>(this, 1.2D, true, BITE_DURATION, 7, this::setAttacking));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.25D, 18.0F, 7.0F));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.BONE), false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3.0F));
        this.goalSelector.addGoal(6, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(7, new GoAfterCatGoal(this));
        this.goalSelector.addGoal(7, this.createRandomActionGoal());

        this.targetSelector.addGoal(10, new HurtByTargetGoal(this));
    }

    private RandomActionGoal createRandomActionGoal() {
        return new RandomActionGoal(new RandomAction() {
            @Override
            public boolean isInterruptable() {
                return false;
            }

            @Override
            public void onStart() {
                setHowling(true);
            }

            @Override
            public void onStop() {
                setHowling(false);
            }

            @Override
            public boolean isPossible() {
                return true;
            }

            @Override
            public void onTick(int tick) {
                if (tick == 20) {
                    level().playSound(null, DogEntity.this, SoundRegistry.DOG_AMBIENT.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
                }
            }

            @Override
            public int duration() {
                return HOWL_DURATION;
            }

            @Override
            public float chance() {
                return 0.005F;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeed) {
                return DogEntity.this.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(movementSpeed));
            }
        });
    }

    @Override
    public void tick() {
        super.tick();
        this.handleSittingState();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void handleSittingState() {
        if (!this.level().isClientSide() && this.isTame() && this.entityData.get(SITTING) != this.isOrderedToSit()) {
            this.setOrderedToSit(this.entityData.get(SITTING));
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        boolean idleAllowed = !moving && !this.isOrderedToSit() && !this.isHowling() && !this.isAttacking();

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.howlingAnimationState.animateWhen(this.isHowling(), this.tickCount);
        this.attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
        this.sitAnimationState.animateWhen(this.isOrderedToSit(), this.tickCount);
    }

    private boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    private boolean isHowling() {
        return this.entityData.get(HOWLING);
    }

    public void setHowling(boolean howling) {
        this.entityData.set(HOWLING, howling);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOWLING, false);
        builder.define(ATTACKING, false);
        builder.define(SITTING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", this.isOrderedToSit());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        boolean sitting = compound.getBoolean("Sitting");
        this.setOrderedToSit(false);
        this.setOrderedToSit(sitting);
        this.entityData.set(SITTING, sitting);
    }

    @Override
    public void setOrderedToSit(boolean sitting) {
        super.setOrderedToSit(sitting);
        this.entityData.set(SITTING, sitting);
    }

    @Override
    public boolean isOrderedToSit() {
        return this.entityData.get(SITTING);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float walkSpeed = this.getPose() == Pose.STANDING ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(walkSpeed, 0.2F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.DOG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.DOG_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.BONE);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            boolean shouldConsume = this.isOwnedBy(player) || this.isTame() || itemStack.is(Items.BONE) && !this.isTame();
            return shouldConsume ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return this.handleServerSideInteraction(player, itemStack, hand);
    }

    private InteractionResult handleServerSideInteraction(Player player, ItemStack itemStack, InteractionHand hand) {
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (itemStack.is(Items.BONE)) {
                    return this.handleHealing();
                }

                return this.handleNonBoneInteraction(player);
            }
        } else if (itemStack.is(Items.BONE)) {
            return this.handleTaming(player, itemStack);
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult handleHealing() {
        if (this.getHealth() < this.getMaxHealth()) {
            this.heal(2.0F);
            this.gameEvent(GameEvent.ENTITY_INTERACT, this);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.CONSUME;
    }

    private InteractionResult handleNonBoneInteraction(Player player) {
        InteractionResult interactionResult = super.mobInteract(player, InteractionHand.MAIN_HAND);
        if (!interactionResult.consumesAction() || this.isBaby()) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }

        return interactionResult;
    }

    private InteractionResult handleTaming(Player player, ItemStack itemStack) {
        this.usePlayerItem(player, InteractionHand.MAIN_HAND, itemStack);
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte) 7);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
        }

        return InteractionResult.SUCCESS;
    }

    public static class GoAfterCatGoal extends Goal {
        private static final int CAT_SEARCH_INTERVAL = 20;
        private static final double CAT_DETECTION_RANGE_SQR = 256.0D;

        private final DogEntity dog;
        private List<Cat> catList;
        private int lastCatUpdate;
        private Cat targetCat;

        public GoAfterCatGoal(DogEntity dogEntity) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
            this.dog = dogEntity;
        }

        @Override
        public boolean canUse() {
            return !this.getNearbyCats().isEmpty();
        }

        @Override
        public void start() {
            super.start();
            this.updateTargetCat();
        }

        @Override
        public void tick() {
            super.tick();
            this.updateTargetCat();
            if (this.targetCat != null) {
                this.dog.getNavigation().moveTo(this.targetCat, 1.5D);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetCat != null && this.targetCat.isAlive() && this.targetCat.distanceToSqr(this.dog) <= CAT_DETECTION_RANGE_SQR;
        }

        @Override
        public void stop() {
            this.targetCat = null;
        }

        private List<Cat> getNearbyCats() {
            if (this.catList == null || this.dog.tickCount - this.lastCatUpdate >= CAT_SEARCH_INTERVAL) {
                this.catList = this.dog.level().getNearbyEntities(Cat.class, TargetingConditions.forNonCombat(), this.dog, this.dog.getBoundingBox().inflate(16.0D));
                this.lastCatUpdate = this.dog.tickCount;
            }

            return this.catList;
        }

        private void updateTargetCat() {
            if (this.targetCat == null || this.targetCat.distanceToSqr(this.dog) > CAT_DETECTION_RANGE_SQR) {
                double closestDistance = Double.MAX_VALUE;
                Cat closestCat = null;

                for (Cat catEntity : this.catList) {
                    double distance = catEntity.distanceToSqr(this.dog);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestCat = catEntity;
                    }
                }

                this.targetCat = closestCat;
            }
        }
    }
}