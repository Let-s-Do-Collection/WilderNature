package net.satisfy.wildernature.core.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.bounty.BountyDefinition;
import net.satisfy.wildernature.core.bounty.BountyManager;
import net.satisfy.wildernature.core.bounty.PlayerBountyData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class ContractItem extends Item {

    public ContractItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return super.getName(itemStack);
        }

        CompoundTag customDataTag = customData.copyTag();
        if (!customDataTag.contains("EntityId") || !customDataTag.contains("BountyId")) {
            return super.getName(itemStack);
        }

        Component entityName = this.getEntityName(customDataTag);
        String[] translationKeys = new String[]{
                "gui.wildernature.bounty_board.title.hunt",
                "gui.wildernature.bounty_board.title.hunter",
                "gui.wildernature.bounty_board.title.trapper",
                "gui.wildernature.bounty_board.title.cull",
                "gui.wildernature.bounty_board.title.slash"
        };

        int titleIndex = Math.abs(customDataTag.getUUID("BountyId").hashCode()) % translationKeys.length;
        return Component.translatable(translationKeys[titleIndex], entityName);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag customDataTag = customData.copyTag();
        if (!customDataTag.contains("EntityId") || !customDataTag.contains("BountyId")) {
            return;
        }

        Component entityName = this.getEntityName(customDataTag);
        int requiredKills = customDataTag.contains("RequiredKills") ? customDataTag.getInt("RequiredKills") : 0;
        int currentProgress = customDataTag.contains("Progress") ? customDataTag.getInt("Progress") : 0;
        boolean isCompleted = currentProgress >= requiredKills && requiredKills > 0;

        if (isCompleted) {
            tooltipComponents.add(Component.translatable("item.wildernature.contract.completed").withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.wildernature.contract.turn_in").withStyle(ChatFormatting.GREEN));
            return;
        }

        int previewCount = customDataTag.contains("PreviewCount") ? customDataTag.getInt("PreviewCount") : 1;
        int experienceReward = customDataTag.contains("ExperienceReward") ? customDataTag.getInt("ExperienceReward") : 0;

        tooltipComponents.add(Component.translatable("gui.wildernature.bounty_board.objective", requiredKills, entityName).withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("gui.wildernature.bounty_board.progress").append(": ").append(Component.literal(currentProgress + "/" + requiredKills)).withStyle(ChatFormatting.GRAY));

        if (customDataTag.contains("PreviewItemId")) {
            ResourceLocation previewItemId = ResourceLocation.parse(customDataTag.getString("PreviewItemId"));
            Item previewItem = BuiltInRegistries.ITEM.get(previewItemId);
            ItemStack rewardStack = new ItemStack(previewItem);
            Component rewardName = rewardStack.getHoverName();
            Component rewardLine = previewCount > 1 ? Component.literal(previewCount + "x ").append(rewardName) : rewardName;
            tooltipComponents.add(Component.translatable("gui.wildernature.bounty_board.reward").append(": ").append(rewardLine).withStyle(ChatFormatting.GRAY));
        }

        if (experienceReward > 0) {
            tooltipComponents.add(Component.translatable("gui.wildernature.bounty_board.experience").append(": ").append(Component.literal(String.valueOf(experienceReward))).withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private Component getEntityName(CompoundTag customDataTag) {
        ResourceLocation entityId = ResourceLocation.parse(customDataTag.getString("EntityId"));
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

        if (entityType == EntityType.PIG && !BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            return Component.literal(entityId.getPath());
        }

        return entityType.getDescription();
    }
}