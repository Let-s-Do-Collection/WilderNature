package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.RaccoonModel;
import net.satisfy.wildernature.entity.RaccoonEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class RaccoonRenderer extends MobRenderer<RaccoonEntity, RaccoonModel<RaccoonEntity>> {
    private static final ResourceLocation RACCOON_TEXTURE = WilderNatureIdentifier.of("textures/entity/raccoon.png");
    private static final ResourceLocation RACOON_SLEEP_TEXTURE = WilderNatureIdentifier.of("textures/entity/raccoon.png");

    public RaccoonRenderer(EntityRendererProvider.Context context) {
        super(context, new RaccoonModel<>(context.bakeLayer(RaccoonModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    protected void setupRotations(RaccoonEntity livingEntity, PoseStack poseStack, float f, float g, float h, float i) {
        super.setupRotations(livingEntity, poseStack, f, g, h, i);
    }

    public @NotNull ResourceLocation getTextureLocation(RaccoonEntity entity) {
        return entity.isSleeping() ? RACOON_SLEEP_TEXTURE : RACCOON_TEXTURE;
    }

    @Override
    public void render(RaccoonEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
