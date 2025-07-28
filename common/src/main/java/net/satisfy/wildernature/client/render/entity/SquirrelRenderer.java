package net.satisfy.wildernature.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.SquirrelModel;
import net.satisfy.wildernature.entity.SquirrelEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;


@Environment(EnvType.CLIENT)
public class SquirrelRenderer extends MobRenderer<SquirrelEntity, SquirrelModel> {

    public SquirrelRenderer(EntityRendererProvider.Context context) {
        super(context, new SquirrelModel(context.bakeLayer(SquirrelModel.LAYER_LOCATION)), 0.4F);
    }

    @Override
    public ResourceLocation getTextureLocation(SquirrelEntity entity) {
        return WilderNatureIdentifier.of("textures/entity/squirrel.png");
    }
}