package net.satisfy.wildernature.core.entity.projectile;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.satisfy.wildernature.core.entity.animal.TurkeyEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class ThrownTurkeyEgg extends ThrowableItemProjectile {
    private static final EntityDimensions ZERO_SIZED_DIMENSIONS = EntityDimensions.fixed(0.0F, 0.0F);

    public ThrownTurkeyEgg(EntityType<? extends ThrownTurkeyEgg> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownTurkeyEgg(Level level, LivingEntity shooter) {
        super(EntityTypeRegistry.TURKEY_EGG.get(), shooter, level);
    }

    public ThrownTurkeyEgg(Level level, double x, double y, double z) {
        super(EntityTypeRegistry.TURKEY_EGG.get(), x, y, z, level);
    }

    public void handleEntityEvent(byte id) {
        if (id == 3) {

            for(int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08);
            }
        }

    }

    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        result.getEntity().hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
    }

    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            if (this.random.nextInt(8) == 0) {
                int count = 1;
                if (this.random.nextInt(32) == 0) {
                    count = 4;
                }

                ServerLevel serverLevel = (ServerLevel) this.level();

                for (int index = 0; index < count; ++index) {
                    TurkeyEntity turkey = EntityTypeRegistry.TURKEY.get().create(serverLevel);
                    if (turkey != null) {
                        turkey.setAge(-24000);
                        turkey.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                        if (!turkey.fudgePositionAfterSizeChange(ZERO_SIZED_DIMENSIONS)) {
                            break;
                        }
                        serverLevel.addFreshEntity(turkey);
                    }
                }
            }

            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    protected @NotNull Item getDefaultItem() {
        return ObjectRegistry.TURKEY_EGG.get();
    }
}
