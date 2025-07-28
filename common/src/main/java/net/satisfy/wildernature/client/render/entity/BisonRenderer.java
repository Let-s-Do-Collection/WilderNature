package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.BisonModel;
import net.satisfy.wildernature.entity.BisonEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;


public class BisonRenderer extends MobRenderer<BisonEntity, BisonModel<BisonEntity>> {

    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/bison.png");

    public BisonRenderer(EntityRendererProvider.Context context) {
        super(context, new BisonModel<>(context.bakeLayer(BisonModel.LAYER_LOCATION)), 0.9f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BisonEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BisonEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.5f, 0.5f, 0.5f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
