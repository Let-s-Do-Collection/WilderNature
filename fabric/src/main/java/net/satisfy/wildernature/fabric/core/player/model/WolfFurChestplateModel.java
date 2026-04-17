package net.satisfy.wildernature.fabric.core.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.WilderNature;

public class WolfFurChestplateModel<T extends Entity> extends EntityModel<T> {
    public static final ResourceLocation WOLF_FUR_CHESTPLATE_TEXTURE = WilderNature.identifier("textures/models/armor/fur_cloak.png");

    private final ModelPart chestplate;
    private final ModelPart cape;

    public WolfFurChestplateModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.chestplate = root.getChild("chestplate");
        this.cape = this.chestplate.getChild("cape");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootPart = meshDefinition.getRoot();

        PartDefinition chestplatePart = rootPart.addOrReplaceChild("chestplate",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-17.0F, -3.5F, -1.0F, 18.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(23, 22).addBox(-12.0F, -4.5F, 5.0F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(-11.0F, -0.5F, 5.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 2.5F, -2.0F));

        chestplatePart.addOrReplaceChild("cape",
                CubeListBuilder.create().texOffs(0, 10).addBox(-6.0F, 0.5F, 0.5F, 12.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, -0.5F, 3.5F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void syncToBody(ModelPart bodyPart, boolean crouching) {
        this.chestplate.xRot = bodyPart.xRot;
        this.chestplate.yRot = bodyPart.yRot;
        this.chestplate.zRot = bodyPart.zRot;
        this.chestplate.x = 8.0F;
        this.chestplate.y = crouching ? 5.2F : 2.5F;
        this.chestplate.z = crouching ? -1.35F : -2.0F;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        float horizontalVelocity = (float) livingEntity.getDeltaMovement().horizontalDistance();
        float movementIntensity = Mth.clamp(horizontalVelocity * 8.0F, 0.0F, 1.0F);
        float walkSwing = Mth.sin(limbSwing * 0.6F) * 0.08F * limbSwingAmount;
        float idleSwing = Mth.sin(ageInTicks * 0.08F) * 0.02F;
        float flowSwing = Mth.sin(ageInTicks * 0.18F) * 0.03F * (0.35F + movementIntensity);
        float crouchOffset = livingEntity.isCrouching() ? 0.08F : 0.0F;

        this.cape.xRot = 0.18F + movementIntensity * 0.45F + walkSwing + idleSwing + flowSwing + crouchOffset;
        this.cape.yRot = Mth.sin(ageInTicks * 0.09F + limbSwing * 0.12F) * 0.015F * (0.5F + movementIntensity);
        this.cape.zRot = Mth.cos(ageInTicks * 0.07F + limbSwing * 0.1F) * 0.02F * (0.4F + movementIntensity);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.chestplate.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}