package net.satisfy.wildernature.client;

import net.satisfy.foundation.tooltip.InfoTooltip;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public final class WilderNatureTooltips {
    private WilderNatureTooltips() {
    }

    public static void init() {
        InfoTooltip.of(ObjectRegistry.BOUNTY_BOARD.get()).placeable().register();
        InfoTooltip.of(ObjectRegistry.DEER_TROPHY.get()).line("tooltip.foundation.canbeplacedwall").register();
        InfoTooltip.of(ObjectRegistry.SWIFT_FOX_TROPHY.get()).line("tooltip.foundation.canbeplacedwall").register();
        InfoTooltip.of(ObjectRegistry.BISON_TROPHY.get()).line("tooltip.foundation.canbeplacedwall").register();
        InfoTooltip.of(ObjectRegistry.FIELD_GUIDE.get()).line("tooltip.wildernature.field_guide").line("tooltip.wildernature.field_guide.edition").register();
    }
}
