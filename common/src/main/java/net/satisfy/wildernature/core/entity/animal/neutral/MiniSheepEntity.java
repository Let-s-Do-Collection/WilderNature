package net.satisfy.wildernature.core.entity.animal.neutral;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.EatBlockGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.entity.ai.goal.animal.MiniSheepGoal;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MiniSheepEntity extends Animal implements Shearable {
    private static final EntityDataAccessor<Boolean> SHEARED = SynchedEntityData.defineId(MiniSheepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(MiniSheepEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> RUNNING = SynchedEntityData.defineId(MiniSheepEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int HERD_CHECK_INTERVAL_TICKS = 40;
    private static final int LEADER_CACHE_TICKS = 40;
    private static final int SLEEPING_PARTICLE_INTERVAL_TICKS = 14;
    private static final int HOME_RETURN_DISTANCE = 50;
    private static final int HOME_RADIUS = 32;
    private static final int FLEE_PLAYER_RADIUS = 9;
    private static final int FLEE_HERD_RADIUS = 18;
    private static final int FLEE_DURATION_TICKS = 80;
    private static final int MAEH_ANIMATION_TICKS = 20;
    private static final double HERD_SEARCH_RADIUS = 12.0D;
    private static final double FLEE_DISTANCE = 12.0D;

    private int eatAnimationTick;
    private int maehAnimationTick;
    private int shearCalmTicks;
    private EatBlockGoal eatBlockGoal;
    private boolean isLeader;
    @Nullable
    private UUID herdLeaderUUID;
    private int herdCheckCooldown;
    private int leaderCacheCooldown;
    @Nullable
    private MiniSheepEntity cachedLeader;
    @Nullable
    private BlockPos meadowHomePos;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;
    public boolean returningHome;
    @Nullable
    private BlockPos fleeTargetPos;
    private int fleeTicks;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState maehAnimationState = new AnimationState();

    public MiniSheepEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MiniSheepGoal.MiniSheepFleePlayerGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT), false) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new MiniSheepGoal.MiniSheepFollowLeaderGoal(this));
        this.goalSelector.addGoal(7, new MiniSheepGoal.MiniSheepReturnHomeGoal(this));
        this.goalSelector.addGoal(8, this.eatBlockGoal);
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && MiniSheepEntity.this.canUseMeadowStrollGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && MiniSheepEntity.this.canUseMeadowStrollGoal() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && !MiniSheepEntity.this.isFleeing() && !MiniSheepEntity.this.isMaehAnimating() && super.canContinueToUse();
            }
        });
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean eating = this.eatAnimationTick > 0;
        boolean sleeping = this.isMiniSheepSleeping();
        boolean running = this.isMiniSheepRunning();
        boolean idleAllowed = !moving && !eating && !sleeping && !running;

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        if (this.maehAnimationTick > 0) {
            this.maehAnimationState.startIfStopped(this.tickCount);
        } else {
            this.maehAnimationState.stop();
        }

        this.eatAnimationState.animateWhen(eating && !sleeping && !running, this.tickCount);
        this.runAnimationState.animateWhen(running, this.tickCount);
        this.sleepAnimationState.animateWhen(sleeping, this.tickCount);
    }

    public boolean isMaehAnimating() {
        return this.maehAnimationTick > 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.shearCalmTicks > 0) {
            this.shearCalmTicks--;
        }

        if (!this.level().isClientSide()) {
            this.updateHerd();
            this.tryPromoteToLeader();
            this.updateSleep();
            this.updateFleeState();
            this.updateRunningState();
        }

        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }

        if (this.eatAnimationTick > 0) {
            this.eatAnimationTick--;
            if (this.eatAnimationTick == 0) {
                this.eatAnimationState.stop();
            }
        }

        if (this.maehAnimationTick > 0) {
            this.maehAnimationTick--;
            if (this.maehAnimationTick == 0) {
                this.maehAnimationState.stop();
            }
        }
    }

    private void updateHerd() {
        if (this.herdCheckCooldown > 0) {
            this.herdCheckCooldown--;
        }
        if (this.leaderCacheCooldown > 0) {
            this.leaderCacheCooldown--;
        }

        if (this.isLeader) {
            this.cachedLeader = this;
            this.herdLeaderUUID = this.getUUID();
            return;
        }

        if (this.cachedLeader != null) {
            if (!this.cachedLeader.isAlive() || !this.cachedLeader.isLeader() || this.distanceToSqr(this.cachedLeader) > HERD_SEARCH_RADIUS * HERD_SEARCH_RADIUS) {
                this.cachedLeader = null;
                this.herdLeaderUUID = null;
                this.leaderCacheCooldown = 0;
            }
        }

        if (this.cachedLeader != null && this.leaderCacheCooldown > 0) {
            return;
        }

        if (this.herdCheckCooldown > 0 && this.cachedLeader == null) {
            return;
        }

        List<MiniSheepEntity> nearbySheep = this.level().getEntitiesOfClass(MiniSheepEntity.class, this.getBoundingBox().inflate(HERD_SEARCH_RADIUS));
        MiniSheepEntity closestLeader = this.findBestLeader(nearbySheep);

        if (closestLeader != null) {
            this.cachedLeader = closestLeader;
            this.herdLeaderUUID = closestLeader.getUUID();
            this.leaderCacheCooldown = LEADER_CACHE_TICKS;
        } else {
            this.cachedLeader = null;
            this.herdLeaderUUID = null;
            this.leaderCacheCooldown = 0;
        }

        this.herdCheckCooldown = HERD_CHECK_INTERVAL_TICKS;
    }

    @Nullable
    private MiniSheepEntity findBestLeader(List<MiniSheepEntity> nearbySheep) {
        MiniSheepEntity bestLeader = null;
        double bestScore = Double.MAX_VALUE;

        for (MiniSheepEntity nearby : nearbySheep) {
            if (nearby == this || !nearby.isLeader() || !nearby.isAlive()) {
                continue;
            }

            double distanceScore = this.distanceToSqr(nearby);
            int followerCount = nearby.countNearbyFollowers(nearbySheep);
            double score = distanceScore - followerCount * 0.75D;

            if (score < bestScore) {
                bestScore = score;
                bestLeader = nearby;
            }
        }

        return bestLeader;
    }

    private int countNearbyFollowers(List<MiniSheepEntity> nearbySheep) {
        int count = 0;
        UUID myUUID = this.getUUID();

        for (MiniSheepEntity nearby : nearbySheep) {
            if (nearby != this && nearby.isAlive() && myUUID.equals(nearby.herdLeaderUUID)) {
                count++;
            }
        }

        return count;
    }

    private void tryPromoteToLeader() {
        if (this.isLeader || this.isBaby()) {
            return;
        }
        if (this.cachedLeader != null || this.herdLeaderUUID != null) {
            return;
        }

        List<MiniSheepEntity> nearbySheep = this.level().getEntitiesOfClass(MiniSheepEntity.class, this.getBoundingBox().inflate(HERD_SEARCH_RADIUS));
        for (MiniSheepEntity nearbySheepEntity : nearbySheep) {
            if (nearbySheepEntity != this && nearbySheepEntity.isLeader()) {
                return;
            }
        }

        if (this.random.nextFloat() < 0.05F) {
            this.isLeader = true;
            this.cachedLeader = this;
            this.herdLeaderUUID = this.getUUID();
            this.herdCheckCooldown = HERD_CHECK_INTERVAL_TICKS;
            this.leaderCacheCooldown = LEADER_CACHE_TICKS;
        }
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        if (this.isFleeing()) {
            this.wakeUp();
            return;
        }

        boolean isNight = !this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.01D;
        boolean isOnGrassBlock = this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK);

        if (isNight && isStill && this.sleepCooldownTicks <= 0 && isOnGrassBlock) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks && !this.isMiniSheepSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isMiniSheepSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 20);
            }
        }

        if (this.isMiniSheepSleeping()) {
            if (this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
            }

            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX(), this.getY() + this.getBbHeight() * 0.75D, this.getZ(), 1, 0.15D, 0.3D, 0.15D, 0.0D);
            }

            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
    }

    private void updateFleeState() {
        if (this.fleeTicks > 0) {
            this.fleeTicks--;
        }

        if (this.fleeTargetPos == null) {
            return;
        }

        if (this.fleeTicks <= 0 || this.fleeTargetPos.closerToCenterThan(this.position(), 2.0D)) {
            this.clearFleeTarget();
        }
    }

    private void updateRunningState() {
        this.setMiniSheepRunning(this.returningHome || this.isFleeing());
    }

    private boolean hasWakeUpTriggerNearby() {
        Player player = this.getNearestThreateningPlayer();
        return player != null;
    }

    public boolean canUseMeadowStrollGoal() {
        if (this.meadowHomePos == null) {
            return true;
        }
        return this.blockPosition().distSqr(this.meadowHomePos) <= (double) (HOME_RADIUS * HOME_RADIUS);
    }

    @Nullable
    public MiniSheepEntity getLeader() {
        if (this.isLeader) {
            return this;
        }

        if (this.cachedLeader != null && this.cachedLeader.isAlive() && this.cachedLeader.isLeader()) {
            return this.cachedLeader;
        }

        if (this.herdLeaderUUID == null) {
            return null;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.herdLeaderUUID);
            if (entity instanceof MiniSheepEntity miniSheepEntity && miniSheepEntity.isAlive() && miniSheepEntity.isLeader()) {
                this.cachedLeader = miniSheepEntity;
                this.leaderCacheCooldown = LEADER_CACHE_TICKS;
                return miniSheepEntity;
            }
        }

        this.herdLeaderUUID = null;
        this.cachedLeader = null;
        this.leaderCacheCooldown = 0;
        return null;
    }

    public boolean isLeader() {
        return this.isLeader;
    }

    public boolean isMiniSheepSleeping() {
        return this.entityData.get(SLEEPING);
    }

    private void setMiniSheepSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    public boolean isMiniSheepRunning() {
        return this.entityData.get(RUNNING);
    }

    public void setMiniSheepRunning(boolean running) {
        this.entityData.set(RUNNING, running);
    }

    private void startSleeping() {
        this.setMiniSheepSleeping(true);
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
    }

    public void wakeUp() {
        if (this.isMiniSheepSleeping()) {
            this.setMiniSheepSleeping(false);
            this.sleepCooldownTicks = 200 + this.random.nextInt(200);
        }
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    public boolean shouldReturnHome() {
        if (this.meadowHomePos == null) {
            return false;
        }
        return this.blockPosition().distSqr(this.meadowHomePos) > (double) (HOME_RETURN_DISTANCE * HOME_RETURN_DISTANCE);
    }

    public void startReturningHome() {
        this.returningHome = true;
    }

    public void stopReturningHome() {
        this.returningHome = false;
    }

    @Nullable
    public BlockPos getMeadowHomePos() {
        return this.meadowHomePos;
    }

    public boolean isFleeing() {
        return this.fleeTicks > 0 && this.fleeTargetPos != null;
    }

    public boolean hasFleeTarget() {
        return this.fleeTargetPos != null;
    }

    @Nullable
    public BlockPos getFleeTargetPos() {
        return this.fleeTargetPos;
    }

    public void clearFleeTarget() {
        this.fleeTargetPos = null;
        this.fleeTicks = 0;
    }

    @Nullable
    public Player getNearestThreateningPlayer() {
        if (this.shearCalmTicks > 0 || this.isMaehAnimating()) {
            return null;
        }

        Player player = this.level().getNearestPlayer(this, FLEE_PLAYER_RADIUS);
        if (player == null) {
            return null;
        }
        if (player.isCreative() || player.isSpectator()) {
            return null;
        }
        if (player.isCrouching()) {
            return null;
        }
        return player;
    }

    public void startHerdFleeFrom(Player player) {
        List<MiniSheepEntity> nearbySheep = this.level().getEntitiesOfClass(MiniSheepEntity.class, this.getBoundingBox().inflate(FLEE_HERD_RADIUS));
        double herdCenterX = this.getX();
        double herdCenterZ = this.getZ();
        int herdCount = 1;

        for (MiniSheepEntity nearbySheepEntity : nearbySheep) {
            if (nearbySheepEntity == this) {
                continue;
            }
            herdCenterX += nearbySheepEntity.getX();
            herdCenterZ += nearbySheepEntity.getZ();
            herdCount++;
        }

        herdCenterX /= herdCount;
        herdCenterZ /= herdCount;

        double fleeDirectionX = herdCenterX - player.getX();
        double fleeDirectionZ = herdCenterZ - player.getZ();
        double fleeLength = Math.sqrt(fleeDirectionX * fleeDirectionX + fleeDirectionZ * fleeDirectionZ);

        if (fleeLength < 1.0E-4D) {
            float randomAngle = this.random.nextFloat() * (float) (Math.PI * 2.0D);
            fleeDirectionX = Mth.cos(randomAngle);
            fleeDirectionZ = Mth.sin(randomAngle);
            fleeLength = 1.0D;
        }

        fleeDirectionX /= fleeLength;
        fleeDirectionZ /= fleeLength;

        for (MiniSheepEntity nearbySheepEntity : nearbySheep) {
            nearbySheepEntity.setSharedFleeDirection(fleeDirectionX, fleeDirectionZ);
        }

        this.setSharedFleeDirection(fleeDirectionX, fleeDirectionZ);
    }

    private void setSharedFleeDirection(double fleeDirectionX, double fleeDirectionZ) {
        this.wakeUp();
        this.stopReturningHome();
        int targetX = Mth.floor(this.getX() + fleeDirectionX * FLEE_DISTANCE);
        int targetY = this.blockPosition().getY();
        int targetZ = Mth.floor(this.getZ() + fleeDirectionZ * FLEE_DISTANCE);
        this.fleeTargetPos = new BlockPos(targetX, targetY, targetZ);
        this.fleeTicks = FLEE_DURATION_TICKS;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.eatAnimationTick = this.eatBlockGoal.getEatAnimationTick();
        if (this.eatAnimationTick > 0) {
            this.level().broadcastEntityEvent(this, (byte) 10);
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SHEARED, false);
        builder.define(SLEEPING, false);
        builder.define(RUNNING, false);
    }

    @Override
    protected void updateWalkAnimation(float value) {
        float animationSpeed;
        if (this.getPose() == Pose.STANDING && !this.isMiniSheepSleeping() && !this.isMaehAnimating()) {
            animationSpeed = Math.min(value * 6.0F, 1.0F);
        } else {
            animationSpeed = 0.0F;
        }
        this.walkAnimation.update(animationSpeed, 0.2F);
    }

    public void setSheared(boolean sheared) {
        this.entityData.set(SHEARED, sheared);
    }

    public boolean isSheared() {
        return this.entityData.get(SHEARED);
    }

    @Override
    public void aiStep() {
        if (this.level().isClientSide) {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
            this.maehAnimationTick = Math.max(0, this.maehAnimationTick - 1);
        }
        super.aiStep();
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.24D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 10) {
            this.eatAnimationTick = 40;
        } else if (event == 11) {
            this.maehAnimationTick = MAEH_ANIMATION_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SHEEP_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.SHEEP_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SHEEP_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.SHEEP_STEP, 0.15F, 1.0F);
    }

    @Nullable
    @Override
    public MiniSheepEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.MINISHEEP.get().create(serverLevel);
    }

    @Override
    public void ate() {
        super.ate();
        this.setSheared(false);
        if (this.isBaby()) {
            this.ageUp(60);
        }
        if (!this.level().isClientSide) {
            BlockPos blockPos = this.blockPosition().below();
            BlockState blockState = this.level().getBlockState(blockPos);
            if (!blockState.isAir()) {
                this.level().levelEvent(2001, blockPos, Block.getId(blockState));
            }
        }
        this.level().broadcastEntityEvent(this, (byte) 10);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
        SpawnGroupData finalizedSpawnData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
        this.isLeader = this.random.nextFloat() < 0.3F;
        this.meadowHomePos = this.blockPosition().immutable();
        if (this.isLeader) {
            this.cachedLeader = this;
            this.herdLeaderUUID = this.getUUID();
        }
        return finalizedSpawnData;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (itemStack.getItem() instanceof ShearsItem && this.readyForShearing()) {
            this.shear(SoundSource.PLAYERS);
            itemStack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(player.getItemInHand(hand)));
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void shear(@NotNull SoundSource shearedSoundCategory) {
        this.level().playSound(null, this, SoundEvents.SHEEP_SHEAR, shearedSoundCategory, 1.0F, 1.0F);
        this.setSheared(true);
        this.level().broadcastEntityEvent(this, (byte) 11);

        if (this.level() instanceof ServerLevel serverLevel) {
            for (int particleIndex = 0; particleIndex < 28; particleIndex++) serverLevel.sendParticles(ParticleTypeRegistry.SHEARED_WOOL.get(), this.getX() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.4D, this.getY() + this.random.nextDouble() * this.getBbHeight() * 0.9D, this.getZ() + (this.random.nextDouble() - 0.5D) * this.getBbWidth() * 1.4D, 1, (this.random.nextDouble() - 0.5D) * 0.35D, this.random.nextDouble() * 0.18D, (this.random.nextDouble() - 0.5D) * 0.35D, 0.0D);
        }

        int woolCount = 2 + this.random.nextInt(3);
        for (int woolIndex = 0; woolIndex < woolCount; ++woolIndex) {
            ItemEntity itemEntity = this.spawnAtLocation(Items.WHITE_WOOL, 1);
            if (itemEntity != null) {
                itemEntity.setDeltaMovement(itemEntity.getDeltaMovement().add((this.random.nextFloat() - this.random.nextFloat()) * 0.1F, this.random.nextFloat() * 0.05F, (this.random.nextFloat() - this.random.nextFloat()) * 0.1F));
            }
        }
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean wasHurt = super.hurt(damageSource, amount);

        if (!wasHurt) {
            return false;
        }

        this.wakeUp();
        this.stopReturningHome();

        Entity attacker = damageSource.getEntity();
        if (attacker instanceof Player player && !player.isCreative() && !player.isSpectator()) {
            this.startHerdFleeFrom(player);
        }

        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        compoundTag.putBoolean("Sheared", this.isSheared());
        compoundTag.putBoolean("Sleeping", this.isMiniSheepSleeping());
        compoundTag.putBoolean("Running", this.isMiniSheepRunning());
        compoundTag.putBoolean("IsLeader", this.isLeader);
        compoundTag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        compoundTag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        compoundTag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        compoundTag.putBoolean("ReturningHome", this.returningHome);
        compoundTag.putInt("FleeTicks", this.fleeTicks);
        compoundTag.putInt("ShearCalmTicks", this.shearCalmTicks);
        if (this.herdLeaderUUID != null) {
            compoundTag.putUUID("HerdLeaderUUID", this.herdLeaderUUID);
        }
        if (this.meadowHomePos != null) {
            compoundTag.putInt("MeadowHomePosX", this.meadowHomePos.getX());
            compoundTag.putInt("MeadowHomePosY", this.meadowHomePos.getY());
            compoundTag.putInt("MeadowHomePosZ", this.meadowHomePos.getZ());
        }
        if (this.fleeTargetPos != null) {
            compoundTag.putInt("FleeTargetPosX", this.fleeTargetPos.getX());
            compoundTag.putInt("FleeTargetPosY", this.fleeTargetPos.getY());
            compoundTag.putInt("FleeTargetPosZ", this.fleeTargetPos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        super.readAdditionalSaveData(compoundTag);
        this.setSheared(compoundTag.getBoolean("Sheared"));
        this.setMiniSheepSleeping(compoundTag.getBoolean("Sleeping"));
        this.setMiniSheepRunning(compoundTag.getBoolean("Running"));
        this.isLeader = compoundTag.getBoolean("IsLeader");
        this.sleepPreparationTicks = compoundTag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = compoundTag.contains("RequiredSleepPreparationTicks") ? compoundTag.getInt("RequiredSleepPreparationTicks") : 100 + this.random.nextInt(120);
        this.sleepCooldownTicks = compoundTag.getInt("SleepCooldownTicks");
        this.returningHome = compoundTag.getBoolean("ReturningHome");
        this.fleeTicks = compoundTag.getInt("FleeTicks");
        this.shearCalmTicks = compoundTag.getInt("ShearCalmTicks");
        this.herdLeaderUUID = compoundTag.hasUUID("HerdLeaderUUID") ? compoundTag.getUUID("HerdLeaderUUID") : null;
        this.cachedLeader = null;
        this.leaderCacheCooldown = 0;
        this.herdCheckCooldown = 0;

        if (compoundTag.contains("MeadowHomePosX") && compoundTag.contains("MeadowHomePosY") && compoundTag.contains("MeadowHomePosZ")) {
            this.meadowHomePos = new BlockPos(compoundTag.getInt("MeadowHomePosX"), compoundTag.getInt("MeadowHomePosY"), compoundTag.getInt("MeadowHomePosZ"));
        } else {
            this.meadowHomePos = null;
        }

        if (compoundTag.contains("FleeTargetPosX") && compoundTag.contains("FleeTargetPosY") && compoundTag.contains("FleeTargetPosZ")) {
            this.fleeTargetPos = new BlockPos(compoundTag.getInt("FleeTargetPosX"), compoundTag.getInt("FleeTargetPosY"), compoundTag.getInt("FleeTargetPosZ"));
        } else {
            this.fleeTargetPos = null;
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }
}