package net.satisfy.wildernature.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.List;
import java.util.Objects;

public class Truffling {

    public record FoodValue(int nutrition, float saturationModifier) {
        @Override
        public String toString() {
            return "{Nutrition:" + nutrition + ",Saturation:" + saturationModifier + "}";
        }
    }

    private static final String TRUFFLED_KEY = "Truffled";

    public static boolean isTruffled(ItemStack itemStack) {
        return itemStack.has(DataComponents.CUSTOM_DATA) && Objects.requireNonNull(itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)).contains(TRUFFLED_KEY);
    }

    public static ItemStack setTruffled(ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putBoolean(TRUFFLED_KEY, true);
        itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return itemStack;
    }

    public static FoodValue getAdditionalFoodValue() {
        return new FoodValue(0, 0.0F);
    }

    public static void addTruffledTooltip(ItemStack itemStack, List<Component> tooltip) {
        if (isTruffled(itemStack)) {
            tooltip.add(Component.translatable("tooltip.wildernature.truffled").withStyle(ChatFormatting.GOLD));
            tooltip.add(Component.translatable("tooltip.wildernature.truffled.nutrition").withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.translatable("tooltip.wildernature.truffled.saturationModifier").withStyle(ChatFormatting.GREEN));
        }
    }
}
