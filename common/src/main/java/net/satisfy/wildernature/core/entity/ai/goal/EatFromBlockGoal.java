package net.satisfy.wildernature.core.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.function.Predicate;

public class EatFromBlockGoal extends Goal {
    private final Mob mob;
    private final double speed;
    private final int searchRange;
    private final Predicate<BlockState> targetPredicate;
    private final EatAction eatAction;
    private final float healAmount;
    private final int cooldown;
    private final SoundEvent sound;
    private final Runnable startAnimation;
    private final Runnable stopAnimation;

    private BlockPos targetPos;
    private int eatTicks;
    private int cooldownTicks;

    public EatFromBlockGoal(Mob mob, double speed, int searchRange, Predicate<BlockState> targetPredicate, EatAction eatAction, float healAmount, int cooldown, SoundEvent sound, @Nullable Runnable startAnimation, @Nullable Runnable stopAnimation) {
        this.mob = mob;
        this.speed = speed;
        this.searchRange = searchRange;
        this.targetPredicate = targetPredicate;
        this.eatAction = eatAction;
        this.healAmount = healAmount;
        this.cooldown = cooldown;
        this.sound = sound;
        this.startAnimation = startAnimation;
        this.stopAnimation = stopAnimation;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldownTicks > 0) {
            this.cooldownTicks--;
            return false;
        }

        this.targetPos = this.findTarget();
        return this.targetPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetPos != null && this.eatTicks < 40;
    }

    @Override
    public void start() {
        this.eatTicks = 0;
        if (this.startAnimation != null) this.startAnimation.run();
    }

    @Override
    public void stop() {
        if (this.stopAnimation != null) this.stopAnimation.run();
        this.cooldownTicks = this.cooldown;
        this.targetPos = null;
    }

    @Override
    public void tick() {
        if (this.targetPos == null) return;

        if (!this.targetPos.closerToCenterThan(this.mob.position(), 1.5D)) {
            this.mob.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speed);
            return;
        }

        this.mob.getNavigation().stop();
        this.mob.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D);

        this.eatTicks++;

        if (this.eatTicks == 20) {
            Level level = this.mob.level();
            BlockState state = level.getBlockState(this.targetPos);

            this.eatAction.eat(level, this.targetPos, state, this.mob);
            this.mob.heal(this.healAmount);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D, 6, 0.2D, 0.2D, 0.2D, 0.0D);
            }

            level.playSound(null, this.targetPos, this.sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    private BlockPos findTarget() {
        BlockPos origin = this.mob.blockPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = -this.searchRange; x <= this.searchRange; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -this.searchRange; z <= this.searchRange; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState state = this.mob.level().getBlockState(pos);

                    if (this.targetPredicate.test(state)) {
                        return pos.immutable();
                    }
                }
            }
        }

        return null;
    }

    public interface EatAction {
        void eat(Level level, BlockPos pos, BlockState state, Mob mob);
    }
}