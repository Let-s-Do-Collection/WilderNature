package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.MiniSheepModel;
import net.satisfy.wildernature.entity.MiniSheepEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(value = EnvType.CLIENT)
public class MiniSheepRenderer extends MobRenderer<MiniSheepEntity, MiniSheepModel<MiniSheepEntity>> {
    private static final ResourceLocation SHEARED_TEXTURE = WilderNatureIdentifier.of("textures/entity/minisheep_sheared.png");
    private static final ResourceLocation DEFAULT_TEXTURE = WilderNatureIdentifier.of("textures/entity/minisheep.png");

    public MiniSheepRenderer(EntityRendererProvider.Context context) {
        super(context, new MiniSheepModel<>(context.bakeLayer(MiniSheepModel.LAYER_LOCATION)), 0.7f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(MiniSheepEntity entity) {
        return entity.isSheared() ? SHEARED_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    public void render(MiniSheepEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack,
                       MultiBufferSource buffer, int packedLight) {
        if (entity.isBaby()) {
            matrixStack.scale(0.4f, 0.4f, 0.4f);
        }
        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }
}
