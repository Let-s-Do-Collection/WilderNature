package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.DogModel;
import net.satisfy.wildernature.client.render.entity.layer.DogHeldItemLayer;
import net.satisfy.wildernature.core.entity.animal.tameable.DogEntity;
import org.jetbrains.annotations.NotNull;

public class DogRenderer extends MobRenderer<DogEntity, DogModel<DogEntity>> {
    private static final ResourceLocation BROWN = WilderNature.identifier("textures/entity/dog_brown.png");
    private static final ResourceLocation WHITE = WilderNature.identifier("textures/entity/dog_white.png");
    private static final ResourceLocation GRAY = WilderNature.identifier("textures/entity/dog_gray.png");

    private static final ResourceLocation BROWN_SLEEP = WilderNature.identifier("textures/entity/dog_brown_sleep.png");
    private static final ResourceLocation WHITE_SLEEP = WilderNature.identifier("textures/entity/dog_white_sleep.png");
    private static final ResourceLocation GRAY_SLEEP = WilderNature.identifier("textures/entity/dog_gray_sleep.png");

    public DogRenderer(EntityRendererProvider.Context context) {
        super(context, new DogModel<>(context.bakeLayer(DogModel.LAYER_LOCATION)), 0.7F);
        this.addLayer(new DogHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(DogEntity entity) {
        int variant = entity.getVariant();

        if (entity.isSleeping() || entity.isLying()) {
            if (variant == 0) {
                return BROWN_SLEEP;
            }
            if (variant == 1) {
                return WHITE_SLEEP;
            }
            return GRAY_SLEEP;
        }

        if (variant == 0) {
            return BROWN;
        }
        if (variant == 1) {
            return WHITE;
        }
        return GRAY;
    }

    @Override
    public void render(DogEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            poseStack.scale(0.4F, 0.4F, 0.4F);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}