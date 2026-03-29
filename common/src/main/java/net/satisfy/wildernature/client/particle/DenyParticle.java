package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class DenyParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 6;

    private final float orbitPhase;
    private final float orbitSpeed;
    private final float orbitRadius;
    private final float verticalDrift;
    private final float rotationSpeed;
    private final float targetAlpha;
    private final float maximumScale;
    private final double centerX;
    private final double centerY;
    private final double centerZ;

    private float previousQuadSize;
    private float growthSpeed;

    protected DenyParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 16 + this.random.nextInt(8);
        this.orbitPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.orbitSpeed = 0.16F + this.random.nextFloat() * 0.08F;
        this.orbitRadius = 0.16F + this.random.nextFloat() * 0.10F;
        this.verticalDrift = 0.0008F + this.random.nextFloat() * 0.0012F;
        this.rotationSpeed = (0.03F + this.random.nextFloat() * 0.03F) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.targetAlpha = 0.9F;
        this.maximumScale = 0.16F + this.random.nextFloat() * 0.03F;
        this.quadSize = 0.04F + this.random.nextFloat() * 0.015F;
        this.previousQuadSize = this.quadSize;
        this.growthSpeed = 0.02F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 0.35F;
        this.bCol = 0.35F;
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.oRoll = this.roll;
        this.previousQuadSize = this.quadSize;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float progress = (float) this.age / (float) this.lifetime;
        float angle = this.orbitPhase + progress * Mth.TWO_PI * this.orbitSpeed * 6.0F;

        this.x = this.centerX + Mth.cos(angle) * this.orbitRadius;
        this.z = this.centerZ + Mth.sin(angle) * this.orbitRadius;
        this.y = this.centerY + this.verticalDrift * this.age;

        this.roll += this.rotationSpeed;

        if (this.quadSize < this.maximumScale) {
            this.quadSize = Math.min(this.maximumScale, this.quadSize + this.growthSpeed);
            this.growthSpeed *= 0.82F;
        }

        if (this.age < FADE_DURATION) {
            this.alpha = Mth.lerp((float) this.age / (float) FADE_DURATION, 0.0F, this.targetAlpha);
        } else {
            int remainingLifetime = this.lifetime - this.age;
            if (remainingLifetime < FADE_DURATION) {
                this.alpha = this.targetAlpha * ((float) remainingLifetime / (float) FADE_DURATION);
            } else {
                this.alpha = this.targetAlpha;
            }
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        return Mth.lerp(partialTick, this.previousQuadSize, this.quadSize);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            DenyParticle particle = new DenyParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}