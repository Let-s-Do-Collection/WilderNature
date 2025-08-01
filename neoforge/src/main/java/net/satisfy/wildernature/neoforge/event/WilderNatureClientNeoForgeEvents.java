package net.satisfy.wildernature.neoforge.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.util.Truffling;

@EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class WilderNatureClientNeoForgeEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Truffling.addTruffledTooltip(itemStack, event.getToolTip());
    }
}
