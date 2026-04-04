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

public class SleepingParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 30;
    private static final float MAXIMUM_ROTATION_OFFSET = 0.22F;

    private final float horizontalPhase;
    private final float verticalSpeed;
    private final float horizontalAmplitude;
    private final float baseRoll;
    private final float targetAlpha;
    private final float maximumScale;
    private float previousQuadSize;
    private float currentGrowthSpeed;

    protected SleepingParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.friction = 0.985F;
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.lifetime = 58 + this.random.nextInt(24);
        this.horizontalPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.verticalSpeed = 0.0065F + this.random.nextFloat() * 0.0035F;
        this.horizontalAmplitude = 0.0025F + this.random.nextFloat() * 0.0025F;
        this.targetAlpha = 0.75F + this.random.nextFloat() * 0.15F;
        this.maximumScale = 0.09F + this.random.nextFloat() * 0.04F;
        this.quadSize = 0.02F + this.random.nextFloat() * 0.01F;
        this.previousQuadSize = this.quadSize;
        this.currentGrowthSpeed = 0.0018F + this.random.nextFloat() * 0.0008F;
        this.baseRoll = (this.random.nextFloat() - 0.5F) * 0.14F;
        this.roll = this.baseRoll;
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.rCol = 0.62F;
        this.gCol = 1.0F;
        this.bCol = 0.68F;
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

        float lifetimeProgress = (float) this.age / (float) this.lifetime;
        float horizontalOffset = Mth.sin(this.horizontalPhase + lifetimeProgress * Mth.TWO_PI * 1.35F) * this.horizontalAmplitude;
        float forwardDrift = 0.0012F + Mth.cos(this.horizontalPhase + lifetimeProgress * Mth.TWO_PI * 0.85F) * 0.0008F;

        this.xd = horizontalOffset;
        this.yd = this.verticalSpeed;
        this.zd = forwardDrift;

        this.move(this.xd, this.yd, this.zd);

        float rotationOffset = Mth.sin(lifetimeProgress * Mth.TWO_PI * 0.9F) * MAXIMUM_ROTATION_OFFSET;
        this.roll = this.baseRoll + rotationOffset;

        if (this.quadSize < this.maximumScale) {
            this.quadSize = Math.min(this.maximumScale, this.quadSize + this.currentGrowthSpeed);
            this.currentGrowthSpeed *= 0.975F;
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
            SleepingParticle particle = new SleepingParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}