package net.satisfy.wildernature.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.util.WilderNatureIdentifier;

public class BountyBoardModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNatureIdentifier.of("bounty_board"), "main");
    private final ModelPart board;


    public BountyBoardModel(ModelPart root) {
        this.board = root.getChild("board");

    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition board = partdefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(0, 26).addBox(22.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -28.0F, 0.0F, 28.0F, 18.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(24, 26).addBox(18.0F, -8.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-6.0F, -10.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(8, 26).addBox(-8.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(16, 26).addBox(-6.0F, -8.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-6.0F, -30.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-16.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int k) {
        board.render(poseStack, vertexConsumer, packedLight, packedOverlay, k);
    }
}