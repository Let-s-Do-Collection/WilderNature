package net.satisfy.wildernature.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;
import net.satisfy.wildernature.WilderNature;

public class BountyBoardModel<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(WilderNature.identifier("bounty_board"), "main");
    private static final float[][] CONTRACTS = {
            {0, 32, 3.6F, -8.45F, 11, 8},
            {0, 50, -11.4F, -8.45F, 7, 9},
            {14, 40, 8.6F, 0.55F, 7, 6},
            {0, 59, -3.4F, -2.45F, 8, 5},
            {14, 54, 1.6F, 2.55F, 7, 5},
            {14, 40, -10.4F, 1.55F, 6, 6},
            {6, 40, -0.4F, -8.45F, 3, 4},
            {14, 46, -11.4F, -0.45F, 3, 4},
            {14, 50, -2.4F, 3.55F, 3, 4},
            {0, 40, -3.4F, -7.45F, 3, 4}
    };
    public static final int CONTRACT_COUNT = CONTRACTS.length;
    private final ModelPart board;
    private final ModelPart contracts;
    private final ModelPart[] contractParts = new ModelPart[CONTRACT_COUNT];

    public BountyBoardModel(ModelPart root) {
        this.board = root.getChild("board");
        this.contracts = root.getChild("contracts");
        for (int index = 0; index < CONTRACT_COUNT; index++) {
            this.contractParts[index] = this.contracts.getChild("contract_" + index);
        }
    }

    @SuppressWarnings("unused")
    public static LayerDefinition getTexturedModelData() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        partdefinition.addOrReplaceChild("board", CubeListBuilder.create().texOffs(36, 32).addBox(22.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -28.0F, 0.0F, 28.0F, 18.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 26).addBox(-6.0F, -8.0F, 0.0F, 28.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 22).addBox(-6.0F, -10.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(28, 32).addBox(-8.0F, -30.0F, -1.0F, 2.0F, 30.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-6.0F, -30.0F, -1.0F, 28.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition contracts = partdefinition.addOrReplaceChild("contracts", CubeListBuilder.create(), PartPose.offset(6.4F, 5.45F, -0.15F));
        for (int index = 0; index < CONTRACT_COUNT; index++) {
            float[] contract = CONTRACTS[index];
            float halfWidth = contract[4] / 2.0F;
            contracts.addOrReplaceChild("contract_" + index, CubeListBuilder.create().texOffs((int) contract[0], (int) contract[1]).addBox(-halfWidth, 0.0F, 0.0F, contract[4], contract[5], 0.0F, new CubeDeformation(0.0F)), PartPose.offset(contract[2] + halfWidth, contract[3], 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public void setContracts(int visibleContracts, float time, float wobble, float seed) {
        for (int index = 0; index < CONTRACT_COUNT; index++) {
            ModelPart contract = this.contractParts[index];
            contract.visible = index < visibleContracts;
            float phase = seed + index * 1.7F;
            contract.xRot = -0.04F - (float) Math.sin(time * 0.05F + phase) * 0.03F - Math.abs(wobble) * (2.5F + index % 3);
            contract.zRot = (float) Math.sin(time * 0.037F + phase * 1.3F) * 0.02F + wobble * (index % 2 == 0 ? 1.5F : -1.5F);
        }
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int k) {
        board.render(poseStack, vertexConsumer, packedLight, packedOverlay, k);
        contracts.render(poseStack, vertexConsumer, packedLight, packedOverlay, k);
    }
}
