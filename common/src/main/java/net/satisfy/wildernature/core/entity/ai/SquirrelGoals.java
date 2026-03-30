package net.satisfy.wildernature.core.entity.ai;

import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.SquirrelEntity;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

public class SquirrelGoals {

    public static class SquirrelGiftTriggerGoal extends Goal {
        private final SquirrelEntity squirrel;

        public SquirrelGiftTriggerGoal(SquirrelEntity squirrel) {
            this.squirrel = squirrel;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.squirrel.hasMaximumTrust()) {
                return false;
            }

            if (this.squirrel.level().isNight()) {
                return false;
            }

            if (this.squirrel.isSheltering() || this.squirrel.isDeliveringGift() || this.squirrel.isWiggling() || this.squirrel.isBaby() || this.squirrel.isPanicking()) {
                return false;
            }

            if (this.squirrel.getGiftCooldownTicks() > 0 || this.squirrel.getGiftFailCooldownTicks() > 0) {
                return false;
            }

            if (!this.squirrel.getMainHandItem().isEmpty() || !this.squirrel.getPendingGiftStack().isEmpty()) {
                return false;
            }

            if (this.squirrel.getRandom().nextInt(SquirrelEntity.GIFT_TRIGGER_CHANCE) != 0) {
                return false;
            }

            if (!this.squirrel.tryTriggerGift()) {
                this.squirrel.startGiftFailCooldown();
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    public static class SquirrelDeliverGiftGoal extends Goal {
        private final SquirrelEntity squirrel;
        private Player targetPlayer;
        private boolean giftDropped;

        public SquirrelDeliverGiftGoal(SquirrelEntity squirrel) {
            this.squirrel = squirrel;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.squirrel.getPendingGiftStack().isEmpty() && this.squirrel.getGiftTargetPlayerUuid() != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }

            if (this.squirrel.getMainHandItem().isEmpty()) {
                return false;
            }

            return this.squirrel.distanceToSqr(this.targetPlayer) <= SquirrelEntity.GIFT_RANGE * SquirrelEntity.GIFT_RANGE;
        }

        @Override
        public void start() {
            this.giftDropped = false;
            this.squirrel.setDeliveringGift(true);
            this.squirrel.setGiftWiggleTicks(0);

            UUID targetPlayerUuid = this.squirrel.getGiftTargetPlayerUuid();
            this.targetPlayer = targetPlayerUuid != null ? this.squirrel.level().getPlayerByUUID(targetPlayerUuid) : null;

            if (!this.squirrel.getPendingGiftStack().isEmpty()) {
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, this.squirrel.getPendingGiftStack().copy());
            }
        }

        @Override
        public void stop() {
            if (!this.giftDropped) {
                ItemStack giftStack = this.squirrel.getMainHandItem().copy();
                if (!giftStack.isEmpty()) {
                    this.squirrel.dropGift(giftStack);
                }
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.squirrel.startGiftFailCooldown();
            }

            this.squirrel.setPendingGiftStack(ItemStack.EMPTY);
            this.squirrel.getNavigation().stop();
            this.squirrel.setDeliveringGift(false);
            this.squirrel.setGiftWiggleTicks(0);
            this.squirrel.setGiftTargetPlayerUuid(null);
            this.targetPlayer = null;
            this.giftDropped = false;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            double distanceToPlayer = this.squirrel.distanceToSqr(this.targetPlayer);

            if (distanceToPlayer > SquirrelEntity.GIFT_STOP_DISTANCE * SquirrelEntity.GIFT_STOP_DISTANCE && this.squirrel.getGiftWiggleTicks() <= 0) {
                this.squirrel.getNavigation().moveTo(this.targetPlayer, 1.1D);
                return;
            }

            this.squirrel.getNavigation().stop();
            this.squirrel.lookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.squirrel.getGiftWiggleTicks() <= 0) {
                this.squirrel.setGiftWiggleTicks(SquirrelEntity.GIFT_WIGGLE_DURATION);
                this.squirrel.startWiggle(SquirrelEntity.GIFT_WIGGLE_DURATION);
                return;
            }

            this.squirrel.setGiftWiggleTicks(this.squirrel.getGiftWiggleTicks() - 1);

            if (this.squirrel.getGiftWiggleTicks() > 0) {
                return;
            }

