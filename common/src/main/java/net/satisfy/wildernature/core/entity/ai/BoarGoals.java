package net.satisfy.wildernature.core.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.BoarEntity;

import java.util.EnumSet;
import java.util.Objects;

public final class BoarGoals {
    private BoarGoals() {
    }

    public static class BoarRootingGoal extends Goal {
        private static final int SEARCH_RADIUS = 10;
        private static final int PREPARATION_TICKS = 8;
        private static final int FAILURE_COOLDOWN = 80;
        private static final int REPATH_COOLDOWN = 10;
        private static final double MOVE_SPEED = 1.25D;
        private static final AttributeModifier MOVEMENT_MODIFIER = new AttributeModifier(ResourceLocation.parse("boar_rooting_do_not_move"), -1000.0D, AttributeModifier.Operation.ADD_VALUE);

        private final BoarEntity boar;
        private BlockPos targetPosition;
        private int preparationTicks;
        private int repathCooldown;
        private int failureCooldown;
        private boolean forcedRooting;

        public BoarRootingGoal(BoarEntity boar) {
            this.boar = boar;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (this.boar.isBaby()) {
                return false;
            }
            if (this.boar.isDigging()) {
                return false;
            }
            if (this.boar.isSleeping()) {
                return false;
            }
            if (this.boar.getRootingCooldownTicks() > 0) {
                return false;
            }
            if (this.failureCooldown > 0) {
                this.failureCooldown--;
                return false;
            }

            BlockPos requestedTargetPosition = this.boar.consumeRequestedRootingTarget();
            if (requestedTargetPosition != null) {
                this.targetPosition = requestedTargetPosition;
                this.forcedRooting = this.boar.consumeForceRooting();
                return true;
            }

            if (this.boar.getRandom().nextInt(100) != 0) {
                return false;
            }

            this.targetPosition = this.findTarget();
            this.forcedRooting = false;
            return this.targetPosition != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPosition == null) {
                return false;
            }
            if (this.boar.isBaby()) {
                return false;
            }
            if (this.boar.isDigging()) {
                return false;
            }
            if (this.boar.isSleeping()) {
                return false;
            }
            if (!this.boar.level().getBlockState(this.targetPosition.below()).is(Blocks.GRASS_BLOCK)) {
                return false;
            }
            if (this.isTargetReservedByAnotherBoar(this.targetPosition)) {
                return false;
            }

            return !this.isAtTarget() || this.preparationTicks < PREPARATION_TICKS;
        }

        @Override
        public void start() {
            this.preparationTicks = 0;
            this.repathCooldown = 0;
        }

        @Override
        public void tick() {
            if (this.targetPosition == null) {
                return;
            }

            Vec3 targetCenter = Vec3.atBottomCenterOf(this.targetPosition);
            this.boar.getLookControl().setLookAt(targetCenter.x, targetCenter.y, targetCenter.z);

            if (!this.isAtTarget()) {
                if (--this.repathCooldown <= 0 || this.boar.getNavigation().isDone()) {
                    this.repathCooldown = REPATH_COOLDOWN;
                    this.boar.getNavigation().moveTo(targetCenter.x, targetCenter.y, targetCenter.z, MOVE_SPEED);
                }
                return;
            }

            this.boar.getNavigation().stop();

            if (this.preparationTicks < PREPARATION_TICKS) {
                this.preparationTicks++;
                if (this.preparationTicks == PREPARATION_TICKS) {
                    this.boar.spawnAlertParticle();
                }
                return;
            }

            if (this.boar.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.boar.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(MOVEMENT_MODIFIER);
            }

            this.boar.startRootingAnimation();
            this.boar.finishRooting(this.targetPosition.below());

            if (this.boar.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.boar.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(MOVEMENT_MODIFIER);
            }

            this.targetPosition = null;
        }

        @Override
        public void stop() {
            if (this.forcedRooting) {
                this.boar.clearRequestedRootingTarget();
            }
            if (this.boar.getAttribute(Attributes.MOVEMENT_SPEED) != null) {
                Objects.requireNonNull(this.boar.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(MOVEMENT_MODIFIER);
            }
            this.targetPosition = null;
            this.preparationTicks = 0;
            this.repathCooldown = 0;
            this.forcedRooting = false;
        }

        private boolean isAtTarget() {
            return this.targetPosition != null && this.boar.distanceToSqr(Vec3.atBottomCenterOf(this.targetPosition)) < 2.8D;
        }

        private BlockPos findTarget() {
            BlockPos origin = this.boar.blockPosition();
            BlockPos bestPosition = null;
            double bestDistance = Double.MAX_VALUE;

            for (int xOffset = -SEARCH_RADIUS; xOffset <= SEARCH_RADIUS; xOffset++) {
                for (int zOffset = -SEARCH_RADIUS; zOffset <= SEARCH_RADIUS; zOffset++) {
                    BlockPos blockPosition = origin.offset(xOffset, 0, zOffset);
                    if (!this.boar.level().getBlockState(blockPosition).is(Blocks.GRASS_BLOCK)) {
                        continue;
                    }

                    BlockPos standPosition = blockPosition.above();
                    if (!this.boar.level().isEmptyBlock(standPosition) || !this.boar.level().isEmptyBlock(standPosition.above())) {
                        continue;
                    }
                    if (this.isTargetReservedByAnotherBoar(standPosition)) {
                        continue;
                    }

                    double distanceToPosition = this.boar.distanceToSqr(Vec3.atBottomCenterOf(standPosition));
                    if (distanceToPosition < bestDistance) {
                        bestDistance = distanceToPosition;
                        bestPosition = standPosition.immutable();
                    }
                }
            }

            if (bestPosition == null) {
                this.failureCooldown = FAILURE_COOLDOWN;
            }

            return bestPosition;
        }

        private boolean isTargetReservedByAnotherBoar(BlockPos standPosition) {
            BlockPos groundPosition = standPosition.below();

            for (BoarEntity otherBoar : this.boar.level().getEntitiesOfClass(BoarEntity.class, this.boar.getBoundingBox().inflate(SEARCH_RADIUS))) {
                if (otherBoar == this.boar) {
                    continue;
                }

                BlockPos otherRequestedTarget = otherBoar.getRequestedRootingTarget();
                if (otherRequestedTarget != null && (otherRequestedTarget.equals(standPosition) || otherRequestedTarget.below().equals(groundPosition))) {
                    return true;
                }

                if (otherBoar.isDigging()) {
                    BlockPos otherBlockPosition = otherBoar.blockPosition();
                    if (otherBlockPosition.equals(standPosition) || otherBlockPosition.below().equals(groundPosition) || otherBlockPosition.closerThan(standPosition, 1.5D)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}