package net.satisfy.wildernature.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.satisfy.wildernature.client.gui.handlers.BountyBlockScreenHandler;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class BountyBlockNetworking {
    public static final ResourceLocation ID_SCREEN_UPDATE = WilderNatureIdentifier.of("screen_update");
    public static final ResourceLocation ID_SCREEN_ACTION = WilderNatureIdentifier.of("screen_action");
    public static final int MAX_SIZE = 32768;

    public enum BountyServerUpdateType {
        SEND_BOARD_DATA,
        UPDATE_CONTRACTS,
        SET_ACTIVE_CONTRACT,
        CLEAR_ACTIVE_CONTRACT,
        MULTI
    }

    public enum BountyClientActionType {
        REROLL,
        CONFIRM_CONTRACT,
        FINISH_CONTRACT,
        DELETE_CONTRACT
    }

    static void handleServerUpdate(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof BountyBlockScreenHandler screenHandler) {
            screenHandler.onServerUpdate(buf);
        }
    }

    static void handleClientAction(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        Player player = context.getPlayer();
        if (player.containerMenu instanceof BountyBlockScreenHandler screenHandler) {
            screenHandler.handleClientAction((ServerPlayer) player, buf);
        }
    }

    public static void register() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, ID_SCREEN_UPDATE, (buf, context) -> {
            Player player = context.getPlayer();
            if (player.containerMenu instanceof BountyBlockScreenHandler screenHandler) {
                screenHandler.onServerUpdate(buf);
            }
        });

        NetworkManager.registerReceiver(NetworkManager.Side.C2S, ID_SCREEN_ACTION, (buf, context) -> {
            ServerPlayer player = (ServerPlayer) context.getPlayer();
            if (player.containerMenu instanceof BountyBlockScreenHandler screenHandler) {
                screenHandler.handleClientAction(player, buf);
            }
        });
    }
}
