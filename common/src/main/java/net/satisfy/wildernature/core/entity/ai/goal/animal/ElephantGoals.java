package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.defensive.ElephantEntity;

import java.util.EnumSet;
import java.util.List;

public class ElephantGoals {
    public static final double THROW_RANGE_MIN = 6.0D;
    public static final double THROW_RANGE_MAX = 14.0D;
    public static final double STAMP_RANGE = 3.4D;
    public static final double CHARGE_START_RANGE = 8.0D;
    public static final double CHARGE_MAX_RANGE = 20.0D;
    public static final int THROW_DURATION = 20;
    public static final int THROW_IMPACT_TICK = 12;
    public static final int STAMP_DURATION = 16;
    public static final int STAMP_IMPACT_TICK = 9;

    public static class ElephantStampGoal extends Goal {
        private final ElephantEntity elephantEntity;

        public ElephantStampGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            return target != null && target.isAlive() && this.elephantEntity.canStartStamp() && this.elephantEntity.distanceTo(target) <= STAMP_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.isStamping() && this.elephantEntity.stampTicks > 0;
        }

        @Override
        public void start() {
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startStamp();
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) return;

            this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (!this.elephantEntity.stampImpactDone && this.elephantEntity.stampTicks <= STAMP_DURATION - STAMP_IMPACT_TICK) {
                this.elephantEntity.stampImpactDone = true;
                List<LivingEntity> hitTargets = this.elephantEntity.level().getEntitiesOfClass(LivingEntity.class,
                        this.elephantEntity.getBoundingBox().inflate(3.6D, 1.0D, 3.6D),
                        entity -> entity != this.elephantEntity && entity.isAlive());
                for (LivingEntity hitTarget : hitTargets) {
                    hitTarget.hurt(this.elephantEntity.damageSources().mobAttack(this.elephantEntity), 11.0F);
                    Vec3 pushDirection = hitTarget.position().subtract(this.elephantEntity.position());
                    if (pushDirection.lengthSqr() > 1.0E-6D) {
                        Vec3 normalizedDirection = pushDirection.normalize().scale(0.9D);
                        hitTarget.push(normalizedDirection.x, 0.3D, normalizedDirection.z);
                    }
                }
            }
        }
    }

    public static class ElephantChargeGoal extends Goal {
        private final ElephantEntity elephantEntity;

        public ElephantChargeGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null || !target.isAlive() || this.elephantEntity.isBaby() || !this.elephantEntity.canStartCharge()) return false;
            double distanceToTarget = this.elephantEntity.distanceTo(target);
            return distanceToTarget >= CHARGE_START_RANGE && distanceToTarget <= CHARGE_MAX_RANGE;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.chargeWindupTicks > 0 || this.elephantEntity.chargeActiveTicks > 0 || this.elephantEntity.chargeRecoveryTicks > 0;
        }

        @Override
        public void start() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) return;
            Vec3 direction = target.position().subtract(this.elephantEntity.position());
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startCharge(direction.x, direction.z);
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target != null && this.elephantEntity.chargeWindupTicks > 0) {
                this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
        }
    }

    public static class ReturnToHerdGoal extends Goal {
        private final ElephantEntity elephantEntity;
        private final double speedModifier;

        public ReturnToHerdGoal(ElephantEntity elephantEntity, double speedModifier) {
            this.elephantEntity = elephantEntity;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.elephantEntity.isBusy() || this.elephantEntity.getTarget() != null) return false;
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            double maxDistance = this.elephantEntity.isBaby() ? ElephantEntity.HERD_RETURN_RADIUS : ElephantEntity.HERD_RADIUS;
            return this.elephantEntity.blockPosition().distSqr(herdCenter) > maxDistance * maxDistance;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.elephantEntity.isBusy() || this.elephantEntity.getTarget() != null) return false;
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            double continueDistance = this.elephantEntity.isBaby() ? 9.0D : 16.0D;
            return this.elephantEntity.blockPosition().distSqr(herdCenter) > continueDistance * continueDistance && !this.elephantEntity.getNavigation().isDone();
        }

        @Override
        public void start() {
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            this.elephantEntity.getNavigation().moveTo(herdCenter.getX() + 0.5D, herdCenter.getY(), herdCenter.getZ() + 0.5D, this.speedModifier);
        }

        @Override
        public void tick() {
            BlockPos herdCenter = this.elephantEntity.getHerdCenter();
            this.elephantEntity.getNavigation().moveTo(herdCenter.getX() + 0.5D, herdCenter.getY(), herdCenter.getZ() + 0.5D, this.speedModifier);
        }
    }

    public static class ElephantDrinkGoal extends Goal {
        private final ElephantEntity elephantEntity;

        public ElephantDrinkGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.elephantEntity.canStartDrinking()) return false;
            if (this.elephantEntity.getTarget() != null) return false;
            if (this.elephantEntity.getRandom().nextInt(200) != 0) return false;
            return this.isNearWater();
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.isDrinking();
        }

        @Override
        public void start() {
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startDrinking();
        }

        private boolean isNearWater() {
            BlockPos pos = this.elephantEntity.blockPosition();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos check = pos.offset(dx, 0, dz);
                    if (this.elephantEntity.level().getFluidState(check).is(FluidTags.WATER)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public static class ElephantThrowGoal extends Goal {
        private final ElephantEntity elephantEntity;

        public ElephantThrowGoal(ElephantEntity elephantEntity) {
            this.elephantEntity = elephantEntity;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null || !target.isAlive() || !this.elephantEntity.canStartThrow()) return false;
            double distanceToTarget = this.elephantEntity.distanceTo(target);
            return distanceToTarget >= THROW_RANGE_MIN && distanceToTarget <= THROW_RANGE_MAX;
        }

        @Override
        public boolean canContinueToUse() {
            return this.elephantEntity.isThrowing() && this.elephantEntity.throwTicks > 0;
        }

        @Override
        public void start() {
            this.elephantEntity.getNavigation().stop();
            this.elephantEntity.startThrow();
        }

        @Override
        public void tick() {
            LivingEntity target = this.elephantEntity.getTarget();
            if (target == null) return;

            this.elephantEntity.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (!this.elephantEntity.throwImpactDone && this.elephantEntity.throwTicks <= THROW_DURATION - THROW_IMPACT_TICK) {
                this.elephantEntity.throwImpactDone = true;
                if (this.elephantEntity.hasLineOfSight(target) && this.elephantEntity.distanceTo(target) <= THROW_RANGE_MAX + 2.0D) {
                    target.hurt(this.elephantEntity.damageSources().mobAttack(this.elephantEntity), 8.0F);
                    Vec3 pushDirection = target.position().subtract(this.elephantEntity.position()).normalize().scale(1.0D);
                    target.push(pushDirection.x, 0.45D, pushDirection.z);
                }
            }
        }
    }
}