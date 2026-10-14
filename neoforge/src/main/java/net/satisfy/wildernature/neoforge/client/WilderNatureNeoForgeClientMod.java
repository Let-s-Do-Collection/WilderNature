package net.satisfy.wildernature.neoforge.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.satisfy.wildernature.WilderNature;

@Mod(value = WilderNature.MOD_ID, dist = Dist.CLIENT)
public class WilderNatureNeoForgeClientMod {
    public WilderNatureNeoForgeClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }
}
