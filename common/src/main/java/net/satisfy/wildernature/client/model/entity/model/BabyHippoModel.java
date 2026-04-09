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
import net.satisfy.wildernature.client.model.entity.animation.BabyHippoAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.HippoEntity;
import org.jetbrains.annotations.NotNull;

public class BabyHippoModel<T extends HippoEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("baby_hippo"), "main");

    private final ModelPart babyHippo;

    public BabyHippoModel(ModelPart root) {
        this.babyHippo = root.getChild("baby_hippo");
        ModelPart hHead = this.babyHippo.getChild("h_head");
        ModelPart jaw = hHead.getChild("jaw");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition babyHippo = partDefinition.addOrReplaceChild("baby_hippo", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 2.0631F, -10.2883F, 12.0F, 12.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.9369F, 0.2883F));

        babyHippo.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(70, 5).mirror().addBox(-1.5F, -2.0185F, -1.343F, 3.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 5.3904F, 9.3431F, 0.3927F, 0.0F, 0.0F));

        PartDefinition hHead = babyHippo.addOrReplaceChild("h_head", CubeListBuilder.create()
                .texOffs(44, 2).mirror().addBox(-7.0F, -10.2465F, -9.8683F, 14.0F, 6.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 32).mirror().addBox(-8.0F, -4.2465F, -11.8683F, 16.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(54, 22).addBox(-6.0F, -6.2465F, -19.8683F, 12.0F, 9.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 7.0222F, -6.8449F, 0.2182F, 0.0F, 0.0F));

        hHead.addOrReplaceChild("ear_cube_r1", CubeListBuilder.create().texOffs(46, 41).addBox(-2.325F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -9.5715F, -2.8683F, 0.0F, 0.0F, -0.3927F));
        hHead.addOrReplaceChild("ear_cube_r2", CubeListBuilder.create().texOffs(46, 41).mirror().addBox(-0.675F, -1.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(7.5F, -9.5715F, -2.8683F, 0.0F, 0.0F, 0.3927F));

        hHead.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(50, 39).addBox(3.0F, -0.9625F, -5.75F, 1.0F, 0.95F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 39).mirror().addBox(-4.0F, -0.9625F, -5.75F, 1.0F, 0.95F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(60, 41).mirror().addBox(-5.0F, -0.0125F, -6.75F, 10.0F, 1.975F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 2.766F, -12.1183F));

        babyHippo.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(80, 2).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 14.0631F, -7.2883F));
        babyHippo.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(80, 2).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 14.0631F, -7.2883F));
        babyHippo.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(80, 2).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, 14.0631F, 6.7117F));
        babyHippo.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(80, 2).mirror().addBox(-2.0F, -2.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.0F, 14.0631F, 6.7117F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.isInWaterOrBubble()) {
            this.animateWalk(BabyHippoAnimation.swim, limbSwing, limbSwingAmount, 1.2F, 2.0F);
        } else {
            this.animateWalk(BabyHippoAnimation.walk, limbSwing, limbSwingAmount, 1.4F, 2.0F);
        }

        this.animate(entity.idleState, BabyHippoAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.biteState, BabyHippoAnimation.bite, ageInTicks, 1.0F);
        this.animate(entity.snapState, BabyHippoAnimation.snap, ageInTicks, 1.0F);
        this.animate(entity.threatState, BabyHippoAnimation.threat, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.babyHippo.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.babyHippo;
    }
}