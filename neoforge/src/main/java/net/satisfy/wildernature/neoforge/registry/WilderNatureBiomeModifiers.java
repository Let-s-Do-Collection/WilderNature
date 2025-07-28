package net.satisfy.wildernature.neoforge.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.neoforge.world.AddAnimalsBiomeModifier;

public class WilderNatureBiomeModifiers {

    public static DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, WilderNature.MOD_ID);

    public static DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddAnimalsBiomeModifier>> ADD_ANIMALS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_animals", () -> MapCodec.unit(AddAnimalsBiomeModifier::new));
}

