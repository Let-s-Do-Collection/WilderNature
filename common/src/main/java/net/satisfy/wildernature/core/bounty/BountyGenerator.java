package net.satisfy.wildernature.core.bounty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;


public final class BountyGenerator {
    private static final int DAILY_BOUNTY_COUNT = 15;

    private BountyGenerator() {
    }

    public static List<BountyDefinition> generateDailyBounties(ServerLevel serverLevel) {
        RandomSource randomSource = serverLevel.getRandom();
        List<BountyDefinition.BountyType> typePool = createTypePool();
        Collections.shuffle(typePool, new java.util.Random(randomSource.nextLong()));

        EnumMap<BountyCategory, List<ResourceLocation>> huntEntityPoolByCategory = new EnumMap<>(BountyCategory.class);
        EnumMap<BountyCategory, List<ResourceLocation>> observeEntityPoolByCategory = new EnumMap<>(BountyCategory.class);

        for (BountyCategory bountyCategory : BountyCategory.values()) {
            huntEntityPoolByCategory.put(bountyCategory, getEntitiesForCategory(serverLevel, bountyCategory, randomSource));
            observeEntityPoolByCategory.put(bountyCategory, getEntitiesForCategory(serverLevel, bountyCategory, randomSource));
        }

        List<ResourceLocation> gatherItemPool = getGatherItems(randomSource);
        List<ResourceLocation> exploreBiomePool = getExploreBiomes(serverLevel, randomSource);

        Set<String> usedTargets = new HashSet<>();
        List<BountyDefinition> generatedBounties = new ArrayList<>();

        for (int index = 0; index < DAILY_BOUNTY_COUNT; index++) {
            BountyDefinition.BountyType selectedType = typePool.get(index);
            BountyCategory selectedCategory = getCategoryForType(selectedType, randomSource);
            ResourceLocation selectedTargetId = getNextTargetId(selectedType, selectedCategory, huntEntityPoolByCategory, observeEntityPoolByCategory, gatherItemPool, exploreBiomePool, usedTargets, randomSource);
            BountyDefinition.BountyTargetType targetType = getTargetType(selectedType);
            int requiredAmount = getRequiredAmount(selectedType, selectedCategory, randomSource);
            int experienceReward = getRandomExperienceReward(selectedType, selectedCategory, randomSource);
            ItemStack previewStack = resolveRewardPreviewStack(serverLevel, selectedCategory);

            generatedBounties.add(new BountyDefinition(
                    UUID.randomUUID(),
                    selectedType,
                    targetType,
                    selectedCategory,
                    selectedTargetId,
                    requiredAmount,
                    new BountyReward(
                            selectedCategory.getLootTableId(),
                            experienceReward,
                            BuiltInRegistries.ITEM.getKey(previewStack.getItem()),
                            previewStack.getCount()
                    )
            ));

            usedTargets.add(selectedType.getSerializedName() + ":" + selectedTargetId);
        }

        return generatedBounties;
    }

    private static List<BountyDefinition.BountyType> createTypePool() {
        List<BountyDefinition.BountyType> typePool = new ArrayList<>();
        typePool.add(BountyDefinition.BountyType.HUNT);
        typePool.add(BountyDefinition.BountyType.HUNT);
        typePool.add(BountyDefinition.BountyType.HUNT);
        typePool.add(BountyDefinition.BountyType.HUNT);
        typePool.add(BountyDefinition.BountyType.GATHER);
        typePool.add(BountyDefinition.BountyType.GATHER);
        typePool.add(BountyDefinition.BountyType.GATHER);
        typePool.add(BountyDefinition.BountyType.GATHER);
        typePool.add(BountyDefinition.BountyType.OBSERVE);
        typePool.add(BountyDefinition.BountyType.OBSERVE);
        typePool.add(BountyDefinition.BountyType.OBSERVE);
        typePool.add(BountyDefinition.BountyType.EXPLORE);
        typePool.add(BountyDefinition.BountyType.EXPLORE);
        typePool.add(BountyDefinition.BountyType.EXPLORE);
        typePool.add(BountyDefinition.BountyType.GATHER);
        return typePool;
    }

