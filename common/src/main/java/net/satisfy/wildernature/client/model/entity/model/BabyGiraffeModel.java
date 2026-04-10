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
import net.satisfy.wildernature.client.model.entity.animation.BabyGiraffeAnimation;
import net.satisfy.wildernature.core.entity.animal.neutral.GiraffeEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BabyGiraffeModel<T extends GiraffeEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("baby_giraffe"), "main");

    private final ModelPart root;
    private final ModelPart giraffe;
    private final ModelPart neck;

    public BabyGiraffeModel(ModelPart root) {
        this.root = root;
        this.giraffe = root.getChild("giraffe");
        ModelPart torso = this.giraffe.getChild("torso");
        this.neck = torso.getChild("neck");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition giraffe = partDefinition.addOrReplaceChild("giraffe", CubeListBuilder.create(), PartPose.offset(0.0F, 8.9369F, 0.2883F));

        PartDefinition torso = giraffe.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -11.0F, -18.0F, 10.0F, 12.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0631F, 7.7117F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(12, 32).addBox(-1.0F, -0.25F, -0.25F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.3551F, 1.584F, 0.1745F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(20, 28).addBox(0.0F, -1.0F, -2.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.75F, 0.75F));

        PartDefinition neck = torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(12, 40).addBox(-3.25F, -14.2089F, -2.75F, 6.0F, 13.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(40, 2).addBox(-4.25F, -22.2089F, -4.75F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(32, 32).addBox(-3.25F, -19.2089F, -9.75F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -5.6872F, -14.5726F, 0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(4, 47).mirror().addBox(0.0F, 15.825F, -4.375F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(4, 47).addBox(4.0F, 15.825F, -4.375F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.25F, -37.7645F, 13.4252F, -0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(32, 42).addBox(-0.5F, -2.0F, -0.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.25F, -19.2089F, 2.75F));
        neck.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(32, 42).mirror().addBox(-6.5F, -2.0F, -0.5F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.75F, -19.2089F, 2.75F));

        giraffe.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.85F, 4.0631F, -7.7883F));
        giraffe.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-2.85F, 4.0631F, -7.7883F));
        giraffe.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 32).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 4.0631F, 7.2117F));
        giraffe.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 32).mirror().addBox(-1.5F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.0F, 4.0631F, 7.2117F));

        return LayerDefinition.create(meshDefinition, 80, 80);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.neck.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.neck.xRot += headPitch * ((float) Math.PI / 180F) * 0.45F;

        this.animateWalk(entity.isRidingBurst() ? BabyGiraffeAnimation.run : BabyGiraffeAnimation.walk, limbSwing, limbSwingAmount, entity.isRidingBurst() ? 1.6F : 1.25F, entity.isRidingBurst() ? 2.8F : 2.0F);
        this.animate(entity.idleState, BabyGiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.eatingState, BabyGiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.alertState, BabyGiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.sleepState, BabyGiraffeAnimation.sleeping, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.giraffe.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }
}