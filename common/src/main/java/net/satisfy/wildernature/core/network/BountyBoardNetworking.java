package net.satisfy.wildernature.core.network;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.gui.handler.BountyBoardMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("removal")
public final class BountyBoardNetworking {
    public static final ResourceLocation ACCEPT = WilderNature.identifier("bounty_accept");
    public static final ResourceLocation ABANDON = WilderNature.identifier("bounty_abandon");
    public static final ResourceLocation CLAIM = WilderNature.identifier("bounty_claim");
    public static final ResourceLocation SELECT = WilderNature.identifier("bounty_select");
    public static final ResourceLocation SYNC = WilderNature.identifier("bounty_sync");

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.c2s(), ACCEPT, (buffer, context) -> context.queue(() -> {
            ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
            if (serverPlayer.containerMenu instanceof BountyBoardMenu menu) {
                menu.acceptSelectedBounty();
                sendSync(serverPlayer, menu);
            }
        }));

        NetworkManager.registerReceiver(NetworkManager.c2s(), ABANDON, (buffer, context) -> context.queue(() -> {
            ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
            if (serverPlayer.containerMenu instanceof BountyBoardMenu menu) {
                menu.abandonActiveBounty(serverPlayer);
                menu.broadcastChanges();
                sendSync(serverPlayer, menu);
            }
        }));

        NetworkManager.registerReceiver(NetworkManager.c2s(), CLAIM, (buffer, context) -> context.queue(() -> {
            ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
            if (serverPlayer.containerMenu instanceof BountyBoardMenu menu) {
                menu.claimActiveBounty();
                sendSync(serverPlayer, menu);
            }
        }));

        NetworkManager.registerReceiver(NetworkManager.c2s(), SELECT, (buffer, context) -> {
            int selectedIndex = buffer.readInt();
            context.queue(() -> {
                ServerPlayer serverPlayer = (ServerPlayer) context.getPlayer();
                if (serverPlayer.containerMenu instanceof BountyBoardMenu menu) {
                    menu.setSelectedBountyIndex(selectedIndex);
                    menu.broadcastChanges();
                    sendSync(serverPlayer, menu);
                }
            });
        });

        NetworkManager.registerReceiver(NetworkManager.s2c(), SYNC, (buffer, context) -> {
            int selectedBountyIndex = buffer.readInt();
            boolean hasActiveBounty = buffer.readBoolean();
            int activeBountyIndex = buffer.readInt();
            int activeProgress = buffer.readInt();
            int activeRequiredKills = buffer.readInt();
            boolean activeCompleted = buffer.readBoolean();
            boolean restoreContractAvailable = buffer.readBoolean();
            int abandonedCount = buffer.readInt();
            List<UUID> abandonedBountyIds = new ArrayList<>(abandonedCount);

            for (int index = 0; index < abandonedCount; index++) {
                abandonedBountyIds.add(buffer.readUUID());
            }

            context.queue(() -> {
                if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.containerMenu instanceof BountyBoardMenu menu) {
                    menu.applySyncFromServer(
                            selectedBountyIndex,
                            hasActiveBounty,
                            activeBountyIndex,
                            activeProgress,
                            activeRequiredKills,
                            activeCompleted,
                            restoreContractAvailable,
                            abandonedBountyIds
                    );
                }
            });
        });
    }

    public static void sendAccept() {
        NetworkManager.sendToServer(ACCEPT, createClientBuffer());
    }

    public static void sendAbandon() {
        NetworkManager.sendToServer(ABANDON, createClientBuffer());
    }

    public static void sendSelect(int selectedIndex) {
        RegistryFriendlyByteBuf buffer = createClientBuffer();
        buffer.writeInt(selectedIndex);
        NetworkManager.sendToServer(SELECT, buffer);
    }

    public static void sendSync(ServerPlayer serverPlayer, BountyBoardMenu menu) {
        sendSync(
                serverPlayer,
                menu.getSelectedBountyIndex(),
                menu.hasActiveBounty(),
                menu.getActiveBounty().map(activeBounty -> menu.getVisibleBounties().indexOf(activeBounty)).orElse(-1),
                menu.getActiveProgress(),
                menu.getActiveRequiredKills(),
                menu.hasCompletedActiveBounty(),
                menu.hasRestoreContractAvailable(),
                new ArrayList<>(menu.getAbandonedBountyIds())
        );
    }

    public static void sendSync(ServerPlayer serverPlayer, int selectedBountyIndex, boolean hasActiveBounty, int activeBountyIndex, int activeProgress, int activeRequiredKills, boolean activeCompleted, boolean restoreContractAvailable, List<UUID> abandonedBountyIds) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), serverPlayer.registryAccess());
        buffer.writeInt(selectedBountyIndex);
        buffer.writeBoolean(hasActiveBounty);
        buffer.writeInt(activeBountyIndex);
        buffer.writeInt(activeProgress);
        buffer.writeInt(activeRequiredKills);
        buffer.writeBoolean(activeCompleted);
        buffer.writeBoolean(restoreContractAvailable);
        buffer.writeInt(abandonedBountyIds.size());

        for (UUID abandonedBountyId : abandonedBountyIds) {
            buffer.writeUUID(abandonedBountyId);
        }

        NetworkManager.sendToPlayer(serverPlayer, SYNC, buffer);
    }

    private static RegistryFriendlyByteBuf createClientBuffer() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() != null) {
            return new RegistryFriendlyByteBuf(Unpooled.buffer(), minecraft.getConnection().registryAccess());
        }
        if (minecraft.level != null) {
            return new RegistryFriendlyByteBuf(Unpooled.buffer(), minecraft.level.registryAccess());
        }
        throw new IllegalStateException("Missing client registry access");
    }

    private BountyBoardNetworking() {
    }
}