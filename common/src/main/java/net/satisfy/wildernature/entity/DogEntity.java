package net.satisfy.wildernature.entity;

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
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.entity.ai.EntityWithAttackAnimation;
import net.satisfy.wildernature.entity.ai.RandomAction;
import net.satisfy.wildernature.entity.ai.RandomActionGoal;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class DogEntity extends TamableAnimal implements EntityWithAttackAnimation {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
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
        return EntityRegistry.DOG.get().create(serverLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new AnimationAttackGoal(this, 1.2f, true,
                (int) (ServerAnimationDurations.dog_bite * 20), 7));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this, 1.25d, 18f, 7f));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.BONE), false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(7, new GoAfterCatGoal(this));
        this.goalSelector.addGoal(7, createRandomActionGoal());

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
                    level().playSound(null, DogEntity.this, SoundRegistry.DOG_AMBIENT.get(),
                            SoundSource.NEUTRAL, 1, 1);
                }
            }

            @Override
            public int duration() {
                return (int) (ServerAnimationDurations.dog_howl * 20);
            }

            @Override
            public float chance() {
                return 0.005f;
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
        handleSittingState();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void handleSittingState() {
        if (!this.level().isClientSide() && this.isTame()
                && this.entityData.get(SITTING) != this.isOrderedToSit()) {
            this.setOrderedToSit(this.entityData.get(SITTING));
        }
    }

    private void setupAnimationStates() {
        handleIdleAnimation();
        howlingAnimationState.animateWhen(this.isHowling(), this.tickCount);
        attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
        sitAnimationState.animateWhen(this.isOrderedToSit(), this.tickCount);
    }

    private void handleIdleAnimation() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    private boolean isAttacking() {
        return entityData.get(ATTACKING);
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
        float f = (this.getPose() == Pose.STANDING) ?
                Math.min(partialTick * 6F, 1f) : 0f;
        this.walkAnimation.update(f, 0.2f);
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
        ItemStack itemstack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            boolean flag = this.isOwnedBy(player) || this.isTame()
                    || (itemstack.is(Items.BONE) && !this.isTame());
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            return handleServerSideInteraction(player, itemstack, hand);
        }
    }

    private InteractionResult handleServerSideInteraction(Player player, ItemStack itemstack, InteractionHand hand) {
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (itemstack.is(Items.BONE)) {
                    return handleHealing();
                } else {
                    return handleNonBoneInteraction(player);
                }
            }
        } else if (itemstack.is(Items.BONE)) {
            return handleTaming(player, itemstack);
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
        InteractionResult interactionresult = super.mobInteract(player, InteractionHand.MAIN_HAND);
        if (!interactionresult.consumesAction() || this.isBaby()) {
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }
        return interactionresult;
    }

    private InteractionResult handleTaming(Player player, ItemStack itemstack) {
        this.usePlayerItem(player, InteractionHand.MAIN_HAND, itemstack);
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

    @Override
    public LivingEntity getTarget_() {
        return getTarget();
    }

    @Override
    public double getMeleeAttackRangeSqr_(LivingEntity target) {
        return this.distanceToSqr(target);
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

    public static class GoAfterCatGoal extends Goal {
        private final DogEntity dog;
        private List<Cat> catList;
        private int lastCatUpdate = 0;
        private Cat targetCat;

        private static final int CAT_SEARCH_INTERVAL = 20;
        private static final double CAT_DETECTION_RANGE_SQR = 16 * 16;

        public GoAfterCatGoal(DogEntity dogEntity) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
            this.dog = dogEntity;
        }

        @Override
        public boolean canUse() {
            return !getNearbyCats().isEmpty();
        }

        @Override
        public void start() {
            super.start();
            updateTargetCat();
        }

        @Override
        public void tick() {
            super.tick();
            updateTargetCat();
            if (this.targetCat != null) {
                this.dog.getNavigation().moveTo(this.targetCat, 1.5);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetCat != null && this.targetCat.isAlive()
                    && this.targetCat.distanceToSqr(this.dog) <= CAT_DETECTION_RANGE_SQR;
        }

        @Override
        public void stop() {
            this.targetCat = null;
        }

        private List<Cat> getNearbyCats() {
            if (this.catList == null || dog.tickCount - lastCatUpdate >= CAT_SEARCH_INTERVAL) {
                this.catList = dog.level().getNearbyEntities(Cat.class,
                        TargetingConditions.forNonCombat(), dog,
                        dog.getBoundingBox().inflate(16));
                lastCatUpdate = dog.tickCount;
            }
            return this.catList;
        }

        private void updateTargetCat() {
            if (this.targetCat == null || this.targetCat.distanceToSqr(this.dog) > CAT_DETECTION_RANGE_SQR) {
                double closestDistance = Double.MAX_VALUE;
                Cat closestCat = null;

                for (Cat cat : this.catList) {
                    double distance = cat.distanceToSqr(this.dog);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestCat = cat;
                    }
                }

                this.targetCat = closestCat;
            }
        }
    }
}
