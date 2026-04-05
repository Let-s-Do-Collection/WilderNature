package net.satisfy.wildernature.core.bounty;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class BountyGenerator {
    private static final int DAILY_BOUNTY_COUNT = 15;

    private BountyGenerator() {
    }

    public static List<BountyDefinition> generateDailyBounties(ServerLevel serverLevel) {
        RandomSource randomSource = serverLevel.getRandom();
        List<BountyCategory> categoryPool = createCategoryPool();
        Collections.shuffle(categoryPool, new java.util.Random(randomSource.nextLong()));

        EnumMap<BountyCategory, List<ResourceLocation>> entityPoolByCategory = new EnumMap<>(BountyCategory.class);
        for (BountyCategory bountyCategory : BountyCategory.values()) {
            entityPoolByCategory.put(bountyCategory, getEntitiesForCategory(serverLevel, bountyCategory, randomSource));
        }

        Set<ResourceLocation> usedEntities = new HashSet<>();
        List<BountyDefinition> generatedBounties = new ArrayList<>();

        for (int index = 0; index < DAILY_BOUNTY_COUNT; index++) {
            BountyCategory selectedCategory = categoryPool.get(index);
            ResourceLocation selectedEntityId = getNextEntityForCategory(selectedCategory, entityPoolByCategory, usedEntities, randomSource);
            int requiredKills = getRandomKillAmount(selectedCategory, randomSource);
            int experienceReward = getRandomExperienceReward(selectedCategory, randomSource);
            ItemStack previewStack = resolveRewardPreviewStack(serverLevel, selectedCategory);

            generatedBounties.add(new BountyDefinition(
                    UUID.randomUUID(),
                    selectedCategory,
                    selectedEntityId,
                    requiredKills,
                    new BountyReward(
                            selectedCategory.getLootTableId(),
                            experienceReward,
                            BuiltInRegistries.ITEM.getKey(previewStack.getItem()),
                            previewStack.getCount()
                    )
            ));

            usedEntities.add(selectedEntityId);
        }

        return generatedBounties;
    }

    private static List<BountyCategory> createCategoryPool() {
        List<BountyCategory> categoryPool = new ArrayList<>();

        for (int index = 0; index < 6; index++) {
            categoryPool.add(BountyCategory.NEUTRAL);
        }

        for (int index = 0; index < 4; index++) {
            categoryPool.add(BountyCategory.DEFENSIVE);
        }

        for (int index = 0; index < 4; index++) {
            categoryPool.add(BountyCategory.AGGRESSIVE);
        }

        categoryPool.add(BountyCategory.BOSS);
        return categoryPool;
    }

    private static List<ResourceLocation> getEntitiesForCategory(ServerLevel serverLevel, BountyCategory bountyCategory, RandomSource randomSource) {
        Optional<HolderSet.Named<EntityType<?>>> optionalTag = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENTITY_TYPE)
                .get(bountyCategory.getEntityTag());

        List<ResourceLocation> entityIds = new ArrayList<>();

        if (optionalTag.isPresent() && optionalTag.get().size() > 0) {
            for (Holder<EntityType<?>> entityHolder : optionalTag.get()) {
                entityHolder.unwrapKey().ifPresent(entityTypeKey -> entityIds.add(entityTypeKey.location()));
            }
        }

        if (entityIds.isEmpty()) {
            entityIds.add(getFallbackEntity(bountyCategory));
        }

        Collections.shuffle(entityIds, new java.util.Random(randomSource.nextLong()));
        return entityIds;
    }

    private static ResourceLocation getNextEntityForCategory(BountyCategory bountyCategory, EnumMap<BountyCategory, List<ResourceLocation>> entityPoolByCategory, Set<ResourceLocation> usedEntities, RandomSource randomSource) {
        List<ResourceLocation> categoryEntities = entityPoolByCategory.get(bountyCategory);

        for (ResourceLocation entityId : categoryEntities) {
            if (!usedEntities.contains(entityId)) {
                return entityId;
            }
        }

        if (!categoryEntities.isEmpty()) {
            return categoryEntities.get(randomSource.nextInt(categoryEntities.size()));
        }

        return getFallbackEntity(bountyCategory);
    }

    private static ResourceLocation getFallbackEntity(BountyCategory bountyCategory) {
        return switch (bountyCategory) {
            case NEUTRAL -> ResourceLocation.withDefaultNamespace("pig");
            case DEFENSIVE -> ResourceLocation.withDefaultNamespace("goat");
            case AGGRESSIVE -> ResourceLocation.withDefaultNamespace("zombie");
            case BOSS -> ResourceLocation.withDefaultNamespace("warden");
        };
    }

    private static int getRandomKillAmount(BountyCategory bountyCategory, RandomSource randomSource) {
        int minimumKills = bountyCategory.getMinimumKills();
        int maximumKills = bountyCategory.getMaximumKills();
        return minimumKills + randomSource.nextInt(maximumKills - minimumKills + 1);
    }

    private static int getRandomExperienceReward(BountyCategory bountyCategory, RandomSource randomSource) {
        return switch (bountyCategory) {
            case NEUTRAL -> 2 + randomSource.nextInt(5);
            case DEFENSIVE -> 5 + randomSource.nextInt(8);
            case AGGRESSIVE -> 10 + randomSource.nextInt(11);
            case BOSS -> 35 + randomSource.nextInt(31);
        };
    }

    private static ItemStack resolveRewardPreviewStack(ServerLevel serverLevel, BountyCategory bountyCategory) {
        ResourceKey<LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, bountyCategory.getLootTableId());

        ItemEntity previewEntity = new ItemEntity(
                serverLevel,
                serverLevel.getSharedSpawnPos().getX(),
                serverLevel.getSharedSpawnPos().getY(),
                serverLevel.getSharedSpawnPos().getZ(),
                ItemStack.EMPTY
        );

        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.THIS_ENTITY, previewEntity)
                .withParameter(LootContextParams.ORIGIN, serverLevel.getSharedSpawnPos().getCenter())
                .create(LootContextParamSets.GIFT);

        List<ItemStack> rewardItems = serverLevel.getServer().reloadableRegistries().getLootTable(lootTableKey).getRandomItems(lootParams);
        if (!rewardItems.isEmpty() && !rewardItems.getFirst().isEmpty()) {
            return rewardItems.getFirst().copy();
        }

        return new ItemStack(Items.PAPER);
    }
}