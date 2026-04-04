package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.SwiftFoxModel;
import net.satisfy.wildernature.client.render.entity.layer.SwiftFoxHeldItemLayer;
import net.satisfy.wildernature.core.entity.animal.SwiftFoxEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SwiftFoxRenderer extends MobRenderer<SwiftFoxEntity, SwiftFoxModel<SwiftFoxEntity>> {
    private static final ResourceLocation DEFAULT_TEXTURE = WilderNature.identifier("textures/entity/swift_fox.png");
    private static final ResourceLocation SLEEPING_TEXTURE = WilderNature.identifier("textures/entity/swift_fox_sleeping.png");

    public SwiftFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new SwiftFoxModel<>(context.bakeLayer(SwiftFoxModel.LAYER_LOCATION)), 0.7F);
        ItemInHandRenderer itemInHandRenderer = context.getItemInHandRenderer();
        this.addLayer(new SwiftFoxHeldItemLayer(this, itemInHandRenderer));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SwiftFoxEntity entity) {
        return entity.isSleeping() ? SLEEPING_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    public void render(SwiftFoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (entity.isBaby()) {
            poseStack.scale(0.4F, 0.4F, 0.4F);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}