package net.satisfy.wildernature.client.model.entity;

import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.entity.OwlEntity;
import net.satisfy.wildernature.entity.animation.OwlAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class OwlModel extends HierarchicalModel<OwlEntity> implements HeadedModel {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("owl"), "main");
    private final ModelPart root;
    private final ModelPart head;

    public OwlModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("root").getChild("animroot").getChild("head");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.8532F, -0.6407F));

        PartDefinition animroot = root.addOrReplaceChild("animroot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso_r1 = animroot.addOrReplaceChild("torso_r1", CubeListBuilder.create().texOffs(0, 26).mirror().addBox(-3.5F, -2.475F, -3.75F, 7.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition cube_r1 = animroot.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(5, 38).addBox(-7.059F, 4.0F, 3.3F, 7.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.559F, 3.2079F, -2.2761F, 0.7854F, 0.0F, 0.0F));

        PartDefinition tailWing = animroot.addOrReplaceChild("tailWing", CubeListBuilder.create(), PartPose.offset(0.0F, 0.6761F, -1.1119F));

        PartDefinition cube_r2 = tailWing.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, -2.5858F, 0.2071F, 7.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.4225F, 2.1273F, 0.7854F, 0.0F, 0.0F));

        PartDefinition wing_right = animroot.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offset(3.501F, -0.9282F, -2.3335F));

        PartDefinition wing_right_r1 = wing_right.addOrReplaceChild("wing_right_r1", CubeListBuilder.create().texOffs(14, 9).mirror().addBox(-0.025F, -0.3609F, -2.4857F, 0.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.074F, 0.2394F, 0.1449F, 0.7854F, 0.0F, 0.0F));

        PartDefinition wing_left = animroot.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offset(-3.501F, -0.9282F, -2.3335F));

        PartDefinition wing_left_r1 = wing_left.addOrReplaceChild("wing_left_r1", CubeListBuilder.create().texOffs(14, 9).addBox(0.025F, -0.3609F, -2.4857F, 0.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.074F, 0.2394F, 0.1449F, 0.7854F, 0.0F, 0.0F));

        PartDefinition leg_right = animroot.addOrReplaceChild("leg_right", CubeListBuilder.create().texOffs(23, 4).addBox(-1.975F, 2.6133F, -2.7904F, 3.95F, 0.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(27, 4).addBox(-0.975F, 0.65F, -0.6F, 1.95F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).mirror().addBox(-1.0F, -0.7354F, -1.0251F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, 4.4468F, 0.2407F));

        PartDefinition leg_left = animroot.addOrReplaceChild("leg_left", CubeListBuilder.create().texOffs(23, 4).mirror().addBox(-1.975F, 2.6133F, -2.7904F, 3.95F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(27, 4).addBox(-0.975F, 0.65F, -0.6F, 1.95F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(20, 0).addBox(-1.0F, -0.7354F, -1.0251F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 4.4468F, 0.2407F));

        PartDefinition head = animroot.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.525F, -4.25F, -3.0833F, 7.05F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(6, 11).addBox(-3.525F, 0.75F, -3.0833F, 7.05F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 11).mirror().addBox(-0.5F, -1.25F, -4.0833F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.6015F, -1.6284F));

        PartDefinition right_horn_r1 = head.addOrReplaceChild("right_horn_r1", CubeListBuilder.create().texOffs(0, -1).mirror().addBox(-1.25F, -2.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.4089F, -3.3661F, -2.0833F, 0.0F, 0.0F, 0.7854F));

        PartDefinition left_horn_r1 = head.addOrReplaceChild("left_horn_r1", CubeListBuilder.create().texOffs(0, 1).addBox(1.25F, -2.0F, -1.0F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4089F, -3.3661F, -2.0833F, 0.0F, 0.0F, -0.7854F));

        return LayerDefinition.create(meshdefinition, 48, 48);
    }

    @Override
    public void setupAnim(OwlEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        animateWalk(OwlAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        if (entity.attackState.isStarted()) {
            animate(entity.attackState, OwlAnimation.attack, ageInTicks);
            return;
        }
        animate(entity.flyingState, OwlAnimation.fly, ageInTicks);
        animate(entity.hootState, OwlAnimation.hoot, ageInTicks);
        animate(entity.sleepState, OwlAnimation.sleep, ageInTicks);
    }

    @Override
    public void prepareMobModel(OwlEntity entity, float limbAngle, float limbDistance, float delta) {
        super.prepareMobModel(entity, limbAngle, limbDistance, delta);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    @Override
    public @NotNull ModelPart getHead() {
        return head;
    }
}
