package net.satisfy.wildernature.core.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;

public interface CacheEatingMob {
    boolean canUseCacheEatGoal();

    boolean canContinueCacheEatGoal();

    int getCacheEatSearchRange();

    int getCacheEatDurationTicks();

    boolean hasEdibleItemInCache(HollowCacheBlockEntity hollowCacheBlockEntity);

    ItemStack takeFoodFromCache(HollowCacheBlockEntity hollowCacheBlockEntity);

    void healFromCacheFood(ItemStack itemStack);

    void spawnCacheEatParticles(ServerLevel serverLevel, ItemStack itemStack);

    void onCacheEatGoalStarted();

    void onCacheEatStarted(ItemStack itemStack);

    void onCacheEatFinished(ItemStack itemStack);

    void onCacheEatGoalStopped();
}