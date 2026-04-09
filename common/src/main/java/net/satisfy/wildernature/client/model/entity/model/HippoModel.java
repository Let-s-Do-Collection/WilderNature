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
import net.satisfy.wildernature.client.model.entity.animation.HippoAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.HippoEntity;
import org.jetbrains.annotations.NotNull;

public class HippoModel<T extends HippoEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("hippo"), "main");

    private final ModelPart hippo;
    private final ModelPart tail;
    private final ModelPart hHead;
    private final ModelPart jaw;
    private final ModelPart leftArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;

    public HippoModel(ModelPart root) {
        this.hippo = root.getChild("hippo");
        this.tail = this.hippo.getChild("tail");
        this.hHead = this.hippo.getChild("h_head");
        this.jaw = this.hHead.getChild("jaw");
        this.leftArm = this.hippo.getChild("leftArm");
        this.leftLeg = this.hippo.getChild("leftLeg");
        this.rightLeg = this.hippo.getChild("rightLeg");
        this.rightArm = this.hippo.getChild("rightArm");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition hippo = partDefinition.addOrReplaceChild("hippo", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -6.9369F, -24.2883F, 22.0F, 22.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0631F, 4.2883F));

        hippo.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(26, 7).addBox(-2.0F, -1.4614F, -1.5493F, 4.0F, 11.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.1361F, 14.7082F, 0.3927F, 0.0F, 0.0F));

        PartDefinition hHead = hippo.addOrReplaceChild("h_head", CubeListBuilder.create()
                .texOffs(68, 62).addBox(-8.0F, -8.7856F, -10.8044F, 16.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(2, 35).mirror().addBox(-10.0F, -9.7856F, -3.8044F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(2, 35).addBox(7.0F, -9.7856F, -3.8044F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 62).addBox(-9.0F, -2.7856F, -12.8044F, 18.0F, 12.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 90).mirror().addBox(-7.0F, -4.7856F, -22.8044F, 14.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 3.6635F, -23.3062F, 0.2182F, 0.0F, 0.0F));

        hHead.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(6, 31).addBox(-5.0F, -2.95F, -7.75F, 1.0F, 1.95F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(6, 31).mirror().addBox(4.0F, -2.95F, -7.75F, 1.0F, 1.95F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(40, 91).addBox(-6.0F, -1.0F, -8.75F, 12.0F, 1.975F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.2144F, -12.0544F));

        hippo.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(12, 21).mirror().addBox(-3.5F, -2.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.5F, 15.0631F, -19.7883F));
        hippo.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(12, 21).mirror().addBox(-3.5F, -2.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(6.5F, 15.0631F, 11.2117F));
        hippo.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(12, 21).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5F, 15.0631F, 11.2117F));
        hippo.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(12, 21).addBox(-3.5F, -2.0F, -3.5F, 7.0F, 12.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5F, 15.0631F, -19.7883F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.hHead.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.hHead.xRot += headPitch * ((float) Math.PI / 180F) * 0.45F;

        if (entity.isInWaterOrBubble()) {
            this.animateWalk(HippoAnimation.swim, limbSwing, limbSwingAmount, 1.2F, 2.0F);
        } else {
            this.animateWalk(HippoAnimation.walk, limbSwing, limbSwingAmount, 1.4F, 2.0F);
        }

        this.animate(entity.idleState, HippoAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.biteState, HippoAnimation.bite, ageInTicks, 1.0F);
        this.animate(entity.snapState, HippoAnimation.snap, ageInTicks, 1.0F);
        this.animate(entity.threatState, HippoAnimation.threat, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.hippo.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.hippo;
    }

    public ModelPart getRenderBody() {
        return this.hippo;
    }

    public ModelPart getRenderHead() {
        return this.hHead;
    }

    public ModelPart getRenderJaw() {
        return this.jaw;
    }
}