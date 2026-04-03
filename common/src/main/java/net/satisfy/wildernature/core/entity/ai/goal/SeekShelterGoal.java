package net.satisfy.wildernature.core.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.entity.ai.behavior.ShelteringMob;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

public class SeekShelterGoal<T extends PathfinderMob & ShelteringMob> extends Goal {
    private static final int SEARCH_COOLDOWN_MIN = 100;
    private static final int SEARCH_COOLDOWN_MAX = 180;

    private final T mob;
    private final double speedModifier;
    private BlockPos shelterTargetPos;
    private int localWanderCooldownTicks;
    private int searchCooldownTicks;

    public SeekShelterGoal(T mob, double speedModifier) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isSheltering() || this.mob.isBaby() || this.mob.isPanicking()) {
            return false;
        }

        if (this.mob.level().isDay()) {
            return false;
        }

        if (this.shelterTargetPos != null && this.isValidShelterStandPos(this.mob.level(), this.shelterTargetPos)) {
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
        if (this.mob.level().isDay() || this.mob.isPanicking()) {
            return false;
        }

        return this.shelterTargetPos != null && this.isValidShelterStandPos(this.mob.level(), this.shelterTargetPos);
    }

    @Override
    public void start() {
        this.mob.setSheltering(true);
        this.localWanderCooldownTicks = 0;
        this.moveToShelterTarget();
    }

    @Override
    public void stop() {
        this.mob.setSheltering(false);
        this.mob.getNavigation().stop();
        this.shelterTargetPos = null;
        this.localWanderCooldownTicks = 0;
        this.searchCooldownTicks = 0;
    }

    @Override
    public void tick() {
        if (this.shelterTargetPos == null) {
            return;
        }

        if (!this.isValidShelterStandPos(this.mob.level(), this.shelterTargetPos)) {
            this.shelterTargetPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
            return;
        }

        if (!this.hasReachedShelter()) {
            this.moveToShelterTarget();
            return;
        }

        this.mob.getNavigation().stop();
        this.mob.getLookControl().setLookAt(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D);

        if (this.localWanderCooldownTicks > 0) {
            this.localWanderCooldownTicks--;
            return;
        }

        BlockPos nearbyShelterPos = this.findNearbyShelterRestPos();
        if (nearbyShelterPos != null && !nearbyShelterPos.equals(this.shelterTargetPos)) {
            this.shelterTargetPos = nearbyShelterPos;
            this.moveToShelterTarget();
        }

        this.localWanderCooldownTicks = this.mob.getShelterLocalWanderCooldownMin() + this.mob.getRandom().nextInt(this.mob.getShelterLocalWanderCooldownMax() - this.mob.getShelterLocalWanderCooldownMin() + 1);
    }

    private int getNextSearchCooldown() {
        return SEARCH_COOLDOWN_MIN + this.mob.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
    }

    private void moveToShelterTarget() {
        this.mob.getNavigation().moveTo(this.shelterTargetPos.getX() + 0.5D, this.shelterTargetPos.getY(), this.shelterTargetPos.getZ() + 0.5D, this.speedModifier);
    }

    private boolean hasReachedShelter() {
        return this.shelterTargetPos.closerToCenterThan(this.mob.position(), 1.35D);
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
        BlockPos originPos = this.mob.blockPosition();
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
                    if (!this.mob.level().getBlockState(mutableBlockPos).is(ObjectRegistry.HOLLOW_CACHE.get())) {
                        continue;
                    }

                    BlockPos standPos = mutableBlockPos.above().immutable();
                    if (!this.isValidShelterStandPos(this.mob.level(), standPos)) {
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
        BlockPos originPos = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos closestShelterPos = null;
        double closestDistance = Double.MAX_VALUE;

        for (int offsetX = -horizontalRange; offsetX <= horizontalRange; offsetX++) {
            for (int offsetY = minYOffset; offsetY <= maxYOffset; offsetY++) {
                for (int offsetZ = -horizontalRange; offsetZ <= horizontalRange; offsetZ++) {
                    mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                    if (!this.mob.level().getBlockState(mutableBlockPos).is(blockTag)) {
                        continue;
                    }

                    BlockPos standPos = mutableBlockPos.above().immutable();
                    if (!this.isValidShelterStandPos(this.mob.level(), standPos)) {
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
        BlockPos originPos = this.mob.blockPosition();
        BlockPos.MutableBlockPos mutableGroundPos = new BlockPos.MutableBlockPos();
        BlockPos closestGroundPos = null;
        double closestDistance = Double.MAX_VALUE;

        for (int offsetX = -10; offsetX <= 10; offsetX++) {
            for (int offsetY = -3; offsetY <= 3; offsetY++) {
                for (int offsetZ = -10; offsetZ <= 10; offsetZ++) {
                    mutableGroundPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                    BlockPos standPos = mutableGroundPos.above().immutable();

                    if (!this.isValidShelterStandPos(this.mob.level(), standPos)) {
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
            int randomOffsetX = Mth.nextInt(this.mob.getRandom(), -this.mob.getShelterLocalWanderRadius(), this.mob.getShelterLocalWanderRadius());
            int randomOffsetZ = Mth.nextInt(this.mob.getRandom(), -this.mob.getShelterLocalWanderRadius(), this.mob.getShelterLocalWanderRadius());
            BlockPos candidatePos = this.shelterTargetPos.offset(randomOffsetX, 0, randomOffsetZ);
            if (this.isValidShelterStandPos(this.mob.level(), candidatePos) && this.isStillShelterArea(candidatePos)) {
                return candidatePos;
            }
        }

        return this.shelterTargetPos;
    }

    private boolean hasTreeCover(BlockPos standPos) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int verticalOffset = 1; verticalOffset <= 4; verticalOffset++) {
            mutableBlockPos.set(standPos.getX(), standPos.getY() + verticalOffset, standPos.getZ());
            BlockState checkedState = this.mob.level().getBlockState(mutableBlockPos);
            if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                return true;
            }
        }

        for (int horizontalOffsetX = -2; horizontalOffsetX <= 2; horizontalOffsetX++) {
            for (int horizontalOffsetZ = -2; horizontalOffsetZ <= 2; horizontalOffsetZ++) {
                mutableBlockPos.set(standPos.getX() + horizontalOffsetX, standPos.getY() + 1, standPos.getZ() + horizontalOffsetZ);
                BlockState checkedState = this.mob.level().getBlockState(mutableBlockPos);
                if (checkedState.is(BlockTags.LEAVES) || checkedState.is(BlockTags.LOGS)) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isStillShelterArea(BlockPos standPos) {
        BlockState belowState = this.mob.level().getBlockState(standPos.below());
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