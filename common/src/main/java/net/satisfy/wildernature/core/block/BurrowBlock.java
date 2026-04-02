package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.wildernature.core.block.entity.BurrowBlockEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BurrowBlock extends BaseEntityBlock {
    public static final MapCodec<BurrowBlock> CODEC = simpleCodec(BurrowBlock::new);

    public BurrowBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any());
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }


    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new BurrowBlockEntity(blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return createTickerHelper(blockEntityType, EntityTypeRegistry.BURROW_BLOCK_ENTITY.get(), BurrowBlockEntity::clientTick);
        }
        return createTickerHelper(blockEntityType, EntityTypeRegistry.BURROW_BLOCK_ENTITY.get(), BurrowBlockEntity::serverTick);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof BurrowBlockEntity burrowBlockEntity) {
            player.openMenu(burrowBlockEntity);
            player.awardStat(Stats.OPEN_CHEST);
            level.playSound(null, blockPos, SoundEvents.GRASS_BREAK, SoundSource.BLOCKS, 0.6F, 0.85F);
            level.gameEvent(player, GameEvent.CONTAINER_OPEN, blockPos);
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState blockState, Level level, BlockPos blockPos, BlockState newBlockState, boolean movedByPiston) {
        if (!blockState.is(newBlockState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity instanceof BurrowBlockEntity burrowBlockEntity) {
                Containers.dropContents(level, blockPos, burrowBlockEntity);
                level.updateNeighbourForOutputSignal(blockPos, this);

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.playSound(null, blockPos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.55F, 0.8F);
                }
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
}