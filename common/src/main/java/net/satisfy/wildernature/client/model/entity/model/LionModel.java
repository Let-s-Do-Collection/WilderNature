package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.LionAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class LionModel<T extends LionEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("lion"), "main");

    private final ModelPart lion;
    private final ModelPart hHead;
    private final ModelPart male;
    private final ModelPart female;

    public LionModel(ModelPart root) {
        this.lion = root.getChild("lion");
        ModelPart tail = this.lion.getChild("tail");
        ModelPart tail2 = tail.getChild("tail_2");
        ModelPart leftLeg = this.lion.getChild("leftLeg");
        ModelPart rightLeg = this.lion.getChild("rightLeg");
        ModelPart upperBody = this.lion.getChild("upperBody");
        this.hHead = upperBody.getChild("h_head");
        this.male = this.hHead.getChild("male");
        this.female = this.hHead.getChild("female");
        ModelPart leftArm = upperBody.getChild("leftArm");
        ModelPart rightArm = upperBody.getChild("rightArm");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition lion = partDefinition.addOrReplaceChild("lion", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-4.5F, -3.809F, -1.4246F, 9.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 9.9369F, 0.2883F, -0.0436F, 0.0F, 0.0F));

        PartDefinition tail = lion.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(42, 43).mirror().addBox(-1.0F, -1.0532F, -1.5099F, 2.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -2.2994F, 10.3418F, 0.48F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(30, 48).addBox(-1.5F, -0.3534F, -1.5681F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.4247F, -0.249F, 0.6545F, 0.0F, 0.0F));

        lion.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(32, 0).addBox(-2.0F, -1.0057F, -2.2617F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 0.7206F, 7.6562F, 0.0436F, 0.0F, 0.0F));
        lion.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-2.0F, -1.0057F, -2.2617F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.5F, 0.7206F, 7.6562F, 0.0436F, 0.0F, 0.0F));

        PartDefinition upperBody = lion.addOrReplaceChild("upperBody", CubeListBuilder.create().texOffs(3, 18).addBox(-5.5F, -4.8637F, -12.9468F, 11.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0688F, 0.2F, 0.1309F, 0.0F, 0.0F));

        PartDefinition hHead = upperBody.addOrReplaceChild("h_head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -0.1075F, -7.4253F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.2274F, -12.1799F, 0.0436F, 0.0F, 0.0F));

        hHead.addOrReplaceChild("snout_r1", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -2.1449F, -2.4423F, 4.0F, 4.6F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.4198F, -7.894F, 0.3927F, 0.0F, 0.0F));

        hHead.addOrReplaceChild("male", CubeListBuilder.create().texOffs(48, 0).mirror().addBox(-7.0F, -5.6618F, -2.7046F, 14.0F, 13.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(38, 26).addBox(2.0F, -4.6618F, -3.7046F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(38, 26).addBox(-5.0F, -4.6618F, -3.7046F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5543F, -1.7206F));

        hHead.addOrReplaceChild("female", CubeListBuilder.create().texOffs(38, 26).addBox(2.0F, -4.6618F, -1.7046F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(38, 26).addBox(-5.0F, -4.6618F, -1.7046F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5543F, -1.7206F));

        upperBody.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(32, 0).addBox(-3.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5F, 0.3257F, -7.7695F, -0.0873F, 0.0F, 0.0F));
        upperBody.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(32, 0).mirror().addBox(-1.0F, -1.0F, -2.0F, 4.0F, 14.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-5.5F, 0.3257F, -7.7695F, -0.0873F, 0.0F, 0.0F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.male.visible = entity.isMale();
        this.female.visible = !entity.isMale();

        if (!entity.isSleeping() && !entity.isPouncing() && !entity.isWarning()) {
            this.hHead.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
            this.hHead.xRot += headPitch * ((float) Math.PI / 180F) * 0.35F;
        }

        boolean isMoving = limbSwingAmount > 0F;

        if (!isMoving && !entity.isSleeping() && !entity.isStalking() && !entity.isPouncing() && !entity.isWarning()) {
            this.animate(entity.idleState, LionAnimation.idle, ageInTicks, 1.0F);
        }

        if (isMoving && !entity.isSleeping() && !entity.isPouncing() && !entity.isWarning()) {
            this.animateWalk(LionAnimation.walk, limbSwing, limbSwingAmount, entity.isStalking() ? 1.3F : 2.0F, entity.isStalking() ? 1.3F : 2.0F);
        }

        this.animate(entity.sleepState, LionAnimation.sleep, ageInTicks, 1.0F);
        this.animate(entity.warnState, LionAnimation.fear, ageInTicks, 1.0F);
        this.animate(entity.pounceState, LionAnimation.pounce, ageInTicks, 1.0F);
        this.animate(entity.roarState, LionAnimation.bite, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.lion.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.lion;
    }
}