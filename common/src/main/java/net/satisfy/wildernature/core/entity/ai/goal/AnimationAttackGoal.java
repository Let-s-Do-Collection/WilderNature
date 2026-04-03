package net.satisfy.wildernature.core.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

import java.util.function.Consumer;

public class AnimationAttackGoal<T extends PathfinderMob> extends MeleeAttackGoal {
    private final T animationEntity;
    private final Consumer<Boolean> attackingSetter;
    private int counter;
    private final int attackDelay;
    private final int attackTick;
    private int timeout;

    public AnimationAttackGoal(T animationEntity, double speedModifier, boolean followingTargetEvenIfNotSeen, int attackDelay, int attackTick, Consumer<Boolean> attackingSetter) {
        super(animationEntity, speedModifier, followingTargetEvenIfNotSeen);
        this.animationEntity = animationEntity;
        this.attackingSetter = attackingSetter;
        this.attackDelay = attackDelay;
        this.attackTick = attackTick;
        this.timeout = 0;
    }

    @Override
    public void start() {
        this.timeout = 0;
        super.start();
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.timeout < 60;
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity targetEntity = this.animationEntity.getTarget();
        if (targetEntity != null) {
            this.checkAndPerformAttack(targetEntity);
        }
        this.attackingSetter.accept(this.counter != 0);

        if (this.counter != 0) {
            this.counter++;
        }

        if (this.counter >= this.attackDelay) {
            this.counter = 0;
        }
    }

    @Override
    protected void checkAndPerformAttack(LivingEntity targetEntity) {
        if (this.isTimeToAttack() && this.mob.isWithinMeleeAttackRange(targetEntity) && this.mob.getSensing().hasLineOfSight(targetEntity)) {
            if (this.counter == 0) {
                this.counter++;
            }

            if (this.counter == this.attackTick) {
                this.animationEntity.doHurtTarget(targetEntity);
            }

            this.timeout = 0;
        } else {
            this.timeout++;
        }
    }

    @Override
    public void stop() {
        this.attackingSetter.accept(false);
        super.stop();
    }
}