package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.GiraffeModel;
import net.satisfy.wildernature.core.entity.animal.neutral.GiraffeEntity;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class GiraffeRenderer extends MobRenderer<GiraffeEntity, GiraffeModel<GiraffeEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/giraffe.png");
    private static final ResourceLocation COWSTICKER_TEXTURE = WilderNature.identifier("textures/entity/giraffe_cowsticker.png");

    public GiraffeRenderer(EntityRendererProvider.Context context) {
        super(context, new GiraffeModel<>(context.bakeLayer(GiraffeModel.LAYER_LOCATION)), 1.0f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(GiraffeEntity entity) {
        if (entity.hasCustomName()) {
            Component nameComponent = entity.getCustomName();
            if (nameComponent != null && "cowsticker".equalsIgnoreCase(nameComponent.getString())) {
                return COWSTICKER_TEXTURE;
            }
        }

        if (entity.isBaby()) {
            return WilderNature.identifier("textures/entity/baby_giraffe.png");
        }

        return TEXTURE;
    }

    @Override
    public void render(GiraffeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (!entity.isBaby()) {
            poseStack.translate(0.0D, 1.4D, 0.0D);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}