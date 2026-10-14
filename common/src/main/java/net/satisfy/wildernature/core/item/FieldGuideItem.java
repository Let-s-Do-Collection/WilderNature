package net.satisfy.wildernature.core.item;

import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.fieldguide.FieldGuideDataLoader;
import net.satisfy.wildernature.core.fieldguide.FieldGuideEntry;
import net.satisfy.wildernature.core.gui.handler.FieldGuideMenu;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FieldGuideItem extends Item {
    public FieldGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        level.playSound(player, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 1.0f);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            List<FieldGuideEntry> entries = FieldGuideDataLoader.INSTANCE.getEntries();
            MenuRegistry.openExtendedMenu(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, menuPlayer) -> new FieldGuideMenu(containerId, entries),
                    Component.empty()
            ), friendlyByteBuf -> friendlyByteBuf.writeCollection(entries, (buffer, entry) -> entry.write(buffer)));
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

}