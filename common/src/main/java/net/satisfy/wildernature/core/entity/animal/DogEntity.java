package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.satisfy.wildernature.core.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.core.entity.ai.DogGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
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

    private final NonNullList<ItemStack> dogInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    private int restTicks;
    private int restCooldownTicks;
    private int jobCooldownTicks;
    private int skeletonDeliveryTicks;
    private int skeletonDeliveryCooldownTicks;

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
        this.goalSelector.addGoal(0, new AnimationAttackGoal<>(this, 1.2D, true, BITE_DURATION, 7, this::setAttacking));
        this.goalSelector.addGoal(1, new DogGoals.RaccoonGuardGoal(this));
        this.goalSelector.addGoal(2, new DogGoals.ReturnToSitGoal(this));
        this.goalSelector.addGoal(3, new DogGoals.CreeperAlertGoal(this));
        this.goalSelector.addGoal(4, new DogGoals.SeekShelterWhenRainingGoal(this, 1.2D));
        this.goalSelector.addGoal(5, new DogGoals.DeliverSkeletonLootGoal(this));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(6, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(7, new FollowOwnerGoal(this, 1.25D, 18.0F, 7.0F) {
            @Override
            public boolean canUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(7, new TemptGoal(this, 1.2D, Ingredient.of(Items.BONE), false) {
            @Override
            public boolean canUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new FollowParentGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(9, new DogGoals.DogCollectBoneGoal(this));
        this.goalSelector.addGoal(10, new DogGoals.DogDigBurrowGoal(this));
        this.goalSelector.addGoal(11, new DogGoals.DogStoreInBurrowGoal(this));
        this.goalSelector.addGoal(12, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(13, new LookAtPlayerGoal(this, Player.class, 3.0F) {
            @Override
            public boolean canUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !DogEntity.this.isResting() && !DogEntity.this.isDigging() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(14, new PanicGoal(this, 2.0D) {
            @Override
            public void start() {
                DogEntity.this.clearRestState();
                DogEntity.this.setDigging(false);
                super.start();
            }
        });
        this.goalSelector.addGoal(15, new DogGoals.GoAfterCatGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Skeleton.class, true, skeleton -> !this.isOrderedToSit() && !this.isResting() && !this.isDigging()));
    }

    @Override
    public void tick() {
        super.tick();
        this.handleSittingState();

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

            if (this.skeletonDeliveryTicks <= 0 && this.skeletonDeliveryOrigin != null && this.getMainHandItem().isEmpty()) {
                this.clearSkeletonDelivery();
            }
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
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
            return;
        }

        if (!this.canStartRest()) {
            return;
        }

        if (this.level().isNight()) {
            if (this.random.nextFloat() < 0.004F) {
                this.startSleeping();
            }
            return;
        }

        if (this.random.nextFloat() < 0.0025F) {
            this.startLying();
        }
    }

    private boolean canStartRest() {
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
        if (this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D) {
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
        return this.isOrderedToSit() || this.isAttacking() || this.isFetching() || this.isPanicking() || this.getTarget() != null || this.hurtTime > 0 || this.isInWaterOrBubble() || this.getDeltaMovement().horizontalDistanceSqr() > 0.01D || this.isDigging();
    }

    private void startLying() {
        this.setSleeping(false);
        this.setLying(true);
        this.restTicks = LIE_DURATION_MIN + this.random.nextInt(LIE_DURATION_MAX - LIE_DURATION_MIN + 1);
        this.getNavigation().stop();
    }

    private void startSleeping() {
        this.setLying(false);
        this.setSleeping(true);
        this.restTicks = SLEEP_DURATION_MIN + this.random.nextInt(SLEEP_DURATION_MAX - SLEEP_DURATION_MIN + 1);
        this.getNavigation().stop();
    }

    public void clearRestState() {
        if (this.isLying() || this.isSleeping()) {
            this.restCooldownTicks = REST_COOLDOWN_MIN + this.random.nextInt(REST_COOLDOWN_MAX - REST_COOLDOWN_MIN + 1);
        }
        this.setLying(false);
        this.setSleeping(false);
        this.restTicks = 0;
    }

    public boolean isResting() {
        return this.isLying() || this.isSleeping();
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
                return true;
            }

            if (ItemStack.isSameItemSameComponents(existingStack, itemStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                existingStack.grow(1);
                itemStack.shrink(1);
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

    public void addBurrowStoredBone() {
        this.burrowStoredBones++;
    }

    public boolean canUseBoneCollectGoal() {
        return this.isTame() && this.canStartJob() && this.hasFreeInventorySlot() && this.skeletonDeliveryTicks <= 0;
    }

    public boolean canUseBurrowGoal() {
        return this.isTame() && this.canStartJob() && this.skeletonDeliveryTicks <= 0;
    }

    public boolean canStartJob() {
        return this.jobCooldownTicks <= 0 && !this.isOrderedToSit() && !this.isResting() && !this.isAttacking() && !this.isFetching() && !this.isPanicking() && !this.isDigging() && this.getTarget() == null;
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

        BlockState blockState = this.level().getBlockState(blockPos);
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), blockPos.getX() + 0.5D, blockPos.getY() + 0.8D, blockPos.getZ() + 0.5D, 24, 0.3D, 0.12D, 0.3D, 0.05D);
    }

    public void spawnCarryParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        ItemStack itemStack = this.getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }

        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), this.getX(), this.getY() + 0.6D, this.getZ(), 2, 0.12D, 0.08D, 0.12D, 0.0D);
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
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Sitting", this.isOrderedToSit());
        compound.putBoolean("Lying", this.isLying());
        compound.putBoolean("Sleeping", this.isSleeping());
        compound.putInt("RestTicks", this.restTicks);
        compound.putInt("RestCooldownTicks", this.restCooldownTicks);
        compound.putInt("JobCooldownTicks", this.jobCooldownTicks);
        compound.putInt("SkeletonDeliveryTicks", this.skeletonDeliveryTicks);
        compound.putInt("SkeletonDeliveryCooldownTicks", this.skeletonDeliveryCooldownTicks);
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
        this.restTicks = compound.getInt("RestTicks");
        this.restCooldownTicks = compound.getInt("RestCooldownTicks");
        this.jobCooldownTicks = compound.getInt("JobCooldownTicks");
        this.skeletonDeliveryTicks = compound.getInt("SkeletonDeliveryTicks");
        this.skeletonDeliveryCooldownTicks = compound.getInt("SkeletonDeliveryCooldownTicks");
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
        float walkSpeed = this.getPose() == Pose.STANDING && !this.isResting() && !this.isOrderedToSit() && !this.isDigging() ? Math.min(partialTick * 6.0F, 1.0F) : 0.0F;
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

        if (success && entity instanceof Skeleton skeleton && !skeleton.isAlive()) {
            boolean killedWithOwner = false;
            if (this.getOwner() instanceof Player player) {
                killedWithOwner = player.distanceToSqr(skeleton) <= 64.0D;
            }
            this.markSkeletonKill(killedWithOwner, skeleton.blockPosition());
        }

        return success;
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
            return this.handleTaming(player, itemStack, hand);
        }

        return super.mobInteract(player, hand);
    }

    private InteractionResult handleHealing() {
        this.clearRestState();
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

    private InteractionResult handleTaming(Player player, ItemStack itemStack, InteractionHand hand) {
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
}