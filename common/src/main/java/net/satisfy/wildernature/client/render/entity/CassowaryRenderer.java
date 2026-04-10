package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BabyCassowaryModel;
import net.satisfy.wildernature.client.model.entity.model.CassowaryModel;
import net.satisfy.wildernature.core.entity.animal.defensive.CassowaryEntity;
import org.jetbrains.annotations.NotNull;

public class CassowaryRenderer extends MobRenderer<CassowaryEntity, HierarchicalModel<CassowaryEntity>> {
    private static final ResourceLocation ADULT_TEXTURE = WilderNature.identifier("textures/entity/cassowary.png");
    private static final ResourceLocation BABY_TEXTURE = WilderNature.identifier("textures/entity/baby_cassowary.png");

    private final CassowaryModel<CassowaryEntity> adultModel;
    private final BabyCassowaryModel<CassowaryEntity> babyModel;

    public CassowaryRenderer(EntityRendererProvider.Context context) {
        super(context, new CassowaryModel<>(context.bakeLayer(CassowaryModel.LAYER_LOCATION)), 0.7f);
        this.adultModel = new CassowaryModel<>(context.bakeLayer(CassowaryModel.LAYER_LOCATION));
        this.babyModel = new BabyCassowaryModel<>(context.bakeLayer(BabyCassowaryModel.LAYER_LOCATION));
        this.model = this.adultModel;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(CassowaryEntity entity) {
        return entity.isBaby() ? BABY_TEXTURE : ADULT_TEXTURE;
    }

    @Override
    public void render(CassowaryEntity entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            this.model = this.babyModel;
            this.shadowRadius = 0.3F;
        } else {
            this.model = this.adultModel;
            this.shadowRadius = 0.7F;
        }

        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }
}