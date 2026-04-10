package net.satisfy.wildernature.core.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.satisfy.wildernature.WilderNature;

public class SoundEventRegistry {
    public static final Registrar<SoundEvent> SOUND_EVENTS = DeferredRegister.create(WilderNature.MOD_ID, Registries.SOUND_EVENT).getRegistrar();

    public static final RegistrySupplier<SoundEvent> LION_AMBIENT = create("lion_ambient");
    public static final RegistrySupplier<SoundEvent> LION_HURT = create("lion_hurt");
    public static final RegistrySupplier<SoundEvent> LION_DEATH = create("lion_death");
    public static final RegistrySupplier<SoundEvent> LION_WARN = create("lion_warn");
    public static final RegistrySupplier<SoundEvent> LION_ATTACK = create("lion_attack");
    public static final RegistrySupplier<SoundEvent> BEAVER_AMBIENT = create("beaver_ambient");
    public static final RegistrySupplier<SoundEvent> BEAVER_HURT = create("beaver_hurt");
    public static final RegistrySupplier<SoundEvent> BEAVER_DEATH = create("beaver_death");
    public static final RegistrySupplier<SoundEvent> BEAVER_TRADE = create("beaver_trade");
    public static final RegistrySupplier<SoundEvent> ELEPHANT_AMBIENT = create("elephant_ambient");
    public static final RegistrySupplier<SoundEvent> ELEPHANT_HURT = create("elephant_hurt");
    public static final RegistrySupplier<SoundEvent> ELEPHANT_DEATH = create("elephant_death");
    public static final RegistrySupplier<SoundEvent> ELEPHANT_STEP = create("elephant_step");
    public static final RegistrySupplier<SoundEvent> HIPPO_AMBIENT = create("hippo_ambient");
    public static final RegistrySupplier<SoundEvent> HIPPO_HURT = create("hippo_hurt");
    public static final RegistrySupplier<SoundEvent> HIPPO_DEATH = create("hippo_death");
    public static final RegistrySupplier<SoundEvent> GIRAFFE_AMBIENT = create("giraffe_ambient");
    public static final RegistrySupplier<SoundEvent> GIRAFFE_HURT = create("giraffe_hurt");
    public static final RegistrySupplier<SoundEvent> GIRAFFE_DEATH = create("giraffe_death");
    public static final RegistrySupplier<SoundEvent> DEER_AMBIENT = create("deer_ambient");
    public static final RegistrySupplier<SoundEvent> DEER_HURT = create("deer_hurt");
    public static final RegistrySupplier<SoundEvent> DEER_DEATH = create("deer_death");
    public static final RegistrySupplier<SoundEvent> RACCOON_AMBIENT = create("raccoon_ambient");
    public static final RegistrySupplier<SoundEvent> RACCOON_HURT = create("raccoon_hurt");
    public static final RegistrySupplier<SoundEvent> RACCOON_DEATH = create("raccoon_death");
    public static final RegistrySupplier<SoundEvent> SQUIRREL_AMBIENT = create("squirrel_ambient");
    public static final RegistrySupplier<SoundEvent> SQUIRREL_HURT = create("squirrel_hurt");
    public static final RegistrySupplier<SoundEvent> SQUIRREL_DEATH = create("squirrel_death");
    public static final RegistrySupplier<SoundEvent> BISON_AMBIENT = create("bison_ambient");
    public static final RegistrySupplier<SoundEvent> BISON_HURT = create("bison_hurt");
    public static final RegistrySupplier<SoundEvent> BISON_HORN = create("bison_horn");
    public static final RegistrySupplier<SoundEvent> BISON_ANGRY = create("bison_angry");
    public static final RegistrySupplier<SoundEvent> BISON_DEATH = create("bison_death");
    public static final RegistrySupplier<SoundEvent> TURKEY_AMBIENT = create("turkey_ambient");
    public static final RegistrySupplier<SoundEvent> TURKEY_HURT = create("turkey_hurt");
    public static final RegistrySupplier<SoundEvent> TURKEY_DEATH = create("turkey_death");
    public static final RegistrySupplier<SoundEvent> BOAR_AMBIENT = create("boar_ambient");
    public static final RegistrySupplier<SoundEvent> BOAR_HURT = create("boar_hurt");
    public static final RegistrySupplier<SoundEvent> BOAR_DEATH = create("boar_death");
    public static final RegistrySupplier<SoundEvent> HEDGEHOG_AMBIENT = create("hedgehog_ambient");
    public static final RegistrySupplier<SoundEvent> HEDGEHOG_HURT = create("hedgehog_hurt");
    public static final RegistrySupplier<SoundEvent> HEDGEHOG_DEATH = create("hedgehog_death");
    public static final RegistrySupplier<SoundEvent> CASSOWARY_AMBIENT = create("cassowary_ambient");
    public static final RegistrySupplier<SoundEvent> CASSOWARY_HURT = create("cassowary_hurt");
    public static final RegistrySupplier<SoundEvent> CASSOWARY_DEATH = create("cassowary_death");
    public static final RegistrySupplier<SoundEvent> DOG_AMBIENT = create("dog_ambient");
    public static final RegistrySupplier<SoundEvent> DOG_HURT = create("dog_hurt");
    public static final RegistrySupplier<SoundEvent> DOG_DEATH = create("dog_death");
    public static final RegistrySupplier<SoundEvent> BLUNDERBUSS_SHOOT = create("blunderbuss_shoot");
    public static final RegistrySupplier<SoundEvent> BLUNDERBUSS_LOAD = create("blunderbuss_load");
    public static final RegistrySupplier<SoundEvent> BOUNTY_ACCEPTED = create("bounty_accepted");
    public static final RegistrySupplier<SoundEvent> BOUNTY_CANCELED = create("bounty_canceled");
    public static final RegistrySupplier<SoundEvent> BOUNTY_COMPLETED = create("bounty_completed");

    private static RegistrySupplier<SoundEvent> create(String name) {
        final ResourceLocation id = WilderNature.identifier(name);
        return SOUND_EVENTS.register(id, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void init() {
    }
}
