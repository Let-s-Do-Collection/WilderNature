package net.satisfy.wildernature.neoforge.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class WolfFurChestplateModel<T extends Entity> extends EntityModel<T> {

    public static final ResourceLocation WOLF_FUR_CHESTPLATE_TEXTURE = WilderNatureIdentifier.of("textures/models/armor/fur_cloak.png");

    private final ModelPart chestplate;
    private final ModelPart cape;

    public WolfFurChestplateModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.chestplate = root.getChild("chestplate");
        this.cape = this.chestplate.getChild("cape");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();

        PartDefinition chestplate = part.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -3.5F, -1.0F, 18.0F, 4.0F, 6.0F, new CubeDeformation(0F))
                .texOffs(23, 22).addBox(-12.0F, -4.5F, 5.0F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 25).addBox(-11.0F, -0.5F, 5.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 2.5F, -2.0F));

        PartDefinition cape = chestplate.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 10).addBox(-6.0F, 0.5F, 0.5F, 12.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -0.5F, 3.5F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof LivingEntity livingEntity) {
            float forwardVelocity = (float) livingEntity.getDeltaMovement().horizontalDistance();
            float waveFrequency = 0.1F;
            float waveAmplitude = 0.05F;

            this.cape.xRot = 0.2F + (0.1F * forwardVelocity) + (waveAmplitude * (float) Math.sin(ageInTicks * waveFrequency));
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        chestplate.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }
}
