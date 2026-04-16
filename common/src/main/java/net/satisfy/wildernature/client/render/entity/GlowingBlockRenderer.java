package net.satisfy.wildernature.client.render.entity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.satisfy.wildernature.core.entity.fx.GlowingBlock;
import org.jetbrains.annotations.NotNull;

import java.util.OptionalDouble;

public class GlowingBlockRenderer extends EntityRenderer<GlowingBlock> {

    private static final RenderType LINES_NO_DEPTH = RenderType.create("lines_no_depth", DefaultVertexFormat.POSITION_COLOR_NORMAL, VertexFormat.Mode.LINES, 256, RenderType.CompositeState.builder().setShaderState(RenderType.RENDERTYPE_LINES_SHADER).setWriteMaskState(RenderType.COLOR_WRITE).setCullState(RenderType.NO_CULL).setDepthTestState(RenderType.NO_DEPTH_TEST).setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY).setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(6.0))).createCompositeState(false));

    public GlowingBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(GlowingBlock entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/white.png");
    }

    @Override
    public boolean shouldRender(GlowingBlock entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public void render(GlowingBlock entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, -0.5D, 0.0D);

        VertexConsumer vertexConsumer = buffer.getBuffer(LINES_NO_DEPTH);
        AABB boundingBox = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());

        int glowColor = entity.getGlowColor();
        float red = (float) (glowColor >> 16 & 255) / 255.0F;
        float green = (float) (glowColor >> 8 & 255) / 255.0F;
        float blue = (float) (glowColor & 255) / 255.0F;

        LevelRenderer.renderLineBox(poseStack, vertexConsumer, boundingBox, red, green, blue, 1.0F);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}