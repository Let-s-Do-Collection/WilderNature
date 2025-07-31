package net.satisfy.wildernature;

import dev.architectury.event.events.common.LifecycleEvent;
import net.satisfy.wildernature.network.BountyBlockNetworking;
import net.satisfy.wildernature.network.BountyEntrypoints;
import net.satisfy.wildernature.registry.*;
import net.satisfy.wildernature.util.contract.ContractInProgress;

public class WilderNature {
    public static final String MOD_ID = "wildernature";

    public static void init() {
        ObjectRegistry.init();
        EntityRegistry.init();
        RecipeRegistry.init();
        TabRegistry.init();
        SoundRegistry.init();
        BountyEntrypoints.serverEntry();
        BountyBlockNetworking.register();
    }

    public static void commonInit() {
        LifecycleEvent.SERVER_BEFORE_START.register(instance -> ContractInProgress.progressPerPlayer.clear());
    }
}

