package net.satisfy.wildernature.core.item;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class ExperienceBurstItem extends Item {
    public ExperienceBurstItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            int experienceAmount = this.getExperienceAmount(itemStack);
            if (experienceAmount > 0) {
                this.spawnUseEffect(serverPlayer.serverLevel(), serverPlayer);
                serverPlayer.giveExperiencePoints(experienceAmount);
                serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.65F, 1.65F);
                itemStack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        int experienceAmount = this.getExperienceAmount(itemStack);
        if (experienceAmount > 0) {
            list.add(
                    Component.translatable("tooltip.wildernature.burst_of_experience.use")
                            .append(Component.literal(" "))
                            .append(
                                    Component.literal("[")
                                            .append(Component.literal(String.valueOf(experienceAmount)))
                                            .append(Component.literal(" "))
                                            .append(Component.translatable("tooltip.wildernature.burst_of_experience.experience"))
                                            .append(Component.literal("]"))
                                            .withStyle(style -> style.withColor(0x80FF20))
                            )
            );
        }
    }

    private int getExperienceAmount(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return 0;
        }

        CompoundTag customDataTag = customData.copyTag();
        return customDataTag.getInt("ExperienceAmount");
    }

    private void spawnUseEffect(ServerLevel level, ServerPlayer serverPlayer) {
        double centerX = serverPlayer.getX();
        double centerY = serverPlayer.getY() + 1.0D;
        double centerZ = serverPlayer.getZ();

        level.sendParticles(ParticleTypeRegistry.BURST_OF_EXPERIENCE.get(), centerX, centerY, centerZ, 18, 0.18D, 0.35D, 0.18D, 0.015D);
        level.sendParticles(ParticleTypeRegistry.BURST_OF_EXPERIENCE.get(), centerX, centerY + 0.35D, centerZ, 12, 0.32D, 0.28D, 0.32D, 0.012D);
        level.sendParticles(ParticleTypeRegistry.BURST_OF_EXPERIENCE.get(), centerX, centerY + 0.7D, centerZ, 8, 0.12D, 0.18D, 0.12D, 0.008D);
        level.sendParticles(ParticleTypeRegistry.SHEARED_WOOL.get(), centerX, centerY + 0.2D, centerZ, 10, 0.22D, 0.3D, 0.22D, 0.01D);
        level.sendParticles(ParticleTypes.GLOW, centerX, centerY + 0.45D, centerZ, 8, 0.18D, 0.22D, 0.18D, 0.01D);
    }
}