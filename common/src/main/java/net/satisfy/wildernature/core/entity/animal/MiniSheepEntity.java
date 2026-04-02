package net.satisfy.wildernature.core.entity.animal;

import java.util.List;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.entity.ai.MiniSheepGoal;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
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
    private static final int WAKE_UP_RADIUS = 8;
    private static final int DEFEND_RADIUS = 18;
    private static final double HERD_SEARCH_RADIUS = 12.0D;


    private int eatAnimationTick;
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

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState eatAnimationState = new AnimationState();
    public final AnimationState runAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();

    public MiniSheepEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    @Override
    protected void registerGoals() {
        this.eatBlockGoal = new EatBlockGoal(this);
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MiniSheepGoal.MiniSheepMeleeAttackGoal(this));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(Items.WHEAT), false) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(6, new MiniSheepGoal.MiniSheepFollowLeaderGoal(this));
        this.goalSelector.addGoal(7, new MiniSheepGoal.MiniSheepReturnHomeGoal(this));
        this.goalSelector.addGoal(8, this.eatBlockGoal);
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && MiniSheepEntity.this.canUseMeadowStrollGoal() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && MiniSheepEntity.this.canUseMeadowStrollGoal() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !MiniSheepEntity.this.isMiniSheepSleeping() && MiniSheepEntity.this.getTarget() == null && super.canContinueToUse();
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

        this.eatAnimationState.animateWhen(eating && !sleeping && !running, this.tickCount);
        this.runAnimationState.animateWhen(running, this.tickCount);
        this.sleepAnimationState.animateWhen(sleeping, this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.updateHerd();
            this.tryPromoteToLeader();
            this.updateSleep();
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

        List<MiniSheepEntity> nearbySheep = this.level().getEntitiesOfClass(
                MiniSheepEntity.class,
                this.getBoundingBox().inflate(HERD_SEARCH_RADIUS)
        );

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

        if (this.getTarget() != null) {
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
                serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + this.getBbHeight() * 0.75D, this.getZ(), 1, 0.15D, 0.05D, 0.15D, 0.0D);
            }

            this.getNavigation().stop();
            this.setDeltaMovement(0.0D, this.getDeltaMovement().y, 0.0D);
        }
    }

    private void updateRunningState() {
        boolean running = this.getTarget() != null || this.returningHome;
        this.setMiniSheepRunning(running);
    }

    private boolean hasWakeUpTriggerNearby() {
        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            return true;
        }

        Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (player == null) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        return !player.isCrouching();
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
        if (this.getPose() == Pose.STANDING && !this.isMiniSheepSleeping()) {
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
        }
        super.aiStep();
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.21D)
                .add(Attributes.ATTACK_DAMAGE, 2.5D)
                .add(Attributes.ATTACK_KNOCKBACK, 0.6D);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 10) {
            this.eatAnimationTick = 40;
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
        int woolCount = 2 + this.random.nextInt(3);
        for (int woolIndex = 0; woolIndex < woolCount; ++woolIndex) {
            ItemEntity itemEntity = this.spawnAtLocation(Items.WHITE_WOOL, 1);
            if (itemEntity != null) {
                itemEntity.setDeltaMovement(
                        itemEntity.getDeltaMovement().add(
                                (this.random.nextFloat() - this.random.nextFloat()) * 0.1F,
                                this.random.nextFloat() * 0.05F,
                                (this.random.nextFloat() - this.random.nextFloat()) * 0.1F
                        )
                );
            }
        }
    }

    @Override
    public boolean readyForShearing() {
        return this.isAlive() && !this.isSheared() && !this.isBaby();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity directEntity = damageSource.getEntity();
        boolean wasHurt = super.hurt(damageSource, amount);

        if (!wasHurt) {
            return false;
        }

        this.wakeUp();

        if (directEntity instanceof LivingEntity livingEntity) {
            this.setTarget(livingEntity);
            this.startReturningHomeIfNeededAfterDefense();
            this.alertHerd(livingEntity);
        }

        return true;
    }

    private void alertHerd(LivingEntity attacker) {
        List<MiniSheepEntity> nearbySheep = this.level().getEntitiesOfClass(MiniSheepEntity.class, this.getBoundingBox().inflate(DEFEND_RADIUS));
        for (MiniSheepEntity nearbySheepEntity : nearbySheep) {
            if (nearbySheepEntity == this) {
                continue;
            }
            if (nearbySheepEntity.isMiniSheepSleeping()) {
                nearbySheepEntity.wakeUp();
            }
            nearbySheepEntity.setTarget(attacker);
            nearbySheepEntity.stopReturningHome();
        }
    }

    private void startReturningHomeIfNeededAfterDefense() {
        if (this.shouldReturnHome()) {
            this.startReturningHome();
            this.setTarget(null);
        } else {
            this.stopReturningHome();
        }
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
        if (this.herdLeaderUUID != null) {
            compoundTag.putUUID("HerdLeaderUUID", this.herdLeaderUUID);
        }
        if (this.meadowHomePos != null) {
            compoundTag.putInt("MeadowHomePosX", this.meadowHomePos.getX());
            compoundTag.putInt("MeadowHomePosY", this.meadowHomePos.getY());
            compoundTag.putInt("MeadowHomePosZ", this.meadowHomePos.getZ());
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
        this.herdLeaderUUID = compoundTag.hasUUID("HerdLeaderUUID") ? compoundTag.getUUID("HerdLeaderUUID") : null;
        this.cachedLeader = null;
        this.leaderCacheCooldown = 0;
        this.herdCheckCooldown = 0;

        if (compoundTag.contains("MeadowHomePosX") && compoundTag.contains("MeadowHomePosY") && compoundTag.contains("MeadowHomePosZ")) {
            this.meadowHomePos = new BlockPos(compoundTag.getInt("MeadowHomePosX"), compoundTag.getInt("MeadowHomePosY"), compoundTag.getInt("MeadowHomePosZ"));
        } else {
            this.meadowHomePos = null;
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.WHEAT);
    }
}