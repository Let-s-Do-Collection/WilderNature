package net.satisfy.wildernature.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.gui.handler.BountyBoardMenu;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class BountyBoardBlockEntity extends BlockEntity implements MenuProvider {
    public static final int WOBBLE_EVENT = 1;
    private long wobbleStart = -1000L;

    public BountyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.BOUNTY_BOARD_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    public @NotNull Component getDisplayName() {
        return this.getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, net.minecraft.world.entity.player.Player player) {
        BountyBoardMenu menu = new BountyBoardMenu(containerId, inventory);
        menu.setBoardPos(this.worldPosition);
        return menu;
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == WOBBLE_EVENT) {
            if (this.level != null) {
                this.wobbleStart = this.level.getGameTime();
            }
            return true;
        }
        return super.triggerEvent(id, type);
    }

    public long getWobbleStart() {
        return this.wobbleStart;
    }
}