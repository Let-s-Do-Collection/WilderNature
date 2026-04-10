package net.satisfy.wildernature.core.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.satisfy.wildernature.WilderNature;

public class TagsRegistry {
    public static final TagKey<Item> BEAVER_PAYMENT = TagKey.create(Registries.ITEM, WilderNature.identifier("beaver_payment"));
    public static final TagKey<Item> DOG_FOOD = TagKey.create(Registries.ITEM, WilderNature.identifier("dog_food"));
    public static final TagKey<Item> SQUIRREL_HOLDABLE = TagKey.create(Registries.ITEM, WilderNature.identifier("squirrel_holdable"));
    public static final TagKey<Item> LOOT_BAG_BLACKLIST = TagKey.create(Registries.ITEM, WilderNature.identifier("loot_bag_blacklist"));
    public static final TagKey<Block> MAKES_BLOCK_GLOW = TagKey.create(Registries.BLOCK, WilderNature.identifier("makes_block_glow"));
    public static final TagKey<Biome> SPAWNS_DEER = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_deer"));
    public static final TagKey<Biome> SPAWNS_BOAR = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_boar"));
    public static final TagKey<Biome> SPAWNS_OWL = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_owl"));
    public static final TagKey<Biome> SPAWNS_BISON = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_bison"));
    public static final TagKey<Biome> SPAWNS_TURKEY = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_turkey"));
    public static final TagKey<Biome> SPAWNS_RACCOON = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_raccoon"));
    public static final TagKey<Biome> SPAWNS_SWIFT_FOX = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_swift_fox"));
    public static final TagKey<Biome> SPAWNS_SQUIRREL = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_squirrel"));
    public static final TagKey<Biome> SPAWNS_DOG = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_dog"));
    public static final TagKey<Biome> SPAWNS_MINISHEEP = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_minisheep"));
    public static final TagKey<Biome> SPAWNS_CASSOWARY = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_cassowary"));
    public static final TagKey<Biome> SPAWNS_HEDGEHOG = TagKey.create(Registries.BIOME, WilderNature.identifier("spawns_hedgehog"));
    public static final TagKey<EntityType<?>> OWL_TARGETS = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("owl_targets"));
    public static final TagKey<EntityType<?>> SWIFT_FOX_TARGETS = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("swift_fox_targets"));
    public static final TagKey<EntityType<?>> NEUTRAL = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("bounty_huntable/neutral"));
    public static final TagKey<EntityType<?>> DEFENSIVE = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("bounty_huntable/defensive"));
    public static final TagKey<EntityType<?>> AGGRESSIVE = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("bounty_huntable/aggressive"));
    public static final TagKey<EntityType<?>> BOSS = TagKey.create(Registries.ENTITY_TYPE, WilderNature.identifier("bounty_huntable/boss"));
}