    private static BountyCategory getCategoryForType(BountyDefinition.BountyType bountyType, RandomSource randomSource) {
        return switch (bountyType) {
            case HUNT -> {
                int categoryRoll = randomSource.nextInt(100);
                if (categoryRoll < 40) {
                    yield BountyCategory.NEUTRAL;
                }
                if (categoryRoll < 70) {
                    yield BountyCategory.DEFENSIVE;
                }
                if (categoryRoll < 95) {
                    yield BountyCategory.AGGRESSIVE;
                }
                yield BountyCategory.BOSS;
            }
            case GATHER, OBSERVE, EXPLORE -> {
                int categoryRoll = randomSource.nextInt(100);
                if (categoryRoll < 60) {
                    yield BountyCategory.NEUTRAL;
                }
                if (categoryRoll < 85) {
                    yield BountyCategory.DEFENSIVE;
                }
                yield BountyCategory.AGGRESSIVE;
            }
        };
    }

    private static BountyDefinition.BountyTargetType getTargetType(BountyDefinition.BountyType bountyType) {
        return switch (bountyType) {
            case HUNT, OBSERVE -> BountyDefinition.BountyTargetType.ENTITY;
            case GATHER -> BountyDefinition.BountyTargetType.ITEM;
            case EXPLORE -> BountyDefinition.BountyTargetType.BIOME;
        };
    }

    private static ResourceLocation getNextTargetId(BountyDefinition.BountyType bountyType, BountyCategory bountyCategory, EnumMap<BountyCategory, List<ResourceLocation>> huntEntityPoolByCategory, EnumMap<BountyCategory, List<ResourceLocation>> observeEntityPoolByCategory, List<ResourceLocation> gatherItemPool, List<ResourceLocation> exploreBiomePool, Set<String> usedTargets, RandomSource randomSource) {
        List<ResourceLocation> pool = switch (bountyType) {
            case HUNT -> huntEntityPoolByCategory.get(bountyCategory);
            case OBSERVE -> observeEntityPoolByCategory.get(bountyCategory);
            case GATHER -> gatherItemPool;
            case EXPLORE -> exploreBiomePool;
        };

        for (ResourceLocation targetId : pool) {
            String key = bountyType.getSerializedName() + ":" + targetId;
            if (!usedTargets.contains(key)) {
                return targetId;
            }
        }

        if (!pool.isEmpty()) {
            return pool.get(randomSource.nextInt(pool.size()));
        }

        return getFallbackTarget(bountyType, bountyCategory);
    }

    private static ResourceLocation getFallbackTarget(BountyDefinition.BountyType bountyType, BountyCategory bountyCategory) {
        return switch (bountyType) {
            case HUNT, OBSERVE -> getFallbackEntity(bountyCategory);
            case GATHER -> BuiltInRegistries.ITEM.getKey(Items.STONE);
            case EXPLORE -> ResourceLocation.withDefaultNamespace("plains");
        };
    }

    private static List<ResourceLocation> getEntitiesForCategory(ServerLevel serverLevel, BountyCategory bountyCategory, RandomSource randomSource) {
        HolderSet.Named<EntityType<?>> entityTag = serverLevel.registryAccess()
                .lookupOrThrow(Registries.ENTITY_TYPE)
                .get(bountyCategory.getEntityTag())
                .orElse(null);

        List<ResourceLocation> entityIds = new ArrayList<>();

        if (entityTag != null && entityTag.size() > 0) {
            for (Holder<EntityType<?>> entityHolder : entityTag) {
                entityHolder.unwrapKey().ifPresent(entityTypeKey -> entityIds.add(entityTypeKey.location()));
            }
        }

        if (entityIds.isEmpty()) {
            entityIds.add(getFallbackEntity(bountyCategory));
        }

        Collections.shuffle(entityIds, new java.util.Random(randomSource.nextLong()));
        return entityIds;
    }

