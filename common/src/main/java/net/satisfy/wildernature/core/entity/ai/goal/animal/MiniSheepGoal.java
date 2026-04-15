package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.satisfy.wildernature.core.entity.animal.neutral.MiniSheepEntity;

import java.util.EnumSet;
import java.util.UUID;

public class MiniSheepGoal {
    private static final double LEADER_FOLLOW_START_DISTANCE = 36.0D;
    private static final double LEADER_FOLLOW_STOP_DISTANCE = 9.0D;
    private static final double LEADER_SLOT_RECALC_DISTANCE = 2.25D;

    public static class MiniSheepFleePlayerGoal extends Goal {
        private final MiniSheepEntity miniSheep;
        private int repathCooldownTicks;

        public MiniSheepFleePlayerGoal(MiniSheepEntity miniSheep) {
            this.miniSheep = miniSheep;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.miniSheep.isMiniSheepSleeping()) {
                return false;
            }
            if (this.miniSheep.isMaehAnimating()) {
                return false;
            }

            if (this.miniSheep.hasFleeTarget()) {
                return true;
            }

            Player nearestPlayer = this.miniSheep.getNearestThreateningPlayer();
            if (nearestPlayer == null) {
                return false;
            }

            this.miniSheep.startHerdFleeFrom(nearestPlayer);
            return this.miniSheep.hasFleeTarget();
        }

        @Override
        public boolean canContinueToUse() {
            return this.miniSheep.hasFleeTarget() && this.miniSheep.isFleeing() && !this.miniSheep.isMaehAnimating();
        }

        @Override
        public void start() {
            this.repathCooldownTicks = 0;
            this.miniSheep.wakeUp();
        }

        @Override
        public void tick() {
            BlockPos fleeTargetPos = this.miniSheep.getFleeTargetPos();
            if (fleeTargetPos == null) {
                return;
            }

            this.miniSheep.getLookControl().setLookAt(fleeTargetPos.getX() + 0.5D, fleeTargetPos.getY() + 0.5D, fleeTargetPos.getZ() + 0.5D);

            if (this.repathCooldownTicks > 0) {
                this.repathCooldownTicks--;
            }

            if (this.repathCooldownTicks <= 0) {
                this.repathCooldownTicks = 8;
                this.miniSheep.getNavigation().moveTo(fleeTargetPos.getX() + 0.5D, fleeTargetPos.getY(), fleeTargetPos.getZ() + 0.5D, 1.45D);
            }
        }

