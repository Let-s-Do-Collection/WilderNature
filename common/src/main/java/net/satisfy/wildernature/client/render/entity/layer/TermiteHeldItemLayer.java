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
import net.satisfy.wildernature.client.model.entity.model.TermiteModel;
import net.satisfy.wildernature.core.entity.animal.passive.TermiteEntity;

@Environment(EnvType.CLIENT)
public class TermiteHeldItemLayer extends RenderLayer<TermiteEntity, TermiteModel<TermiteEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public TermiteHeldItemLayer(RenderLayerParent<TermiteEntity, TermiteModel<TermiteEntity>> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, TermiteEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = entity.getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }

        ModelPart basePart = this.getParentModel().root();
        ModelPart torsoPart = basePart.getChild("torso");

        poseStack.pushPose();
        basePart.translateAndRotate(poseStack);
        torsoPart.translateAndRotate(poseStack);

        poseStack.translate(0.0F, -0.25F, -0.15F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.6F, 0.6F, 0.6F);

        this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.FIXED, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}