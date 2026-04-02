package net.satisfy.wildernature.core.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.satisfy.wildernature.core.entity.animal.MiniSheepEntity;

import java.util.EnumSet;

public class MiniSheepGoal {
    private static final double LEADER_FOLLOW_START_DISTANCE = 16.0D;
    private static final double LEADER_FOLLOW_STOP_DISTANCE = 6.0D;

    public static class MiniSheepReturnHomeGoal extends Goal {
        private final MiniSheepEntity miniSheep;

        public MiniSheepReturnHomeGoal(MiniSheepEntity miniSheep) {
            this.miniSheep = miniSheep;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.miniSheep.getTarget() != null) {
                return false;
            }
            if (this.miniSheep.isMiniSheepSleeping()) {
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
            if (this.miniSheep.getTarget() != null) {
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

    public static class MiniSheepMeleeAttackGoal extends MeleeAttackGoal {
        private final MiniSheepEntity miniSheep;

        public MiniSheepMeleeAttackGoal(MiniSheepEntity miniSheep) {
            super(miniSheep, 1.3D, true);
            this.miniSheep = miniSheep;
        }

        @Override
        public boolean canUse() {
            return !this.miniSheep.isMiniSheepSleeping() && this.miniSheep.getTarget() != null && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = this.miniSheep.getTarget();
            if (target == null) {
                return false;
            }
            if (!target.isAlive()) {
                return false;
            }
            if (this.miniSheep.shouldReturnHome()) {
                return false;
            }
            return !this.miniSheep.isMiniSheepSleeping() && super.canContinueToUse();
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target) {
            double attackReach = this.getMiniSheepAttackReachSqr(target);
            if (this.mob.distanceToSqr(target) <= attackReach && this.isTimeToAttack()) {
                this.resetAttackCooldown();
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(target);
                this.mob.level().playSound(null, this.mob.blockPosition(), SoundEvents.SHEEP_HURT, SoundSource.NEUTRAL, 0.6F, 0.85F);
            }
        }

        private double getMiniSheepAttackReachSqr(LivingEntity target) {
            float width = this.mob.getBbWidth() * 2.0F;
            return width * width + target.getBbWidth();
        }

        @Override
        public void stop() {
            super.stop();
            this.miniSheep.setTarget(null);
            if (this.miniSheep.shouldReturnHome()) {
                this.miniSheep.startReturningHome();
            }
        }
    }

    public static class MiniSheepFollowLeaderGoal extends Goal {
        private final MiniSheepEntity miniSheep;

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
            if (this.miniSheep.getTarget() != null) {
                return false;
            }

            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null) {
                return false;
            }

            return this.miniSheep.distanceToSqr(leader) > LEADER_FOLLOW_START_DISTANCE;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.miniSheep.isBaby()) {
                return false;
            }
            if (this.miniSheep.isMiniSheepSleeping()) {
                return false;
            }
            if (this.miniSheep.getTarget() != null) {
                return false;
            }

            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null) {
                return false;
            }

            return this.miniSheep.distanceToSqr(leader) > LEADER_FOLLOW_STOP_DISTANCE;
        }

        @Override
        public void tick() {
            MiniSheepEntity leader = this.miniSheep.getLeader();
            if (leader == null) {
                return;
            }

            this.miniSheep.getLookControl().setLookAt(leader, 20.0F, 20.0F);
            this.miniSheep.getNavigation().moveTo(leader, 1.0D);
        }

        @Override
        public void stop() {
            this.miniSheep.getNavigation().stop();
        }
    }
}
