package net.satisfy.wildernature.client.render.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.SquirrelModel;
import net.satisfy.wildernature.client.render.entity.layer.SquirrelHeldItemLayer;
import net.satisfy.wildernature.core.entity.animal.SquirrelEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SquirrelRenderer extends MobRenderer<SquirrelEntity, SquirrelModel> {
    private static final ResourceLocation BROWN = WilderNature.identifier("textures/entity/squirrel_brown.png");
    private static final ResourceLocation RED = WilderNature.identifier("textures/entity/squirrel_red.png");
    private static final ResourceLocation GRAY = WilderNature.identifier("textures/entity/squirrel_gray.png");

    public SquirrelRenderer(EntityRendererProvider.Context context) {
        super(context, new SquirrelModel(context.bakeLayer(SquirrelModel.LAYER_LOCATION)), 0.4F);
        this.addLayer(new SquirrelHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(SquirrelEntity entity) {
        return switch (entity.getVariant()) {
            case 1 -> RED;
            case 2 -> GRAY;
            default -> BROWN;
        };
    }
}