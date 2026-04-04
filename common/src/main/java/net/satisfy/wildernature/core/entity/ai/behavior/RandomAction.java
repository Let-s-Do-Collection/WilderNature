package net.satisfy.wildernature.core.entity.ai.behavior;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public interface RandomAction {
    boolean isInterruptable();

    void onStart();

    default void onTick() {
    }

    void onStop();

    boolean isPossible();

    int duration();

    float chance();

    AttributeInstance getAttribute(Attribute movementSpeed);
}
