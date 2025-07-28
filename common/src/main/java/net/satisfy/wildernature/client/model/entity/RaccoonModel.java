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
import net.satisfy.wildernature.entity.RaccoonEntity;
import net.satisfy.wildernature.entity.animation.RaccoonAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RaccoonModel<T extends RaccoonEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("raccoon"), "main");
    private final ModelPart root;

    public RaccoonModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.5F));

        PartDefinition animroot = root.addOrReplaceChild("animroot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = animroot.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -5.5F, -3.0F, 8.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-4.0F, -6.0F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(2.0F, -6.0F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(22, 13).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 24).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.0F));

        PartDefinition leftHindLeg = animroot.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(8, 30).addBox(-0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 3.0F));

        PartDefinition rightHindLeg = animroot.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(0, 30).addBox(0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 3.0F));

        PartDefinition leftFrontLeg = animroot.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(8, 30).addBox(-1.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.0F, -4.0F));

        PartDefinition rightFrontLeg = animroot.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(0, 30).addBox(-0.995F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.0F, -4.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    public void setupAnim(T raccoon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        if (raccoon.isRaccoonRunning()) {
            this.animateWalk(RaccoonAnimation.run, limbSwing, limbSwingAmount, 1f, 2.5f);
        } else {
            this.animateWalk(RaccoonAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        }
        this.animate(raccoon.openDoorState, RaccoonAnimation.opening_door, ageInTicks, 1.0f);
        this.animate(raccoon.washingState, RaccoonAnimation.wash, ageInTicks, 1.0f);
    }


    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}