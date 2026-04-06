package net.satisfy.wildernature.core.bounty;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.UUID;

public final class BountyEventHandler {
    private BountyEventHandler() {
    }

    public static void onLivingHurt(LivingEntity livingEntity, DamageSource damageSource, float damageAmount) {
        if (livingEntity.level().isClientSide() || damageAmount <= 0.0F) {
            return;
        }

        BountyAssistTracker.recordDamage(livingEntity, damageSource.getEntity());
    }

    public static void onLivingDeath(ServerLevel serverLevel, LivingEntity livingEntity) {
        List<UUID> assistingPlayers = BountyAssistTracker.consumeAssistingPlayers(livingEntity);
        if (assistingPlayers.isEmpty()) {
            return;
        }

        BountyManager.handleEntityKilled(serverLevel, livingEntity, assistingPlayers);
    }
}