package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.recipe.TrufflingRecipe;

public class RecipeRegistry {

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(WilderNature.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<?>> TRUFFLING = SERIALIZERS.register("truffling", TrufflingRecipe.Serializer::new);


    public static void init() {
        SERIALIZERS.register();
    }
}
