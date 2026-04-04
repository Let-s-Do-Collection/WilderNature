package net.satisfy.wildernature.core.entity.animal.neutral;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheEatingMob;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheStoringMob;
import net.satisfy.wildernature.core.entity.ai.behavior.ShelteringMob;
import net.satisfy.wildernature.core.entity.ai.navigation.BetterWallClimberNavigation;
import net.satisfy.wildernature.core.entity.ai.goal.CacheEatGoal;
import net.satisfy.wildernature.core.entity.ai.goal.CacheStoreGoal;
import net.satisfy.wildernature.core.entity.ai.goal.animal.RaccoonGoals;
import net.satisfy.wildernature.core.entity.ai.goal.SeekShelterGoal;
import net.satisfy.wildernature.core.entity.animal.tameable.DogEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RaccoonEntity extends Animal implements CacheStoringMob, ShelteringMob, CacheEatingMob {
    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.APPLE, Items.BEETROOT, Items.SWEET_BERRIES, Items.POTATO, Items.COOKED_COD, Items.COOKED_SALMON, Items.CARROT);
    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int FLAG_WASHING = 1 << 1;
    private static final int FLAG_SLEEPING = 1 << 5;
    private static final int FLAG_RUNNING = 1 << 8;
    private static final int FLAG_OPEN_DOOR = 1 << 12;
    private static final int FLAG_SHELTERING = 1 << 13;
    private static final int FLAG_CONTAINER_LOOTING = 1 << 14;
    private static final int FLAG_STORING_LOOT = 1 << 15;
    private int cacheToChestCooldownTicks = 0;

    public static final int SHELTER_LOCAL_WANDER_RADIUS = 2;
    public static final int SHELTER_LOCAL_WANDER_COOLDOWN_MIN = 60;
    public static final int SHELTER_LOCAL_WANDER_COOLDOWN_MAX = 140;
    public static final int CONTAINER_SEARCH_RANGE = 12;
    public static final int VILLAGE_STROLL_RANGE = 16;
    public static final int CONTAINER_LOOT_COOLDOWN_MIN = 200;
    public static final int CONTAINER_LOOT_COOLDOWN_MAX = 400;
    public static final int STORE_LOOT_COOLDOWN_MIN = 120;
    public static final int STORE_LOOT_COOLDOWN_MAX = 240;
    public static final int CACHE_SEARCH_RANGE = 12;
    public static final int CACHE_STORE_WIGGLE_DURATION = 16;
    public static final int SLEEPING_PARTICLE_INTERVAL_TICKS = 18;
    public static final int WAKE_UP_RADIUS = 6;
    public static final int SLEEP_COOLDOWN_MIN = 200;
    public static final int SLEEP_COOLDOWN_MAX = 400;
    public static final int SLEEP_PREPARATION_MIN = 80;
    public static final int SLEEP_PREPARATION_MAX = 180;
    public static final int INVENTORY_SIZE = 6;
    public static final int CROPS_NIBBLED_PER_NIGHT_MIN = 1;
    public static final int CROPS_NIBBLED_PER_NIGHT_MAX = 3;
    public static final int CACHE_TO_CHEST_COOLDOWN_MIN = 8000;
    public static final int CACHE_TO_CHEST_COOLDOWN_MAX = 14000;

    public static final AttributeModifier DOOR_DO_NOT_MOVE_MODIFIER = new AttributeModifier(ResourceLocation.parse("wildernature:raccoon_door_do_not_move"), -1000.0D, AttributeModifier.Operation.ADD_VALUE);

    public final AnimationState walkState = new AnimationState();
    public final AnimationState runState = new AnimationState();
    public final AnimationState washingState = new AnimationState();
    public final AnimationState openDoorState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();

    private int cropsNibbledThisNight;
    private int maxCropsToNibbleThisNight = CROPS_NIBBLED_PER_NIGHT_MIN + this.random.nextInt(CROPS_NIBBLED_PER_NIGHT_MAX - CROPS_NIBBLED_PER_NIGHT_MIN + 1);
    private boolean wasDay = true;
    private int containerLootCooldownTicks;
    private int storeLootCooldownTicks;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;
    private NonNullList<ItemStack> raccoonInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public RaccoonEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().getNodeEvaluator().setCanOpenDoors(true);
        this.getNavigation().getNodeEvaluator().setCanPassDoors(true);
        this.requiredSleepPreparationTicks = SLEEP_PREPARATION_MIN + this.random.nextInt(SLEEP_PREPARATION_MAX - SLEEP_PREPARATION_MIN + 1);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.275D)
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.ATTACK_DAMAGE, 1.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new RaccoonGoals.RaccoonFloatGoal(this));
        this.goalSelector.addGoal(1, new RaccoonGoals.RaccoonPanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new RaccoonGoals.RaccoonAvoidEntityGoal<>(this, Player.class));
        this.goalSelector.addGoal(3, new RaccoonGoals.RaccoonAvoidEntityGoal<>(this, IronGolem.class));
        this.goalSelector.addGoal(4, new RaccoonGoals.RaccoonAvoidEntityGoal<>(this, Villager.class));
        this.goalSelector.addGoal(5, new RaccoonGoals.RaccoonAvoidEntityGoal<>(this, DogEntity.class));
        this.goalSelector.addGoal(6, new SeekShelterGoal<>(this, 1.15D));
        this.goalSelector.addGoal(7, new RaccoonGoals.RaccoonDoorInteractGoal(this));
        this.goalSelector.addGoal(8, new RaccoonGoals.RaccoonWashSelfGoal(this));
        this.goalSelector.addGoal(9, new CacheStoreGoal<>(this, 1.15D));
        this.goalSelector.addGoal(10, new CacheEatGoal<>(this, 1.15D));
        this.goalSelector.addGoal(11, new RaccoonGoals.RaccoonOpenContainerGoal(this, 1.2D));
        this.goalSelector.addGoal(12, new RaccoonGoals.RaccoonNibbleCropGoal(this, 1.15D));
        this.goalSelector.addGoal(13, new RaccoonGoals.RaccoonStealEggGoal(this, 1.15D));
        this.goalSelector.addGoal(14, new RaccoonGoals.RaccoonVillageStrollGoal(this, 1.1D));
        this.goalSelector.addGoal(15, new RaccoonGoals.RaccoonCuriosityGoal(this));
        this.goalSelector.addGoal(16, new BreedGoal(this, 1.1D));
        this.goalSelector.addGoal(17, new TemptGoal(this, 1.1D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(18, new FollowParentGoal(this, 1.2D));
        this.goalSelector.addGoal(19, new WaterAvoidingRandomStrollGoal(this, 1.05D));
        this.goalSelector.addGoal(20, new RandomLookAroundGoal(this));
    }

    public void tick() {
        super.tick();

        if (this.containerLootCooldownTicks > 0) {
            this.containerLootCooldownTicks--;
        }

        if (this.storeLootCooldownTicks > 0) {
            this.storeLootCooldownTicks--;
        }

        if (this.cacheToChestCooldownTicks > 0) {
            this.cacheToChestCooldownTicks--;
        }

        if (!this.level().isClientSide) {
            boolean isCurrentlyDay = this.level().isDay();

            if (this.wasDay && !isCurrentlyDay) {
                this.cropsNibbledThisNight = 0;
                this.maxCropsToNibbleThisNight = CROPS_NIBBLED_PER_NIGHT_MIN + this.random.nextInt(CROPS_NIBBLED_PER_NIGHT_MAX - CROPS_NIBBLED_PER_NIGHT_MIN + 1);
            }

            this.wasDay = isCurrentlyDay;
            this.updateSleep();
        }

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            this.walkState.animateWhen(isMoving && !this.isRaccoonRunning() && !this.isSleeping(), this.tickCount);
            this.runState.animateWhen(this.isRaccoonRunning(), this.tickCount);
            this.openDoorState.animateWhen(this.isOpeningDoor(), this.tickCount);
            this.washingState.animateWhen(this.isWashing(), this.tickCount);
            this.sleepState.animateWhen(this.isSleeping(), this.tickCount);
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            this.stopWash();
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }

        super.aiStep();

        if (this.isSleeping()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        return new BetterWallClimberNavigation(this, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("RaccoonFlags", this.entityData.get(DATA_FLAGS_ID));
        compoundTag.putInt("ContainerLootCooldown", this.containerLootCooldownTicks);
        compoundTag.putInt("StoreLootCooldown", this.storeLootCooldownTicks);
        compoundTag.putInt("CacheToChestCooldown", this.cacheToChestCooldownTicks);
        compoundTag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        compoundTag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        compoundTag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        compoundTag.putInt("CropsNibbledThisNight", this.cropsNibbledThisNight);
        compoundTag.putInt("MaxCropsToNibbleThisNight", this.maxCropsToNibbleThisNight);
        compoundTag.putBoolean("Sleeping", this.isSleeping());
        compoundTag.putBoolean("WasDay", this.wasDay);
        ContainerHelper.saveAllItems(compoundTag, this.raccoonInventory, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_FLAGS_ID, compoundTag.getInt("RaccoonFlags"));
        this.containerLootCooldownTicks = compoundTag.getInt("ContainerLootCooldown");
        this.storeLootCooldownTicks = compoundTag.getInt("StoreLootCooldown");
        this.cacheToChestCooldownTicks = compoundTag.getInt("CacheToChestCooldown");
        this.setSleeping(compoundTag.getBoolean("Sleeping"));
        this.sleepPreparationTicks = compoundTag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = compoundTag.contains("RequiredSleepPreparationTicks")
                ? compoundTag.getInt("RequiredSleepPreparationTicks")
                : SLEEP_PREPARATION_MIN + this.random.nextInt(SLEEP_PREPARATION_MAX - SLEEP_PREPARATION_MIN + 1);
        this.sleepCooldownTicks = compoundTag.getInt("SleepCooldownTicks");
        this.cropsNibbledThisNight = compoundTag.getInt("CropsNibbledThisNight");
        this.maxCropsToNibbleThisNight = compoundTag.contains("MaxCropsToNibbleThisNight")
                ? compoundTag.getInt("MaxCropsToNibbleThisNight")
                : CROPS_NIBBLED_PER_NIGHT_MIN + this.random.nextInt(CROPS_NIBBLED_PER_NIGHT_MAX - CROPS_NIBBLED_PER_NIGHT_MIN + 1);
        this.wasDay = compoundTag.contains("WasDay") ? compoundTag.getBoolean("WasDay") : this.level().isDay();
        this.raccoonInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.raccoonInventory, this.registryAccess());
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }

    @Nullable
    @Override
    public RaccoonEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.RACCOON.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canUseCacheEatGoal() {
        return this.getHealth() < this.getMaxHealth()
                && !this.isSleeping()
                && !this.isPanicking()
                && !this.isOpeningDoor()
                && !this.isContainerLooting()
                && !this.isStoringLoot()
                && !this.isWashing()
                && !this.isBaby();
    }

    @Override
    public boolean canContinueCacheEatGoal() {
        return !this.isSleeping()
                && !this.isPanicking()
                && !this.isOpeningDoor()
                && !this.isContainerLooting()
                && !this.isStoringLoot();
    }

    @Override
    public int getCacheEatSearchRange() {
        return CACHE_SEARCH_RANGE;
    }

    @Override
    public int getCacheEatDurationTicks() {
        return 28;
    }

    @Override
    public boolean hasEdibleItemInCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        for (int slotIndex = 0; slotIndex < hollowCacheBlockEntity.getContainerSize(); slotIndex++) {
            ItemStack itemStack = hollowCacheBlockEntity.getItem(slotIndex);
            if (itemStack.has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack takeFoodFromCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        for (int slotIndex = 0; slotIndex < hollowCacheBlockEntity.getContainerSize(); slotIndex++) {
            ItemStack itemStack = hollowCacheBlockEntity.getItem(slotIndex);
            if (itemStack.has(DataComponents.FOOD)) {
                return hollowCacheBlockEntity.removeItem(slotIndex, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void healFromCacheFood(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            this.heal(3.0F);
        }
    }

    @Override
    public void spawnCacheEatParticles(ServerLevel serverLevel, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            for (int particleIndex = 0; particleIndex < 8; particleIndex++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 0.4D;
                double offsetY = this.random.nextDouble() * 0.3D + 0.55D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 0.4D;
                double velocityX = (this.random.nextDouble() - 0.5D) * 0.08D;
                double velocityY = this.random.nextDouble() * 0.08D;
                double velocityZ = (this.random.nextDouble() - 0.5D) * 0.08D;
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0D);
            }
        }
    }

    @Override
    public void onCacheEatGoalStarted() {
        this.wakeUp();
    }

    @Override
    public void onCacheEatStarted(ItemStack itemStack) {
        this.stopWash();
    }

    @Override
    public void onCacheEatFinished(ItemStack itemStack) {
    }

    @Override
    public void onCacheEatGoalStopped() {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.RACCOON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.RACCOON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.RACCOON_DEATH.get();
    }

    public boolean canNibbleMoreCropsThisNight() {
        return this.cropsNibbledThisNight < this.maxCropsToNibbleThisNight;
    }

    public void markCropNibbled() {
        this.cropsNibbledThisNight++;
    }

    public boolean canLootContainers() {
        return this.level() instanceof ServerLevel serverLevel
                && !this.level().isDay()
                && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && this.containerLootCooldownTicks <= 0
                && this.cacheToChestCooldownTicks <= 0
                && !this.isSleeping()
                && !this.isPanicking()
                && !this.isWashing()
                && !this.isOpeningDoor()
                && !this.isSheltering()
                && (this.getMainHandItem().isEmpty() || this.hasFreeInventorySlot());
    }

    public boolean tryTakeItemFromContainer(Container container) {
        if (!this.getMainHandItem().isEmpty() && !this.hasFreeInventorySlot()) {
            return false;
        }

        NonNullList<Integer> validSlots = NonNullList.create();
        for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
            ItemStack slotStack = container.getItem(slotIndex);
            if (!slotStack.isEmpty() && this.isStealableLoot(slotStack)) {
                validSlots.add(slotIndex);
            }
        }

        if (validSlots.isEmpty()) {
            return false;
        }

        int selectedSlot = validSlots.get(this.random.nextInt(validSlots.size()));
        ItemStack extractedStack = container.removeItem(selectedSlot, 1);
        if (extractedStack.isEmpty()) {
            return false;
        }

        boolean stored;
        if (this.getMainHandItem().isEmpty()) {
            this.setItemSlot(EquipmentSlot.MAINHAND, extractedStack);
            stored = true;
        } else {
            stored = this.tryStoreInInventory(extractedStack.copy());
        }

        if (!stored) {
            container.setItem(selectedSlot, extractedStack);
            return false;
        }

        container.setChanged();
        this.startContainerLootCooldown();
        this.startStoreLootCooldown();

        return true;
    }

    public boolean depositLootIntoCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        boolean depositedAnyItem = false;

        ItemStack heldStack = this.getMainHandItem();
        if (!heldStack.isEmpty() && hollowCacheBlockEntity.tryAddItem(heldStack.copy())) {
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            depositedAnyItem = true;
        }

        for (int slotIndex = 0; slotIndex < this.raccoonInventory.size(); slotIndex++) {
            ItemStack storedStack = this.raccoonInventory.get(slotIndex);
            if (storedStack.isEmpty()) {
                continue;
            }

            if (hollowCacheBlockEntity.tryAddItem(storedStack.copy())) {
                this.raccoonInventory.set(slotIndex, ItemStack.EMPTY);
                depositedAnyItem = true;
            }
        }

        if (depositedAnyItem) {
            this.startCacheToChestCooldown();
        }

        return depositedAnyItem;
    }

    public void startCacheToChestCooldown() {
        this.cacheToChestCooldownTicks = CACHE_TO_CHEST_COOLDOWN_MIN + this.random.nextInt(CACHE_TO_CHEST_COOLDOWN_MAX - CACHE_TO_CHEST_COOLDOWN_MIN + 1);
    }

    public void startContainerLootCooldown() {
        this.containerLootCooldownTicks = CONTAINER_LOOT_COOLDOWN_MIN + this.random.nextInt(CONTAINER_LOOT_COOLDOWN_MAX - CONTAINER_LOOT_COOLDOWN_MIN + 1);
    }

    public void startStoreLootCooldown() {
        this.storeLootCooldownTicks = STORE_LOOT_COOLDOWN_MIN + this.random.nextInt(STORE_LOOT_COOLDOWN_MAX - STORE_LOOT_COOLDOWN_MIN + 1);
    }

    public boolean hasStoredItems() {
        for (ItemStack itemStack : this.raccoonInventory) {
            if (!itemStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFreeInventorySlot() {
        for (ItemStack itemStack : this.raccoonInventory) {
            if (itemStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING) || this.getFlag(FLAG_SLEEPING);
    }

    public boolean isWashing() {
        return this.getFlag(FLAG_WASHING);
    }

    public boolean isRaccoonRunning() {
        return this.getFlag(FLAG_RUNNING);
    }

    public boolean isOpeningDoor() {
        return this.getFlag(FLAG_OPEN_DOOR);
    }

    public boolean isSheltering() {
        return this.getFlag(FLAG_SHELTERING);
    }

    public boolean isContainerLooting() {
        return this.getFlag(FLAG_CONTAINER_LOOTING);
    }

    public boolean isStoringLoot() {
        return this.getFlag(FLAG_STORING_LOOT);
    }

    public void startWash() {
        this.setFlag(FLAG_WASHING, true);
    }

    public void stopWash() {
        this.setFlag(FLAG_WASHING, false);
    }

    public void startRunningAnim() {
        this.setFlag(FLAG_RUNNING, true);
    }

    public void stopRunningAnim() {
        this.setFlag(FLAG_RUNNING, false);
    }

    public void startOpenDoorAnim() {
        this.setFlag(FLAG_OPEN_DOOR, true);
        if (this.level().isClientSide) {
            this.openDoorState.start(this.tickCount);
        }
    }

    public void stopOpenDoorAnim() {
        this.setFlag(FLAG_OPEN_DOOR, false);
        if (this.level().isClientSide) {
            this.openDoorState.stop();
        }
    }

    @Override
    public void setSheltering(boolean sheltering) {
        this.setFlag(FLAG_SHELTERING, sheltering);
    }

    @Override
    public boolean canUseShelterGoal() {
        return !this.isSheltering()
                && !this.isOpeningDoor()
                && !this.isContainerLooting()
                && !this.isStoringLoot()
                && !this.isBaby()
                && !this.isSleeping()
                && this.level().isDay();
    }

    @Override
    public boolean canContinueShelterGoal() {
        return this.level().isDay()
                && !this.isPanicking()
                && !this.isSleeping();
    }

    @Override
    public int getShelterLocalWanderRadius() {
        return SHELTER_LOCAL_WANDER_RADIUS;
    }

    @Override
    public int getShelterLocalWanderCooldownMin() {
        return SHELTER_LOCAL_WANDER_COOLDOWN_MIN;
    }

    @Override
    public int getShelterLocalWanderCooldownMax() {
        return SHELTER_LOCAL_WANDER_COOLDOWN_MAX;
    }

    public void setContainerLooting(boolean containerLooting) {
        this.setFlag(FLAG_CONTAINER_LOOTING, containerLooting);
    }

    public void setStoringLoot(boolean storingLoot) {
        this.setFlag(FLAG_STORING_LOOT, storingLoot);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
        this.setFlag(FLAG_SLEEPING, sleeping);
    }

    public void wakeUp() {
        if (this.isSleeping()) {
            this.setSleeping(false);
            this.sleepCooldownTicks = SLEEP_COOLDOWN_MIN + this.random.nextInt(SLEEP_COOLDOWN_MAX - SLEEP_COOLDOWN_MIN + 1);
        }

        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = SLEEP_PREPARATION_MIN + this.random.nextInt(SLEEP_PREPARATION_MAX - SLEEP_PREPARATION_MIN + 1);
    }

    public void startSleeping() {
        this.setSleeping(true);
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        boolean isDay = this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4D;
        boolean isInShelterArea = this.isInShelterRestArea();

        if (isDay && isInShelterArea && !this.isPanicking() && !this.isRaccoonRunning() && !this.isOpeningDoor() && !this.isContainerLooting() && !this.isStoringLoot() && !this.isWashing() && isStill && this.sleepCooldownTicks <= 0) {
            this.setSheltering(true);
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks >= this.requiredSleepPreparationTicks && !this.isSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 10);
            }

            if (!isDay || !isInShelterArea) {
                this.setSheltering(false);
            }
        }

        if (this.isSleeping()) {
            if (!isInShelterArea || this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
            }

            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D, this.getY() + this.getBbHeight() * 0.75D, this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private boolean isInShelterRestArea() {
        BlockPos standPos = this.blockPosition();
        BlockState belowState = this.level().getBlockState(standPos.below());

        if (belowState.is(ObjectRegistry.HOLLOW_CACHE.get()) || belowState.is(BlockTags.LOGS) || belowState.is(BlockTags.LEAVES)) {
            return true;
        }

        return this.hasTreeCover(standPos);
    }

    private boolean hasTreeCover(BlockPos standPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int verticalOffset = 1; verticalOffset <= 4; verticalOffset++) {
            mutableBlockPos.set(standPos.getX(), standPos.getY() + verticalOffset, standPos.getZ());
            BlockState checkedState = this.level().getBlockState(mutableBlockPos);
            if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                return true;
            }
        }

        for (int horizontalOffsetX = -2; horizontalOffsetX <= 2; horizontalOffsetX++) {
            for (int horizontalOffsetZ = -2; horizontalOffsetZ <= 2; horizontalOffsetZ++) {
                mutableBlockPos.set(standPos.getX() + horizontalOffsetX, standPos.getY() + 1, standPos.getZ() + horizontalOffsetZ);
                BlockState checkedState = this.level().getBlockState(mutableBlockPos);
                if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasWakeUpTriggerNearby() {
        if (!this.level().isDay()) {
            return true;
        }

        Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (player == null) {
            return false;
        }

        if (player.isCreative() || player.isSpectator()) {
            return false;
        }

        return !player.isCrouching();
    }

    private boolean isStealableLoot(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        if (itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS)) {
            return true;
        }

        return itemStack.has(DataComponents.FOOD);
    }

    @Override
    public boolean hasItemsToStore() {
        return this.hasStoredItems() || !this.getMainHandItem().isEmpty();
    }

    @Override
    public int getStoreCooldownTicks() {
        return this.storeLootCooldownTicks;
    }

    @Override
    public boolean canUseStoreGoal() {
        return !this.isSleeping() && !this.isPanicking() && !this.isOpeningDoor() && !this.isContainerLooting() && !this.isWashing();
    }

    @Override
    public boolean canContinueStoreGoal() {
        return !this.isSleeping() && !this.isPanicking() && !this.isOpeningDoor() && !this.isContainerLooting() && !this.isWashing();
    }

    @Override
    public int getCacheSearchRange() {
        return CACHE_SEARCH_RANGE;
    }

    @Override
    public int getCacheStoreWiggleDuration() {
        return CACHE_STORE_WIGGLE_DURATION;
    }

    @Override
    public void onStoreGoalStarted() {
        this.wakeUp();
        this.setStoringLoot(true);
    }

    @Override
    public void onStoreGoalStopped() {
        this.setStoringLoot(false);
    }

    @Override
    public void onStoreWiggleStarted(int durationTicks) {
    }

    @Override
    public boolean depositItemsIntoCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        return this.depositLootIntoCache(hollowCacheBlockEntity);
    }

    @Override
    public void startStoreCooldown() {
        this.startStoreLootCooldown();
    }

    private boolean tryStoreInInventory(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.raccoonInventory.size(); slotIndex++) {
            ItemStack existingStack = this.raccoonInventory.get(slotIndex);

            if (existingStack.isEmpty()) {
                this.raccoonInventory.set(slotIndex, itemStack.copy());
                return true;
            }

            if (ItemStack.isSameItemSameComponents(existingStack, itemStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                int transferableCount = Math.min(itemStack.getCount(), existingStack.getMaxStackSize() - existingStack.getCount());
                if (transferableCount > 0) {
                    existingStack.grow(transferableCount);
                    itemStack.shrink(transferableCount);
                    return itemStack.isEmpty();
                }
            }
        }

        return false;
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) | flag);
        } else {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) & ~flag);
        }
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS_ID) & flag) != 0;
    }
}