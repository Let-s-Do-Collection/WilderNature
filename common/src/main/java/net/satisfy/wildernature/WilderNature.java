package net.satisfy.wildernature;

import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.core.event.BountyEvents;
import net.satisfy.wildernature.core.event.VanillaBoneThrowEvent;
import net.satisfy.wildernature.core.network.BountyBoardNetworking;
import net.satisfy.wildernature.core.registry.*;

public class WilderNature {
    public static final String MOD_ID = "wildernature";

    public static ResourceLocation identifier(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }

    public static void init() {
        ObjectRegistry.init();
        EntityTypeRegistry.init();
        VanillaBoneThrowEvent.init();
        BountyEvents.init();
        BountyBoardNetworking.init();
        MenuTypeRegistry.init();
        ParticleTypeRegistry.init();
        TreeDecoratorTypeRegistry.init();
        RecipeRegistry.init();
        TabRegistry.init();
        SoundEventRegistry.init();
    }
}

