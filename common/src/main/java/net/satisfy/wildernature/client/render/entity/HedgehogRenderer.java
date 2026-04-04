package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.HedgehogModel;
import net.satisfy.wildernature.core.entity.animal.passive.HedgehogEntity;
import org.jetbrains.annotations.NotNull;


@Environment(value = EnvType.CLIENT)
public class HedgehogRenderer extends MobRenderer<HedgehogEntity, HedgehogModel<HedgehogEntity>> {
    private static final ResourceLocation DEFAULT_TEXTURE = WilderNature.identifier("textures/entity/hedgehog.png");
    private static final ResourceLocation SLEEPING_TEXTURE = WilderNature.identifier("textures/entity/hedgehog_sleep.png");

    public HedgehogRenderer(EntityRendererProvider.Context context) {
        super(context, new HedgehogModel<>(context.bakeLayer(HedgehogModel.LAYER_LOCATION)), 0.7f);
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(HedgehogEntity entity) {
        return entity.isSleeping() ? SLEEPING_TEXTURE : DEFAULT_TEXTURE;
    }

    @Override
    public void render(HedgehogEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        if (pEntity.isBaby()) {
            pMatrixStack.scale(0.4f, 0.4f, 0.4f);
        }
        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}

