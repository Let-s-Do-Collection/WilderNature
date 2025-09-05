package net.satisfy.wildernature.neoforge.core.event;

import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.util.Truffling;

@EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT)
public class WilderNatureClientNeoForgeEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Truffling.addTruffledTooltip(itemStack, event.getToolTip());
    }
}
