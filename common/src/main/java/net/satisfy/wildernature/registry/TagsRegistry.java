package net.satisfy.wildernature.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class TagsRegistry {
    public static final TagKey<Item> SQUIRREL_HOLDABLE = TagKey.create(Registries.ITEM, WilderNatureIdentifier.of("squirrel_holdable"));
    public static final TagKey<Item> CAN_BE_TRUFFLED = TagKey.create(Registries.ITEM, WilderNatureIdentifier.of("can_be_truffled"));
    public static final TagKey<Item> LOOT_BAG_BLACKLIST = TagKey.create(Registries.ITEM, WilderNatureIdentifier.of("loot_bag_blacklist"));
    public static final TagKey<Block> MAKES_BLOCK_GLOW = TagKey.create(Registries.BLOCK, WilderNatureIdentifier.of("makes_block_glow"));
    public static final TagKey<Biome> SPAWNS_DEER = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_deer"));
    public static final TagKey<Biome> SPAWNS_BOAR = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_boar"));
    public static final TagKey<Biome> SPAWNS_OWL = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_owl"));
    public static final TagKey<Biome> SPAWNS_BISON = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_bison"));
    public static final TagKey<Biome> SPAWNS_TURKEY = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_turkey"));
    public static final TagKey<Biome> SPAWNS_RACCOON = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_raccoon"));
    public static final TagKey<Biome> SPAWNS_RED_WOLF = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_red_wolf"));
    public static final TagKey<Biome> SPAWNS_SQUIRREL = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_squirrel"));
    public static final TagKey<Biome> SPAWNS_DOG = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_dog"));
    public static final TagKey<Biome> SPAWNS_PENGUIN = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_penguin"));
    public static final TagKey<Biome> SPAWNS_MINISHEEP = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_minisheep"));
    public static final TagKey<Biome> SPAWNS_CASSOWARY = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_cassowary"));
    public static final TagKey<Biome> SPAWNS_HEDGEHOG = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_hedgehog"));
    public static final TagKey<Biome> SPAWNS_FLAMINGO = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_flamingo"));
    public static final TagKey<Biome> SPAWNS_PELICAN = TagKey.create(Registries.BIOME, WilderNatureIdentifier.of("spawns_pelican"));
    public static final TagKey<EntityType<?>> OWL_TARGETS = TagKey.create(Registries.ENTITY_TYPE, WilderNatureIdentifier.of("owl_targets"));
}

