package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.GiraffeAnimation;
import net.satisfy.wildernature.core.entity.animal.neutral.GiraffeEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GiraffeModel<T extends GiraffeEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("giraffe"), "main");

    private final ModelPart root;
    private final ModelPart adultGiraffe;
    private final ModelPart adultNeck;

    private final ModelPart babyGiraffe;
    private final ModelPart babyTorso;
    private final ModelPart babyNeck;
    private final ModelPart babyLeftFrontLeg;
    private final ModelPart babyRightFrontLeg;
    private final ModelPart babyLeftBackLeg;
    private final ModelPart babyRightBackLeg;
    private final ModelPart babyTail;

    public GiraffeModel(ModelPart root) {
        this.root = root;

        this.adultGiraffe = root.getChild("giraffe");
        ModelPart adultTorso = this.adultGiraffe.getChild("torso");
        this.adultNeck = adultTorso.getChild("neck");

        this.babyGiraffe = root.getChild("baby_giraffe");
        this.babyTorso = this.babyGiraffe.getChild("torso");
        this.babyNeck = this.babyTorso.getChild("neck");
        this.babyLeftFrontLeg = this.babyGiraffe.getChild("leftArm");
        this.babyRightFrontLeg = this.babyGiraffe.getChild("rightArm");
        this.babyLeftBackLeg = this.babyGiraffe.getChild("leftLeg");
        this.babyRightBackLeg = this.babyGiraffe.getChild("rightLeg");
        this.babyTail = this.babyTorso.getChild("tail");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition giraffe = partDefinition.addOrReplaceChild("giraffe", CubeListBuilder.create(), PartPose.offset(0.0F, 10.9369F, 0.2883F));

        PartDefinition torso = giraffe.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -17.0F, -29.0F, 14.0F, 18.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0631F, 12.7117F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 50).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.4677F, 2.409F, 0.1745F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(38, 44).addBox(0.0F, 0.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 29.0F, 1.0F));

        PartDefinition neck = torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(92, 0).addBox(-4.25F, -38.2089F, -4.25F, 8.0F, 38.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(20, 50).addBox(-1.25F, -46.2089F, 5.75F, 2.0F, 40.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(88, 48).addBox(-4.25F, -46.2089F, -6.25F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(62, 50).addBox(-3.25F, -43.2089F, -13.25F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -6.2304F, -21.8415F, 0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(80, 62).addBox(-1.0F, -10.5F, -1.1F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(80, 62).mirror().addBox(3.0F, -10.5F, -1.1F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.25F, -42.0133F, 2.2192F, -0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(108, 68).addBox(-1.0F, -2.0F, -0.5F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.75F, -43.2089F, 4.25F));
        neck.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(108, 68).mirror().addBox(-8.0F, -2.0F, -0.5F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.25F, -43.2089F, 4.25F));

        giraffe.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.35F, 7.0631F, -12.7883F));
        giraffe.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.35F, 7.0631F, -12.7883F));
        giraffe.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 7.0631F, 12.2117F));
        giraffe.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 7.0631F, 12.2117F));

        PartDefinition babyGiraffe = partDefinition.addOrReplaceChild("baby_giraffe", CubeListBuilder.create(), PartPose.offset(0.0F, 8.9369F, 0.2883F));

        PartDefinition babyTorso = babyGiraffe.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -11.0F, -18.0F, 10.0F, 12.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0631F, 7.7117F));

        PartDefinition babyTail = babyTorso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 32).addBox(-1.0F, -0.25F, -0.25F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.3551F, 1.584F, 0.1745F, 0.0F, 0.0F));

        babyTail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(20, 28).addBox(0.0F, -1.0F, -2.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.75F, 0.75F));

        PartDefinition babyNeck = babyTorso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(12, 40).addBox(-3.25F, -14.2089F, -2.75F, 6.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(40, 2).addBox(-4.25F, -22.2089F, -4.75F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(32, 32).addBox(-3.25F, -19.2089F, -9.75F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -5.6872F, -14.5726F, 0.3927F, 0.0F, 0.0F));

        babyNeck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 47).mirror().addBox(0.0F, 15.825F, -4.375F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(4, 47).addBox(4.0F, 15.825F, -4.375F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, -37.7645F, 13.4252F, -0.3927F, 0.0F, 0.0F));

        babyNeck.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 42).addBox(-0.5F, -2.0F, -0.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.25F, -19.2089F, 2.75F));
        babyNeck.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(32, 42).mirror().addBox(-6.5F, -2.0F, -0.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.75F, -19.2089F, 2.75F));

        babyGiraffe.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.85F, 4.0631F, -7.7883F));
        babyGiraffe.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.85F, 4.0631F, -7.7883F));
        babyGiraffe.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 4.0631F, 7.2117F));
        babyGiraffe.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 4.0631F, 7.2117F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        boolean isBaby = entity.isBaby();

        this.adultGiraffe.visible = !isBaby;
        this.babyGiraffe.visible = isBaby;

        if (isBaby) {
            this.babyNeck.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
            this.babyNeck.xRot += headPitch * ((float) Math.PI / 180F) * 0.45F;

            float walkSpeed = entity.isRidingBurst() ? 0.9F : 0.6F;
            float walkDegree = entity.isRidingBurst() ? 1.2F : 0.7F;
            float clampedLimbSwingAmount = Math.min(limbSwingAmount, 1.0F);

            this.babyLeftFrontLeg.xRot = Mth.cos(limbSwing * walkSpeed) * walkDegree * clampedLimbSwingAmount;
            this.babyRightFrontLeg.xRot = Mth.cos(limbSwing * walkSpeed + (float) Math.PI) * walkDegree * clampedLimbSwingAmount;
            this.babyLeftBackLeg.xRot = Mth.cos(limbSwing * walkSpeed + (float) Math.PI) * walkDegree * clampedLimbSwingAmount;
            this.babyRightBackLeg.xRot = Mth.cos(limbSwing * walkSpeed) * walkDegree * clampedLimbSwingAmount;

            this.babyTorso.xRot += Mth.cos(limbSwing * walkSpeed * 0.5F) * 0.04F * clampedLimbSwingAmount;
            this.babyTail.yRot += Mth.cos(ageInTicks * 0.12F) * 0.12F;

            if (entity.isEating()) this.babyNeck.xRot += 0.35F;
            if (entity.isRidingBurst()) this.babyTorso.xRot -= 0.08F;
            return;
        }

        this.adultNeck.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.adultNeck.xRot += headPitch * ((float) Math.PI / 180F) * 0.45F;

        this.animateWalk(entity.isRidingBurst() ? GiraffeAnimation.run : GiraffeAnimation.walk, limbSwing, limbSwingAmount, entity.isRidingBurst() ? 1.6F : 1.25F, entity.isRidingBurst() ? 2.8F : 2.0F);
        this.animate(entity.idleState, GiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.eatingState, GiraffeAnimation.eat, ageInTicks, 1.0F);
        this.animate(entity.alertState, GiraffeAnimation.idle, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}