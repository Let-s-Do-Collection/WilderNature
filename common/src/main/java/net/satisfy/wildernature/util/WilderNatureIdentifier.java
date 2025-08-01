package net.satisfy.wildernature.util;

import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;

@SuppressWarnings("unused")
public class WilderNatureIdentifier {

    public static ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(WilderNature.MOD_ID, path);
    }

    public static String asString(String path) {
        return (WilderNature.MOD_ID + ":" + path);
    }
}
