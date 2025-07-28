package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.client.model.entity.RedWolfModel;
import net.satisfy.wildernature.entity.RedWolfEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;


@Environment(value = EnvType.CLIENT)
public class RedWolfRenderer extends MobRenderer<RedWolfEntity, RedWolfModel<RedWolfEntity>> {
    private static final ResourceLocation TEXTURE = WilderNatureIdentifier.of("textures/entity/red_wolf.png");

    public RedWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new RedWolfModel<>(context.bakeLayer(RedWolfModel.LAYER_LOCATION)), 0.7f);
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(RedWolfEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(RedWolfEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}

