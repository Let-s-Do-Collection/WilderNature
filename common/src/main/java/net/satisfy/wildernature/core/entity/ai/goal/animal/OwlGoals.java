package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.OwlEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public final class OwlGoals {
    private OwlGoals() {
    }

    public static class ExtendedFlyOntoTree extends WaterAvoidingRandomStrollGoal {
        public ExtendedFlyOntoTree(PathfinderMob pathfinderMob, double speedModifier, float probability) {
            super(pathfinderMob, speedModifier, probability);
        }

        @Override
        protected Vec3 getPosition() {
            Vec3 targetPosition = null;
            if (this.mob.isInWaterOrBubble()) {
                targetPosition = LandRandomPos.getPos(this.mob, 15, 7);
            }

            if (this.mob.getRandom().nextFloat() >= this.probability) {
                targetPosition = this.getTreeTarget();
            }

            return targetPosition == null ? super.getPosition() : targetPosition;
        }

        private Vec3 getTreeTarget() {
            BlockPos currentBlockPos = this.mob.getOnPos();
            BlockPos.MutableBlockPos upperCheckPosition = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos lowerCheckPosition = new BlockPos.MutableBlockPos();
            Iterable<BlockPos> nearbyPositions = BlockPos.betweenClosed(
                    Mth.floor(this.mob.getX() - 3.0D),
                    Mth.floor(this.mob.getY() - 6.0D),
                    Mth.floor(this.mob.getZ() - 3.0D),
                    Mth.floor(this.mob.getX() + 3.0D),
                    Mth.floor(this.mob.getY() + 6.0D),
                    Mth.floor(this.mob.getZ() + 3.0D)
            );

            for (BlockPos candidatePosition : nearbyPositions) {
                if (currentBlockPos.equals(candidatePosition)) {
                    continue;
                }

                BlockState supportingBlockState = this.mob.level().getBlockState(lowerCheckPosition.setWithOffset(candidatePosition, Direction.DOWN));
                boolean validTreeBlock = supportingBlockState.getBlock() instanceof LeavesBlock || supportingBlockState.is(BlockTags.LOGS);
                if (!validTreeBlock) {
                    continue;
                }

                if (!this.mob.level().isEmptyBlock(candidatePosition)) {
                    continue;
                }

                if (!this.mob.level().isEmptyBlock(upperCheckPosition.setWithOffset(candidatePosition, Direction.UP))) {
                    continue;
                }

                return Vec3.atBottomCenterOf(candidatePosition);
            }

            return null;
        }
    }

    public static class MoveToSleepPerchGoal extends Goal {
        private final OwlEntity owl;
        private final double speedModifier;
        private BlockPos targetPerchPosition;
        private int pathRecalculationTicks;
        private int stuckTicks;

        public MoveToSleepPerchGoal(OwlEntity owl, double speedModifier) {
            this.owl = owl;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.owl.canSearchForSleepPerch()) {
                return false;
            }

            this.targetPerchPosition = this.owl.findNearbySleepPerch();
            return this.targetPerchPosition != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPerchPosition != null
                    && this.owl.canSearchForSleepPerch()
                    && !this.owl.isValidSleepPerch()
                    && !this.owl.hasReachedPerchTarget(this.targetPerchPosition)
                    && this.stuckTicks < 60;
        }

        @Override
        public void start() {
            this.pathRecalculationTicks = 0;
            this.stuckTicks = 0;
            this.moveToTargetPerch();
        }

        @Override
        public void tick() {
            if (this.targetPerchPosition == null) {
                return;
            }

            if (this.owl.hasReachedPerchTarget(this.targetPerchPosition)) {
                this.owl.snapToPerch(this.targetPerchPosition);
                this.stuckTicks = 0;
                return;
            }

            this.pathRecalculationTicks--;
            if (this.pathRecalculationTicks <= 0 || this.owl.getNavigation().isDone()) {
                this.pathRecalculationTicks = 10;
                this.moveToTargetPerch();
            }

            Vec3 movement = this.owl.getDeltaMovement();
            boolean barelyMoving = Math.abs(movement.x) < 0.01D
                    && Math.abs(movement.y) < 0.01D
                    && Math.abs(movement.z) < 0.01D;

            if (this.owl.getNavigation().isDone() && barelyMoving) {
                this.stuckTicks++;
            } else {
                this.stuckTicks = 0;
            }
        }

        @Override
        public void stop() {
            this.owl.getNavigation().stop();
            this.targetPerchPosition = null;
            this.pathRecalculationTicks = 0;
            this.stuckTicks = 0;
        }

        private void moveToTargetPerch() {
            if (this.targetPerchPosition != null) {
                this.owl.getNavigation().moveTo(
                        this.targetPerchPosition.getX() + 0.5D,
                        this.targetPerchPosition.getY(),
                        this.targetPerchPosition.getZ() + 0.5D,
                        this.speedModifier
                );
            }
        }
    }

    public static class SleepGoal extends Goal {
        private final OwlEntity owl;

        public SleepGoal(OwlEntity owlEntity) {
            this.owl = owlEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.owl.canStartSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return this.owl.canContinueSleeping();
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public void start() {
            super.start();
            this.owl.startSleeping();
        }

        @Override
        public void tick() {
            this.owl.getNavigation().stop();
            this.owl.setDeltaMovement(Vec3.ZERO);
            if (!this.owl.canContinueSleeping()) {
                this.owl.wakeUp();
            }
        }

        @Override
        public void stop() {
            super.stop();
            this.owl.wakeUp();
        }
    }

    public static class EatRottenFleshGoal extends Goal {
        private final OwlEntity owl;
        private final double speedModifier;
        private ItemEntity targetRottenFlesh;

        public EatRottenFleshGoal(OwlEntity owl, double speedModifier) {
            this.owl = owl;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.owl.canHuntNow()) {
                return false;
            }

            this.targetRottenFlesh = this.owl.findNearbyRottenFlesh();
            return this.targetRottenFlesh != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetRottenFlesh != null
                    && this.targetRottenFlesh.isAlive()
                    && this.targetRottenFlesh.getItem().is(Items.ROTTEN_FLESH)
                    && this.owl.canHuntNow();
        }

        @Override
        public void start() {
            this.moveToTarget();
        }

        @Override
        public void tick() {
            if (this.targetRottenFlesh == null || !this.targetRottenFlesh.isAlive()) {
                return;
            }

            if (this.owl.distanceToSqr(this.targetRottenFlesh) <= 2.25D) {
                this.owl.consumeRottenFlesh(this.targetRottenFlesh);
                this.targetRottenFlesh = null;
                this.owl.getNavigation().stop();
                return;
            }

            this.moveToTarget();
        }

        @Override
        public void stop() {
            this.targetRottenFlesh = null;
            this.owl.getNavigation().stop();
        }

        private void moveToTarget() {
            if (this.targetRottenFlesh != null) {
                this.owl.getNavigation().moveTo(
                        this.targetRottenFlesh.getX(),
                        this.targetRottenFlesh.getY(),
                        this.targetRottenFlesh.getZ(),
                        this.speedModifier
                );
            }
        }
    }

    public static class BetterFollowOwnerGoal extends Goal {
        public static final RandomSource RANDOM = RandomSource.create();
        protected final TamableAnimal tameable;
        protected final Level level;
        protected final double speed;
        protected final PathNavigation navigation;
        protected final float maxDistance;
        protected final float minDistance;
        protected final boolean leavesAllowed;
        protected LivingEntity owner;
        protected int updateCountdownTicks;
        protected float oldWaterPathfindingPenalty;

        public BetterFollowOwnerGoal(TamableAnimal tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            this.tameable = tameable;
            this.level = tameable.level();
            this.speed = speed;
            this.navigation = tameable.getNavigation();
            this.minDistance = minDistance;
            this.maxDistance = maxDistance;
            this.leavesAllowed = leavesAllowed;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            if (!(tameable.getNavigation() instanceof GroundPathNavigation) && !(tameable.getNavigation() instanceof FlyingPathNavigation)) {
                throw new IllegalArgumentException("Unsupported mob tier for FollowOwnerGoal");
            }
        }

        public static int randomRange(int min, int max) {
            return RANDOM.nextInt(max - min) + min;
        }

        @SuppressWarnings("unused")
        public static int randomBias(int min, int max) {
            int num = randomRange(min, max);
            int mid = (max / 2) - (min / 2);
            int halfMid = mid / 2;
            if (num > mid) num -= RANDOM.nextInt((halfMid + 1));
            else if (num < mid) num += RANDOM.nextInt((halfMid + 1));

            return num;
        }

        @Override
        public boolean canUse() {
            LivingEntity livingEntity = this.tameable.getOwner();
            if (livingEntity == null) {
                return false;
            } else if (livingEntity.isSpectator()) {
                return false;
            } else if (this.tameable.isOrderedToSit()) {
                return false;
            } else if (this.tameable.distanceToSqr(livingEntity) < (double) (this.minDistance * this.minDistance)) {
                return false;
            } else {
                this.owner = livingEntity;
                return true;
            }
        }

        @Override
        public boolean canContinueToUse() {
            if (this.navigation.isDone()) {
                return false;
            } else if (this.tameable.isOrderedToSit()) {
                return false;
            } else {
                return this.tameable.distanceToSqr(this.owner) > (double) (this.maxDistance * this.maxDistance);
            }
        }

        public void start() {
            this.updateCountdownTicks = 0;
            this.oldWaterPathfindingPenalty = this.tameable.getPathfindingMalus(PathType.WATER);
            this.tameable.setPathfindingMalus(PathType.WATER, 0.0F);
        }

        public void stop() {
            this.owner = null;
            this.navigation.stop();
            this.tameable.setPathfindingMalus(PathType.WATER, this.oldWaterPathfindingPenalty);
        }

        public void tick() {
            this.tameable.getLookControl().setLookAt(this.owner, 10.0F, (float) this.tameable.getMaxHeadXRot());
            if (--this.updateCountdownTicks <= 0) {
                this.updateCountdownTicks = 10;
                if (!this.tameable.isLeashed() && !this.tameable.isPassenger()) {
                    if (this.tameable.distanceToSqr(this.owner) >= 144.0D) {
                        this.tryTeleport();
                    } else {
                        startFollowing();
                    }

                }
            }
        }

        protected void startFollowing() {
            this.navigation.moveTo(this.owner, this.speed);
        }

        protected void tryTeleport() {
            BlockPos blockPos = this.owner.getOnPos();

            for (int i = 0; i < 10; ++i) {
                int j = randomRange(-3, 3);
                int k = randomRange(-1, 1);
                int l = randomRange(-3, 3);
                boolean didTeleport = this.tryTeleportTo(blockPos.getX() + j, blockPos.getY() + k, blockPos.getZ() + l);
                if (didTeleport) {
                    return;
                }
            }
        }

        protected boolean tryTeleportTo(int x, int y, int z) {
            if (Math.abs((double) x - this.owner.getX()) < 2.0D && Math.abs((double) z - this.owner.getZ()) < 2.0D) {
                return false;
            } else if (!this.canTeleportTo(new BlockPos(x, y, z))) {
                return false;
            } else {
                this.tameable.moveTo((double) x + 0.5D, y, (double) z + 0.5D, this.tameable.xRotO, this.tameable.yRotO);
                this.navigation.stop();
                return true;
            }
        }

        protected boolean canTeleportTo(BlockPos pos) {
            PathType pathNodeType = WalkNodeEvaluator.getPathTypeStatic(this.tameable, pos.mutable());
            if (pathNodeType != PathType.WALKABLE) {
                return false;
            } else {
                BlockState blockState = this.level.getBlockState(pos.below());
                if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockPos = pos.subtract(this.tameable.getOnPos());
                    return this.level.noCollision(this.tameable, this.tameable.getBoundingBox().move(blockPos));
                }
            }
        }
    }


        public static class FlyingFollowOwnerGoal extends BetterFollowOwnerGoal {

        public FlyingFollowOwnerGoal(TamableAnimal tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
            super(tameable, speed, minDistance, maxDistance, leavesAllowed);
        }

        @Override
        protected void startFollowing() {
            this.navigation.moveTo(this.owner, owner.isFallFlying() ? 1.2F : speed);
        }

        @Override
        protected boolean canTeleportTo(BlockPos pos) {
            PathType pathNodeType = WalkNodeEvaluator.getPathTypeStatic(this.tameable, pos.mutable());
            if (pathNodeType != PathType.WALKABLE && pathNodeType != PathType.OPEN) {
                return false;
            } else {
                BlockState blockState = this.level.getBlockState(pos.below());
                if (!this.leavesAllowed && blockState.getBlock() instanceof LeavesBlock) {
                    return false;
                } else {
                    BlockPos blockPos = pos.subtract(this.tameable.getOnPos());
                    return this.level.noCollision(this.tameable, this.tameable.getBoundingBox().move(blockPos));
                }
            }
        }
    }

    public static class PredicateTemptGoal extends Goal {
        private static final TargetingConditions TEMP_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
        protected final PathfinderMob mob;
        private final TargetingConditions targetingConditions;
        private final double speedModifier;
        private final boolean canScare;
        @Nullable
        protected Player player;
        protected Predicate<ItemStack> condition;
        private double px;
        private double py;
        private double pz;
        private double pRotX;
        private double pRotY;
        private int calmDown;
        private boolean isRunning;

        public PredicateTemptGoal(PathfinderMob pathfinderMob, double d, Predicate<ItemStack> condition, boolean canScare) {
            this.mob = pathfinderMob;
            this.speedModifier = d;
            this.canScare = canScare;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
            this.condition = condition;
            this.targetingConditions = TEMP_TARGETING.copy().selector(this::shouldFollow);
        }

        @Override
        public boolean canUse() {
            if (this.calmDown > 0) {
                --this.calmDown;
                return false;
            }
            this.player = this.mob.level().getNearestPlayer(this.targetingConditions, this.mob);
            return this.player != null;
        }

        private boolean shouldFollow(LivingEntity livingEntity) {
            return this.condition.test(livingEntity.getMainHandItem()) || this.condition.test(livingEntity.getOffhandItem());
        }

        @Override
        public boolean canContinueToUse() {
            if (this.canScare()) {
                if (this.mob.distanceToSqr(this.player) < 36.0) {
                    if (this.player.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002) {
                        return false;
                    }
                    if (Math.abs((double) this.player.getXRot() - this.pRotX) > 5.0 || Math.abs((double) this.player.getYRot() - this.pRotY) > 5.0) {
                        return false;
                    }
                } else {
                    this.px = this.player.getX();
                    this.py = this.player.getY();
                    this.pz = this.player.getZ();
                }
                this.pRotX = this.player.getXRot();
                this.pRotY = this.player.getYRot();
            }
            return this.canUse();
        }

        protected boolean canScare() {
            return this.canScare;
        }

        @Override
        public void start() {
            assert this.player != null;
            this.px = this.player.getX();
            this.py = this.player.getY();
            this.pz = this.player.getZ();
            this.isRunning = true;
        }

        @Override
        public void stop() {
            this.player = null;
            this.mob.getNavigation().stop();
            this.calmDown = TemptGoal.reducedTickDelay(100);
            this.isRunning = false;
        }

        @Override
        public void tick() {
            this.mob.getLookControl().setLookAt(this.player, this.mob.getMaxHeadYRot() + 20, this.mob.getMaxHeadXRot());
            if (this.mob.distanceToSqr(this.player) < 6.25) {
                this.mob.getNavigation().stop();
            } else {
                this.mob.getNavigation().moveTo(this.player, this.speedModifier);
            }
        }

        public boolean isRunning() {
            return this.isRunning;
        }
    }
}