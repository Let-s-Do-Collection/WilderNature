package net.satisfy.wildernature.client;

import dev.architectury.registry.client.level.entity.EntityModelLayerRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.particle.ParticleProviderRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import dev.architectury.registry.menu.MenuRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.satisfy.wildernature.client.gui.screen.BountyBoardScreen;
import net.satisfy.wildernature.client.gui.screen.FieldGuideScreen;
import net.satisfy.wildernature.client.model.armor.StylinPurpleHatModel;
import net.satisfy.wildernature.client.model.block.BountyBoardModel;
import net.satisfy.wildernature.client.model.block.GlowingBlockModel;
import net.satisfy.wildernature.client.model.block.HollowCacheModel;
import net.satisfy.wildernature.client.model.entity.model.*;
import net.satisfy.wildernature.client.particle.*;
import net.satisfy.wildernature.client.render.block.BountyBoardRenderer;
import net.satisfy.wildernature.client.render.block.CompletionistBannerRenderer;
import net.satisfy.wildernature.client.render.block.HollowCacheRenderer;
import net.satisfy.wildernature.client.render.entity.*;
import net.satisfy.wildernature.client.util.WilderNatureClientUtil;
import net.satisfy.wildernature.core.registry.MenuTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;

import static net.satisfy.wildernature.client.util.WilderNatureClientUtil.makeHorn;
import static net.satisfy.wildernature.core.registry.EntityTypeRegistry.TURKEY_EGG;
import static net.satisfy.wildernature.core.registry.EntityTypeRegistry.*;
import static net.satisfy.wildernature.core.registry.ObjectRegistry.*;

@Environment(EnvType.CLIENT)
public class WilderNatureClient {
    public static final ModelLayerLocation WOLF_FUR_CHESTPLATE_LAYER = new ModelLayerLocation(ResourceLocation.parse("minecraft:player"), "wolf_fur_chestplate");

    public static void onInitializeClient() {
        RenderTypeRegistry.register(RenderType.cutout(), DEER_TROPHY.get(), HAZELNUT_BUSH.get(), BOUNTY_BOARD.get(), BURROW.get());

        BlockEntityRendererRegistry.register(COMPLETIONIST_BANNER_BLOCK_ENTITY.get(), CompletionistBannerRenderer::new);
        BlockEntityRendererRegistry.register(BOUNTY_BOARD_BLOCK_ENTITY.get(), BountyBoardRenderer::new);
        BlockEntityRendererRegistry.register(HOLLOW_CACHE_BLOCK_ENTITY.get(), HollowCacheRenderer::new);

        ColorHandlerRegistry.registerBlockColors((blockState, blockAndTintGetter, blockPos, tintIndex) -> {
            if (blockAndTintGetter != null && blockPos != null) {
                return BiomeColors.getAverageGrassColor(blockAndTintGetter, blockPos);
            }
            return GrassColor.getDefaultColor();
        }, BURROW.get());

        ColorHandlerRegistry.registerItemColors((itemStack, tintIndex) -> GrassColor.getDefaultColor(), BURROW.get());

        ParticleProviderRegistry.register(ParticleTypeRegistry.SLEEPING.get(), SleepingParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.ALERT.get(), AlertParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.QUESTION.get(), QuestionParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.DENY.get(), DenyParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.TRUST_POSITIVE.get(), FloatingFeedbackParticle.TrustPositiveProvider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.TRUST_NEGATIVE.get(), FloatingFeedbackParticle.TrustNegativeProvider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.LOVE.get(), FloatingFeedbackParticle.LoveProvider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.CACHE_OPEN.get(), CacheLeafParticle.OpenProvider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.CACHE_CLOSE.get(), CacheLeafParticle.CloseProvider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.SHEARED_WOOL.get(), WoolFluffParticle.Provider::new);
        ParticleProviderRegistry.register(ParticleTypeRegistry.BURST_OF_EXPERIENCE.get(), BurstOfExperienceParticle.Provider::new);

        MenuRegistry.registerScreenFactory(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), BountyBoardScreen::new);
        MenuRegistry.registerScreenFactory(MenuTypeRegistry.FIELD_GUIDE_MENU.get(), FieldGuideScreen::new);

