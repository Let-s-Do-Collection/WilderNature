package net.satisfy.wildernature.client.model.entity.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.model.entity.animation.BisonAnimation;
import net.satisfy.wildernature.core.entity.animal.defensive.BisonEntity;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class BisonModel<T extends BisonEntity> extends HierarchicalModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("bison"), "main");

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart head;

    private boolean baby;

    public BisonModel(ModelPart root) {
        this.root = root;
        ModelPart bison = root.getChild("bison");
        this.body = bison.getChild("Body");
        this.head = this.body.getChild("Head");
    }

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bison = partdefinition.addOrReplaceChild("bison", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Body = bison.addOrReplaceChild("Body", CubeListBuilder.create()
                        .texOffs(0, 46).addBox(-8.5F, -10.88F, -11.15F, 17.0F, 22.0F, 21.0F, new CubeDeformation(0.0F))
                        .texOffs(56, 69).addBox(-8.5F, 11.12F, -11.15F, 17.0F, 7.0F, 21.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-9.5F, -11.88F, -11.4F, 19.0F, 23.0F, 22.0F, new CubeDeformation(0.0F))
                        .texOffs(64, 27).addBox(-7.5F, -8.98F, 7.85F, 15.0F, 20.0F, 19.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 90).addBox(0.0F, 9.12F, -11.15F, 0.0F, 11.0F, 21.0F, new CubeDeformation(0.0F)),
                PartPose.offset(0.0F, -24.12F, -7.85F));

        Body.addOrReplaceChild("Tail", CubeListBuilder.create()
                        .texOffs(65, 120).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 7).addBox(-1.5F, -1.5F, 9.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 0).addBox(-1.5F, -1.5F, 12.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offsetAndRotation(0.0F, -1.88F, 25.85F, -0.9599F, 0.0F, 0.0F));

        Body.addOrReplaceChild("Head", CubeListBuilder.create()
                        .texOffs(83, 0).addBox(-5.5F, -0.9677F, -6.4743F, 11.0F, 13.0F, 12.0F, new CubeDeformation(0.0F))
                        .texOffs(114, 26).addBox(-3.5F, 12.0323F, -2.4743F, 7.0F, 5.0F, 8.0F, new CubeDeformation(0.0F))
                        .texOffs(92, 124).addBox(-0.1F, -0.9677F, 5.5257F, 0.0F, 16.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(43, 98).addBox(-6.5F, -1.9677F, -11.4743F, 13.0F, 15.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(112, 67).addBox(-6.5F, -1.9677F, -5.4743F, 13.0F, 15.0F, 2.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 90).addBox(-9.5F, 3.346F, -13.043F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 46).addBox(-6.5F, 3.346F, -6.043F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(22, 90).mirror().addBox(6.5F, 3.346F, -13.043F, 3.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)).mirror(false)
                        .texOffs(0, 46).mirror().addBox(4.5F, 3.346F, -6.043F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
                PartPose.offsetAndRotation(0.0F, 2.12F, -10.15F, -1.2217F, 0.0F, 0.0F));

        bison.addOrReplaceChild("leg_back_right", CubeListBuilder.create()
                        .texOffs(82, 98).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.6F, -15.0F, 12.5F));

        bison.addOrReplaceChild("leg_front_right", CubeListBuilder.create()
                        .texOffs(36, 120).addBox(-3.5F, -3.5F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.5F, -14.5F, -12.5F));

        bison.addOrReplaceChild("leg_front_left", CubeListBuilder.create()
                        .texOffs(0, 123).addBox(-3.5F, -3.4515F, -3.4145F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.5F, -14.5F, -12.5F));

        bison.addOrReplaceChild("leg_back_left", CubeListBuilder.create()
                        .texOffs(111, 98).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 18.0F, 7.0F, new CubeDeformation(0.0F)),
                PartPose.offset(4.6F, -15.0F, 12.5F));

        return LayerDefinition.create(meshdefinition, 256, 256);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.baby = entity.isBaby();

        this.animateWalk(BisonAnimation.walk, limbSwing, limbSwingAmount, 2.0F, 2.5F);
        this.animate(entity.idleAnimationState, BisonAnimation.idle, ageInTicks, 1.0F);
        this.animate(entity.attackAnimationState, BisonAnimation.attack, ageInTicks, 1.0F);
        this.animate(entity.alertAnimationState, BisonAnimation.alert, ageInTicks, 1.0F);
        this.animate(entity.snortAnimationState, BisonAnimation.snort, ageInTicks, 1.0F);
        this.animate(entity.grazeAnimationState, BisonAnimation.graze, ageInTicks, 1.0F);
        this.animate(entity.sleepAnimationState, BisonAnimation.sleep, ageInTicks, 1.0F);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int alpha) {
        if (this.baby) {
            boolean headVisible = this.head.visible;
            this.head.visible = false;
            this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
            this.head.visible = headVisible;

            poseStack.pushPose();
            this.body.translateAndRotate(poseStack);
            poseStack.translate(0.0D, 1.25D, 0.5D);
            poseStack.scale(1.85F, 1.85F, 1.85F);
            this.head.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
            poseStack.popPose();
            return;
        }
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, alpha);
    }

    @Override
    public @NotNull ModelPart root() {
        return this.root;
    }

    public ModelPart head() {
        return this.head;
    }
}