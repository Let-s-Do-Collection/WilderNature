package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.BoarModel;
import net.satisfy.wildernature.entity.BoarEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class BoarRenderer extends MobRenderer<BoarEntity, BoarModel<BoarEntity>> {
    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/boar.png");

    public BoarRenderer(EntityRendererProvider.Context context) {
        super(context, new BoarModel<>(context.bakeLayer(BoarModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BoarEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BoarEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
