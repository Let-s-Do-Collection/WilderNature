package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.DeerEntity;

import java.util.EnumSet;
import java.util.Objects;

public final class DeerGoals {
    private DeerGoals() {
    }

    public static class DeerFollowLeaderGoal extends Goal {
        private final DeerEntity deer;

        public DeerFollowLeaderGoal(DeerEntity deer) {
            this.deer = deer;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.deer.isLeader()) {
                return false;
            }
            if (this.deer.isDeerRunning()) {
                return false;
            }
            if (this.deer.isSleeping()) {
                return false;
            }

            DeerEntity leader = this.deer.getLeader();
            return leader != null && leader != this.deer;
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void tick() {
            DeerEntity leader = this.deer.getLeader();
            if (leader == null) {
                return;
            }

            this.deer.getLookControl().setLookAt(leader);

            double distanceToLeader = this.deer.distanceToSqr(leader);

            if (distanceToLeader > 4.0D) {
                this.deer.getNavigation().moveTo(leader, 1.1D);
            }

            if (distanceToLeader > 1600.0D) {
                Vec3 newPosition = leader.position().add((this.deer.getRandom().nextDouble() - 0.5D) * 2.0D, 0.0D, (this.deer.getRandom().nextDouble() - 0.5D) * 2.0D);
                this.deer.setPos(newPosition.x, newPosition.y, newPosition.z);
            }
        }
    }

    public static class DeerAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final DeerEntity deer;

        public DeerAvoidEntityGoal(DeerEntity deer, Class<T> toAvoidClass) {
            super(deer, toAvoidClass, 16.0F, 2.0D, 2.0D);
            this.deer = deer;
        }

        @Override
        public boolean canUse() {
            if (this.toAvoid instanceof Player player) {
                if (player.isCrouching() || player.isCreative() || player.isSpectator()) {
                    return false;
                }
            }
            return super.canUse();
        }

        @Override
        public void start() {
            this.deer.spawnAlertParticle();
            if (this.toAvoid != null) {
                this.deer.triggerPanic(this.toAvoid.position());
            }
            super.start();
        }
    }

    public static class DeerSeekShelterGoal extends Goal {
        private static final int SEARCH_RADIUS = 10;
        private static final int COOLDOWN_AFTER_FAILURE = 60;

        private final DeerEntity deer;
        private BlockPos targetPosition;
        private int recalculationCooldown = 0;
        private int failureCooldown = 0;

        public DeerSeekShelterGoal(DeerEntity deer) {
            this.deer = deer;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.deer.isDeerRunning() || this.deer.isSleeping() || this.deer.level().isDay()) {
                return false;
            }
            if (failureCooldown > 0) {
                failureCooldown--;
                return false;
            }
            if (this.deer.level().getBlockState(this.deer.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                return false;
            }

            this.targetPosition = findNearestGrassPosition();
            return this.targetPosition != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPosition != null
                    && !this.deer.isDeerRunning()
                    && !this.deer.isSleeping()
                    && !this.deer.level().isDay()
                    && !this.deer.level().getBlockState(this.deer.blockPosition().below()).is(Blocks.GRASS_BLOCK);
        }

        @Override
        public void start() {
            this.recalculationCooldown = 0;
            this.moveToTarget();
        }

        @Override
        public void tick() {
            if (this.targetPosition == null) return;

            if (--this.recalculationCooldown <= 0 || this.deer.getNavigation().isDone()) {
                this.recalculationCooldown = 15;
                this.moveToTarget();
            }

            if (this.deer.distanceToSqr(Vec3.atBottomCenterOf(this.targetPosition)) < 2.5D) {
                this.targetPosition = null;
            }
        }

        @Override
        public void stop() {
            this.targetPosition = null;
        }

        private void moveToTarget() {
            if (this.targetPosition == null) return;
            Vec3 center = Vec3.atBottomCenterOf(this.targetPosition);
            this.deer.getNavigation().moveTo(center.x, center.y, center.z, 1.15D);
        }

        private BlockPos findNearestGrassPosition() {
            BlockPos origin = this.deer.blockPosition();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;

            for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    BlockPos pos = origin.offset(x, 0, z);
                    if (!this.deer.level().getBlockState(pos).is(Blocks.GRASS_BLOCK)) continue;

                    BlockPos standPos = pos.above();
                    if (!this.deer.level().isEmptyBlock(standPos) || !this.deer.level().isEmptyBlock(standPos.above()))
                        continue;

                    double dist = this.deer.distanceToSqr(Vec3.atBottomCenterOf(standPos));
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = standPos.immutable();
                    }
                }
            }
            if (best == null) {
                this.failureCooldown = COOLDOWN_AFTER_FAILURE;
            }
            return best;
        }
    }

    public static class DeerEatingGoal extends Goal {
        private static final int EATING_DURATION_TICKS = 37;
        private static final int COOLDOWN_TICKS = 400;
        private static final AttributeModifier MOVEMENT_MODIFIER = new AttributeModifier(ResourceLocation.parse("deer_eat_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        private final DeerEntity deer;
        private int counter;

        public DeerEatingGoal(DeerEntity deer) {
            this.deer = deer;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.deer.getRandom().nextFloat() < 0.01F && !this.deer.isDeerRunning() && this.deer.getGlobalCooldown() == 0 && !this.deer.isSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return this.counter < EATING_DURATION_TICKS && !this.deer.isDeerRunning() && !this.deer.isSleeping();
        }

        @Override
        public void tick() {
            this.counter++;
        }

        @Override
        public void start() {
            this.counter = 0;
            if (this.deer.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.deer.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(MOVEMENT_MODIFIER);
            }
            this.deer.startEating();
        }

        @Override
        public void stop() {
            if (this.deer.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.deer.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(MOVEMENT_MODIFIER);
            }
            this.deer.stopEating();
            this.deer.setGlobalCooldown(COOLDOWN_TICKS);
        }
    }

    public static class DeerLookAroundGoal extends Goal {
        private static final int DURATION = 70;
        private static final int COOLDOWN = 400;
        private static final AttributeModifier MOVEMENT_MODIFIER = new AttributeModifier(ResourceLocation.parse("deer_look_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        private final DeerEntity deer;
        private int counter;

        public DeerLookAroundGoal(DeerEntity deer) {
            this.deer = deer;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.deer.getRandom().nextFloat() < 0.01F && !this.deer.isDeerRunning() && this.deer.getGlobalCooldown() == 0 && !this.deer.isSleeping();
        }

        @Override
        public boolean canContinueToUse() {
            return this.counter < DURATION && !this.deer.isDeerRunning() && !this.deer.isSleeping();
        }

        @Override
        public void tick() {
            this.counter++;
        }

        @Override
        public void start() {
            this.counter = 0;
            if (this.deer.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.deer.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(MOVEMENT_MODIFIER);
            }
            this.deer.startLookingAround();
        }

        @Override
        public void stop() {
            if (this.deer.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.deer.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(MOVEMENT_MODIFIER);
            }
            this.deer.stopLookingAround();
            this.deer.setGlobalCooldown(COOLDOWN);
        }
    }
}