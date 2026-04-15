package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.BeaverAnimation;
import net.satisfy.wildernature.core.entity.animal.passive.BeaverEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BeaverModel<T extends BeaverEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("beaver"), "main");

    private final ModelPart root;
    private final ModelPart head;

    public BeaverModel(ModelPart root) {
        this.root = root.getChild("root");
        ModelPart beaver = this.root.getChild("beaver");
        ModelPart torso = beaver.getChild("torso");
        this.head = torso.getChild("h_head");
        ModelPart tail = torso.getChild("tail");
        ModelPart leftArm = beaver.getChild("leftArm");
        ModelPart rightArm = beaver.getChild("rightArm");
        ModelPart leftLeg = beaver.getChild("leftLeg");
        ModelPart rightLeg = beaver.getChild("rightLeg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition root = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.0F));
        PartDefinition beaver = root.addOrReplaceChild("beaver", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso = beaver.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -6.75F, -10.0F, 9.0F, 7.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.75F, 5.5F));

        PartDefinition head = torso.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(30, 1).addBox(-2.5F, -2.5F, -6.0F, 5.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 6).addBox(-3.5F, -2.5F, -1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 6).mirror().addBox(2.5F, -2.5F, -1.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(19, 26).mirror().addBox(-1.0F, 1.5F, -6.025F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -3.75F, -9.5F));

        head.addOrReplaceChild("head_cube_r1", CubeListBuilder.create().texOffs(0, 9).addBox(-0.5F, -1.5F, -1.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.1464F, 1.0F, -0.9393F, 0.0F, -0.7854F, 0.0F));
        head.addOrReplaceChild("head_cube_r2", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(-1.5F, -1.5F, -1.0F, 2.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.1464F, 1.0F, -0.9393F, 0.0F, 0.7854F, 0.0F));

        torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 19).addBox(-2.5F, -0.5F, -1.5F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.25F, 1.5F));

        beaver.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(19, 19).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 2.0F, -2.5F));
        beaver.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(19, 19).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 2.0F, -2.5F));
        beaver.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(19, 19).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 2.0F, 5.5F));
        beaver.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(19, 19).mirror().addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.5F, 2.0F, 5.5F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.animate(entity.idleState, BeaverAnimation.idle, ageInTicks);
        this.animate(entity.walkState, BeaverAnimation.walk, ageInTicks);
        this.animate(entity.gnawState, BeaverAnimation.chew, ageInTicks);
        this.animate(entity.nodState, BeaverAnimation.nod, ageInTicks);
        
        
        this.animate(entity.sleepState, BeaverAnimation.sleep, ageInTicks);

        if (!entity.isSleeping()) {
            this.head.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.5F;
            this.head.xRot += headPitch * ((float) Math.PI / 180F) * 0.35F;
        }    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}