        @Override
        public void stop() {
            this.repathCooldownTicks = 0;
            this.miniSheep.clearFleeTarget();
            this.miniSheep.getNavigation().stop();
        }
    }

    public static class MiniSheepReturnHomeGoal extends Goal {
        private final MiniSheepEntity miniSheep;

        public MiniSheepReturnHomeGoal(MiniSheepEntity miniSheep) {
            this.miniSheep = miniSheep;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.miniSheep.isMiniSheepSleeping()) {
                return false;
            }
            if (this.miniSheep.isFleeing()) {
                return false;
            }
            if (this.miniSheep.isMaehAnimating()) {
                return false;
            }
            if (this.miniSheep.getMeadowHomePos() == null) {
                return false;
            }
            if (this.miniSheep.returningHome) {
                return true;
            }
            return this.miniSheep.shouldReturnHome();
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos meadowHomePos = this.miniSheep.getMeadowHomePos();
            if (meadowHomePos == null) {
                return false;
            }
            if (this.miniSheep.isFleeing()) {
                return false;
            }
            if (this.miniSheep.isMaehAnimating()) {
                return false;
            }
            return !meadowHomePos.closerToCenterThan(this.miniSheep.position(), 4.0D);
        }

        @Override
        public void start() {
            this.miniSheep.startReturningHome();
            this.miniSheep.wakeUp();
        }

        @Override
        public void tick() {
            BlockPos meadowHomePos = this.miniSheep.getMeadowHomePos();
            if (meadowHomePos == null) {
                return;
            }

            this.miniSheep.getLookControl().setLookAt(meadowHomePos.getX() + 0.5D, meadowHomePos.getY() + 0.5D, meadowHomePos.getZ() + 0.5D);
            this.miniSheep.getNavigation().moveTo(meadowHomePos.getX() + 0.5D, meadowHomePos.getY(), meadowHomePos.getZ() + 0.5D, 1.35D);
        }

        @Override
        public void stop() {
            this.miniSheep.stopReturningHome();
            this.miniSheep.getNavigation().stop();
        }
    }

    public static class MiniSheepFollowLeaderGoal extends Goal {
        private final MiniSheepEntity miniSheep;
        private BlockPos currentFollowPos;
        private int followRepathCooldown;

        public MiniSheepFollowLeaderGoal(MiniSheepEntity miniSheep) {
            this.miniSheep = miniSheep;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.miniSheep.isBaby()) {
                return false;
            }
            if (this.miniSheep.isLeader()) {
                return false;
            }
            if (this.miniSheep.isMiniSheepSleeping()) {
                return false;
            }
            if (this.miniSheep.isFleeing()) {
                return false;
            }
            if (this.miniSheep.isMaehAnimating()) {
                return false;
            }

            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null || leader.isMiniSheepSleeping() || leader.isFleeing() || leader.isMaehAnimating()) {
                return false;
            }

            BlockPos desiredFollowPos = this.getDesiredFollowPos(leader);
            return this.miniSheep.blockPosition().distSqr(desiredFollowPos) > LEADER_FOLLOW_START_DISTANCE;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.miniSheep.isBaby()) {
                return false;
            }
            if (this.miniSheep.isMiniSheepSleeping()) {
                return false;
            }
            if (this.miniSheep.isFleeing()) {
                return false;
            }
            if (this.miniSheep.isMaehAnimating()) {
                return false;
            }

            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null || leader.isMiniSheepSleeping() || leader.isFleeing() || leader.isMaehAnimating()) {
                return false;
            }

            BlockPos desiredFollowPos = this.getDesiredFollowPos(leader);
            return this.miniSheep.blockPosition().distSqr(desiredFollowPos) > LEADER_FOLLOW_STOP_DISTANCE;
        }

        @Override
        public void start() {
            this.followRepathCooldown = 0;
            this.currentFollowPos = null;
        }

        @Override
        public void tick() {
            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null) {
                return;
            }

            if (this.followRepathCooldown > 0) {
                this.followRepathCooldown--;
            }

            BlockPos desiredFollowPos = this.getDesiredFollowPos(leader);

            if (this.currentFollowPos == null || this.currentFollowPos.distSqr(desiredFollowPos) > LEADER_SLOT_RECALC_DISTANCE || this.followRepathCooldown <= 0) {
                this.currentFollowPos = desiredFollowPos;
                this.followRepathCooldown = 10 + this.miniSheep.getRandom().nextInt(10);
                this.miniSheep.getNavigation().moveTo(desiredFollowPos.getX() + 0.5D, desiredFollowPos.getY(), desiredFollowPos.getZ() + 0.5D, 1.0D + this.getFollowSpeedVariation());
            }

            this.miniSheep.getLookControl().setLookAt(leader, 20.0F, 20.0F);
        }

        @Override
        public void stop() {
            this.currentFollowPos = null;
            this.followRepathCooldown = 0;
            this.miniSheep.getNavigation().stop();
        }

        private double getFollowSpeedVariation() {
            int variationSeed = Math.floorMod(this.miniSheep.getUUID().hashCode(), 5);
            return variationSeed * 0.035D;
        }

        private BlockPos getDesiredFollowPos(MiniSheepEntity leader) {
            UUID sheepUuid = this.miniSheep.getUUID();
            int slotSeed = sheepUuid.hashCode();
            double angle = Math.toRadians(Math.floorMod(slotSeed, 360));
            double radius = 2.5D + Math.floorMod(slotSeed >> 8, 4) * 1.15D;
            double forwardBias = leader.getDeltaMovement().horizontalDistanceSqr() > 0.0025D ? 1.5D : 0.0D;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius + forwardBias;
            int desiredX = Mth.floor(leader.getX() + offsetX);
            int desiredY = leader.blockPosition().getY();
            int desiredZ = Mth.floor(leader.getZ() + offsetZ);
            return new BlockPos(desiredX, desiredY, desiredZ);
        }
    }
}