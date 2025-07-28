package net.satisfy.wildernature.forge.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.util.Truffling;

@Mod.EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class WilderNatureClientForgeEvents {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        Truffling.addTruffledTooltip(itemStack, event.getToolTip());
    }
}
