package net.satisfy.wildernature.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import net.satisfy.wildernature.fabric.client.renderer.StylinPurpleHatRenderer;
import net.satisfy.wildernature.client.render.entity.FurCloakLayer;

import java.util.List;

public class WilderNatureClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WilderNatureClient.preInitClient();
        WilderNatureClient.onInitializeClient();

        ArmorRenderer.register(new StylinPurpleHatRenderer(), ObjectRegistry.STYLIN_PURPLE_HAT.get());


        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerRenderer renderer) {
                registrationHelper.register(new FurCloakLayer<>(renderer));
            } else if (entityRenderer instanceof ArmorStandRenderer armorStandRenderer) {
                registrationHelper.register(new FurCloakLayer<>(armorStandRenderer));
            }
        });

        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }

    private void onItemTooltip(ItemStack itemStack, Item.TooltipContext var2,  TooltipFlag context, List<Component> tooltip) {
        WilderNatureUtil.Truffling.addTruffledTooltip(itemStack, tooltip);
    }
}
