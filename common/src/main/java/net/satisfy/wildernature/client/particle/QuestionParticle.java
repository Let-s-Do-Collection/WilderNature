package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class QuestionParticle extends TextureSheetParticle {
    private static final int FADE_IN_DURATION = 4;
    private static final float START_SCALE = 0.05F;
    private static final float MAX_SCALE = 0.11F;

    private final float targetAlpha;
    private final float hoverOffsetX;
    private final float hoverOffsetZ;
    private float previousQuadSize;

    protected QuestionParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(level, x, y, z, velocityX, velocityY, velocityZ);
        this.lifetime = 18;
        this.gravity = 0.0F;
        this.hasPhysics = false;

        this.hoverOffsetX = (this.random.nextFloat() - 0.5F) * 0.004F;
        this.hoverOffsetZ = (this.random.nextFloat() - 0.5F) * 0.004F;

        this.xd = 0.0D;
        this.yd = 0.006D;
        this.zd = 0.0D;

        this.targetAlpha = 0.95F;
        this.quadSize = START_SCALE;
        this.previousQuadSize = this.quadSize;

        this.alpha = 0.0F;
        this.rCol = 1.0F;
        this.gCol = 0.92F;
        this.bCol = 0.35F;
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

        double horizontalX = this.hoverOffsetX * Mth.sin((float) this.age * 0.35F);
        double horizontalZ = this.hoverOffsetZ * Mth.cos((float) this.age * 0.35F);

        this.move(horizontalX, this.yd, horizontalZ);

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
            QuestionParticle particle = new QuestionParticle(level, x, y, z, velocityX, velocityY, velocityZ);
            particle.pickSprite(this.spriteSet);
            return particle;
        }
    }
}