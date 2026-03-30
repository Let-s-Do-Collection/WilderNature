package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HollowCacheBlock extends BaseEntityBlock {
    public static final MapCodec<HollowCacheBlock> CODEC = simpleCodec(HollowCacheBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 12.0D, 14.0D);

    public HollowCacheBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new HollowCacheBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return createTickerHelper(blockEntityType, EntityTypeRegistry.HOLLOW_CACHE_BLOCK_ENTITY.get(), HollowCacheBlockEntity::clientTick);
        }
        return createTickerHelper(blockEntityType, EntityTypeRegistry.HOLLOW_CACHE_BLOCK_ENTITY.get(), HollowCacheBlockEntity::serverTick);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        boolean isOpen = blockState.getValue(OPEN);
        setOpen(blockState, level, blockPos, !isOpen);

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
            player.openMenu(hollowCacheBlockEntity);
            player.awardStat(Stats.OPEN_CHEST);
        }

        if (!isOpen) {
            level.gameEvent(player, GameEvent.CONTAINER_OPEN, blockPos);
            spawnCacheOpenParticles((ServerLevel) level, blockPos);
        } else {
            level.gameEvent(player, GameEvent.CONTAINER_CLOSE, blockPos);
            if (blockEntity instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
                hollowCacheBlockEntity.scheduleCloseParticles();
            }
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newBlockState, boolean movedByPiston) {
        if (!blockState.is(newBlockState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
                Containers.dropContents(level, blockPos, hollowCacheBlockEntity);
                level.updateNeighbourForOutputSignal(blockPos, this);
            }
        }

        super.onRemove(blockState, level, blockPos, newBlockState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(blockPos));
    }

    public static void setOpen(BlockState blockState, Level level, BlockPos blockPos, boolean open) {
        level.setBlock(blockPos, blockState.setValue(OPEN, open), 3);
    }

    public static void spawnCacheOpenParticles(ServerLevel level, BlockPos blockPos) {
        for (int particleIndex = 0; particleIndex < 12; particleIndex++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 0.7D;
            double offsetY = level.random.nextDouble() * 0.25D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 0.7D;
            level.sendParticles(ParticleTypeRegistry.CACHE_OPEN.get(), blockPos.getX() + 0.5D + offsetX, blockPos.getY() + 0.45D + offsetY, blockPos.getZ() + 0.5D + offsetZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void spawnCacheCloseParticles(ServerLevel level, BlockPos blockPos) {
        for (int particleIndex = 0; particleIndex < 10; particleIndex++) {
            double offsetX = (level.random.nextDouble() - 0.5D) * 0.7D;
            double offsetY = level.random.nextDouble() * 0.0D;
            double offsetZ = (level.random.nextDouble() - 0.5D) * 0.7D;
            level.sendParticles(ParticleTypeRegistry.CACHE_CLOSE.get(), blockPos.getX() + 0.5D + offsetX, blockPos.getY() + offsetY, blockPos.getZ() + 0.5D + offsetZ, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }
}