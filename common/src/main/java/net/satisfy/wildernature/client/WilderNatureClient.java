package net.satisfy.wildernature.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.armor.StylinPurpleHatModel;
import net.satisfy.wildernature.client.model.block.BountyBoardModel;
import net.satisfy.wildernature.client.model.entity.BisonModel;
import net.satisfy.wildernature.client.model.entity.BoarModel;
import net.satisfy.wildernature.client.model.entity.CassowaryModel;
import net.satisfy.wildernature.client.model.entity.DeerModel;
import net.satisfy.wildernature.client.model.entity.DogModel;
import net.satisfy.wildernature.client.model.entity.FlamingoModel;
import net.satisfy.wildernature.client.model.entity.HedgehogModel;
import net.satisfy.wildernature.client.model.entity.MiniSheepModel;
import net.satisfy.wildernature.client.model.entity.OwlModel;
import net.satisfy.wildernature.client.model.entity.PelicanModel;
import net.satisfy.wildernature.client.model.entity.PenguinModel;
import net.satisfy.wildernature.client.model.entity.RaccoonModel;
import net.satisfy.wildernature.client.model.entity.RedWolfModel;
import net.satisfy.wildernature.client.model.entity.SquirrelModel;
import net.satisfy.wildernature.client.model.entity.TurkeyModel;
import net.satisfy.wildernature.client.particle.AlertParticle;
import net.satisfy.wildernature.client.particle.QuestionParticle;
import net.satisfy.wildernature.client.particle.SleepingParticle;
import net.satisfy.wildernature.client.render.block.BountyBoardRenderer;
import net.satisfy.wildernature.client.render.block.CompletionistBannerRenderer;
import net.satisfy.wildernature.client.render.entity.BisonRenderer;
import net.satisfy.wildernature.client.render.entity.BoarRenderer;
import net.satisfy.wildernature.client.render.entity.CassowaryRenderer;
import net.satisfy.wildernature.client.render.entity.DeerRenderer;
import net.satisfy.wildernature.client.render.entity.DogRenderer;
import net.satisfy.wildernature.client.render.entity.FlamingoRenderer;
import net.satisfy.wildernature.client.render.entity.HedgehogRenderer;
import net.satisfy.wildernature.client.render.entity.MiniSheepRenderer;
import net.satisfy.wildernature.client.render.entity.OwlRenderer;
import net.satisfy.wildernature.client.render.entity.PelicanRenderer;
import net.satisfy.wildernature.client.render.entity.PenguinRenderer;
import net.satisfy.wildernature.client.render.entity.RaccoonRenderer;
import net.satisfy.wildernature.client.render.entity.RedWolfRenderer;
import net.satisfy.wildernature.client.render.entity.SquirrelRenderer;
import net.satisfy.wildernature.client.render.entity.TurkeyRenderer;
import net.satisfy.wildernature.client.util.WilderNatureClientUtil;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;

import static net.satisfy.wildernature.client.util.WilderNatureClientUtil.makeHorn;
import static net.satisfy.wildernature.core.registry.EntityTypeRegistry.*;
import static net.satisfy.wildernature.core.registry.EntityTypeRegistry.TURKEY_EGG;
import static net.satisfy.wildernature.core.registry.ObjectRegistry.*;

@Environment(EnvType.CLIENT)
public class WilderNatureClient {
    public static final ModelLayerLocation WOLF_FUR_CHESTPLATE_LAYER = new ModelLayerLocation(ResourceLocation.parse("minecraft:player"), "wolf_fur_chestplate");

