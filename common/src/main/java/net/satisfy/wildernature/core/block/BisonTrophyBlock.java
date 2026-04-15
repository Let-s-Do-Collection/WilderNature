package net.satisfy.wildernature.core.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
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
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult blockHitResult) {
        if (!world.isClientSide) {
            long currentTime = System.currentTimeMillis();
            Long lastUsed = lastUseTime.getOrDefault(player, 0L);
            if (currentTime - lastUsed < 180000) {
                world.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 0.25F, 0.5F);
                return InteractionResult.FAIL;
            }

            lastUseTime.put(player, currentTime);
            world.playSound(null, pos, SoundEventRegistry.BISON_ANGRY.get(), SoundSource.BLOCKS, 0.4F, 0.9F);

            ServerLevel serverLevel = (ServerLevel) world;
            Direction facing = state.getValue(FACING);
            double pushX = facing.getStepX();
            double pushZ = facing.getStepZ();
            double baseX = pos.getX() + 0.5D + pushX * 0.35D;
            double baseZ = pos.getZ() + 0.5D + pushZ * 0.35D;

            for (int particleIndex = 0; particleIndex < 18; particleIndex++) {
                double distance = 0.35D + particleIndex * 0.22D;
                double spreadX = facing.getAxis() == Direction.Axis.Z ? (serverLevel.random.nextDouble() - 0.5D) * 0.8D : (serverLevel.random.nextDouble() - 0.5D) * 0.18D;
                double spreadZ = facing.getAxis() == Direction.Axis.X ? (serverLevel.random.nextDouble() - 0.5D) * 0.8D : (serverLevel.random.nextDouble() - 0.5D) * 0.18D;
                double particleX = baseX + pushX * distance + spreadX;
                double particleZ = baseZ + pushZ * distance + spreadZ;
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COARSE_DIRT.defaultBlockState()), particleX, pos.getY() + 0.15D, particleZ, 2, 0.08D, 0.04D, 0.08D, 0.01D);
            }

            AABB area = new AABB(pos).inflate(6.0D);
            List<LivingEntity> entities = world.getEntitiesOfClass(LivingEntity.class, area);

            for (LivingEntity entity : entities) {
                if (entity == player) {
                    continue;
                }

                double deltaX = entity.getX() - (pos.getX() + 0.5D);
                double deltaZ = entity.getZ() - (pos.getZ() + 0.5D);
                double forwardAlignment = deltaX * pushX + deltaZ * pushZ;
                if (forwardAlignment <= -1.0D) {
                    continue;
                }

                double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
                if (distance > 6.0D) {
                    continue;
                }

                double strength = Math.max(0.12D, 0.55D - distance * 0.06D);
                entity.push(pushX * strength, 0.08D, pushZ * strength);
                entity.hurtMarked = true;
            }

            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 60, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
        }

        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}