package net.satisfy.wildernature.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.block.HollowCacheModel;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import org.joml.Quaternionf;

public class HollowCacheRenderer implements BlockEntityRenderer<HollowCacheBlockEntity> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/hollow_cache.png");
    private final HollowCacheModel<?> model;

    public HollowCacheRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new HollowCacheModel<>(context.bakeLayer(HollowCacheModel.LAYER_LOCATION));
    }

    @Override
    public void render(HollowCacheBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        Direction direction = blockState.getValue(HollowCacheBlock.FACING);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D, 0.5D);

        switch (direction) {
            case NORTH -> poseStack.mulPose(new Quaternionf().rotateY(0.0F));
            case SOUTH -> poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180.0D)));
            case WEST -> poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(90.0D)));
            case EAST -> poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-90.0D)));
        }

        poseStack.scale(1.0F, -1.0F, -1.0F);

        this.model.hollowCache.xRot = 0.0F;
        this.model.hollowCache.yRot = 0.0F;
        this.model.hollowCache.zRot = 0.0F;

        float openProgress = blockEntity.getOpenProgress(partialTick);
        float eased = 1.0F - (1.0F - openProgress) * (1.0F - openProgress) * (1.0F - openProgress);
        this.model.hollowCache.xRot = -eased * ((float) Math.PI / 3.0F);

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}