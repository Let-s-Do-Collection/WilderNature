package net.satisfy.wildernature.neoforge.core.player.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.satisfy.wildernature.WilderNature;

public class WolfFurChestplateModel<T extends Entity> extends EntityModel<T> {

    public static final ResourceLocation WOLF_FUR_CHESTPLATE_TEXTURE = WilderNature.identifier("textures/models/armor/fur_cloak.png");

    private final ModelPart chestplate;
    private final ModelPart cape;

    public WolfFurChestplateModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.chestplate = root.getChild("chestplate");
        this.cape = this.chestplate.getChild("cape");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition part = mesh.getRoot();
        PartDefinition chestplate = part.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(0, 0).addBox(-17.0F, -3.5F, -1.0F, 18.0F, 4.0F, 6.0F, new CubeDeformation(0F)).texOffs(23, 22).addBox(-12.0F, -4.5F, 5.0F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 25).addBox(-11.0F, -0.5F, 5.0F, 6.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 2.5F, -2.0F));
        chestplate.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(0, 10).addBox(-6.0F, 0.5F, 0.5F, 12.0F, 14.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -0.5F, 3.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof LivingEntity e) {
            float speed = (float) e.getDeltaMovement().horizontalDistance();
            float falling = e.onGround() ? 0F : Mth.clamp((float) (-e.getDeltaMovement().y * 0.8F), 0F, 0.6F);
            float sprint = e.isSprinting() ? 0.25F : 0F;
            float thunder = e.level().isThundering() ? 0.35F + e.level().getThunderLevel(0F) * 0.65F : 0F;
            float base = 0.15F;
            float targetX = base + speed * 0.6F + sprint + falling + thunder;
            float sway = 0.05F + thunder * 0.6F;
            float targetY = Mth.sin(ageInTicks * 0.25F) * sway;
            float targetZ = Mth.cos(ageInTicks * 0.2F) * (0.02F + thunder * 0.2F);
            float lerp = 0.25F;
            this.cape.xRot = Mth.lerp(lerp, this.cape.xRot, targetX);
            this.cape.yRot = Mth.lerp(lerp, this.cape.yRot, targetY);
            this.cape.zRot = Mth.lerp(lerp, this.cape.zRot, targetZ);
        }
    }

    public void syncWithHumanoid(HumanoidModel<?> humanoid) {
        this.chestplate.copyFrom(humanoid.body);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        chestplate.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
