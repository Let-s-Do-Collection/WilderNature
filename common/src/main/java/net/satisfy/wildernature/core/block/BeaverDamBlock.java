package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

public class BeaverDamBlock extends Block implements SimpleWaterloggedBlock {
    public static final MapCodec<BeaverDamBlock> CODEC = simpleCodec(BeaverDamBlock::new);
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public BeaverDamBlock() {
        this(BlockBehaviour.Properties.of().strength(0.3F).sound(SoundType.WOOD).noOcclusion().pushReaction(PushReaction.DESTROY));
    }

    public BeaverDamBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(WATERLOGGED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BeaverDamBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!PlatformHelper.isAnimationsEnabled()) {
            return;
        }
        if (state.getValue(WATERLOGGED)) {
            if (random.nextInt(4) == 0) {
                level.addParticle(ParticleTypes.BUBBLE, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble() * 0.6D, pos.getZ() + random.nextDouble(), 0.0D, 0.04D, 0.0D);
            }
            if (random.nextInt(6) == 0 && !level.getFluidState(pos.above()).is(FluidTags.WATER)) {
                level.addParticle(ParticleTypes.SPLASH, pos.getX() + random.nextDouble(), pos.getY() + 1.0D, pos.getZ() + random.nextDouble(), 0.0D, 0.0D, 0.0D);
            }
            return;
        }
        if (random.nextInt(5) != 0) {
            return;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                double x = pos.getX() + 0.5D + direction.getStepX() * 0.5D;
                double z = pos.getZ() + 0.5D + direction.getStepZ() * 0.5D;
                level.addParticle(ParticleTypes.SPLASH, x + (random.nextDouble() - 0.5D) * 0.6D, pos.getY() + 0.3D + random.nextDouble() * 0.4D, z + (random.nextDouble() - 0.5D) * 0.6D, 0.0D, 0.0D, 0.0D);
                return;
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        entity.makeStuckInBlock(state, new Vec3(0.85D, 0.85D, 0.85D));
    }
}