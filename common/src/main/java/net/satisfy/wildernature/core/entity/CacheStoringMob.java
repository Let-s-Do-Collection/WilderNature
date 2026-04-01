package net.satisfy.wildernature.core.entity;

import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;

public interface CacheStoringMob {
    boolean hasItemsToStore();

    int getStoreCooldownTicks();

    boolean canUseStoreGoal();

    boolean canContinueStoreGoal();

    int getCacheSearchRange();

    int getCacheStoreWiggleDuration();

    void onStoreGoalStarted();

    void onStoreGoalStopped();

    void onStoreWiggleStarted(int durationTicks);

    boolean depositItemsIntoCache(HollowCacheBlockEntity hollowCacheBlockEntity);

    void startStoreCooldown();
}