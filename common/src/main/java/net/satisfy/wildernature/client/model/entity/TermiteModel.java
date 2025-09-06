package net.satisfy.wildernature.client.model.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.TermiteEntity;
import net.satisfy.wildernature.core.entity.animation.TermiteAnimation;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TermiteModel extends HierarchicalModel<TermiteEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("termite"), "main");
    private final ModelPart termite;

    public TermiteModel(ModelPart root) {
        this.termite = root.getChild("termite");
        ModelPart torso = this.termite.getChild("torso");
        ModelPart antennae = torso.getChild("antennae");
        ModelPart rightArm = this.termite.getChild("rightArm");
        ModelPart leftArm = this.termite.getChild("leftArm");
        ModelPart rightLeg = this.termite.getChild("rightLeg");
        ModelPart leftLeg = this.termite.getChild("leftLeg");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition termite = partdefinition.addOrReplaceChild("termite", CubeListBuilder.create(), PartPose.offset(0.0F, 21.2F, 1.25F));

        PartDefinition torso = termite.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(-1.5F, -1.55F, -2.25F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 6).addBox(-1.0F, -0.55F, -3.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.0F, -1.0F));

        PartDefinition antennae = torso.addOrReplaceChild("antennae", CubeListBuilder.create().texOffs(-3, 11).addBox(-1.5F, -0.0924F, -3.0383F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.45F, -3.25F, -0.3927F, 0.0F, 0.0F));

        PartDefinition rightArm = termite.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 10).mirror().addBox(-1.9826F, -0.0985F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(3, 6).addBox(-1.9826F, -0.0985F, 2.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 2.551F, -2.75F, 0.0F, 0.0F, -0.1745F));

        PartDefinition leftArm = termite.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 10).addBox(-1.0174F, -0.0985F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(3, 6).mirror().addBox(-1.0174F, -0.0985F, 2.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, 2.551F, -2.75F, 0.0F, 0.0F, 0.1745F));

        PartDefinition rightLeg = termite.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(1, 10).addBox(-2.0F, 0.0F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 2.451F, -1.5F, 0.0F, 0.0F, -0.1745F));

        PartDefinition leftLeg = termite.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(1, 10).mirror().addBox(-1.0174F, -0.0985F, 0.75F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.5F, 2.551F, -2.75F, 0.0F, 0.0F, 0.1745F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public @NotNull ModelPart root() {
        return termite;
    }

    @Override
    public void setupAnim(TermiteEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        float move = (float) entity.getDeltaMovement().horizontalDistance();
        float amount = Math.min(1.0f, limbSwingAmount * 2.2f + move * 6.0f);
        this.animateWalk(TermiteAnimation.WALK, limbSwing, amount, 2.6f, 2.0f);
        this.animate(entity.idleAnimationState, TermiteAnimation.IDLE, ageInTicks, 1f);
    }

}
