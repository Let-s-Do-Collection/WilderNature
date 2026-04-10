package net.satisfy.wildernature.core.registry;

import dev.architectury.core.item.ArchitecturySpawnEggItem;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.block.*;
import net.satisfy.wildernature.core.item.*;
import net.satisfy.wildernature.core.util.WilderNatureUtil;

import java.util.function.Consumer;
import java.util.function.Supplier;


public class ObjectRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(WilderNature.MOD_ID, Registries.ITEM);
    public static final Registrar<Item> ITEM_REGISTRAR = ITEMS.getRegistrar();
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(WilderNature.MOD_ID, Registries.BLOCK);
    public static final Registrar<Block> BLOCK_REGISTRAR = BLOCKS.getRegistrar();

    public static final RegistrySupplier<Item> BISON_MEAT = registerItem("bison_meat", () -> new Item(getSettings().food(Foods.BEEF)));
    public static final RegistrySupplier<Item> COOKED_BISON_MEAT = registerItem("cooked_bison_meat", () -> new Item(getSettings().food(Foods.COOKED_BEEF)));
    public static final RegistrySupplier<Item> VENISON = registerItem("venison", () -> new Item(getSettings().food(Foods.MUTTON)));
    public static final RegistrySupplier<Item> COOKED_VENISON = registerItem("cooked_venison", () -> new Item(getSettings().food(Foods.COOKED_MUTTON)));
    public static final RegistrySupplier<Item> TURKEY_MEAT = registerItem("turkey_meat", () -> new Item(getSettings().food(Foods.CHICKEN)));
    public static final RegistrySupplier<Item> COOKED_TURKEY_MEAT = registerItem("cooked_turkey_meat", () -> new Item(getSettings().food(Foods.COOKED_CHICKEN)));
    public static final RegistrySupplier<Item> CASSOWARY_MEAT = registerItem("cassowary_meat", () -> new Item(getSettings().food(Foods.RABBIT)));
    public static final RegistrySupplier<Item> COOKED_CASSOWARY_MEAT = registerItem("cooked_cassowary_meat", () -> new Item(getSettings().food(Foods.COOKED_RABBIT)));
    public static final RegistrySupplier<Item> BLUNDERBUSS = registerItem("blunderbuss", BlunderBussItem::new);
    public static final RegistrySupplier<Item> FLINT_AMMUNITION = registerItem("flint_ammunition", () -> new FlintAmmunitionItem(getSettings().rarity(Rarity.UNCOMMON), 2));
    public static final RegistrySupplier<Item> DIAMOND_AMMUNITION = registerItem("diamond_ammunition", () -> new AmmunitionItem(getSettings().rarity(Rarity.COMMON), 12));
    public static final RegistrySupplier<Item> FUR_CLOAK = registerItem("fur_cloak", () -> new FurCloakItem(ArmorMaterials.LEATHER.value(), ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistrySupplier<Item> FISH_OIL = registerItem("fish_oil", () -> new Item(getSettings().stacksTo(16)));
    public static final RegistrySupplier<Item> LOOT_BAG = registerItem("loot_bag", () -> new LootBagItem(getSettings().rarity(Rarity.COMMON)));
    public static final RegistrySupplier<Item> BISON_HORN = registerItem("bison_horn", () -> new BisonHornItem(new Item.Properties().stacksTo(1), SoundEventRegistry.BISON_HORN.get()));
    public static final RegistrySupplier<Item> FIELD_NOTES = registerItem("field_notes", () -> new ContractItem(getSettings().rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> PATHFINDERS_CALL = registerItem("pathfinders_call", () -> new ContractItem(getSettings().rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> TRACKING_ORDER = registerItem("tracking_order", () -> new ContractItem(getSettings().rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> PROVISION_REQUEST = registerItem("provision_request", () -> new ContractItem(getSettings().rarity(Rarity.UNCOMMON)));
    public static final RegistrySupplier<Item> ELITE_BOUNTY = registerItem("elite_bounty", () -> new ContractItem(getSettings().rarity(Rarity.EPIC)));
    public static final RegistrySupplier<Item> GUILD_COMMISSION = registerItem("guild_commission", () -> new ContractItem(getSettings().rarity(Rarity.RARE)));
    public static final RegistrySupplier<Item> DEER_SPAWN_EGG = registerItem("deer_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.DEER, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> SWIFT_FOX_SPAWN_EGG = registerItem("swift_fox_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.SWIFT_FOX, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> RACCOON_SPAWN_EGG = registerItem("raccoon_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.RACCOON, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> SQUIRREL_SPAWN_EGG = registerItem("squirrel_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.SQUIRREL, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> OWL_SPAWN_EGG = registerItem("owl_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.OWL, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> BOAR_SPAWN_EGG = registerItem("boar_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.BOAR, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> BISON_SPAWN_EGG = registerItem("bison_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.BISON, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> GIRAFFE_SPAWN_EGG = registerItem("giraffe_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.GIRAFFE, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> DOG_SPAWN_EGG = registerItem("dog_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.DOG, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> MINISHEEP_SPAWN_EGG = registerItem("minisheep_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.MINISHEEP, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> HIPPO_SPAWN_EGG = registerItem("hippo_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.HIPPO, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> TURKEY_SPAWN_EGG = registerItem("turkey_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.TURKEY, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> CASSOWARY_SPAWN_EGG = registerItem("cassowary_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.CASSOWARY, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> HEDGEHOG_SPAWN_EGG = registerItem("hedgehog_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.HEDGEHOG, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> TRUFFLE = registerItem("truffle", () -> new Item(getSettings().rarity(Rarity.RARE)));
    public static final RegistrySupplier<Block> HAZELNUT_BUSH = registerWithoutItem("hazelnut_bush", () -> new HazelnutBushBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH).pushReaction(PushReaction.DESTROY)));
    public static final RegistrySupplier<Item> HAZELNUT = registerItem("hazelnut", () -> new ItemNameBlockItem(ObjectRegistry.HAZELNUT_BUSH.get(), getSettings().food((new FoodProperties.Builder()).nutrition(4).saturationModifier(0.3F).fast().build())));
    public static final RegistrySupplier<Block> BOUNTY_BOARD = registerWithItem("bounty_board", () -> new BountyBoardBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    public static final RegistrySupplier<Block> DEER_TROPHY = registerWithItem("deer_trophy", () -> new DeerTrophyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    public static final RegistrySupplier<Block> SWIFT_FOX_TROPHY = registerWithItem("swift_fox_trophy", () -> new RedWolfTrophyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    public static final RegistrySupplier<Block> BISON_TROPHY = registerWithItem("bison_trophy", () -> new BisonTrophyBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)));
    public static final RegistrySupplier<Block> FOX_TRAPPER_BANNER = registerWithItem("fox_trapper_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> FOX_TRAPPER_WALL_BANNER = registerWithoutItem("fox_trapper_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BUNNY_STALKER_BANNER = registerWithItem("bunny_stalker_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> BUNNY_STALKER_WALL_BANNER = registerWithoutItem("bunny_stalker_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> COD_CATCHER_BANNER = registerWithItem("cod_catcher_banner", () -> new CompletionistBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Block> COD_CATCHER_WALL_BANNER = registerWithoutItem("cod_catcher_wall_banner", () -> new CompletionistWallBannerBlock(BlockBehaviour.Properties.of().strength(1F).instrument(NoteBlockInstrument.BASS).noCollission().sound(SoundType.WOOD)));
    public static final RegistrySupplier<Item> STYLIN_PURPLE_HAT = registerItem("stylin_purple_hat", () -> new StylinPurpleHatItem(ArmorMaterialRegistry.STYLIN_HAT.value(), ArmorItem.Type.HELMET, getSettings().rarity(Rarity.RARE), WilderNature.identifier("textures/models/armor/stylin_purple_hat.png")));
    public static final RegistrySupplier<Item> TURKEY_EGG = registerItem("turkey_egg", () -> new TurkeyEggItem(getSettings()));
    public static final RegistrySupplier<Block> TRUFFLE_BAG = registerWithItem("truffle_bag", () -> new BagBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACK_WOOL)));
    public static final RegistrySupplier<Block> HOLLOW_CACHE = registerWithItem("hollow_cache", () -> new HollowCacheBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).strength(2.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final RegistrySupplier<Block> BURROW = registerWithItem("burrow", () -> new BurrowBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK)));
    public static final RegistrySupplier<Block> BROWN_MUSHROOM_COLONY = registerWithItem("brown_mushroom_colony", () -> new MushroomColonyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)));
    public static final RegistrySupplier<Block> RED_MUSHROOM_COLONY = registerWithItem("red_mushroom_colony", () -> new MushroomColonyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).randomTicks().noCollission().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY)));
    public static final RegistrySupplier<Item> BURST_OF_EXPERIENCE = registerItem("burst_of_experience", () -> new ExperienceBurstItem(new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistrySupplier<Item> THICK_LEATHER = registerItem("thick_leather", () -> new Item(getSettings()));
    public static final RegistrySupplier<Item> ELEPHANT_SPAWN_EGG = registerItem("elephant_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.ELEPHANT, -1, -1, getSettings()));
    public static final RegistrySupplier<Item> BEAVER_SPAWN_EGG = registerItem("beaver_spawn_egg", () -> new ArchitecturySpawnEggItem(EntityTypeRegistry.BEAVER, -1, -1, getSettings()));
    public static final RegistrySupplier<Block> BEAVER_DAM = registerWithItem("beaver_dam", () -> new BeaverDamBlock(BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY).instabreak()));

    /**
     * Ideas for Items:
     * Animal Compendium
     * Ideas for Animals:
     * Ram, rideable - just like a slow Horse with LOTS of health that pushes away all other entities
     * Koala
     * Chameleon
     * Kangaroos
     * Jaguars
     * Porcupines
     * Bears
     * Crocodiles
     */


    public static void init() {
        ITEMS.register();
        BLOCKS.register();
    }

    public static BlockBehaviour.Properties properties(float strength) {
        return properties(strength, strength);
    }

    public static BlockBehaviour.Properties properties(float breakSpeed, float explosionResist) {
        return BlockBehaviour.Properties.of().strength(breakSpeed, explosionResist);
    }

    private static Item.Properties getSettings(Consumer<Item.Properties> consumer) {
        Item.Properties settings = new Item.Properties();
        consumer.accept(settings);
        return settings;
    }

    static Item.Properties getSettings() {
        return getSettings(settings -> {
        });
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(String name, Supplier<T> block) {
        return WilderNatureUtil.registerWithItem(BLOCKS, BLOCK_REGISTRAR, ITEMS, ITEM_REGISTRAR, WilderNature.identifier(name), block);
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(String path, Supplier<T> block) {
        return WilderNatureUtil.registerWithoutItem(BLOCKS, BLOCK_REGISTRAR, WilderNature.identifier(path), block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(String path, Supplier<T> itemSupplier) {
        return WilderNatureUtil.registerItem(ITEMS, ITEM_REGISTRAR, WilderNature.identifier(path), itemSupplier);
    }
}
