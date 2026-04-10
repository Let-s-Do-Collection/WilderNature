package net.satisfy.wildernature.core.entity.ai.goal;

import java.util.List;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import org.jetbrains.annotations.Nullable;

public class FollowParentAtDistanceGoal extends Goal {
    private static final double HORIZONTAL_SCAN_RANGE = 8.0D;
    private static final double VERTICAL_SCAN_RANGE = 4.0D;
    private static final double START_FOLLOW_DISTANCE_SQR = 16.0D;
    private static final double STOP_FOLLOW_DISTANCE_SQR = 6.25D;
    private static final double MAX_PARENT_DISTANCE_SQR = 256.0D;

    private final Animal child;
    private final double speedModifier;
    private int timeToRecalcPath;

    @Nullable
    private Animal parent;

    public FollowParentAtDistanceGoal(Animal child, double speedModifier) {
        this.child = child;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse() {
        if (this.child.getAge() >= 0) {
            return false;
        }

        List<? extends Animal> nearbyAnimals = this.child.level().getEntitiesOfClass(this.child.getClass(), this.child.getBoundingBox().inflate(HORIZONTAL_SCAN_RANGE, VERTICAL_SCAN_RANGE, HORIZONTAL_SCAN_RANGE));
        Animal nearestParent = null;
        double nearestDistance = Double.MAX_VALUE;

        for (Animal nearbyAnimal : nearbyAnimals) {
            if (nearbyAnimal.getAge() < 0) {
                continue;
            }

            double parentDistance = this.child.distanceToSqr(nearbyAnimal);
            if (parentDistance < nearestDistance) {
                nearestDistance = parentDistance;
                nearestParent = nearbyAnimal;
            }
        }

        if (nearestParent == null || nearestDistance < START_FOLLOW_DISTANCE_SQR) {
            return false;
        }

        this.parent = nearestParent;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.child.getAge() >= 0 || this.parent == null || !this.parent.isAlive()) {
            return false;
        }

        double parentDistance = this.child.distanceToSqr(this.parent);
        return parentDistance >= STOP_FOLLOW_DISTANCE_SQR && parentDistance <= MAX_PARENT_DISTANCE_SQR;
    }

    @Override
    public void start() {
        this.timeToRecalcPath = 0;
    }

    @Override
    public void stop() {
        this.parent = null;
        this.child.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.parent == null) {
            return;
        }

        double parentDistance = this.child.distanceToSqr(this.parent);
        if (parentDistance <= STOP_FOLLOW_DISTANCE_SQR) {
            this.child.getNavigation().stop();
            return;
        }

        if (--this.timeToRecalcPath > 0) {
            return;
        }

        this.timeToRecalcPath = this.adjustedTickDelay(10);

        double distance = Math.sqrt(parentDistance);
        double offsetDistance = 2.5D;
        double directionX = this.child.getX() - this.parent.getX();
        double directionZ = this.child.getZ() - this.parent.getZ();

        if (distance < 1.0E-4D) {
            this.child.getNavigation().moveTo(this.parent, this.speedModifier);
            return;
        }

        double normalizedX = directionX / distance;
        double normalizedZ = directionZ / distance;
        double targetX = this.parent.getX() + normalizedX * offsetDistance;
        double targetY = this.parent.getY();
        double targetZ = this.parent.getZ() + normalizedZ * offsetDistance;

        this.child.getNavigation().moveTo(targetX, targetY, targetZ, this.speedModifier);
    }
}