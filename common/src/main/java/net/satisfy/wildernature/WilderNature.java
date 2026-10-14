package net.satisfy.wildernature;

import dev.architectury.platform.Platform;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.satisfy.wildernature.core.compat.AccessoriesCompat;
import net.satisfy.wildernature.core.event.BountyEvents;
import net.satisfy.wildernature.core.event.ThickLeatherScissorHandler;
import net.satisfy.wildernature.core.event.VanillaBoneThrowEvent;
import net.satisfy.wildernature.core.fieldguide.FieldGuideDataLoader;
import net.satisfy.wildernature.core.network.BountyBoardNetworking;
import net.satisfy.wildernature.core.registry.*;
import dev.architectury.event.events.common.LifecycleEvent;
import net.satisfy.foundation.rarity.FoundationRarities;
import net.satisfy.foundation.rarity.FoundationRarity;

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
        if (Platform.isModLoaded("accessories")) {
            AccessoriesCompat.init();
        }
        LifecycleEvent.SETUP.register(WilderNature::registerRarities);
    }

    private static void registerRarities() {
        FoundationRarities.register(ObjectRegistry.FOX_TRAPPER_BANNER.get(), FoundationRarity.LEGENDARY);
        FoundationRarities.register(ObjectRegistry.BUNNY_STALKER_BANNER.get(), FoundationRarity.LEGENDARY);
        FoundationRarities.register(ObjectRegistry.COD_CATCHER_BANNER.get(), FoundationRarity.LEGENDARY);
        FoundationRarities.register(ObjectRegistry.BLUNDERBUSS.get(), FoundationRarity.EPIC);
        FoundationRarities.register(ObjectRegistry.FLINT_AMMUNITION.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.DIAMOND_AMMUNITION.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.DEER_TROPHY.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.SWIFT_FOX_TROPHY.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.BISON_TROPHY.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.FUR_CLOAK.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.BISON_HORN.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.THICK_LEATHER.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.FIELD_NOTES.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.PATHFINDERS_CALL.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.PROVISION_REQUEST.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.TRACKING_ORDER.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.GUILD_COMMISSION.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.ELITE_BOUNTY.get(), FoundationRarity.MYTHIC);
        FoundationRarities.register(ObjectRegistry.FIELD_GUIDE.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.TRUFFLE.get(), FoundationRarity.RARE);
        FoundationRarities.register(ObjectRegistry.STYLIN_PURPLE_HAT.get(), FoundationRarity.EPIC);
        FoundationRarities.register(ObjectRegistry.LOOT_BAG.get(), FoundationRarity.UNCOMMON);
        FoundationRarities.register(ObjectRegistry.BURST_OF_EXPERIENCE.get(), FoundationRarity.CELESTIAL);
    }
}

