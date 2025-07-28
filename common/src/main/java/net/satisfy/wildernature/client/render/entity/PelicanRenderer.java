package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.PelicanModel;
import net.satisfy.wildernature.entity.PelicanEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class PelicanRenderer extends MobRenderer<PelicanEntity, PelicanModel<PelicanEntity>> {
    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/pelican.png");

    public PelicanRenderer(EntityRendererProvider.Context context) {
        super(context, new PelicanModel<>(context.bakeLayer(PelicanModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(PelicanEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(PelicanEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}

