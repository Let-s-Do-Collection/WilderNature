package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.entity.TermiteMoundBlockEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TermiteMoundBlock extends BaseEntityBlock {
    public TermiteMoundBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TermiteMoundBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TermiteMoundBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, EntityTypeRegistry.TERMITE_MOUND_BLOCK_ENTITY.get(), (lvl, pos, blockState, be) -> TermiteMoundBlockEntity.tick((ServerLevel) lvl, pos, be));
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}