            ItemStack giftStack = this.squirrel.getMainHandItem().copy();
            if (!giftStack.isEmpty()) {
                this.squirrel.dropGift(giftStack);
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.squirrel.setPendingGiftStack(ItemStack.EMPTY);
                this.squirrel.startGiftCooldown();
                this.giftDropped = true;
            }

            this.squirrel.setDeliveringGift(false);
            this.squirrel.setGiftTargetPlayerUuid(null);
        }
    }

    public static class SquirrelSeekShelterGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 100;
        private static final int SEARCH_COOLDOWN_MAX = 180;

        private final SquirrelEntity squirrel;
        private final double speedModifier;
        private BlockPos shelterTargetPos;
        private int localWanderCooldownTicks;
        private int searchCooldownTicks;

        public SquirrelSeekShelterGoal(SquirrelEntity squirrel, double speedModifier) {
            this.squirrel = squirrel;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.squirrel.isSheltering() || this.squirrel.isDeliveringGift() || this.squirrel.isWiggling() || this.squirrel.isBaby()) {
                return false;
            }

            if (this.squirrel.level().isDay()) {
                return false;
            }

            if (this.shelterTargetPos != null && this.isValidShelterStandPos(this.squirrel.level(), this.shelterTargetPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.shelterTargetPos = this.findShelterTarget();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.shelterTargetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.squirrel.level().isDay() || this.squirrel.isPanicking()) {
                return false;
            }

            return this.shelterTargetPos != null && this.isValidShelterStandPos(this.squirrel.level(), this.shelterTargetPos);
        }

        @Override
        public void start() {
            this.squirrel.setSheltering(true);
            this.localWanderCooldownTicks = 0;
            this.moveToShelterTarget();
        }

        @Override
        public void stop() {
            this.squirrel.setSheltering(false);
            this.squirrel.getNavigation().stop();
            this.shelterTargetPos = null;
            this.localWanderCooldownTicks = 0;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.shelterTargetPos == null) {
                return;
            }

            if (!this.isValidShelterStandPos(this.squirrel.level(), this.shelterTargetPos)) {
                this.shelterTargetPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.hasReachedShelter()) {
                this.moveToShelterTarget();
                return;
            }

            this.squirrel.getNavigation().stop();
            this.squirrel.getLookControl().setLookAt(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D);

            if (this.localWanderCooldownTicks > 0) {
                this.localWanderCooldownTicks--;
                return;
            }

            BlockPos nearbyShelterPos = this.findNearbyShelterRestPos();
            if (nearbyShelterPos != null && !nearbyShelterPos.equals(this.shelterTargetPos)) {
                this.shelterTargetPos = nearbyShelterPos;
                this.moveToShelterTarget();
            }

            this.localWanderCooldownTicks = SquirrelEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MIN + this.squirrel.getRandom().nextInt(SquirrelEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MAX - SquirrelEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MIN + 1);
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.squirrel.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        private void moveToShelterTarget() {
            this.squirrel.getNavigation().moveTo(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D, this.speedModifier * 0.55D);
        }

        private boolean hasReachedShelter() {
            return this.shelterTargetPos.closerToCenterThan(this.squirrel.position(), 1.35D);
        }

        @Nullable
        private BlockPos findShelterTarget() {
            BlockPos cachePos = this.findNearestStumpShelterPos();
            if (cachePos != null) {
                return cachePos;
            }

            BlockPos logPos = this.findNearestTaggedStandPos(BlockTags.LOGS, 10, -4, 8);
            if (logPos != null) {
                return logPos;
            }

            BlockPos leavesPos = this.findNearestTaggedStandPos(BlockTags.LEAVES, 8, -3, 6);
            if (leavesPos != null) {
                return leavesPos;
            }

            return this.findNearestGroundUnderTreePos();
        }

        @Nullable
        private BlockPos findNearestStumpShelterPos() {
            BlockPos originPos = this.squirrel.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestShelterPos = null;
            double closestDistance = Double.MAX_VALUE;
            int horizontalRange = 10;
            int minYOffset = -7;
            int maxYOffset = 8;

            for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
                for (int offsetY = minYOffset; offsetY <= maxYOffset; offsetY++) {
                    for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.squirrel.level().getBlockState(mutableBlockPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                            continue;
                        }

                        BlockPos standPos = mutableBlockPos.above().immutable();
                        if (!this.isValidShelterStandPos(this.squirrel.level(), standPos)) {
                            continue;
                        }

                        double checkedDistance = standPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestShelterPos = standPos;
                        }
                    }
                }
            }

            return closestShelterPos;
        }

        @Nullable
        private BlockPos findNearestTaggedStandPos(TagKey<Block> blockTag, int horizontalRange, int minYOffset, int maxYOffset) {
            BlockPos originPos = this.squirrel.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestShelterPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
                for (int offsetY = minYOffset; offsetY <= maxYOffset; offsetY++) {
                    for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.squirrel.level().getBlockState(mutableBlockPos).is(blockTag)) {
                            continue;
                        }

                        BlockPos standPos = mutableBlockPos.above().immutable();
                        if (!this.isValidShelterStandPos(this.squirrel.level(), standPos)) {
                            continue;
                        }

                        double checkedDistance = standPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestShelterPos = standPos;
                        }
                    }
                }
            }

            return closestShelterPos;
        }

        @Nullable
        private BlockPos findNearestGroundUnderTreePos() {
            BlockPos originPos = this.squirrel.blockPosition();
            BlockPos.MutableBlockPos mutableGroundPos = new BlockPos.MutableBlockPos();
            BlockPos closestGroundPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -10; offsetX <= 10; offsetX++) {
                for (int offsetY = -3; offsetY <= 3; offsetY++) {
                    for (int offsetZ = -10; offsetZ <= 10; offsetZ++) {
                        mutableGroundPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        BlockPos standPos = mutableGroundPos.above().immutable();

                        if (!this.isValidShelterStandPos(this.squirrel.level(), standPos)) {
                            continue;
                        }

                        if (!this.hasTreeCover(standPos)) {
                            continue;
                        }

                        double checkedDistance = standPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestGroundPos = standPos;
                        }
                    }
                }
            }

            return closestGroundPos;
        }

        @Nullable
        private BlockPos findNearbyShelterRestPos() {
            if (this.shelterTargetPos == null) {
                return null;
            }

            for (int attemptIndex = 0; attemptIndex < 8; attemptIndex++) {
                int randomOffsetX = Mth.nextInt(this.squirrel.getRandom(), -SquirrelEntity.SHELTER_LOCAL_WANDER_RADIUS, SquirrelEntity.SHELTER_LOCAL_WANDER_RADIUS);
                int randomOffsetZ = Mth.nextInt(this.squirrel.getRandom(), -SquirrelEntity.SHELTER_LOCAL_WANDER_RADIUS, SquirrelEntity.SHELTER_LOCAL_WANDER_RADIUS);
                BlockPos candidatePos = this.shelterTargetPos.offset(randomOffsetX, 0, randomOffsetZ);
                if (this.isValidShelterStandPos(this.squirrel.level(), candidatePos) && this.isStillShelterArea(candidatePos)) {
                    return candidatePos;
                }
            }

            return this.shelterTargetPos;
        }

        private boolean hasTreeCover(BlockPos standPos) {
            BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();

            for (int dy = 1; dy <= 4; dy++) {
                m.set(standPos.getX(), standPos.getY() + dy, standPos.getZ());
                BlockState state = this.squirrel.level().getBlockState(m);
                if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
                    return true;
                }
            }

            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    m.set(standPos.getX() + dx, standPos.getY() + 1, standPos.getZ() + dz);
                    BlockState state = this.squirrel.level().getBlockState(m);
                    if (state.is(BlockTags.LEAVES) || state.is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean isStillShelterArea(BlockPos standPos) {
            BlockState belowState = this.squirrel.level().getBlockState(standPos.below());
            if (belowState.is(ObjectRegistry.HOLLOW_CACHE.get()) || belowState.is(BlockTags.LOGS) || belowState.is(BlockTags.LEAVES)) {
                return true;
            }

            return this.hasTreeCover(standPos);
        }

        private boolean isValidShelterStandPos(LevelReader level, BlockPos standPos) {
            if (!level.getBlockState(standPos).isAir()) {
                return false;
            }

            if (!level.getBlockState(standPos.above()).isAir()) {
                return false;
            }

            return level.getBlockState(standPos.below()).isSolidRender(level, standPos.below());
        }
    }

    public static class SquirrelStoreInventoryGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 100;
        private static final int SEARCH_COOLDOWN_MAX = 180;

        private final SquirrelEntity squirrel;
        private final double speedModifier;
        private BlockPos cachePos;
        private int storeWiggleTicks;
        private int searchCooldownTicks;

        public SquirrelStoreInventoryGoal(SquirrelEntity squirrel, double speedModifier) {
            this.squirrel = squirrel;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.squirrel.hasStoredItems()) {
                return false;
            }

            if (this.squirrel.getCacheStoreCooldownTicks() > 0 || this.squirrel.isPanicking() || this.squirrel.isWiggling() || this.squirrel.isDeliveringGift() || this.squirrel.isSheltering() || this.squirrel.level().isNight()) {
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
            return this.cachePos != null && this.squirrel.hasStoredItems() && !this.squirrel.isPanicking() && !this.squirrel.level().isNight() && this.isValidCache(this.cachePos);
        }

        @Override
        public void start() {
            this.storeWiggleTicks = 0;
            this.moveToCache();
        }

        @Override
        public void stop() {
            if (this.cachePos != null && this.squirrel.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
                HollowCacheBlock.setOpen(this.squirrel.level().getBlockState(this.cachePos), this.squirrel.level(), this.cachePos, false);
                hollowCacheBlockEntity.scheduleCloseParticles();
            }

            this.squirrel.getNavigation().stop();
            this.cachePos = null;
            this.storeWiggleTicks = 0;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.cachePos == null) {
                return;
            }

            if (!(this.squirrel.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
                this.cachePos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!hollowCacheBlockEntity.hasFreeSlot()) {
                this.cachePos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.cachePos.closerToCenterThan(this.squirrel.position(), 1.75D)) {
                this.moveToCache();
                return;
            }

            this.squirrel.getNavigation().stop();
            this.squirrel.getLookControl().setLookAt(this.cachePos.getX() + 0.5D, this.cachePos.getY() + 0.5D, this.cachePos.getZ() + 0.5D);
            HollowCacheBlock.setOpen(this.squirrel.level().getBlockState(this.cachePos), this.squirrel.level(), this.cachePos, true);

            if (this.storeWiggleTicks <= 0) {
                this.storeWiggleTicks = SquirrelEntity.CACHE_STORE_WIGGLE_DURATION;
                this.squirrel.startWiggle(SquirrelEntity.CACHE_STORE_WIGGLE_DURATION);
                return;
            }

            this.storeWiggleTicks--;
            if (this.storeWiggleTicks > 0) {
                return;
            }

            boolean depositedAnyItem = this.squirrel.depositInventoryIntoCache(hollowCacheBlockEntity);
            HollowCacheBlock.setOpen(this.squirrel.level().getBlockState(this.cachePos), this.squirrel.level(), this.cachePos, false);
            hollowCacheBlockEntity.scheduleCloseParticles();
            if (depositedAnyItem) {
                this.squirrel.startCacheStoreCooldown();
            }
            this.cachePos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.squirrel.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        private void moveToCache() {
            this.squirrel.getNavigation().moveTo(this.cachePos.getX() + 0.5D, this.cachePos.getY(), this.cachePos.getZ() + 0.5D, this.speedModifier);
        }

        private boolean isValidCache(BlockPos checkedPos) {
            if (!this.squirrel.level().getBlockState(checkedPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                return false;
            }

            if (!(this.squirrel.level().getBlockEntity(checkedPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
                return false;
            }

            return hollowCacheBlockEntity.hasFreeSlot();
        }

        @Nullable
        private BlockPos findNearestAvailableCache() {
            BlockPos originPos = this.squirrel.blockPosition();
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            BlockPos closest = null;
            double closestDist = Double.MAX_VALUE;

            int range = Math.min(12, SquirrelEntity.CACHE_SEARCH_RANGE);

            for (int x = -range; x <= range; x++) {
                for (int y = -5; y <= 5; y++) {
                    for (int z = -range; z <= range; z++) {
                        mutable.set(originPos.getX() + x, originPos.getY() + y, originPos.getZ() + z);

                        if (!this.squirrel.level().getBlockState(mutable).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                            continue;
                        }

                        if (!(this.squirrel.level().getBlockEntity(mutable) instanceof HollowCacheBlockEntity be)) {
                            continue;
                        }

                        if (!be.hasFreeSlot()) {
                            continue;
                        }

                        double dist = mutable.distSqr(originPos);
                        if (dist < closestDist) {
                            closestDist = dist;
                            closest = mutable.immutable();
                        }
                    }
                }
            }
            return closest;
        }
    }
}