package net.satisfy.wildernature.core.entity.ai.goal.animal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.pathfinder.Path;
import net.satisfy.wildernature.core.entity.animal.defensive.HippoEntity;

public final class HippoGoals {
    private HippoGoals() {
    }

    public static class SeekWaterGoal extends MoveToBlockGoal {
        private final HippoEntity hippoEntity;

        public SeekWaterGoal(HippoEntity hippoEntity, double speedModifier, int searchRange) {
            super(hippoEntity, speedModifier, searchRange);
            this.hippoEntity = hippoEntity;
        }

        @Override
        public boolean canUse() {
            if (this.hippoEntity.isInWaterOrBubble() || this.hippoEntity.isTossFeeding() || this.hippoEntity.getTarget() != null) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            if (this.hippoEntity.isInWaterOrBubble() || this.hippoEntity.isTossFeeding() || this.hippoEntity.getTarget() != null) {
                return false;
            }
            return super.canContinueToUse();
        }

        @Override
        protected boolean isValidTarget(LevelReader level, BlockPos pos) {
            return level.getFluidState(pos).is(FluidTags.WATER);
        }
    }

    public static class HippoAttackBoatGoal extends Goal {
        private final HippoEntity hippoEntity;
        private final double speedModifier;

        private Boat targetBoat;
        private Path currentPath;
        private double pathedTargetX;
        private double pathedTargetY;
        private double pathedTargetZ;
        private int ticksUntilNextPathRecalculation;
        private int ticksUntilNextAttack;
        private long lastCanUseCheck;

        public HippoAttackBoatGoal(HippoEntity hippoEntity, double speedModifier) {
            this.hippoEntity = hippoEntity;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.hippoEntity.isBaby() || this.hippoEntity.isTossFeeding()) {
                return false;
            }

            long gameTime = this.hippoEntity.level().getGameTime();
            if (gameTime - this.lastCanUseCheck < 20L) {
                return false;
            }
            this.lastCanUseCheck = gameTime;

            List<Boat> nearbyBoats = this.hippoEntity.level().getEntitiesOfClass(Boat.class, this.hippoEntity.getBoundingBox().inflate(8.0D, 4.0D, 8.0D));
            if (nearbyBoats.isEmpty()) {
                return false;
            }

            this.targetBoat = nearbyBoats.get(0);
            if (this.targetBoat.isRemoved()) {
                return false;
            }

            this.currentPath = this.hippoEntity.getNavigation().createPath(this.targetBoat, 0);
            if (this.currentPath != null) {
                return true;
            }

            return this.getAttackReachSqr(this.targetBoat) >= this.hippoEntity.distanceToSqr(this.targetBoat.getX(), this.targetBoat.getY(), this.targetBoat.getZ());
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetBoat == null || this.targetBoat.isRemoved() || this.hippoEntity.isBaby() || this.hippoEntity.isTossFeeding()) {
                return false;
            }
            return !this.hippoEntity.getNavigation().isDone() || this.hippoEntity.distanceToSqr(this.targetBoat) <= this.getAttackReachSqr(this.targetBoat) + 4.0D;
        }

        @Override
        public void start() {
            if (this.currentPath != null) {
                this.hippoEntity.getNavigation().moveTo(this.currentPath, this.speedModifier);
            }
            this.hippoEntity.startThreat();
            this.ticksUntilNextPathRecalculation = 0;
            this.ticksUntilNextAttack = 0;
        }

        @Override
        public void stop() {
            this.targetBoat = null;
            this.currentPath = null;
            this.hippoEntity.getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.targetBoat == null || this.targetBoat.isRemoved()) {
                return;
            }

            this.hippoEntity.startThreat();
            this.hippoEntity.getLookControl().setLookAt(this.targetBoat, 30.0F, 30.0F);

            double distanceToTargetSqr = this.hippoEntity.distanceToSqr(this.targetBoat.getX(), this.targetBoat.getY(), this.targetBoat.getZ());
            this.ticksUntilNextPathRecalculation = Math.max(this.ticksUntilNextPathRecalculation - 1, 0);

            if (this.hippoEntity.getSensing().hasLineOfSight(this.targetBoat) && this.ticksUntilNextPathRecalculation <= 0 && (this.pathedTargetX == 0.0D && this.pathedTargetY == 0.0D && this.pathedTargetZ == 0.0D || this.targetBoat.distanceToSqr(this.pathedTargetX, this.pathedTargetY, this.pathedTargetZ) >= 1.0D || this.hippoEntity.getRandom().nextFloat() < 0.05F)) {
                this.pathedTargetX = this.targetBoat.getX();
                this.pathedTargetY = this.targetBoat.getY();
                this.pathedTargetZ = this.targetBoat.getZ();
                this.ticksUntilNextPathRecalculation = 4 + this.hippoEntity.getRandom().nextInt(7);

                if (distanceToTargetSqr > 1024.0D) {
                    this.ticksUntilNextPathRecalculation += 10;
                } else if (distanceToTargetSqr > 256.0D) {
                    this.ticksUntilNextPathRecalculation += 5;
                }

                if (!this.hippoEntity.getNavigation().moveTo(this.targetBoat, this.speedModifier)) {
                    this.ticksUntilNextPathRecalculation += 15;
                }

                this.ticksUntilNextPathRecalculation = this.adjustedTickDelay(this.ticksUntilNextPathRecalculation);
            }

            this.ticksUntilNextAttack = Math.max(this.ticksUntilNextAttack - 1, 0);
            this.checkAndPerformAttack(this.targetBoat, distanceToTargetSqr);
        }

        private void checkAndPerformAttack(Entity targetEntity, double distanceToTargetSqr) {
            double attackReachSqr = this.getAttackReachSqr(targetEntity);
            if (distanceToTargetSqr <= attackReachSqr && this.ticksUntilNextAttack <= 0) {
                this.ticksUntilNextAttack = this.adjustedTickDelay(20);
                this.hippoEntity.startSnap();
                this.hippoEntity.doHurtTarget(targetEntity);
            }
        }

        private double getAttackReachSqr(Entity targetEntity) {
            return Mth.square(this.hippoEntity.getBbWidth() * 1.2F) + targetEntity.getBbWidth();
        }
    }
}