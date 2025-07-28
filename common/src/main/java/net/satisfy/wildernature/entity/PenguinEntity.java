package net.satisfy.wildernature.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class PenguinEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

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
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
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
        return EntityRegistry.PENGUIN.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public static class BoatDrivingGoal extends Goal {
        private final Mob entity;
        private Boat boat;
        private final double speed;

        public BoatDrivingGoal(Mob entity, double speed) {
            this.entity = entity;
            this.speed = speed;
        }

        @Override
        public boolean canUse() {
            if (entity.isPassenger() && entity.getVehicle() instanceof Boat) {
                boat = (Boat) entity.getVehicle();
                return true;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse() && !boat.isRemoved();
        }

        @Override
        public void start() {
            boat.setYRot(entity.getYRot());
        }

        @Override
        public void stop() {
            boat = null;
        }

        @Override
        public void tick() {
            if (boat == null) return;

            if (isOnWater()) {
                float yaw = entity.getYRot();
                Vector3f moveDirection = new Vector3f((float) -Math.sin(Math.toRadians(yaw)), 0, (float) Math.cos(Math.toRadians(yaw)));
                boat.setDeltaMovement(moveDirection.x() * speed * 1.25, boat.getDeltaMovement().y(), moveDirection.z() * speed * 1.25);
                boat.setPaddleState(moveDirection.x() != 0 || moveDirection.z() != 0, moveDirection.x() != 0 || moveDirection.z() != 0);

            } else {
                navigateToWater();
            }
        }

        private boolean isOnWater() {
            BlockPos pos = boat.blockPosition();
            BlockState blockState = entity.level().getBlockState(pos.below());
            return blockState.is(Blocks.WATER);
        }

        private void navigateToWater() {
            BlockPos boatPos = boat.blockPosition();
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    BlockPos pos = boatPos.offset(dx, 0, dz);
                    BlockState blockState = entity.level().getBlockState(pos);
                    if (blockState.is(Blocks.WATER)) {
                        double directionX = pos.getX() + 0.5 - boat.getX();
                        double directionZ = pos.getZ() + 0.5 - boat.getZ();
                        Vector3f direction = new Vector3f((float) directionX, 0, (float) directionZ).normalize();
                        boat.setDeltaMovement(direction.x() * speed, boat.getDeltaMovement().y, direction.z() * speed);
                        return;
                    }
                }
            }
        }
    }
}
