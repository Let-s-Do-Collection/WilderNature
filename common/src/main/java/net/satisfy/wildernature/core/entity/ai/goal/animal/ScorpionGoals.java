package net.satisfy.wildernature.core.entity.ai.goal.animal;

import java.util.EnumSet;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.satisfy.wildernature.core.entity.animal.tameable.ScorpionEntity;

public class ScorpionGoals {

    public static class ScorpionBurrowGoal extends Goal {
        private static final int BURROW_CHANCE_INTERVAL = 80;

        private final ScorpionEntity scorpion;
        private int checkCooldown = BURROW_CHANCE_INTERVAL;

        public ScorpionBurrowGoal(ScorpionEntity scorpion) {
            this.scorpion = scorpion;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            if (!this.scorpion.canBurrow()) {
                return false;
            }
            if (this.checkCooldown-- > 0) {
                return false;
            }
            this.checkCooldown = BURROW_CHANCE_INTERVAL;
            return this.scorpion.getRandom().nextFloat() < 0.3F;
        }

        @Override
        public boolean canContinueToUse() {
            return this.scorpion.isBurrowed() && this.scorpion.getTarget() == null && !this.scorpion.isTame() && !this.scorpion.isOrderedToSit();
        }

        @Override
        public void start() {
            this.scorpion.burrow();
        }

        @Override
        public void stop() {
            if (this.scorpion.isBurrowed()) {
                this.scorpion.unburrow();
            }
        }

        @Override
        public void tick() {
            this.scorpion.getNavigation().stop();
        }
    }

    public static class ScorpionProximityTargetGoal extends TargetGoal {

        private static final int SEARCH_COOLDOWN = 20;

        private final ScorpionEntity scorpion;
        private int searchCooldown = SEARCH_COOLDOWN;

        public ScorpionProximityTargetGoal(ScorpionEntity scorpion) {
            super(scorpion, false);
            this.scorpion = scorpion;
        }

        @Override
        public boolean canUse() {
            if (this.scorpion.isTame() || this.scorpion.isOrderedToSit() || this.scorpion.getTarget() != null) {
                return false;
            }
            if (this.searchCooldown-- > 0) {
                return false;
            }

            this.searchCooldown = SEARCH_COOLDOWN;

            Player nearestPlayer = this.scorpion.level().getNearestPlayer(this.scorpion, ScorpionEntity.AGGRO_RADIUS);
            if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator()) {
                return false;
            }

            this.scorpion.unburrow();
            this.scorpion.setTarget(nearestPlayer);
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}