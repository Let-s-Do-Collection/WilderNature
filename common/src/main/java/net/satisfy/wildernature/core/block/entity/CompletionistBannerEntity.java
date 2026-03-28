package net.satisfy.wildernature.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;

public class CompletionistBannerEntity extends BlockEntity {
    public CompletionistBannerEntity(BlockPos blockPos, BlockState state) {
        super(EntityTypeRegistry.COMPLETIONIST_BANNER_BLOCK_ENTITY.get(), blockPos, state);
    }
}