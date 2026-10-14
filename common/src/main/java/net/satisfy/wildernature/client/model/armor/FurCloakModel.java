package net.satisfy.wildernature.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.satisfy.wildernature.WilderNature;

public class FurCloakModel<T extends LivingEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.parse("minecraft:player"), "wolf_fur_chestplate");
    public static final ResourceLocation TEXTURE = WilderNature.identifier("textures/models/armor/fur_cloak.png");

    private final ModelPart chestplate;
    private final ModelPart cape;

    public FurCloakModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.chestplate = root.getChild("chestplate");
        this.cape = this.chestplate.getChild("cape");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition rootPart = meshDefinition.getRoot();

        PartDefinition chestplatePart = rootPart.addOrReplaceChild("chestplate",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-17.0F, -3.5F, -1.0F, 18.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(23, 22).addBox(-12.0F, -4.5F, 5.0F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 25).addBox(-11.0F, -0.5F, 5.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
                PartPose.offset(8.0F, 2.5F, -2.0F));

        chestplatePart.addOrReplaceChild("cape",
                CubeListBuilder.create().texOffs(0, 10).addBox(-6.0F, 0.5F, 0.5F, 12.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-8.0F, -0.5F, 3.5F));

        return LayerDefinition.create(meshDefinition, 64, 64);
    }

    public void syncToBody(ModelPart bodyPart, boolean crouching) {
        this.chestplate.xRot = bodyPart.xRot;
        this.chestplate.yRot = bodyPart.yRot;
        this.chestplate.zRot = bodyPart.zRot;
        this.chestplate.x = 8.0F;
        this.chestplate.y = crouching ? 5.2F : 2.5F;
        this.chestplate.z = crouching ? -1.35F : -2.0F;
    }

    public void setupCape(T entity, float partialTick) {
        if (!(entity instanceof Player player)) {
            this.cape.xRot = 6.0F * Mth.DEG_TO_RAD;
            this.cape.yRot = 0.0F;
            this.cape.zRot = 0.0F;
            return;
        }

        double dx = Mth.lerp(partialTick, player.xCloakO, player.xCloak) - Mth.lerp(partialTick, player.xo, player.getX());
        double dy = Mth.lerp(partialTick, player.yCloakO, player.yCloak) - Mth.lerp(partialTick, player.yo, player.getY());
        double dz = Mth.lerp(partialTick, player.zCloakO, player.zCloak) - Mth.lerp(partialTick, player.zo, player.getZ());
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot) * Mth.DEG_TO_RAD;
        double sin = Mth.sin(bodyYaw);
        double cos = -Mth.cos(bodyYaw);

        float lift = Mth.clamp((float) dy * 10.0F, -6.0F, 32.0F);
        float back = Mth.clamp((float) (dx * sin + dz * cos) * 100.0F, 0.0F, 150.0F);
        float side = Mth.clamp((float) (dx * cos - dz * sin) * 100.0F, -20.0F, 20.0F);
        float bob = Mth.lerp(partialTick, player.oBob, player.bob);
        lift += Mth.sin(Mth.lerp(partialTick, player.walkDistO, player.walkDist) * 6.0F) * 32.0F * bob;

        this.cape.xRot = (6.0F + back / 2.0F + lift) * Mth.DEG_TO_RAD;
        this.cape.yRot = -side / 2.0F * Mth.DEG_TO_RAD;
        this.cape.zRot = side / 2.0F * Mth.DEG_TO_RAD;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.chestplate.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
