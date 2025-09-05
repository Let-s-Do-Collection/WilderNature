package net.satisfy.wildernature.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.SquirrelModel;
import net.satisfy.wildernature.core.entity.SquirrelEntity;
import org.jetbrains.annotations.NotNull;


@Environment(EnvType.CLIENT)
public class SquirrelRenderer extends MobRenderer<SquirrelEntity, SquirrelModel> {

    public SquirrelRenderer(EntityRendererProvider.Context context) {
        super(context, new SquirrelModel(context.bakeLayer(SquirrelModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SquirrelEntity entity) {
        return WilderNature.identifier("textures/entity/squirrel.png");
    }
}