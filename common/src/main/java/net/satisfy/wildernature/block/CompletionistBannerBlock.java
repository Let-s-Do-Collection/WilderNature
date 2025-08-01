package net.satisfy.wildernature.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.block.entity.CompletionistBannerEntity;
import net.satisfy.wildernature.registry.ObjectRegistry;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class CompletionistBannerBlock extends BaseEntityBlock {
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    public CompletionistBannerBlock(Properties properties) {
        super(properties);
        makeDefaultState();
    }
    public static final MapCodec<CompletionistBannerBlock> CODEC = simpleCodec(CompletionistBannerBlock::new);
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new CompletionistBannerEntity(blockPos, blockState);
    }

    protected void makeDefaultState() {
        registerDefaultState(this.stateDefinition.any().setValue(ROTATION, 0));
    }

    @Override
    public boolean canSurvive(@NotNull BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockState belowBlockState = levelReader.getBlockState(blockPos.below());
        return belowBlockState.isSolid();
    }

    @Override
    public boolean isPossibleToRespawnInThis(BlockState blockState) {
        return true;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        if (clickedFace == Direction.UP || clickedFace == Direction.DOWN) {
            return this.defaultBlockState().setValue(ROTATION, Mth.floor((double) ((180.0f + context.getRotation()) * 16.0f / 360.0f) + 0.5) & 0xF);
        } else {
            if (this == ObjectRegistry.WOLF_TRAPPER_BANNER.get()) {
                return ObjectRegistry.WOLF_TRAPPER_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
            } else if (this == ObjectRegistry.BUNNY_STALKER_BANNER.get()) {
                return ObjectRegistry.BUNNY_STALKER_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
            } else if (this == ObjectRegistry.COD_CATCHER_BANNER.get()) {
                return ObjectRegistry.COD_CATCHER_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
            } else {
                return ObjectRegistry.WOLF_TRAPPER_WALL_BANNER.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, clickedFace.getOpposite());
            }
        }
    }

    @Override
    public @NotNull BlockState rotate(BlockState blockState, Rotation rotation) {
        return blockState.setValue(ROTATION, rotation.rotate(blockState.getValue(ROTATION), 16));
    }

    @Override
    public @NotNull BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.setValue(ROTATION, mirror.mirror(blockState.getValue(ROTATION), 16));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ROTATION);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
        if (direction == Direction.DOWN && !blockState.canSurvive(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
    }

    public ResourceLocation getRenderTexture() {
        if (this == ObjectRegistry.WOLF_TRAPPER_BANNER.get()) {
            return WilderNatureIdentifier.of("textures/banner/wolf_trapper.png");
        } else if (this == ObjectRegistry.BUNNY_STALKER_BANNER.get()) {
            return WilderNatureIdentifier.of("textures/banner/rabbit_hunter.png");
        } else if (this == ObjectRegistry.BUNNY_STALKER_WALL_BANNER.get()) {
            return WilderNatureIdentifier.of("textures/banner/rabbit_hunter.png");
        } else if (this == ObjectRegistry.COD_CATCHER_BANNER.get()) {
            return WilderNatureIdentifier.of("textures/banner/cod_catcher.png");
        } else if (this == ObjectRegistry.COD_CATCHER_WALL_BANNER.get()) {
            return WilderNatureIdentifier.of("textures/banner/cod_catcher.png");
        }

        return WilderNatureIdentifier.of("textures/banner/wolf_trapper.png");
    }
}
