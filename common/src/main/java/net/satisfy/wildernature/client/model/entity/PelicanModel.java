package net.satisfy.wildernature.client.model.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.entity.PelicanEntity;
import net.satisfy.wildernature.entity.animation.PelicanAnimation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

public class PelicanModel<T extends Entity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("pelican"), "main");
    private final ModelPart root;

    public PelicanModel(ModelPart root) {
        this.root = root;
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();

        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(17, 1)
                        .addBox(-3.0F, -4.0F, -4.0F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(18, 18).addBox(0.0F, -2.0F, 4.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2.0F, -9.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -4.0F, 1.0F, -1.5708F, 0.0F, 0.0F));

        head.addOrReplaceChild("comb", CubeListBuilder.create().texOffs(1, 30)
                        .addBox(-2.0F, 0.0F, -8.0F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.0F, -2.0F));

        head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(1, 22)
                        .addBox(-2.0F, -0.5F, -8.0F, 4.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -5.5F, -2.0F));

        body.addOrReplaceChild("wing0", CubeListBuilder.create().texOffs(26, 21)
                        .addBox(-1.0F, -1.0F, -3.0F, 1.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(-3.0F, 0.0F, 3.0F, -1.5708F, 0.0F, 0.0F));

        body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(26, 21)
                        .addBox(0.0F, -1.0F, -3.0F, 1.0F, 7.0F, 9.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(3.0F, 0.0F, 3.0F, -1.5708F, 0.0F, 0.0F));

        root.addOrReplaceChild("leg0", CubeListBuilder.create().texOffs(6, 45)
                        .addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.0F, -5.0F, 1.0F));

        root.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(6, 45)
                        .addBox(-1.0F, 0.0F, -3.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(1.0F, -5.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);

        this.animateWalk(PelicanAnimation.walk, limbSwing, limbSwingAmount, 2f, 2.5f);
        this.animate(((PelicanEntity) entity).attackAnimationState, PelicanAnimation.attack, ageInTicks, 1f);
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
