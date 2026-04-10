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
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.BabyLionAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BabyLionModel<T extends LionEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("baby_lion"), "main");

    private final ModelPart base;
    private final ModelPart hHead;

    public BabyLionModel(ModelPart root) {
        this.base = root.getChild("base");
        this.hHead = this.base.getChild("h_head");
        ModelPart tail = this.base.getChild("tail");
        ModelPart tail2 = tail.getChild("tail_2");
        ModelPart rightLeg = this.base.getChild("rightLeg");
        ModelPart leftLeg = this.base.getChild("leftLeg");
        ModelPart rightArm = this.base.getChild("rightArm");
        ModelPart leftArm = this.base.getChild("leftArm");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition base = partDefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.758F, -6.2883F, 6.0F, 7.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.9369F, 0.2883F));

        PartDefinition hHead = base.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0478F, -4.0722F, -6.3431F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0478F, -0.7413F, -6.7067F, 0.1745F, 0.0F, 0.0F));

        hHead.addOrReplaceChild("snout_r1", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, -1.4979F, -1.7169F, 4.0F, 4.6F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0478F, 1.1371F, -7.7546F, 0.3927F, 0.0F, 0.0F));
        hHead.addOrReplaceChild("ears_r1", CubeListBuilder.create().texOffs(0, 2).addBox(-0.5981F, -1.1652F, -0.6736F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9522F, -5.132F, -1.6695F, 0.0F, 0.0F, 0.3927F));
        hHead.addOrReplaceChild("ears_r2", CubeListBuilder.create().texOffs(0, 2).mirror().addBox(-2.4019F, -1.1652F, -0.6736F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0478F, -5.132F, -1.6695F, 0.0F, 0.0F, -0.3927F));

        PartDefinition tail = base.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 7).addBox(-0.5F, 0.1986F, -1.2762F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.9244F, 6.5668F, 0.6545F, 0.0F, 0.0F));
        tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(4, 8).addBox(-1.0F, -0.1606F, -1.9183F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.3261F, 0.0237F, 0.6545F, 0.0F, 0.0F));

        base.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(25, 3).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0284F, 4.3338F));
        base.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(25, 3).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0284F, 4.3338F));
        base.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(25, 3).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.5F, 0.0284F, -3.6662F));
        base.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(25, 3).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 0.0284F, -3.6662F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (!entity.isSleeping()) {
            this.hHead.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
            this.hHead.xRot += headPitch * ((float) Math.PI / 180F) * 0.35F;
        }

        boolean isMoving = limbSwingAmount > 0.05F;

        if (!isMoving && !entity.isSleeping() && !entity.isPouncing() && !entity.isWarning()) {
            this.animate(entity.idleState, BabyLionAnimation.idle, ageInTicks, 1.0F);
        }

        if (isMoving && !entity.isSleeping() && !entity.isPouncing() && !entity.isWarning()) {
            this.animateWalk(BabyLionAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.0F);
        }

        this.animate(entity.sleepState, BabyLionAnimation.sleep, ageInTicks, 1.0F);
        this.animate(entity.warnState, BabyLionAnimation.tail_wave, ageInTicks, 1.0F);
        this.animate(entity.roarState, BabyLionAnimation.tail_wave, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.base;
    }

    public ModelPart getRenderBody() {
        return this.base;
    }

    public ModelPart getRenderHead() {
        return this.hHead;
    }
}