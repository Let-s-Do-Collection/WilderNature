package net.satisfy.wildernature.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class StylinPurpleHatModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("stylin_purple_hat"), "main");
    private final ModelPart stylin_purple_hat;

    public StylinPurpleHatModel(ModelPart root) {
        this.stylin_purple_hat = root.getChild("stylin_purple_hat");
    }

    @SuppressWarnings("unused")
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition stylin_purple_hat = partdefinition.addOrReplaceChild("stylin_purple_hat", CubeListBuilder.create()
                .texOffs(-17, 13).addBox(-8.5F, -6.0F, -8.5F, 17.0F, 0.0F, 17.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -10.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int k) {
        poseStack.pushPose();
        poseStack.scale(1.05F, 1.05F, 1.05F);
        stylin_purple_hat.render(poseStack, buffer, packedLight, packedOverlay);
        poseStack.popPose();
    }

    @Override
    public void setupAnim(T entity, float f, float g, float h, float i, float j) {

    }

    public void copyHead(ModelPart model) {
        stylin_purple_hat.copyFrom(model);
    }
}
