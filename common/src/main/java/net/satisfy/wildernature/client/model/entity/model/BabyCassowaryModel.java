package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.BabyCassowaryAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.CassowaryEntity;
import org.jetbrains.annotations.NotNull;

public class BabyCassowaryModel<T extends CassowaryEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("baby_cassowary"), "main");

    private final ModelPart cassowary;
    private final ModelPart neck;
    private final ModelPart head;

    public BabyCassowaryModel(ModelPart root) {
        this.cassowary = root.getChild("cassowary");
        this.neck = this.cassowary.getChild("neck");
        this.head = this.neck.getChild("h_head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition cassowary = partDefinition.addOrReplaceChild("cassowary", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 2.0956F, -3.4267F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.3134F, 0.4225F, -0.0873F, 0.0F, 0.0F));

        cassowary.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(15, 0).addBox(-1.5F, -4.6715F, 2.3167F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.358F, 1.2608F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rightLeg = cassowary.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(-1, 6).mirror().addBox(-1.0F, 3.8703F, -0.716F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(22, 10).addBox(-0.5F, 1.8953F, 0.284F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 4.7919F, -0.1201F, 0.0873F, 0.0F, 0.0F));
        rightLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-1.0F, -2.2119F, -1.2587F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.3953F, 0.784F, 0.3927F, 0.0F, 0.0F));

        PartDefinition leftLeg = cassowary.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(-1, 6).addBox(-1.0F, 3.8703F, -0.716F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(22, 10).addBox(-0.5F, 1.8953F, 0.284F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 4.7919F, -0.1201F, 0.0873F, 0.0F, 0.0F));
        leftLeg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -2.2119F, -1.2587F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.3953F, 0.784F, 0.3927F, 0.0F, 0.0F));

        PartDefinition neck = cassowary.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 4.123F, -3.2402F, 0.2182F, 0.0F, 0.0F));
        neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(18, 7).addBox(-0.975F, -3.483F, -3.1294F, 1.95F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.2854F, -0.8397F, -0.3927F, 0.0F, 0.0F));
        neck.addOrReplaceChild("neck_r2", CubeListBuilder.create().texOffs(24, 13).mirror().addBox(-0.975F, -1.7225F, -1.0028F, 1.95F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.0026F, -1.3654F, -0.3927F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(0, 11).addBox(-1.5F, -2.9181F, -3.982F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(10, 11).addBox(-1.0F, -1.9181F, -5.982F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.2943F, 0.1971F));
        head.addOrReplaceChild("horn_r1", CubeListBuilder.create().texOffs(13, 12).addBox(2.5F, 3.2917F, -0.2358F, 1.0F, 1.8F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -6.0125F, -2.8509F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.cassowary.getAllParts().forEach(ModelPart::resetPose);

        boolean isMoving = limbSwingAmount > 0F;

        if (!isMoving && !entity.isAlert() && !entity.isThreatening()) {
            this.animate(entity.idleAnimationState, BabyCassowaryAnimation.idle, ageInTicks, 1.0F);
        }

        if (isMoving) {
            this.animateWalk(BabyCassowaryAnimation.walk, limbSwing, limbSwingAmount, 0.75F, 2.5F);
        }

        this.animate(entity.alertAnimationState, BabyCassowaryAnimation.play_growl, ageInTicks, 1.0F);
        this.animate(entity.threateningAnimationState, BabyCassowaryAnimation.play_growl, ageInTicks, 1.0F);

        if (!entity.isAlert() && !entity.isThreatening()) {
            this.neck.yRot += netHeadYaw * 0.017453292F * 0.5F;
            this.head.yRot += netHeadYaw * 0.017453292F * 0.5F;
            this.head.xRot += headPitch * 0.017453292F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.cassowary.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.cassowary;
    }
}