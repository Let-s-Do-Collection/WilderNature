package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.satisfy.wildernature.WilderNature;

public class ParticleTypeRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(WilderNature.MOD_ID, Registries.PARTICLE_TYPE);

    public static final RegistrySupplier<SimpleParticleType> SLEEPING = PARTICLE_TYPES.register("sleeping", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> QUESTION = PARTICLE_TYPES.register("question", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> ALERT = PARTICLE_TYPES.register("alert", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> DENY = PARTICLE_TYPES.register("deny", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> TRUST_POSITIVE = PARTICLE_TYPES.register("trust_positive", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> TRUST_NEGATIVE = PARTICLE_TYPES.register("trust_negative", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> LOVE = PARTICLE_TYPES.register("love", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> CACHE_OPEN = PARTICLE_TYPES.register("cache_open", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> CACHE_CLOSE = PARTICLE_TYPES.register("cache_close", () -> new SimpleParticleType(false) {});
    public static final RegistrySupplier<SimpleParticleType> SHEARED_WOOL = PARTICLE_TYPES.register("sheared_wool", () -> new SimpleParticleType(false) {});

    public static void init() {
        PARTICLE_TYPES.register();
    }
}