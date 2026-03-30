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
import net.satisfy.wildernature.client.model.entity.SquirrelModel;
import net.satisfy.wildernature.core.entity.SquirrelEntity;

@Environment(EnvType.CLIENT)
public class SquirrelHeldItemLayer extends RenderLayer<SquirrelEntity, SquirrelModel> {
    private final ItemInHandRenderer itemInHandRenderer;

    public SquirrelHeldItemLayer(RenderLayerParent<SquirrelEntity, SquirrelModel> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, SquirrelEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }

        ModelPart rootPart = this.getParentModel().root();
        ModelPart hipsPart = rootPart.getChild("hips");
        ModelPart torsoPart = hipsPart.getChild("torso");
        ModelPart headPart = torsoPart.getChild("h_head");

        poseStack.pushPose();
        rootPart.translateAndRotate(poseStack);
        hipsPart.translateAndRotate(poseStack);
        torsoPart.translateAndRotate(poseStack);
        headPart.translateAndRotate(poseStack);

        if (entity.isBaby()) {
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }

        poseStack.translate(0.0F, 0.1F, -0.45F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.ZN.rotationDegrees(33.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(0.4F, 0.4F, 0.4F);

        this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}