package net.satisfy.wildernature.core.entity;

public interface ShelteringMob {
    boolean isSheltering();

    void setSheltering(boolean sheltering);

    boolean canUseShelterGoal();

    boolean canContinueShelterGoal();

    int getShelterLocalWanderRadius();

    int getShelterLocalWanderCooldownMin();

    int getShelterLocalWanderCooldownMax();
}