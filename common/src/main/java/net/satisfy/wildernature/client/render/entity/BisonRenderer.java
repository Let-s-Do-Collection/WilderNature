package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.BisonModel;
import net.satisfy.wildernature.core.entity.BisonEntity;
import org.jetbrains.annotations.NotNull;

public class BisonRenderer extends MobRenderer<BisonEntity, BisonModel<BisonEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/bison.png");

    public BisonRenderer(EntityRendererProvider.Context context) {
        super(context, new BisonModel<>(context.bakeLayer(BisonModel.LAYER_LOCATION)), 0.9f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(BisonEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(BisonEntity entity, float yaw, float partialTicks, PoseStack pose, MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            this.shadowRadius = 0.3f;
            var h = this.model.head();
            float sx = h.xScale, sy = h.yScale, sz = h.zScale;
            h.xScale = 1.75f;
            h.yScale = 1.75f;
            h.zScale = 1.75f;
            pose.scale(0.5f, 0.5f, 0.5f);
            super.render(entity, yaw, partialTicks, pose, buffer, packedLight);
            h.xScale = sx;
            h.yScale = sy;
            h.zScale = sz;
            return;
        }
        this.shadowRadius = 0.9f;
        super.render(entity, yaw, partialTicks, pose, buffer, packedLight);
    }
}
