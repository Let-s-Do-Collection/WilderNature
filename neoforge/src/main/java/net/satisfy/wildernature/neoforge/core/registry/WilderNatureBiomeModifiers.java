package net.satisfy.wildernature.neoforge.core.registry;

import com.mojang.serialization.MapCodec;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.neoforge.core.world.AddAnimalsBiomeModifier;
import net.satisfy.wildernature.neoforge.core.world.ConfigCondition;

public class WilderNatureBiomeModifiers {
    public static DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, WilderNature.MOD_ID);

    public static DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<AddAnimalsBiomeModifier>> ADD_ANIMALS_CODEC = BIOME_MODIFIER_SERIALIZERS.register("add_animals", () -> MapCodec.unit(AddAnimalsBiomeModifier::new));

    public static DeferredRegister<MapCodec<? extends ICondition>> CONDITION_SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, WilderNature.MOD_ID);

    public static DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCondition>> CONFIG_CONDITION = CONDITION_SERIALIZERS.register("config", () -> ConfigCondition.CODEC);
}
