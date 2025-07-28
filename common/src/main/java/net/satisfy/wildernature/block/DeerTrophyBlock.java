package net.satisfy.wildernature.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.satisfy.wildernature.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DeerTrophyBlock extends WallDecorationBlock {
    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.9375, 1, 0.8125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.3125, 0.5, 0.6875, 0.625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.3125, 0.625, 0.5625, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.4375, 0.8125, 0.3125, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.4375, 0.8125, 0.875, 0.625, 0.875), BooleanOp.OR);
        return shape;
    };
    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, WilderNatureUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    public DeerTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide) {
            Vector3d center = new Vector3d(pos.getX(), pos.getY(), pos.getZ());
            double range = 32.0;
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, new AABB(center.x - range, center.y - range, center.z - range, center.x + range, center.y + range, center.z + range));
            for (LivingEntity entity : entities) {
                entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 600, 0));
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
