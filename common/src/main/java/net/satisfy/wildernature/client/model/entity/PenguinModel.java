package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.entity.PenguinEntity;
import net.satisfy.wildernature.entity.animation.PenguinAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class PenguinModel<T extends PenguinEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("penguin"), "main");
    private final ModelPart root;

    public PenguinModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -11.8566F, -10.4853F, 9.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 22.8566F, 6.4853F));

        PartDefinition cube_r1 = root.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 20).addBox(-3.0F, -2.625F, -1.1F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.1434F, -2.4853F, -0.3927F, 0.0F, 0.0F));

        PartDefinition left_wing = root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 20).addBox(0.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, -10.8566F, -6.4853F));

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(33, 19).mirror().addBox(-3.5F, -4.8333F, -3.6667F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.25F)).mirror(false)
                .texOffs(0, 0).addBox(-1.0F, -1.8333F, -5.6667F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 0).addBox(-3.5F, -4.8333F, -3.6667F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0232F, -6.3186F));

        PartDefinition right_wing = root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -10.8566F, -6.4853F));

        PartDefinition right_foot = root.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(34, 12).addBox(-1.5F, 0.0F, -5.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.1434F, -6.4853F));

        PartDefinition left_foot = root.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(34, 12).addBox(-1.5F, 0.0F, -5.0F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.1434F, -6.4853F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.animateWalk(PenguinAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(entity.idleAnimationState, PenguinAnimation.idle, ageInTicks, 1f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return root;
    }
}