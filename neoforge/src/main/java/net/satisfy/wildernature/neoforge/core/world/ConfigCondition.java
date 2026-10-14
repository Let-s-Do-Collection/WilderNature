package net.satisfy.wildernature.neoforge.core.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.satisfy.wildernature.neoforge.core.config.WilderNatureNeoForgeConfig;

public record ConfigCondition(String option) implements ICondition {
    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.STRING.fieldOf("option").forGetter(ConfigCondition::option)).apply(instance, ConfigCondition::new));

    @Override
    public boolean test(IContext context) {
        return WilderNatureNeoForgeConfig.isEnabled(option);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
