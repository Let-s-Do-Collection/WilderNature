package net.satisfy.wildernature.neoforge;

import dev.architectury.platform.hooks.EventBusesHooks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import net.satisfy.wildernature.neoforge.core.registry.WilderNatureBiomeModifiers;
import net.satisfy.wildernature.neoforge.core.registry.WilderNatureConfig;

@Mod(WilderNature.MOD_ID)
public class WilderNatureNeoForge {
    public WilderNatureNeoForge(final IEventBus modEventBus, ModContainer modContainer) {
        EventBusesHooks.whenAvailable(WilderNature.MOD_ID, IEventBus::start);
        WilderNature.init();
        WilderNatureBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, WilderNatureConfig.COMMON_CONFIG);
        modEventBus.addListener(WilderNatureConfig::onLoad);
        modEventBus.addListener(WilderNatureConfig::onReload);
        NeoForge.EVENT_BUS.addListener(this::registerFuel);
        NeoForge.EVENT_BUS.addListener(this::onFoodEating);
    }

    private void registerFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == ObjectRegistry.FISH_OIL.get()) {
            event.setBurnTime(1600);
        }
    }

    private void onFoodEating(LivingEntityUseItemEvent event) {
        var player = event.getEntity();
        if (!WilderNatureUtil.Truffling.isTruffled(player.getItemInHand(player.getUsedItemHand()))) return;
        var add = WilderNatureUtil.Truffling.getAdditionalFoodValue();
        FoodProperties props = player.getItemInHand(player.getUsedItemHand()).get(DataComponents.FOOD);
        if (props != null) {
            props.nutrition = props.nutrition() + (int)(props.nutrition() * 0.2F) + add.nutrition();
            props.saturation = props.saturation() + (props.saturation() * 0.2F) + add.saturationModifier();
        }
    }
}
