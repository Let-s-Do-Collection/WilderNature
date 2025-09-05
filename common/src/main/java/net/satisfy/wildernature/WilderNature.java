package net.satisfy.wildernature;

import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.core.registry.*;

public class WilderNature {
    public static final String MOD_ID = "wildernature";

    public static ResourceLocation identifier(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        ObjectRegistry.init();
        EntityTypeRegistry.init();
        RecipeRegistry.init();
        TabRegistry.init();
        SoundRegistry.init();
    }
}

