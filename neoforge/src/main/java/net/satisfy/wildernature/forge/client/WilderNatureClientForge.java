package net.satisfy.wildernature.forge.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.forge.player.layer.WolfFurChestplateLayer;
import net.satisfy.wildernature.forge.player.model.WolfFurChestplateModel;

import java.util.function.Function;

@Mod.EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WilderNatureClientForge {

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
        LivingEntityRenderer renderer = event.getSkin(skinName);
        if (renderer != null) renderer.addLayer(factory.apply(renderer));
    }
}