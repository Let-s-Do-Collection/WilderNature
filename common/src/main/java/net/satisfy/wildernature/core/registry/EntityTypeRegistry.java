package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.block.entity.BountyBoardBlockEntity;
import net.satisfy.wildernature.core.block.entity.BurrowBlockEntity;
import net.satisfy.wildernature.core.block.entity.CompletionistBannerEntity;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.animal.BisonEntity;
import net.satisfy.wildernature.core.entity.animal.BoarEntity;
import net.satisfy.wildernature.core.entity.projectile.BulletEntity;
import net.satisfy.wildernature.core.entity.monster.CassowaryEntity;
import net.satisfy.wildernature.core.entity.animal.DeerEntity;
import net.satisfy.wildernature.core.entity.animal.DogEntity;
import net.satisfy.wildernature.core.entity.animal.HedgehogEntity;
import net.satisfy.wildernature.core.entity.animal.MiniSheepEntity;
import net.satisfy.wildernature.core.entity.animal.OwlEntity;
import net.satisfy.wildernature.core.entity.animal.RaccoonEntity;
import net.satisfy.wildernature.core.entity.animal.RedWolfEntity;
import net.satisfy.wildernature.core.entity.animal.SquirrelEntity;
import net.satisfy.wildernature.core.entity.projectile.ThrownBoneEntity;
import net.satisfy.wildernature.core.entity.projectile.ThrownTurkeyEgg;
import net.satisfy.wildernature.core.entity.animal.TurkeyEntity;

