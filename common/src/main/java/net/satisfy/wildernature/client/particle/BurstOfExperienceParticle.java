package net.satisfy.wildernature.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BurstOfExperienceParticle extends TextureSheetParticle {
    private final SpriteSet spriteSet;
    private final float initialScale;
    private final float peakAlpha;

    protected BurstOfExperienceParticle(ClientLevel level, double positionX, double positionY, double positionZ, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
        super(level, positionX, positionY, positionZ, velocityX, velocityY, velocityZ);
        this.spriteSet = spriteSet;
        this.friction = 0.9F;
        this.gravity = -0.01F;
        this.initialScale = 0.08F + this.random.nextFloat() * 0.05F;
        this.quadSize = this.initialScale;
        this.lifetime = 14 + this.random.nextInt(8);
        this.xd = velocityX + (this.random.nextDouble() - 0.5D) * 0.045D;
        this.yd = Math.max(0.025D, velocityY + this.random.nextDouble() * 0.06D);
        this.zd = velocityZ + (this.random.nextDouble() - 0.5D) * 0.045D;
        this.rCol = 0.98F;
        this.gCol = 0.90F + this.random.nextFloat() * 0.06F;
        this.bCol = 0.42F + this.random.nextFloat() * 0.08F;
        this.peakAlpha = 0.78F + this.random.nextFloat() * 0.18F;
        this.alpha = 0.0F;
        this.hasPhysics = false;
        this.pickSprite(this.spriteSet);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.removed) {
            return;
        }

        float ageProgress = (float) this.age / (float) this.lifetime;

        if (ageProgress < 0.14F) {
            this.alpha = ageProgress / 0.14F * this.peakAlpha;
        } else if (ageProgress < 0.58F) {
            this.alpha = this.peakAlpha;
        } else {
            this.alpha = Math.max(0.0F, (1.0F - ageProgress) / 0.42F * this.peakAlpha);
        }

        if (ageProgress < 0.28F) {
            this.quadSize = this.initialScale * (0.82F + ageProgress * 1.15F);
        } else {
            this.quadSize = this.initialScale * (1.14F - (ageProgress - 0.28F) * 0.34F);
        }

        this.setSpriteFromAge(this.spriteSet);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    public net.minecraft.client.particle.@NotNull ParticleRenderType getRenderType() {
        return net.minecraft.client.particle.ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double positionX, double positionY, double positionZ, double velocityX, double velocityY, double velocityZ) {
            return new BurstOfExperienceParticle(clientLevel, positionX, positionY, positionZ, velocityX, velocityY, velocityZ, this.spriteSet);
        }
    }
}