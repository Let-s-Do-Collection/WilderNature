package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.entity.TurkeyEntity;
import net.satisfy.wildernature.entity.animation.TurkeyAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class TurkeyModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("turkeymodel"), "main");

    private final ModelPart root;

    public TurkeyModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 18.0F, -0.5F));

        PartDefinition animroot = root.addOrReplaceChild("animroot", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition body = animroot.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition tail = animroot.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, -1.4438F, 3.5127F));

        PartDefinition tail_trim_r1 = tail.addOrReplaceChild("tail_trim_r1", CubeListBuilder.create().texOffs(27, 0).addBox(-7.5F, -17.5F, 3.0F, 13.0F, 11.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 12).addBox(-6.0F, -16.0F, 2.0F, 10.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 7.4438F, -3.0127F, -0.0873F, 0.0F, 0.0F));

        PartDefinition right_wing = animroot.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(20, 19).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -0.5F, -2.0F));

        PartDefinition left_wing = animroot.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(20, 19).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -0.5F, -2.0F));

        PartDefinition right_leg = animroot.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(19, 2).addBox(-1.0F, -1.0F, -0.6667F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(4, 2).addBox(-0.5F, 2.0F, 0.3333F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 4).addBox(-1.5F, 4.0F, -1.6667F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 2.0F, -0.8333F));

        PartDefinition left_leg = animroot.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(19, 2).addBox(-1.0F, -1.0F, -0.6667F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 4).addBox(-1.5F, 4.0F, -1.6667F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(4, 2).addBox(-0.5F, 2.0F, 0.3333F, 1.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 2.0F, -0.8333F));

        PartDefinition head = animroot.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 27).addBox(-1.0F, -3.125F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 11).addBox(-2.0F, -7.125F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(6, 23).addBox(-1.0F, -5.125F, -4.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 23).addBox(-1.0F, -3.125F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.875F, -3.0F));

        PartDefinition easteregg = head.addOrReplaceChild("easteregg", CubeListBuilder.create().texOffs(8, 28).addBox(-2.0F, -0.874F, -2.0436F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.1F))
                .texOffs(8, 33).addBox(-2.0F, -2.624F, -2.0436F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.8126F, -0.035F, -0.0436F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        var turkey = ((TurkeyEntity) entity);
        this.root().getAllParts().forEach(ModelPart::resetPose);
        animateWalk(TurkeyAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        animate(turkey.attackAnimationState, TurkeyAnimation.attack, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int k) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, k);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}
