package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.effect.NeurotoxinMobEffect;

public class MobEffectRegistry {
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(WilderNature.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> NEUROTOXIN = EFFECTS.register("neurotoxin", NeurotoxinMobEffect::new);

    public static void init() {
        EFFECTS.register();
    }

    public static Holder<MobEffect> holder() {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(NEUROTOXIN.get());
    }

    public static MobEffectInstance inst(int duration, int amplifier) {
        return new MobEffectInstance(holder(), duration, amplifier);
    }
}