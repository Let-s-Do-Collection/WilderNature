package net.satisfy.wildernature.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.foundation.client.render.WobbleAnimation;
import net.satisfy.wildernature.platform.PlatformHelper;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.ClientBountyBoardNotes;
import net.satisfy.wildernature.client.model.block.BountyBoardModel;
import net.satisfy.wildernature.core.block.BountyBoardBlock;
import net.satisfy.wildernature.core.block.entity.BountyBoardBlockEntity;
import org.joml.Quaternionf;

public class BountyBoardRenderer implements BlockEntityRenderer<BountyBoardBlockEntity> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/entity/bounty_board.png");
    private final BountyBoardModel<?> model;

    public BountyBoardRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new BountyBoardModel<>(context.bakeLayer(BountyBoardModel.LAYER_LOCATION));
    }

    @Override
    public void render(BountyBoardBlockEntity blockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        BlockState blockState = blockEntity.getBlockState();
        Direction direction = blockState.getValue(BountyBoardBlock.FACING);
        poseStack.pushPose();

        switch (direction) {
            case NORTH -> {
                poseStack.translate(1.5, 1.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(0.0F)));
            }
            case SOUTH -> {
                poseStack.translate(-0.5, 1.5, 0.5);
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(180.0F)));
            }
            case WEST -> {
                poseStack.translate(0.5, 1.5, -0.5);
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(90.0F)));
            }
            case EAST -> {
                poseStack.translate(0.5, 1.5, 1.5);
                poseStack.mulPose(new Quaternionf().rotateY((float) Math.toRadians(-90.0F)));
            }
        }

        poseStack.mulPose(new Quaternionf().rotateZ((float) Math.toRadians(180.0F)));
        Level level = blockEntity.getLevel();
        boolean animated = level != null && PlatformHelper.isAnimationsEnabled();
        float time = animated ? level.getGameTime() + f : 0.0F;
        BlockPos pos = blockEntity.getBlockPos();
        float phase = WobbleAnimation.seed(pos.asLong());
        float wobble = !animated ? 0.0F : WobbleAnimation.angle(level, blockEntity.getWobbleStart(), f) * Mth.DEG_TO_RAD;
        model.setContracts(level == null ? BountyBoardModel.CONTRACT_COUNT : ClientBountyBoardNotes.getVisibleContracts(BountyBoardModel.CONTRACT_COUNT), time, wobble, phase);

        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entityCutout(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}
