package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.BabyElephantAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.ElephantEntity;
import org.jetbrains.annotations.NotNull;

public class BabyElephantModel<T extends ElephantEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("baby_elephant"), "main");

    private final ModelPart base;
    private final ModelPart head;

    @SuppressWarnings("unused")
    public BabyElephantModel(ModelPart root) {
        this.base = root.getChild("base");
        ModelPart hips = this.base.getChild("hips");
        ModelPart torso = hips.getChild("torso");
        this.head = torso.getChild("h_head");
        ModelPart trunk = this.head.getChild("h_trunk");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition base = partDefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition hips = base.addOrReplaceChild("hips", CubeListBuilder.create(), PartPose.offset(0.0F, 2.734F, -0.7936F));

        PartDefinition torso = hips.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-7.0F, -18.734F, -10.2064F, 14.0F, 17.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(8, 2).mirror().addBox(-1.0F, -0.5F, 0.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -16.234F, 10.7936F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.5F, 1.0F));

        PartDefinition head = torso.addOrReplaceChild("h_head", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(-9.0F, -6.63F, -15.4089F, 18.0F, 21.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -15.104F, -8.7975F));

        PartDefinition trunk = head.addOrReplaceChild("h_trunk", CubeListBuilder.create()
                .texOffs(68, 0).addBox(-4.0F, -1.2691F, -7.1255F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.6392F, -14.2834F));

        trunk.addOrReplaceChild("h_trunk_2", CubeListBuilder.create()
                .texOffs(68, 20).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.7309F, -7.1255F));

        head.addOrReplaceChild("h_leftEar", CubeListBuilder.create()
                .texOffs(50, 39).addBox(-2.0F, -13.0F, -1.0F, 16.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -0.63F, -4.4089F));

        head.addOrReplaceChild("h_rightEar", CubeListBuilder.create()
                .texOffs(50, 39).mirror().addBox(-14.0F, -13.0F, -1.0F, 16.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-7.0F, -0.63F, -4.4089F));

        hips.addOrReplaceChild("rightArm", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -2.734F, -5.7064F));

        hips.addOrReplaceChild("leftArm", CubeListBuilder.create()
                .texOffs(0, 42).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, -2.734F, -5.7064F));

        base.addOrReplaceChild("rightLeg", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, 0.0F, 7.5F));

        base.addOrReplaceChild("leftLeg", CubeListBuilder.create()
                .texOffs(0, 42).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.5F, 0.0F, 7.5F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.head.xRot += headPitch * ((float) Math.PI / 180F) * 0.25F;

        boolean isBusy = entity.isThrowing() || entity.isStamping() || entity.isCharging() || entity.isDrinking() || entity.isTrumpeting();

        if (limbSwingAmount < 0.01F && !isBusy) {
            this.animate(entity.idleState, BabyElephantAnimation.idle, ageInTicks, 1.0F);
        }

        if (!entity.isThrowing() && !entity.isStamping() && !entity.isCharging()) {
            this.animateWalk(BabyElephantAnimation.walk, limbSwing, limbSwingAmount, 2.5F, 2.5F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.base;
    }
}