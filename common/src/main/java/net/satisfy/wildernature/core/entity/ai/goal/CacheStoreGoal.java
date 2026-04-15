package net.satisfy.wildernature.core.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.ai.behavior.CacheStoringMob;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class CacheStoreGoal<T extends PathfinderMob & CacheStoringMob> extends Goal {
    private static final int SEARCH_COOLDOWN_MIN = 100;
    private static final int SEARCH_COOLDOWN_MAX = 180;
    private static final float BONUS_LOOT_BAG_CHANCE = 0.125F;

    private final T mob;
    private final double speedModifier;
    private BlockPos cachePos;
    private int storeWiggleTicks;
    private int searchCooldownTicks;

    public CacheStoreGoal(T mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.hasItemsToStore()) {
            return false;
        }

        if (this.mob.getStoreCooldownTicks() > 0 || !this.mob.canUseStoreGoal()) {
            return false;
        }

        if (this.cachePos != null && this.isValidCache(this.cachePos)) {
            return true;
        }

        if (this.searchCooldownTicks > 0) {
            this.searchCooldownTicks--;
            return false;
        }

        this.cachePos = this.findNearestAvailableCache();
        this.searchCooldownTicks = this.getNextSearchCooldown();
        return this.cachePos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.cachePos != null && this.mob.hasItemsToStore() && this.mob.canContinueStoreGoal() && this.isValidCache(this.cachePos);
    }

    @Override
    public void start() {
        this.mob.onStoreGoalStarted();
        this.storeWiggleTicks = 0;
        this.moveToCache();
    }

    @Override
    public void stop() {
        if (this.cachePos != null && this.mob.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
            HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, false);
            hollowCacheBlockEntity.scheduleCloseParticles();
        }

        this.mob.onStoreGoalStopped();
        this.mob.getNavigation().stop();
        this.cachePos = null;
        this.storeWiggleTicks = 0;
        this.searchCooldownTicks = 0;
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

        if (!hollowCacheBlockEntity.hasFreeSlot()) {
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

        if (this.storeWiggleTicks <= 0) {
            this.storeWiggleTicks = this.mob.getCacheStoreWiggleDuration();
            this.mob.onStoreWiggleStarted(this.mob.getCacheStoreWiggleDuration());
            return;
        }

        this.storeWiggleTicks--;
        if (this.storeWiggleTicks > 0) {
            return;
        }

        boolean depositedAnyItem = this.mob.depositItemsIntoCache(hollowCacheBlockEntity);
        if (depositedAnyItem && this.mob.getRandom().nextFloat() < BONUS_LOOT_BAG_CHANCE) {
            hollowCacheBlockEntity.tryAddItem(new ItemStack(ObjectRegistry.LOOT_BAG.get()));
        }

        HollowCacheBlock.setOpen(this.mob.level().getBlockState(this.cachePos), this.mob.level(), this.cachePos, false);
        hollowCacheBlockEntity.scheduleCloseParticles();

        if (depositedAnyItem) {
            this.mob.startStoreCooldown();
        }

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

        return hollowCacheBlockEntity.hasFreeSlot();
    }

    @Nullable
    private BlockPos findNearestAvailableCache() {
        BlockPos originPos = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos closestCachePos = null;
        double closestDistance = Double.MAX_VALUE;
        int horizontalRange = this.mob.getCacheSearchRange();

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

                    if (!hollowCacheBlockEntity.hasFreeSlot()) {
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