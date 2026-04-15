package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class AlertParticle extends TextureSheetParticle {
    private static final int FADE_IN_DURATION = 3;
    private static final float START_SCALE = 0.06F;
    private static final float MAX_SCALE = 0.14F;

    private final float targetAlpha;
    private float previousQuadSize;

    protected AlertParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.lifetime = 14;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        this.xd = 0.0D;
        this.yd = 0.01D;
        this.zd = 0.0D;

        this.targetAlpha = 1.0F;
        this.quadSize = START_SCALE;
        this.previousQuadSize = this.quadSize;

        this.alpha = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 0.25F;
        this.bCol = 0.25F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        this.previousQuadSize = this.quadSize;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);

        if (this.age <= FADE_IN_DURATION) {
            float progress = (float) this.age / (float) FADE_IN_DURATION;
            this.alpha = Mth.lerp(progress, 0.0F, this.targetAlpha);
            this.quadSize = Mth.lerp(progress, START_SCALE, MAX_SCALE);
        } else {
            int remainingLifetime = this.lifetime - this.age;
            float fadeProgress = (float) remainingLifetime / (float) (this.lifetime - FADE_IN_DURATION);
            this.alpha = this.targetAlpha * fadeProgress;
            this.quadSize = MAX_SCALE;
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
            AlertParticle particle = new AlertParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}