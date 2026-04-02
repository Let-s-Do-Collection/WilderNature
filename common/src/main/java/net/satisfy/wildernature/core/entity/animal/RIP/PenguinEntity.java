package net.satisfy.wildernature.core.entity.animal.RIP;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class PenguinEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
        if (this.isPassenger() && this.getVehicle() instanceof Boat boat) {
            boat.setDeltaMovement(boat.getDeltaMovement().multiply(1.25, 1.0, 1.25));
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        boolean idleAllowed = !moving;

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        float f;
        if (this.getPose() == Pose.STANDING) {
            f = Math.min(pPartialTick * 6F, 1f);
        } else {
            f = 0f;
        }

        this.walkAnimation.update(f, 0.2f);
    }

    public PenguinEntity(EntityType<? extends PenguinEntity> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0F);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Animal.createLivingAttributes().add(Attributes.MAX_HEALTH, 14).add(Attributes.FOLLOW_RANGE, 10).add(Attributes.MOVEMENT_SPEED, 0.23000000417232513);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new BoatDrivingGoal(this, 0.5));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.PENGUIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.PENGUIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.PENGUIN_DEATH.get();
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CHICKEN_STEP, 0.15F, 1.0F);
    }

    @Override
    @Nullable
    public PenguinEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.PENGUIN.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }


    public static class BoatDrivingGoal extends Goal {
        private final Mob entity;
        private Boat boat;
        private final double speed;

        private Vec3 routeCenter;
        private double routeRadius;
        private double routeAngle;
        private int dismountCooldown;

        public BoatDrivingGoal(Mob entity, double speed) {
            this.entity = entity;
            this.speed = speed;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (entity.isPassenger() && entity.getVehicle() instanceof Boat currentBoat && !currentBoat.isRemoved()) {
                boat = currentBoat;
                return true;
            }
            boat = null;
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return boat != null && !boat.isRemoved() && entity.isPassenger() && entity.getVehicle() == boat;
        }

        @Override
        public void start() {
            routeCenter = boat.position();
            routeRadius = 6.0 + entity.getRandom().nextInt(6);
            routeAngle = entity.getRandom().nextDouble() * 6.283185307179586;
            dismountCooldown = 80;
            boat.setPaddleState(false, false);
        }

        @Override
        public void stop() {
            if (boat != null) {
                boat.setPaddleState(false, false);
            }
            boat = null;
            routeCenter = null;
        }

        @Override
        public void tick() {
            if (boat == null || routeCenter == null) return;

            if (dismountCooldown > 0) dismountCooldown--;

            double targetX = routeCenter.x + Math.cos(routeAngle) * routeRadius;
            double targetZ = routeCenter.z + Math.sin(routeAngle) * routeRadius;

            double dx = targetX - boat.getX();
            double dz = targetZ - boat.getZ();
            double distSq = dx * dx + dz * dz;

            if (distSq < 1.2) {
                routeAngle += 0.12;
                if (routeAngle > 6.283185307179586) routeAngle -= 6.283185307179586;
            }

            double len = Math.sqrt(dx * dx + dz * dz);
            if (len > 1.0E-4) {
                dx /= len;
                dz /= len;
            } else {
                dx = 0.0;
                dz = 0.0;
            }

            Vec3 current = boat.getDeltaMovement();
            double desiredX = dx * speed;
            double desiredZ = dz * speed;

            double blendedX = current.x + (desiredX - current.x) * 0.25;
            double blendedZ = current.z + (desiredZ - current.z) * 0.25;

            boat.setDeltaMovement(blendedX, current.y, blendedZ);

            boolean paddling = (blendedX * blendedX + blendedZ * blendedZ) > 1.0E-4;
            boat.setPaddleState(paddling, paddling);

            double cx = boat.getX() - routeCenter.x;
            double cz = boat.getZ() - routeCenter.z;
            double centerDistSq = cx * cx + cz * cz;

            if (!entity.level().isClientSide() && dismountCooldown == 0 && centerDistSq < 2.0) {
                if (entity.getRandom().nextFloat() < 0.02F) {
                    boat.setPaddleState(false, false);
                    entity.stopRiding();
                    dismountCooldown = 200;
                } else {
                    dismountCooldown = 40;
                }
            }
        }
    }
}