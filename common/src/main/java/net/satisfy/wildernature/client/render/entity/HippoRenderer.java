package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BabyHippoModel;
import net.satisfy.wildernature.client.model.entity.model.HippoModel;
import net.satisfy.wildernature.client.render.entity.layer.HippoTossedItemLayer;
import net.satisfy.wildernature.core.entity.animal.defensive.HippoEntity;
import org.jetbrains.annotations.NotNull;

public class HippoRenderer extends MobRenderer<HippoEntity, EntityModel<HippoEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/hippo.png");
    private static final ResourceLocation BABY_TEXTURE = WilderNature.identifier("textures/entity/baby_hippo.png");

    private final HippoModel<HippoEntity> adultModel;
    private final BabyHippoModel<HippoEntity> babyModel;

    @SuppressWarnings("unused")
    public HippoRenderer(EntityRendererProvider.Context context) {
        super(context, new HippoModel<>(context.bakeLayer(HippoModel.LAYER_LOCATION)), 1.1F);
        this.adultModel = new HippoModel<>(context.bakeLayer(HippoModel.LAYER_LOCATION));
        this.babyModel = new BabyHippoModel<>(context.bakeLayer(BabyHippoModel.LAYER_LOCATION));
        this.model = this.adultModel;
        this.addLayer(new HippoTossedItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(HippoEntity entity) {
        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }

    @Override
    public void render(HippoEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = entity.isBaby() ? this.babyModel : this.adultModel;

        poseStack.pushPose();

        if (entity.isInWaterOrBubble() && !entity.isBaby()) {
            poseStack.translate(0.0D, -0.3D, 0.0D);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}