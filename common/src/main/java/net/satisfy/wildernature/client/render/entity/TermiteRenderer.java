package net.satisfy.wildernature.client.render.entity;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.TermiteModel;
import net.satisfy.wildernature.client.render.entity.layer.TermiteHeldItemLayer;
import net.satisfy.wildernature.core.entity.animal.passive.TermiteEntity;
import org.jetbrains.annotations.NotNull;

public class TermiteRenderer extends MobRenderer<TermiteEntity, TermiteModel<TermiteEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/termite.png");

    public TermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new TermiteModel<>(context.bakeLayer(TermiteModel.LAYER_LOCATION)), 0.2F);
        this.addLayer(new TermiteHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(TermiteEntity entity) {
        return TEXTURE;
    }
}