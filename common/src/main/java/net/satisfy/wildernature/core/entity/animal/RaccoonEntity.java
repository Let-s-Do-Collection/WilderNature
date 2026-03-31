package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.BetterWallClimberNavigation;
import net.satisfy.wildernature.core.entity.ai.RaccoonGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RaccoonEntity extends Animal {
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

    public static final AttributeModifier DOOR_DO_NOT_MOVE_MODIFIER = new AttributeModifier(ResourceLocation.parse("wildernature:raccoon_door_do_not_move"), -1000.0D, AttributeModifier.Operation.ADD_VALUE);

    public final AnimationState walkState = new AnimationState();
    public final AnimationState runState = new AnimationState();
    public final AnimationState washingState = new AnimationState();
    public final AnimationState openDoorState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();

    private int ticksSinceEaten;
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
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2D).add(Attributes.MAX_HEALTH, 6.0D).add(Attributes.ATTACK_DAMAGE, 1.5D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        int goalPriority = 0;
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonFloatGoal(this));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonPanicGoal(this, 1.4D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonDoorInteractGoal(this));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonSeekShelterGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonWashSelfGoal(this));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonStoreLootGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonOpenContainerGoal(this, 1.05D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonNibbleCropGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonVillageStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonCuriosityGoal(this));
        this.goalSelector.addGoal(++goalPriority, new RaccoonGoals.RaccoonAvoidEntityGoal<>(this, Player.class));
        this.goalSelector.addGoal(++goalPriority, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new TemptGoal(this, 1.0D, FOOD_ITEMS, false));
        this.goalSelector.addGoal(++goalPriority, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(++goalPriority, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(++goalPriority, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.containerLootCooldownTicks > 0) {
            this.containerLootCooldownTicks--;
        }

        if (this.storeLootCooldownTicks > 0) {
            this.storeLootCooldownTicks--;
        }

        if (!this.level().isClientSide) {
            this.updateSleep();
        }

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            this.walkState.animateWhen(isMoving && !this.isRaccoonRunning() && !this.isSleeping(), this.tickCount);
            this.runState.animateWhen(this.isRaccoonRunning(), this.tickCount);
            this.openDoorState.animateWhen(this.isOpeningDoor(), this.tickCount);
            this.washingState.animateWhen(this.isWashing(), this.tickCount);
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            this.ticksSinceEaten++;
            ItemStack heldStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.isFood(heldStack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack resultStack = heldStack.finishUsingItem(this.level(), this);
                    if (!resultStack.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, resultStack);
                    } else {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    }
                    this.ticksSinceEaten = 0;
                    this.stopWash();
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatingSound(heldStack), 1.0F, 1.0F);
                    this.level().broadcastEntityEvent(this, (byte) 45);
                }
            } else {
                this.ticksSinceEaten = 0;
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }

        super.aiStep();
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        return new BetterWallClimberNavigation(this, level);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("RaccoonFlags", this.entityData.get(DATA_FLAGS_ID));
        compoundTag.putInt("TicksSinceEaten", this.ticksSinceEaten);
        compoundTag.putInt("ContainerLootCooldown", this.containerLootCooldownTicks);
        compoundTag.putInt("StoreLootCooldown", this.storeLootCooldownTicks);
        compoundTag.putBoolean("Sleeping", this.isSleeping());
        compoundTag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        compoundTag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        compoundTag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        ContainerHelper.saveAllItems(compoundTag, this.raccoonInventory, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.entityData.set(DATA_FLAGS_ID, compoundTag.getInt("RaccoonFlags"));
        this.ticksSinceEaten = compoundTag.getInt("TicksSinceEaten");
        this.containerLootCooldownTicks = compoundTag.getInt("ContainerLootCooldown");
        this.storeLootCooldownTicks = compoundTag.getInt("StoreLootCooldown");
        this.setSleeping(compoundTag.getBoolean("Sleeping"));
        this.sleepPreparationTicks = compoundTag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = compoundTag.contains("RequiredSleepPreparationTicks") ? compoundTag.getInt("RequiredSleepPreparationTicks") : SLEEP_PREPARATION_MIN + this.random.nextInt(SLEEP_PREPARATION_MAX - SLEEP_PREPARATION_MIN + 1);
        this.sleepCooldownTicks = compoundTag.getInt("SleepCooldownTicks");
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

    public boolean canLootContainers() {
        return this.level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.containerLootCooldownTicks <= 0 && !this.isSleeping() && !this.isPanicking() && !this.isWashing() && !this.isOpeningDoor() && (this.getMainHandItem().isEmpty() || this.hasFreeInventorySlot());
    }

    public boolean canStoreLoot() {
        return this.storeLootCooldownTicks <= 0 && !this.isSleeping() && !this.isPanicking() && !this.isOpeningDoor() && !this.isContainerLooting() && !this.isWashing() && (this.hasStoredItems() || !this.getMainHandItem().isEmpty());
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
            container.setItem(selectedSlot, ItemStack.EMPTY);
            return false;
        }

        container.setChanged();
        this.startContainerLootCooldown();
        if (this.getMainHandItem().isEmpty() && this.isFood(extractedStack)) {
            this.startWash();
        } else if (!this.getMainHandItem().isEmpty() && this.isFood(this.getMainHandItem())) {
            this.startWash();
        }
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

        return depositedAnyItem;
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
    }

    public void stopOpenDoorAnim() {
        this.setFlag(FLAG_OPEN_DOOR, false);
    }

    public void setSheltering(boolean sheltering) {
        this.setFlag(FLAG_SHELTERING, sheltering);
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
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.002D;

        if (isDay && this.isSheltering() && !this.isPanicking() && !this.isRaccoonRunning() && !this.isOpeningDoor() && !this.isContainerLooting() && !this.isStoringLoot() && !this.isWashing() && isStill && this.sleepCooldownTicks <= 0) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks && !this.isSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 20);
            }
        }

        if (this.isSleeping()) {
            if (this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
            }

            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D, this.getY() + this.getBbHeight() * 0.75D, this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
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