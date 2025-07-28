package net.satisfy.wildernature.network;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;

public class EntityPacketHandler {
    public static Packet<ClientGamePacketListener> createAddEntityPacket(Entity entity, ServerEntity server) {
        return NetworkManager.createAddEntityPacket(entity, server);
    }
}
