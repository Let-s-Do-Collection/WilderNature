package net.satisfy.wildernature.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.WilderNature;

@SuppressWarnings("unused")
public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> WILDERNATURE_TABS = DeferredRegister.create(WilderNature.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> WILDERNATURE_TAB = WILDERNATURE_TABS.register("wildernature", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 1)
            .icon(() -> new ItemStack(ObjectRegistry.BOUNTY_BOARD.get()))
            .title(Component.translatable("creative_tab.wildernature"))
            .displayItems((parameters, out) -> {
                out.accept(ObjectRegistry.HAZELNUT.get());
                out.accept(ObjectRegistry.BISON_MEAT.get());
                out.accept(ObjectRegistry.COOKED_BISON_MEAT.get());
                out.accept(ObjectRegistry.VENISON.get());
                out.accept(ObjectRegistry.COOKED_VENISON.get());
                out.accept(ObjectRegistry.CASSOWARY_MEAT.get());
                out.accept(ObjectRegistry.COOKED_CASSOWARY_MEAT.get());
                out.accept(ObjectRegistry.TURKEY_MEAT.get());
                out.accept(ObjectRegistry.COOKED_TURKEY_MEAT.get());
                out.accept(ObjectRegistry.PELICAN_MEAT.get());
                out.accept(ObjectRegistry.COOKED_PELICAN_MEAT.get());
                out.accept(ObjectRegistry.UNCOMMON_CONTRACT.get());
                out.accept(ObjectRegistry.LEVELING_CONTRACT.get());
                out.accept(ObjectRegistry.COMMON_CONTRACT.get());
                out.accept(ObjectRegistry.RARE_CONTRACT.get());
                out.accept(ObjectRegistry.BISON_HORN.get());
                out.accept(ObjectRegistry.FISH_OIL.get());
                out.accept(ObjectRegistry.TRUFFLE.get());
                out.accept(ObjectRegistry.LOOT_BAG.get());
                out.accept(ObjectRegistry.FUR_CLOAK.get());
                out.accept(ObjectRegistry.BOUNTY_BOARD.get());
                out.accept(ObjectRegistry.RED_WOLF_TROPHY.get());
                out.accept(ObjectRegistry.DEER_TROPHY.get());
                out.accept(ObjectRegistry.BISON_TROPHY.get());
                out.accept(ObjectRegistry.BLUNDERBUSS.get());
                out.accept(ObjectRegistry.FLINT_AMMUNITION.get());
                out.accept(ObjectRegistry.DIAMOND_AMMUNITION.get());
                out.accept(ObjectRegistry.WOLF_TRAPPER_BANNER.get());
                out.accept(ObjectRegistry.BUNNY_STALKER_BANNER.get());
                out.accept(ObjectRegistry.COD_CATCHER_BANNER.get());
                out.accept(ObjectRegistry.DEER_SPAWN_EGG.get());
                out.accept(ObjectRegistry.RED_WOLF_SPAWN_EGG.get());
                out.accept(ObjectRegistry.RACCOON_SPAWN_EGG.get());
                out.accept(ObjectRegistry.MINISHEEP_SPAWN_EGG.get());
                out.accept(ObjectRegistry.SQUIRREL_SPAWN_EGG.get());
                out.accept(ObjectRegistry.PELICAN_SPAWN_EGG.get());
                out.accept(ObjectRegistry.BOAR_SPAWN_EGG.get());
                out.accept(ObjectRegistry.OWL_SPAWN_EGG.get());
                out.accept(ObjectRegistry.BISON_SPAWN_EGG.get());
                out.accept(ObjectRegistry.TURKEY_SPAWN_EGG.get());
                out.accept(ObjectRegistry.DOG_SPAWN_EGG.get());
                out.accept(ObjectRegistry.FLAMINGO_SPAWN_EGG.get());
                out.accept(ObjectRegistry.PENGUIN_SPAWN_EGG.get());
                out.accept(ObjectRegistry.HEDGEHOG_SPAWN_EGG.get());
                out.accept(ObjectRegistry.CASSOWARY_SPAWN_EGG.get());
                out.accept(ObjectRegistry.STYLIN_PURPLE_HAT.get());
            })
            .build());

    public static void init() {
        WILDERNATURE_TABS.register();
    }
}
