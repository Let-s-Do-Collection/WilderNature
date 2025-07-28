package net.satisfy.wildernature.util;

import dev.architectury.platform.Platform;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.registry.ObjectRegistry;

import java.util.function.Supplier;

public class WilderNatureUtil {

    public static void init() {
        if (Platform.isFabric() && Platform.getEnvironment() == Env.CLIENT) {
            initClient();
        }
    }

    private static void initClient() {
        makeHorn(ObjectRegistry.BISON_HORN.get());
    }

    public static void makeHorn(Item item) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            ItemProperties.register(item, ResourceLocation.withDefaultNamespace("blowing"), (p_174635_, p_174636_, p_174637_, p_174638_) -> {
                if (p_174637_ == null) {
                    return 0.0F;
                } else {
                    return p_174637_.getUseItem() != p_174635_ ? 0.0F : (float) (p_174635_.getUseDuration(player) -
                            p_174637_.getUseItemRemainingTicks()) / 20.0F;
                }
            });
        }
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("using"), (p_174630_, p_174631_, p_174632_, p_174633_) -> p_174632_ != null && p_174632_.isUsingItem() && p_174632_.getUseItem() == p_174630_ ? 1.0F : 0.0F);
    }

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
}
