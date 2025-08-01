package net.satisfy.wildernature.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AreaEffectCloud;
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
import net.satisfy.wildernature.registry.TagsRegistry;
import net.satisfy.wildernature.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RedWolfTrophyBlock extends WallDecorationBlock {
    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.9375, 1, 0.8125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.28125, 0.5625, 0.75, 0.65625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.65625, 0.8125, 0.75, 0.90625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.65625, 0.8125, 0.4375, 0.90625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.28125, 0.375, 0.625, 0.46875, 0.5625), BooleanOp.OR);
        return shape;
    };
    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, WilderNatureUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    public RedWolfTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (!world.isClientSide) {
            Vector3d center = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            double range = 32.0;
            AABB boundingBox = new AABB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range);

            List<BlockPos> blockPositions = BlockPos.betweenClosedStream(boundingBox).map(BlockPos::immutable).toList();
            for (BlockPos blockPos : blockPositions) {
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.is(TagsRegistry.MAKES_BLOCK_GLOW)) {
                    AreaEffectCloud effectCloud = new AreaEffectCloud(world, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
                    effectCloud.setRadius(0.5F);
                    effectCloud.setDuration(600);
                    effectCloud.setWaitTime(0);
                    effectCloud.setParticle(ParticleTypes.GLOW);
                    world.addFreshEntity(effectCloud);
                }
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}

