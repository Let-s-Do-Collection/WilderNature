package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.entity.HedgehogEntity;
import net.satisfy.wildernature.entity.animation.HedgehogAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class HedgehogModel<T extends HedgehogEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("hedgehog"), "main");
    private final ModelPart root;

    public HedgehogModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 21.0F, 0.0F));

        PartDefinition hedgehog = root.addOrReplaceChild("hedgehog", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition torso = hedgehog.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.984F, -3.9564F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).mirror().addBox(-1.0F, -0.984F, -5.9564F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.984F, -0.0436F));

        PartDefinition spikes = torso.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.offset(-1.3536F, -4.3375F, 0.5436F));

        PartDefinition spikes_r1 = spikes.addOrReplaceChild("spikes_r1", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition spikes_r2 = spikes.addOrReplaceChild("spikes_r2", CubeListBuilder.create().texOffs(14, 0).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition spikes_r3 = spikes.addOrReplaceChild("spikes_r3", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 3.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition spikes_r4 = spikes.addOrReplaceChild("spikes_r4", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 1.5F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition spikes_r5 = spikes.addOrReplaceChild("spikes_r5", CubeListBuilder.create().texOffs(16, 1).addBox(-0.5F, 0.0F, -2.5F, 1.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 3.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition spikes_r6 = spikes.addOrReplaceChild("spikes_r6", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, -1.0607F, 4.9142F, 0.7854F, 0.0F, 0.0F));

        PartDefinition spikes_r7 = spikes.addOrReplaceChild("spikes_r7", CubeListBuilder.create().texOffs(1, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 1.9393F, 4.9142F, 0.7854F, 0.0F, 0.0F));

        PartDefinition spikes_r8 = spikes.addOrReplaceChild("spikes_r8", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0.0F, -2.0F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1464F, 0.4393F, 4.9142F, 0.7854F, 0.0F, 0.0F));

        PartDefinition spikes_r9 = spikes.addOrReplaceChild("spikes_r9", CubeListBuilder.create().texOffs(15, 0).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 1.5F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition spikes_r10 = spikes.addOrReplaceChild("spikes_r10", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.7071F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition spikes_r11 = spikes.addOrReplaceChild("spikes_r11", CubeListBuilder.create().texOffs(15, 1).addBox(-0.5F, 0.0F, -3.5F, 1.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

        PartDefinition leftEar = torso.addOrReplaceChild("leftEar", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -2.984F, -2.9814F, 0.0F, 0.0F, 0.0873F));

        PartDefinition rightEar = torso.addOrReplaceChild("rightEar", CubeListBuilder.create().texOffs(20, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.984F, -2.9814F, 0.0F, 0.0F, -0.0873F));

        PartDefinition nose = torso.addOrReplaceChild("nose", CubeListBuilder.create(), PartPose.offset(0.0F, -0.509F, -5.4814F));

        PartDefinition rightArm = hedgehog.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, 1.5F, -2.75F));

        PartDefinition leftArm = hedgehog.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 1.5F, -2.75F));

        PartDefinition leftLeg = hedgehog.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, 1.5F, 2.75F));

        PartDefinition rightLeg = hedgehog.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-1.0F, -0.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.75F, 1.5F, 2.75F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animateWalk(HedgehogAnimation.walk, limbSwing, limbSwingAmount, 1f, 2.5f);
        if (entity.getDeltaMovement().multiply(1, 0, 1).length() != 0) {
            this.animate(entity.idleAnimationState, HedgehogAnimation.walk, ageInTicks, 1f);
        } else {
            this.animate(entity.idleAnimationState, HedgehogAnimation.idle, ageInTicks, 1f);
        }
        this.animate(entity.sniffAnimationState, HedgehogAnimation.sniff, ageInTicks, 1f);

    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay,int alpha) {
        root.render(matrices, vertexConsumer, light, overlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}
