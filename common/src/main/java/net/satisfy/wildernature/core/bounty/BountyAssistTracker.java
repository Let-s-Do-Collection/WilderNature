package net.satisfy.wildernature.core.bounty;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class BountyAssistTracker {
    private static final Map<UUID, LinkedHashSet<UUID>> ENTITY_ASSIST_PLAYERS = new ConcurrentHashMap<>();

    private BountyAssistTracker() {
    }

    public static void recordDamage(Entity targetEntity, Entity sourceEntity) {
        if (targetEntity == null || sourceEntity == null) {
            return;
        }

        ServerPlayer attackingPlayer = getAttackingPlayer(sourceEntity);
        if (attackingPlayer == null) {
            return;
        }

        ENTITY_ASSIST_PLAYERS.computeIfAbsent(targetEntity.getUUID(), ignoredEntityId -> new LinkedHashSet<>()).add(attackingPlayer.getUUID());
    }

    public static List<UUID> consumeAssistingPlayers(Entity targetEntity) {
        LinkedHashSet<UUID> assistingPlayers = ENTITY_ASSIST_PLAYERS.remove(targetEntity.getUUID());
        if (assistingPlayers == null || assistingPlayers.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(assistingPlayers);
    }

    public static void clear(Entity targetEntity) {
        ENTITY_ASSIST_PLAYERS.remove(targetEntity.getUUID());
    }

    private static ServerPlayer getAttackingPlayer(Entity sourceEntity) {
        if (sourceEntity instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        if (sourceEntity instanceof Projectile projectile && projectile.getOwner() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }

        return null;
    }
}