public class EntityTypeRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(WilderNature.MOD_ID, Registries.BLOCK_ENTITY_TYPE);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(WilderNature.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<CompletionistBannerEntity>> COMPLETIONIST_BANNER_BLOCK_ENTITY = createBlockEntity("completionist_banner", () -> BlockEntityType.Builder.of(CompletionistBannerEntity::new, ObjectRegistry.WOLF_TRAPPER_BANNER.get(), ObjectRegistry.WOLF_TRAPPER_WALL_BANNER.get(), ObjectRegistry.BUNNY_STALKER_BANNER.get(), ObjectRegistry.BUNNY_STALKER_WALL_BANNER.get(), ObjectRegistry.COD_CATCHER_BANNER.get(), ObjectRegistry.COD_CATCHER_WALL_BANNER.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BountyBoardBlockEntity>> BOUNTY_BOARD_BLOCK_ENTITY = createBlockEntity("bounty_board", () -> BlockEntityType.Builder.of(BountyBoardBlockEntity::new, ObjectRegistry.BOUNTY_BOARD.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<HollowCacheBlockEntity>> HOLLOW_CACHE_BLOCK_ENTITY = createBlockEntity("hollow_cache", () -> BlockEntityType.Builder.of(HollowCacheBlockEntity::new, ObjectRegistry.HOLLOW_CACHE.get()).build(null));
    public static final RegistrySupplier<BlockEntityType<BurrowBlockEntity>> BURROW_BLOCK_ENTITY = createBlockEntity("burrow", () -> BlockEntityType.Builder.of(BurrowBlockEntity::new, ObjectRegistry.BURROW.get()).build(null));

    public static final RegistrySupplier<EntityType<BisonEntity>> BISON = createEntity("bison", () -> EntityType.Builder.of(BisonEntity::new, MobCategory.CREATURE).sized(1.8f, 2.2f).build(WilderNature.identifier("bison").toString()));
    public static final RegistrySupplier<EntityType<BoarEntity>> BOAR = createEntity("boar", () -> EntityType.Builder.of(BoarEntity::new, MobCategory.CREATURE).sized(1.1f, 1.1f).build(WilderNature.identifier("boar").toString()));
    public static final RegistrySupplier<EntityType<CassowaryEntity>> CASSOWARY = createEntity("cassowary", () -> EntityType.Builder.of(CassowaryEntity::new, MobCategory.CREATURE).sized(0.8f, 0.8f).clientTrackingRange(10).build(WilderNature.identifier("cassowary").toString()));
    public static final RegistrySupplier<EntityType<DeerEntity>> DEER = createEntity("deer", () -> EntityType.Builder.of(DeerEntity::new, MobCategory.CREATURE).sized(1.3F, 1.6F).build(WilderNature.identifier("deer").toString()));
    public static final RegistrySupplier<EntityType<DogEntity>> DOG = createEntity("dog", () -> EntityType.Builder.of(DogEntity::new, MobCategory.CREATURE).sized(0.9f, 1.3f).build(WilderNature.identifier("dog").toString()));
    public static final RegistrySupplier<EntityType<HedgehogEntity>> HEDGEHOG = createEntity("hedgehog", () -> EntityType.Builder.of(HedgehogEntity::new, MobCategory.CREATURE).sized(0.3f, 0.3f).clientTrackingRange(10).build(WilderNature.identifier("hedgehog").toString()));
    public static final RegistrySupplier<EntityType<MiniSheepEntity>> MINISHEEP = createEntity("minisheep", () -> EntityType.Builder.of(MiniSheepEntity::new, MobCategory.CREATURE).sized(0.9f, 1.3f).build(WilderNature.identifier("minisheep").toString()));
    public static final RegistrySupplier<EntityType<OwlEntity>> OWL = createEntity("owl", () -> EntityType.Builder.of(OwlEntity::new, MobCategory.CREATURE).sized(0.6F, 1.0F).build(WilderNature.identifier("owl").toString()));
   public static final RegistrySupplier<EntityType<RaccoonEntity>> RACCOON = createEntity("raccoon", () -> EntityType.Builder.of(RaccoonEntity::new, MobCategory.CREATURE).sized(0.6f, 0.6f).build(WilderNature.identifier("raccoon").toString()));
    public static final RegistrySupplier<EntityType<RedWolfEntity>> RED_WOLF = createEntity("red_wolf", () -> EntityType.Builder.of(RedWolfEntity::new, MobCategory.CREATURE).sized(0.7f, 0.9f).clientTrackingRange(10).build(String.valueOf(WilderNature.identifier("red_wolf"))));
    public static final RegistrySupplier<EntityType<SquirrelEntity>> SQUIRREL = createEntity("squirrel", () -> EntityType.Builder.of(SquirrelEntity::new, MobCategory.CREATURE).sized(0.4f, 0.9f).build(WilderNature.identifier("squirrel").toString()));
    public static final RegistrySupplier<EntityType<TurkeyEntity>> TURKEY = createEntity("turkey", () -> EntityType.Builder.of(TurkeyEntity::new, MobCategory.CREATURE).sized(0.6F, 1.0F).build(WilderNature.identifier("turkey").toString()));
    public static final RegistrySupplier<EntityType<BulletEntity>> BULLET = createEntity("bullet", () -> EntityType.Builder.<BulletEntity>of(BulletEntity::new, MobCategory.MISC).sized(0.3125f, 0.3125f).clientTrackingRange(64).updateInterval(2).build(WilderNature.identifier("bullet").toString()));
    public static final RegistrySupplier<EntityType<ThrownTurkeyEgg>> TURKEY_EGG = createEntity("turkey_egg", () -> EntityType.Builder.<ThrownTurkeyEgg>of(ThrownTurkeyEgg::new, MobCategory.MISC).sized(0.25f, 0.25f).build(WilderNature.identifier("turkey_egg").toString()));
    public static final RegistrySupplier<EntityType<ThrownBoneEntity>> BONE = createEntity("bone", () -> EntityType.Builder.<ThrownBoneEntity>of(ThrownBoneEntity::new, MobCategory.MISC).sized(0.25f, 0.25f).build(WilderNature.identifier("bone").toString()));

    public static <T extends EntityType<?>> RegistrySupplier<T> createEntity(final String path, final Supplier<T> type) {
        return ENTITY_TYPES.register(WilderNature.identifier(path), type);
    }

    private static <T extends BlockEntityType<?>> RegistrySupplier<T> createBlockEntity(final String path, final Supplier<T> type) {
        return BLOCK_ENTITIES.register(WilderNature.identifier(path), type);
    }

    public static void init() {
        ENTITY_TYPES.register();
        BLOCK_ENTITIES.register();
        EntityAttributeRegistry.register(BISON, BisonEntity::createMobAttributes);
        EntityAttributeRegistry.register(BOAR, BoarEntity::createMobAttributes);
        EntityAttributeRegistry.register(CASSOWARY, CassowaryEntity::createMobAttributes);
        EntityAttributeRegistry.register(DEER, DeerEntity::createMobAttributes);
        EntityAttributeRegistry.register(DOG, DogEntity::createMobAttributes);
        EntityAttributeRegistry.register(HEDGEHOG, HedgehogEntity::createMobAttributes);
        EntityAttributeRegistry.register(MINISHEEP, MiniSheepEntity::createMobAttributes);
        EntityAttributeRegistry.register(OWL, OwlEntity::createMobAttributes);
        EntityAttributeRegistry.register(RACCOON, RaccoonEntity::createMobAttributes);
        EntityAttributeRegistry.register(RED_WOLF, RedWolfEntity::createMobAttributes);
        EntityAttributeRegistry.register(SQUIRREL, SquirrelEntity::createMobAttributes);
        EntityAttributeRegistry.register(TURKEY, TurkeyEntity::createMobAttributes);
    }
}