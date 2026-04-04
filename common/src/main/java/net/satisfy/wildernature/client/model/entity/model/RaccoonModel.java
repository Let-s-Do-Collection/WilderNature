package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.satisfy.wildernature.core.entity.animal.neutral.RaccoonEntity;
import net.satisfy.wildernature.client.model.entity.animation.RaccoonAnimation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class RaccoonModel<T extends RaccoonEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("raccoon"), "main");
    private final ModelPart root;

    public RaccoonModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition raccoon = partDefinition.addOrReplaceChild("raccoon", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, 0.5F));

        raccoon.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(0, 30).addBox(-0.995F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.0F, -4.0F));
        raccoon.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(8, 30).addBox(-1.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.0F, -4.0F));
        raccoon.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(0, 30).addBox(0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 3.0F));
        raccoon.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(8, 30).addBox(-0.005F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 0.0F, 3.0F));

        PartDefinition body = raccoon.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 13).addBox(-4.0F, -5.5F, -3.0F, 8.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 1.5708F, 0.0F, 0.0F));

        body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -4.0F, -6.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(0, 13).addBox(-4.0F, -6.0F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 0).addBox(2.0F, -6.0F, -2.0F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(22, 13).addBox(-2.0F, 0.0F, -9.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.5F, 0.0F, -1.5708F, 0.0F, 0.0F));
        body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(22, 24).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T raccoon, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (raccoon.isSleeping()) {
            this.animate(raccoon.sleepState, RaccoonAnimation.sleep, ageInTicks, 1.0F);
            return;
        }

        if (raccoon.isRaccoonRunning()) {
            this.animateWalk(RaccoonAnimation.run, limbSwing, limbSwingAmount, 1.0F, 2.5F);
        } else {
            this.animateWalk(RaccoonAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        }

        this.animate(raccoon.openDoorState, RaccoonAnimation.open_door, ageInTicks, 1.0F);
        this.animate(raccoon.washingState, RaccoonAnimation.wash, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}