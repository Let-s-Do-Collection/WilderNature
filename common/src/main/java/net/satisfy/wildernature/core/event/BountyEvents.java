package net.satisfy.wildernature.core.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.core.bounty.BountyEventHandler;

public final class BountyEvents {
    public static void init() {
        EntityEvent.LIVING_HURT.register(BountyEvents::onLivingHurt);
        EntityEvent.LIVING_DEATH.register(BountyEvents::onLivingDeath);
    }

    private static EventResult onLivingHurt(LivingEntity livingEntity, DamageSource damageSource, float damageAmount) {
        if (!livingEntity.level().isClientSide() && damageAmount > 0.0F) {
            BountyEventHandler.onLivingHurt(livingEntity, damageSource, damageAmount);
        }

        return EventResult.pass();
    }

    private static EventResult onLivingDeath(LivingEntity livingEntity, DamageSource damageSource) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            BountyEventHandler.onLivingDeath(serverLevel, livingEntity);
        }

        return EventResult.pass();
    }

    private BountyEvents() {
    }
}