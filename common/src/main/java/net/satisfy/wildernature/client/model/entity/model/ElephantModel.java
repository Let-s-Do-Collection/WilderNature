package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.ElephantAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.ElephantEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ElephantModel<T extends ElephantEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("elephant"), "main");

    private final ModelPart base;
    private final ModelPart head;

    public ElephantModel(ModelPart root) {
        this.base = root.getChild("base");
        ModelPart hips = this.base.getChild("hips");
        ModelPart torso = hips.getChild("torso");
        ModelPart tail = torso.getChild("tail");
        this.head = torso.getChild("head");
        ModelPart trunk = this.head.getChild("trunk");
        ModelPart trunkMiddle = trunk.getChild("trunk_2");
        ModelPart trunkTip = trunkMiddle.getChild("trunk_3");
        ModelPart rightEar = this.head.getChild("rightEar");
        ModelPart leftEar = this.head.getChild("leftEar");
        ModelPart rightArm = hips.getChild("rightArm");
        ModelPart leftArm = hips.getChild("leftArm");
        ModelPart rightLeg = this.base.getChild("rightLeg");
        ModelPart leftLeg = this.base.getChild("leftLeg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition base = partDefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition hips = base.addOrReplaceChild("hips", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 16.0F));

        PartDefinition torso = hips.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-16.0F, -36.0F, -24.0F, 32.0F, 36.0F, 48.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -16.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(71, 84).mirror().addBox(-2.0F, -2.0F, 0.0F, 4.0F, 18.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -30.0F, 22.0F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create()
                .texOffs(87, 84).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 2.0F));

        PartDefinition head = torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(1, 84).addBox(-12.0F, -21.63F, -19.4089F, 24.0F, 32.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(18, 24).mirror().addBox(8.0F, 0.37F, -23.4089F, 7.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(18, 24).addBox(-15.0F, 0.37F, -23.4089F, 7.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).mirror().addBox(12.0F, 0.37F, -19.4089F, 3.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(125, 106).mirror().addBox(8.0F, 2.37F, -43.4089F, 6.0F, 6.0F, 20.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(24, 10).mirror().addBox(8.0F, -5.63F, -43.4089F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 26).addBox(-15.0F, 0.37F, -19.4089F, 3.0F, 10.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(125, 106).addBox(-14.0F, 2.37F, -43.4089F, 6.0F, 6.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(24, 10).addBox(-14.0F, -5.63F, -43.4089F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -20.37F, -22.5911F));

        PartDefinition trunk = head.addOrReplaceChild("trunk", CubeListBuilder.create()
                .texOffs(77, 124).mirror().addBox(-8.0F, -4.0F, -13.0F, 16.0F, 20.0F, 16.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.63F, -16.4089F));

        PartDefinition trunkMiddle = trunk.addOrReplaceChild("trunk_2", CubeListBuilder.create()
                .texOffs(0, 138).mirror().addBox(-6.0F, 0.0F, 0.0F, 12.0F, 16.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 16.0F, -13.0F));

        trunkMiddle.addOrReplaceChild("trunk_3", CubeListBuilder.create()
                .texOffs(141, 84).addBox(-4.0F, 0.0F, 0.0F, 8.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, 0.0F));

        head.addOrReplaceChild("rightEar", CubeListBuilder.create()
                .texOffs(112, 18).addBox(-22.0F, -20.0F, -1.0F, 24.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, -9.63F, -4.4089F));

        head.addOrReplaceChild("leftEar", CubeListBuilder.create()
                .texOffs(112, 18).mirror().addBox(-2.0F, -20.0F, -1.0F, 24.0F, 28.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(9.0F, -9.63F, -4.4089F));

        hips.addOrReplaceChild("rightArm", CubeListBuilder.create()
                .texOffs(93, 84).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 28.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, -2.0F, -33.0F));

        hips.addOrReplaceChild("leftArm", CubeListBuilder.create()
                .texOffs(93, 84).mirror().addBox(-6.0F, 0.0F, -6.0F, 12.0F, 28.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(9.0F, -2.0F, -33.0F));

        base.addOrReplaceChild("rightLeg", CubeListBuilder.create()
                .texOffs(93, 84).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 28.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.0F, -3.0F, 16.0F));

        base.addOrReplaceChild("leftLeg", CubeListBuilder.create()
                .texOffs(93, 84).mirror().addBox(-6.0F, 0.0F, -6.0F, 12.0F, 28.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(9.0F, -3.0F, 16.0F));

        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.head.xRot += headPitch * ((float) Math.PI / 180F) * 0.25F;

        boolean isMoving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
        boolean isBusy = entity.isThrowing() || entity.isStamping() || entity.isCharging() || entity.isDrinking() || entity.isTrumpeting();
        float walkAnimationSpeed = isMoving ? Math.max(limbSwingAmount, 0.6F) : 0.0F;

        if (!isMoving && !isBusy) {
            this.animate(entity.idleState, ElephantAnimation.idle, ageInTicks, 1.0F);
        }

        if (!entity.isThrowing() && !entity.isStamping() && !entity.isCharging()) {
            this.animateWalk(ElephantAnimation.walk, limbSwing, walkAnimationSpeed, 4.0F, 2.5F);
        }

        this.animate(entity.throwState, ElephantAnimation.throwing, ageInTicks, 1.0F);
        this.animate(entity.stampState, ElephantAnimation.stamp, ageInTicks, 1.0F);
        this.animate(entity.chargeState, ElephantAnimation.charge, ageInTicks, 1.0F);
        this.animate(entity.drinkState, ElephantAnimation.throwing, ageInTicks, 1.0F);
        this.animate(entity.trumpetState, ElephantAnimation.throwing, ageInTicks, 1.0F);
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