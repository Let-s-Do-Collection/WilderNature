package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.RottenLogBlock;
import net.satisfy.wildernature.core.block.entity.BurrowBlockEntity;
import net.satisfy.wildernature.core.block.entity.RottenLogBlockEntity;
import net.satisfy.wildernature.core.block.entity.TermiteMoundBlockEntity;
import net.satisfy.wildernature.core.entity.animal.passive.TermiteEntity;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class TermiteGoals {
    private static final double STAND_REACH_DISTANCE = 2.0D;
    private static final double STAND_CLOSE_ENOUGH_DISTANCE = 1.25D;
    private static final double TARGET_INTERACT_DISTANCE_SQUARED = 3.0D;
    private static final double RETURN_RANGE_PADDING = 2.0D;
    private static final double NAVIGATION_SPEED = 1.0D;

    private record LogCandidate(BlockPos targetPos, BlockPos standPos) {
    }

    @Nullable
    private static BlockPos findBestStandPos(TermiteEntity termite, BlockPos targetPos) {
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidatePos = targetPos.relative(direction);
            if (!isValidStandPos(termite, candidatePos, targetPos)) {
                continue;
            }

            double distance = termite.blockPosition().distSqr(candidatePos);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = candidatePos;
            }
        }

        BlockPos abovePos = targetPos.above();
        if (bestPos == null && isValidStandPos(termite, abovePos, targetPos)) {
            bestPos = abovePos;
        }

        return bestPos;
    }

    private static boolean isValidStandPos(TermiteEntity termite, BlockPos standPos, BlockPos targetPos) {
        if (!standPos.closerThan(targetPos, STAND_REACH_DISTANCE)) {
            return false;
        }

        BlockState standState = termite.level().getBlockState(standPos);
        BlockState belowState = termite.level().getBlockState(standPos.below());
        BlockState headState = termite.level().getBlockState(standPos.above());

        return standState.isAir() && headState.isAir() && !belowState.isAir();
    }

    private static boolean isWithinMoundRange(TermiteEntity termite, BlockPos pos) {
        BlockPos moundPos = termite.getMoundPos();
        if (moundPos == null) {
            return true;
        }

        double targetCenterX = pos.getX() + 0.5D;
        double targetCenterY = pos.getY() + 0.5D;
        double targetCenterZ = pos.getZ() + 0.5D;
        return moundPos.distToCenterSqr(targetCenterX, targetCenterY, targetCenterZ) <= TermiteEntity.MAX_MOUND_DISTANCE * TermiteEntity.MAX_MOUND_DISTANCE;
    }

    private static boolean isTermiteWithinAllowedRange(TermiteEntity termite) {
        BlockPos moundPos = termite.getMoundPos();
        if (moundPos == null) {
            return true;
        }

        double moundCenterX = moundPos.getX() + 0.5D;
        double moundCenterY = moundPos.getY() + 0.5D;
        double moundCenterZ = moundPos.getZ() + 0.5D;
        double allowedDistance = TermiteEntity.MAX_MOUND_DISTANCE + RETURN_RANGE_PADDING;
        return termite.distanceToSqr(moundCenterX, moundCenterY, moundCenterZ) <= allowedDistance * allowedDistance;
    }

    private static boolean isCloseEnoughToInteract(TermiteEntity termite, @Nullable BlockPos targetPos, @Nullable BlockPos standPos) {
        if (targetPos == null || standPos == null) {
            return false;
        }

        if (!termite.blockPosition().closerThan(standPos, STAND_CLOSE_ENOUGH_DISTANCE)) {
            return false;
        }

        double targetCenterX = targetPos.getX() + 0.5D;
        double targetCenterY = targetPos.getY() + 0.5D;
        double targetCenterZ = targetPos.getZ() + 0.5D;
        return termite.distanceToSqr(targetCenterX, targetCenterY, targetCenterZ) <= TARGET_INTERACT_DISTANCE_SQUARED;
    }

    private static void navigateTo(TermiteEntity termite, BlockPos pos) {
        termite.getNavigation().moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, NAVIGATION_SPEED);
    }

    @Nullable
    private static BlockPos findNearestMatchingBlock(TermiteEntity termite, BlockPos origin, Predicate<BlockPos> predicate) {
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int offsetX = -16; offsetX <= 16; offsetX++) {
            for (int offsetZ = -16; offsetZ <= 16; offsetZ++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    mutablePos.set(origin.getX() + offsetX, origin.getY() + offsetY, origin.getZ() + offsetZ);
                    if (!predicate.test(mutablePos)) {
                        continue;
                    }

                    double distance = termite.distanceToSqr(mutablePos.getX() + 0.5D, mutablePos.getY() + 0.5D, mutablePos.getZ() + 0.5D);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        bestPos = mutablePos.immutable();
                    }
                }
            }
        }

        return bestPos;
    }

    private static void pruneInvalidCachedPositions(List<BlockPos> cachedPositions, Predicate<BlockPos> predicate) {
        cachedPositions.removeIf(cachedPos -> !predicate.test(cachedPos));
    }

    private static void rebuildCachedPositions(BlockPos origin, Predicate<BlockPos> predicate, List<BlockPos> cachedPositions) {
        cachedPositions.clear();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int offsetX = -16; offsetX <= 16; offsetX++) {
            for (int offsetZ = -16; offsetZ <= 16; offsetZ++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    mutablePos.set(origin.getX() + offsetX, origin.getY() + offsetY, origin.getZ() + offsetZ);
                    if (predicate.test(mutablePos)) {
                        cachedPositions.add(mutablePos.immutable());
                    }
                }
            }
        }
    }

    @Nullable
    private static BlockPos findNearestCachedPosition(TermiteEntity termite, List<BlockPos> cachedPositions) {
        BlockPos bestPos = null;
        double bestDistance = Double.MAX_VALUE;

        for (BlockPos cachedPos : cachedPositions) {
            double distance = termite.distanceToSqr(cachedPos.getX() + 0.5D, cachedPos.getY() + 0.5D, cachedPos.getZ() + 0.5D);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPos = cachedPos;
            }
        }

        return bestPos;
    }

    private static void sortLogCandidatesByDistance(TermiteEntity termite, List<LogCandidate> cachedCandidates) {
        cachedCandidates.sort(Comparator.comparingDouble(candidate -> termite.distanceToSqr(candidate.targetPos().getX() + 0.5D, candidate.targetPos().getY() + 0.5D, candidate.targetPos().getZ() + 0.5D)));
    }

    private static void pruneInvalidLogCandidates(TermiteEntity termite, List<LogCandidate> cachedCandidates, Predicate<BlockPos> targetPredicate) {
        cachedCandidates.removeIf(candidate -> !targetPredicate.test(candidate.targetPos()) || !isValidStandPos(termite, candidate.standPos(), candidate.targetPos()));
    }

    private static void rebuildLogCandidates(TermiteEntity termite, BlockPos origin, Predicate<BlockPos> targetPredicate, List<LogCandidate> cachedCandidates) {
        cachedCandidates.clear();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        for (int offsetX = -TermiteEatLogGoal.SEARCH_RANGE; offsetX <= TermiteEatLogGoal.SEARCH_RANGE; offsetX++) {
            for (int offsetZ = -TermiteEatLogGoal.SEARCH_RANGE; offsetZ <= TermiteEatLogGoal.SEARCH_RANGE; offsetZ++) {
                for (int offsetY = -2; offsetY <= 4; offsetY++) {
                    mutablePos.set(origin.getX() + offsetX, origin.getY() + offsetY, origin.getZ() + offsetZ);
                    if (!targetPredicate.test(mutablePos)) {
                        continue;
                    }

                    BlockPos targetPos = mutablePos.immutable();
                    BlockPos standPos = findBestStandPos(termite, targetPos);
                    if (standPos != null) {
                        cachedCandidates.add(new LogCandidate(targetPos, standPos));
                    }
                }
            }
        }

        sortLogCandidatesByDistance(termite, cachedCandidates);
    }

    public static class FetchWoodmealFromInfestedLogGoal extends Goal {
        private static final int SEARCH_COOLDOWN = 200;
        private static final int CACHE_REFRESH_COOLDOWN = 100;
        private static final int COLLECT_TICKS = 40;
        private static final int NAV_STUCK_TICKS = 50;

        private final TermiteEntity termite;
        private final List<BlockPos> cachedInfestedLogs = new ArrayList<>();
        @Nullable
        private BlockPos targetLogPos;
        @Nullable
        private BlockPos standPos;
        private int searchCooldown;
        private int cacheRefreshCooldown;
        private int collectTicks;
        private int navStuckTicks;

        public FetchWoodmealFromInfestedLogGoal(TermiteEntity termite) {
            this.termite = termite;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.termite.getTarget() != null || this.termite.isReturningToMound() || this.termite.hasWoodmeal() || this.termite.getMoundPos() == null) {
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            this.targetLogPos = findNearestInfestedLog();
            if (this.targetLogPos == null) {
                this.searchCooldown = SEARCH_COOLDOWN;
                return false;
            }

            this.standPos = findBestStandPos(this.termite, this.targetLogPos);
            if (this.standPos == null) {
                this.cachedInfestedLogs.remove(this.targetLogPos);
                this.targetLogPos = null;
                this.searchCooldown = SEARCH_COOLDOWN;
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.termite.getTarget() == null && !this.termite.isReturningToMound() && !this.termite.hasWoodmeal() && this.targetLogPos != null && this.standPos != null;
        }

        @Override
        public void start() {
            this.collectTicks = 0;
            this.navStuckTicks = 0;
            assert this.standPos != null;
            navigateTo(this.termite, this.standPos);
        }

        @Override
        public void stop() {
            this.termite.getNavigation().stop();
            this.targetLogPos = null;
            this.standPos = null;
            this.collectTicks = 0;
            this.navStuckTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetLogPos == null || this.standPos == null) {
                return;
            }

            if (!isValidInfestedLog(this.targetLogPos) || !isWithinMoundRange(this.termite, this.targetLogPos)) {
                this.cachedInfestedLogs.remove(this.targetLogPos);
                this.searchCooldown = SEARCH_COOLDOWN;
                this.stop();
                return;
            }

            this.termite.getLookControl().setLookAt(this.targetLogPos.getX() + 0.5D, this.targetLogPos.getY() + 0.5D, this.targetLogPos.getZ() + 0.5D);

            if (!isCloseEnoughToInteract(this.termite, this.targetLogPos, this.standPos)) {
                this.collectTicks = 0;
                if (this.termite.getNavigation().isDone()) {
                    if (++this.navStuckTicks >= NAV_STUCK_TICKS) {
                        this.searchCooldown = SEARCH_COOLDOWN;
                        this.stop();
                    } else {
                        navigateTo(this.termite, this.standPos);
                    }
                } else {
                    this.navStuckTicks = 0;
                }
                return;
            }

            this.termite.getNavigation().stop();
            this.navStuckTicks = 0;
            this.collectTicks++;

            if (this.termite.level() instanceof ServerLevel serverLevel && this.collectTicks % 10 == 0) {
                BlockState state = serverLevel.getBlockState(this.targetLogPos);
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), this.targetLogPos.getX() + 0.5D, this.targetLogPos.getY() + 0.7D, this.targetLogPos.getZ() + 0.5D, 3, 0.18D, 0.08D, 0.18D, 0.01D);
                this.termite.playSound(SoundEvents.SILVERFISH_STEP, 0.25F, 0.75F + this.termite.getRandom().nextFloat() * 0.15F);
            }

            if (this.collectTicks < COLLECT_TICKS) {
                return;
            }

            this.termite.setHasWoodmeal(true);
            this.termite.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ObjectRegistry.WOODMEAL.get()));
            this.cachedInfestedLogs.remove(this.targetLogPos);
            this.searchCooldown = SEARCH_COOLDOWN;
            this.stop();
        }

        @Nullable
        private BlockPos findNearestInfestedLog() {
            BlockPos moundPos = this.termite.getMoundPos();
            if (moundPos == null) {
                return null;
            }

            if (this.cacheRefreshCooldown-- <= 0 || this.cachedInfestedLogs.isEmpty()) {
                this.cacheRefreshCooldown = CACHE_REFRESH_COOLDOWN;
                rebuildCachedPositions(moundPos, this::isValidInfestedLog, this.cachedInfestedLogs);
            } else {
                pruneInvalidCachedPositions(this.cachedInfestedLogs, this::isValidInfestedLog);
            }

            return findNearestCachedPosition(this.termite, this.cachedInfestedLogs);
        }

        private boolean isValidInfestedLog(BlockPos pos) {
            BlockState state = this.termite.level().getBlockState(pos);
            return state.is(ObjectRegistry.ROTTEN_LOG.get()) && state.getValue(RottenLogBlock.STAGE) == RottenLogBlock.Stage.INFESTED;
        }
    }

    public static class TermiteEatLogGoal extends Goal {
        private enum State {
            IDLE,
            MOVING,
            EATING,
            INFESTING
        }

        private static final int EAT_TICKS = TermiteEntity.EAT_DURATION_TICKS;
        private static final int ACTION_PAUSE = 4;
        private static final int NO_LOG_COOLDOWN = 80;
        private static final int SEARCH_COOLDOWN = 60;
        private static final int CACHE_REFRESH_COOLDOWN = 120;
        private static final int NAV_STUCK_TICKS = 50;
        private static final int SEARCH_RANGE = (int) TermiteEntity.LOG_SEARCH_RANGE;

        private final TermiteEntity termite;
        private final List<LogCandidate> cachedLogCandidates = new ArrayList<>();
        private State state = State.IDLE;
        @Nullable
        private BlockPos targetPos;
        @Nullable
        private BlockPos standPos;
        private int eatTicksRemaining;
        private int actionPauseTicks;
        private int noLogCooldown;
        private int searchCooldown;
        private int cacheRefreshCooldown;
        private int navStuckTicks;

        public TermiteEatLogGoal(TermiteEntity termite) {
            this.termite = termite;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.noLogCooldown > 0) {
                this.noLogCooldown--;
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }
            return this.termite.getTarget() == null && !this.termite.isReturningToMound() && !this.termite.hasWoodmeal() && this.state == State.IDLE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.termite.getTarget() == null && !this.termite.isReturningToMound() && !this.termite.hasWoodmeal() && this.state != State.IDLE;
        }

        @Override
        public void start() {
            this.state = State.IDLE;
            this.targetPos = null;
            this.standPos = null;
            this.navStuckTicks = 0;
            this.eatTicksRemaining = 0;
            this.actionPauseTicks = 0;
            this.termite.setEating(false);
        }

        @Override
        public void stop() {
            this.termite.setEating(false);
            this.termite.getNavigation().stop();
            clearBreakProgress();
            this.state = State.IDLE;
            this.targetPos = null;
            this.standPos = null;
            this.navStuckTicks = 0;
            this.eatTicksRemaining = 0;
            this.actionPauseTicks = 0;
        }

        @Override
        public void tick() {
            switch (this.state) {
                case IDLE -> tickIdle();
                case MOVING -> tickMoving();
                case EATING -> tickEating();
                case INFESTING -> tickInfesting();
            }
        }

        private void tickIdle() {
            LogCandidate bestCandidate = findNearestLogCandidate();
            this.searchCooldown = SEARCH_COOLDOWN;

            if (bestCandidate == null) {
                this.targetPos = null;
                this.standPos = null;
                this.noLogCooldown = NO_LOG_COOLDOWN;
                return;
            }

            this.targetPos = bestCandidate.targetPos();
            this.standPos = bestCandidate.standPos();
            this.navStuckTicks = 0;
            this.state = State.MOVING;
            navigateTo(this.termite, this.standPos);
        }

        private void tickMoving() {
            if (this.targetPos == null || this.standPos == null) {
                resetToIdle(false, true);
                return;
            }

            if (!isEatableLog(this.targetPos) || !isWithinMoundRange(this.termite, this.targetPos)) {
                removeCachedLogCandidate(this.targetPos);
                resetToIdle(false, true);
                return;
            }

            if (!isTermiteWithinAllowedRange(this.termite)) {
                resetToIdle(true, true);
                return;
            }

            if (!isValidStandPos(this.termite, this.standPos, this.targetPos)) {
                BlockPos refreshedStandPos = findBestStandPos(this.termite, this.targetPos);
                if (refreshedStandPos == null) {
                    removeCachedLogCandidate(this.targetPos);
                    resetToIdle(false, true);
                    return;
                }
                replaceCachedLogCandidate(this.targetPos, refreshedStandPos);
                this.standPos = refreshedStandPos;
                navigateTo(this.termite, this.standPos);
            }

            this.termite.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D);

            if (isCloseEnoughToInteract(this.termite, this.targetPos, this.standPos)) {
                this.termite.getNavigation().stop();
                this.eatTicksRemaining = EAT_TICKS;
                this.navStuckTicks = 0;
                this.termite.setEating(true);
                this.state = State.EATING;
                return;
            }

            if (this.termite.getNavigation().isDone()) {
                if (++this.navStuckTicks >= NAV_STUCK_TICKS) {
                    navigateTo(this.termite, this.standPos);
                    if (this.navStuckTicks >= NAV_STUCK_TICKS * 2) {
                        resetToIdle(false, true);
                    }
                }
            } else {
                this.navStuckTicks = 0;
            }
        }

        private void tickEating() {
            if (this.targetPos == null || this.standPos == null) {
                resetToIdle(false, true);
                return;
            }

            if (!isEatableLog(this.targetPos) || !isWithinMoundRange(this.termite, this.targetPos) || !isTermiteWithinAllowedRange(this.termite) || !isCloseEnoughToInteract(this.termite, this.targetPos, this.standPos)) {
                if (this.targetPos != null && !isEatableLog(this.targetPos)) {
                    removeCachedLogCandidate(this.targetPos);
                }
                resetToIdle(false, true);
                return;
            }

            this.termite.getNavigation().stop();
            this.termite.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D);

            if (this.termite.level() instanceof ServerLevel serverLevel) {
                int elapsedTicks = EAT_TICKS - this.eatTicksRemaining;
                int breakStage = (int) ((elapsedTicks / (float) EAT_TICKS) * 9.0F);
                serverLevel.destroyBlockProgress(this.termite.getId(), this.targetPos, breakStage);

                if (this.eatTicksRemaining % 10 == 0) {
                    BlockState particleState = serverLevel.getBlockState(this.targetPos);
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, particleState), this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.8D, this.targetPos.getZ() + 0.5D, 4, 0.2D, 0.1D, 0.2D, 0.02D);
                }
            }

            if (this.eatTicksRemaining % 15 == 0) {
                this.termite.playSound(SoundEvents.SILVERFISH_STEP, 0.4F, 0.8F + this.termite.getRandom().nextFloat() * 0.3F);
            }

            if (--this.eatTicksRemaining > 0) {
                return;
            }

            this.termite.setEating(false);

            if (this.termite.level() instanceof ServerLevel serverLevel) {
                clearBreakProgress();
                BlockState currentState = serverLevel.getBlockState(this.targetPos);
                serverLevel.setBlock(this.targetPos, buildRottenState(currentState), 3);
            }

            removeCachedLogCandidate(this.targetPos);
            this.actionPauseTicks = ACTION_PAUSE;
            this.state = State.INFESTING;
        }

        private void tickInfesting() {
            if (this.actionPauseTicks-- > 0) {
                return;
            }

            if (this.targetPos == null) {
                resetToIdle(false, true);
                return;
            }

            if (!(this.termite.level() instanceof ServerLevel serverLevel)) {
                resetToIdle(false, true);
                return;
            }

            if (this.termite.getMoundPos() == null) {
                resetToIdle(true, false);
                return;
            }

            if (!isWithinMoundRange(this.termite, this.targetPos) || !isTermiteWithinAllowedRange(this.termite)) {
                resetToIdle(true, false);
                return;
            }

            int infestationId = TermiteMoundBlockEntity.requestInfestationId(serverLevel, this.termite.getMoundPos());
            if (infestationId <= 0) {
                resetToIdle(true, false);
                return;
            }

            BlockState currentState = serverLevel.getBlockState(this.targetPos);
            if (currentState.is(ObjectRegistry.ROTTEN_LOG.get()) && currentState.getValue(RottenLogBlock.STAGE) == RottenLogBlock.Stage.HOLLOW) {
                serverLevel.setBlock(this.targetPos, currentState.setValue(RottenLogBlock.STAGE, RottenLogBlock.Stage.INFESTED).setValue(RottenLogBlock.MOISTURE, 0), 3);
                RottenLogBlockEntity.initializeInfestation(serverLevel, this.targetPos, this.termite.getMoundPos(), infestationId, 3);
                resetToIdle(true, false);
            } else {
                TermiteMoundBlockEntity.releaseInfestationId(serverLevel, this.termite.getMoundPos(), infestationId);
                resetToIdle(false, false);
            }
        }

        @Nullable
        private LogCandidate findNearestLogCandidate() {
            BlockPos searchOrigin = this.termite.getMoundPos() != null ? this.termite.getMoundPos() : this.termite.blockPosition();
            Predicate<BlockPos> targetPredicate = pos -> isEatableLog(pos) && isWithinMoundRange(this.termite, pos);

            if (this.cacheRefreshCooldown-- <= 0 || this.cachedLogCandidates.isEmpty()) {
                this.cacheRefreshCooldown = CACHE_REFRESH_COOLDOWN;
                rebuildLogCandidates(this.termite, searchOrigin, targetPredicate, this.cachedLogCandidates);
            } else {
                pruneInvalidLogCandidates(this.termite, this.cachedLogCandidates, targetPredicate);
                sortLogCandidatesByDistance(this.termite, this.cachedLogCandidates);
            }

            if (this.cachedLogCandidates.isEmpty()) {
                return null;
            }

            return this.cachedLogCandidates.get(0);
        }

        private void removeCachedLogCandidate(BlockPos targetPos) {
            this.cachedLogCandidates.removeIf(candidate -> candidate.targetPos().equals(targetPos));
        }

        private void replaceCachedLogCandidate(BlockPos targetPos, BlockPos standPos) {
            for (int index = 0; index < this.cachedLogCandidates.size(); index++) {
                LogCandidate candidate = this.cachedLogCandidates.get(index);
                if (candidate.targetPos().equals(targetPos)) {
                    this.cachedLogCandidates.set(index, new LogCandidate(targetPos, standPos));
                    sortLogCandidatesByDistance(this.termite, this.cachedLogCandidates);
                    return;
                }
            }

            this.cachedLogCandidates.add(new LogCandidate(targetPos, standPos));
            sortLogCandidatesByDistance(this.termite, this.cachedLogCandidates);
        }

        private boolean isEatableLog(BlockPos pos) {
            BlockState state = this.termite.level().getBlockState(pos);
            return state.is(BlockTags.LOGS) && !state.is(ObjectRegistry.ROTTEN_LOG.get());
        }

        private BlockState buildRottenState(BlockState originalState) {
            Direction.Axis axis = originalState.hasProperty(RotatedPillarBlock.AXIS) ? originalState.getValue(RotatedPillarBlock.AXIS) : Direction.Axis.Y;
            return ObjectRegistry.ROTTEN_LOG.get().defaultBlockState().setValue(RottenLogBlock.AXIS, axis).setValue(RottenLogBlock.STAGE, RottenLogBlock.Stage.HOLLOW).setValue(RottenLogBlock.MOISTURE, 0);
        }

        private void resetToIdle(boolean returnToMound, boolean applyCooldown) {
            this.termite.setEating(false);
            this.termite.getNavigation().stop();
            clearBreakProgress();
            this.targetPos = null;
            this.standPos = null;
            this.state = State.IDLE;
            this.navStuckTicks = 0;
            this.eatTicksRemaining = 0;
            this.actionPauseTicks = 0;
            if (applyCooldown) {
                this.noLogCooldown = NO_LOG_COOLDOWN;
            }
            if (returnToMound) {
                this.termite.setReturningToMound(true);
            }
        }

        private void clearBreakProgress() {
            if (this.targetPos != null && this.termite.level() instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlockProgress(this.termite.getId(), this.targetPos, -1);
            }
        }
    }

    public static class ReturnWoodmealToStorageGoal extends Goal {
        private static final double STORAGE_ENTER_DISTANCE = 1.7D;

        private final TermiteEntity termite;
        @Nullable
        private BlockPos storagePos;
        private int repathDelay;

        public ReturnWoodmealToStorageGoal(TermiteEntity termite) {
            this.termite = termite;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.termite.hasWoodmeal() || this.termite.getTarget() != null) {
                return false;
            }

            this.storagePos = findNearestStoragePos();
            if (this.storagePos == null) {
                this.termite.setReturningToMound(true);
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.termite.hasWoodmeal() && this.termite.getTarget() == null && this.storagePos != null;
        }

        @Override
        public void start() {
            this.repathDelay = 0;
        }

        @Override
        public void stop() {
            this.termite.getNavigation().stop();
            this.storagePos = null;
        }

        @Override
        public void tick() {
            if (this.storagePos == null || !(this.termite.level() instanceof ServerLevel serverLevel)) {
                this.termite.setReturningToMound(true);
                return;
            }

            if (!serverLevel.getBlockState(this.storagePos).is(ObjectRegistry.TERMITE_MOUND_STORAGE.get())) {
                this.storagePos = findNearestStoragePos();
                if (this.storagePos == null) {
                    this.termite.setReturningToMound(true);
                    return;
                }
            }

            this.termite.getLookControl().setLookAt(this.storagePos.getX() + 0.5D, this.storagePos.getY() + 0.5D, this.storagePos.getZ() + 0.5D);

            if (this.termite.distanceToSqr(this.storagePos.getX() + 0.5D, this.storagePos.getY() + 0.5D, this.storagePos.getZ() + 0.5D) <= STORAGE_ENTER_DISTANCE * STORAGE_ENTER_DISTANCE) {
                if (serverLevel.getBlockEntity(this.storagePos) instanceof BurrowBlockEntity burrowBlockEntity) {
                    if (burrowBlockEntity.tryAddItem(new ItemStack(ObjectRegistry.WOODMEAL.get()))) {
                        this.termite.setHasWoodmeal(false);
                        this.termite.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        this.termite.setReturningToMound(true);
                    } else {
                        this.termite.setReturningToMound(true);
                    }
                } else {
                    this.termite.setReturningToMound(true);
                }
                return;
            }

            if (this.repathDelay-- <= 0) {
                this.repathDelay = 20;
                this.termite.getNavigation().moveTo(this.storagePos.getX() + 0.5D, this.storagePos.getY() + 0.2D, this.storagePos.getZ() + 0.5D, NAVIGATION_SPEED);
            }
        }

        @Nullable
        private BlockPos findNearestStoragePos() {
            BlockPos moundPos = this.termite.getMoundPos();
            if (moundPos == null) {
                return null;
            }

            return findNearestMatchingBlock(this.termite, moundPos, pos -> this.termite.level().getBlockState(pos).is(ObjectRegistry.TERMITE_MOUND_STORAGE.get()));
        }
    }

    public static class ReturnToMoundGoal extends Goal {
        private static final double ENTER_DISTANCE = 1.6D;

        private final TermiteEntity termite;
        @Nullable
        private BlockPos entrancePos;
        private int repathDelay;

        public ReturnToMoundGoal(TermiteEntity termite) {
            this.termite = termite;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.termite.isReturningToMound() || this.termite.hasWoodmeal()) {
                return false;
            }

            if (this.termite.getMoundPos() == null) {
                this.termite.setReturningToMound(false);
                return false;
            }

            this.entrancePos = TermiteMoundBlockEntity.findEntrancePos(this.termite.level(), this.termite.getMoundPos());
            return this.entrancePos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.termite.isReturningToMound() && !this.termite.hasWoodmeal() && this.termite.getTarget() == null && this.entrancePos != null;
        }

        @Override
        public void start() {
            this.repathDelay = 0;
        }

        @Override
        public void stop() {
            this.termite.getNavigation().stop();
            this.entrancePos = null;
        }

        @Override
        public void tick() {
            if (this.entrancePos == null || !(this.termite.level() instanceof ServerLevel serverLevel)) {
                this.termite.setReturningToMound(false);
                return;
            }

            this.termite.getLookControl().setLookAt(this.entrancePos.getX() + 0.5D, this.entrancePos.getY() + 0.5D, this.entrancePos.getZ() + 0.5D);

            if (this.entrancePos.closerToCenterThan(this.termite.position(), ENTER_DISTANCE)) {
                if (TermiteMoundBlockEntity.tryStoreTermite(serverLevel, this.entrancePos)) {
                    this.termite.discard();
                    return;
                }
                this.termite.setReturningToMound(false);
                return;
            }

            if (this.repathDelay-- <= 0) {
                this.repathDelay = 20;
                this.termite.getNavigation().moveTo(this.entrancePos.getX() + 0.5D, this.entrancePos.getY() + 0.2D, this.entrancePos.getZ() + 0.5D, NAVIGATION_SPEED);
            }
        }
    }

    public static class TermiteMoundDefenseGoal extends TargetGoal {
        private static final int SEARCH_COOLDOWN = 40;

        private final TermiteEntity termite;
        private int searchCooldown = SEARCH_COOLDOWN;

        public TermiteMoundDefenseGoal(TermiteEntity termite) {
            super(termite, false);
            this.termite = termite;
        }

        @Override
        public boolean canUse() {
            if (this.termite.getMoundPos() == null) {
                return false;
            }
            if (this.termite.getTarget() != null) {
                return false;
            }
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            this.searchCooldown = SEARCH_COOLDOWN;

            Player nearestPlayer = this.termite.level().getNearestPlayer(this.termite, TermiteEntity.MOUND_ALERT_RADIUS);
            if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator()) {
                return false;
            }

            BlockPos entrancePos = TermiteMoundBlockEntity.findEntrancePos(this.termite.level(), this.termite.getMoundPos());
            if (entrancePos == null) {
                return false;
            }

            double distanceSquared = nearestPlayer.distanceToSqr(entrancePos.getX() + 0.5D, entrancePos.getY() + 0.5D, entrancePos.getZ() + 0.5D);
            if (distanceSquared > TermiteEntity.MOUND_ALERT_RADIUS * TermiteEntity.MOUND_ALERT_RADIUS) {
                return false;
            }

            this.termite.setReturningToMound(false);
            this.termite.setTarget(nearestPlayer);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}