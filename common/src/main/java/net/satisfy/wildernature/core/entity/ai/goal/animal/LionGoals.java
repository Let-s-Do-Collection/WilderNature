package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class LionGoals {
    public static class LionPounceGoal extends Goal {
        private static final double POUNCE_TRIGGER_DIST_SQR = 16.0D;

        private final LionEntity lion;
        @Nullable
        private LivingEntity target;

        public LionPounceGoal(LionEntity lion) {
            this.lion = lion;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!this.lion.canStartPounce()) {
                return false;
            }

            this.target = this.lion.getTarget();
            if (this.target == null || !this.target.isAlive()) {
                return false;
            }

            return this.lion.distanceToSqr(this.target) <= POUNCE_TRIGGER_DIST_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            return this.lion.isPouncing() || this.lion.pounceWindupTicks > 0 || this.lion.pounceRecoveryTicks > 0;
        }

        @Override
        public void start() {
            this.lion.startPounce();
            this.lion.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void tick() {
            if (this.target != null) {
                this.lion.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
            }
            this.lion.getNavigation().stop();
        }
    }

    public static class LionWarnGoal extends Goal {
        private static final int WARN_SEARCH_COOLDOWN = 60;

        private final LionEntity lion;
        private int searchCooldown;
        @Nullable
        private Player targetPlayer;

        public LionWarnGoal(LionEntity lion) {
            this.lion = lion;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.lion.isBaby() || this.lion.isSleeping() || this.lion.isBusy() || this.lion.getTarget() != null) {
                return false;
            }

            if (this.searchCooldown > 0) {
                this.searchCooldown--;
                return false;
            }

            Player nearestPlayer = this.lion.level().getNearestPlayer(this.lion, LionEntity.AGGRO_PLAYER_RADIUS);
            if (!this.lion.canWarnAtPlayer(nearestPlayer)) {
                this.searchCooldown = WARN_SEARCH_COOLDOWN;
                return false;
            }

            this.targetPlayer = nearestPlayer;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.lion.isWarning() && this.targetPlayer != null && this.targetPlayer.isAlive() && this.lion.canWarnAtPlayer(this.targetPlayer);
        }

        @Override
        public void start() {
            this.lion.startWarning();
            this.lion.getNavigation().stop();
        }

        @Override
        public void stop() {
            this.searchCooldown = WARN_SEARCH_COOLDOWN;
            this.targetPlayer = null;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.lion.getNavigation().stop();
            this.lion.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.lion.getWarnTicks() <= 1) {
                this.lion.setTarget(this.targetPlayer);
                this.lion.alertPride(this.targetPlayer);
                this.lion.triggerRoar();
            }
        }
    }

    public static class LionSleepGoal extends Goal {
        private final LionEntity lion;

        public LionSleepGoal(LionEntity lion) {
            this.lion = lion;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.lion.isSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return this.lion.isSleeping();
        }

        @Override
        public void tick() {
            this.lion.getNavigation().stop();
            this.lion.setDeltaMovement(Vec3.ZERO);
        }
    }

    public static class LionStalkGoal extends Goal {
        private static final double STALK_START_DIST_SQR = 144.0D;
        private static final double POUNCE_TRIGGER_DIST_SQR = 16.0D;

        private final LionEntity lion;
        private final double speedModifier;
        @Nullable
        private LivingEntity prey;

        public LionStalkGoal(LionEntity lion, double speedModifier) {
            this.lion = lion;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.lion.getTarget();
            if (target == null || !target.isAlive() || target instanceof Player) {
                return false;
            }

            if (this.lion.isSleeping() || this.lion.isBusy()) {
                return false;
            }

            double distanceSqr = this.lion.distanceToSqr(target);
            if (distanceSqr > STALK_START_DIST_SQR || distanceSqr <= POUNCE_TRIGGER_DIST_SQR) {
                return false;
            }

            this.prey = target;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.prey == null || !this.prey.isAlive()) {
                return false;
            }

            double distanceSqr = this.lion.distanceToSqr(this.prey);
            return distanceSqr > POUNCE_TRIGGER_DIST_SQR && distanceSqr <= STALK_START_DIST_SQR * 1.5D && !this.lion.isBusy() && !this.lion.isSleeping();
        }

        @Override
        public void start() {
            this.lion.setStalking(true);
        }

        @Override
        public void stop() {
            this.lion.setStalking(false);
            this.prey = null;
        }

        @Override
        public void tick() {
            if (this.prey == null) {
                return;
            }

            this.lion.getLookControl().setLookAt(this.prey, 30.0F, 30.0F);
            this.lion.getNavigation().moveTo(this.prey, this.speedModifier);
        }
    }

    public static class LionHuntGoal extends NearestAttackableTargetGoal<Animal> {
        private final LionEntity lion;

        public LionHuntGoal(LionEntity lion) {
            super(lion, Animal.class, 10, true, false, target -> target != null && target.getType().is(TagsRegistry.LION_TARGETS));
            this.lion = lion;
        }

        @Override
        public boolean canUse() {
            return !this.lion.isBaby() && !this.lion.isSleeping() && this.lion.level().isNight() && super.canUse();
        }
    }

    public static class ReturnToPrideCenterGoal extends Goal {
        private static final double RETURN_THRESHOLD_SQR = 576.0D;

        private final LionEntity lion;
        private final double speedModifier;

        public ReturnToPrideCenterGoal(LionEntity lion, double speedModifier) {
            this.lion = lion;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.lion.getPrideCenterPos() == null) {
                return false;
            }

            if (this.lion.getTarget() != null || this.lion.isBusy() || this.lion.isSleeping()) {
                return false;
            }

            return this.lion.blockPosition().distSqr(this.lion.getPrideCenterPos()) > RETURN_THRESHOLD_SQR;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.lion.getPrideCenterPos() == null) {
                return false;
            }

            return this.lion.blockPosition().distSqr(this.lion.getPrideCenterPos()) > RETURN_THRESHOLD_SQR / 2.0D && this.lion.getTarget() == null && !this.lion.isBusy();
        }

        @Override
        public void tick() {
            BlockPos prideCenterPos = this.lion.getPrideCenterPos();
            if (prideCenterPos != null) {
                this.lion.getNavigation().moveTo(prideCenterPos.getX() + 0.5D, prideCenterPos.getY(), prideCenterPos.getZ() + 0.5D, this.speedModifier);
            }
        }
    }
}