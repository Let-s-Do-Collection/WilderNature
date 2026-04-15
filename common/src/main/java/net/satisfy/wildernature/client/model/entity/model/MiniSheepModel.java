package net.satisfy.wildernature.client.model.entity.model;

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
import net.satisfy.wildernature.client.model.entity.animation.MiniSheepAnimation;
import net.satisfy.wildernature.core.entity.animal.neutral.MiniSheepEntity;
import org.jetbrains.annotations.NotNull;

@Environment(EnvType.CLIENT)
public class MiniSheepModel<T extends MiniSheepEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("minisheep"), "main");

    private final ModelPart miniSheep;
    private final ModelPart head;

    public MiniSheepModel(ModelPart root) {
        this.miniSheep = root.getChild("mini_sheep");
        this.head = this.miniSheep.getChild("head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition miniSheep = partdefinition.addOrReplaceChild("mini_sheep", CubeListBuilder.create(), PartPose.offset(0.0F, 14.0F, 0.0F));
        PartDefinition head = miniSheep.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, -2.24F, -7.04F));
        PartDefinition hHorn = head.addOrReplaceChild("h_horn", CubeListBuilder.create(), PartPose.offset(-0.24F, -2.16F, -0.48F));

        hHorn.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(39, 9).mirror().addBox(-1.44F, -0.6F, -4.32F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(39, 9).addBox(-8.24F, -0.6F, -4.32F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.08F, -2.4F, 0.0F, 0.3927F, 0.0F, 0.0F));

        PartDefinition hEar = head.addOrReplaceChild("h_ear", CubeListBuilder.create(), PartPose.offset(0.48F, -1.68F, -1.92F));
        PartDefinition rightEar = hEar.addOrReplaceChild("right_ear", CubeListBuilder.create(), PartPose.offset(3.84F, 0.0F, 0.0F));
        rightEar.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(28, 27).addBox(-7.22F, -1.48F, -0.04F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.4F, 5.4F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition leftEar = hEar.addOrReplaceChild("left_ear", CubeListBuilder.create(), PartPose.offset(-4.8F, 0.0F, 0.0F));
        leftEar.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(28, 27).mirror().addBox(4.22F, -1.48F, -0.04F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-3.4F, 5.4F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition headMain = head.addOrReplaceChild("h_head2", CubeListBuilder.create().texOffs(0, 27).mirror().addBox(-3.0F, -2.2F, -3.84F, 6.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -1.2F, 0.0F));
        headMain.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(17, 27).mirror().addBox(-2.0F, -4.76F, 0.84F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 7.2F, -4.8F, 0.3927F, 0.0F, 0.0F));

        miniSheep.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-5.28F, -5.8F, -7.62F, 12.0F, 12.0F, 15.0F, new CubeDeformation(0.8F))
                .texOffs(1, 33).addBox(-5.28F, -5.8F, -7.62F, 12.0F, 12.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.72F, -1.0F, 1.12F));

        miniSheep.addOrReplaceChild("right_front_leg", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.08F, -0.2F, -1.16F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.6F, 5.2F, -4.88F));
        miniSheep.addOrReplaceChild("left_front_leg", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.56F, -0.2F, -1.16F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.6F, 5.2F, -4.88F));
        miniSheep.addOrReplaceChild("right_hind_leg", CubeListBuilder.create().texOffs(0, 7).mirror().addBox(-1.08F, -0.2F, -1.2F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(3.6F, 5.2F, 6.12F));
        miniSheep.addOrReplaceChild("left_hind_leg", CubeListBuilder.create().texOffs(0, 7).addBox(-1.56F, -0.2F, -2.2F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.6F, 5.2F, 7.12F));
        miniSheep.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(4, 2).addBox(-0.96F, -0.6F, -1.52F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.12F, 1.84F, 9.76F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(MiniSheepEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        if (entity.isMaehAnimating()) {
            this.animate(entity.maehAnimationState, MiniSheepAnimation.maeh, ageInTicks, 1.0F);
            return;
        }

        if (!entity.isMiniSheepSleeping() && !entity.isMiniSheepRunning()) {
            this.applyHeadRotation(netHeadYaw, headPitch);
        }

        if (entity.isMiniSheepSleeping()) {
            this.animate(entity.sleepAnimationState, MiniSheepAnimation.sleep, ageInTicks, 1.0F);
            return;
        }

        if (entity.isMiniSheepRunning()) {
            this.animate(entity.runAnimationState, MiniSheepAnimation.run, ageInTicks, 1.0F);
            return;
        }

        this.animateWalk(MiniSheepAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.idleAnimationState, MiniSheepAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.eatAnimationState, MiniSheepAnimation.eat, ageInTicks, 1.0F);
    }

    private void applyHeadRotation(float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.clamp(netHeadYaw, -30.0F, 30.0F);
        headPitch = Mth.clamp(headPitch, -25.0F, 45.0F);

        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.miniSheep.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.miniSheep;
    }
}