package net.satisfy.wildernature.fabric.api;

import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketItem;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

import static net.satisfy.wildernature.core.registry.ObjectRegistry.FUR_CLOAK;

@SuppressWarnings("unused")
public class FurCloakTrinket extends TrinketItem {
    public FurCloakTrinket(Properties settings) {
        super(settings);
    }

    public static boolean isEquippedBy(Player player) {
        if (TrinketsApi.getTrinketComponent(player).isPresent()) {
            Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);
            assert component.isPresent();
            return component.get().isEquipped(FUR_CLOAK.get());
        }
        return false;
    }
}