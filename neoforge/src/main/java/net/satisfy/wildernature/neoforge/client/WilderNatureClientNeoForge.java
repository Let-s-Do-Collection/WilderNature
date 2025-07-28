package net.satisfy.wildernature.neoforge.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.neoforge.player.layer.WolfFurChestplateLayer;
import net.satisfy.wildernature.neoforge.player.model.WolfFurChestplateModel;

import java.util.function.Function;

@EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class WilderNatureClientNeoForge {

    @SubscribeEvent
    public static void onClientSetup(RegisterEvent event) {
        WilderNatureClient.preInitClient();
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        WilderNatureClient.onInitializeClient();
    }
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(WilderNatureClient.WOLF_FUR_CHESTPLATE_LAYER, WolfFurChestplateModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void constructLayers(EntityRenderersEvent.AddLayers event) {
        addLayerToPlayerSkin(event, "default", WolfFurChestplateLayer::new);
        addLayerToPlayerSkin(event, "slim", WolfFurChestplateLayer::new);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <E extends Player, M extends HumanoidModel<E>>
    void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName, Function<LivingEntityRenderer<E, M>, ? extends RenderLayer<E, M>> factory) {
        LivingEntityRenderer renderer = event.getSkin(PlayerSkin.Model.byName(skinName));
        if (renderer != null) renderer.addLayer(factory.apply(renderer));
    }
}