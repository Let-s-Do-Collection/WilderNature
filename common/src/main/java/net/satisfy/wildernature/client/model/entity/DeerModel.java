package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.entity.DeerEntity;
import net.satisfy.wildernature.entity.animation.DeerAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class DeerModel extends HierarchicalModel<DeerEntity> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("deer"), "main");
    private final ModelPart deer;
    private final ModelPart head;

    public DeerModel(ModelPart root) {
        this.deer = root.getChild("deer");
        this.head = this.deer.getChild("body").getChild("head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition deer = partdefinition.addOrReplaceChild("deer", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = deer.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -6.0F, 7.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(-5.0F,
                -4.0F, -9.0F, 10.0F, 9.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -15.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(18, 27).addBox(-3.0F, -12.0F, -7.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 9)
                        .addBox(-2.0F, -10.0F, -10.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 27).addBox(-2.0F, -7.0F, -5.0F, 4.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, 0.0F, -7.0F));

        head.addOrReplaceChild("horn_right_r1", CubeListBuilder.create().texOffs(19, 28).addBox(-3.09F, -37.0F, -14.0F, 0.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 15.0F, 7.0F, 0.0F, 0.0F, 0.2182F));

        head.addOrReplaceChild("horn_left_r1", CubeListBuilder.create().texOffs(19, 28).addBox(2.93F, -37.0F, -14.0F, 0.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 15.0F, 7.0F, 0.0F, 0.0F, -0.2182F));

        head.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(38, 0).mirror().addBox(-7.0F, -26.0F, -9.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 15.0F, 7.0F, 0.0F, 0.0F, 0.0436F));

        head.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(38, 0).addBox(4.0F, -26.0F, -9.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, 15.0F, 7.0F, 0.0F, 0.0F, -0.0436F));

        deer.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(38, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -10.0F, 7.5F));

        deer.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(38, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.5F, -10.0F, 7.5F));

        deer.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(38, 5).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offset(3.5F, -10.0F, -7.5F));

        deer.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(38, 5).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-3.5F, -10.0F, -7.5F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(DeerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * 0.0089453292F;
        this.head.xRot = headPitch * 0.0047453292F;

        this.animate(entity.idleState, DeerAnimation.idle, ageInTicks, 1f);
        if (entity.isDeerRunning()) {
            this.animateWalk(DeerAnimation.run, limbSwing, limbSwingAmount, 1.5f, 2.5f);
        } else {
            this.animateWalk(DeerAnimation.walk, limbSwing, limbSwingAmount, 1f, 2.5f);
        }
        this.animate(entity.lookAroundState, DeerAnimation.look_around, ageInTicks, 1.0f);
        this.animate(entity.eatingState, DeerAnimation.eat, ageInTicks, 1.0f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        deer.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return deer;
    }
}