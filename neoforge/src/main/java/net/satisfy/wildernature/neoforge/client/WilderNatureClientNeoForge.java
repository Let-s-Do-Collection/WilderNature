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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.client.gui.screen.BountyBoardScreen;
import net.satisfy.wildernature.client.particle.*;
import net.satisfy.wildernature.core.registry.MenuTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.neoforge.client.extensions.WilderNatureHatExtensions;
import net.satisfy.wildernature.neoforge.core.player.layer.WolfFurChestplateLayer;
import net.satisfy.wildernature.neoforge.core.player.model.WolfFurChestplateModel;

import java.util.function.Function;

@EventBusSubscriber(modid = WilderNature.MOD_ID, value = Dist.CLIENT)
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
    public static void clientSetup(RegisterMenuScreensEvent event) {
        event.register(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), BountyBoardScreen::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleTypeRegistry.SLEEPING.get(), SleepingParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.QUESTION.get(), QuestionParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.ALERT.get(), AlertParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.DENY.get(), DenyParticle.Provider::new);
        event.registerSpriteSet(ParticleTypeRegistry.TRUST_POSITIVE.get(), FloatingFeedbackParticle.TrustPositiveProvider::new);
        event.registerSpriteSet(ParticleTypeRegistry.TRUST_NEGATIVE.get(), FloatingFeedbackParticle.TrustNegativeProvider::new);
        event.registerSpriteSet(ParticleTypeRegistry.LOVE.get(), FloatingFeedbackParticle.LoveProvider::new);
        event.registerSpriteSet(ParticleTypeRegistry.CACHE_OPEN.get(), CacheLeafParticle.OpenProvider::new);
        event.registerSpriteSet(ParticleTypeRegistry.CACHE_CLOSE.get(), CacheLeafParticle.CloseProvider::new);
        event.registerSpriteSet(ParticleTypeRegistry.SHEARED_WOOL.get(), WoolFluffParticle.Provider::new);
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

    private static <E extends Player, M extends HumanoidModel<E>>
    void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName, Function<LivingEntityRenderer<E, M>, ? extends RenderLayer<E, M>> factory) {
        LivingEntityRenderer<E, M> renderer = event.getSkin(PlayerSkin.Model.byName(skinName));
        if (renderer != null) {
            renderer.addLayer(factory.apply(renderer));
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new WilderNatureHatExtensions(), ObjectRegistry.STYLIN_PURPLE_HAT.get());
    }
}