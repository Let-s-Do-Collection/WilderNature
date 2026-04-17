package net.satisfy.wildernature.fabric.core.player.layer;

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
import net.satisfy.wildernature.core.item.FurCloakItem;
import net.satisfy.wildernature.fabric.api.FurCloakTrinket;
import net.satisfy.wildernature.fabric.core.player.model.WolfFurChestplateModel;
import org.jetbrains.annotations.NotNull;

public class WolfFurChestplateLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    private final WolfFurChestplateModel<T> model;

    public WolfFurChestplateLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
        this.model = new WolfFurChestplateModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(WilderNatureClient.WOLF_FUR_CHESTPLATE_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof Player player)) {
            return;
        }

        ItemStack chestItem = player.getInventory().armor.get(2);
        boolean hasFurCloakTrinket = FurCloakTrinket.isEquippedBy(player);
        boolean chestSlotEmpty = chestItem.isEmpty();
        boolean chestSlotHasFurCloak = chestItem.getItem() instanceof FurCloakItem;
        boolean shouldRender = (hasFurCloakTrinket && chestSlotEmpty) || chestSlotHasFurCloak;

        if (!shouldRender) {
            return;
        }

        this.model.syncToBody(this.getParentModel().body, player.isCrouching());
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        renderColoredCutoutModel(this.model, getTextureLocation(entity), poseStack, multiBufferSource, packedLight, entity, -1);
    }

    @Override
    protected @NotNull ResourceLocation getTextureLocation(T entity) {
        return WolfFurChestplateModel.WOLF_FUR_CHESTPLATE_TEXTURE;
    }
}