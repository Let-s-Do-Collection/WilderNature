package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.animal.SquirrelEntity;
import net.satisfy.wildernature.core.entity.animation.SquirrelAnimation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class SquirrelModel extends HierarchicalModel<SquirrelEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("squirrel"), "main");

    private final ModelPart squirrel;
    public final ModelPart head;

    public SquirrelModel(ModelPart root) {
        this.squirrel = root.getChild("squirrel");
        this.head = this.squirrel.getChild("hips").getChild("torso").getChild("h_head");
    }


    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition squirrel = partDefinition.addOrReplaceChild("squirrel", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition hips = squirrel.addOrReplaceChild("hips", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition torso = hips.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -4.5F, -6.0F, 5.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, -0.3927F, 0.0F, 0.0F));

        PartDefinition head = torso.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-2.0F, -2.5F, -4.75F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(19, 0).addBox(-1.5F, -0.5F, -6.75F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.2848F, -5.5426F, 0.3927F, 0.0F, 0.0F));

        head.addOrReplaceChild("h_leftEar", CubeListBuilder.create().texOffs(13, 0).addBox(-0.5F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 1).addBox(1.5F, -4.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -2.0F, -1.25F));

        head.addOrReplaceChild("h_rightEar", CubeListBuilder.create().texOffs(13, 0).mirror().addBox(-1.5F, -3.5F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(0, 1).mirror().addBox(-2.5F, -4.5F, 0.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.5F, -2.0F, -1.25F));

        hips.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 21).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.25F, -1.0F, -4.25F));

        hips.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 21).mirror().addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.25F, -1.0F, -4.25F));

        torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(20, 2).addBox(-1.5F, -1.5F, -0.5F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(14, 11).addBox(-1.5F, -3.5F, 2.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(19, 11).addBox(-1.5F, -10.5F, 2.5F, 3.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 26).addBox(0.0F, -11.5F, 5.5F, 0.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.5F, 1.5F, 0.7854F, 0.0F, 0.0F));

        PartDefinition leftLeg = squirrel.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 9).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -0.5F, 3.5F, 0.7854F, 0.0F, 0.0F));

        leftLeg.addOrReplaceChild("leftFoot", CubeListBuilder.create().texOffs(7, 13).mirror().addBox(-0.975F, 0.0F, -3.0F, 1.95F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, -0.7854F, 0.0F, 0.0F));

        PartDefinition rightLeg = squirrel.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-1.0F, -1.0F, -1.5F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -0.5F, 3.5F, 0.7854F, 0.0F, 0.0F));

        rightLeg.addOrReplaceChild("rightFoot", CubeListBuilder.create().texOffs(7, 13).addBox(-0.975F, 0.0F, -3.0F, 1.95F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.5F, -1.0F, -0.7854F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 48, 48);
    }

    @Override
    public void setupAnim(SquirrelEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD;

        if (entity.isWiggling()) {
            this.animate(entity.wiggleAnimationState, SquirrelAnimation.wiggle, ageInTicks, 1.0F);
            return;
        }

        this.animateWalk(SquirrelAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.idleAnimationState, SquirrelAnimation.idle, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.squirrel.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.squirrel;
    }
}