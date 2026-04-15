package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BeaverModel;
import net.satisfy.wildernature.core.entity.animal.passive.BeaverEntity;
import org.jetbrains.annotations.NotNull;

public class BeaverRenderer extends MobRenderer<BeaverEntity, BeaverModel<BeaverEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/beaver.png");
    private static final ResourceLocation SLEEPING_TEXTURE = WilderNature.identifier("textures/entity/beaver_sleep.png");

    public BeaverRenderer(EntityRendererProvider.Context context) {
        super(context, new BeaverModel<>(context.bakeLayer(BeaverModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BeaverEntity entity) {
        return entity.isSleeping() ? SLEEPING_TEXTURE : TEXTURE;
    }

    @Override
    public void render(BeaverEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (entity.isBaby()) {
            poseStack.scale(0.55F, 0.55F, 0.55F);
        }

        if (entity.isSleeping()) {
            poseStack.translate(0.0D, 0.2D, 0.0D);
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}