    private static List<ResourceLocation> getGatherItems(RandomSource randomSource) {
        List<ResourceLocation> itemIds = new ArrayList<>();
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.STONE));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.COBBLESTONE));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.OAK_LOG));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.SPRUCE_LOG));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.BIRCH_LOG));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.COAL));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.LEATHER));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.CARROT));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.POTATO));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.WHEAT));
        itemIds.add(BuiltInRegistries.ITEM.getKey(Items.FEATHER));
        Collections.shuffle(itemIds, new java.util.Random(randomSource.nextLong()));
        return itemIds;
    }

    private static List<ResourceLocation> getExploreBiomes(ServerLevel serverLevel, RandomSource randomSource) {
        List<ResourceLocation> biomeIds = new ArrayList<>();

        addBiomeIfPresent(serverLevel, biomeIds, "plains");
        addBiomeIfPresent(serverLevel, biomeIds, "forest");
        addBiomeIfPresent(serverLevel, biomeIds, "birch_forest");
        addBiomeIfPresent(serverLevel, biomeIds, "taiga");
        addBiomeIfPresent(serverLevel, biomeIds, "snowy_plains");
        addBiomeIfPresent(serverLevel, biomeIds, "jagged_peaks");
        addBiomeIfPresent(serverLevel, biomeIds, "swamp");
        addBiomeIfPresent(serverLevel, biomeIds, "dark_forest");
        addBiomeIfPresent(serverLevel, biomeIds, "savanna");
        addBiomeIfPresent(serverLevel, biomeIds, "desert");

        if (biomeIds.isEmpty()) {
            biomeIds.add(ResourceLocation.withDefaultNamespace("plains"));
        }

        Collections.shuffle(biomeIds, new java.util.Random(randomSource.nextLong()));
        return biomeIds;
    }

    private static void addBiomeIfPresent(ServerLevel serverLevel, List<ResourceLocation> biomeIds, String biomeName) {
        ResourceLocation biomeId = ResourceLocation.withDefaultNamespace(biomeName);
        if (serverLevel.registryAccess().lookupOrThrow(Registries.BIOME).get(ResourceKey.create(Registries.BIOME, biomeId)).isPresent()) {
            biomeIds.add(biomeId);
        }
    }

    private static ResourceLocation getFallbackEntity(BountyCategory bountyCategory) {
        return switch (bountyCategory) {
            case NEUTRAL -> ResourceLocation.withDefaultNamespace("pig");
            case DEFENSIVE -> ResourceLocation.withDefaultNamespace("goat");
            case AGGRESSIVE -> ResourceLocation.withDefaultNamespace("zombie");
            case BOSS -> ResourceLocation.withDefaultNamespace("warden");
        };
    }

    private static int getRequiredAmount(BountyDefinition.BountyType bountyType, BountyCategory bountyCategory, RandomSource randomSource) {
        return switch (bountyType) {
            case HUNT -> {
                int minimumAmount = bountyCategory.getMinimumKills();
                int maximumAmount = bountyCategory.getMaximumKills();
                yield minimumAmount + randomSource.nextInt(maximumAmount - minimumAmount + 1);
            }
            case GATHER -> 16 + randomSource.nextInt(49);
            case OBSERVE -> 1;
            case EXPLORE -> 1;
        };
    }

    private static int getRandomExperienceReward(BountyDefinition.BountyType bountyType, BountyCategory bountyCategory, RandomSource randomSource) {
        return switch (bountyType) {
            case HUNT -> switch (bountyCategory) {
                case NEUTRAL -> 2 + randomSource.nextInt(5);
                case DEFENSIVE -> 5 + randomSource.nextInt(8);
                case AGGRESSIVE -> 10 + randomSource.nextInt(11);
                case BOSS -> 35 + randomSource.nextInt(31);
            };
            case GATHER -> 4 + randomSource.nextInt(6);
            case OBSERVE -> 6 + randomSource.nextInt(6);
            case EXPLORE -> 8 + randomSource.nextInt(8);
        };
    }

    private static ItemStack resolveRewardPreviewStack(ServerLevel serverLevel, BountyCategory bountyCategory) {
        ResourceKey<net.minecraft.world.level.storage.loot.LootTable> lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, bountyCategory.getLootTableId());

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