package net.satisfy.wildernature.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.TermiteModel;
import net.satisfy.wildernature.core.entity.TermiteEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class TermiteRenderer extends MobRenderer<TermiteEntity, TermiteModel> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/termite.png");

    public TermiteRenderer(EntityRendererProvider.Context context) {
        super(context, new TermiteModel(context.bakeLayer(TermiteModel.LAYER_LOCATION)), 0.05f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(TermiteEntity entity) {
        return TEXTURE;
    }
}
