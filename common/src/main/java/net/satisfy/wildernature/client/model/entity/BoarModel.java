package net.satisfy.wildernature.client.model.entity;

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
import net.minecraft.util.Mth;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.BoarEntity;
import net.satisfy.wildernature.core.entity.animation.BoarAnimation;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class BoarModel<T extends BoarEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("boar"), "main");

    private final ModelPart boar;
    private final ModelPart head;

    public BoarModel(ModelPart root) {
        this.boar = root.getChild("boar");
        this.head = this.boar.getChild("body").getChild("head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition boar = partdefinition.addOrReplaceChild("boar", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = boar.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(0.0F, -14.7393F, -13.0868F, 0.0F, 10.0F, 19.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.2607F, 3.0868F));
        body.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -3.0F, -10.0F, 16.0F, 11.0F, 20.0F, new CubeDeformation(0.02F)), PartPose.offsetAndRotation(0.0F, -2.7393F, -0.0868F, -0.1309F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.7393F, -8.0868F, 0.8727F, 0.0F, 0.0F));
        head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 0).addBox(3.0F, -5.6134F, -4.8137F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(7, 8).addBox(-5.0F, -5.6134F, -4.8137F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(20, 32).addBox(-5.0F, -4.6134F, -1.8137F, 10.0F, 7.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(64, 48).addBox(-4.0F, -2.6134F, -5.8137F, 8.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.7673F, -9.4924F, -0.3054F, 0.0F, 0.0F));

        PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offsetAndRotation(-7.0F, -5.0F, -2.0F, 0.0F, 0.0F, -0.8727F));
        rightEar.addOrReplaceChild("right_ear_r1", CubeListBuilder.create().texOffs(64, 64).addBox(-5.9654F, -6.3277F, 2.1863F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0231F, 7.4403F, -7.4924F, -0.2F, -0.2324F, 0.0234F));

        PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offsetAndRotation(7.0F, -5.0F, -2.0F, 0.0F, 0.0F, 0.8727F));
        leftEar.addOrReplaceChild("left_ear_r1", CubeListBuilder.create().texOffs(64, 58).addBox(-0.0346F, -6.3277F, 2.1863F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0231F, 7.4403F, -7.4924F, -0.2F, 0.2324F, -0.0234F));

        boar.addOrReplaceChild("legfrontleft", CubeListBuilder.create().texOffs(39, 50).addBox(-4.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -12.0F, -3.0F));
        boar.addOrReplaceChild("legbackright", CubeListBuilder.create().texOffs(0, 62).addBox(-14.0F, -2.0F, -2.0F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -8.0F, 9.0F));
        boar.addOrReplaceChild("legbackleft", CubeListBuilder.create().texOffs(61, 32).addBox(9.0F, -2.0F, -3.0F, 5.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -8.0F, 10.0F));
        boar.addOrReplaceChild("legfrontright", CubeListBuilder.create().texOffs(53, 0).addBox(-2.0F, -1.0F, -3.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.0F, -12.0F, -3.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.head.yRot = netHeadYaw * 0.017453292F;

        if (entity.getDenyAnimationTick() > 0) {
            float progress = (float) entity.getDenyAnimationTick() / 16.0F;
            float shake = Mth.sin((16.0F - entity.getDenyAnimationTick()) * 1.6F) * 0.65F;
            this.head.yRot += shake * Math.min(1.0F, progress * 2.0F);
        }

        if (entity.isSleeping()) {
            this.animate(entity.sleepAnimationState, BoarAnimation.idle, ageInTicks, 1.0F);
            return;
        }

        if (entity.isDigging()) {
            this.animate(entity.diggingAnimationState, BoarAnimation.dig, ageInTicks, 1.0F);
        } else {
            this.animate(entity.idleAnimationState, BoarAnimation.idle, ageInTicks, 1.0F);
            this.animateWalk(BoarAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.boar.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.boar;
    }
}