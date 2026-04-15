package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class DenyParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 5;

    private final float swayPhase;
    private final float swaySpeed;
    private final float swayAmount;
    private final float upwardVelocity;
    private final float rotationSpeed;
    private final float targetAlpha;
    private final float maximumScale;
    private final double startX;
    private final double startY;
    private final double startZ;

    private float previousQuadSize;
    private float growthSpeed;

    protected DenyParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.friction = 0.92F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = 22 + this.random.nextInt(8);
        this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.swaySpeed = 0.22F + this.random.nextFloat() * 0.10F;
        this.swayAmount = 0.025F + this.random.nextFloat() * 0.025F;
        this.upwardVelocity = 0.012F + this.random.nextFloat() * 0.008F;
        this.rotationSpeed = (0.01F + this.random.nextFloat() * 0.015F) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.targetAlpha = 0.95F;
        this.maximumScale = 0.14F + this.random.nextFloat() * 0.02F;
        this.quadSize = 0.06F + this.random.nextFloat() * 0.01F;
        this.previousQuadSize = this.quadSize;
        this.growthSpeed = 0.012F;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 0.35F;
        this.bCol = 0.35F;
        this.startX = x;
        this.startY = y;
        this.startZ = z;
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
        float swayAngle = this.swayPhase + this.age * this.swaySpeed;

        this.x = this.startX + Mth.sin(swayAngle) * this.swayAmount;
        this.z = this.startZ + Mth.cos(swayAngle * 0.8F) * this.swayAmount * 0.6F;
        this.y = this.startY + this.upwardVelocity * this.age;

        this.roll += this.rotationSpeed;

        if (this.quadSize < this.maximumScale) {
            this.quadSize = Math.min(this.maximumScale, this.quadSize + this.growthSpeed);
            this.growthSpeed *= 0.84F;
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

        this.alpha *= 1.0F - progress * 0.15F;
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