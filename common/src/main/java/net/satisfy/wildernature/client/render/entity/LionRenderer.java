package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BabyLionModel;
import net.satisfy.wildernature.client.model.entity.model.LionModel;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import org.jetbrains.annotations.NotNull;

public class LionRenderer extends MobRenderer<LionEntity, HierarchicalModel<LionEntity>> {
    private static final ResourceLocation ADULT_TEXTURE = WilderNature.identifier("textures/entity/lion.png");
    private static final ResourceLocation BABY_TEXTURE = WilderNature.identifier("textures/entity/baby_lion.png");
    private static final ResourceLocation ADULT_SLEEPING_TEXTURE = WilderNature.identifier("textures/entity/lion_sleep.png");
    private static final ResourceLocation BABY_SLEEPING_TEXTURE = WilderNature.identifier("textures/entity/baby_lion_sleep.png");

    private final LionModel<LionEntity> adultModel;
    private final BabyLionModel<LionEntity> babyModel;

    public LionRenderer(EntityRendererProvider.Context context) {
        super(context, new LionModel<>(context.bakeLayer(LionModel.LAYER_LOCATION)), 0.9F);
        this.adultModel = new LionModel<>(context.bakeLayer(LionModel.LAYER_LOCATION));
        this.babyModel = new BabyLionModel<>(context.bakeLayer(BabyLionModel.LAYER_LOCATION));
        this.model = this.adultModel;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(LionEntity entity) {
        if (entity.isBaby()) {
            return entity.isSleeping() ? BABY_SLEEPING_TEXTURE : BABY_TEXTURE;
        }
        return entity.isSleeping() ? ADULT_SLEEPING_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    protected void setupRotations(LionEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale) {
        super.setupRotations(entity, poseStack, bob, yBodyRot, partialTick, scale);

        if (entity.isSleeping()) {
            if (entity.isBaby()) {
                poseStack.translate(0.0F, -0.15F, 0.0F);
                poseStack.mulPose(Axis.ZN.rotationDegrees(22.5F));
            } else {
                poseStack.translate(0.0F, -0.3F, 0.0F);
                poseStack.mulPose(Axis.ZN.rotationDegrees(45.0F));
            }
        }
    }

    @Override
    public void render(LionEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (entity.isBaby()) {
            this.model = this.babyModel;
            this.shadowRadius = 0.45F;
        } else {
            this.model = this.adultModel;
            this.shadowRadius = 0.9F;
        }

        super.render(entity, yaw, partialTicks, poseStack, multiBufferSource, packedLight);
    }
}