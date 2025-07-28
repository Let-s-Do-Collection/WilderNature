package net.satisfy.wildernature.fabric.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class PlacedFeatures {
    public static final ResourceKey<PlacedFeature> PATCH_HAZELNUT_BUSH = registerKey("patch_hazelnut_bush");

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, WilderNatureIdentifier.of(name));
    }
}
