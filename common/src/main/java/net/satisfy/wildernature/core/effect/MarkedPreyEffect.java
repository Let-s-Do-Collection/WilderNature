package net.satisfy.wildernature.core.effect;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class MarkedPreyEffect extends MobEffect {

    public MarkedPreyEffect() {
        super(MobEffectCategory.NEUTRAL, 0x6B3A2A, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x6B3A2A));
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            double dx = entity.getX() - entity.xOld;
            double dy = entity.getY() - entity.yOld;
            double dz = entity.getZ() - entity.zOld;
            boolean isMoving = (dx * dx + dy * dy + dz * dz) > 0.0001;

            if (isMoving) {
                serverLevel.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x6B3A2A), entity.getX(), entity.getY() + 0.1, entity.getZ(), 12, 0.2, 0.05, 0.2, 0.02
                );
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}