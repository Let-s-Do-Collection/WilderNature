package net.satisfy.wildernature.core.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.ai.BoarGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoarEntity extends Animal {

    private static final Ingredient FOOD_ITEMS = Ingredient.of(Items.BEEF, Items.CHICKEN, Items.BEETROOT, Items.SWEET_BERRIES, Items.POTATO, Items.COOKED_COD, Items.COOKED_SALMON, Items.CARROT);
    private static final ResourceKey<LootTable> ROOTING_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, WilderNature.identifier("gameplay/boar_rooting"));

    private static final EntityDataAccessor<Boolean> SLEEPING = SynchedEntityData.defineId(BoarEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ROOTING_ANIMATION_TICKS = 40;
    private static final int ROOTING_COOLDOWN_TICKS = 200;
    private static final int PLAYER_ROOTING_SEARCH_RADIUS = 10;
    private static final int DENY_ANIMATION_TICKS = 16;
    private static final int SLEEPING_PARTICLE_INTERVAL_TICKS = 14;
    private static final double WAKE_UP_RADIUS = 6.0D;

    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState diggingAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();

    private int digAnimationTick;
    private int rootingCooldownTicks;
    private int denyAnimationTick;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;

    @Nullable
    private BlockPos requestedRootingTarget;
    private boolean forceRooting;

    public BoarEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
        this.requiredSleepPreparationTicks = 80 + this.random.nextInt(80);
        this.sleepPreparationTicks = 0;
        this.sleepCooldownTicks = 0;
        this.denyAnimationTick = 0;
        this.digAnimationTick = 0;
        this.rootingCooldownTicks = 0;
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.2D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        int goalPriority = 0;
        this.goalSelector.addGoal(++goalPriority, new FloatGoal(this));
        this.goalSelector.addGoal(++goalPriority, new PanicGoal(this, 1.25D));

        this.goalSelector.addGoal(++goalPriority, new BreedGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new TemptGoal(this, 1.2D, Ingredient.of(Items.CARROT_ON_A_STICK), false) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new TemptGoal(this, 1.2D, FOOD_ITEMS, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new FollowParentGoal(this, 1.1D) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new BoarGoals.BoarRootingGoal(this));

        this.goalSelector.addGoal(++goalPriority, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new LookAtPlayerGoal(this, Player.class, 6.0F) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
        });

        this.goalSelector.addGoal(++goalPriority, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BoarEntity.this.isDigging() && !BoarEntity.this.isSleeping();
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (this.rootingCooldownTicks > 0) {
            this.rootingCooldownTicks--;
        }
        if (this.digAnimationTick > 0) {
            this.digAnimationTick--;
        }
        if (this.denyAnimationTick > 0) {
            this.denyAnimationTick--;
        }

        if (!this.level().isClientSide) {
            this.updateSleep();
        }
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        boolean isNight = !this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.01D;
        boolean isOnValidGround = this.level().getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)
                || this.level().getBlockState(this.blockPosition().below()).is(Blocks.DIRT)
                || this.level().getBlockState(this.blockPosition().below()).is(Blocks.COARSE_DIRT);

        if (isNight && !this.isDigging() && isStill && this.sleepCooldownTicks <= 0 && isOnValidGround) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks && !this.isSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 15);
            }
        }

        if (this.isSleeping()) {
            if (this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
            }

            if (this.level() instanceof ServerLevel serverLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(),
                        this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D,
                        this.getY() + this.getBbHeight() * 0.75D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D,
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean idleAllowed = !moving && !this.isDigging() && !this.isSleeping();

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        if (this.isDigging()) {
            this.diggingAnimationState.startIfStopped(this.tickCount);
        } else {
            this.diggingAnimationState.stop();
        }

        this.sleepAnimationState.animateWhen(this.isSleeping(), this.tickCount);
    }

    @Override
    protected void updateWalkAnimation(float partialTick) {
        float animationSpeed;
        if (this.getPose() == Pose.STANDING && !this.isDigging() && !this.isSleeping()) {
            animationSpeed = Math.min(partialTick * 6.0F, 1.0F);
        } else {
            animationSpeed = 0.0F;
        }
        this.walkAnimation.update(animationSpeed, 0.2F);
    }

    public boolean isDigging() {
        return this.digAnimationTick > 0;
    }

    public boolean isSleeping() {
        return this.entityData.get(SLEEPING);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(SLEEPING, sleeping);
    }

    private void startSleeping() {
        this.setSleeping(true);
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void wakeUp() {
        if (this.isSleeping()) {
            this.setSleeping(false);
            this.sleepCooldownTicks = 200 + this.random.nextInt(200);
        }
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 80 + this.random.nextInt(80);
    }

    private boolean hasWakeUpTriggerNearby() {
        Player player = this.level().getNearestPlayer(this, WAKE_UP_RADIUS);
        if (player == null) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        return !player.isCrouching();
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(ObjectRegistry.TRUFFLE.get())) {
            if (!this.level().isClientSide) {
                boolean startedRooting = this.tryStartRootingFromPlayer();
                if (startedRooting) {
                    if (!player.getAbilities().instabuild) {
                        itemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
                this.triggerDenyFeedback();
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    public boolean tryStartRootingFromPlayer() {
        if (this.isBaby()) return false;
        if (this.isDigging()) return false;
        if (this.isSleeping()) return false;
        if (this.getRootingCooldownTicks() > 0) return false;

        BlockPos bestPosition = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos origin = this.blockPosition();

        for (int xOffset = -PLAYER_ROOTING_SEARCH_RADIUS; xOffset <= PLAYER_ROOTING_SEARCH_RADIUS; xOffset++) {
            for (int zOffset = -PLAYER_ROOTING_SEARCH_RADIUS; zOffset <= PLAYER_ROOTING_SEARCH_RADIUS; zOffset++) {
                BlockPos blockPosition = origin.offset(xOffset, 0, zOffset);
                if (!this.level().getBlockState(blockPosition).is(Blocks.GRASS_BLOCK)) continue;

                BlockPos standPosition = blockPosition.above();
                if (!this.level().isEmptyBlock(standPosition) || !this.level().isEmptyBlock(standPosition.above())) continue;

                double distanceToPosition = this.distanceToSqr(Vec3.atBottomCenterOf(standPosition));
                if (distanceToPosition < bestDistance) {
                    bestDistance = distanceToPosition;
                    bestPosition = standPosition.immutable();
                }
            }
        }

        if (bestPosition == null) return false;

        this.wakeUp();
        this.requestedRootingTarget = bestPosition;
        this.forceRooting = true;
        this.getNavigation().moveTo(bestPosition.getX() + 0.5D, bestPosition.getY(), bestPosition.getZ() + 0.5D, 1.25D);
        return true;
    }

    @Nullable
    public BlockPos consumeRequestedRootingTarget() {
        BlockPos targetPosition = this.requestedRootingTarget;
        this.requestedRootingTarget = null;
        return targetPosition;
    }

    public boolean consumeForceRooting() {
        boolean shouldForceRooting = this.forceRooting;
        this.forceRooting = false;
        return shouldForceRooting;
    }

    public void clearRequestedRootingTarget() {
        this.requestedRootingTarget = null;
        this.forceRooting = false;
    }

    @Nullable
    public BlockPos getRequestedRootingTarget() {
        return this.requestedRootingTarget;
    }

    public int getRootingCooldownTicks() {
        return this.rootingCooldownTicks;
    }

    public int getDenyAnimationTick() {
        return this.denyAnimationTick;
    }

    public void spawnAlertParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), this.getX(), this.getY() + this.getBbHeight() + 0.3D, this.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public void triggerDenyFeedback() {
        this.denyAnimationTick = DENY_ANIMATION_TICKS;
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypeRegistry.DENY.get(), this.getX(), this.getY() + this.getBbHeight() + 0.35D, this.getZ(), 4, 0.18D, 0.08D, 0.18D, 0.0D);
        }
    }

    public void startRootingAnimation() {
        this.wakeUp();
        this.digAnimationTick = ROOTING_ANIMATION_TICKS;
        this.rootingCooldownTicks = ROOTING_COOLDOWN_TICKS;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
        this.level().broadcastEntityEvent(this, (byte) 10);
    }

    public void finishRooting(BlockPos blockPos) {
        if (!(this.level() instanceof ServerLevel serverLevel)) return;

        if (!this.level().getBlockState(blockPos).is(Blocks.GRASS_BLOCK)) return;

        this.level().levelEvent(2001, blockPos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GRASS_BLOCK.defaultBlockState()),
                blockPos.getX() + 0.5D, blockPos.getY() + 1.0D, blockPos.getZ() + 0.5D, 10, 0.25D, 0.25D, 0.25D, 0.5D);

        if (this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
            this.level().setBlock(blockPos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
            this.spawnRootingLoot(serverLevel, blockPos);
        }

        this.ate();
    }

    private void spawnRootingLoot(ServerLevel serverLevel, BlockPos blockPos) {
        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(ROOTING_LOOT_TABLE);
        LootParams lootParams = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(blockPos))
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .create(LootContextParamSets.GIFT);

        List<ItemStack> generatedLoot = lootTable.getRandomItems(lootParams);
        for (ItemStack itemStack : generatedLoot) {
            Block.popResource(serverLevel, blockPos.above(), itemStack);
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.digAnimationTick = ROOTING_ANIMATION_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Digging", this.isDigging());
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putInt("DigAnimationTick", this.digAnimationTick);
        tag.putInt("RootingCooldownTicks", this.rootingCooldownTicks);
        tag.putInt("DenyAnimationTick", this.denyAnimationTick);
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        tag.putBoolean("ForceRooting", this.forceRooting);

        if (this.requestedRootingTarget != null) {
            tag.putInt("RequestedRootingTargetX", this.requestedRootingTarget.getX());
            tag.putInt("RequestedRootingTargetY", this.requestedRootingTarget.getY());
            tag.putInt("RequestedRootingTargetZ", this.requestedRootingTarget.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.digAnimationTick = tag.getInt("DigAnimationTick");
        this.setSleeping(tag.getBoolean("Sleeping"));
        this.rootingCooldownTicks = tag.getInt("RootingCooldownTicks");
        this.denyAnimationTick = tag.getInt("DenyAnimationTick");
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks")
                ? tag.getInt("RequiredSleepPreparationTicks")
                : 80 + this.random.nextInt(80);
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.forceRooting = tag.getBoolean("ForceRooting");

        if (tag.contains("RequestedRootingTargetX") && tag.contains("RequestedRootingTargetY") && tag.contains("RequestedRootingTargetZ")) {
            this.requestedRootingTarget = new BlockPos(
                    tag.getInt("RequestedRootingTargetX"),
                    tag.getInt("RequestedRootingTargetY"),
                    tag.getInt("RequestedRootingTargetZ")
            );
        } else {
            this.requestedRootingTarget = null;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.BOAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        this.wakeUp();
        return SoundRegistry.BOAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.BOAR_DEATH.get();
    }

    @Override
    public BoarEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.BOAR.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }
}