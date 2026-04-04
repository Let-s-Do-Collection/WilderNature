package net.satisfy.wildernature.client.render.entity.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.client.model.entity.model.DogModel;
import net.satisfy.wildernature.core.entity.animal.tameable.DogEntity;

public class DogHeldItemLayer extends RenderLayer<DogEntity, DogModel<DogEntity>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public DogHeldItemLayer(RenderLayerParent<DogEntity, DogModel<DogEntity>> renderer, ItemInHandRenderer itemInHandRenderer) {
        super(renderer);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, DogEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack itemStack = this.getRenderStack(entity);
        if (itemStack.isEmpty()) {
            return;
        }

        ModelPart dogPart = this.getParentModel().root();
        ModelPart headPart = dogPart.getChild("head");
        ModelPart mouthPart = headPart.getChild("mouth");

        poseStack.pushPose();
        dogPart.translateAndRotate(poseStack);
        headPart.translateAndRotate(poseStack);
        mouthPart.translateAndRotate(poseStack);

        if (entity.isBaby()) {
            poseStack.scale(0.85F, 0.85F, 0.85F);
        }

        poseStack.translate(0.2F, 0.15F, -0.1F);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
        poseStack.scale(0.65F, 0.65F, 0.65F);

        this.itemInHandRenderer.renderItem(entity, itemStack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false, poseStack, buffer, packedLight);
        poseStack.popPose();
    }

    private ItemStack getRenderStack(DogEntity entity) {
        return entity.getDisplayedItem();
    }
}