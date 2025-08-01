package net.satisfy.wildernature.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.registry.SoundRegistry;
import net.satisfy.wildernature.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BisonTrophyBlock extends WallDecorationBlock {
    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.9375, 1, 0.8125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.15625, 0.125, 0.125, 0.84375, 0.875, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.15625, 0.5625, 0.375, 0.15625, 0.75, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.28125, 0.125, -0.1875, 0.71875, 0.625, 0.125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.84375, 0.5625, 0.375, 1.15625, 0.75, 0.4375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.625, 0.5, 0.1875, 1.25, 0.6875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.8125, 0.625, 0.5, 1, 1.25, 0.6875), BooleanOp.OR);
        return shape;
    };
    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, WilderNatureUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    private final Map<Player, Long> lastUseTime = new HashMap<>();

    public BisonTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (!world.isClientSide) {
            long currentTime = System.currentTimeMillis();
            Long lastUsed = lastUseTime.getOrDefault(player, 0L);
            if (currentTime - lastUsed < 180000) {
                world.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.25f, 0.5f);
                return InteractionResult.FAIL;
            }
            lastUseTime.put(player, currentTime);
            world.playSound(null, pos, SoundRegistry.BISON_ANGRY.get(), SoundSource.BLOCKS, 0.25f, 1.0f);

            ServerLevel serverLevel = (ServerLevel) world;

            for (int radius = 0; radius <= 20; radius++) {
                for (int angle = 0; angle < 360; angle += 10) {
                    double radians = Math.toRadians(angle);
                    double offsetX = radius * Math.cos(radians);
                    double offsetZ = radius * Math.sin(radians);
                    serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5 + offsetX, pos.getY() + 0.5, pos.getZ() + 0.5 + offsetZ, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }

            AABB area = new AABB(pos).inflate(20);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity entity : entities) {
                if (entity != player) {
                    double dx = entity.getX() - pos.getX();
                    double dz = entity.getZ() - pos.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    double strength = 2.0 / (distance + 1.0);

                    entity.knockback(strength, dx, dz);
                    entity.hurt(world.damageSources().hotFloor(), 1.0F);
                }
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
