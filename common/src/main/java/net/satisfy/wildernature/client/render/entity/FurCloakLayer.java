package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.client.model.armor.FurCloakModel;
import net.satisfy.wildernature.core.item.FurCloakItem;
import org.jetbrains.annotations.NotNull;

public class FurCloakLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final FurCloakModel<T> model;

    public FurCloakLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
        this.model = new FurCloakModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(FurCloakModel.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int packedLight, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!FurCloakItem.isVisibleOn(entity)) {
            return;
        }

        this.model.syncToBody(this.getParentModel().body, entity.isCrouching());
        this.model.setupCape(entity, partialTicks);
        renderColoredCutoutModel(this.model, getTextureLocation(entity), poseStack, multiBufferSource, packedLight, entity, -1);
    }

    @Override
    protected @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return FurCloakModel.TEXTURE;
    }
}
