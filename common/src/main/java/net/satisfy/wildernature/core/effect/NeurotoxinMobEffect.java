package net.satisfy.wildernature.core.effect;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;

public class NeurotoxinMobEffect extends MobEffect {
    public NeurotoxinMobEffect() {
        super(MobEffectCategory.HARMFUL, 0x2F8F2F);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = Math.max(10, 40 - amplifier * 10);
        return duration % interval == 0;
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        float damageAmount = 1.0F + amplifier * 0.5F;
        livingEntity.hurt(livingEntity.damageSources().magic(), damageAmount);

        double movementMultiplier = Math.max(0.6D, 1.0D - (0.08D * (amplifier + 1)));
        livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().multiply(movementMultiplier, 1.0D, movementMultiplier));

        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            double mouthY = livingEntity.getY() + livingEntity.getEyeHeight() * 0.9D;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GREEN_WOOL.defaultBlockState()), livingEntity.getX(), mouthY, livingEntity.getZ(), 6, 0.15D, 0.05D, 0.15D, 0.02D);
        }

        if (amplifier >= 1 && livingEntity.onGround() && livingEntity.level().random.nextFloat() < 0.18F + amplifier * 0.08F) {
            float yawOffset = livingEntity.level().random.nextBoolean() ? 35.0F + livingEntity.level().random.nextFloat() * 25.0F : -(35.0F + livingEntity.level().random.nextFloat() * 25.0F);
            float newYaw = livingEntity.getYRot() + yawOffset;
            livingEntity.setYRot(newYaw);
            livingEntity.setYBodyRot(newYaw);
            livingEntity.setYHeadRot(newYaw);
        }

        if (amplifier >= 2 && livingEntity.level().random.nextFloat() < 0.2F) {
            double randomX = (livingEntity.level().random.nextDouble() - 0.5D) * 0.4D;
            double randomZ = (livingEntity.level().random.nextDouble() - 0.5D) * 0.4D;
            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().add(randomX, 0.0D, randomZ));
        }

        return true;
    }
}