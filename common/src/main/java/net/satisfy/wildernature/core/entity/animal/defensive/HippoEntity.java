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
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
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
import net.satisfy.wildernature.core.entity.ai.goal.animal.HippoGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//TODO
public class HippoEntity extends Animal {
    private static final int FLAG_THREATENING = 0x00000001;
    private static final int FLAG_SNAPPING = 0x00000010;
    private static final int FLAG_BITING = 0x00000100;

    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<ItemStack> DATA_DISPLAYED_ITEM = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> DATA_TOSS_FEEDING = SynchedEntityData.defineId(HippoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int THREAT_DURATION_TICKS = 52;
    private static final int SNAP_DURATION_TICKS = 10;
    private static final int BITE_DURATION_TICKS = 14;
    private static final int FEED_TOSS_DURATION_TICKS = 24;
    private static final int FISH_CHECK_INTERVAL = 20;
    private static final int FISH_SNAP_COOLDOWN_MIN = 80;
    private static final int FISH_SNAP_COOLDOWN_RANDOM = 80;
    private static final double FISH_SNAP_RADIUS = 2.8D;

    private int threatTicks;
    private int snapTicks;
    private int biteTicks;
    private int feedTossTicks;
    private int fishSnapCooldownTicks;
    private int fishCheckCooldown;

    public final AnimationState idleState = new AnimationState();
    public final AnimationState swimState = new AnimationState();
    public final AnimationState biteState = new AnimationState();
    public final AnimationState snapState = new AnimationState();
    public final AnimationState threatState = new AnimationState();

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
                return !HippoEntity.this.isBaby() && !HippoEntity.this.isTossFeeding() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !HippoEntity.this.isBaby() && !HippoEntity.this.isTossFeeding() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new HippoGoals.SeekWaterGoal(this, 1.0D, 16));
        this.goalSelector.addGoal(7, new RandomSwimmingGoal(this, 1.0D, 10) {
            @Override
            public boolean canUse() {
                return HippoEntity.this.isInWaterOrBubble() && !HippoEntity.this.isTossFeeding() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return HippoEntity.this.isInWaterOrBubble() && !HippoEntity.this.isTossFeeding() && super.canContinueToUse();
            }
        });
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, target -> target instanceof Player player && this.canTargetPlayer(player)));
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

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData output = super.finalizeSpawn(level, difficulty, reason, spawnData);
        this.threatTicks = 0;
        this.snapTicks = 0;
        this.biteTicks = 0;
        this.feedTossTicks = 0;
        this.fishSnapCooldownTicks = 0;
        this.fishCheckCooldown = 0;
        return output;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Flags", this.entityData.get(DATA_FLAGS_ID));
        if (!this.getDisplayedItem().isEmpty()) {
            tag.put("DisplayedItem", this.getDisplayedItem().save(this.registryAccess()));
        }
        tag.putBoolean("TossFeeding", this.isTossFeeding());
        tag.putInt("ThreatTicks", this.threatTicks);
        tag.putInt("SnapTicks", this.snapTicks);
        tag.putInt("BiteTicks", this.biteTicks);
        tag.putInt("FeedTossTicks", this.feedTossTicks);
        tag.putInt("FishSnapCooldownTicks", this.fishSnapCooldownTicks);
        tag.putInt("FishCheckCooldown", this.fishCheckCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_FLAGS_ID, tag.getInt("Flags"));
        if (tag.contains("DisplayedItem")) {
            this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.parseOptional(this.registryAccess(), tag.getCompound("DisplayedItem")));
        } else {
            this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.EMPTY);
        }
        this.entityData.set(DATA_TOSS_FEEDING, tag.getBoolean("TossFeeding"));
        this.threatTicks = tag.getInt("ThreatTicks");
        this.snapTicks = tag.getInt("SnapTicks");
        this.biteTicks = tag.getInt("BiteTicks");
        this.feedTossTicks = tag.getInt("FeedTossTicks");
        this.fishSnapCooldownTicks = tag.getInt("FishSnapCooldownTicks");
        this.fishCheckCooldown = tag.getInt("FishCheckCooldown");
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            this.updateTimers();
            this.updateFishSnapBehavior();
        }

        this.updateAnimations();
    }

    private void updateTimers() {
        if (this.threatTicks > 0) {
            this.threatTicks--;
            if (this.threatTicks <= 0 && !this.isTossFeeding()) {
                this.stopThreatening();
            }
        }

        if (this.snapTicks > 0) {
            this.snapTicks--;
            if (this.snapTicks <= 0) {
                this.stopSnapping();
            }
        }

        if (this.biteTicks > 0) {
            this.biteTicks--;
            if (this.biteTicks <= 0) {
                this.stopBiting();
            }
        }

        if (this.feedTossTicks > 0) {
            this.feedTossTicks--;
            if (this.feedTossTicks <= 0) {
                this.stopTossFeeding();
            }
        }

        if (this.fishSnapCooldownTicks > 0) {
            this.fishSnapCooldownTicks--;
        }

        if (this.fishCheckCooldown > 0) {
            this.fishCheckCooldown--;
        }
    }

    private void updateFishSnapBehavior() {
        if (this.fishCheckCooldown > 0) {
            return;
        }

        this.fishCheckCooldown = FISH_CHECK_INTERVAL;

        if (!this.isInWaterOrBubble() || this.isBaby() || this.fishSnapCooldownTicks > 0 || this.isThreatening() || this.isTossFeeding() || this.getTarget() != null) {
            return;
        }

        List<AbstractFish> nearbyFish = this.level().getEntitiesOfClass(AbstractFish.class, this.getBoundingBox().inflate(FISH_SNAP_RADIUS, 1.5D, FISH_SNAP_RADIUS));
        if (nearbyFish.isEmpty()) {
            return;
        }

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
        this.swimState.animateWhen(this.isInWaterOrBubble() && this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-5D, this.tickCount);

        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
        boolean allowIdle = !isMoving && !this.isInWaterOrBubble() && !this.isBiting() && !this.isSnapping() && !this.isThreatening();

        if (allowIdle) {
            this.idleState.startIfStopped(this.tickCount);
        } else {
            this.idleState.stop();
        }
    }

    public ItemStack getDisplayedItem() {
        return this.entityData.get(DATA_DISPLAYED_ITEM);
    }

    public boolean isTossFeeding() {
        return this.entityData.get(DATA_TOSS_FEEDING);
    }

    public float getTossFeedProgress(float partialTick) {
        if (!this.isTossFeeding()) {
            return 0.0F;
        }
        float progress = (FEED_TOSS_DURATION_TICKS - this.feedTossTicks + partialTick) / (float) FEED_TOSS_DURATION_TICKS;
        return Mth.clamp(progress, 0.0F, 1.0F);
    }

    public void startTossFeeding(ItemStack itemStack) {
        ItemStack renderStack = itemStack.copy();
        renderStack.setCount(1);
        this.entityData.set(DATA_DISPLAYED_ITEM, renderStack);
        this.entityData.set(DATA_TOSS_FEEDING, true);
        this.feedTossTicks = FEED_TOSS_DURATION_TICKS;
        this.startThreat();
    }

    public void stopTossFeeding() {
        this.entityData.set(DATA_DISPLAYED_ITEM, ItemStack.EMPTY);
        this.entityData.set(DATA_TOSS_FEEDING, false);
        if (this.threatTicks <= 0) {
            this.stopThreatening();
        }
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

    public boolean isThreatening() {
        return this.getFlag(FLAG_THREATENING);
    }

    public void startThreat() {
        this.setFlag(FLAG_THREATENING, true);
        this.threatTicks = THREAT_DURATION_TICKS;
    }

    public void stopThreatening() {
        this.setFlag(FLAG_THREATENING, false);
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

    @Override
    public boolean doHurtTarget(Entity target) {
        this.startBite();
        this.startThreat();
        return super.doHurtTarget(target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        boolean wasHurt = super.hurt(damageSource, amount);
        if (wasHurt && this.getLastHurtByMob() != null) {
            this.startThreat();
        }
        return wasHurt;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return this.isAcceptedFood(itemStack.getItem());
    }

    private boolean isAcceptedFood(Item item) {
        return item == Items.COD || item == Items.SALMON || item == Items.TROPICAL_FISH || item == Items.BEEF || item == Items.PORKCHOP || item == Items.MUTTON || item == Items.CHICKEN || item == Items.RABBIT;
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (this.isAcceptedFood(itemStack.getItem())) {
            if (!this.level().isClientSide) {
                ItemStack renderStack = itemStack.copy();
                renderStack.setCount(1);
                this.startTossFeeding(renderStack);
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