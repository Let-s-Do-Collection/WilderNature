package net.satisfy.wildernature.core.entity.ai;

import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.animal.RaccoonEntity;
import net.satisfy.wildernature.core.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

public class RaccoonGoals {

    public static class RaccoonFloatGoal extends FloatGoal {
        public RaccoonFloatGoal(RaccoonEntity raccoon) {
            super(raccoon);
        }
    }

    public static class RaccoonPanicGoal extends PanicGoal {
        private final RaccoonEntity raccoon;

        public RaccoonPanicGoal(RaccoonEntity raccoon, double speedModifier) {
            super(raccoon, speedModifier);
            this.raccoon = raccoon;
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.startRunningAnim();
            super.start();
        }

        @Override
        public void stop() {
            this.raccoon.stopRunningAnim();
            super.stop();
        }
    }

    public static class RaccoonAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final RaccoonEntity raccoon;

        public RaccoonAvoidEntityGoal(RaccoonEntity raccoon, Class<T> targetClass) {
            super(raccoon, targetClass, 16.0F, 2.0D, 2.0D);
            this.raccoon = raccoon;
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.startRunningAnim();
            super.start();
        }

        @Override
        public void stop() {
            this.raccoon.stopRunningAnim();
            super.stop();
        }
    }

    public static class RaccoonDoorInteractGoal extends DoorInteractGoal {
        private final RaccoonEntity raccoon;
        private int counter;

        public RaccoonDoorInteractGoal(RaccoonEntity raccoon) {
            super(raccoon);
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        @Override
        public void start() {
            this.counter = 0;
            Objects.requireNonNull(this.raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(RaccoonEntity.DOOR_DO_NOT_MOVE_MODIFIER);
            super.start();
            this.raccoon.startOpenDoorAnim();
        }

        @Override
        public boolean canContinueToUse() {
            return this.counter > 0 && this.counter < ServerAnimationDurations.raccoon_opening_door_length && (!this.isOpen() || this.counter >= ServerAnimationDurations.raccoon_opening_door_tick);
        }

        @Override
        public void tick() {
            if (this.canContinueToUse()) {
                this.raccoon.startOpenDoorAnim();
            } else {
                this.raccoon.stopOpenDoorAnim();
            }

            if (this.counter < ServerAnimationDurations.raccoon_opening_door_length) {
                this.counter++;
            }

            if (this.counter == ServerAnimationDurations.raccoon_opening_door_tick) {
                this.setOpen(true);
            }

            if (this.counter == ServerAnimationDurations.raccoon_opening_door_length) {
                super.tick();
            }
        }

        @Override
        public void stop() {
            Objects.requireNonNull(this.raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(RaccoonEntity.DOOR_DO_NOT_MOVE_MODIFIER.id());
            this.counter = 0;
            super.stop();
            this.setOpen(true);
            this.raccoon.stopOpenDoorAnim();
        }
    }

    public static class RaccoonSeekShelterGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 100;
        private static final int SEARCH_COOLDOWN_MAX = 180;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos shelterTargetPos;
        private int localWanderCooldownTicks;
        private int searchCooldownTicks;

        public RaccoonSeekShelterGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSheltering() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isBaby() || this.raccoon.isSleeping()) {
                return false;
            }

            if (this.raccoon.level().isDay()) {
                return false;
            }

            if (this.shelterTargetPos != null && this.isValidShelterStandPos(this.raccoon.level(), this.shelterTargetPos)) {
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
            return !this.raccoon.level().isDay() && !this.raccoon.isPanicking() && !this.raccoon.isSleeping() && this.shelterTargetPos != null && this.isValidShelterStandPos(this.raccoon.level(), this.shelterTargetPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.setSheltering(true);
            this.localWanderCooldownTicks = 0;
            this.moveToShelterTarget();
        }

        @Override
        public void stop() {
            this.raccoon.setSheltering(false);
            this.raccoon.getNavigation().stop();
            this.shelterTargetPos = null;
            this.localWanderCooldownTicks = 0;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.shelterTargetPos == null) {
                return;
            }

            if (!this.isValidShelterStandPos(this.raccoon.level(), this.shelterTargetPos)) {
                this.shelterTargetPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.hasReachedShelter()) {
                this.moveToShelterTarget();
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D);

            if (this.localWanderCooldownTicks > 0) {
                this.localWanderCooldownTicks--;
                return;
            }

            BlockPos nearbyShelterPos = this.findNearbyShelterRestPos();
            if (nearbyShelterPos != null && !nearbyShelterPos.equals(this.shelterTargetPos)) {
                this.shelterTargetPos = nearbyShelterPos;
                this.moveToShelterTarget();
            }

            this.localWanderCooldownTicks = RaccoonEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(RaccoonEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MAX - RaccoonEntity.SHELTER_LOCAL_WANDER_COOLDOWN_MIN + 1);
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        private void moveToShelterTarget() {
            this.raccoon.getNavigation().moveTo(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D, this.speedModifier * 0.55D);
        }

        private boolean hasReachedShelter() {
            return this.shelterTargetPos.closerToCenterThan(this.raccoon.position(), 1.35D);
        }

        @Nullable
        private BlockPos findShelterTarget() {
            BlockPos cachePos = this.findNearestCacheShelterPos();
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
        private BlockPos findNearestCacheShelterPos() {
            BlockPos originPos = this.raccoon.blockPosition();
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
                        if (!this.raccoon.level().getBlockState(mutableBlockPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                            continue;
                        }

                        BlockPos standPos = mutableBlockPos.above().immutable();
                        if (!this.isValidShelterStandPos(this.raccoon.level(), standPos)) {
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
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestShelterPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
                for (int offsetY = minYOffset; offsetY <= maxYOffset; offsetY++) {
                    for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.raccoon.level().getBlockState(mutableBlockPos).is(blockTag)) {
                            continue;
                        }

                        BlockPos standPos = mutableBlockPos.above().immutable();
                        if (!this.isValidShelterStandPos(this.raccoon.level(), standPos)) {
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
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableGroundPos = new BlockPos.MutableBlockPos();
            BlockPos closestGroundPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -10; offsetX <= 10; offsetX++) {
                for (int offsetY = -3; offsetY <= 3; offsetY++) {
                    for (int offsetZ = -10; offsetZ <= 10; offsetZ++) {
                        mutableGroundPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        BlockPos standPos = mutableGroundPos.above().immutable();

                        if (!this.isValidShelterStandPos(this.raccoon.level(), standPos)) {
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
                int randomOffsetX = Mth.nextInt(this.raccoon.getRandom(), -RaccoonEntity.SHELTER_LOCAL_WANDER_RADIUS, RaccoonEntity.SHELTER_LOCAL_WANDER_RADIUS);
                int randomOffsetZ = Mth.nextInt(this.raccoon.getRandom(), -RaccoonEntity.SHELTER_LOCAL_WANDER_RADIUS, RaccoonEntity.SHELTER_LOCAL_WANDER_RADIUS);
                BlockPos candidatePos = this.shelterTargetPos.offset(randomOffsetX, 0, randomOffsetZ);
                if (this.isValidShelterStandPos(this.raccoon.level(), candidatePos) && this.isStillShelterArea(candidatePos)) {
                    return candidatePos;
                }
            }

            return this.shelterTargetPos;
        }

        private boolean hasTreeCover(BlockPos standPos) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

            for (int verticalOffset = 1; verticalOffset <= 4; verticalOffset++) {
                mutableBlockPos.set(standPos.getX(), standPos.getY() + verticalOffset, standPos.getZ());
                BlockState checkedState = this.raccoon.level().getBlockState(mutableBlockPos);
                if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                    return true;
                }
            }

            for (int horizontalOffsetX = -2; horizontalOffsetX <= 2; horizontalOffsetX++) {
                for (int horizontalOffsetZ = -2; horizontalOffsetZ <= 2; horizontalOffsetZ++) {
                    mutableBlockPos.set(standPos.getX() + horizontalOffsetX, standPos.getY() + 1, standPos.getZ() + horizontalOffsetZ);
                    BlockState checkedState = this.raccoon.level().getBlockState(mutableBlockPos);
                    if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                        return true;
                    }
                }
            }

            return false;
        }

        private boolean isStillShelterArea(BlockPos standPos) {
            BlockState belowState = this.raccoon.level().getBlockState(standPos.below());
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

    public static class RaccoonVillageStrollGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 140;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetDoorPos;
        private int searchCooldownTicks;

        public RaccoonVillageStrollGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.level().isDay() || this.raccoon.isPanicking() || this.raccoon.isSheltering() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isSleeping()) {
                return false;
            }

            if (this.targetDoorPos != null && this.isValidDoor(this.targetDoorPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetDoorPos = this.findNearestDoor();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetDoorPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.raccoon.level().isDay() && !this.raccoon.isPanicking() && !this.raccoon.isSleeping() && this.targetDoorPos != null && this.isValidDoor(this.targetDoorPos);
        }

        @Override
        public void stop() {
            this.raccoon.getNavigation().stop();
            this.targetDoorPos = null;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetDoorPos == null) {
                return;
            }

            if (!this.isValidDoor(this.targetDoorPos)) {
                this.targetDoorPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            this.raccoon.getNavigation().moveTo(this.targetDoorPos.getX() + 0.5D, this.targetDoorPos.getY(), this.targetDoorPos.getZ() + 0.5D, this.speedModifier);
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestDoor() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestDoorPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.VILLAGE_STROLL_RANGE; offsetX <= RaccoonEntity.VILLAGE_STROLL_RANGE; offsetX++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.VILLAGE_STROLL_RANGE; offsetZ <= RaccoonEntity.VILLAGE_STROLL_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.isValidDoor(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestDoorPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestDoorPos;
        }

        private boolean isValidDoor(BlockPos blockPos) {
            return this.raccoon.level().getBlockState(blockPos).is(BlockTags.WOODEN_DOORS);
        }
    }

    public static class RaccoonOpenContainerGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 140;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetContainerPos;
        private int searchCooldownTicks;

        public RaccoonOpenContainerGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.raccoon.canLootContainers()) {
                return false;
            }

            if (this.targetContainerPos != null && this.isValidContainer(this.targetContainerPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetContainerPos = this.findNearestContainer();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetContainerPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.raccoon.canLootContainers() && this.targetContainerPos != null && this.isValidContainer(this.targetContainerPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.setContainerLooting(true);
        }

        @Override
        public void stop() {
            this.raccoon.setContainerLooting(false);
            this.raccoon.getNavigation().stop();
            this.targetContainerPos = null;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetContainerPos == null) {
                return;
            }

            if (!this.isValidContainer(this.targetContainerPos)) {
                this.targetContainerPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.targetContainerPos.closerToCenterThan(this.raccoon.position(), 2.0D)) {
                this.raccoon.getNavigation().moveTo(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY(), this.targetContainerPos.getZ() + 0.5D, this.speedModifier);
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY() + 0.5D, this.targetContainerPos.getZ() + 0.5D);

            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);
            if (blockEntity instanceof Container container && this.raccoon.tryTakeItemFromContainer(container)) {
                this.targetContainerPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return;
            }

            this.targetContainerPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestContainer() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestContainerPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.isValidContainer(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestContainerPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestContainerPos;
        }

        private boolean isValidContainer(BlockPos blockPos) {
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(blockPos);
            if (!(blockEntity instanceof Container container)) {
                return false;
            }

            if (blockEntity instanceof HollowCacheBlockEntity) {
                return false;
            }

            for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
                ItemStack itemStack = container.getItem(slotIndex);
                if ((!itemStack.isEmpty() && this.raccoon.isFood(itemStack)) || (!itemStack.isEmpty() && itemStack.is(net.minecraft.tags.ItemTags.VILLAGER_PLANTABLE_SEEDS))) {
                    return true;
                }
            }

            return false;
        }
    }

    public static class RaccoonStoreLootGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 140;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos cachePos;
        private int storeWiggleTicks;
        private int searchCooldownTicks;

        public RaccoonStoreLootGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.raccoon.canStoreLoot()) {
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
            return this.raccoon.canStoreLoot() && this.cachePos != null && this.isValidCache(this.cachePos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.setStoringLoot(true);
            this.storeWiggleTicks = 0;
            this.moveToCache();
        }

        @Override
        public void stop() {
            if (this.cachePos != null && this.raccoon.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
                HollowCacheBlock.setOpen(this.raccoon.level().getBlockState(this.cachePos), this.raccoon.level(), this.cachePos, false);
                hollowCacheBlockEntity.scheduleCloseParticles();
            }

            this.raccoon.setStoringLoot(false);
            this.raccoon.getNavigation().stop();
            this.cachePos = null;
            this.storeWiggleTicks = 0;
            this.searchCooldownTicks = 0;
        }

        @Override
        public void tick() {
            if (this.cachePos == null) {
                return;
            }

            if (!(this.raccoon.level().getBlockEntity(this.cachePos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
                this.cachePos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!hollowCacheBlockEntity.hasFreeSlot()) {
                this.cachePos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.cachePos.closerToCenterThan(this.raccoon.position(), 1.75D)) {
                this.moveToCache();
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.cachePos.getX() + 0.5D, this.cachePos.getY() + 0.5D, this.cachePos.getZ() + 0.5D);
            HollowCacheBlock.setOpen(this.raccoon.level().getBlockState(this.cachePos), this.raccoon.level(), this.cachePos, true);

            if (this.storeWiggleTicks <= 0) {
                this.storeWiggleTicks = RaccoonEntity.CACHE_STORE_WIGGLE_DURATION;
                return;
            }

            this.storeWiggleTicks--;
            if (this.storeWiggleTicks > 0) {
                return;
            }

            boolean depositedAnyItem = this.raccoon.depositLootIntoCache(hollowCacheBlockEntity);
            HollowCacheBlock.setOpen(this.raccoon.level().getBlockState(this.cachePos), this.raccoon.level(), this.cachePos, false);
            hollowCacheBlockEntity.scheduleCloseParticles();
            if (depositedAnyItem) {
                this.raccoon.startStoreLootCooldown();
            }
            this.cachePos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        private void moveToCache() {
            this.raccoon.getNavigation().moveTo(this.cachePos.getX() + 0.5D, this.cachePos.getY(), this.cachePos.getZ() + 0.5D, this.speedModifier);
        }

        private boolean isValidCache(BlockPos checkedPos) {
            if (!this.raccoon.level().getBlockState(checkedPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                return false;
            }

            if (!(this.raccoon.level().getBlockEntity(checkedPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
                return false;
            }

            return hollowCacheBlockEntity.hasFreeSlot();
        }

        @Nullable
        private BlockPos findNearestAvailableCache() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestCachePos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.CACHE_SEARCH_RANGE; offsetX <= RaccoonEntity.CACHE_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -5; offsetY <= 5; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.CACHE_SEARCH_RANGE; offsetZ <= RaccoonEntity.CACHE_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.raccoon.level().getBlockState(mutableBlockPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                            continue;
                        }

                        if (!(this.raccoon.level().getBlockEntity(mutableBlockPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity)) {
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

    public static class RaccoonWashSelfGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 140;

        private final RaccoonEntity raccoon;
        private int washTicks;
        private int searchCooldownTicks;

        public RaccoonWashSelfGoal(RaccoonEntity raccoon) {
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            return this.raccoon.getRandom().nextFloat() < 0.015F;
        }

        @Override
        public boolean canContinueToUse() {
            return this.washTicks > 0 && !this.raccoon.isPanicking() && !this.raccoon.isSleeping();
        }

        @Override
        public void start() {
            this.raccoon.getNavigation().stop();
            this.raccoon.startWash();
            this.washTicks = 60 + this.raccoon.getRandom().nextInt(40);
        }

        @Override
        public void stop() {
            this.raccoon.stopWash();
            this.washTicks = 0;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        @Override
        public void tick() {
            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.raccoon.getX(), this.raccoon.getEyeY(), this.raccoon.getZ());

            if (this.washTicks > 0) {
                this.washTicks--;
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }
    }

    public static class RaccoonCuriosityGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 160;
        private static final double CURIOSITY_RADIUS = 7.0D;

        private final RaccoonEntity raccoon;
        private Player targetPlayer;
        private int curiosityTicks;
        private int searchCooldownTicks;

        public RaccoonCuriosityGoal(RaccoonEntity raccoon) {
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isRaccoonRunning()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            Player nearestPlayer = this.raccoon.level().getNearestPlayer(this.raccoon, CURIOSITY_RADIUS);
            if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator() || nearestPlayer.isSprinting()) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            if (this.raccoon.getRandom().nextFloat() >= 0.12F) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            this.targetPlayer = nearestPlayer;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.targetPlayer.isAlive() && !this.targetPlayer.isCreative() && !this.targetPlayer.isSpectator() && !this.targetPlayer.isSprinting() && this.raccoon.distanceToSqr(this.targetPlayer) <= CURIOSITY_RADIUS * CURIOSITY_RADIUS && this.curiosityTicks > 0 && !this.raccoon.isPanicking() && !this.raccoon.isSleeping();
        }

        @Override
        public void start() {
            this.raccoon.getNavigation().stop();
            this.curiosityTicks = 30 + this.raccoon.getRandom().nextInt(30);
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.curiosityTicks = 0;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.curiosityTicks > 0) {
                this.curiosityTicks--;
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }
    }

    public static class RaccoonNibbleCropGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 140;
        private static final int SEARCH_COOLDOWN_MAX = 260;
        private static final int NIBBLE_DURATION_MIN = 20;
        private static final int NIBBLE_DURATION_MAX = 40;
        private static final int CROP_SEARCH_RANGE = 8;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetCropPos;
        private int searchCooldownTicks;
        private int nibbleTicks;

        public RaccoonNibbleCropGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.level().isDay()) {
                return false;
            }

            if (!(this.raccoon.level() instanceof ServerLevel serverLevel) || !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }

            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isSheltering() || this.raccoon.isBaby()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            if (this.raccoon.getRandom().nextFloat() >= 0.08F) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            this.targetCropPos = this.findNearestCrop();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetCropPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetCropPos != null && !this.raccoon.level().isDay() && !this.raccoon.isPanicking() && !this.raccoon.isSleeping() && this.isValidCrop(this.targetCropPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.nibbleTicks = 0;
        }

        @Override
        public void stop() {
            this.raccoon.getNavigation().stop();
            this.targetCropPos = null;
            this.nibbleTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetCropPos == null) {
                return;
            }

            if (!this.isValidCrop(this.targetCropPos)) {
                this.targetCropPos = null;
                return;
            }

            if (!this.targetCropPos.closerToCenterThan(this.raccoon.position(), 1.6D)) {
                this.raccoon.getNavigation().moveTo(this.targetCropPos.getX() + 0.5D, this.targetCropPos.getY(), this.targetCropPos.getZ() + 0.5D, this.speedModifier);
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetCropPos.getX() + 0.5D, this.targetCropPos.getY() + 0.5D, this.targetCropPos.getZ() + 0.5D);

            if (this.nibbleTicks <= 0) {
                this.nibbleTicks = NIBBLE_DURATION_MIN + this.raccoon.getRandom().nextInt(NIBBLE_DURATION_MAX - NIBBLE_DURATION_MIN + 1);
                return;
            }

            this.nibbleTicks--;
            if (this.nibbleTicks > 0) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetCropPos);
            if (blockState.getBlock() instanceof CropBlock cropBlock) {
                IntegerProperty ageProperty = cropBlock.getAgeProperty();
                int currentAge = blockState.getValue(ageProperty);
                int reducedAge = Math.max(0, currentAge - 1);
                this.raccoon.level().setBlock(this.targetCropPos, blockState.setValue(ageProperty, reducedAge), 2);
            }

            this.targetCropPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestCrop() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestCropPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -CROP_SEARCH_RANGE; offsetX <= CROP_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -2; offsetY <= 2; offsetY++) {
                    for (int offsetZ = -CROP_SEARCH_RANGE; offsetZ <= CROP_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.isValidCrop(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestCropPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestCropPos;
        }

        private boolean isValidCrop(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof CropBlock cropBlock)) {
                return false;
            }

            IntegerProperty ageProperty = cropBlock.getAgeProperty();
            int currentAge = blockState.getValue(ageProperty);
            int maximumAge = cropBlock.getMaxAge();
            return currentAge >= Math.max(1, maximumAge - 1);
        }
    }
}