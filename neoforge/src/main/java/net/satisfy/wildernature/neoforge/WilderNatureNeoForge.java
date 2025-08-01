package net.satisfy.wildernature.neoforge;

import dev.architectury.platform.hooks.EventBusesHooks;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.registry.ObjectRegistry;
import net.satisfy.wildernature.util.Truffling;
import net.satisfy.wildernature.util.contract.ContractReloader;
import net.satisfy.wildernature.neoforge.registry.WilderNatureBiomeModifiers;

@Mod(WilderNature.MOD_ID)
public class WilderNatureNeoForge {
    public WilderNatureNeoForge(final IEventBus modEventBus, ModContainer modContainer) {
        EventBusesHooks.whenAvailable(WilderNature.MOD_ID, IEventBus::start);
        WilderNature.init();
        WilderNatureBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.addListener(this::resourceLoaderEvent);
        NeoForge.EVENT_BUS.addListener(this::registerFuel);
        NeoForge.EVENT_BUS.addListener(this::onFoodEating);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        WilderNature.commonInit();
    }

    private void resourceLoaderEvent(AddReloadListenerEvent event){
        event.addListener(new ContractReloader());
    }

    private void registerFuel(FurnaceFuelBurnTimeEvent event) {
        if (event.getItemStack().getItem() == ObjectRegistry.FISH_OIL.get()) {
            event.setBurnTime(1600);
        }
    }

    private void onFoodEating(LivingEntityUseItemEvent event) {
        var player = event.getEntity();
        if (!Truffling.isTruffled(player.getItemInHand(player.getUsedItemHand()))) {
            return;
        }
        Truffling.FoodValue additionalFoodValues = Truffling.getAdditionalFoodValue();

        FoodProperties foodProperties = player.getItemInHand(player.getUsedItemHand()).get(DataComponents.FOOD);
        if (foodProperties != null) {
            foodProperties.nutrition = foodProperties.nutrition() + (int) (foodProperties.nutrition() * 0.20F) + additionalFoodValues.nutrition();
            foodProperties.saturation = foodProperties.saturation() + (foodProperties.saturation() * .20F) + additionalFoodValues.saturationModifier();
        }
    }
}