    public static void onInitializeClient() {
        RenderTypeRegistry.register(RenderType.cutout(), DEER_TROPHY.get(), HAZELNUT_BUSH.get(), BOUNTY_BOARD.get());

        BlockEntityRendererRegistry.register(COMPLETIONIST_BANNER_BLOCK_ENTITY.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(BOUNTY_BOARD_BLOCK_ENTITY.get(), BountyBoardRenderer::new);

        ParticleProviderRegistry.register(ParticleTypeRegistry.SLEEPING.get(), SleepingParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.ALERT.get(), AlertParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.QUESTION.get(), QuestionParticle.Provider::new);

        makeHorn(ObjectRegistry.BISON_HORN.get());
    }

    public static void preInitClient() {
        registerEntityRenderers();
        registerEntityModelLayer();
        WilderNatureClientUtil.init();
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(BISON, BisonRenderer::new);
        EntityRendererRegistry.register(BOAR, BoarRenderer::new);
        EntityRendererRegistry.register(CASSOWARY, CassowaryRenderer::new);
        EntityRendererRegistry.register(DEER, DeerRenderer::new);
        EntityRendererRegistry.register(DOG, DogRenderer::new);
        EntityRendererRegistry.register(FLAMINGO, FlamingoRenderer::new);
        EntityRendererRegistry.register(HEDGEHOG, HedgehogRenderer::new);
        EntityRendererRegistry.register(MINISHEEP, MiniSheepRenderer::new);
        EntityRendererRegistry.register(OWL, OwlRenderer::new);
        EntityRendererRegistry.register(PELICAN, PelicanRenderer::new);
        EntityRendererRegistry.register(PENGUIN, PenguinRenderer::new);
        EntityRendererRegistry.register(RACCOON, RaccoonRenderer::new);
        EntityRendererRegistry.register(RED_WOLF, RedWolfRenderer::new);
        EntityRendererRegistry.register(SQUIRREL, SquirrelRenderer::new);
        EntityRendererRegistry.register(TURKEY, TurkeyRenderer::new);
        EntityRendererRegistry.register(BULLET, ThrownItemRenderer::new);
        EntityRendererRegistry.register(TURKEY_EGG, ThrownItemRenderer::new);
    }

    public static void registerEntityModelLayer() {
        EntityModelLayerRegistry.register(StylinPurpleHatModel.LAYER_LOCATION, StylinPurpleHatModel::createBodyLayer);
        EntityModelLayerRegistry.register(BisonModel.LAYER_LOCATION, BisonModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BoarModel.LAYER_LOCATION, BoarModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BountyBoardModel.LAYER_LOCATION, BountyBoardModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CassowaryModel.LAYER_LOCATION, CassowaryModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::createBodyLayer);
        EntityModelLayerRegistry.register(DeerModel.LAYER_LOCATION, DeerModel::getTexturedModelData);
        EntityModelLayerRegistry.register(DogModel.LAYER_LOCATION, DogModel::getTexturedModelData);
        EntityModelLayerRegistry.register(FlamingoModel.LAYER_LOCATION, FlamingoModel::getTexturedModelData);
        EntityModelLayerRegistry.register(HedgehogModel.LAYER_LOCATION, HedgehogModel::getTexturedModelData);
        EntityModelLayerRegistry.register(MiniSheepModel.LAYER_LOCATION, MiniSheepModel::getTexturedModelData);
        EntityModelLayerRegistry.register(OwlModel.LAYER_LOCATION, OwlModel::getTexturedModelData);
        EntityModelLayerRegistry.register(PelicanModel.LAYER_LOCATION, PelicanModel::getTexturedModelData);
        EntityModelLayerRegistry.register(PenguinModel.LAYER_LOCATION, PenguinModel::getTexturedModelData);
        EntityModelLayerRegistry.register(RaccoonModel.LAYER_LOCATION, RaccoonModel::getTexturedModelData);
        EntityModelLayerRegistry.register(RedWolfModel.LAYER_LOCATION, RedWolfModel::getTexturedModelData);
        EntityModelLayerRegistry.register(SquirrelModel.LAYER_LOCATION, SquirrelModel::getTexturedModelData);
        EntityModelLayerRegistry.register(TurkeyModel.LAYER_LOCATION, TurkeyModel::getTexturedModelData);
    }
}