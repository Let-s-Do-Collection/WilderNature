package net.satisfy.wildernature.fabric.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.fabric.client.renderer.StylinPurpleHatRenderer;
import net.satisfy.wildernature.fabric.player.layer.WolfFurChestplateLayer;
import net.satisfy.wildernature.fabric.player.model.WolfFurChestplateModel;
import net.satisfy.wildernature.registry.ObjectRegistry;
import net.satisfy.wildernature.util.Truffling;

import java.util.List;

public class WilderNatureClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WilderNatureClient.preInitClient();
        WilderNatureClient.onInitializeClient();

        ArmorRenderer.register(new StylinPurpleHatRenderer(), ObjectRegistry.STYLIN_PURPLE_HAT.get());

        EntityModelLayerRegistry.registerModelLayer(WilderNatureClient.WOLF_FUR_CHESTPLATE_LAYER, WolfFurChestplateModel::createBodyLayer);

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityRenderer instanceof PlayerRenderer renderer) {
                registrationHelper.register(new WolfFurChestplateLayer<>(renderer));
            }
        });

        ItemTooltipCallback.EVENT.register(this::onItemTooltip);
    }

    private void onItemTooltip(ItemStack itemStack, Item.TooltipContext var2,  TooltipFlag context, List<Component> tooltip) {
        Truffling.addTruffledTooltip(itemStack, tooltip);
    }
}
