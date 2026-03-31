package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.*;
import net.satisfy.wildernature.core.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class OwlEntity extends ShoulderRidingEntity {
    private static final EntityDataAccessor<Integer> STANDING_STATE = SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HOOTING = SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(OwlEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Predicate<LivingEntity> IS_OWL_TARGET = entity -> entity.getType().is(TagsRegistry.OWL_TARGETS);

    private float leaningPitch;
    private float lastLeaningPitch;
    private int sleepCooldownTicks;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int huntScheduleNight = -1;
    private boolean huntsThisNight;
    private int huntStartTime;
    private int huntEndTime;
    private int rottenFleshCooldownTicks;

    public AnimationState flyingState = new AnimationState();
    public AnimationState hootState = new AnimationState();
    public AnimationState attackState = new AnimationState();
    public AnimationState sleepState = new AnimationState();
    public AnimationState idleState = new AnimationState();

    public OwlEntity(EntityType<? extends ShoulderRidingEntity> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 0, false);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0F);
        this.requiredSleepPreparationTicks = this.getRandomSleepPreparationTicks();
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        int goalPriority = 0;

        this.goalSelector.addGoal(++goalPriority, new AvoidEntityGoal<>(this, Player.class, 32.0F, 2.0D, 2.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && isInPanicRightNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new PanicGoal(this, 1.75D) {
            @Override
            protected boolean findRandomPosition() {
                Vec3 randomPosition = DefaultRandomPos.getPos(this.mob, 20, 20);
                if (randomPosition == null) {
                    return false;
                }
                this.posX = randomPosition.x;
                this.posY = randomPosition.y;
                this.posZ = randomPosition.z;
                return true;
            }
        });

        this.goalSelector.addGoal(++goalPriority, new OwlGoals.EatRottenFleshGoal(this, 1.15D));
        this.goalSelector.addGoal(++goalPriority, new OwlGoals.MoveToSleepPerchGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new OwlGoals.SleepGoal(this));
        this.goalSelector.addGoal(++goalPriority, new BreedGoal(this, 1.0D));

        this.goalSelector.addGoal(++goalPriority, new FloatGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new SitWhenOrderedToGoal(this));

        this.goalSelector.addGoal(++goalPriority, new AnimationAttackGoal<>(this, 1.0D, true, (int) (ServerAnimationDurations.owl_attack * 20), 15, this::setAttacking) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior() && canHuntNow();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior() && canHuntNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new OwlGoals.FlyingFollowOwnerGoal(this, 1.2D, 10.0F, 2.0F, true) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior() && !canHuntNow();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior() && !canHuntNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new OwlGoals.PredicateTemptGoal(this, 1.2D, this::isFood, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior() && !canHuntNow();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior() && !canHuntNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new LookAtPlayerGoal(this, Player.class, 8.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new OwlGoals.ExtendedFlyOntoTree(this, 1.0D, 0.5F) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior() && !canSearchForSleepPerch() && !canHuntNow();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior() && !canSearchForSleepPerch() && !canHuntNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior() && !canSearchForSleepPerch() && !canHuntNow();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior() && !canSearchForSleepPerch() && !canHuntNow();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && canUseActiveBehavior();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && canUseActiveBehavior();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new RandomActionGoal(new RandomAction() {
            @Override
            public boolean isInterruptable() {
                return false;
            }

            @Override
            public void onStop() {
                setHooting(false);
            }

            @Override
            public void onStart() {
                if (onGround()) {
                    setHooting(true);
                }
                SoundEvent owlAmbientSound = SoundRegistry.OWL_AMBIENT.get();
                level().playSound(null, OwlEntity.this, owlAmbientSound, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }

            @Override
            public boolean isPossible() {
                return !isAttacking() && canUseActiveBehavior() && !canSearchForSleepPerch() && !canHuntNow();
            }

            @Override
            public int duration() {
                return (int) (ServerAnimationDurations.owl_hoot * 20);
            }

            @Override
            public float chance() {
                return 0.01F;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeedAttribute) {
                return OwlEntity.this.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(movementSpeedAttribute));
            }
        }));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, entity -> canHuntNow() && entity.getType().is(EntityTypeTags.UNDEAD)) {
            @Override
            public boolean canUse() {
                return canHuntNow() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return canHuntNow() && super.canContinueToUse();
            }
        });
        this.targetSelector.addGoal(4, new NonTameRandomTargetGoal<>(this, LivingEntity.class, false, IS_OWL_TARGET));
    }

    @Override
    public void tick() {
        super.tick();

        this.updateNightHuntSchedule();

        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        if (this.rottenFleshCooldownTicks > 0) {
            this.rottenFleshCooldownTicks--;
        }

        if (this.canPrepareForSleep()) {
            this.sleepPreparationTicks++;
        } else {
            this.resetSleepPreparation();
        }

        if (this.isSleeping() && !this.canContinueSleeping()) {
            this.wakeUp();
        }

        this.setStandingState(this.onGround() || this.isInWater() || this.isOrderedToSit() || this.isSleeping() || this.isValidSleepPerch() ? StandingState.STANDING : StandingState.FLYING);

        this.lastLeaningPitch = this.leaningPitch;
        switch (this.getStandingState()) {
            case STANDING -> this.leaningPitch = Math.max(0.0F, this.leaningPitch - 2.0F);
            case FLYING -> this.leaningPitch = Math.min(7.0F, this.leaningPitch + 1.5F);
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            this.spawnSleepingParticle();
        }
    }

    private void updateNightHuntSchedule() {
        long currentNight = this.level().getDayTime() / 24000L;
        if (currentNight == this.huntScheduleNight) {
            return;
        }

        this.huntScheduleNight = (int) currentNight;
        this.huntsThisNight = this.random.nextFloat() < 0.4F;

        if (this.huntsThisNight) {
            this.huntStartTime = 13000 + this.random.nextInt(9000);
            int huntDuration = 600 + this.random.nextInt(1600);
            this.huntEndTime = Math.min(23999, this.huntStartTime + huntDuration);
        } else {
            this.huntStartTime = 0;
            this.huntEndTime = 0;
        }
    }

    public boolean canHuntNow() {
        if (!this.level().isNight()) {
            return false;
        }

        if (this.isSleeping()) {
            return false;
        }

        int timeOfDay = (int) (this.level().getDayTime() % 24000L);
        return this.huntsThisNight && timeOfDay >= this.huntStartTime && timeOfDay <= this.huntEndTime;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        Vec3 velocity = this.getDeltaMovement();
        if (!this.onGround() && velocity.y < 0.0D) {
            this.setDeltaMovement(velocity.multiply(1.0D, 0.75D, 1.0D));
        }
    }

    @Override
    public void setTame(boolean tamed, boolean applyTamingSideEffects) {
        super.setTame(tamed, applyTamingSideEffects);

        if (tamed) {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(20.0D);
            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(4.0D);
            this.setHealth(20.0F);
        } else {
            Objects.requireNonNull(this.getAttribute(Attributes.MAX_HEALTH)).setBaseValue(6.0D);
            Objects.requireNonNull(this.getAttribute(Attributes.ATTACK_DAMAGE)).setBaseValue(2.0D);
        }
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        Item item = itemStack.getItem();

        if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                if (!this.level().isClientSide()) {
                    if (!player.isCreative()) {
                        itemStack.shrink(1);
                    }
                    this.heal((float) item.getDefaultInstance().getOrDefault(DataComponents.FOOD, Foods.CARROT).nutrition());
                }
                return InteractionResult.SUCCESS;
            }

            InteractionResult interactionResult = super.mobInteract(player, hand);
            if ((!interactionResult.consumesAction() || this.isBaby()) && this.isOwnedBy(player)) {
                if (!this.level().isClientSide()) {
                    this.wakeUp();
                    this.setOrderedToSit(!this.isOrderedToSit());
                    this.jumping = false;
                    this.navigation.stop();
                    this.setTarget(null);
                }
                return InteractionResult.SUCCESS;
            }
            return interactionResult;
        }

        if (this.isFood(itemStack) && this.getTarget() == null) {
            if (!this.level().isClientSide()) {
                if (!player.isCreative()) {
                    itemStack.shrink(1);
                }

                if (this.random.nextInt(3) == 0) {
                    this.tame(player);
                    this.navigation.stop();
                    this.setTarget(null);
                    this.setOrderedToSit(true);
                    this.level().broadcastEntityEvent(this, (byte) 7);
                } else {
                    this.level().broadcastEntityEvent(this, (byte) 6);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.has(DataComponents.FOOD) && itemStack.get(DataComponents.FOOD) != null && itemStack.is(ItemTags.MEAT);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("StandingState", this.getEntityData().get(STANDING_STATE));
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("HuntScheduleNight", this.huntScheduleNight);
        tag.putBoolean("HuntsThisNight", this.huntsThisNight);
        tag.putInt("HuntStartTime", this.huntStartTime);
        tag.putInt("HuntEndTime", this.huntEndTime);
        tag.putInt("RottenFleshCooldownTicks", this.rottenFleshCooldownTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setStandingState(StandingState.values()[tag.getInt("StandingState")]);
        this.setSleeping(tag.getBoolean("Sleeping"));
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks") ? tag.getInt("RequiredSleepPreparationTicks") : this.getRandomSleepPreparationTicks();
        this.huntScheduleNight = tag.getInt("HuntScheduleNight");
        this.huntsThisNight = tag.getBoolean("HuntsThisNight");
        this.huntStartTime = tag.getInt("HuntStartTime");
        this.huntEndTime = tag.getInt("HuntEndTime");
        this.rottenFleshCooldownTicks = tag.getInt("RottenFleshCooldownTicks");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STANDING_STATE, 0);
        builder.define(ATTACKING, false);
        builder.define(HOOTING, false);
        builder.define(SLEEPING, false);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double yMotion, boolean onGround, BlockState blockState, BlockPos blockPos) {
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        OwlEntity owlEntity = EntityTypeRegistry.OWL.get().create(serverLevel);
        UUID ownerUuid = this.getOwnerUUID();
        if (ownerUuid != null && owlEntity != null) {
            owlEntity.setOwnerUUID(ownerUuid);
            owlEntity.setTame(true, true);
        }
        return owlEntity;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level);
        flyingPathNavigation.setCanPassDoors(true);
        flyingPathNavigation.setCanFloat(false);
        flyingPathNavigation.setCanOpenDoors(false);
        return flyingPathNavigation;
    }

    @Override
    public float getSwimAmount(float partialTick) {
        return Mth.rotLerp(partialTick, this.lastLeaningPitch, this.leaningPitch);
    }

    public StandingState getStandingState() {
        return StandingState.values()[this.getEntityData().get(STANDING_STATE)];
    }

    public void setStandingState(StandingState standingState) {
        this.getEntityData().set(STANDING_STATE, standingState.ordinal());
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.OWL_DEATH.get();
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        this.wakeUp();
        return SoundRegistry.OWL_HURT.get();
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public boolean canUseActiveBehavior() {
        return !this.isSleeping() && !this.isPassenger();
    }

    public boolean canPrepareForSleep() {
        return this.level().isDay()
                && !this.canHuntNow()
                && !this.isInPanicRightNow()
                && !this.isAttacking()
                && this.getTarget() == null
                && !this.isInWaterOrBubble()
                && !this.isOrderedToSit()
                && !this.isPassenger()
                && !this.isVehicle()
                && !this.isLeashed()
                && !this.isPanickingFromNearbyThreat()
                && this.hasValidSleepLocationForCurrentState();
    }

    public boolean canStartSleeping() {
        return !this.isSleeping()
                && this.sleepCooldownTicks <= 0
                && this.canPrepareForSleep()
                && this.sleepPreparationTicks >= this.requiredSleepPreparationTicks;
    }

    public boolean canContinueSleeping() {
        return this.isSleeping()
                && this.level().isDay()
                && !this.canHuntNow()
                && !this.isInPanicRightNow()
                && !this.isInWaterOrBubble()
                && !this.isPassenger()
                && !this.isOrderedToSit()
                && this.hasValidSleepLocationForCurrentState()
                && !this.hasWakeUpTriggerNearby();
    }

    public boolean canSearchForSleepPerch() {
        return !this.isTame()
                && this.level().isDay()
                && !this.canHuntNow()
                && !this.isSleeping()
                && this.sleepCooldownTicks <= 0
                && !this.isInPanicRightNow()
                && !this.isAttacking()
                && this.getTarget() == null
                && !this.isInWaterOrBubble()
                && !this.isOrderedToSit()
                && !this.isPassenger()
                && !this.isVehicle()
                && !this.isLeashed()
                && !this.hasThreatNearby()
                && !this.isValidSleepPerch();
    }

    public boolean hasValidSleepLocationForCurrentState() {
        return this.isTame() || this.isValidSleepPerch();
    }

    public boolean isValidSleepPerch() {
        BlockPos currentBlockPosition = this.blockPosition();
        BlockState currentBlockState = this.level().getBlockState(currentBlockPosition);
        if (currentBlockState.is(BlockTags.LOGS) || currentBlockState.getBlock() instanceof LeavesBlock) {
            return true;
        }

        BlockPos belowBlockPosition = currentBlockPosition.below();
        BlockState belowBlockState = this.level().getBlockState(belowBlockPosition);
        return belowBlockState.is(BlockTags.LOGS) || belowBlockState.getBlock() instanceof LeavesBlock;
    }

    public boolean isValidSleepPerchSupport(BlockPos blockPos) {
        BlockState blockState = this.level().getBlockState(blockPos);
        return blockState.is(BlockTags.LOGS) || blockState.getBlock() instanceof LeavesBlock;
    }

    public boolean hasReachedPerchTarget(BlockPos blockPos) {
        Vec3 perchCenter = Vec3.atBottomCenterOf(blockPos);
        double deltaX = this.getX() - perchCenter.x;
        double deltaZ = this.getZ() - perchCenter.z;
        double horizontalDistance = deltaX * deltaX + deltaZ * deltaZ;
        double verticalDistance = Math.abs(this.getY() - perchCenter.y);
        return horizontalDistance <= 1.75D && verticalDistance <= 2.5D;
    }

    public void snapToPerch(BlockPos blockPos) {
        Vec3 perchCenter = Vec3.atBottomCenterOf(blockPos);
        this.setPos(perchCenter.x, perchCenter.y, perchCenter.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.navigation.stop();
    }

    @Nullable
    public BlockPos findNearbySleepPerch() {
        BlockPos bestPerchPosition = null;
        double bestDistance = Double.MAX_VALUE;
        Iterable<BlockPos> nearbyPositions = BlockPos.betweenClosed(
                Mth.floor(this.getX() - 8.0D),
                Mth.floor(this.getY() - 4.0D),
                Mth.floor(this.getZ() - 8.0D),
                Mth.floor(this.getX() + 8.0D),
                Mth.floor(this.getY() + 6.0D),
                Mth.floor(this.getZ() + 8.0D)
        );

        for (BlockPos candidateSupportPosition : nearbyPositions) {
            if (!this.isValidSleepPerchSupport(candidateSupportPosition)) {
                continue;
            }

            BlockPos candidatePerchPosition = candidateSupportPosition.above();
            if (!this.level().isEmptyBlock(candidatePerchPosition) || !this.level().isEmptyBlock(candidatePerchPosition.above())) {
                continue;
            }

            double distanceToCandidate = this.distanceToSqr(Vec3.atBottomCenterOf(candidatePerchPosition));
            if (distanceToCandidate < 2.0D) {
                continue;
            }

            if (distanceToCandidate < bestDistance) {
                bestDistance = distanceToCandidate;
                bestPerchPosition = candidatePerchPosition.immutable();
            }
        }

        return bestPerchPosition;
    }

    @Nullable
    public ItemEntity findNearbyRottenFlesh() {
        if (this.rottenFleshCooldownTicks > 0) {
            return null;
        }

        List<ItemEntity> rottenFleshEntities = this.level().getEntitiesOfClass(
                ItemEntity.class,
                this.getBoundingBox().inflate(10.0D, 4.0D, 10.0D),
                itemEntity -> itemEntity.isAlive() && itemEntity.getItem().is(Items.ROTTEN_FLESH)
        );

        if (rottenFleshEntities.isEmpty()) {
            return null;
        }

        rottenFleshEntities.sort(Comparator.comparingDouble(this::distanceToSqr));
        return rottenFleshEntities.get(0);
    }

    public void consumeRottenFlesh(ItemEntity itemEntity) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack rottenFleshStack = itemEntity.getItem().copy();

        serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, rottenFleshStack),
                itemEntity.getX(),
                itemEntity.getY() + 0.1D,
                itemEntity.getZ(),
                8,
                0.1D,
                0.1D,
                0.1D,
                0.02D
        );

        itemEntity.discard();
        this.rottenFleshCooldownTicks = 40;
    }

    public boolean hasThreatNearby() {
        AABB threatCheckBox = this.getBoundingBox().inflate(8.0D, 4.0D, 8.0D);
        return !this.level().getEntitiesOfClass(LivingEntity.class, threatCheckBox, this::isRelevantThreat).isEmpty();
    }

    public boolean hasWakeUpTriggerNearby() {
        AABB wakeUpCheckBox = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        return !this.level().getEntitiesOfClass(LivingEntity.class, wakeUpCheckBox, this::isRelevantWakeUpTrigger).isEmpty();
    }

    public boolean isInPanicRightNow() {
        return this.getLastHurtByMob() != null || this.isOnFire();
    }

    private void setupAnimationStates() {
        StandingState standingState = this.getStandingState();
        boolean idleAllowed = standingState == StandingState.STANDING && !this.isSleeping() && !this.isAttacking() && !this.isHooting();
        this.idleState.animateWhen(idleAllowed, this.tickCount);
        this.flyingState.animateWhen(standingState == StandingState.FLYING, this.tickCount);
        this.attackState.animateWhen(this.isAttacking(), this.tickCount);
        this.hootState.animateWhen(this.isHooting(), this.tickCount);
        this.sleepState.animateWhen(this.isSleeping(), this.tickCount);
    }

    private void spawnSleepingParticle() {
        if (!this.isSleeping()) {
            return;
        }

        if (this.tickCount % 12 != 0) {
            return;
        }

        double particleX = this.getX() + (this.random.nextDouble() - 0.5D) * 0.2D;
        double particleY = this.getY() + this.getBbHeight() * 0.8D;
        double particleZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.2D;
        double particleVelocityX = (this.random.nextDouble() - 0.5D) * 0.01D;
        double particleVelocityY = 0.01D + this.random.nextDouble() * 0.01D;
        double particleVelocityZ = (this.random.nextDouble() - 0.5D) * 0.01D;

        this.level().addParticle(ParticleTypeRegistry.SLEEPING.get(), particleX, particleY, particleZ, particleVelocityX, particleVelocityY, particleVelocityZ);
    }

    private boolean isHooting() {
        return this.entityData.get(HOOTING);
    }

    void setHooting(boolean hooting) {
        this.entityData.set(HOOTING, hooting);
    }

    void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    private boolean isRelevantThreat(LivingEntity entity) {
        if (!entity.isAlive() || entity == this) {
            return false;
        }

        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        if (entity == this.getOwner()) {
            return false;
        }

        if (entity instanceof Player player) {
            return !this.isOwnedBy(player);
        }

        if (entity instanceof OwnableEntity ownableEntity) {
            LivingEntity ownableOwner = ownableEntity.getOwner();
            if (ownableOwner != null && ownableOwner == this.getOwner()) {
                return false;
            }
        }

        return entity == this.getTarget()
                || entity.getLastHurtMob() == this
                || entity.getLastHurtByMob() == this
                || entity.getType().is(TagsRegistry.OWL_TARGETS);
    }

    private boolean isRelevantWakeUpTrigger(LivingEntity entity) {
        if (!entity.isAlive() || entity == this) {
            return false;
        }

        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return false;
        }

        if (entity == this.getOwner()) {
            return true;
        }

        if (entity instanceof Player player) {
            return !this.isOwnedBy(player);
        }

        if (entity == this.getTarget()) {
            return true;
        }

        return entity.getType().is(TagsRegistry.OWL_TARGETS)
                || entity.getLastHurtMob() == this
                || entity.getLastHurtByMob() == this;
    }

    private boolean isPanickingFromNearbyThreat() {
        return this.hasThreatNearby();
    }

    private int getRandomSleepPreparationTicks() {
        return 40;
    }

    private int getRandomSleepCooldownTicks() {
        return 200 + this.random.nextInt(201);
    }

    void resetSleepPreparation() {
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = this.getRandomSleepPreparationTicks();
    }

    public void startSleeping() {
        this.setSleeping(true);
        this.setHooting(false);
        this.setAttacking(false);
        this.setTarget(null);
        this.navigation.stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void wakeUp() {
        if (!this.isSleeping()) {
            this.resetSleepPreparation();
            return;
        }

        this.setSleeping(false);
        this.sleepCooldownTicks = this.getRandomSleepCooldownTicks();
        this.resetSleepPreparation();

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getBbHeight() + 0.2D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D
            );
        }
    }

    public enum StandingState {
        STANDING,
        FLYING
    }
}