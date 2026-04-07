package net.satisfy.wildernature.core.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ContractItem extends Item {
    public ContractItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return super.getName(itemStack);
        }

        CompoundTag customDataTag = customData.copyTag();
        if (!customDataTag.contains("BountyType") || !customDataTag.contains("TargetType") || !customDataTag.contains("TargetId")) {
            return super.getName(itemStack);
        }

        String bountyType = customDataTag.getString("BountyType");
        return Component.translatable("gui.wildernature.bounty_board.title." + bountyType);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return;
        }

        CompoundTag customDataTag = customData.copyTag();
        if (!customDataTag.contains("BountyType") || !customDataTag.contains("TargetType") || !customDataTag.contains("TargetId")) {
            return;
        }

        Component targetName = this.getTargetName(customDataTag);
        String bountyType = customDataTag.getString("BountyType");
        int requiredAmount = customDataTag.contains("RequiredAmount") ? customDataTag.getInt("RequiredAmount") : 0;
        int currentProgress = customDataTag.contains("Progress") ? customDataTag.getInt("Progress") : 0;
        boolean isCompleted = currentProgress >= requiredAmount && requiredAmount > 0;

        if (isCompleted) {
            tooltipComponents.add(Component.translatable("item.wildernature.contract.completed").copy().withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("item.wildernature.contract.turn_in").copy().withStyle(ChatFormatting.GREEN));
            return;
        }

        int previewCount = customDataTag.contains("PreviewCount") ? customDataTag.getInt("PreviewCount") : 1;
        int experienceReward = customDataTag.contains("ExperienceReward") ? customDataTag.getInt("ExperienceReward") : 0;

        tooltipComponents.add(this.getObjectiveComponent(bountyType, requiredAmount, targetName).copy().withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(
                Component.translatable("gui.wildernature.bounty_board.progress")
                        .copy()
                        .append(Component.literal(": "))
                        .append(Component.literal(currentProgress + "/" + requiredAmount))
                        .withStyle(ChatFormatting.GRAY)
        );

        if (customDataTag.contains("PreviewItemId")) {
            ResourceLocation previewItemId = ResourceLocation.parse(customDataTag.getString("PreviewItemId"));
            Item previewItem = BuiltInRegistries.ITEM.get(previewItemId);
            ItemStack rewardStack = new ItemStack(previewItem);
            Component rewardName = rewardStack.getHoverName();
            Component rewardLine = previewCount > 1 ? Component.literal(previewCount + "x ").append(rewardName) : rewardName;

            tooltipComponents.add(
                    Component.translatable("gui.wildernature.bounty_board.reward")
                            .copy()
                            .append(Component.literal(": "))
                            .append(rewardLine)
                            .withStyle(ChatFormatting.GRAY)
            );
        }

        if (experienceReward > 0) {
            tooltipComponents.add(
                    Component.translatable("gui.wildernature.bounty_board.experience")
                            .copy()
                            .append(Component.literal(": "))
                            .append(Component.literal(String.valueOf(experienceReward)))
                            .withStyle(ChatFormatting.GRAY)
            );
        }
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private Component getObjectiveComponent(String bountyType, int requiredAmount, Component targetName) {
        return switch (bountyType) {
            case "hunt" -> requiredAmount == 1
                    ? Component.translatable("gui.wildernature.bounty_board.objective.hunt.single", targetName)
                    : Component.translatable("gui.wildernature.bounty_board.objective.hunt.plural", requiredAmount, targetName);
            case "gather" -> Component.translatable("gui.wildernature.bounty_board.objective.gather", targetName);
            case "observe" -> Component.translatable("gui.wildernature.bounty_board.objective.observe", targetName);
            case "explore" -> Component.translatable("gui.wildernature.bounty_board.objective.explore", targetName);
            default -> Component.empty();
        };
    }

    private Component getTargetName(CompoundTag customDataTag) {
        String targetType = customDataTag.getString("TargetType");
        ResourceLocation targetId = ResourceLocation.parse(customDataTag.getString("TargetId"));

        return switch (targetType) {
            case "entity" -> {
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(targetId)) {
                    EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(targetId);
                    yield entityType.getDescription();
                }
                yield Component.literal(targetId.getPath());
            }
            case "item" -> new ItemStack(BuiltInRegistries.ITEM.get(targetId)).getHoverName();
            case "biome" -> Component.translatable("biome." + targetId.getNamespace() + "." + targetId.getPath());
            default -> Component.literal(targetId.getPath());
        };
    }
}