package net.satisfy.wildernature.client.compat;

import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public final class AccessoriesClientCompat {
    private AccessoriesClientCompat() {
    }

    public static void init() {
        AccessoriesRendererRegistry.registerNoRenderer(ObjectRegistry.FUR_CLOAK.get());
    }
}
