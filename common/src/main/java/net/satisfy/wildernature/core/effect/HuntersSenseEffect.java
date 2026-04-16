package net.satisfy.wildernature.core.effect;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.core.registry.MobEffectRegistry;

public class HuntersSenseEffect extends MobEffect {
    private static final float DAMAGE_BONUS = 0.25f;

    public HuntersSenseEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513, ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, 0x8B4513));
    }

    @Override
    public void onMobHurt(LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker) {
            if (attacker.hasEffect(MobEffectRegistry.huntersSenseHolder()) &&
                    entity.hasEffect(MobEffectRegistry.markedPreyHolder())) {
                entity.hurt(source, amount * DAMAGE_BONUS);
            }
        }
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}