package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.entity.CassowaryEntity;
import net.satisfy.wildernature.entity.animation.CassowaryAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class CassowaryModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("cassowary"), "main");
    private final ModelPart root;

    public CassowaryModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.4063F, -6.3831F, 8.0F, 8.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 15.3134F, 0.4225F, -0.0873F, 0.0F, 0.0F));

        PartDefinition body_r1 = root.addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(29, 2).addBox(-3.0F, -3.5F, 0.95F, 6.0F, 7.425F, 3.05F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.642F, 4.2608F, 0.3927F, 0.0F, 0.0F));

        PartDefinition rightLeg = root.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(28, 21).addBox(-1.0F, 4.1764F, 1.5091F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 9).mirror().addBox(-2.5F, 9.0508F, -1.6653F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(0, 29).addBox(-0.5F, 8.1764F, -1.4909F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -0.3642F, -1.7572F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r1 = rightLeg.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(47, 0).addBox(-1.5F, -3.6173F, -2.5761F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.6764F, 1.5091F, 0.3927F, 0.0F, 0.0F));

        PartDefinition leftLeg = root.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(28, 21).addBox(-1.0F, 4.1764F, 1.5091F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-0.5F, 8.1764F, -1.4909F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(-1, 9).mirror().addBox(-2.5F, 9.0508F, -1.6653F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(3.5F, -0.3642F, -1.7572F, 0.0873F, 0.0F, 0.0F));

        PartDefinition cube_r2 = leftLeg.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(47, 0).mirror().addBox(-1.5F, -3.6173F, -2.5761F, 3.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 1.6764F, 1.5091F, 0.3927F, 0.0F, 0.0F));

        PartDefinition neck = root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 6).addBox(-1.475F, 0.0089F, -5.218F, 2.95F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.4032F, -6.2757F, 0.2182F, 0.0F, 0.0F));

        PartDefinition neck_r1 = neck.addOrReplaceChild("neck_r1", CubeListBuilder.create().texOffs(42, 12).addBox(-1.475F, -2.0F, -3.0F, 2.95F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.6908F, -1.68F, -0.3927F, 0.0F, 0.0F));

        PartDefinition neck_r2 = neck.addOrReplaceChild("neck_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.475F, -1.2396F, -0.8734F, 2.95F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.9788F, -2.2057F, -0.3927F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 21).addBox(-1.5005F, -2.9853F, -4.9234F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(18, 21).addBox(-1.0005F, -1.9853F, -7.9234F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0005F, -5.3249F, 0.1569F));

        PartDefinition horn_r1 = head.addOrReplaceChild("horn_r1", CubeListBuilder.create().texOffs(11, 24).addBox(2.5F, -0.2503F, -0.1064F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0005F, -6.5061F, -3.7557F, -0.3927F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.animate(((CassowaryEntity) entity).idleAnimationState, CassowaryAnimation.idle, ageInTicks, 1f);
        this.animateWalk(CassowaryAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(((CassowaryEntity) entity).attackAnimationState, CassowaryAnimation.attack, ageInTicks, 1f);
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer vertexConsumer, int light, int overlay, int alpha) {
        root.render(matrices, vertexConsumer, light, overlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}
