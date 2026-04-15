package net.satisfy.wildernature.core.entity.animal.defensive;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
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
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.satisfy.wildernature.core.entity.ai.goal.FollowParentAtDistanceGoal;
import net.satisfy.wildernature.core.entity.ai.goal.animal.HippoGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HippoEntity extends Animal {
    private static final int FLAG_SNAPPING = 0x00000010;
    private static final int FLAG_BITING = 0x00000100;
    private static final int FLAG_EATING = 0x00001000;

    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_THREAT_LEVEL = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_DISPLAYED_ITEM = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_TOSS_FEEDING = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int THREAT_MAX = 100;
    private static final int THREAT_ANIMATE_THRESHOLD = 60;
    private static final int THREAT_YAWN_THRESHOLD = 50;
    private static final int THREAT_BABY_MINIMUM = 40;

    private static final int THREAT_RATE_ARMED = 4;
    private static final int THREAT_RATE_SPRINTING = 3;
    private static final int THREAT_RATE_WALKING = 2;
    private static final int THREAT_RATE_SNEAKING = 1;
    private static final int THREAT_DECAY_RATE = 1;

    private static final double THREAT_DETECTION_RADIUS = 12.0D;
    private static final int THREAT_UPDATE_INTERVAL = 10;
    private static final int FEEDING_COOLDOWN_TICKS = 200;
    private static final double ALARM_RADIUS = 16.0D;

    private static final int SNAP_DURATION_TICKS = 10;
    private static final int BITE_DURATION_TICKS = 14;
    private static final int FEED_TOSS_DURATION_TICKS = 24;
    private static final int EAT_DURATION_TICKS = 40;
    private static final int FISH_CHECK_INTERVAL = 20;
    private static final int FISH_SNAP_COOLDOWN_MIN = 80;
    private static final int FISH_SNAP_COOLDOWN_RANDOM = 80;
    private static final double FISH_SNAP_RADIUS = 2.8D;
    private static final int YAWN_COOLDOWN_TICKS = 100;

    private int snapTicks;
    private int biteTicks;
    private int feedTossTicks;
    private int eatTicks;
    private int fishSnapCooldownTicks;
    private int fishCheckCooldown;
    private int threatUpdateCooldown;
    private int feedingCooldownTicks;
    private int yawnCooldownTicks;
    private int particleCooldownTicks;
    private boolean hasYawnedAtCurrentThreshold;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState swimState = new AnimationState();
    public final AnimationState biteState = new AnimationState();
    public final AnimationState snapState = new AnimationState();
    public final AnimationState threatState = new AnimationState();
    public final AnimationState yawnState = new AnimationState();
    public final AnimationState eatState = new AnimationState();

    public HippoEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 36.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55D)
                .add(Attributes.STEP_HEIGHT, 1.0D)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY, 1.2D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_THREAT_LEVEL, 0);
        builder.define(DATA_DISPLAYED_ITEM, ItemStack.EMPTY);
        builder.define(DATA_TOSS_FEEDING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.0D, Ingredient.of(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.BEEF, Items.PORKCHOP, Items.MUTTON, Items.CHICKEN, Items.RABBIT), false));
        this.goalSelector.addGoal(3, new HippoGoals.HippoAttackBoatGoal(this, 1.25D));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.25D, true) {
            @Override
            public boolean canUse() {
                return !HippoEntity.this.isBaby()
                        && !HippoEntity.this.isTossFeeding()
                        && HippoEntity.this.getThreatLevel() >= THREAT_MAX
                        && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !HippoEntity.this.isBaby()
                        && !HippoEntity.this.isTossFeeding()
                        && HippoEntity.this.getThreatLevel() >= THREAT_ANIMATE_THRESHOLD
                        && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentAtDistanceGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new HippoGoals.SeekWaterGoal(this, 1.0D, 16));
        this.goalSelector.addGoal(7, new HippoGoals.GrazingGoal(this, 0.9D));
        this.goalSelector.addGoal(8, new RandomSwimmingGoal(this, 1.0D, 10) {
            @Override
            public boolean canUse() {
                return HippoEntity.this.isInWaterOrBubble() && !HippoEntity.this.isTossFeeding() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HippoEntity.this.isInWaterOrBubble() && !HippoEntity.this.isTossFeeding() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(9, new WaterAvoidingRandomStrollGoal(this, 0.9D));
        this.goalSelector.addGoal(10, new Goal() {
            @Override
            public boolean canUse() {
                if (!HippoEntity.this.isThreatening()) return false;
                Player nearest = HippoEntity.this.level().getNearestPlayer(HippoEntity.this, THREAT_DETECTION_RADIUS);
                return nearest != null && !nearest.isCreative() && !nearest.isSpectator();
            }

            @Override
            public boolean canContinueToUse() {
                return HippoEntity.this.isThreatening() && canUse();
            }

            @Override
            public void tick() {
                Player nearest = HippoEntity.this.level().getNearestPlayer(HippoEntity.this, THREAT_DETECTION_RADIUS);
                if (nearest != null) {
                    HippoEntity.this.getLookControl().setLookAt(nearest, 30.0F, 30.0F);
                }
            }

            @Override
            public boolean requiresUpdateEveryTick() {
                return true;
            }
        });
        this.goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(12, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this) {
            @Override
            public void start() {
                HippoEntity.this.setThreatLevel(THREAT_MAX);
                HippoEntity.this.alertNearbyHippos();
                super.start();
            }
        });
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> target instanceof Player player && this.canTargetPlayer(player)) {
            @Override
            public boolean canUse() {
                return HippoEntity.this.getThreatLevel() >= THREAT_MAX && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HippoEntity.this.getThreatLevel() >= THREAT_ANIMATE_THRESHOLD && super.canContinueToUse();
            }
        });
    }

    public boolean canTargetPlayer(@Nullable Player player) {
        if (player == null || this.isBaby() || player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (this.getLastHurtByMob() == player) {
            return true;
        }
        return player.isInWaterOrBubble() && this.distanceToSqr(player) <= 100.0D;
    }

    public void alertNearbyHippos() {
        List<HippoEntity> nearby = this.level().getEntitiesOfClass(HippoEntity.class,
                this.getBoundingBox().inflate(ALARM_RADIUS),
                h -> h != this && !h.isBaby());
        for (HippoEntity hippo : nearby) {
            hippo.setThreatLevel(THREAT_MAX);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData output = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.snapTicks = 0;
        this.biteTicks = 0;
        this.feedTossTicks = 0;
        this.eatTicks = 0;
        this.fishSnapCooldownTicks = 0;
        this.fishCheckCooldown = 0;
        this.threatUpdateCooldown = 0;
        this.feedingCooldownTicks = 0;
        this.yawnCooldownTicks = 0;
        this.particleCooldownTicks = 0;
        this.hasYawnedAtCurrentThreshold = false;
        return output;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Flags", this.entityData.get(DATA_FLAGS_ID));
        tag.putInt("ThreatLevel", this.getThreatLevel());
        if (!this.getDisplayedItem().isEmpty()) {
            tag.put("DisplayedItem", this.getDisplayedItem().save(this.registryAccess()));
        }
        tag.putBoolean("TossFeeding", this.isTossFeeding());
        tag.putInt("SnapTicks", this.snapTicks);
        tag.putInt("BiteTicks", this.biteTicks);
        tag.putInt("FeedTossTicks", this.feedTossTicks);
        tag.putInt("EatTicks", this.eatTicks);
        tag.putInt("FishSnapCooldownTicks", this.fishSnapCooldownTicks);
        tag.putInt("FishCheckCooldown", this.fishCheckCooldown);
        tag.putInt("FeedingCooldownTicks", this.feedingCooldownTicks);
        tag.putInt("YawnCooldownTicks", this.yawnCooldownTicks);
        tag.putInt("ParticleCooldownTicks", this.particleCooldownTicks);
        tag.putBoolean("HasYawnedAtCurrentThreshold", this.hasYawnedAtCurrentThreshold);
        tag.putBoolean("Snapping", this.isSnapping());
        tag.putBoolean("Biting", this.isBiting());
        tag.putBoolean("Eating", this.isEating());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS_ID, tag.getInt("Flags"));
        this.setThreatLevel(tag.getInt("ThreatLevel"));
        if (tag.contains("DisplayedItem")) {
            this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.parseOptional(this.registryAccess(), tag.getCompound("DisplayedItem")));
        } else {
            this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.EMPTY);
        }
        this.entityData.set(DATA_TOSS_FEEDING, tag.getBoolean("TossFeeding"));
        this.snapTicks = tag.getInt("SnapTicks");
        this.biteTicks = tag.getInt("BiteTicks");
        this.feedTossTicks = tag.getInt("FeedTossTicks");
        this.eatTicks = tag.getInt("EatTicks");
        this.fishSnapCooldownTicks = tag.getInt("FishSnapCooldownTicks");
        this.fishCheckCooldown = tag.getInt("FishCheckCooldown");
        this.feedingCooldownTicks = tag.getInt("FeedingCooldownTicks");
        this.yawnCooldownTicks = tag.getInt("YawnCooldownTicks");
        this.particleCooldownTicks = tag.getInt("ParticleCooldownTicks");
        this.hasYawnedAtCurrentThreshold = tag.getBoolean("HasYawnedAtCurrentThreshold");
        if (tag.getBoolean("Snapping")) { this.startSnap(); this.snapTicks = 99999; }
        if (tag.getBoolean("Biting")) { this.startBite(); this.biteTicks = 99999; }
        if (tag.getBoolean("Eating")) { this.startEating(); this.eatTicks = 99999; }
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateTimers();
            this.updateThreatLevel();
            this.updateFishSnapBehavior();
        }

        this.updateAnimations();
    }

    private void updateTimers() {
        if (this.snapTicks > 0) {
            this.snapTicks--;
            if (this.snapTicks <= 0) this.stopSnapping();
        }
        if (this.biteTicks > 0) {
            this.biteTicks--;
            if (this.biteTicks <= 0) this.stopBiting();
        }
        if (this.feedTossTicks > 0) {
            this.feedTossTicks--;
            if (this.feedTossTicks <= 0) this.stopTossFeeding();
        }
        if (this.eatTicks > 0) {
            this.eatTicks--;
            if (this.eatTicks <= 0) this.stopEating();
        }
        if (this.fishSnapCooldownTicks > 0) this.fishSnapCooldownTicks--;
        if (this.fishCheckCooldown > 0) this.fishCheckCooldown--;
        if (this.threatUpdateCooldown > 0) this.threatUpdateCooldown--;
        if (this.feedingCooldownTicks > 0) this.feedingCooldownTicks--;
        if (this.yawnCooldownTicks > 0) this.yawnCooldownTicks--;
        if (this.particleCooldownTicks > 0) this.particleCooldownTicks--;
    }

    private void updateThreatLevel() {
        if (this.threatUpdateCooldown > 0) return;
        this.threatUpdateCooldown = THREAT_UPDATE_INTERVAL;

        int currentThreat = this.getThreatLevel();

        if (this.isBaby()) {
            if (currentThreat < THREAT_BABY_MINIMUM) {
                this.setThreatLevel(THREAT_BABY_MINIMUM);
            }
            return;
        }

        boolean hasNearbyBaby = !this.level().getEntitiesOfClass(HippoEntity.class,
                this.getBoundingBox().inflate(10.0D), AgeableMob::isBaby).isEmpty();

        if (hasNearbyBaby && currentThreat < THREAT_BABY_MINIMUM) {
            this.setThreatLevel(THREAT_BABY_MINIMUM);
            return;
        }

        Player nearest = this.level().getNearestPlayer(this, THREAT_DETECTION_RADIUS);

        if (nearest == null || nearest.isCreative() || nearest.isSpectator() || this.feedingCooldownTicks > 0) {
            if (currentThreat > 0) {
                int minimum = hasNearbyBaby ? THREAT_BABY_MINIMUM : 0;
                this.setThreatLevel(Math.max(minimum, currentThreat - THREAT_DECAY_RATE));
            }
            this.hasYawnedAtCurrentThreshold = false;
            return;
        }

        int increase;
        if (!nearest.getMainHandItem().isEmpty() || !nearest.getOffhandItem().isEmpty()) {
            increase = THREAT_RATE_ARMED;
        } else if (nearest.isSprinting()) {
            increase = THREAT_RATE_SPRINTING;
        } else if (nearest.isCrouching()) {
            increase = THREAT_RATE_SNEAKING;
        } else {
            increase = THREAT_RATE_WALKING;
        }

        int newThreat = Math.min(THREAT_MAX, currentThreat + increase);
        this.setThreatLevel(newThreat);

        boolean crossedAnimate = currentThreat < THREAT_ANIMATE_THRESHOLD && newThreat >= THREAT_ANIMATE_THRESHOLD;
        boolean crossedMax = currentThreat < THREAT_MAX && newThreat == THREAT_MAX;
        if (crossedAnimate || crossedMax) {
            this.spawnAlertParticle();
        }

        this.updateYawnLogic(currentThreat, newThreat);
    }

    private void updateYawnLogic(int previousThreat, int newThreat) {
        if (this.yawnCooldownTicks > 0) return;

        boolean crossedYawn = previousThreat < THREAT_YAWN_THRESHOLD && newThreat >= THREAT_YAWN_THRESHOLD;
        boolean crossedMax = previousThreat < THREAT_MAX && newThreat >= THREAT_MAX;

        if (crossedMax) {
            this.triggerYawn();
            this.hasYawnedAtCurrentThreshold = true;
        } else if (crossedYawn && !this.hasYawnedAtCurrentThreshold) {
            this.triggerYawn();
            this.hasYawnedAtCurrentThreshold = true;
        }

        if (newThreat < THREAT_YAWN_THRESHOLD) {
            this.hasYawnedAtCurrentThreshold = false;
        }
    }

    private void triggerYawn() {
        this.yawnCooldownTicks = YAWN_COOLDOWN_TICKS;
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 70);
        }
    }

    private void spawnAlertParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            double x = this.getX();
            double y = this.getY() + this.getBbHeight() + 0.5D;
            double z = this.getZ();
            serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), x, y, z, 5, 0.15D, 0.1D, 0.15D, 0.0D
            );
        }
    }

    private void updateFishSnapBehavior() {
        if (this.fishCheckCooldown > 0) return;
        this.fishCheckCooldown = FISH_CHECK_INTERVAL;

        if (!this.isInWaterOrBubble() || this.isBaby() || this.fishSnapCooldownTicks > 0 || this.isTossFeeding() || this.getTarget() != null) {
            return;
        }

        List<AbstractFish> nearbyFish = this.level().getEntitiesOfClass(AbstractFish.class,
                this.getBoundingBox().inflate(FISH_SNAP_RADIUS, 1.5D, FISH_SNAP_RADIUS));
        if (nearbyFish.isEmpty()) return;

        AbstractFish targetFish = nearbyFish.get(this.random.nextInt(nearbyFish.size()));
        this.startSnap();
        this.startTossFeeding(this.resolveFishDisplayItem(targetFish));
        targetFish.hurt(this.damageSources().mobAttack(this), 100.0F);
        this.fishSnapCooldownTicks = FISH_SNAP_COOLDOWN_MIN + this.random.nextInt(FISH_SNAP_COOLDOWN_RANDOM);
    }

    private ItemStack resolveFishDisplayItem(AbstractFish fish) {
        ItemStack pickedStack = fish.getPickResult();
        if (pickedStack != null && !pickedStack.isEmpty()) {
            ItemStack resultStack = pickedStack.copy();
            resultStack.setCount(1);
            return resultStack;
        }
        return new ItemStack(Items.COD);
    }

    private void updateAnimations() {
        this.biteState.animateWhen(this.isBiting(), this.tickCount);
        this.snapState.animateWhen(this.isSnapping(), this.tickCount);
        this.threatState.animateWhen(this.isThreatening(), this.tickCount);
        this.eatState.animateWhen(this.isEating(), this.tickCount);
        this.swimState.animateWhen(this.isInWaterOrBubble() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5D, this.tickCount);

        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 70) {
            this.yawnState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    private void setupAnimationStates() {
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean allowIdle = !isMoving && !this.isInWaterOrBubble() && !this.isBiting()
                && !this.isSnapping() && !this.isThreatening() && !this.isEating();

        if (allowIdle) {
            this.idleState.startIfStopped(this.tickCount);
        } else {
            this.idleState.stop();
        }
    }

    public int getThreatLevel() {
        return this.entityData.get(DATA_THREAT_LEVEL);
    }

    public void setThreatLevel(int level) {
        this.entityData.set(DATA_THREAT_LEVEL, Mth.clamp(level, 0, THREAT_MAX));
    }

    public boolean isThreatening() {
        return this.getThreatLevel() >= THREAT_ANIMATE_THRESHOLD;
    }

    public ItemStack getDisplayedItem() {
        return this.entityData.get(DATA_DISPLAYED_ITEM);
    }

    public boolean isTossFeeding() {
        return this.entityData.get(DATA_TOSS_FEEDING);
    }

    public float getTossFeedProgress(float partialTick) {
        if (!this.isTossFeeding()) return 0.0F;
        float progress = (FEED_TOSS_DURATION_TICKS - this.feedTossTicks + partialTick) / (float) FEED_TOSS_DURATION_TICKS;
        return Mth.clamp(progress, 0.0F, 1.0F);
    }

    public void startTossFeeding(ItemStack itemStack) {
        ItemStack renderStack = itemStack.copy();
        renderStack.setCount(1);
        this.entityData.set(DATA_DISPLAYED_ITEM, renderStack);
        this.entityData.set(DATA_TOSS_FEEDING, true);
        this.feedTossTicks = FEED_TOSS_DURATION_TICKS;
    }

    public void stopTossFeeding() {
        this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.EMPTY);
        this.entityData.set(DATA_TOSS_FEEDING, false);
    }

    private void setFlag(int flag, boolean value) {
        if (value) {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) | flag);
        } else {
            this.entityData.set(DATA_FLAGS_ID, this.entityData.get(DATA_FLAGS_ID) & ~flag);
        }
    }

    private boolean getFlag(int flag) {
        return (this.entityData.get(DATA_FLAGS_ID) & flag) != 0;
    }

    public boolean isSnapping() {
        return this.getFlag(FLAG_SNAPPING);
    }

    public void startSnap() {
        this.setFlag(FLAG_SNAPPING, true);
        this.snapTicks = SNAP_DURATION_TICKS;
    }

    public void stopSnapping() {
        this.setFlag(FLAG_SNAPPING, false);
    }

    public boolean isBiting() {
        return this.getFlag(FLAG_BITING);
    }

    public void startBite() {
        this.setFlag(FLAG_BITING, true);
        this.biteTicks = BITE_DURATION_TICKS;
    }

    public void stopBiting() {
        this.setFlag(FLAG_BITING, false);
    }

    public boolean isEating() {
        return this.getFlag(FLAG_EATING);
    }

    public void startEating() {
        this.setFlag(FLAG_EATING, true);
        this.eatTicks = EAT_DURATION_TICKS;
    }

    public void stopEating() {
        this.setFlag(FLAG_EATING, false);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.startBite();
        boolean result = super.doHurtTarget(target);
        if (result) {
            double knockbackX = target.getX() - this.getX();
            double knockbackZ = target.getZ() - this.getZ();
            double length = Math.sqrt(knockbackX * knockbackX + knockbackZ * knockbackZ);
            if (length > 0) {
                target.setDeltaMovement(
                        target.getDeltaMovement().add(
                                (knockbackX / length) * 1.8,
                                0.5,
                                (knockbackZ / length) * 1.8
                        )
                );
            }
        }
        return result;
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean wasHurt = super.hurt(damageSource, amount);
        if (wasHurt) {
            this.setThreatLevel(THREAT_MAX);
            this.alertNearbyHippos();
        }
        return wasHurt;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(TagsRegistry.HIPPO_FOOD);
    }

    private boolean isAcceptedFood(Item item) {
        return item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH
                || item == Items.BEEF || item == Items.PORKCHOP || item == Items.MUTTON
                || item == Items.CHICKEN || item == Items.RABBIT;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.isEmpty() && this.isBaby()) {
            if (!this.level().isClientSide) {
                List<HippoEntity> nearbyParents = this.level().getEntitiesOfClass(HippoEntity.class,
                        this.getBoundingBox().inflate(16.0D),
                        h -> !h.isBaby() && h != this);
                for (HippoEntity parent : nearbyParents) {
                    parent.setThreatLevel(THREAT_MAX);
                    parent.alertNearbyHippos();
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (this.isAcceptedFood(itemStack.getItem())) {
            if (!this.level().isClientSide) {
                ItemStack renderStack = itemStack.copy();
                renderStack.setCount(1);
                this.startTossFeeding(renderStack);
                this.setThreatLevel(Math.max(0, this.getThreatLevel() - 40));
                this.feedingCooldownTicks = FEEDING_COOLDOWN_TICKS;
                this.hasYawnedAtCurrentThreshold = false;
                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }

    @Nullable
    @Override
    public HippoEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.HIPPO.get().create(serverLevel);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.HIPPO_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEventRegistry.HIPPO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.HIPPO_DEATH.get();
    }
}