package net.satisfy.wildernature.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.forge.registry.WilderNatureConfig;
import net.satisfy.wildernature.registry.ObjectRegistry;
import net.satisfy.wildernature.util.contract.ContractReloader;
import net.satisfy.wildernature.forge.registry.WilderNatureBiomeModifiers;

@Mod(WilderNature.MOD_ID)
public class WilderNatureForge {
    public WilderNatureForge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(WilderNature.MOD_ID, modEventBus);
        WilderNature.init();
        WilderNatureBiomeModifiers.BIOME_MODIFIER_SERIALIZERS.register(modEventBus);
        WilderNatureConfig.loadConfig(WilderNatureConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve("wildernature.toml").toString());

        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.addListener(this::resourceLoaderEvent);
        MinecraftForge.EVENT_BUS.addListener(this::registerFuel);
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
}
