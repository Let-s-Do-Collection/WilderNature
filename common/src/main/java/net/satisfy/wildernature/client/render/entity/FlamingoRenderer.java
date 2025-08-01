package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.FlamingoModel;
import net.satisfy.wildernature.entity.FlamingoEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class FlamingoRenderer extends MobRenderer<FlamingoEntity, FlamingoModel<FlamingoEntity>> {
    private static final ResourceLocation WHITE_TEXTURE = WilderNatureIdentifier.of("textures/entity/flamingo_white.png");
    private static final ResourceLocation PINK_TEXTURE = WilderNatureIdentifier.of("textures/entity/flamingo_pink.png");
    private static final ResourceLocation RED_TEXTURE = WilderNatureIdentifier.of("textures/entity/flamingo_red.png");

    public FlamingoRenderer(EntityRendererProvider.Context context) {
        super(context, new FlamingoModel<>(context.bakeLayer(FlamingoModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(FlamingoEntity entity) {
        int textureIndex = entity.getId() % 3;
        return switch (textureIndex) {
            case 0 -> WHITE_TEXTURE;
            case 1 -> RED_TEXTURE;
            default -> PINK_TEXTURE;
        };
    }

    @Override
    public void render(FlamingoEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}
