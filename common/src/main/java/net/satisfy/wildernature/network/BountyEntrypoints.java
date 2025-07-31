package net.satisfy.wildernature.network;

import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.satisfy.wildernature.util.contract.ContractInProgress;

public class BountyEntrypoints {
    public static void clientEntry() {
        // @reason - do not duplicate register
        // NetworkManager.registerReceiver(NetworkManager.Side.S2C, BountyBlockNetworking.ID_SCREEN_UPDATE, BountyBlockNetworking::handleServerUpdate);
    }

    public static void serverEntry() {
        // @reason - do not duplicate register
        // NetworkManager.registerReceiver(NetworkManager.Side.C2S, BountyBlockNetworking.ID_SCREEN_ACTION, BountyBlockNetworking::handleClientAction);
        EntityEvent.LIVING_DEATH.register(ContractInProgress::onEntityDeath);
        TickEvent.SERVER_POST.register(ContractInProgress::onServerTick);
    }
}
