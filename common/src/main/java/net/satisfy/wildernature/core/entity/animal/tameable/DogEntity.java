package net.satisfy.wildernature.core.entity.animal.tameable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.satisfy.wildernature.core.entity.ai.goal.animal.DogGoals;
import net.satisfy.wildernature.core.registry.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DogEntity extends TamableAnimal {
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState howlingAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState lyingAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState fetchHowlAnimationState = new AnimationState();
    public final AnimationState digAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> HOWLING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SITTING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LYING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FETCHING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DIGGING = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DISPLAYED_ITEM = SynchedEntityData.defineId(DogEntity.class, EntityDataSerializers.ITEM_STACK);

    private static final int INVENTORY_SIZE = 3;
    private static final double MOVEMENT_SPEED = 0.23D;
    private static final double MAX_HEALTH = 12.0D;
    private static final double ATTACK_DAMAGE = 3.0D;
    private static final float SOUND_VOLUME = 0.3F;
    private static final int BITE_DURATION = 34;
    private static final int LIE_DURATION_MIN = 100;
    private static final int LIE_DURATION_MAX = 220;
    private static final int SLEEP_DURATION_MIN = 180;
    private static final int SLEEP_DURATION_MAX = 360;
    private static final int REST_COOLDOWN_MIN = 160;
    private static final int REST_COOLDOWN_MAX = 320;
    private static final int JOB_COOLDOWN_MIN = 400;
    private static final int JOB_COOLDOWN_MAX = 900;
    private static final double OWNER_REST_RANGE = 8.0D;
    private static final int SKELETON_DELIVERY_WINDOW_TICKS = 200;
    private static final int SKELETON_DELIVERY_COOLDOWN_MIN = 500;
    private static final int SKELETON_DELIVERY_COOLDOWN_MAX = 900;
    private static final int BONE_SEARCH_INTERVAL_MIN = 30;
    private static final int BONE_SEARCH_INTERVAL_MAX = 70;
    private static final int BURROW_SEARCH_INTERVAL_MIN = 60;
    private static final int BURROW_SEARCH_INTERVAL_MAX = 120;
    private static final int CREEPER_SEARCH_INTERVAL_MIN = 10;
    private static final int CREEPER_SEARCH_INTERVAL_MAX = 20;

    private final NonNullList<ItemStack> dogInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int attackAnimationTicks;
    private int restTicks;
    private int restCooldownTicks;
    private int restAttemptTicks;
    private int jobCooldownTicks;
    private int skeletonDeliveryTicks;
    private int skeletonDeliveryCooldownTicks;
    private int boneSearchCooldownTicks;
    private int burrowSearchCooldownTicks;
    private int creeperSearchCooldownTicks;

    @Nullable
    public BlockPos returnSitPos;

    public boolean shouldReturnToSit;

    @Nullable
    private BlockPos burrowPos;

    private int burrowStoredBones;

    private boolean deliverSkeletonLootToOwner;

    @Nullable
    private BlockPos skeletonDeliveryOrigin;

    private boolean skeletonKilledWithOwner;

    public DogEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED).add(Attributes.MAX_HEALTH, MAX_HEALTH).add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE);
    }

    @Nullable
    @Override
    public DogEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        DogEntity babyDog = EntityTypeRegistry.DOG.get().create(serverLevel);
        if (babyDog == null) {
            return null;
        }

        if (ageableMob instanceof DogEntity otherDog) {
            int firstVariant = this.getVariant();
            int secondVariant = otherDog.getVariant();

            if (firstVariant == 0 && secondVariant == 0) {
                babyDog.setVariant(0);
            } else if (firstVariant == 1 && secondVariant == 1) {
                babyDog.setVariant(1);
            } else {
                babyDog.setVariant(2);
            }
        } else {
            babyDog.setVariant(this.getVariant());
        }

        return babyDog;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalizedSpawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        this.setVariant(this.random.nextBoolean() ? 0 : 1);
        return finalizedSpawnGroupData;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(0, new MeleeAttackGoal(this, 1.2D, true));
        this.goalSelector.addGoal(1, new DogGoals.RaccoonGuardGoal(this));
        this.goalSelector.addGoal(2, new DogGoals.ReturnToSitGoal(this));
        this.goalSelector.addGoal(3, new DogGoals.CreeperAlertGoal(this));
        this.goalSelector.addGoal(4, new DogGoals.SeekShelterWhenRainingGoal(this, 1.2D));
        this.goalSelector.addGoal(5, new DogGoals.FetchThrownBoneGoal(this));
        this.goalSelector.addGoal(6, new DogGoals.DeliverSkeletonLootGoal(this));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(7, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(8, new FollowOwnerGoal(this, 1.25D, 18.0F, 7.0F) {
            @Override
            public boolean canUse() {
                return DogEntity.this.canMoveFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return DogEntity.this.canMoveFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new TemptGoal(this, 1.2D, Ingredient.of(Items.BONE), false) {
            @Override
            public boolean canUse() {
                return DogEntity.this.canMoveFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return DogEntity.this.canMoveFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(9, new FollowParentGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return DogEntity.this.canMoveFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return DogEntity.this.canMoveFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new DogGoals.DogCollectBoneGoal(this));
        this.goalSelector.addGoal(11, new DogGoals.DogDigBurrowGoal(this));
        this.goalSelector.addGoal(12, new DogGoals.DogStoreInBurrowGoal(this));
        this.goalSelector.addGoal(13, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return DogEntity.this.canMoveFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return DogEntity.this.canMoveFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(14, new LookAtPlayerGoal(this, Player.class, 3.0F) {
            @Override
            public boolean canUse() {
                return DogEntity.this.canMoveFreely() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return DogEntity.this.canMoveFreely() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(15, new PanicGoal(this, 2.0D) {
            @Override
            public void start() {
                DogEntity.this.clearRestState();
                DogEntity.this.setDigging(false);
                super.start();
            }
        });
        this.goalSelector.addGoal(16, new DogGoals.GoAfterCatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Skeleton.class, true, skeleton -> !this.isOrderedToSit() && !this.isResting() && !this.isDigging()));
    }

    @Override
    public void tick() {
        super.tick();
        this.handleSittingState();

        if (this.attackAnimationTicks > 0) {
            this.attackAnimationTicks--;
        }

        this.setAttacking(this.attackAnimationTicks > 0);

        if (!this.level().isClientSide()) {
            this.updateRestState();

            if (this.jobCooldownTicks > 0) {
                this.jobCooldownTicks--;
            }

            if (this.skeletonDeliveryTicks > 0) {
                this.skeletonDeliveryTicks--;
            }

            if (this.skeletonDeliveryCooldownTicks > 0) {
                this.skeletonDeliveryCooldownTicks--;
            }

            if (this.boneSearchCooldownTicks > 0) {
                this.boneSearchCooldownTicks--;
            }

            if (this.burrowSearchCooldownTicks > 0) {
                this.burrowSearchCooldownTicks--;
            }

            if (this.creeperSearchCooldownTicks > 0) {
                this.creeperSearchCooldownTicks--;
            }

            if (this.skeletonDeliveryTicks <= 0 && this.skeletonDeliveryOrigin != null && this.getMainHandItem().isEmpty()) {
                this.clearSkeletonDelivery();
            }

            if (this.burrowPos != null && !this.hasValidBurrow()) {
                this.burrowPos = this.findNearbyBurrow(8);
            }
        } else {
            this.setupAnimationStates();

            if ((this.isSleeping() || this.isLying()) && this.random.nextInt(this.isSleeping() ? 14 : 22) == 0) {
                this.spawnRestParticles();
            }
        }
    }

    private void spawnRestParticles() {
        double particleX = this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D;
        double particleY = this.getY() + 0.9D;
        double particleZ = this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D;
        double velocityX = 0.0D;
        double velocityY = 0.02D + this.random.nextDouble() * 0.01D;
        double velocityZ = 0.0D;
        this.level().addParticle(ParticleTypeRegistry.SLEEPING.get(), particleX, particleY, particleZ, velocityX, velocityY, velocityZ);
    }

    private void handleSittingState() {
        if (!this.level().isClientSide() && this.isTame() && this.entityData.get(SITTING) != this.isOrderedToSit()) {
            this.setOrderedToSit(this.entityData.get(SITTING));
        }
    }

    private void updateRestState() {
        if (this.restCooldownTicks > 0) {
            this.restCooldownTicks--;
        }

        if (this.shouldCancelRest()) {
            this.clearRestState();
            return;
        }

        if (this.isSleeping() || this.isLying()) {
            if (this.restTicks > 0) {
                this.restTicks--;
            } else {
                this.clearRestState();
            }

            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
            this.xxa = 0.0F;
            this.zza = 0.0F;
            this.restAttemptTicks = 0;
            return;
        }

        if (!this.canStartRest()) {
            this.restAttemptTicks = 0;
            return;
        }

        if (!this.getNavigation().isDone()) {
            this.restAttemptTicks = 0;
            return;
        }

        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.03D) {
            this.restAttemptTicks = 0;
            return;
        }

        this.restAttemptTicks++;

        if (this.restAttemptTicks < 40) {
            return;
        }

        if (this.level().isNight()) {
            if (this.random.nextInt(80) == 0) {
                this.startSleeping();
            }
            return;
        }

        if (this.random.nextInt(120) == 0) {
            this.startLying();
        }
    }

    public boolean canStartRest() {
        if (this.restCooldownTicks > 0) {
            return false;
        }
        if (this.isBaby()) {
            return false;
        }
        if (this.isOrderedToSit()) {
            return false;
        }
        if (this.isAttacking() || this.isHowling() || this.isFetching() || this.isDigging()) {
            return false;
        }
        if (this.isPanicking() || this.getTarget() != null || this.hurtTime > 0) {
            return false;
        }
        if (this.isInWaterOrBubble()) {
            return false;
        }
        if (!this.onGround()) {
            return false;
        }
        if (!this.getNavigation().isDone()) {
            return false;
        }
        if (this.getDeltaMovement().horizontalDistanceSqr() > 0.0025D) {
            return false;
        }
        if (this.isTame()) {
            if (this.getOwner() == null) {
                return false;
            }
            return this.distanceToSqr(this.getOwner()) <= OWNER_REST_RANGE * OWNER_REST_RANGE;
        }
        return true;
    }

    private boolean shouldCancelRest() {
        return this.isOrderedToSit()
                || this.isAttacking()
                || this.isFetching()
                || this.isPanicking()
                || this.getTarget() != null
                || this.hurtTime > 0
                || this.isInWaterOrBubble()
                || !this.onGround()
                || !this.getNavigation().isDone()
                || this.getDeltaMovement().horizontalDistanceSqr() > 0.01D
                || this.isDigging();
    }

    private void startLying() {
        this.setSleeping(false);
        this.setLying(true);
        this.restTicks = LIE_DURATION_MIN + this.random.nextInt(LIE_DURATION_MAX - LIE_DURATION_MIN + 1);
        this.restAttemptTicks = 0;
        this.getNavigation().stop();
    }

    private void startSleeping() {
        this.setLying(false);
        this.setSleeping(true);
        this.restTicks = SLEEP_DURATION_MIN + this.random.nextInt(SLEEP_DURATION_MAX - SLEEP_DURATION_MIN + 1);
        this.restAttemptTicks = 0;
        this.getNavigation().stop();
    }

    public void clearRestState() {
        if (this.isLying() || this.isSleeping()) {
            this.restCooldownTicks = REST_COOLDOWN_MIN + this.random.nextInt(REST_COOLDOWN_MAX - REST_COOLDOWN_MIN + 1);
        }
        this.setLying(false);
        this.setSleeping(false);
        this.restTicks = 0;
        this.restAttemptTicks = 0;
    }

    public boolean isResting() {
        return this.isLying() || this.isSleeping();
    }

    public boolean canMoveFreely() {
        return !this.isResting() && !this.isDigging();
    }

    public boolean canAct() {
        return !this.isOrderedToSit() && !this.isResting() && !this.isAttacking() && !this.isFetching() && !this.isPanicking() && !this.isDigging() && this.getTarget() == null;
    }

    public boolean canRunJob() {
        return this.canAct() && this.jobCooldownTicks <= 0;
    }

    public boolean canGuard() {
        return !this.isDigging() && !this.isAttacking() && !this.isFetching() && !this.isResting();
    }

    public boolean canUseBoneCollectGoal() {
        return this.isTame() && this.canRunJob() && this.hasFreeInventorySlot() && this.skeletonDeliveryTicks <= 0;
    }

    public boolean canUseBurrowGoal() {
        return this.isTame() && this.canRunJob() && this.skeletonDeliveryTicks <= 0;
    }

    public boolean canSearchForBones() {
        return this.boneSearchCooldownTicks <= 0;
    }

    public void resetBoneSearchCooldown() {
        this.boneSearchCooldownTicks = BONE_SEARCH_INTERVAL_MIN + this.random.nextInt(BONE_SEARCH_INTERVAL_MAX - BONE_SEARCH_INTERVAL_MIN + 1);
    }

    public boolean canSearchForBurrow() {
        return this.burrowSearchCooldownTicks <= 0;
    }

    public void resetBurrowSearchCooldown() {
        this.burrowSearchCooldownTicks = BURROW_SEARCH_INTERVAL_MIN + this.random.nextInt(BURROW_SEARCH_INTERVAL_MAX - BURROW_SEARCH_INTERVAL_MIN + 1);
    }

    public boolean canSearchForCreepers() {
        return this.creeperSearchCooldownTicks <= 0;
    }

    public void resetCreeperSearchCooldown() {
        this.creeperSearchCooldownTicks = CREEPER_SEARCH_INTERVAL_MIN + this.random.nextInt(CREEPER_SEARCH_INTERVAL_MAX - CREEPER_SEARCH_INTERVAL_MIN + 1);
    }

    @Nullable
    public BlockPos findNearbyBurrow(int searchRadius) {
        BlockPos originPos = this.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos closestBurrowPos = null;
        double closestDistance = Double.MAX_VALUE;

        for (int offsetX = -searchRadius; offsetX <= searchRadius; offsetX++) {
            for (int offsetY = -2; offsetY <= 2; offsetY++) {
                for (int offsetZ = -searchRadius; offsetZ <= searchRadius; offsetZ++) {
                    mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                    if (!this.level().getBlockState(mutableBlockPos).is(ObjectRegistry.BURROW.get())) {
                        continue;
                    }

                    double checkedDistance = mutableBlockPos.distSqr(originPos);
                    if (checkedDistance < closestDistance) {
                        closestDistance = checkedDistance;
                        closestBurrowPos = mutableBlockPos.immutable();
                    }
                }
            }
        }

        return closestBurrowPos;
    }

    public void triggerOwnerDelivery(BlockPos originPos) {
        this.deliverSkeletonLootToOwner = true;
        this.skeletonDeliveryOrigin = originPos.immutable();
        this.skeletonDeliveryTicks = SKELETON_DELIVERY_WINDOW_TICKS;
    }

    public int getBurrowStoredBones() {
        return this.burrowStoredBones;
    }

    public void setBurrowStoredBones(int burrowStoredBones) {
        this.burrowStoredBones = burrowStoredBones;
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean sitting = this.isOrderedToSit();
        boolean howling = this.isHowling();
        boolean attacking = this.isAttacking();
        boolean lyingOrSleeping = this.isLying() || this.isSleeping();

        if (!moving && !sitting && !howling && !attacking && !lyingOrSleeping && !this.isFetching() && !this.isDigging()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.howlingAnimationState.animateWhen(howling, this.tickCount);
        this.attackAnimationState.animateWhen(attacking, this.tickCount);
        this.sitAnimationState.animateWhen(sitting && !lyingOrSleeping, this.tickCount);
        this.lyingAnimationState.animateWhen(this.isLying() && !this.isSleeping(), this.tickCount);
        this.sleepAnimationState.animateWhen(this.isSleeping(), this.tickCount);
        this.fetchHowlAnimationState.animateWhen(this.isFetching(), this.tickCount);
        this.digAnimationState.animateWhen(this.isDigging(), this.tickCount);
    }

    public int getVariant() {
        return this.entityData.get(VARIANT);
    }

    public void setVariant(int variant) {
        this.entityData.set(VARIANT, variant);
    }

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    public void setAttacking(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
        if (attacking) {
            this.clearRestState();
        }
    }

    public boolean isHowling() {
        return this.entityData.get(HOWLING);
    }

    public void setHowling(boolean howling) {
        this.entityData.set(HOWLING, howling);
    }

    public boolean isLying() {
        return this.entityData.get(LYING);
    }

    public void setLying(boolean lying) {
        this.entityData.set(LYING, lying);
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean isFetching() {
        return this.entityData.get(FETCHING);
    }

    public void setFetching(boolean fetching) {
        this.entityData.set(FETCHING, fetching);
        if (fetching) {
            this.clearRestState();
        }
    }

    public boolean isDigging() {
        return this.entityData.get(DIGGING);
    }

    public void setDigging(boolean digging) {
        this.entityData.set(DIGGING, digging);
        if (digging) {
            this.clearRestState();
        }
    }

    public boolean hasFreeInventorySlot() {
        for (ItemStack itemStack : this.dogInventory) {
            if (itemStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasBoneInInventory() {
        for (ItemStack itemStack : this.dogInventory) {
            if (itemStack.is(Items.BONE) && !itemStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean tryStoreItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.dogInventory.size(); slotIndex++) {
            ItemStack existingStack = this.dogInventory.get(slotIndex);

            if (existingStack.isEmpty()) {
                this.dogInventory.set(slotIndex, itemStack.copyWithCount(1));
                itemStack.shrink(1);
                this.updateDisplayedItem();
                return true;
            }

            if (ItemStack.isSameItemSameComponents(existingStack, itemStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                existingStack.grow(1);
                itemStack.shrink(1);
                this.updateDisplayedItem();
                return true;
            }
        }

        return false;
    }

    public void dropDogInventory() {
        for (int slotIndex = 0; slotIndex < this.dogInventory.size(); slotIndex++) {
            ItemStack itemStack = this.dogInventory.get(slotIndex);
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(itemStack.copy());
                this.dogInventory.set(slotIndex, ItemStack.EMPTY);
            }
        }
        this.updateDisplayedItem();
    }

    @Nullable
    public BlockPos getBurrowPos() {
        return this.burrowPos;
    }

    public void setBurrowPos(@Nullable BlockPos burrowPos) {
        this.burrowPos = burrowPos;
    }

    public boolean hasBurrow() {
        return this.burrowPos != null;
    }

    public void startJob() {
        this.clearRestState();
        this.jobCooldownTicks = JOB_COOLDOWN_MIN + this.random.nextInt(JOB_COOLDOWN_MAX - JOB_COOLDOWN_MIN + 1);
    }

    public void stopJob() {
        this.setFetching(false);
        this.setDigging(false);
    }

    public void markSkeletonKill(boolean killedWithOwner, BlockPos deathPos) {
        if (!this.isTame()) {
            return;
        }

        this.skeletonKilledWithOwner = killedWithOwner;
        this.skeletonDeliveryOrigin = deathPos.immutable();
        this.skeletonDeliveryTicks = SKELETON_DELIVERY_WINDOW_TICKS;
        this.skeletonDeliveryCooldownTicks = SKELETON_DELIVERY_COOLDOWN_MIN + this.random.nextInt(SKELETON_DELIVERY_COOLDOWN_MAX - SKELETON_DELIVERY_COOLDOWN_MIN + 1);

        float ownerChance = killedWithOwner ? 0.9F : 0.35F;
        this.deliverSkeletonLootToOwner = this.random.nextFloat() < ownerChance;
    }

    public boolean canDeliverSkeletonLoot() {
        return this.isTame() && this.skeletonDeliveryTicks > 0 && this.skeletonDeliveryOrigin != null && !this.isOrderedToSit() && !this.isResting() && !this.isFetching() && !this.isAttacking() && !this.isPanicking() && !this.isDigging();
    }

    public boolean shouldDeliverSkeletonLootToOwner() {
        return this.deliverSkeletonLootToOwner;
    }

    @Nullable
    public BlockPos getSkeletonDeliveryOrigin() {
        return this.skeletonDeliveryOrigin;
    }

    public void clearSkeletonDelivery() {
        this.skeletonDeliveryTicks = 0;
        this.deliverSkeletonLootToOwner = false;
        this.skeletonDeliveryOrigin = null;
        this.skeletonKilledWithOwner = false;
    }

    public void spawnDigParticles(BlockPos blockPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.DIRT.defaultBlockState()), blockPos.getX() + 0.5D, blockPos.getY() + 0.9D, blockPos.getZ() + 0.5D, 24, 0.3D, 0.12D, 0.3D, 0.05D);
    }

    public void spawnCarryParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        double lookOffsetX = this.getLookAngle().x * 0.35D;
        double lookOffsetZ = this.getLookAngle().z * 0.35D;
        double particleX = this.getX() + lookOffsetX;
        double particleY = this.getY() + 0.72D;
        double particleZ = this.getZ() + lookOffsetZ;

        serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x6FA8FF), particleX, particleY, particleZ, 2, 0.02D, 0.02D, 0.02D, 1.0D);
    }

    public boolean hasValidBurrow() {
        if (this.burrowPos == null) {
            return false;
        }

        if (!this.level().isLoaded(this.burrowPos)) {
            return false;
        }

        if (!this.level().getBlockState(this.burrowPos).is(ObjectRegistry.BURROW.get())) {
            this.burrowPos = null;
            return false;
        }

        return true;
    }

    public int getInventorySize() {
        return this.dogInventory.size();
    }

    public ItemStack getInventoryItem(int index) {
        return this.dogInventory.get(index);
    }

    public void setInventoryItem(int index, ItemStack stack) {
        this.dogInventory.set(index, stack);
        this.updateDisplayedItem();
    }

    public int getInventoryItemCount() {
        int count = 0;

        for (ItemStack stack : this.dogInventory) {
            if (!stack.isEmpty()) {
                count++;
            }
        }

        return count;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        super.setItemSlot(equipmentSlot, itemStack);
        if (equipmentSlot == EquipmentSlot.MAINHAND) {
            this.updateDisplayedItem();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HOWLING, false);
        builder.define(ATTACKING, false);
        builder.define(SITTING, false);
        builder.define(LYING, false);
        builder.define(SLEEPING, false);
        builder.define(FETCHING, false);
        builder.define(DIGGING, false);
        builder.define(VARIANT, 0);
        builder.define(DISPLAYED_ITEM, ItemStack.EMPTY);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", this.isOrderedToSit());
        compound.putBoolean("Lying", this.isLying());
        compound.putBoolean("Sleeping", this.isSleeping());
        compound.putBoolean("Howling", this.isHowling());
        compound.putBoolean("Fetching", this.isFetching());
        compound.putBoolean("Digging", this.isDigging());
        compound.putInt("AttackAnimationTicks", this.attackAnimationTicks);
        compound.putInt("RestTicks", this.restTicks);
        compound.putInt("RestCooldownTicks", this.restCooldownTicks);
        compound.putInt("RestAttemptTicks", this.restAttemptTicks);
        compound.putInt("JobCooldownTicks", this.jobCooldownTicks);
        compound.putInt("SkeletonDeliveryTicks", this.skeletonDeliveryTicks);
        compound.putInt("SkeletonDeliveryCooldownTicks", this.skeletonDeliveryCooldownTicks);
        compound.putInt("BoneSearchCooldownTicks", this.boneSearchCooldownTicks);
        compound.putInt("BurrowSearchCooldownTicks", this.burrowSearchCooldownTicks);
        compound.putInt("CreeperSearchCooldownTicks", this.creeperSearchCooldownTicks);
        compound.putBoolean("DeliverSkeletonLootToOwner", this.deliverSkeletonLootToOwner);
        compound.putBoolean("SkeletonKilledWithOwner", this.skeletonKilledWithOwner);
        compound.putBoolean("ShouldReturnToSit", this.shouldReturnToSit);
        compound.putInt("Variant", this.getVariant());
        compound.putInt("BurrowStoredBones", this.burrowStoredBones);
        ContainerHelper.saveAllItems(compound, this.dogInventory, this.registryAccess());

        if (this.returnSitPos != null) {
            compound.putInt("ReturnSitPosX", this.returnSitPos.getX());
            compound.putInt("ReturnSitPosY", this.returnSitPos.getY());
            compound.putInt("ReturnSitPosZ", this.returnSitPos.getZ());
        }

        if (this.burrowPos != null) {
            compound.putInt("BurrowPosX", this.burrowPos.getX());
            compound.putInt("BurrowPosY", this.burrowPos.getY());
            compound.putInt("BurrowPosZ", this.burrowPos.getZ());
        }

        if (this.skeletonDeliveryOrigin != null) {
            compound.putInt("SkeletonDeliveryOriginX", this.skeletonDeliveryOrigin.getX());
            compound.putInt("SkeletonDeliveryOriginY", this.skeletonDeliveryOrigin.getY());
            compound.putInt("SkeletonDeliveryOriginZ", this.skeletonDeliveryOrigin.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        boolean sitting = compound.getBoolean("Sitting");
        this.setOrderedToSit(sitting);
        this.entityData.set(SITTING, sitting);
        this.setLying(compound.getBoolean("Lying"));
        this.setSleeping(compound.getBoolean("Sleeping"));
        this.setHowling(compound.getBoolean("Howling"));
        this.setFetching(compound.getBoolean("Fetching"));
        this.setDigging(compound.getBoolean("Digging"));
        this.attackAnimationTicks = compound.getInt("AttackAnimationTicks");
        this.restTicks = compound.getInt("RestTicks");
        this.restCooldownTicks = compound.getInt("RestCooldownTicks");
        this.restAttemptTicks = compound.getInt("RestAttemptTicks");
        this.jobCooldownTicks = compound.getInt("JobCooldownTicks");
        this.skeletonDeliveryTicks = compound.getInt("SkeletonDeliveryTicks");
        this.skeletonDeliveryCooldownTicks = compound.getInt("SkeletonDeliveryCooldownTicks");
        this.boneSearchCooldownTicks = compound.getInt("BoneSearchCooldownTicks");
        this.burrowSearchCooldownTicks = compound.getInt("BurrowSearchCooldownTicks");
        this.creeperSearchCooldownTicks = compound.getInt("CreeperSearchCooldownTicks");
        this.deliverSkeletonLootToOwner = compound.getBoolean("DeliverSkeletonLootToOwner");
        this.skeletonKilledWithOwner = compound.getBoolean("SkeletonKilledWithOwner");
        this.shouldReturnToSit = compound.getBoolean("ShouldReturnToSit");
        this.setVariant(compound.getInt("Variant"));
        this.burrowStoredBones = compound.getInt("BurrowStoredBones");
        ContainerHelper.loadAllItems(compound, this.dogInventory, this.registryAccess());

        if (compound.contains("ReturnSitPosX") && compound.contains("ReturnSitPosY") && compound.contains("ReturnSitPosZ")) {
            this.returnSitPos = new BlockPos(compound.getInt("ReturnSitPosX"), compound.getInt("ReturnSitPosY"), compound.getInt("ReturnSitPosZ"));
        } else {
            this.returnSitPos = null;
        }

        if (compound.contains("BurrowPosX") && compound.contains("BurrowPosY") && compound.contains("BurrowPosZ")) {
            this.burrowPos = new BlockPos(compound.getInt("BurrowPosX"), compound.getInt("BurrowPosY"), compound.getInt("BurrowPosZ"));
        } else {
            this.burrowPos = null;
        }

        if (compound.contains("SkeletonDeliveryOriginX") && compound.contains("SkeletonDeliveryOriginY") && compound.contains("SkeletonDeliveryOriginZ")) {
            this.skeletonDeliveryOrigin = new BlockPos(compound.getInt("SkeletonDeliveryOriginX"), compound.getInt("SkeletonDeliveryOriginY"), compound.getInt("SkeletonDeliveryOriginZ"));
        } else {
            this.skeletonDeliveryOrigin = null;
        }

        this.setAttacking(this.attackAnimationTicks > 0);
        this.updateDisplayedItem();
    }

    @Override
    public void setOrderedToSit(boolean sitting) {
        super.setOrderedToSit(sitting);
        this.entityData.set(SITTING, sitting);
        if (sitting) {
            this.clearRestState();
        }
    }

    @Override
    public boolean isOrderedToSit() {
        return this.entityData.get(SITTING);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float walkSpeed = this.getPose() == Pose.STANDING && !this.isResting() && !this.isOrderedToSit() ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(walkSpeed, 0.2F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.DOG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.DOG_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return SOUND_VOLUME;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        this.clearRestState();
        this.setDigging(false);
        return super.hurt(damageSource, amount);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            this.dropDogInventory();
        }
        super.die(damageSource);
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        boolean success = super.doHurtTarget(entity);

        if (success) {
            this.attackAnimationTicks = BITE_DURATION;
            this.setAttacking(true);

            if (entity instanceof Skeleton skeleton && !skeleton.isAlive()) {
                boolean killedWithOwner = false;
                if (this.getOwner() instanceof Player player) {
                    killedWithOwner = player.distanceToSqr(skeleton) <= 64.0D;
                }
                this.markSkeletonKill(killedWithOwner, skeleton.blockPosition());
            }
        }

        return success;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.DOG_FOOD);
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (this.level().isClientSide()) {
            boolean shouldConsume = this.isOwnedBy(player) || this.isTame() || this.isFood(itemStack) && !this.isTame();
            return shouldConsume ? InteractionResult.CONSUME : InteractionResult.PASS;
        }

        return this.handleServerSideInteraction(player, itemStack, hand);
    }

    private InteractionResult handleServerSideInteraction(Player player, ItemStack itemStack, InteractionHand hand) {
        if (this.isTame()) {
            if (this.isOwnedBy(player)) {
                if (player.isShiftKeyDown()) {
                    if (!itemStack.isEmpty()) {
                        InteractionResult giveItemInteractionResult = this.handleInventoryInsertInteraction(player, itemStack, hand);
                        if (giveItemInteractionResult.consumesAction()) {
                            return giveItemInteractionResult;
                        }
                    }
                    return this.handleInventoryDropInteraction();
                }
                if (this.isFood(itemStack)) {
                    if (this.getAge() == 0 && this.canFallInLove()) {
                        return super.mobInteract(player, hand);
                    }
                    return this.handleHealingWithFood(player, itemStack, hand);
                }
                InteractionResult giveItemInteractionResult = this.handleInventoryInsertInteraction(player, itemStack, hand);
                if (giveItemInteractionResult.consumesAction()) {
                    return giveItemInteractionResult;
                }
                return this.handleNonBoneInteraction(player);
            }
        } else if (this.isFood(itemStack)) {
            return this.handleTamingWithFood(player, itemStack, hand);
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult handleInventoryDropInteraction() {
        if (this.getInventoryItemCount() <= 0) {
            return InteractionResult.CONSUME;
        }

        this.level().playSound(null, this.blockPosition(), SoundEvents.WOLF_GROWL, SoundSource.NEUTRAL, 0.8F, 1.0F);
        this.dropDogInventory();
        this.gameEvent(GameEvent.ENTITY_INTERACT, this);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleHealingWithFood(Player player, ItemStack itemStack, InteractionHand hand) {
        this.clearRestState();
        if (this.getHealth() < this.getMaxHealth()) {
            this.usePlayerItem(player, hand, itemStack);
            this.heal(3.0F);
            this.gameEvent(GameEvent.ENTITY_INTERACT, this);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.CONSUME;
    }

    private InteractionResult handleInventoryInsertInteraction(Player player, ItemStack itemStack, InteractionHand hand) {
        if (itemStack.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!this.hasValidBurrow()) {
            BlockPos nearbyBurrowPos = this.findNearbyBurrow(8);
            if (nearbyBurrowPos != null) {
                this.setBurrowPos(nearbyBurrowPos);
            }
        }

        if (!this.hasValidBurrow()) {
            return InteractionResult.PASS;
        }

        if (!this.hasFreeInventorySlot()) {
            return InteractionResult.CONSUME;
        }

        ItemStack singleItemStack = itemStack.copyWithCount(1);
        if (!this.tryStoreItem(singleItemStack)) {
            return InteractionResult.CONSUME;
        }

        this.usePlayerItem(player, hand, itemStack);
        this.clearRestState();
        this.setTarget(null);
        this.gameEvent(GameEvent.ENTITY_INTERACT, this);
        this.level().playSound(null, this.blockPosition(), SoundEvents.FOX_BITE, SoundSource.NEUTRAL, 0.6F, 1.2F);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleNonBoneInteraction(Player player) {
        InteractionResult interactionResult = super.mobInteract(player, InteractionHand.MAIN_HAND);
        if (!interactionResult.consumesAction() || this.isBaby()) {
            this.clearRestState();
            this.setOrderedToSit(!this.isOrderedToSit());
            if (this.isOrderedToSit()) {
                this.returnSitPos = this.blockPosition().immutable();
                this.shouldReturnToSit = false;
            }
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS;
        }
        return interactionResult;
    }

    private InteractionResult handleTamingWithFood(Player player, ItemStack itemStack, InteractionHand hand) {
        this.usePlayerItem(player, hand, itemStack);
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.clearRestState();
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.returnSitPos = this.blockPosition().immutable();
            this.shouldReturnToSit = false;
            this.level().broadcastEntityEvent(this, (byte) 7);
        } else {
            this.level().broadcastEntityEvent(this, (byte) 6);
        }
        return InteractionResult.SUCCESS;
    }

    public ItemStack getDisplayedItem() {
        return this.entityData.get(DISPLAYED_ITEM);
    }

    private void updateDisplayedItem() {
        ItemStack mainHandItem = this.getMainHandItem();
        if (!mainHandItem.isEmpty()) {
            this.entityData.set(DISPLAYED_ITEM, mainHandItem.copyWithCount(1));
            return;
        }

        for (ItemStack inventoryItem : this.dogInventory) {
            if (!inventoryItem.isEmpty()) {
                this.entityData.set(DISPLAYED_ITEM, inventoryItem.copyWithCount(1));
                return;
            }
        }

        this.entityData.set(DISPLAYED_ITEM, ItemStack.EMPTY);
    }
}