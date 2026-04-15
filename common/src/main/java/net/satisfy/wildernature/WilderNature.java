package net.satisfy.wildernature;

import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.satisfy.wildernature.core.event.BountyEvents;
import net.satisfy.wildernature.core.event.ThickLeatherScissorHandler;
import net.satisfy.wildernature.core.event.VanillaBoneThrowEvent;
import net.satisfy.wildernature.core.fieldguide.FieldGuideDataLoader;
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
        ThickLeatherScissorHandler.init();
        BountyEvents.init();
        MobEffectRegistry.init();
        BountyBoardNetworking.init();
        MenuTypeRegistry.init();
        ParticleTypeRegistry.init();
        WorldgenRegistry.init();
        RecipeRegistry.init();
        TabRegistry.init();
        SoundEventRegistry.init();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, FieldGuideDataLoader.INSTANCE);
    }
}

