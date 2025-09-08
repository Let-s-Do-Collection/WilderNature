package net.satisfy.wildernature.client.util;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public class WilderNatureClientUtil {

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
}
