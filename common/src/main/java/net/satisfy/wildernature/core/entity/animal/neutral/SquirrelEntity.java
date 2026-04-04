package net.satisfy.wildernature.core.entity.animal.neutral;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.block.HazelnutBushBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheEatingMob;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheStoringMob;
import net.satisfy.wildernature.core.entity.ai.behavior.ShelteringMob;
import net.satisfy.wildernature.core.entity.ai.goal.CacheEatGoal;
import net.satisfy.wildernature.core.entity.ai.goal.CacheStoreGoal;
import net.satisfy.wildernature.core.entity.ai.goal.SeekShelterGoal;
import net.satisfy.wildernature.core.entity.ai.goal.animal.SquirrelGoals;
import net.satisfy.wildernature.core.entity.ai.navigation.BetterWallClimberNavigation;
import net.satisfy.wildernature.core.registry.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SquirrelEntity extends Animal implements CacheStoringMob, ShelteringMob, CacheEatingMob {
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_TRUST_LEVEL = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DELIVERING_GIFT = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_WIGGLING = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SHELTERING = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_FORAGING = SynchedEntityData.defineId(SquirrelEntity.class, EntityDataSerializers.BOOLEAN);

    private static final Ingredient TEMPT_INGREDIENT = Ingredient.of(TagsRegistry.SQUIRREL_HOLDABLE);
    private static final ResourceKey<LootTable> GIFT_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, WilderNature.identifier("gameplay/squirrel_gifts"));

    public static final int VARIANT_UNSET = -1;
    public static final int VARIANT_BROWN = 0;
    public static final int VARIANT_RED = 1;
    public static final int VARIANT_GRAY = 2;

    public static final int MAX_TRUST_LEVEL = 100;
    public static final int PETTING_TRUST_LEVEL = 75;

    public static final int TRUST_GAIN_HAZELNUT = 20;
    public static final int TRUST_GAIN_DEFAULT = 8;
    public static final int TRUST_LOSS_ON_PLAYER_HIT = 80;

    public static final int FEED_COOLDOWN_TICKS = 20;
    public static final int PETTING_COOLDOWN_TICKS = 60;
    public static final int GIFT_COOLDOWN_MIN = 72000;
    public static final int GIFT_COOLDOWN_MAX = 84000;
    public static final int GIFT_FAIL_COOLDOWN_MIN = 200;
    public static final int GIFT_FAIL_COOLDOWN_MAX = 400;
    public static final int GIFT_WIGGLE_DURATION = 24;
    public static final int PETTING_WIGGLE_DURATION = 24;
    public static final int GIFT_RANGE = 12;
    public static final double GIFT_STOP_DISTANCE = 2.0D;
    public static final int GIFT_TRIGGER_CHANCE = 200;

    public static final int SHELTER_LOCAL_WANDER_RADIUS = 2;
    public static final int SHELTER_LOCAL_WANDER_COOLDOWN_MIN = 60;
    public static final int SHELTER_LOCAL_WANDER_COOLDOWN_MAX = 140;

    public static final int INVENTORY_SIZE = 12;
    public static final int CACHE_SEARCH_RANGE = 12;
    public static final int CACHE_STORE_WIGGLE_DURATION = 16;
    public static final int CACHE_STORE_COOLDOWN_MIN = 200;
    public static final int CACHE_STORE_COOLDOWN_MAX = 400;

    public static final int FORAGE_BUSH_SEARCH_RANGE = 10;
    public static final int FORAGE_ITEM_SEARCH_RANGE = 8;
    public static final int FORAGE_WIGGLE_DURATION = 16;
    public static final int FORAGE_COOLDOWN_MIN = 80;
    public static final int FORAGE_COOLDOWN_MAX = 160;

    private static final byte TRUST_POSITIVE_EVENT = 40;
    private static final byte TRUST_NEGATIVE_EVENT = 41;
    private static final byte LOVE_EVENT = 42;
    private static final byte TRUST_DENY_EVENT = 43;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState wiggleAnimationState = new AnimationState();

    private int feedCooldownTicks;
    private int pettingCooldownTicks;
    private int giftCooldownTicks;
    private int giftFailCooldownTicks;
    private int giftWiggleTicks;
    private int wiggleTicks;
    private int cacheStoreCooldownTicks;
    private int forageCooldownTicks;
    private int pendingTrustItemTicks;
    private UUID pendingTrustPlayerUuid;
    private UUID giftTargetPlayerUuid;
    private UUID lastTrustedPlayerUuid;
    private ItemStack pendingGiftStack = ItemStack.EMPTY;
    private NonNullList<ItemStack> squirrelInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public SquirrelEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new SeekShelterGoal<>(this, 1.0D));
        this.goalSelector.addGoal(3, new CacheStoreGoal<>(this, 1.0D));
        this.goalSelector.addGoal(4, new CacheEatGoal<>(this, 1.0D));
        this.goalSelector.addGoal(5, new SquirrelGoals.SquirrelForageGoal(this, 1.05D));
        this.goalSelector.addGoal(6, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new SquirrelGoals.SquirrelGiftTriggerGoal(this));
        this.goalSelector.addGoal(8, new SquirrelGoals.SquirrelDeliverGiftGoal(this));
        this.goalSelector.addGoal(9, new TemptGoal(this, 1.0D, TEMPT_INGREDIENT, false));
        this.goalSelector.addGoal(10, new FollowParentGoal(this, 1.0D));
        this.goalSelector.addGoal(11, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(13, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte) 0);
        builder.define(DATA_TRUST_LEVEL, 0);
        builder.define(DATA_DELIVERING_GIFT, false);
        builder.define(DATA_WIGGLING, false);
        builder.define(DATA_VARIANT, VARIANT_UNSET);
        builder.define(DATA_SHELTERING, false);
        builder.define(DATA_FORAGING, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putInt("TrustLevel", this.getTrustLevel());
        compoundTag.putInt("FeedCooldown", this.feedCooldownTicks);
        compoundTag.putInt("PettingCooldown", this.pettingCooldownTicks);
        compoundTag.putInt("GiftCooldown", this.giftCooldownTicks);
        compoundTag.putInt("GiftFailCooldown", this.giftFailCooldownTicks);
        compoundTag.putInt("GiftWiggleTicks", this.giftWiggleTicks);
        compoundTag.putInt("WiggleTicks", this.wiggleTicks);
        compoundTag.putInt("Variant", this.getVariant());
        compoundTag.putInt("CacheStoreCooldown", this.cacheStoreCooldownTicks);
        compoundTag.putInt("ForageCooldown", this.forageCooldownTicks);

        if (!this.pendingGiftStack.isEmpty()) {
            compoundTag.put("PendingGiftStack", this.pendingGiftStack.saveOptional(this.registryAccess()));
        }

        ContainerHelper.saveAllItems(compoundTag, this.squirrelInventory, this.registryAccess());

        if (this.giftTargetPlayerUuid != null) {
            compoundTag.putUUID("GiftTargetPlayer", this.giftTargetPlayerUuid);
        }

        if (this.lastTrustedPlayerUuid != null) {
            compoundTag.putUUID("LastTrustedPlayer", this.lastTrustedPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setTrustLevel(compoundTag.getInt("TrustLevel"));
        this.feedCooldownTicks = compoundTag.getInt("FeedCooldown");
        this.pettingCooldownTicks = compoundTag.getInt("PettingCooldown");
        this.giftCooldownTicks = compoundTag.getInt("GiftCooldown");
        this.giftFailCooldownTicks = compoundTag.getInt("GiftFailCooldown");
        this.giftWiggleTicks = compoundTag.getInt("GiftWiggleTicks");
        this.wiggleTicks = compoundTag.getInt("WiggleTicks");
        this.cacheStoreCooldownTicks = compoundTag.getInt("CacheStoreCooldown");
        this.forageCooldownTicks = compoundTag.getInt("ForageCooldown");
        this.setWiggling(this.wiggleTicks > 0);
        this.setVariant(compoundTag.contains("Variant") ? compoundTag.getInt("Variant") : VARIANT_UNSET);
        this.pendingGiftStack = compoundTag.contains("PendingGiftStack")
                ? ItemStack.parseOptional(this.registryAccess(), compoundTag.getCompound("PendingGiftStack"))
                : ItemStack.EMPTY;
        this.squirrelInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compoundTag, this.squirrelInventory, this.registryAccess());
        this.giftTargetPlayerUuid = compoundTag.hasUUID("GiftTargetPlayer") ? compoundTag.getUUID("GiftTargetPlayer") : null;
        this.lastTrustedPlayerUuid = compoundTag.hasUUID("LastTrustedPlayer") ? compoundTag.getUUID("LastTrustedPlayer") : null;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalizedSpawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
        this.updateVariantFromBiome();
        return finalizedSpawnGroupData;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageableMob) {
        return EntityTypeRegistry.SQUIRREL.get().create(level);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ObjectRegistry.HAZELNUT.get());
    }

    public int getTrustLevel() {
        return this.entityData.get(DATA_TRUST_LEVEL);
    }

    public boolean canBePetted() {
        return this.getTrustLevel() >= PETTING_TRUST_LEVEL;
    }

    public boolean hasMaximumTrust() {
        return this.getTrustLevel() >= MAX_TRUST_LEVEL;
    }

    public boolean isDeliveringGift() {
        return this.entityData.get(DATA_DELIVERING_GIFT);
    }

    public boolean isWiggling() {
        return this.entityData.get(DATA_WIGGLING);
    }

    public int getVariant() {
        return this.entityData.get(DATA_VARIANT);
    }

    public boolean isSheltering() {
        return this.entityData.get(DATA_SHELTERING);
    }

    public boolean isForaging() {
        return this.entityData.get(DATA_FORAGING);
    }

    public int getGiftCooldownTicks() {
        return this.giftCooldownTicks;
    }

    public int getGiftFailCooldownTicks() {
        return this.giftFailCooldownTicks;
    }

    public int getGiftWiggleTicks() {
        return this.giftWiggleTicks;
    }

    public int getCacheStoreCooldownTicks() {
        return this.cacheStoreCooldownTicks;
    }

    public int getForageCooldownTicks() {
        return this.forageCooldownTicks;
    }

    public ItemStack getPendingGiftStack() {
        return this.pendingGiftStack;
    }

    @Nullable
    public UUID getGiftTargetPlayerUuid() {
        return this.giftTargetPlayerUuid;
    }

    public void setGiftWiggleTicks(int giftWiggleTicks) {
        this.giftWiggleTicks = giftWiggleTicks;
    }

    public void setPendingGiftStack(ItemStack pendingGiftStack) {
        this.pendingGiftStack = pendingGiftStack;
    }

    public void setGiftTargetPlayerUuid(@Nullable UUID giftTargetPlayerUuid) {
        this.giftTargetPlayerUuid = giftTargetPlayerUuid;
    }

    public void setTrustLevel(int trustLevel) {
        this.entityData.set(DATA_TRUST_LEVEL, Math.max(0, Math.min(MAX_TRUST_LEVEL, trustLevel)));
    }

    public void setDeliveringGift(boolean deliveringGift) {
        this.entityData.set(DATA_DELIVERING_GIFT, deliveringGift);
    }

    public void setWiggling(boolean wiggling) {
        this.entityData.set(DATA_WIGGLING, wiggling);
    }

    public void setVariant(int variant) {
        this.entityData.set(DATA_VARIANT, variant);
    }

    public void setSheltering(boolean sheltering) {
        this.entityData.set(DATA_SHELTERING, sheltering);
    }

    @Override
    public boolean canUseShelterGoal() {
        return !this.isSheltering()
                && !this.isDeliveringGift()
                && !this.isWiggling()
                && !this.isBaby()
                && !this.isForaging()
                && this.level().isNight();
    }

    @Override
    public boolean canContinueShelterGoal() {
        return this.level().isNight()
                && !this.isPanicking();
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

    @Override
    public boolean canUseCacheEatGoal() {
        return this.getHealth() < this.getMaxHealth()
                && !this.isPanicking()
                && !this.isWiggling()
                && !this.isDeliveringGift()
                && !this.isForaging()
                && !this.isBaby();
    }

    @Override
    public boolean canContinueCacheEatGoal() {
        return !this.isPanicking()
                && !this.isWiggling()
                && !this.isDeliveringGift()
                && !this.isForaging();
    }

    @Override
    public int getCacheEatSearchRange() {
        return CACHE_SEARCH_RANGE;
    }

    @Override
    public int getCacheEatDurationTicks() {
        return 24;
    }

    @Override
    public boolean hasEdibleItemInCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        for (int slotIndex = 0; slotIndex < hollowCacheBlockEntity.getContainerSize(); slotIndex++) {
            ItemStack itemStack = hollowCacheBlockEntity.getItem(slotIndex);
            if (this.isFood(itemStack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack takeFoodFromCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        for (int slotIndex = 0; slotIndex < hollowCacheBlockEntity.getContainerSize(); slotIndex++) {
            ItemStack itemStack = hollowCacheBlockEntity.getItem(slotIndex);
            if (this.isFood(itemStack)) {
                return hollowCacheBlockEntity.removeItem(slotIndex, 1);
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void healFromCacheFood(ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            this.heal(2.0F);
        }
    }

    @Override
    public void spawnCacheEatParticles(ServerLevel serverLevel, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            for (int particleIndex = 0; particleIndex < 8; particleIndex++) {
                double offsetX = (this.random.nextDouble() - 0.5D) * 0.35D;
                double offsetY = this.random.nextDouble() * 0.25D + 0.45D;
                double offsetZ = (this.random.nextDouble() - 0.5D) * 0.35D;
                double velocityX = (this.random.nextDouble() - 0.5D) * 0.08D;
                double velocityY = this.random.nextDouble() * 0.08D;
                double velocityZ = (this.random.nextDouble() - 0.5D) * 0.08D;
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), this.getX() + offsetX, this.getY() + offsetY, this.getZ() + offsetZ, 1, velocityX, velocityY, velocityZ, 0.0D);
            }
        }
    }

    @Override
    public void onCacheEatGoalStarted() {
    }

    @Override
    public void onCacheEatStarted(ItemStack itemStack) {
        this.startWiggle(this.getCacheEatDurationTicks());
    }

    @Override
    public void onCacheEatFinished(ItemStack itemStack) {
    }

    @Override
    public void onCacheEatGoalStopped() {
    }

    public void setForaging(boolean foraging) {
        this.entityData.set(DATA_FORAGING, foraging);
    }

    private void updateVariantFromBiome() {
        if (this.getVariant() != VARIANT_UNSET) {
            return;
        }

        if (this.level().getBiome(this.blockPosition()).is(BiomeTags.IS_TAIGA)) {
            this.setVariant(VARIANT_GRAY);
            return;
        }

        if (this.level().getBiome(this.blockPosition()).is(BiomeTags.IS_FOREST)) {
            this.setVariant(VARIANT_RED);
            return;
        }

        this.setVariant(VARIANT_BROWN);
    }

    private void addTrust(int trustAmount) {
        if (trustAmount <= 0) {
            return;
        }

        int previousTrustLevel = this.getTrustLevel();
        this.setTrustLevel(previousTrustLevel + trustAmount);

        if (!this.level().isClientSide() && this.getTrustLevel() > previousTrustLevel) {
            this.level().broadcastEntityEvent(this, TRUST_POSITIVE_EVENT);
        }
    }

    private void removeTrust() {
        if (TRUST_LOSS_ON_PLAYER_HIT <= 0) {
            return;
        }

        int previousTrustLevel = this.getTrustLevel();
        this.setTrustLevel(previousTrustLevel - TRUST_LOSS_ON_PLAYER_HIT);

        if (!this.level().isClientSide() && this.getTrustLevel() < previousTrustLevel) {
            this.level().broadcastEntityEvent(this, TRUST_NEGATIVE_EVENT);
        }
    }

    private int getTrustGain(ItemStack itemStack) {
        if (itemStack.is(ObjectRegistry.HAZELNUT.get())) {
            return TRUST_GAIN_HAZELNUT;
        }

        return TRUST_GAIN_DEFAULT;
    }

    private boolean canAcceptTrustItem(ItemStack itemStack) {
        return TEMPT_INGREDIENT.test(itemStack);
    }

    public boolean hasStoredItems() {
        for (ItemStack storedItemStack : this.squirrelInventory) {
            if (!storedItemStack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    public boolean hasFreeInventorySlot() {
        for (ItemStack storedItemStack : this.squirrelInventory) {
            if (storedItemStack.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean tryStoreInSquirrelInventory(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.squirrelInventory.size(); slotIndex++) {
            ItemStack existingStack = this.squirrelInventory.get(slotIndex);

            if (existingStack.isEmpty()) {
                this.squirrelInventory.set(slotIndex, itemStack.copy());
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

    public boolean tryStoreSingleItem(ItemStack sourceStack) {
        if (sourceStack.isEmpty()) {
            return false;
        }

        ItemStack singleStack = sourceStack.copyWithCount(1);
        if (this.tryStoreInSquirrelInventory(singleStack)) {
            sourceStack.shrink(1);
            return true;
        }

        return false;
    }

    public boolean storeForagedItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        ItemStack singleStack = itemStack.copyWithCount(1);
        return this.tryStoreInSquirrelInventory(singleStack);
    }

    public boolean canForageItem(ItemStack itemStack) {
        return !itemStack.isEmpty() && itemStack.is(ObjectRegistry.HAZELNUT.get()) && this.hasFreeInventorySlot();
    }

    public void harvestHazelnutBush(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            return;
        }

        if (!(blockState.getBlock() instanceof HazelnutBushBlock) || !blockState.hasProperty(HazelnutBushBlock.AGE) || blockState.getValue(HazelnutBushBlock.AGE) < 2) {
            return;
        }

        int hazelnutCount = 1 + serverLevel.random.nextInt(2);
        if (blockState.getValue(HazelnutBushBlock.AGE) >= 3) {
            hazelnutCount++;
        }

        for (int hazelnutIndex = 0; hazelnutIndex < hazelnutCount; hazelnutIndex++) {
            if (!this.storeForagedItem(new ItemStack(ObjectRegistry.HAZELNUT.get()))) {
                Block.popResource(serverLevel, blockPos, new ItemStack(ObjectRegistry.HAZELNUT.get()));
            }
        }

        serverLevel.setBlock(blockPos, blockState.setValue(HazelnutBushBlock.AGE, 1), 2);
    }

    public boolean depositInventoryIntoCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        boolean depositedAnyItem = false;

        for (int slotIndex = 0; slotIndex < this.squirrelInventory.size(); slotIndex++) {
            ItemStack storedStack = this.squirrelInventory.get(slotIndex);
            if (storedStack.isEmpty()) {
                continue;
            }

            if (hollowCacheBlockEntity.tryAddItem(storedStack.copy())) {
                this.squirrelInventory.set(slotIndex, ItemStack.EMPTY);
                depositedAnyItem = true;
            }
        }

        return depositedAnyItem;
    }

    public void startGiftCooldown() {
        this.giftCooldownTicks = GIFT_COOLDOWN_MIN + this.random.nextInt(GIFT_COOLDOWN_MAX - GIFT_COOLDOWN_MIN + 1);
    }

    public void startGiftFailCooldown() {
        this.giftFailCooldownTicks = GIFT_FAIL_COOLDOWN_MIN + this.random.nextInt(GIFT_FAIL_COOLDOWN_MAX - GIFT_FAIL_COOLDOWN_MIN + 1);
    }

    public void startCacheStoreCooldown() {
        this.cacheStoreCooldownTicks = CACHE_STORE_COOLDOWN_MIN + this.random.nextInt(CACHE_STORE_COOLDOWN_MAX - CACHE_STORE_COOLDOWN_MIN + 1);
    }

    public void startForageCooldown() {
        this.forageCooldownTicks = FORAGE_COOLDOWN_MIN + this.random.nextInt(FORAGE_COOLDOWN_MAX - FORAGE_COOLDOWN_MIN + 1);
    }

    public void startWiggle(int durationTicks) {
        this.wiggleTicks = durationTicks;
        this.setWiggling(true);
    }

    private void setupAnimationStates() {
        if (this.isWiggling()) {
            this.wiggleAnimationState.startIfStopped(this.tickCount);
            this.idleAnimationState.stop();
            return;
        }

        this.wiggleAnimationState.stop();
        if (this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-5D) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }
    }

    private ItemStack rollGift() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return ItemStack.EMPTY;
        }

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(GIFT_LOOT_TABLE);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .create(LootContextParamSets.GIFT);
        List<ItemStack> generatedItems = lootTable.getRandomItems(lootParams);
        return generatedItems.isEmpty() ? ItemStack.EMPTY : generatedItems.get(0).copy();
    }

    public void dropGift(ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(this.level(), this.getX(), this.getY() + 0.2D, this.getZ(), itemStack);
        double directionX = -Math.sin(this.getYRot() * (Math.PI / 180.0D)) * 0.15D;
        double directionZ = Math.cos(this.getYRot() * (Math.PI / 180.0D)) * 0.15D;
        itemEntity.setDeltaMovement(directionX, 0.2D, directionZ);
        this.level().addFreshEntity(itemEntity);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.isEmpty()) {
            if (this.canBePetted() && !this.isBaby() && this.pettingCooldownTicks <= 0) {
                if (!this.level().isClientSide()) {
                    this.pettingCooldownTicks = PETTING_COOLDOWN_TICKS;
                    this.startWiggle(PETTING_WIGGLE_DURATION);
                    this.level().broadcastEntityEvent(this, LOVE_EVENT);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            return super.mobInteract(player, hand);
        }

        if (this.isFood(itemStack) && this.canFallInLove()) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, itemStack);
                this.setInLove(player);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (this.canAcceptTrustItem(itemStack) && this.feedCooldownTicks <= 0 && !this.isBaby() && this.hasFreeInventorySlot() && this.getMainHandItem().isEmpty()) {
            if (!this.level().isClientSide()) {
                ItemStack singleStack = itemStack.copyWithCount(1);
                this.setItemSlot(EquipmentSlot.MAINHAND, singleStack);
                itemStack.shrink(1);
                this.feedCooldownTicks = FEED_COOLDOWN_TICKS;
                this.pendingTrustItemTicks = 40;
                this.pendingTrustPlayerUuid = player.getUUID();
                this.startWiggle(40);
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == TRUST_POSITIVE_EVENT) {
            this.spawnParticles(ParticleTypeRegistry.TRUST_POSITIVE.get());
            return;
        }

        if (id == TRUST_NEGATIVE_EVENT) {
            this.spawnParticles(ParticleTypeRegistry.TRUST_NEGATIVE.get());
            return;
        }

        if (id == TRUST_DENY_EVENT) {
            this.spawnParticles(ParticleTypeRegistry.DENY.get());
            return;
        }

        if (id == LOVE_EVENT) {
            this.spawnParticles(ParticleTypeRegistry.LOVE.get());
            return;
        }

        super.handleEntityEvent(id);
    }

    private void spawnParticles(ParticleOptions particleOptions) {
        for (int particleIndex = 0; particleIndex < 7; ++particleIndex) {
            double velocityX = this.random.nextGaussian() * 0.02D;
            double velocityY = this.random.nextGaussian() * 0.02D;
            double velocityZ = this.random.nextGaussian() * 0.02D;
            this.level().addParticle(particleOptions, this.getRandomX(1.0D), this.getRandomY() + 0.5D, this.getRandomZ(1.0D), velocityX, velocityY, velocityZ);
        }
    }

    @Override
    protected @NotNull PathNavigation createNavigation(Level level) {
        return new BetterWallClimberNavigation(this, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.feedCooldownTicks > 0) {
            this.feedCooldownTicks--;
        }

        if (this.pettingCooldownTicks > 0) {
            this.pettingCooldownTicks--;
        }

        if (this.giftCooldownTicks > 0) {
            this.giftCooldownTicks--;
        }

        if (this.giftFailCooldownTicks > 0) {
            this.giftFailCooldownTicks--;
        }

        if (this.cacheStoreCooldownTicks > 0) {
            this.cacheStoreCooldownTicks--;
        }

        if (this.forageCooldownTicks > 0) {
            this.forageCooldownTicks--;
        }

        if (this.pendingTrustItemTicks > 0) {
            this.pendingTrustItemTicks--;

            if (this.pendingTrustItemTicks == 0 && !this.level().isClientSide()) {
                ItemStack heldStack = this.getMainHandItem();

                if (!heldStack.isEmpty()) {
                    boolean accepted = this.random.nextFloat() < 0.8F;

                    if (accepted && this.tryStoreInSquirrelInventory(heldStack.copy())) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        Player targetPlayer = this.pendingTrustPlayerUuid != null ? this.level().getPlayerByUUID(this.pendingTrustPlayerUuid) : null;
                        if (targetPlayer != null) {
                            this.lastTrustedPlayerUuid = targetPlayer.getUUID();
                        }
                        this.addTrust(this.getTrustGain(heldStack));
                        this.level().broadcastEntityEvent(this, TRUST_POSITIVE_EVENT);
                    } else {
                        this.dropGift(heldStack.copy());
                        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                        this.setTrustLevel(this.getTrustLevel() - 1);
                        this.level().broadcastEntityEvent(this, TRUST_DENY_EVENT);
                    }
                }

                this.pendingTrustPlayerUuid = null;
            }
        }

        if (this.wiggleTicks > 0) {
            this.wiggleTicks--;
            if (this.wiggleTicks <= 0) {
                this.setWiggling(false);
            }
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        } else {
            this.setClimbing(this.horizontalCollision);
            this.updateVariantFromBiome();

            if (this.isSheltering() && this.level().isNight() && this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4D && this.random.nextInt(20) == 0) {
                ServerLevel serverLevel = (ServerLevel) this.level();
                serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX(), this.getY() + 0.8D, this.getZ(), 1, 0.1D, 0.05D, 0.1D, 0.0D);
            }
        }
    }

    @Override
    public boolean onClimbable() {
        return this.isClimbing();
    }

    public boolean isClimbing() {
        return (this.entityData.get(DATA_FLAGS_ID) & 1) != 0;
    }

    public void setClimbing(boolean climbing) {
        byte flags = this.entityData.get(DATA_FLAGS_ID);
        if (climbing) {
            flags = (byte) (flags | 1);
        } else {
            flags = (byte) (flags & -2);
        }
        this.entityData.set(DATA_FLAGS_ID, flags);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource damageSource) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float damageAmount) {
        boolean wasHurt = super.hurt(damageSource, damageAmount);
        if (wasHurt && !this.level().isClientSide() && damageSource.getEntity() instanceof Player) {
            this.removeTrust();
        }
        return wasHurt;
    }

    @Override
    public boolean canBeLeashed() {
        return this.getTrustLevel() >= PETTING_TRUST_LEVEL;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);

        for (ItemStack itemStack : this.squirrelInventory) {
            if (!itemStack.isEmpty()) {
                this.spawnAtLocation(itemStack.copy());
            }
        }
    }

    @Override
    public boolean hasItemsToStore() {
        return this.hasStoredItems();
    }

    @Override
    public int getStoreCooldownTicks() {
        return this.getCacheStoreCooldownTicks();
    }

    @Override
    public boolean canUseStoreGoal() {
        return !this.isPanicking() && !this.isWiggling() && !this.isDeliveringGift() && !this.isSheltering() && !this.level().isNight() && !this.isForaging();
    }

    @Override
    public boolean canContinueStoreGoal() {
        return !this.isPanicking() && !this.level().isNight();
    }

    @Override
    public int getCacheSearchRange() {
        return Math.min(12, CACHE_SEARCH_RANGE);
    }

    @Override
    public int getCacheStoreWiggleDuration() {
        return CACHE_STORE_WIGGLE_DURATION;
    }

    @Override
    public void onStoreGoalStarted() {
    }

    @Override
    public void onStoreGoalStopped() {
    }

    @Override
    public void onStoreWiggleStarted(int durationTicks) {
        this.startWiggle(durationTicks);
    }

    @Override
    public boolean depositItemsIntoCache(HollowCacheBlockEntity hollowCacheBlockEntity) {
        return this.depositInventoryIntoCache(hollowCacheBlockEntity);
    }

    @Override
    public void startStoreCooldown() {
        this.startCacheStoreCooldown();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.SQUIRREL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.SQUIRREL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.SQUIRREL_DEATH.get();
    }

    public boolean tryTriggerGift() {
        Player targetPlayer = null;

        if (this.lastTrustedPlayerUuid != null) {
            Player preferredPlayer = this.level().getPlayerByUUID(this.lastTrustedPlayerUuid);
            if (preferredPlayer != null && preferredPlayer.isAlive() && !preferredPlayer.isSpectator() && this.distanceToSqr(preferredPlayer) <= GIFT_RANGE * GIFT_RANGE) {
                targetPlayer = preferredPlayer;
            }
        }

        if (targetPlayer == null) {
            List<Player> nearbyPlayers = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(GIFT_RANGE), player -> player.isAlive() && !player.isSpectator());
            if (nearbyPlayers.isEmpty()) {
                return false;
            }

            targetPlayer = nearbyPlayers.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
        }

        ItemStack giftStack = this.rollGift();
        if (giftStack.isEmpty()) {
            return false;
        }

        this.pendingGiftStack = giftStack;
        this.giftTargetPlayerUuid = targetPlayer.getUUID();
        return true;
    }
}