        makeHorn(ObjectRegistry.BISON_HORN.get());
    }

    public static void preInitClient() {
        registerEntityRenderers();
        registerEntityModelLayer();
        WilderNatureClientUtil.init();
    }

    public static void registerEntityRenderers() {
        EntityRendererRegistry.register(GLOWING_BLOCK, GlowingBlockRenderer::new);
        EntityRendererRegistry.register(ELEPHANT, ElephantRenderer::new);
        EntityRendererRegistry.register(HIPPO, HippoRenderer::new);
        EntityRendererRegistry.register(GIRAFFE, GiraffeRenderer::new);
        EntityRendererRegistry.register(BISON, BisonRenderer::new);
        EntityRendererRegistry.register(BOAR, BoarRenderer::new);
        EntityRendererRegistry.register(CASSOWARY, CassowaryRenderer::new);
        EntityRendererRegistry.register(DEER, DeerRenderer::new);
        EntityRendererRegistry.register(DOG, DogRenderer::new);
        EntityRendererRegistry.register(HEDGEHOG, HedgehogRenderer::new);
        EntityRendererRegistry.register(MINISHEEP, MiniSheepRenderer::new);
        EntityRendererRegistry.register(RACCOON, RaccoonRenderer::new);
        EntityRendererRegistry.register(SWIFT_FOX, SwiftFoxRenderer::new);
        EntityRendererRegistry.register(SQUIRREL, SquirrelRenderer::new);
        EntityRendererRegistry.register(TURKEY, TurkeyRenderer::new);
        EntityRendererRegistry.register(BEAVER, BeaverRenderer::new);
        EntityRendererRegistry.register(TERMITE, TermiteRenderer::new);
        EntityRendererRegistry.register(SCORPION, ScorpionRenderer::new);
        EntityRendererRegistry.register(LION, LionRenderer::new);
        EntityRendererRegistry.register(BULLET, ThrownItemRenderer::new);
        EntityRendererRegistry.register(TURKEY_EGG, ThrownItemRenderer::new);
        EntityRendererRegistry.register(BONE, ThrownItemRenderer::new);
    }

    public static void registerEntityModelLayer() {
        EntityModelLayerRegistry.register(StylinPurpleHatModel.LAYER_LOCATION, StylinPurpleHatModel::getTexturedModelData);
        EntityModelLayerRegistry.register(TermiteModel.LAYER_LOCATION, TermiteModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BabyLionModel.LAYER_LOCATION, BabyLionModel::getTexturedModelData);
        EntityModelLayerRegistry.register(LionModel.LAYER_LOCATION, LionModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BeaverModel.LAYER_LOCATION, BeaverModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BabyElephantModel.LAYER_LOCATION, BabyElephantModel::getTexturedModelData);
        EntityModelLayerRegistry.register(ElephantModel.LAYER_LOCATION, ElephantModel::getTexturedModelData);
        EntityModelLayerRegistry.register(HippoModel.LAYER_LOCATION, HippoModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BabyHippoModel.LAYER_LOCATION, BabyHippoModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BabyGiraffeModel.LAYER_LOCATION, BabyGiraffeModel::getTexturedModelData);
        EntityModelLayerRegistry.register(GiraffeModel.LAYER_LOCATION, GiraffeModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BisonModel.LAYER_LOCATION, BisonModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BoarModel.LAYER_LOCATION, BoarModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BountyBoardModel.LAYER_LOCATION, BountyBoardModel::getTexturedModelData);
        EntityModelLayerRegistry.register(BabyCassowaryModel.LAYER_LOCATION, BabyCassowaryModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CassowaryModel.LAYER_LOCATION, CassowaryModel::getTexturedModelData);
        EntityModelLayerRegistry.register(CompletionistBannerRenderer.LAYER_LOCATION, CompletionistBannerRenderer::getTexturedModelData);
        EntityModelLayerRegistry.register(DeerModel.LAYER_LOCATION, DeerModel::getTexturedModelData);
        EntityModelLayerRegistry.register(DogModel.LAYER_LOCATION, DogModel::getTexturedModelData);
        EntityModelLayerRegistry.register(HedgehogModel.LAYER_LOCATION, HedgehogModel::getTexturedModelData);
        EntityModelLayerRegistry.register(MiniSheepModel.LAYER_LOCATION, MiniSheepModel::getTexturedModelData);
        EntityModelLayerRegistry.register(RaccoonModel.LAYER_LOCATION, RaccoonModel::getTexturedModelData);
        EntityModelLayerRegistry.register(SwiftFoxModel.LAYER_LOCATION, SwiftFoxModel::getTexturedModelData);
        EntityModelLayerRegistry.register(SquirrelModel.LAYER_LOCATION, SquirrelModel::getTexturedModelData);
        EntityModelLayerRegistry.register(TurkeyModel.LAYER_LOCATION, TurkeyModel::getTexturedModelData);
        EntityModelLayerRegistry.register(ScorpionModel.LAYER_LOCATION, ScorpionModel::getTexturedModelData);
        EntityModelLayerRegistry.register(HollowCacheModel.LAYER_LOCATION, HollowCacheModel::getTexturedModelData);
        EntityModelLayerRegistry.register(GlowingBlockModel.LAYER_LOCATION, GlowingBlockModel::getTexturedModelData);
    }
}