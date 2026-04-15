package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.passive.BoarEntity;

import java.util.EnumSet;

public class BoarRootingGoal extends Goal {
    private static final int SEARCH_RADIUS = 10;
    private static final int PREPARATION_TICKS = 8;
    private static final int FAILURE_COOLDOWN = 80;
    private static final int REPATH_COOLDOWN = 10;
    private static final double MOVE_SPEED = 1.25D;
    private static final AttributeModifier ROOTING_IMMOBILIZE_MODIFIER = new AttributeModifier(ResourceLocation.parse("wildernature:boar_rooting_immobilize"), -1000.0D, AttributeModifier.Operation.ADD_VALUE);
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
        if (this.boar.isBaby()) return false;
        if (this.boar.isDigging()) return false;
        if (this.boar.isSleeping()) return false;
        if (this.boar.getRootingCooldownTicks() > 0) return false;

        if (this.failureCooldown > 0) {
            this.failureCooldown--;
            return false;
        }

        BlockPos requested = this.boar.consumeRequestedRootingTarget();
        if (requested != null) {
            this.targetPosition = requested;
            this.forcedRooting = this.boar.consumeForceRooting();
            return true;
        }

        if (this.boar.getRandom().nextInt(100) != 0) return false;

        this.targetPosition = this.findTarget();
        this.forcedRooting = false;
        return this.targetPosition != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetPosition == null) return false;
        if (this.boar.isBaby()) return false;
        if (this.boar.isDigging()) return false;
        if (this.boar.isSleeping()) return false;
        if (!this.isRootable(this.boar.level().getBlockState(this.targetPosition.below()))) return false;
        if (this.isReserved(this.targetPosition)) return false;

        return !this.isAtTarget() || this.preparationTicks < PREPARATION_TICKS;
    }

    @Override
    public void start() {
        this.preparationTicks = 0;
        this.repathCooldown = 0;
    }

    @Override
    public void tick() {
        if (this.targetPosition == null) return;

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

        this.applyRootingModifier();
        this.boar.startRootingAnimation();
        this.finishRooting(this.targetPosition.below());

        this.targetPosition = null;
    }

    private void applyRootingModifier() {
        var instance = this.boar.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null && !instance.hasModifier(ROOTING_IMMOBILIZE_MODIFIER.id())) {
            instance.addTransientModifier(ROOTING_IMMOBILIZE_MODIFIER);
        }
    }

    private void removeRootingModifier() {
        var instance = this.boar.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(ROOTING_IMMOBILIZE_MODIFIER.id());
        }
    }

    @Override
    public void stop() {
        if (this.forcedRooting) {
            this.boar.clearRequestedRootingTarget();
        }

        this.removeRootingModifier();

        this.targetPosition = null;
        this.preparationTicks = 0;
        this.repathCooldown = 0;
        this.forcedRooting = false;
    }

    private void finishRooting(BlockPos pos) {
        if (!(this.boar.level() instanceof ServerLevel serverLevel)) return;

        BlockState state = this.boar.level().getBlockState(pos);
        if (!this.isRootable(state)) return;

        this.boar.level().levelEvent(2001, pos, Block.getId(state));

        if (!this.boar.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) return;

        BlockState replacement = Blocks.COARSE_DIRT.defaultBlockState();
        float lootChance = 1.0F;

        if (state.is(Blocks.DIRT)) {
            lootChance = 0.30F;
            BlockState fertilized = this.getFertilized();
            if (fertilized != null && this.boar.getRandom().nextFloat() < 0.025F) {
                replacement = fertilized;
            }
        } else if (state.is(Blocks.COARSE_DIRT)) {
            lootChance = 0.15F;
            BlockState fertilized = this.getFertilized();
            if (fertilized != null && this.boar.getRandom().nextFloat() < 0.075F) {
                replacement = fertilized;
            }
        }

        this.boar.level().setBlock(pos, replacement, 2);

        if (this.boar.getRandom().nextFloat() < lootChance) {
            this.boar.spawnRootingLoot(serverLevel, pos);
        }

        this.boar.ate();
    }

    private boolean isRootable(BlockState state) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT);
    }

    private BlockState getFertilized() {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("farm_and_charm:fertilized_soil"))
                .filter(block -> block != Blocks.AIR)
                .map(Block::defaultBlockState)
                .orElse(null);
    }

    private boolean isAtTarget() {
        return this.targetPosition != null && this.boar.distanceToSqr(Vec3.atBottomCenterOf(this.targetPosition)) < 2.8D;
    }

    private BlockPos findTarget() {
        BlockPos origin = this.boar.blockPosition();
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                BlockPos ground = origin.offset(x, 0, z);
                if (!this.isRootable(this.boar.level().getBlockState(ground))) continue;

                BlockPos stand = ground.above();
                if (!this.boar.level().isEmptyBlock(stand) || !this.boar.level().isEmptyBlock(stand.above())) continue;
                if (this.isReserved(stand)) continue;

                double dist = this.boar.distanceToSqr(Vec3.atBottomCenterOf(stand));
                if (dist < bestDistance) {
                    bestDistance = dist;
                    best = stand.immutable();
                }
            }
        }

        if (best == null) {
            this.failureCooldown = FAILURE_COOLDOWN;
        }

        return best;
    }

    private boolean isReserved(BlockPos stand) {
        BlockPos ground = stand.below();

        for (BoarEntity other : this.boar.level().getEntitiesOfClass(BoarEntity.class, this.boar.getBoundingBox().inflate(SEARCH_RADIUS))) {
            if (other == this.boar) continue;

            BlockPos requested = other.getRequestedRootingTarget();
            if (requested != null && (requested.equals(stand) || requested.below().equals(ground))) return true;

            if (other.isDigging()) {
                BlockPos pos = other.blockPosition();
                if (pos.equals(stand) || pos.below().equals(ground) || pos.closerThan(stand, 1.5D)) return true;
            }
        }

        return false;
    }
}