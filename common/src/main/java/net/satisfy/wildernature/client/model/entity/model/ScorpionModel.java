package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.ScorpionAnimation;
import net.satisfy.wildernature.core.entity.animal.tameable.ScorpionEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class ScorpionModel<T extends ScorpionEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("scorpion"), "main");

    private final ModelPart root;
    private final ModelPart scorpion;
    private final ModelPart tail;

    public ScorpionModel(ModelPart root) {
        this.root = root;
        this.scorpion = root.getChild("scorpion");
        this.tail = this.scorpion.getChild("torso").getChild("tail");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition scorpion = partDefinition.addOrReplaceChild("scorpion", CubeListBuilder.create(), PartPose.offset(0.0F, 23.2F, 0.25F));

        scorpion.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(0, 13).addBox(-2.025F, 1.5121F, -1.55F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -3.2121F, -1.75F));

        PartDefinition torso = scorpion.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0F, -1.7F, -4.25F, 4.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(7, 13).addBox(-1.0F, -0.7F, -5.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(7, 13).mirror().addBox(1.0F, -0.7F, -5.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.3F, 1.75F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail_2", CubeListBuilder.create()
                .texOffs(14, 1).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).mirror().addBox(-0.5F, -4.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, -0.25F, 2.0F));

        tail2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(13, 4).mirror().addBox(-0.5F, 0.0F, -0.5F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -3.8077F, 2.5384F, 0.3927F, 0.0F, 0.0F));

        torso.addOrReplaceChild("rightPincer", CubeListBuilder.create()
                .texOffs(5, 9).mirror().addBox(-2.0F, -0.5F, -2.6982F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(5, 8).addBox(-1.5F, -0.5F, -3.6982F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(5, 8).addBox(-1.5F, 0.5F, -3.6982F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -0.7F, -3.5518F));

        torso.addOrReplaceChild("leftPincer", CubeListBuilder.create()
                .texOffs(5, 9).addBox(0.0F, -0.5F, -2.6982F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(5, 8).mirror().addBox(0.5F, -0.5F, -3.6982F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(5, 8).mirror().addBox(0.5F, 0.5F, -3.6982F, 1.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(2.0F, -0.7F, -3.5518F));

        scorpion.addOrReplaceChild("rightLeg_1", CubeListBuilder.create()
                .texOffs(11, 10).addBox(-1.7891F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 7).addBox(-1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0006F, -0.1992F, -1.5F, 0.0F, 0.0F, 0.3927F));

        scorpion.addOrReplaceChild("leftLeg_1", CubeListBuilder.create()
                .texOffs(11, 10).mirror().addBox(-0.2108F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 7).addBox(1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0006F, -0.1992F, -1.5F, 0.0F, 0.0F, -0.3927F));

        scorpion.addOrReplaceChild("rightLeg_2", CubeListBuilder.create()
                .texOffs(11, 10).addBox(-1.7891F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 7).addBox(-1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0006F, -0.1992F, 0.25F, 0.0F, 0.0F, 0.3927F));

        scorpion.addOrReplaceChild("leftLeg_2", CubeListBuilder.create()
                .texOffs(11, 10).mirror().addBox(-0.2108F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 7).addBox(1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0006F, -0.1992F, 0.25F, 0.0F, 0.0F, -0.3927F));

        scorpion.addOrReplaceChild("rightLeg_3", CubeListBuilder.create()
                .texOffs(11, 10).addBox(-1.7891F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(12, 7).addBox(-1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0006F, -0.1992F, 2.0F, 0.0F, 0.0F, 0.3927F));

        scorpion.addOrReplaceChild("leftLeg_3", CubeListBuilder.create()
                .texOffs(11, 10).mirror().addBox(-0.2108F, -0.1343F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(12, 7).addBox(1.7891F, -0.1343F, -0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0006F, -0.1992F, 2.0F, 0.0F, 0.0F, -0.3927F));

        return LayerDefinition.create(meshDefinition, 24, 24);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.idleState, ScorpionAnimation.idle, ageInTicks, 1.0F);
        this.animateWalk(ScorpionAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.0F);
        this.animate(entity.attackState, ScorpionAnimation.attack, ageInTicks, 1.0F);
        this.animate(entity.burrowState, ScorpionAnimation.bury, ageInTicks, 1.0F);

        if (entity.isOrderedToSit()) {
            this.scorpion.y += 2.0F;
        }

        if (entity.isCalmed()) {
            this.tail.xRot += 0.6F;
        }
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