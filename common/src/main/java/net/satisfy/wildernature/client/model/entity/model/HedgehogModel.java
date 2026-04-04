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
import net.satisfy.wildernature.core.entity.animal.passive.HedgehogEntity;
import net.satisfy.wildernature.client.model.entity.animation.HedgehogAnimation;
import org.jetbrains.annotations.NotNull;

public class HedgehogModel<T extends HedgehogEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("hedgehog"), "main");

    private final ModelPart root;
    private final ModelPart hedgehog;
    private final ModelPart curled;
    private final ModelPart torso;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public HedgehogModel(ModelPart root) {
        this.root = root;
        this.hedgehog = root.getChild("hedgehog");
        this.curled = this.hedgehog.getChild("curled");
        this.torso = this.hedgehog.getChild("torso");
        this.rightArm = this.hedgehog.getChild("rightArm");
        this.leftArm = this.hedgehog.getChild("leftArm");
        this.leftLeg = this.hedgehog.getChild("leftLeg");
        this.rightLeg = this.hedgehog.getChild("rightLeg");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition hedgehog = partdefinition.addOrReplaceChild("hedgehog", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition curled = hedgehog.addOrReplaceChild("curled", CubeListBuilder.create().texOffs(0, 15).addBox(-3.0F, -6.0F, -4.0F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition spikes2 = curled.addOrReplaceChild("spikes2", CubeListBuilder.create(), PartPose.offset(-1.3536F, -6.3536F, 0.5F));

        spikes2.addOrReplaceChild("spikes_r1", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes2.addOrReplaceChild("spikes_r2", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes2.addOrReplaceChild("spikes_r3", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 3.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes2.addOrReplaceChild("spikes_r4", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 1.5F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes2.addOrReplaceChild("spikes_r5", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes2.addOrReplaceChild("spikes_r6", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, -1.0607F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes2.addOrReplaceChild("spikes_r7", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 1.9393F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes2.addOrReplaceChild("spikes_r8", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 0.4393F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes2.addOrReplaceChild("spikes_r9", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes2.addOrReplaceChild("spikes_r10", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes2.addOrReplaceChild("spikes_r11", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition torso = hedgehog.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.984F, -3.9564F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-1.0F, -0.984F, -5.9564F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.984F, -0.0436F));

        PartDefinition spikes = torso.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.offset(-1.3536F, -4.3375F, 0.5436F));

        spikes.addOrReplaceChild("spikes_r12", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes.addOrReplaceChild("spikes_r13", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes.addOrReplaceChild("spikes_r14", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 3.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes.addOrReplaceChild("spikes_r15", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 1.5F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes.addOrReplaceChild("spikes_r16", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes.addOrReplaceChild("spikes_r17", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, -1.0607F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes.addOrReplaceChild("spikes_r18", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 1.9393F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes.addOrReplaceChild("spikes_r19", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 0.4393F, 4.9142F, 0.7854F, 0.0F, 0.0F));
        spikes.addOrReplaceChild("spikes_r20", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        spikes.addOrReplaceChild("spikes_r21", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        spikes.addOrReplaceChild("spikes_r22", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        torso.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.984F, -2.9814F, 0.0F, 0.0F, 0.0873F));
        torso.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.984F, -2.9814F, 0.0F, 0.0F, -0.0873F));
        torso.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.offset(0.0F, -0.509F, -5.4814F));

        hedgehog.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 1.5F, -2.75F));
        hedgehog.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 1.5F, -2.75F));
        hedgehog.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 1.5F, 2.75F));
        hedgehog.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 1.5F, 2.75F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isCurled = entity.isCurled();

        this.curled.visible = isCurled;
        this.torso.visible = !isCurled;
        this.rightArm.visible = !isCurled;
        this.leftArm.visible = !isCurled;
        this.leftLeg.visible = !isCurled;
        this.rightLeg.visible = !isCurled;

        if (isCurled) {
            return;
        }

        this.animateWalk(HedgehogAnimation.walk, limbSwing, limbSwingAmount, 4.0F, 2.0F);

        if (entity.getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).lengthSqr() > 1.0E-6D) {
            this.animate(entity.idleAnimationState, HedgehogAnimation.walk, ageInTicks, 1.0F);
        } else {
            this.animate(entity.idleAnimationState, HedgehogAnimation.idle, ageInTicks, 1.0F);
        }

        this.animate(entity.sniffAnimationState, HedgehogAnimation.sniff, ageInTicks, 1.0F);
        this.animate(entity.noAnimationState, HedgehogAnimation.no, ageInTicks, 1.0F);
        this.animate(entity.sleepAnimationState, HedgehogAnimation.sleep, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int alpha) {
        this.root.render(matrices, vertexConsumer, light, overlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}