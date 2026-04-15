package net.satisfy.wildernature.core.entity.animal.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.MushroomColonyBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheEatingMob;
import net.satisfy.wildernature.core.entity.ai.goal.EatFromBlockGoal;
import net.satisfy.wildernature.core.entity.ai.behavior.RandomAction;
import net.satisfy.wildernature.core.entity.ai.goal.RandomActionGoal;
import net.satisfy.wildernature.core.entity.animal.neutral.SwiftFoxEntity;
import net.satisfy.wildernature.core.entity.animal.defensive.BisonEntity;
import net.satisfy.wildernature.core.entity.animal.defensive.CassowaryEntity;
import net.satisfy.wildernature.core.registry.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class HedgehogEntity extends Animal implements CacheEatingMob {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState sniffAnimationState = new AnimationState();
    public final AnimationState noAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> SNIFFING = SynchedEntityData.defineId(HedgehogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> CURLED = SynchedEntityData.defineId(HedgehogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(HedgehogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> PLAYING_NO_ANIMATION = SynchedEntityData.defineId(HedgehogEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int CURLED_DURATION_TICKS = 100;
    private static final int NO_ANIMATION_DURATION_TICKS = 44;
    private static final int MILK_REJECTION_COOLDOWN_TICKS = 72000;
    private static final int THREAT_CHECK_COOLDOWN_TICKS = 8;

    private int curledTicks;
    private int noAnimationTicks;
    private int milkRejectionCooldownTicks;
    private int sleepTicks;
    private int threatCheckCooldownTicks;
    private boolean cachedNearbyThreat;

    public HedgehogEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.13D)
                .add(Attributes.MAX_HEALTH, 6.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new PanicGoal(this, 2.0D));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.2D, Ingredient.of(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM), true));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(5, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 5.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new HedgehogConvertMushroomGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new EatFromBlockGoal(this, 1.0D, 8, state -> state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM), (level, pos, state, mob) -> HedgehogEntity.this.consumeWildMushroom(level, pos, state), 1.5F, 1200, SoundEvents.FOX_EAT, () -> HedgehogEntity.this.setSniffing(true), () -> HedgehogEntity.this.setSniffing(false)) {
            @Override
            public boolean canUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new EatFromBlockGoal(this, 1.0D, 8, state -> state.getBlock() == ObjectRegistry.RED_MUSHROOM_COLONY.get() && state.getValue(MushroomColonyBlock.COLONY_AGE) > 0, (level, pos, state, mob) -> level.setBlock(pos, state.setValue(MushroomColonyBlock.COLONY_AGE, state.getValue(MushroomColonyBlock.COLONY_AGE) - 1), 2), 1.5F, 1800, SoundEvents.FOX_EAT, () -> HedgehogEntity.this.setSniffing(true), () -> HedgehogEntity.this.setSniffing(false)) {
            @Override
            public boolean canUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(11, new EatFromBlockGoal(this, 1.0D, 8, state -> state.getBlock() == ObjectRegistry.BROWN_MUSHROOM_COLONY.get() && state.getValue(MushroomColonyBlock.COLONY_AGE) > 0, (level, pos, state, mob) -> level.setBlock(pos, state.setValue(MushroomColonyBlock.COLONY_AGE, state.getValue(MushroomColonyBlock.COLONY_AGE) - 1), 2), 1.5F, 1800, SoundEvents.FOX_EAT, () -> HedgehogEntity.this.setSniffing(true), () -> HedgehogEntity.this.setSniffing(false)) {
            @Override
            public boolean canUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(12, new EatFromBlockGoal(this, 1.0D, 8, state -> state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) > 1, (level, pos, state, mob) -> level.setBlock(pos, state.setValue(SweetBerryBushBlock.AGE, state.getValue(SweetBerryBushBlock.AGE) - 1), 2), 1.0F, 1800, SoundEvents.FOX_EAT, () -> HedgehogEntity.this.setSniffing(true), () -> HedgehogEntity.this.setSniffing(false)) {
            @Override
            public boolean canUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(13, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(14, new RandomActionGoal(new RandomAction() {
            @Override
            public boolean isInterruptable() {
                return false;
            }

            @Override
            public void onStart() {
                HedgehogEntity.this.setSniffing(true);
            }

            @Override
            public void onStop() {
                HedgehogEntity.this.setSniffing(false);
            }

            @Override
            public boolean isPossible() {
                return HedgehogEntity.this.isAvailableForNormalBehavior() && !HedgehogEntity.this.hasNearbyThreat();
            }

            @Override
            public int duration() {
                return 75;
            }

            @Override
            public float chance() {
                if (!HedgehogEntity.this.isAvailableForNormalBehavior()) {
                    return 0.0F;
                }
                return HedgehogEntity.this.level().isNight() ? 0.012F : 0.003F;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeed) {
                return HedgehogEntity.this.getAttribute(Attributes.MOVEMENT_SPEED);
            }
        }));
    }

    private void consumeWildMushroom(Level level, BlockPos pos, BlockState state) {
        if (state.is(Blocks.RED_MUSHROOM)) {
            level.setBlock(pos, ObjectRegistry.RED_MUSHROOM_COLONY.get().defaultBlockState().setValue(MushroomColonyBlock.COLONY_AGE, 0), 2);
            return;
        }

        if (state.is(Blocks.BROWN_MUSHROOM)) {
            level.setBlock(pos, ObjectRegistry.BROWN_MUSHROOM_COLONY.get().defaultBlockState().setValue(MushroomColonyBlock.COLONY_AGE, 0), 2);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
            return;
        }

        if (this.milkRejectionCooldownTicks > 0) {
            this.milkRejectionCooldownTicks--;
        }

        if (this.noAnimationTicks > 0) {
            this.noAnimationTicks--;
            this.setPlayingNoAnimation(true);
        } else if (this.isPlayingNoAnimation()) {
            this.setPlayingNoAnimation(false);
        }

        if (this.threatCheckCooldownTicks > 0) {
            this.threatCheckCooldownTicks--;
        }

        this.updateCurledState();
        this.updateSleepingState();

        if (this.getHealth() < this.getMaxHealth() * 0.45F && this.random.nextFloat() < 0.005F && this.isAvailableForNormalBehavior()) {
            this.heal(1.5F);
            this.triggerNoAnimation();
            this.startSleeping(40 + this.random.nextInt(60));
        }

        if (this.isCurled()) {
            this.stopAllMovement();
            this.setSniffing(false);

            if (this.curledTicks == 1 && !this.isHedgehogSleeping()) {
                this.triggerNoAnimation();
            }
        }

        if (this.isHedgehogSleeping()) {
            this.stopAllMovement();
            this.setSniffing(false);

            if (this.tickCount % 30 == 0) {
                ((ServerLevel) this.level()).sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX(), this.getY() + 0.25D, this.getZ(), 1, 0.1D, 0.05D, 0.1D, 0.0D);
            }
        }

        if (this.isCurled() || this.isHedgehogSleeping()) {
            this.lockStillRotation();
        }

        if (this.tickCount % 6 == 0) {
            this.checkFallDamageFromAbove();
        }
    }

    private void stopAllMovement() {
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        this.xxa = 0.0F;
        this.yya = 0.0F;
        this.zza = 0.0F;
    }

    private void lockStillRotation() {
        this.setYRot(this.yRotO);
        this.setXRot(0.0F);
        this.yHeadRot = this.getYRot();
        this.yBodyRot = this.getYRot();
        this.yHeadRotO = this.yHeadRot;
        this.yBodyRotO = this.yBodyRot;
        this.setTarget(null);
        this.getLookControl().setLookAt(this.getX(), this.getEyeY(), this.getZ());
    }

    private void checkFallDamageFromAbove() {
        List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(0.5D, 1.2D, 0.5D));

        for (Player nearbyPlayer : nearbyPlayers) {
            if (nearbyPlayer.isCreative() || nearbyPlayer.isSpectator()) {
                continue;
            }

            if (nearbyPlayer.fallDistance > 1.0F && nearbyPlayer.getY() > this.getY() + 0.8D) {
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2D, this.getZ(), 5, 0.15D, 0.1D, 0.15D, 0.0D);
                }

                this.kill();
                nearbyPlayer.hurt(this.level().damageSources().generic(), 3.0F);
                return;
            }
        }
    }

    private void updateCurledState() {
        if (this.isHedgehogSleeping() || this.isBaby()) {
            this.setCurled(false);
            this.curledTicks = 0;
            return;
        }

        if (this.curledTicks > 0) {
            this.curledTicks--;
            this.setCurled(true);
            return;
        }

        if (this.shouldCurl()) {
            this.curledTicks = CURLED_DURATION_TICKS;
            this.setCurled(true);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + 0.6D, this.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);
            }
            return;
        }

        this.setCurled(false);
    }

    private void updateSleepingState() {
        if (this.isCurled() || this.isPlayingNoAnimation() || this.isPanicking()) {
            this.wakeUp();
            return;
        }

        if (this.isHedgehogSleeping()) {
            if (this.shouldWakeUp()) {
                this.wakeUp();
            } else if (this.sleepTicks > 0) {
                this.sleepTicks--;
            } else {
                this.wakeUp();
            }
            return;
        }

        if (this.canStartSleeping() && this.random.nextFloat() < 0.006F) {
            this.startSleeping(120 + this.random.nextInt(180));
        }
    }

    private boolean shouldCurl() {
        if (this.isBaby()) {
            return false;
        }
        if (!this.onGround()) {
            return false;
        }
        if (this.isInWaterOrBubble()) {
            return false;
        }
        return this.hasNearbyThreat();
    }

    private boolean hasNearbyThreat() {
        if (this.threatCheckCooldownTicks > 0) {
            return this.cachedNearbyThreat;
        }

        this.threatCheckCooldownTicks = THREAT_CHECK_COOLDOWN_TICKS;
        this.cachedNearbyThreat = false;

        List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(8.0D, 3.5D, 8.0D));

        for (LivingEntity livingEntity : nearbyEntities) {
            if (livingEntity instanceof Player player) {
                if (player.isCreative() || player.isSpectator()) {
                    continue;
                }

                if (this.isThreateningItem(player.getMainHandItem()) || this.isThreateningItem(player.getOffhandItem())) {
                    this.cachedNearbyThreat = true;
                    return true;
                }
            } else if (livingEntity instanceof CassowaryEntity || livingEntity instanceof BoarEntity || livingEntity instanceof SwiftFoxEntity) {
                this.cachedNearbyThreat = true;
                return true;
            } else if (livingEntity instanceof BisonEntity bison) {
                if (this.distanceTo(bison) < 5.0D || bison.getTarget() != null) {
                    this.cachedNearbyThreat = true;
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isThreateningItem(ItemStack itemStack) {
        return itemStack.getItem() instanceof SwordItem
                || itemStack.getItem() instanceof AxeItem
                || itemStack.getItem() instanceof BowItem
                || itemStack.getItem() instanceof CrossbowItem;
    }

    private boolean canStartSleeping() {
        if (!this.level().isDay()) {
            return false;
        }
        if (this.isBaby() || this.isInLove() || this.isPanicking()) {
            return false;
        }
        if (!this.onGround() || this.isInWaterOrBubble()) {
            return false;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
            return false;
        }
        if (this.isSniffing()) {
            return false;
        }
        return !this.hasNearbyThreat();
    }

    private boolean shouldWakeUp() {
        return this.hurtTime > 0
                || this.hasNearbyThreat()
                || !this.level().isDay()
                || this.isInWaterOrBubble()
                || this.isPanicking()
                || this.isSniffing()
                || this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-3D;
    }

    private boolean isAvailableForNormalBehavior() {
        return !this.isCurled()
                && !this.isHedgehogSleeping()
                && !this.isPlayingNoAnimation()
                && !this.isPanicking();
    }

    private void startSleeping(int durationTicks) {
        this.sleepTicks = durationTicks;
        this.setSleepingState(true);
    }

    private void wakeUp() {
        this.sleepTicks = 0;
        this.setSleepingState(false);
    }

    private void triggerNoAnimation() {
        this.noAnimationTicks = NO_ANIMATION_DURATION_TICKS;
        this.setPlayingNoAnimation(true);
        this.wakeUp();
        this.setSniffing(false);
    }

    private void setupAnimationStates() {
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean canIdle = !isMoving && !this.isSniffing() && this.isAvailableForNormalBehavior();

        if (canIdle) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.sniffAnimationState.animateWhen(this.isSniffing() && this.isAvailableForNormalBehavior(), this.tickCount);
        this.noAnimationState.animateWhen(this.isPlayingNoAnimation() && !this.isCurled(), this.tickCount);
        this.sleepAnimationState.animateWhen(this.isHedgehogSleeping() && !this.isCurled(), this.tickCount);
    }

    public boolean isCurled() {
        return this.entityData.get(CURLED);
    }

    public void setCurled(boolean curled) {
        this.entityData.set(CURLED, curled);
    }

    private boolean isSniffing() {
        return this.entityData.get(SNIFFING);
    }

    public void setSniffing(boolean sniffing) {
        this.entityData.set(SNIFFING, sniffing);
    }

    public boolean isHedgehogSleeping() {
        return this.entityData.get(SLEEPING);
    }

    private void setSleepingState(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean isPlayingNoAnimation() {
        return this.entityData.get(PLAYING_NO_ANIMATION);
    }

    private void setPlayingNoAnimation(boolean playingNoAnimation) {
        this.entityData.set(PLAYING_NO_ANIMATION, playingNoAnimation);
    }

    @Override
    public int getHeadRotSpeed() {
        return this.isCurled() || this.isHedgehogSleeping() ? 0 : super.getHeadRotSpeed();
    }

    @Override
    public int getMaxHeadXRot() {
        return this.isCurled() || this.isHedgehogSleeping() ? 0 : super.getMaxHeadXRot();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        this.wakeUp();
        this.setSniffing(false);

        if (this.isCurled()) {
            if (amount <= 8.0F && !damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
                amount *= 0.05F;
            }
        }

        boolean wasHurt = super.hurt(damageSource, amount);

        if (wasHurt && !damageSource.is(DamageTypeTags.IS_PROJECTILE)) {
            Entity directEntity = damageSource.getDirectEntity();
            if (directEntity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(this.level().damageSources().cactus(), 1.0F);
            }

            this.curledTicks = CURLED_DURATION_TICKS;
            this.setCurled(true);
        }

        return wasHurt;
    }

    @Override
    protected void doPush(Entity entity) {
        super.doPush(entity);

        if (this.level().isClientSide()) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity && !(entity instanceof HedgehogEntity) && !livingEntity.isInvulnerable()) {
            livingEntity.hurt(this.level().damageSources().cactus(), 1.0F);
        }
    }

    @Override
    public boolean canUseCacheEatGoal() {
        return !this.isBaby()
                && this.isAvailableForNormalBehavior()
                && this.getHealth() < this.getMaxHealth() * 0.7F;
    }

    @Override
    public boolean canContinueCacheEatGoal() {
        return this.isAvailableForNormalBehavior()
                && this.getHealth() < this.getMaxHealth();
    }

    @Override
    public int getCacheEatSearchRange() {
        return 8;
    }

    @Override
    public int getCacheEatDurationTicks() {
        return 50;
    }

    @Override
    public boolean hasEdibleItemInCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        if (hollowCacheBlockEntity == null) {
            return false;
        }

        return hollowCacheBlockEntity.hasItem(Items.BROWN_MUSHROOM)
                || hollowCacheBlockEntity.hasItem(Items.RED_MUSHROOM)
                || hollowCacheBlockEntity.hasItem(Items.ROTTEN_FLESH)
                || hollowCacheBlockEntity.hasItem(Items.SPIDER_EYE)
                || hollowCacheBlockEntity.hasItem(Items.SWEET_BERRIES);
    }

    @Override
    public ItemStack takeFoodFromCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        if (hollowCacheBlockEntity == null) {
            return ItemStack.EMPTY;
        }

        ItemStack brownMushroomStack = hollowCacheBlockEntity.extractItem(Items.BROWN_MUSHROOM, 1);
        if (!brownMushroomStack.isEmpty()) {
            return brownMushroomStack;
        }

        ItemStack redMushroomStack = hollowCacheBlockEntity.extractItem(Items.RED_MUSHROOM, 1);
        if (!redMushroomStack.isEmpty()) {
            return redMushroomStack;
        }

        ItemStack rottenFleshStack = hollowCacheBlockEntity.extractItem(Items.ROTTEN_FLESH, 1);
        if (!rottenFleshStack.isEmpty()) {
            return rottenFleshStack;
        }

        ItemStack spiderEyeStack = hollowCacheBlockEntity.extractItem(Items.SPIDER_EYE, 1);
        if (!spiderEyeStack.isEmpty()) {
            return spiderEyeStack;
        }

        return hollowCacheBlockEntity.extractItem(Items.SWEET_BERRIES, 1);
    }

    @Override
    public void healFromCacheFood(ItemStack itemStack) {
        if (itemStack.is(Items.RED_MUSHROOM) || itemStack.is(Items.BROWN_MUSHROOM)) {
            this.heal(2.5F);
        } else if (itemStack.is(Items.ROTTEN_FLESH) || itemStack.is(Items.SPIDER_EYE)) {
            this.heal(1.5F);
        } else if (itemStack.is(Items.SWEET_BERRIES)) {
            this.heal(1.0F);
        }
    }

    @Override
    public void spawnCacheEatParticles(ServerLevel serverLevel, ItemStack itemStack) {
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.25D, this.getZ(), 2, 0.15D, 0.1D, 0.15D, 0.0D);
    }

    @Override
    public void onCacheEatGoalStarted() {
        this.wakeUp();
        this.setSniffing(false);
    }

    @Override
    public void onCacheEatStarted(ItemStack itemStack) {
        this.triggerNoAnimation();
    }

    @Override
    public void onCacheEatFinished(ItemStack itemStack) {
        this.triggerNoAnimation();
        this.startSleeping(40 + this.random.nextInt(60));
    }

    @Override
    public void onCacheEatGoalStopped() {
        this.setSniffing(false);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.HEDGEHOG.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SNIFFING, false);
        builder.define(CURLED, false);
        builder.define(SLEEPING, false);
        builder.define(PLAYING_NO_ANIMATION, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("CurledTicks", this.curledTicks);
        compoundTag.putInt("NoAnimationTicks", this.noAnimationTicks);
        compoundTag.putInt("MilkRejectionCooldownTicks", this.milkRejectionCooldownTicks);
        compoundTag.putInt("SleepTicks", this.sleepTicks);
        compoundTag.putInt("ThreatCheckCooldownTicks", this.threatCheckCooldownTicks);
        compoundTag.putBoolean("CachedNearbyThreat", this.cachedNearbyThreat);
        compoundTag.putBoolean("Curled", this.isCurled());
        compoundTag.putBoolean("Sleeping", this.isHedgehogSleeping());
        compoundTag.putBoolean("PlayingNoAnimation", this.isPlayingNoAnimation());
        compoundTag.putBoolean("Sniffing", this.isSniffing());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.curledTicks = compoundTag.getInt("CurledTicks");
        this.noAnimationTicks = compoundTag.getInt("NoAnimationTicks");
        this.milkRejectionCooldownTicks = compoundTag.getInt("MilkRejectionCooldownTicks");
        this.sleepTicks = compoundTag.getInt("SleepTicks");
        this.threatCheckCooldownTicks = compoundTag.getInt("ThreatCheckCooldownTicks");
        this.cachedNearbyThreat = compoundTag.getBoolean("CachedNearbyThreat");
        this.setCurled(compoundTag.getBoolean("Curled"));
        this.setSleepingState(compoundTag.getBoolean("Sleeping"));
        this.setPlayingNoAnimation(compoundTag.getBoolean("PlayingNoAnimation"));
        this.setSniffing(compoundTag.getBoolean("Sniffing"));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.HEDGEHOG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.HEDGEHOG_DEATH.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.HEDGEHOG_AMBIENT.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.HEDGEHOG_FOOD);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.is(Items.MILK_BUCKET)) {
            if (!this.level().isClientSide()) {
                this.triggerNoAnimation();
                this.milkRejectionCooldownTicks = MILK_REJECTION_COOLDOWN_TICKS;
                ((ServerLevel) this.level()).sendParticles(ParticleTypeRegistry.DENY.get(), this.getX(), this.getY() + 0.35D, this.getZ(), 1, 0.1D, 0.1D, 0.1D, 0.0D);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (this.isFood(heldItem) && this.getAge() == 0 && !this.level().isClientSide()) {
            this.usePlayerItem(player, hand, heldItem);
            this.setInLove(player);
            this.wakeUp();
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    public static class HedgehogConvertMushroomGoal extends Goal {
        private static final int SEARCH_RANGE = 8;
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 160;
        private static final int CONVERT_DURATION_MIN = 20;
        private static final int CONVERT_DURATION_MAX = 36;

        private final HedgehogEntity hedgehog;
        private final double speedModifier;
        private BlockPos targetMushroomPos;
        private int searchCooldownTicks;
        private int convertTicks;

        public HedgehogConvertMushroomGoal(HedgehogEntity hedgehog, double speedModifier) {
            this.hedgehog = hedgehog;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.hedgehog.isAvailableForNormalBehavior()) {
                return false;
            }
            if (this.hedgehog.isBaby()) {
                return false;
            }
            if (this.hedgehog.level().isDay()) {
                return false;
            }
            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetMushroomPos = this.findNearestMushroom();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetMushroomPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetMushroomPos != null
                    && this.hedgehog.isAvailableForNormalBehavior()
                    && !this.hedgehog.level().isDay()
                    && this.isValidMushroom(this.targetMushroomPos);
        }

        @Override
        public void start() {
            this.convertTicks = 0;
            this.hedgehog.setSniffing(false);
        }

        @Override
        public void stop() {
            this.hedgehog.getNavigation().stop();
            this.targetMushroomPos = null;
            this.convertTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetMushroomPos == null) {
                return;
            }

            if (!this.isValidMushroom(this.targetMushroomPos)) {
                this.targetMushroomPos = null;
                return;
            }

            if (!this.targetMushroomPos.closerToCenterThan(this.hedgehog.position(), 1.8D)) {
                this.hedgehog.getNavigation().moveTo(this.targetMushroomPos.getX() + 0.5D, this.targetMushroomPos.getY(), this.targetMushroomPos.getZ() + 0.5D, this.speedModifier);
                return;
            }

            this.hedgehog.getNavigation().stop();
            this.hedgehog.getLookControl().setLookAt(this.targetMushroomPos.getX() + 0.5D, this.targetMushroomPos.getY() + 0.25D, this.targetMushroomPos.getZ() + 0.5D);

            if (this.convertTicks <= 0) {
                this.convertTicks = CONVERT_DURATION_MIN + this.hedgehog.getRandom().nextInt(CONVERT_DURATION_MAX - CONVERT_DURATION_MIN + 1);
                this.hedgehog.triggerNoAnimation();
                return;
            }

            this.convertTicks--;
            if (this.convertTicks > 0) {
                return;
            }

            this.convertMushroom(this.targetMushroomPos);
            this.targetMushroomPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
        }

        @Nullable
        private BlockPos findNearestMushroom() {
            BlockPos originPos = this.hedgehog.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestMushroomPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -SEARCH_RANGE; offsetX <= SEARCH_RANGE; offsetX++) {
                for (int offsetY = -2; offsetY <= 2; offsetY++) {
                    for (int offsetZ = -SEARCH_RANGE; offsetZ <= SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.isValidMushroom(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestMushroomPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestMushroomPos;
        }

        private boolean isValidMushroom(BlockPos blockPos) {
            BlockState blockState = this.hedgehog.level().getBlockState(blockPos);
            if (!blockState.is(Blocks.RED_MUSHROOM) && !blockState.is(Blocks.BROWN_MUSHROOM)) {
                return false;
            }

            BlockState belowState = this.hedgehog.level().getBlockState(blockPos.below());
            if (belowState.is(Blocks.DIRT)
                    || belowState.is(Blocks.COARSE_DIRT)
                    || belowState.is(Blocks.GRASS_BLOCK)
                    || belowState.is(Blocks.PODZOL)
                    || belowState.is(Blocks.MYCELIUM)) {
                return true;
            }

            return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("farm_and_charm:fertilized_soil"))
                    .map(belowState::is)
                    .orElse(false);
        }

        private void convertMushroom(BlockPos blockPos) {
            Level level = this.hedgehog.level();
            BlockState oldState = level.getBlockState(blockPos);
            if (!oldState.is(Blocks.RED_MUSHROOM) && !oldState.is(Blocks.BROWN_MUSHROOM)) {
                return;
            }

            BlockState colonyState = oldState.is(Blocks.RED_MUSHROOM)
                    ? ObjectRegistry.RED_MUSHROOM_COLONY.get().defaultBlockState().setValue(MushroomColonyBlock.COLONY_AGE, 1)
                    : ObjectRegistry.BROWN_MUSHROOM_COLONY.get().defaultBlockState().setValue(MushroomColonyBlock.COLONY_AGE, 1);

            level.levelEvent(2001, blockPos, net.minecraft.world.level.block.Block.getId(oldState));
            level.setBlock(blockPos, colonyState, 2);
            level.playSound(null, blockPos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.7F, 0.9F + level.random.nextFloat() * 0.2F);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, blockPos.getX() + 0.5D, blockPos.getY() + 0.3D, blockPos.getZ() + 0.5D, 3, 0.15D, 0.1D, 0.15D, 0.0D);
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.hedgehog.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }
    }
}