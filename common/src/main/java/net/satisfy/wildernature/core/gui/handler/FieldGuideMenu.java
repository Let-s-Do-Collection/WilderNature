package net.satisfy.wildernature.core.gui.handler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.core.fieldguide.FieldGuideEntry;
import net.satisfy.wildernature.core.registry.MenuTypeRegistry;

import java.util.List;

public class FieldGuideMenu extends AbstractContainerMenu {
    private final List<FieldGuideEntry> entries;

    public FieldGuideMenu(int containerId, FriendlyByteBuf friendlyByteBuf) {
        super(MenuTypeRegistry.FIELD_GUIDE_MENU.get(), containerId);
        this.entries = friendlyByteBuf.readList(FieldGuideEntry::read);
    }

    public FieldGuideMenu(int containerId, List<FieldGuideEntry> entries) {
        super(MenuTypeRegistry.FIELD_GUIDE_MENU.get(), containerId);
        this.entries = List.copyOf(entries);
    }

    public List<FieldGuideEntry> getEntries() {
        return this.entries;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}