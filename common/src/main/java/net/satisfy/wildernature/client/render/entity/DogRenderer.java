package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.DogModel;
import net.satisfy.wildernature.entity.DogEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class DogRenderer extends MobRenderer<DogEntity, DogModel<DogEntity>> {
    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/dog.png");

    public DogRenderer(EntityRendererProvider.Context context) {
        super(context, new DogModel<>(context.bakeLayer(DogModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DogEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(DogEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
