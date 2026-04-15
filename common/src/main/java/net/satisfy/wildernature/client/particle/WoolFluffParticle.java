package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class WoolFluffParticle extends TextureSheetParticle {
    private static final int FADE_DURATION = 8;

    private final float horizontalSwayPhase;
    private final float horizontalSwaySpeed;
    private final float horizontalSwayAmountX;
    private final float horizontalSwayAmountZ;
    private final float sideVelocityX;
    private final float sideVelocityZ;
    private final float upwardVelocityStart;
    private final float upwardVelocityEnd;
    private final float targetAlpha;
    private final float maximumScale;
    private final float gravityStrength;
    private final float downwardAcceleration;
    private final float glideStrength;

    private float previousQuadSize;
    private float growthSpeed;

    protected WoolFluffParticle(ClientLevel level, double x, double y, double z, MotionProfile motionProfile) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.friction = 0.96F;
        this.gravity = 0.0F;
        this.hasPhysics = true;
        this.lifetime = motionProfile.minimumLifetime + this.random.nextInt(motionProfile.maximumLifetime - motionProfile.minimumLifetime + 1);
        this.horizontalSwayPhase = this.random.nextFloat() * Mth.TWO_PI;
        this.horizontalSwaySpeed = motionProfile.minimumSwaySpeed + this.random.nextFloat() * (motionProfile.maximumSwaySpeed - motionProfile.minimumSwaySpeed);
        this.horizontalSwayAmountX = motionProfile.minimumSwayAmount + this.random.nextFloat() * (motionProfile.maximumSwayAmount - motionProfile.minimumSwayAmount);
        this.horizontalSwayAmountZ = motionProfile.minimumSwayAmount + this.random.nextFloat() * (motionProfile.maximumSwayAmount - motionProfile.minimumSwayAmount);
        this.sideVelocityX = (motionProfile.minimumSideVelocity + this.random.nextFloat() * (motionProfile.maximumSideVelocity - motionProfile.minimumSideVelocity)) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.sideVelocityZ = (motionProfile.minimumSideVelocity + this.random.nextFloat() * (motionProfile.maximumSideVelocity - motionProfile.minimumSideVelocity)) * (this.random.nextBoolean() ? 1.0F : -1.0F);
        this.upwardVelocityStart = motionProfile.minimumUpwardVelocityStart + this.random.nextFloat() * (motionProfile.maximumUpwardVelocityStart - motionProfile.minimumUpwardVelocityStart);
        this.upwardVelocityEnd = motionProfile.minimumUpwardVelocityEnd + this.random.nextFloat() * (motionProfile.maximumUpwardVelocityEnd - motionProfile.minimumUpwardVelocityEnd);
        this.targetAlpha = motionProfile.targetAlpha;
        this.maximumScale = motionProfile.minimumScale + this.random.nextFloat() * (motionProfile.maximumScale - motionProfile.minimumScale);
        this.gravityStrength = motionProfile.minimumGravityStrength + this.random.nextFloat() * (motionProfile.maximumGravityStrength - motionProfile.minimumGravityStrength);
        this.downwardAcceleration = motionProfile.minimumDownwardAcceleration + this.random.nextFloat() * (motionProfile.maximumDownwardAcceleration - motionProfile.minimumDownwardAcceleration);
        this.glideStrength = motionProfile.minimumGlideStrength + this.random.nextFloat() * (motionProfile.maximumGlideStrength - motionProfile.minimumGlideStrength);
        this.quadSize = motionProfile.initialScale + this.random.nextFloat() * motionProfile.initialScaleVariance;
        this.previousQuadSize = this.quadSize;
        this.growthSpeed = motionProfile.growthSpeed;
        this.roll = this.random.nextFloat() * 0.15F - 0.075F;
        this.oRoll = this.roll;
        this.alpha = 0.0F;
        this.rCol = 0.96F + this.random.nextFloat() * 0.03F;
        this.gCol = 0.95F + this.random.nextFloat() * 0.03F;
        this.bCol = 0.93F + this.random.nextFloat() * 0.03F;
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
        float swayAngle = this.horizontalSwayPhase + progress * Mth.TWO_PI * this.horizontalSwaySpeed;
        float upwardVelocity = Mth.lerp(progress, this.upwardVelocityStart, this.upwardVelocityEnd);
        float glideCurve = Mth.sin(progress * (float) Math.PI) * this.glideStrength;
        float verticalVelocity = upwardVelocity - (this.gravityStrength * progress * progress + this.downwardAcceleration * this.age * progress - glideCurve);

        this.xd = this.sideVelocityX + Mth.sin(swayAngle) * this.horizontalSwayAmountX * 0.08F;
        this.zd = this.sideVelocityZ + Mth.cos(swayAngle * 0.85F) * this.horizontalSwayAmountZ * 0.08F;
        this.yd = verticalVelocity * 0.08F;

        this.move(this.xd, this.yd, this.zd);

        if (this.onGround) {
            this.xd *= 0.35D;
            this.zd *= 0.35D;
            this.yd = 0.0D;
        }

        this.roll = Mth.sin(progress * Mth.TWO_PI + this.horizontalSwayPhase) * 0.12F;

        if (this.quadSize < this.maximumScale) {
            this.quadSize = Math.min(this.maximumScale, this.quadSize + this.growthSpeed);
            this.growthSpeed *= 0.9F;
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

    public record MotionProfile(int minimumLifetime, int maximumLifetime, float minimumSwaySpeed, float maximumSwaySpeed, float minimumSwayAmount, float maximumSwayAmount, float minimumUpwardVelocityStart, float maximumUpwardVelocityStart, float minimumUpwardVelocityEnd, float maximumUpwardVelocityEnd, float minimumSideVelocity, float maximumSideVelocity, float targetAlpha, float initialScale, float initialScaleVariance, float minimumScale, float maximumScale, float growthSpeed, float minimumGravityStrength, float maximumGravityStrength, float minimumDownwardAcceleration, float maximumDownwardAcceleration, float minimumGlideStrength, float maximumGlideStrength) {
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private static final MotionProfile MOTION_PROFILE = new MotionProfile(28, 42, 1.1F, 1.8F, 0.045F, 0.085F, 0.05F, 0.085F, -0.02F, 0.005F, 0.01F, 0.03F, 0.92F, 0.09F, 0.03F, 0.11F, 0.18F, 0.012F, 0.45F, 0.72F, 0.012F, 0.024F, 0.08F, 0.14F);
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
            WoolFluffParticle particle = new WoolFluffParticle(level, x, y, z, MOTION_PROFILE);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}