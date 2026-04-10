package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.model.BabyGiraffeModel;
import net.satisfy.wildernature.client.model.entity.model.GiraffeModel;
import net.satisfy.wildernature.core.entity.animal.neutral.GiraffeEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class GiraffeRenderer extends MobRenderer<GiraffeEntity, EntityModel<GiraffeEntity>> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/giraffe.png");
    private static final ResourceLocation BABY_TEXTURE = WilderNature.identifier("textures/entity/baby_giraffe.png");
    private static final ResourceLocation COWSTICKER_TEXTURE = WilderNature.identifier("textures/entity/giraffe_cowsticker.png");

    private final GiraffeModel<GiraffeEntity> adultModel;
    private final BabyGiraffeModel<GiraffeEntity> babyModel;

    public GiraffeRenderer(EntityRendererProvider.Context context) {
        super(context, new GiraffeModel<>(context.bakeLayer(GiraffeModel.LAYER_LOCATION)), 1.0F);
        this.adultModel = new GiraffeModel<>(context.bakeLayer(GiraffeModel.LAYER_LOCATION));
        this.babyModel = new BabyGiraffeModel<>(context.bakeLayer(BabyGiraffeModel.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(GiraffeEntity entity) {
        if (entity.hasCustomName()) {
            Component nameComponent = entity.getCustomName();
            if (nameComponent != null && "cowsticker".equalsIgnoreCase(nameComponent.getString())) {
                return COWSTICKER_TEXTURE;
            }
        }

        return entity.isBaby() ? BABY_TEXTURE : TEXTURE;
    }

    @Override
    public void render(GiraffeEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        this.model = entity.isBaby() ? this.babyModel : this.adultModel;

        poseStack.pushPose();

        if (entity.isBaby()) {
            if (entity.isSleeping()) {
                poseStack.translate(0.0D, 1.2D, 0.0D);
            }
        } else {
            poseStack.translate(0.0D, 1.4D, 0.0D);
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        poseStack.popPose();
    }
}