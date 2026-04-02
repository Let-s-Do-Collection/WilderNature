package net.satisfy.wildernature.core.util;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class WilderNatureUtil {
    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;

        for (int i = 0; i < times; ++i) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.joinUnoptimized(buffer[1], Shapes.box(1.0 - maxZ, minY, minX, 1.0 - minZ, maxY, maxX), BooleanOp.OR));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }

    public static <T extends Block> RegistrySupplier<T> registerWithItem(DeferredRegister<Block> registerB, Registrar<Block> registrarB, DeferredRegister<Item> registerI, Registrar<Item> registrarI, ResourceLocation name, Supplier<T> block) {
        RegistrySupplier<T> toReturn = registerWithoutItem(registerB, registrarB, name, block);
        registerItem(registerI, registrarI, name, () -> new BlockItem(toReturn.get(), new Item.Properties()));
        return toReturn;
    }

    public static <T extends Block> RegistrySupplier<T> registerWithoutItem(DeferredRegister<Block> register, Registrar<Block> registrar, ResourceLocation path, Supplier<T> block) {
        return Platform.isNeoForge() ? register.register(path.getPath(), block) : registrar.register(path, block);
    }

    public static <T extends Item> RegistrySupplier<T> registerItem(DeferredRegister<Item> register, Registrar<Item> registrar, ResourceLocation path, Supplier<T> itemSupplier) {
        return Platform.isNeoForge() ? register.register(path.getPath(), itemSupplier) : registrar.register(path, itemSupplier);
    }

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

        public static Truffling.FoodValue getAdditionalFoodValue() {
            return new Truffling.FoodValue(0, 0.0F);
        }

        public static void addTruffledTooltip(ItemStack itemStack, List<Component> tooltip) {
            if (isTruffled(itemStack)) {
                tooltip.add(Component.translatable("tooltip.wildernature.truffled").withStyle(ChatFormatting.GOLD));
                tooltip.add(Component.translatable("tooltip.wildernature.truffled.nutrition").withStyle(ChatFormatting.GREEN));
                tooltip.add(Component.translatable("tooltip.wildernature.truffled.saturationModifier").withStyle(ChatFormatting.GREEN));
            }
        }
    }
}
