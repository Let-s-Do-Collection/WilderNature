package net.satisfy.wildernature.core.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheEatingMob;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

public class CacheEatGoal<T extends PathfinderMob & CacheEatingMob> extends Goal {
    private static final int SEARCH_COOLDOWN_MIN = 80;
    private static final int SEARCH_COOLDOWN_MAX = 160;

    private final T mob;
    private final double speedModifier;
    private BlockPos cachePos;
    private int eatTicks;
    private int searchCooldownTicks;
    private ItemStack cachedFoodStack = ItemStack.EMPTY;

    public CacheEatGoal(T mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.canUseCacheEatGoal()) {
            return false;
        }

        if (this.searchCooldownTicks > 0) {
            this.searchCooldownTicks--;
            return false;
        }

        if (this.cachePos != null && this.isValidCache(this.cachePos)) {
            return true;
        }

        this.cachePos = this.findNearestFoodCache();
        this.searchCooldownTicks = this.getNextSearchCooldown();
        return this.cachePos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.cachePos != null && this.mob.canContinueCacheEatGoal() && this.isValidCache(this.cachePos);
    }

    @Override
    public void start() {
        this.eatTicks = 0;
        this.cachedFoodStack = ItemStack.EMPTY;
        this.mob.onCacheEatGoalStarted();
        this.moveToCache();
    }

    @Override
    public void stop() {
        if (this.cachePos != null && this.mob.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
            HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, false);
            hollowCacheBlockEntity.scheduleCloseParticles();
        }

        this.mob.onCacheEatGoalStopped();
        this.mob.getNavigation().stop();
        this.cachePos = null;
        this.eatTicks = 0;
        this.searchCooldownTicks = 0;
        this.cachedFoodStack = ItemStack.EMPTY;
    }

    @Override
    public void tick() {
        if (this.cachePos == null) {
            return;
        }

        if (!(this.mob.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
            this.cachePos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
            return;
        }

        if (!this.cachePos.closerToCenterThan(this.mob.position(), 1.75D)) {
            this.moveToCache();
            return;
        }

        this.mob.getNavigation().stop();
        this.mob.getLookControl().setLookAt(this.cachePos.getX() + 0.5D, this.cachePos.getY() + 0.5D, this.cachePos.getZ() + 0.5D);
        HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, true);

        if (this.eatTicks <= 0) {
            ItemStack extractedFoodStack = this.mob.takeFoodFromCache(hollowCacheBlockEntity);
            if (extractedFoodStack.isEmpty()) {
                HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, false);
                hollowCacheBlockEntity.scheduleCloseParticles();
                this.cachePos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            this.cachedFoodStack = extractedFoodStack.copy();
            this.eatTicks = this.mob.getCacheEatDurationTicks();
            this.mob.onCacheEatStarted(this.cachedFoodStack);
            return;
        }

        this.eatTicks--;
        if (this.eatTicks > 0) {
            return;
        }

        if (!this.cachedFoodStack.isEmpty()) {
            if (this.mob.level() instanceof ServerLevel serverLevel) {
                this.mob.spawnCacheEatParticles(serverLevel, this.cachedFoodStack);
            }
            this.mob.healFromCacheFood(this.cachedFoodStack);
        }

        HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, false);
        hollowCacheBlockEntity.scheduleCloseParticles();
        this.mob.onCacheEatFinished(this.cachedFoodStack);
        this.cachedFoodStack = ItemStack.EMPTY;
        this.cachePos = null;
        this.searchCooldownTicks = this.getNextSearchCooldown();
    }

    private int getNextSearchCooldown() {
        return SEARCH_COOLDOWN_MIN + this.mob.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
    }

    private void moveToCache() {
        this.mob.getNavigation().moveTo(this.cachePos.getX() + 0.5D, this.cachePos.getY(), this.cachePos.getZ() + 0.5D, this.speedModifier);
    }

    private boolean isValidCache(BlockPos checkedPos) {
        if (!this.mob.level().getBlockState(checkedPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
            return false;
        }

        if (!(this.mob.level().getBlockEntity(checkedPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
            return false;
        }

        return this.mob.hasEdibleItemInCache(hollowCacheBlockEntity);
    }

    @Nullable
    private BlockPos findNearestFoodCache() {
        BlockPos originPos = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos closestCachePos = null;
        double closestDistance = Double.MAX_VALUE;
        int horizontalRange = this.mob.getCacheEatSearchRange();

        for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
            for (int offsetY = -5; offsetY <= 5; offsetY++) {
                for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                    mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                    if (!this.mob.level().getBlockState(mutableBlockPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                        continue;
                    }

                    if (!(this.mob.level().getBlockEntity(mutableBlockPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
                        continue;
                    }

                    if (!this.mob.hasEdibleItemInCache(hollowCacheBlockEntity)) {
                        continue;
                    }

                    double checkedDistance = mutableBlockPos.distSqr(originPos);
                    if (checkedDistance < closestDistance) {
                        closestDistance = checkedDistance;
                        closestCachePos = mutableBlockPos.immutable();
                    }
                }
            }
        }

        return closestCachePos;
    }
}