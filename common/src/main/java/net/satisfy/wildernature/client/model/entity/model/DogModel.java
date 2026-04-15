package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.DogAnimation;
import net.satisfy.wildernature.core.entity.animal.tameable.DogEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class DogModel<T extends DogEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("dog"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public DogModel(ModelPart modelPart) {
        this.root = modelPart.getChild("dog");
        this.head = this.root.getChild("head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition dog = partDefinition.addOrReplaceChild("dog", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition head = dog.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -5.0F, -6.0F, 7.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, -8.0F));
        head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(27, 0).addBox(-2.0F, -2.01F, -2.5F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 0.0F, -6.0F));

        PartDefinition ears = head.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(-0.5F, -5.0F, -2.0F));

        PartDefinition earLeft = ears.addOrReplaceChild("ear_left", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.7489F));
        earLeft.addOrReplaceChild("ear_left_r1", CubeListBuilder.create().texOffs(26, 57).addBox(-1.0F, -2.0F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -0.5F, 3.1416F, 0.0F, 0.0F));

        PartDefinition earRight = ears.addOrReplaceChild("ear_right", CubeListBuilder.create(), PartPose.offsetAndRotation(4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 2.7489F));
        earRight.addOrReplaceChild("ear_right_r1", CubeListBuilder.create().texOffs(26, 57).mirror().addBox(-1.0F, -2.0F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0F, -0.5F, 3.1416F, 0.0F, 0.0F));

        dog.addOrReplaceChild("upper_body", CubeListBuilder.create().texOffs(0, 14).addBox(-4.0F, -4.0F, -1.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, -7.0F));
        dog.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-3.5F, -3.0F, 7.0F, 7.0F, 8.0F, 10.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, -12.0F, -7.0F));
        dog.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(0, 54).mirror().addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, -6.0F, -5.0F));
        dog.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(0, 54).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -6.0F, -5.0F));
        dog.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(13, 54).mirror().addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.3F, -6.0F, 7.0F));
        dog.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(13, 54).addBox(-1.0F, -1.0F, -1.0F, 3.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.3F, -6.0F, 7.0F));

        PartDefinition tail = dog.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 15.0F));
        tail.addOrReplaceChild("tail_r1", CubeListBuilder.create().texOffs(46, 19).addBox(-1.0F, 2.0F, -5.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition realTail = tail.addOrReplaceChild("realTail", CubeListBuilder.create(), PartPose.ZERO);
        realTail.addOrReplaceChild("tail_r2", CubeListBuilder.create().texOffs(46, 10).addBox(-1.0F, 2.0F, -5.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot += headPitch * ((float) Math.PI / 180F);

        this.animateWalk(DogAnimation.walk, limbSwing, limbSwingAmount, 2.5F, 2.5F);
        this.animate(entity.idleAnimationState, DogAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.sitAnimationState, DogAnimation.sit, ageInTicks, 1.0F);
        this.animate(entity.howlingAnimationState, DogAnimation.howl, ageInTicks, 1.0F);
        this.animate(entity.attackAnimationState, DogAnimation.bite, ageInTicks, 1.0F);
        this.animate(entity.lyingAnimationState, DogAnimation.sleep, ageInTicks, 1.0F);
        this.animate(entity.sleepAnimationState, DogAnimation.sleep, ageInTicks, 1.0F);
        this.animate(entity.digAnimationState, DogAnimation.dig, ageInTicks, 1.0F);
    }
}