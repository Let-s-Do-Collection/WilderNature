package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.DogModel;
import net.satisfy.wildernature.core.entity.animal.DogEntity;
import org.jetbrains.annotations.NotNull;

public class DogRenderer extends MobRenderer<DogEntity, DogModel<DogEntity>> {
    private static final ResourceLocation BROWN = WilderNature.identifier("textures/entity/dog_brown.png");
    private static final ResourceLocation WHITE = WilderNature.identifier("textures/entity/dog_white.png");
    private static final ResourceLocation GRAY = WilderNature.identifier("textures/entity/dog_gray.png");

    public DogRenderer(EntityRendererProvider.Context context) {
        super(context, new DogModel<>(context.bakeLayer(DogModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DogEntity entity) {
        int variant = entity.getId() % 3;

        if (variant == 0) return BROWN;
        if (variant == 1) return WHITE;
        return GRAY;
    }

    @Override
    public void render(DogEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.4f, 0.4f, 0.4f);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}