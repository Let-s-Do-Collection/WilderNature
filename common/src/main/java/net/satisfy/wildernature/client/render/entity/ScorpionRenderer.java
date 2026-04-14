package net.satisfy.wildernature.client.render.entity;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.ScorpionModel;
import net.satisfy.wildernature.core.entity.animal.tameable.ScorpionEntity;
import org.jetbrains.annotations.NotNull;

public class ScorpionRenderer extends MobRenderer<ScorpionEntity, ScorpionModel<ScorpionEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/scorpion.png");
    private static final ResourceLocation EYES_TEXTURE = WilderNature.identifier("textures/entity/scorpion_eyes.png");

    public ScorpionRenderer(EntityRendererProvider.Context context) {
        super(context, new ScorpionModel<>(context.bakeLayer(ScorpionModel.LAYER_LOCATION)), 0.35F);
        this.addLayer(new EyesLayer<>(this) {
            @Override
            public @NotNull RenderType renderType() {
                return RenderType.eyes(EYES_TEXTURE);
            }
        });
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(ScorpionEntity entity) {
        return TEXTURE;
    }
}