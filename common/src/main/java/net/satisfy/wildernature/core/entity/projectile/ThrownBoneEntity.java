package net.satisfy.wildernature.core.entity.projectile;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class ThrownBoneEntity extends ThrowableItemProjectile {
    private static final float DAMPING_FACTOR = 0.35F;

    public ThrownBoneEntity(Level level, LivingEntity owner) {
        super(EntityTypeRegistry.BONE.get(), owner, level);
    }

    public ThrownBoneEntity(EntityType<? extends ThrownBoneEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return Items.BONE;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        return new ClientboundAddEntityPacket(this, serverEntity);
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        Direction direction = hitResult.getDirection();
        double velocityX = this.getDeltaMovement().x;
        double velocityY = this.getDeltaMovement().y;
        double velocityZ = this.getDeltaMovement().z;

        if (direction == Direction.EAST || direction == Direction.WEST) {
            velocityX = -velocityX * DAMPING_FACTOR;
        }
        if (direction == Direction.DOWN || direction == Direction.UP) {
            velocityY = -velocityY * DAMPING_FACTOR;
        }
        if (direction == Direction.NORTH || direction == Direction.SOUTH) {
            velocityZ = -velocityZ * DAMPING_FACTOR;
        }

        this.setDeltaMovement(velocityX, velocityY, velocityZ);

        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOD_HIT, SoundSource.NEUTRAL, 0.9F, 1.2F);
        }
    }

    @Override
    public void shootFromRotation(Entity entity, float xRotation, float yRotation, float angleOffset, float velocity, float inaccuracy) {
        float directionX = -Mth.sin(yRotation * ((float) Math.PI / 180F)) * Mth.cos(xRotation * ((float) Math.PI / 180F));
        float directionY = -Mth.sin((xRotation + angleOffset) * ((float) Math.PI / 180F));
        float directionZ = Mth.cos(yRotation * ((float) Math.PI / 180F)) * Mth.cos(xRotation * ((float) Math.PI / 180F));
        this.shoot(directionX, directionY, directionZ, velocity, inaccuracy);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.85F, 0.9F, 0.85F));
        this.setDeltaMovement(this.getDeltaMovement().add(entity.getDeltaMovement().x, entity.onGround() ? 0.0D : entity.getDeltaMovement().y, entity.getDeltaMovement().z));
    }
}