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
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.entity.FlamingoEntity;
import net.satisfy.wildernature.entity.animation.FlamingoAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class FlamingoModel<T extends FlamingoEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("flamingo"), "main");
    private final ModelPart root;

    public FlamingoModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 12.3172F, 0.5097F, -0.0873F, 0.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -6.5F, -6.25F, 7.0F, 7.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0937F, 0.8669F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0178F, 3.8536F));

        PartDefinition body_r1 = tail.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(36, 9).addBox(-2.5F, 0.5F, -0.5F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.432F, 0.8964F, -0.7854F, 0.0F, 0.0F));

        PartDefinition leftWing = body.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, 0.0F, -4.5F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(11, 19).addBox(0.0F, 1.0F, 4.5F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -6.5F, -0.75F, 0.0F, 0.0F, -0.3491F));

        PartDefinition rightWing = body.addOrReplaceChild("rightWing", CubeListBuilder.create().texOffs(0, 18).addBox(0.0F, 0.0F, -4.5F, 1.0F, 6.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(11, 19).addBox(0.0F, 1.0F, 4.5F, 0.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -6.5F, -0.75F, 0.0F, 0.0F, 0.3491F));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.4641F, -5.9365F, 0.2182F, 0.0F, 0.0F));

        PartDefinition neck_r1 = neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(25, 3).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.1517F, -3.0128F, -0.3927F, 0.0F, 0.0F));

        PartDefinition neck_r2 = neck.addOrReplaceChild("neck_r2", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0F, -5.2396F, 0.1266F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.4397F, -3.5385F, -0.3927F, 0.0F, 0.0F));

        PartDefinition h_head = neck.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(35, 0).addBox(-3.0005F, -4.0308F, -4.8927F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(25, 0).addBox(-2.0005F, -2.0308F, -5.8927F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-2.0005F, -2.0308F, -7.8927F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0005F, -8.4359F, 0.3241F));

        PartDefinition rightLeg = root.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(20, 18).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(22, 18).addBox(-0.525F, 5.5F, -0.5F, 1.05F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, 1.0909F, 0.8013F, 0.0873F, 0.0F, 0.0F));

        PartDefinition rightLeg_lower = rightLeg.addOrReplaceChild("rightLeg_lower", CubeListBuilder.create().texOffs(20, 24).addBox(-0.5F, -0.725F, 0.0F, 1.0F, 4.975F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.25F, 0.0F));

        PartDefinition rightFoot = rightLeg_lower.addOrReplaceChild("rightFoot", CubeListBuilder.create().texOffs(8, 18).addBox(-1.5F, 0.0F, -3.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.25F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(20, 18).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(22, 18).addBox(-0.525F, 5.5F, -0.5F, 1.05F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, 1.0909F, 0.8013F, 0.0873F, 0.0F, 0.0F));

        PartDefinition leftLeg_lower = leftLeg.addOrReplaceChild("leftLeg_lower", CubeListBuilder.create().texOffs(20, 24).addBox(-0.5F, -0.725F, 0.0F, 1.0F, 4.975F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.25F, 0.0F));

        PartDefinition leftFoot = leftLeg_lower.addOrReplaceChild("leftFoot", CubeListBuilder.create().texOffs(8, 18).mirror().addBox(-1.5F, 0.0F, -3.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 4.25F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int k) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, k);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.idleAnimationState, FlamingoAnimation.idle, ageInTicks, 1f);
        this.animateWalk(FlamingoAnimation.walk, limbSwing, limbSwingAmount, 3f, 3f);
        this.animate(entity.standAnimationState, FlamingoAnimation.pose, ageInTicks, 1f);
    }
}