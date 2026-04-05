package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.gui.handler.BountyBoardMenu;

public class MenuTypeRegistry {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(WilderNature.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<BountyBoardMenu>> BOUNTY_BOARD_MENU = MENU_TYPES.register("bounty_board_menu", () -> MenuRegistry.ofExtended(BountyBoardMenu::new));

    public static void init() {
        MENU_TYPES.register();
    }
}