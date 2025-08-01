package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.TurkeyModel;
import net.satisfy.wildernature.entity.TurkeyEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class TurkeyRenderer extends MobRenderer<TurkeyEntity, TurkeyModel<TurkeyEntity>> {
    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/turkey.png");

    public TurkeyRenderer(EntityRendererProvider.Context context) {
        super(context, new TurkeyModel<>(context.bakeLayer(TurkeyModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(TurkeyEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(TurkeyEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);

    }
}
