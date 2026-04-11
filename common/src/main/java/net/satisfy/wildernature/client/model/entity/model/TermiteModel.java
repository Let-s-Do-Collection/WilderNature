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
import net.satisfy.wildernature.client.model.entity.animation.TermiteAnimation;
import net.satisfy.wildernature.core.entity.monster.TermiteEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TermiteModel<T extends TermiteEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("termite"), "main");

    private final ModelPart base;

    public TermiteModel(ModelPart root) {
        this.base = root.getChild("base");
        ModelPart torso = this.base.getChild("torso");
        ModelPart antennae = torso.getChild("antennae");
        ModelPart rightArm = this.base.getChild("rightArm");
        ModelPart leftArm = this.base.getChild("leftArm");
        ModelPart rightLeg = this.base.getChild("rightLeg");
        ModelPart leftLeg = this.base.getChild("leftLeg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition base = partDefinition.addOrReplaceChild("base", CubeListBuilder.create(), PartPose.offset(0.0F, 21.2F, 1.25F));

        PartDefinition torso = base.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(0, 0).mirror().addBox(-1.5F, -1.55F, -2.25F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(-2, 8).addBox(-1.0F, -1.55F, 1.75F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 6).addBox(-1.0F, -0.55F, -3.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -1.0F));

        torso.addOrReplaceChild("antennae", CubeListBuilder.create()
                .texOffs(-3, 11).addBox(-1.5F, -0.0924F, -3.0383F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.45F, -3.25F, -0.3927F, 0.0F, 0.0F));

        base.addOrReplaceChild("rightArm", CubeListBuilder.create()
                .texOffs(5, 8).mirror().addBox(-1.9826F, -0.0985F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 6).addBox(-1.9826F, -0.0985F, 2.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 2.551F, -2.75F, 0.0F, 0.0F, -0.1745F));

        base.addOrReplaceChild("leftArm", CubeListBuilder.create()
                .texOffs(5, 8).addBox(-1.0174F, -0.0985F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 6).mirror().addBox(-1.0174F, -0.0985F, 2.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, 2.551F, -2.75F, 0.0F, 0.0F, 0.1745F));

        base.addOrReplaceChild("rightLeg", CubeListBuilder.create()
                .texOffs(5, 7).addBox(-2.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 2.451F, -1.5F, 0.0F, 0.0F, -0.1745F));

        base.addOrReplaceChild("leftLeg", CubeListBuilder.create()
                .texOffs(2, 8).mirror().addBox(-1.0174F, -0.0985F, 0.75F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, 2.551F, -2.75F, 0.0F, 0.0F, 0.1745F));

        return LayerDefinition.create(meshDefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        boolean isMoving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-6D;
        boolean isEating = entity.isEating();

        if (isEating) {
            this.animate(entity.eatState, TermiteAnimation.eat, ageInTicks, 1.0F);
        } else if (isMoving) {
            this.animateWalk(TermiteAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.0F);
        } else {
            this.animate(entity.idleState, TermiteAnimation.idle, ageInTicks, 1.0F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.base.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.base;
    }
}