package net.satisfy.wildernature.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.client.model.entity.model.HippoModel;
import net.satisfy.wildernature.core.entity.animal.defensive.HippoEntity;

public class HippoTossedItemLayer extends RenderLayer<HippoEntity, EntityModel<HippoEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public HippoTossedItemLayer(RenderLayerParent<HippoEntity, EntityModel<HippoEntity>> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, HippoEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = entity.getDisplayedItem();
        if (itemStack.isEmpty() || !entity.isTossFeeding()) return;

        EntityModel<HippoEntity> parentModel = this.getParentModel();
        if (!(parentModel instanceof HippoModel<HippoEntity> hippoModel)) return;

        float tossProgress = entity.getTossFeedProgress(partialTick);
        float arcHeight = Mth.sin(tossProgress * (float) Math.PI) * 0.85F;
        float forwardOffset = tossProgress < 0.5F ? Mth.lerp(tossProgress / 0.5F, -0.25F, -1.2F) : Mth.lerp((tossProgress - 0.5F) / 0.5F, -1.2F, -0.2F);
        float verticalOffset = Mth.lerp(tossProgress, 0.2F, 0.05F) + arcHeight;

        ModelPart bodyPart = hippoModel.getRenderBody();
        ModelPart headPart = hippoModel.getRenderHead();
        ModelPart jawPart = hippoModel.getRenderJaw();

        poseStack.pushPose();
        bodyPart.translateAndRotate(poseStack);
        headPart.translateAndRotate(poseStack);
        jawPart.translateAndRotate(poseStack);
        poseStack.translate(0.0F, verticalOffset, forwardOffset);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F - tossProgress * 60.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(tossProgress * 180.0F));
        poseStack.scale(0.85F, 0.85F, 0.85F);
        this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}