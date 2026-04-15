package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.GiraffeAnimation;
import net.satisfy.wildernature.core.entity.animal.neutral.GiraffeEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class GiraffeModel<T extends GiraffeEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("giraffe"), "main");

    private final ModelPart root;
    private final ModelPart giraffe;
    private final ModelPart neck;

    public GiraffeModel(ModelPart root) {
        this.root = root;
        this.giraffe = root.getChild("giraffe");
        ModelPart torso = this.giraffe.getChild("torso");
        this.neck = torso.getChild("neck");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition partDefinition = meshDefinition.getRoot();

        PartDefinition giraffe = partDefinition.addOrReplaceChild("giraffe", CubeListBuilder.create(), PartPose.offset(0.0F, 10.9369F, 0.2883F));

        PartDefinition torso = giraffe.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -17.0F, -29.0F, 14.0F, 18.0F, 32.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0631F, 12.7117F));

        PartDefinition tail = torso.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(30, 50).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.4677F, 2.409F, 0.1745F, 0.0F, 0.0F));

        tail.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(38, 44).addBox(0.0F, 0.0F, -3.0F, 0.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 29.0F, 1.0F));

        PartDefinition neck = torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(92, 0).addBox(-4.25F, -38.2089F, -4.25F, 8.0F, 38.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(20, 50).addBox(-1.25F, -46.2089F, 5.75F, 2.0F, 40.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(88, 48).addBox(-4.25F, -46.2089F, -6.25F, 8.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(62, 50).addBox(-3.25F, -43.2089F, -13.25F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -6.2304F, -21.8415F, 0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(80, 62).addBox(-1.0F, -10.5F, -1.1F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(80, 62).mirror().addBox(3.0F, -10.5F, -1.1F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.25F, -42.0133F, 2.2192F, -0.3927F, 0.0F, 0.0F));

        neck.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(108, 68).addBox(-1.0F, -2.0F, -0.5F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.75F, -43.2089F, 4.25F));
        neck.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(108, 68).mirror().addBox(-8.0F, -2.0F, -0.5F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-4.25F, -43.2089F, 4.25F));

        giraffe.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(0, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.35F, 7.0631F, -12.7883F));
        giraffe.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.35F, 7.0631F, -12.7883F));
        giraffe.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(0, 50).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, 7.0631F, 12.2117F));
        giraffe.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 50).mirror().addBox(-2.5F, 0.0F, -2.5F, 5.0F, 30.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, 7.0631F, 12.2117F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.giraffe.visible = !entity.isBaby();

        this.neck.yRot += netHeadYaw * ((float) Math.PI / 180F) * 0.35F;
        this.neck.xRot += headPitch * ((float) Math.PI / 180F) * 0.45F;

        this.animateWalk(entity.isRidingBurst() ? GiraffeAnimation.run : GiraffeAnimation.walk, limbSwing, limbSwingAmount, entity.isRidingBurst() ? 1.6F : 1.25F, entity.isRidingBurst() ? 2.8F : 2.0F);
        this.animate(entity.idleState, GiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.eatingState, GiraffeAnimation.eat, ageInTicks, 1.0F);
        this.animate(entity.alertState, GiraffeAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.sleepState, GiraffeAnimation.sleeping, ageInTicks, 1.0F);
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