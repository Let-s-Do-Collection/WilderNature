package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.DeerModel;
import net.satisfy.wildernature.core.entity.animal.DeerEntity;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class DeerRenderer extends MobRenderer<DeerEntity, DeerModel> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/deer.png");
    private static final ResourceLocation WHITE_TEXTURE = WilderNature.identifier("textures/entity/deer_white.png");

    public DeerRenderer(EntityRendererProvider.Context context) {
        super(context, new DeerModel(context.bakeLayer(DeerModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DeerEntity entity) {
        return entity.isWhite() ? WHITE_TEXTURE : TEXTURE;
    }

    @Override
    public void render(DeerEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (entity.isBaby()) {
            poseStack.scale(0.4f, 0.4f, 0.4f);
        }

        if (entity.isSleeping()) {
            poseStack.translate(0.0D, -0.4D, 0.0D);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}