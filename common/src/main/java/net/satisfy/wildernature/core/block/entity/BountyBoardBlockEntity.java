package net.satisfy.wildernature.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;

public class BountyBoardBlockEntity extends BlockEntity {
    public BountyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.BOUNTY_BOARD_ENTITY.get(), pos, state);
    }
}
