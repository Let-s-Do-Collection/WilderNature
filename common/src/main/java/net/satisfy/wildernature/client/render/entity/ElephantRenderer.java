package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BabyElephantModel;
import net.satisfy.wildernature.client.model.entity.model.ElephantModel;
import net.satisfy.wildernature.core.entity.animal.defensive.ElephantEntity;
import org.jetbrains.annotations.NotNull;

public class ElephantRenderer extends MobRenderer<ElephantEntity, HierarchicalModel<ElephantEntity>> {
    private static final ResourceLocation ADULT_TEXTURE = WilderNature.identifier("textures/entity/elephant.png");
    private static final ResourceLocation BABY_TEXTURE = WilderNature.identifier("textures/entity/baby_elephant.png");

    private final ElephantModel<ElephantEntity> adultModel;
    private final BabyElephantModel<ElephantEntity> babyModel;

    public ElephantRenderer(EntityRendererProvider.Context context) {
        super(context, new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION)), 1.2F);
        this.adultModel = new ElephantModel<>(context.bakeLayer(ElephantModel.LAYER_LOCATION));
        this.babyModel = new BabyElephantModel<>(context.bakeLayer(BabyElephantModel.LAYER_LOCATION));
        this.model = this.adultModel;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ElephantEntity entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    public void render(ElephantEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (entity.isBaby()) {
            this.model = this.babyModel;
            this.shadowRadius = 1F;
        } else {
            this.model = this.adultModel;
            this.shadowRadius = 1.5F;
        }

        super.render(entity, yaw, partialTicks, poseStack, multiBufferSource, packedLight);
    }
}