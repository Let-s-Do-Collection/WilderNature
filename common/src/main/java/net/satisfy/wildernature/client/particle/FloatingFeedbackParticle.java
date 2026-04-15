package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class FloatingFeedbackParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 6;

    private final float swayPhase;
    private final float swaySpeed;
    private final float swayAmountX;
    private final float swayAmountZ;
    private final float upwardSpeed;
    private final float rotationSpeed;
    private final float targetAlpha;
    private final float maximumScale;
    private final double startX;
    private final double startY;
    private final double startZ;

    private float previousQuadSize;
    private float growthSpeed;

    protected FloatingFeedbackParticle(ClientLevel level, double x, double y, double z, MotionProfile motionProfile) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.lifetime = motionProfile.minimumLifetime + this.random.nextInt(motionProfile.maximumLifetime - motionProfile.minimumLifetime + 1);
        this.swayPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.swaySpeed = motionProfile.minimumSwaySpeed + this.random.nextFloat() * (motionProfile.maximumSwaySpeed - motionProfile.minimumSwaySpeed);
        this.swayAmountX = motionProfile.minimumSwayAmount + this.random.nextFloat() * (motionProfile.maximumSwayAmount - motionProfile.minimumSwayAmount);
        this.swayAmountZ = motionProfile.minimumSwayAmount + this.random.nextFloat() * (motionProfile.maximumSwayAmount - motionProfile.minimumSwayAmount);
        this.upwardSpeed = motionProfile.minimumUpwardSpeed + this.random.nextFloat() * (motionProfile.maximumUpwardSpeed - motionProfile.minimumUpwardSpeed);
        this.rotationSpeed = (motionProfile.minimumRotationSpeed + this.random.nextFloat() * (motionProfile.maximumRotationSpeed - motionProfile.minimumRotationSpeed)) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.targetAlpha = motionProfile.targetAlpha;
        this.maximumScale = motionProfile.minimumScale + this.random.nextFloat() * (motionProfile.maximumScale - motionProfile.minimumScale);
        this.quadSize = motionProfile.initialScale + this.random.nextFloat() * motionProfile.initialScaleVariance;
        this.previousQuadSize = this.quadSize;
        this.growthSpeed = motionProfile.growthSpeed;
        this.roll = this.random.nextFloat() * Mth.TWO_PI;
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
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
        float swayAngle = this.swayPhase + progress * Mth.TWO_PI * this.swaySpeed;

        this.x = this.startX + Mth.sin(swayAngle) * this.swayAmountX;
        this.z = this.startZ + Mth.cos(swayAngle * 0.85F) * this.swayAmountZ;
        this.y = this.startY + this.upwardSpeed * this.age;

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

    public static record MotionProfile(int minimumLifetime, int maximumLifetime, float minimumSwaySpeed, float maximumSwaySpeed, float minimumSwayAmount, float maximumSwayAmount, float minimumUpwardSpeed, float maximumUpwardSpeed, float minimumRotationSpeed, float maximumRotationSpeed, float targetAlpha, float initialScale, float initialScaleVariance, float minimumScale, float maximumScale, float growthSpeed) {
    }

    public static class TrustPositiveProvider implements ParticleProvider<SimpleParticleType> {
        private static final MotionProfile MOTION_PROFILE = new MotionProfile(18, 24, 0.8F, 1.1F, 0.035F, 0.06F, 0.022F, 0.03F, 0.015F, 0.025F, 0.95F, 0.11F, 0.02F, 0.14F, 0.18F, 0.018F);
        private final SpriteSet spriteSet;

        public TrustPositiveProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            FloatingFeedbackParticle particle = new FloatingFeedbackParticle(level, x, y, z, MOTION_PROFILE);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

    public static class TrustNegativeProvider implements ParticleProvider<SimpleParticleType> {
        private static final MotionProfile MOTION_PROFILE = new MotionProfile(16, 22, 1.0F, 1.35F, 0.045F, 0.075F, 0.018F, 0.025F, 0.02F, 0.035F, 0.92F, 0.105F, 0.02F, 0.135F, 0.17F, 0.018F);
        private final SpriteSet spriteSet;

        public TrustNegativeProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            FloatingFeedbackParticle particle = new FloatingFeedbackParticle(level, x, y, z, MOTION_PROFILE);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }

    public static class LoveProvider implements ParticleProvider<SimpleParticleType> {
        private static final MotionProfile MOTION_PROFILE = new MotionProfile(22, 30, 0.55F, 0.8F, 0.03F, 0.05F, 0.016F, 0.022F, 0.01F, 0.018F, 0.98F, 0.12F, 0.025F, 0.16F, 0.2F, 0.016F);
        private final SpriteSet spriteSet;

        public LoveProvider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            FloatingFeedbackParticle particle = new FloatingFeedbackParticle(level, x, y, z, MOTION_PROFILE);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}