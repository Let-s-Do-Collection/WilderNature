package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.world.feature.HollowCacheFeature;
import net.satisfy.wildernature.core.world.tree.decorator.TermiteMoundDecorator;

public class WorldgenRegistry {
    public static final DeferredRegister<TreeDecoratorType<?>> TREE_DECORATOR_TYPES = DeferredRegister.create(WilderNature.MOD_ID, Registries.TREE_DECORATOR_TYPE);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(WilderNature.MOD_ID, Registries.FEATURE);

    public static final RegistrySupplier<TreeDecoratorType<TermiteMoundDecorator>> TERMITE_MOUND_DECORATOR = TREE_DECORATOR_TYPES.register("termite_mound_decorator", () -> new TreeDecoratorType<>(TermiteMoundDecorator.CODEC));
    public static final RegistrySupplier<Feature<NoneFeatureConfiguration>> HOLLOW_CACHE = FEATURES.register("hollow_cache", () -> new HollowCacheFeature(NoneFeatureConfiguration.CODEC));

    public static void init() {
        TREE_DECORATOR_TYPES.register();
        FEATURES.register();
    }
}