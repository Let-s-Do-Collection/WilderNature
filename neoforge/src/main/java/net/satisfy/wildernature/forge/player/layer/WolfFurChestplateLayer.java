package net.satisfy.wildernature.forge.player.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.client.WilderNatureClient;
import net.satisfy.wildernature.forge.player.model.WolfFurChestplateModel;
import org.jetbrains.annotations.NotNull;

public class WolfFurChestplateLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

    private final WolfFurChestplateModel<T> model;

    public WolfFurChestplateLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
        this.model = new WolfFurChestplateModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(WilderNatureClient.WOLF_FUR_CHESTPLATE_LAYER));
    }

    @Override
    public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource multiBufferSource, int i, @NotNull T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean shouldRender = false;

        for (ItemStack stack : ((Player) entity).getInventory().armor) {
            if (stack.getItem().getDescriptionId().toLowerCase().contains("fur")) {
                shouldRender = true;
            }
        }

        if (shouldRender) {
            this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            poseStack.pushPose();
            poseStack.translate(0.0d, 0.0d, 0.0d);
            renderColoredCutoutModel(this.model, getTextureLocation(entity), poseStack, multiBufferSource, i, entity, 1.0f, 1.0f, 1.0f);
            poseStack.popPose();
        }
    }

    @Override
    protected @NotNull ResourceLocation getTextureLocation(@NotNull T entity) {
        return WolfFurChestplateModel.WOLF_FUR_CHESTPLATE_TEXTURE;
    }
}
