package net.satisfy.wildernature.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.client.model.entity.model.SwiftFoxModel;
import net.satisfy.wildernature.core.entity.animal.neutral.SwiftFoxEntity;

@Environment(EnvType.CLIENT)
public class SwiftFoxHeldItemLayer extends RenderLayer<SwiftFoxEntity, SwiftFoxModel<SwiftFoxEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public SwiftFoxHeldItemLayer(RenderLayerParent<SwiftFoxEntity, SwiftFoxModel<SwiftFoxEntity>> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SwiftFoxEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }

        ModelPart rootPart = this.getParentModel().root();
        ModelPart swiftFoxPart = rootPart.getChild("swift_fox");
        ModelPart bodyPart = swiftFoxPart.getChild("body");
        ModelPart headPart = bodyPart.getChild("head");

        poseStack.pushPose();
        swiftFoxPart.translateAndRotate(poseStack);
        bodyPart.translateAndRotate(poseStack);
        headPart.translateAndRotate(poseStack);

        if (entity.isBaby()) {
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }

        poseStack.translate(0.05F, 0.15F, -0.45F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(33.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.4F, 0.4F, 0.4F);

        this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}