package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.SwiftFoxAnimation;
import net.satisfy.wildernature.core.entity.animal.neutral.SwiftFoxEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public class SwiftFoxModel<T extends SwiftFoxEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("swift_fox"), "main");

    private final ModelPart root;

    public SwiftFoxModel(ModelPart root) {
        this.root = root;
        ModelPart swiftFox = root.getChild("swift_fox");
        ModelPart head = swiftFox.getChild("body").getChild("head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition swiftFox = partDefinition.addOrReplaceChild("swift_fox", CubeListBuilder.create(), PartPose.offset(1.0F, 16.0F, 8.0F));

        swiftFox.addOrReplaceChild("rightFrontLeg", CubeListBuilder.create().texOffs(26, 33).addBox(0.0F, 0.0F, -0.99F, 2.0F, 8.0F, 2.01F), PartPose.offset(-0.5F, 0.0F, -12.0F));
        swiftFox.addOrReplaceChild("leftFrontLeg", CubeListBuilder.create().texOffs(0, 34).addBox(0.0F, 0.0F, -0.99F, 2.0F, 8.0F, 2.0F), PartPose.offset(-3.5F, 0.0F, -12.0F));
        swiftFox.addOrReplaceChild("rightHindLeg", CubeListBuilder.create().texOffs(26, 33).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.offset(-0.5F, 0.0F, -1.0F));
        swiftFox.addOrReplaceChild("leftHindLeg", CubeListBuilder.create().texOffs(0, 34).addBox(0.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F), PartPose.offset(-3.5F, 0.0F, -1.0F));

        PartDefinition body = swiftFox.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -7.0F, -5.0F, 6.0F, 14.0F, 8.0F), PartPose.offsetAndRotation(-1.0F, -2.0F, -6.0F, 1.5708F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-3.0F, -3.0F, -4.0F, 8.0F, 6.0F, 6.0F)
                .texOffs(20, 0).addBox(-3.0F, -7.0F, 0.0F, 3.0F, 4.0F, 1.0F)
                .texOffs(0, 0).addBox(2.0F, -7.0F, 0.0F, 3.0F, 4.0F, 1.0F)
                .texOffs(28, 27).addBox(-1.0F, 0.0F, -7.0F, 4.0F, 3.0F, 3.0F), PartPose.offsetAndRotation(-1.0F, -8.5F, -1.0F, -1.5708F, 0.0F, 0.0F));

        PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(28, 0).addBox(-1.0F, 0.0F, -2.25F, 4.0F, 9.0F, 5.0F)
                .texOffs(23, 17).addBox(-1.0F, 9.0F, -2.25F, 4.0F, 5.0F, 5.0F), PartPose.offset(-1.0F, 7.0F, -1.0F));

        tail.addOrReplaceChild("real_tail", CubeListBuilder.create(), PartPose.offset(1.0F, 12.0F, -6.0F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    @Override
    public void setupAnim(T swiftFoxEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.animateWalk(SwiftFoxAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(swiftFoxEntity.attackAnimationState, SwiftFoxAnimation.attack, ageInTicks);
        this.animate(swiftFoxEntity.sneakAnimationState, SwiftFoxAnimation.sneak, ageInTicks);
        this.animate(swiftFoxEntity.sleepAnimationState, SwiftFoxAnimation.sleep, ageInTicks);
        this.animate(swiftFoxEntity.idleAnimationState, SwiftFoxAnimation.idle, ageInTicks);

        if (swiftFoxEntity.isSleeping()) {
            this.root.y += 3.0F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}