package net.satisfy.wildernature.core.entity.animal.neutral;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.ai.goal.animal.SwiftFoxGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwiftFoxEntity extends Animal {
    private static final EntityDataAccessor<Boolean> DATA_SNEAKING = SynchedEntityData.defineId(SwiftFoxEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(SwiftFoxEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_AGGRESSIVE = SynchedEntityData.defineId(SwiftFoxEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int ATTACK_DURATION = 12;
    private static final int SNEAK_DURATION = 20;
    private static final int SLEEP_START_TICKS = 120;
    private static final int STEAL_COOLDOWN_MIN = 8000;
    private static final int STEAL_COOLDOWN_MAX = 12000;
    private static final int RETURN_COOLDOWN_MIN = 200;
    private static final int RETURN_COOLDOWN_MAX = 400;
    private static final int GIFT_COOLDOWN_MIN = 600;
    private static final int GIFT_COOLDOWN_MAX = 1200;
    private static final int TRUST_MIN = 0;
    private static final int TRUST_MAX = 100;

    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState sneakAnimationState = new AnimationState();
    public final AnimationState sleepAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();

    private boolean tryingToStealFromPlayer;
    private int sneakTicks;
    private int sleepTicks;
    private int attackAnimationTicks;
    private int stealCooldownTicks;
    private int returnCooldownTicks;
    private int giftCooldownTicks;
    private int trustLevel;
    private ItemStack hiddenItem = ItemStack.EMPTY;

    @Nullable
    private BlockPos hidePos;

    @Nullable
    private UUID stolenFromPlayerUuid;

    @Nullable
    private UUID trustedPlayerUuid;

    @Nullable
    private UUID returnTargetPlayerUuid;

    public SwiftFoxEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.MAX_HEALTH, 14.0D)
                .add(Attributes.ATTACK_DAMAGE, 4.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SNEAKING, false);
        builder.define(DATA_SLEEPING, false);
        builder.define(DATA_AGGRESSIVE, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.6D));
        this.goalSelector.addGoal(2, new SwiftFoxGoals.SwiftFoxReturnItemGoal(this));
        this.goalSelector.addGoal(3, new SwiftFoxGoals.SwiftFoxGiftGoal(this));
        this.goalSelector.addGoal(4, new SwiftFoxGoals.SwiftFoxHideCarriedItemGoal(this));
        this.goalSelector.addGoal(5, new SwiftFoxGoals.SwiftFoxStealPlayerItemGoal(this));
        this.goalSelector.addGoal(6, new SwiftFoxGoals.SwiftFoxStealGroundItemGoal(this));
        this.goalSelector.addGoal(7, new SwiftFoxGoals.SwiftFoxHuntSmallCritterGoal(this));
        this.goalSelector.addGoal(8, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(9, new SwiftFoxGoals.SwiftFoxSensitivePlayerReactionGoal(this));
        this.goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 0.95D));
        this.goalSelector.addGoal(11, new SwiftFoxGoals.SwiftFoxAmbientGoal(this));
        this.goalSelector.addGoal(12, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(13, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.stealCooldownTicks > 0) this.stealCooldownTicks--;
            if (this.returnCooldownTicks > 0) this.returnCooldownTicks--;
            if (this.giftCooldownTicks > 0) this.giftCooldownTicks--;

            if (this.getTarget() != null) {
                this.sneakTicks = SNEAK_DURATION;
                this.sleepTicks = 0;
            } else {
                if (this.sneakTicks > 0) this.sneakTicks--;
                if (this.getNavigation().isDone() && this.getDeltaMovement().horizontalDistanceSqr() < 0.01D && this.getMainHandItem().isEmpty() && this.hiddenItem.isEmpty()) this.sleepTicks++; else this.sleepTicks = 0;
            }

            if (this.attackAnimationTicks > 0) this.attackAnimationTicks--;

            this.setSneaking(this.sneakTicks > 0);
            this.setSleeping(this.sleepTicks > SLEEP_START_TICKS);
            this.setAggressive(this.attackAnimationTicks > 0);

            if (this.isSleeping() && this.random.nextInt(20) == 0 && this.level() instanceof ServerLevel serverLevel) serverLevel.sendParticles(ParticleTypeRegistry.SLEEPING.get(), this.getX(), this.getEyeY(), this.getZ(), 1, 0.1D, 0.05D, 0.1D, 0.0D);        }

        if (this.level().isClientSide()) {
            boolean idleActive = this.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4D && !this.isSleeping() && !this.isSneaking() && !this.isAggressive();
            this.idleAnimationState.animateWhen(idleActive, this.tickCount);
            this.attackAnimationState.animateWhen(this.isAggressive(), this.tickCount);
            this.sneakAnimationState.animateWhen(this.isSneaking(), this.tickCount);
            this.sleepAnimationState.animateWhen(this.isSleeping(), this.tickCount);
        }
    }

    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide()) {
            ItemStack carriedItem = this.getMainHandItem();
            if (!carriedItem.isEmpty()) {
                this.spawnAtLocation(carriedItem.copy());
            }

            if (!this.hiddenItem.isEmpty()) {
                this.spawnAtLocation(this.hiddenItem.copy());
                this.hiddenItem = ItemStack.EMPTY;
                this.hidePos = null;
            }
        }

        super.die(damageSource);
    }

    public boolean isSneaking() {
        return this.entityData.get(DATA_SNEAKING);
    }

    private void setSneaking(boolean sneaking) {
        this.entityData.set(DATA_SNEAKING, sneaking);
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING);
    }

    private void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    public boolean isAggressive() {
        return this.entityData.get(DATA_AGGRESSIVE);
    }

    public void setAggressive(boolean aggressive) {
        this.entityData.set(DATA_AGGRESSIVE, aggressive);
    }

    public void startSneaking() {
        this.sneakTicks = SNEAK_DURATION;
        this.setSneaking(true);
    }

    public void stopSneaking() {
        this.sneakTicks = 0;
        this.setSneaking(false);
    }

    public void resetSleep() {
        this.sleepTicks = 0;
        this.setSleeping(false);
    }

    public boolean canStartAttackAnimation() {
        return this.attackAnimationTicks <= 0;
    }

    public void triggerAttackAnimation() {
        this.attackAnimationTicks = ATTACK_DURATION;
        this.setAggressive(true);
    }

    public boolean isTryingToStealFromPlayer() {
        return this.tryingToStealFromPlayer;
    }

    public void setTryingToStealFromPlayer(boolean tryingToStealFromPlayer) {
        this.tryingToStealFromPlayer = tryingToStealFromPlayer;
    }

    public boolean canStealNow() {
        return this.stealCooldownTicks <= 0 && this.getMainHandItem().isEmpty() && this.hiddenItem.isEmpty();
    }

    public boolean canReturnNow() {
        return !this.hiddenItem.isEmpty() && this.returnTargetPlayerUuid != null;
    }

    public boolean canGiftNow() {
        return this.giftCooldownTicks <= 0 && this.trustedPlayerUuid != null && this.getMainHandItem().isEmpty() && this.hiddenItem.isEmpty();
    }

    public void resetStealCooldown() {
        this.stealCooldownTicks = STEAL_COOLDOWN_MIN + this.random.nextInt(STEAL_COOLDOWN_MAX - STEAL_COOLDOWN_MIN + 1);
    }

    public void resetReturnCooldown() {
        this.returnCooldownTicks = RETURN_COOLDOWN_MIN + this.random.nextInt(RETURN_COOLDOWN_MAX - RETURN_COOLDOWN_MIN + 1);
    }

    public void resetGiftCooldown() {
        this.giftCooldownTicks = GIFT_COOLDOWN_MIN + this.random.nextInt(GIFT_COOLDOWN_MAX - GIFT_COOLDOWN_MIN + 1);
    }

    public void addTrust(int amount) {
        this.trustLevel = Math.max(TRUST_MIN, Math.min(TRUST_MAX, this.trustLevel + amount));
    }

    public void reduceTrust(int amount) {
        this.trustLevel = Math.max(TRUST_MIN, Math.min(TRUST_MAX, this.trustLevel - amount));
    }

    public boolean shouldAvoidPlayer(Player player) {
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (this.returnTargetPlayerUuid != null && this.returnTargetPlayerUuid.equals(player.getUUID())) {
            return false;
        }
        return this.trustedPlayerUuid == null || !this.trustedPlayerUuid.equals(player.getUUID()) || this.trustLevel < 45;
    }

    public void setStolenFromPlayer(@Nullable Player player) {
        this.stolenFromPlayerUuid = player != null ? player.getUUID() : null;
        if (player != null) {
            this.trustedPlayerUuid = player.getUUID();
        }
    }

    @Nullable
    public Player getReturnTargetPlayer() {
        if (this.returnTargetPlayerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.returnTargetPlayerUuid);
    }

    @Nullable
    public Player getTrustedPlayer() {
        if (this.trustedPlayerUuid == null || !(this.level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getPlayerByUUID(this.trustedPlayerUuid);
    }

    public void startReturningHiddenItemTo(Player player) {
        this.returnTargetPlayerUuid = player.getUUID();
    }

    public void stopReturningHiddenItem() {
        this.returnTargetPlayerUuid = null;
    }

    public boolean hasHiddenItem() {
        return !this.hiddenItem.isEmpty();
    }

    public ItemStack getHiddenItem() {
        return this.hiddenItem;
    }

    public void setHiddenItem(ItemStack hiddenItem) {
        this.hiddenItem = hiddenItem;
    }

    @Nullable
    public BlockPos getHidePos() {
        return this.hidePos;
    }

    public void setHidePos(@Nullable BlockPos hidePos) {
        this.hidePos = hidePos;
    }

    public void hideCurrentItem(BlockPos hidePos) {
        this.hiddenItem = this.getMainHandItem().copy();
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.hidePos = hidePos;
    }

    @Nullable
    public BlockPos findHidePos(int radius) {
        BlockPos originPos = this.blockPosition();

        for (int searchIndex = 0; searchIndex < 20; searchIndex++) {
            BlockPos targetPos = originPos.offset(this.random.nextInt(radius * 2 + 1) - radius, 0, this.random.nextInt(radius * 2 + 1) - radius);
            if (this.level().isEmptyBlock(targetPos) && this.level().getBlockState(targetPos.below()).isSolidRender(this.level(), targetPos.below())) {
                return targetPos;
            }
        }

        return null;
    }

    public void onItemStolenFromPlayer(Player player, ItemStack stolenItem) {
        this.setItemSlot(EquipmentSlot.MAINHAND, stolenItem);
        this.setStolenFromPlayer(player);
        this.resetStealCooldown();
        this.resetReturnCooldown();
        this.triggerAttackAnimation();
    }

    public ItemStack createGiftItem() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return ItemStack.EMPTY;
        }

        LootTable lootTable = serverLevel.getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, WilderNature.identifier("gameplay/swift_fox_gifts")));
        LootParams lootParams = new LootParams.Builder(serverLevel).withParameter(LootContextParams.THIS_ENTITY, this).withParameter(LootContextParams.ORIGIN, this.position()).create(LootContextParamSets.GIFT);
        List<ItemStack> items = lootTable.getRandomItems(lootParams);
        return items.isEmpty() ? ItemStack.EMPTY : items.get(0).copy();
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        this.resetSleep();
        this.stopSneaking();
        this.triggerAttackAnimation();
        if (damageSource.getEntity() instanceof Player) {
            this.reduceTrust(10);
        }
        return super.hurt(damageSource, amount);
    }

    @Override
    protected void updateWalkAnimation(float movementSpeed) {
        float speed = this.getPose() == Pose.STANDING && !this.isSleeping() ? Math.min(movementSpeed * 6.0F, 1.0F) : 0.0F;
        this.walkAnimation.update(speed, 0.2F);
    }

    @Nullable
    @Override
    public SwiftFoxEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.SWIFT_FOX.get().create(serverLevel);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.FOX_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.FOX_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.FOX_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.3F;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(TagsRegistry.SWIFT_FOX_FOOD);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (itemStack.is(TagsRegistry.SWIFT_FOX_BRIBE)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, itemStack);
                this.addTrust(8);
                this.trustedPlayerUuid = player.getUUID();

                if (this.hasHiddenItem()) {
                    this.startReturningHiddenItemTo(player);
                }

                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(2.0F);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (this.isFood(itemStack)) {
            if (!this.level().isClientSide()) {
                this.usePlayerItem(player, hand, itemStack);
                this.addTrust(8);
                this.trustedPlayerUuid = player.getUUID();

                if (this.getHealth() < this.getMaxHealth()) {
                    this.heal(2.0F);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("TryingToStealFromPlayer", this.tryingToStealFromPlayer);
        compound.putInt("SneakTicks", this.sneakTicks);
        compound.putInt("SleepTicks", this.sleepTicks);
        compound.putInt("AttackAnimationTicks", this.attackAnimationTicks);
        compound.putInt("StealCooldownTicks", this.stealCooldownTicks);
        compound.putInt("ReturnCooldownTicks", this.returnCooldownTicks);
        compound.putInt("GiftCooldownTicks", this.giftCooldownTicks);
        compound.putInt("TrustLevel", this.trustLevel);

        if (!this.hiddenItem.isEmpty()) {
            compound.put("HiddenItem", this.hiddenItem.save(this.registryAccess()));
        }
        if (this.hidePos != null) {
            compound.putInt("HidePosX", this.hidePos.getX());
            compound.putInt("HidePosY", this.hidePos.getY());
            compound.putInt("HidePosZ", this.hidePos.getZ());
        }
        if (this.stolenFromPlayerUuid != null) {
            compound.putUUID("StolenFromPlayerUuid", this.stolenFromPlayerUuid);
        }
        if (this.trustedPlayerUuid != null) {
            compound.putUUID("TrustedPlayerUuid", this.trustedPlayerUuid);
        }
        if (this.returnTargetPlayerUuid != null) {
            compound.putUUID("ReturnTargetPlayerUuid", this.returnTargetPlayerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.tryingToStealFromPlayer = compound.getBoolean("TryingToStealFromPlayer");
        this.sneakTicks = compound.getInt("SneakTicks");
        this.sleepTicks = compound.getInt("SleepTicks");
        this.attackAnimationTicks = compound.getInt("AttackAnimationTicks");
        this.stealCooldownTicks = compound.getInt("StealCooldownTicks");
        this.returnCooldownTicks = compound.getInt("ReturnCooldownTicks");
        this.giftCooldownTicks = compound.getInt("GiftCooldownTicks");
        this.trustLevel = compound.getInt("TrustLevel");
        this.hiddenItem = compound.contains("HiddenItem") ? ItemStack.parseOptional(this.registryAccess(), compound.getCompound("HiddenItem")) : ItemStack.EMPTY;

        if (compound.contains("HidePosX") && compound.contains("HidePosY") && compound.contains("HidePosZ")) {
            this.hidePos = new BlockPos(compound.getInt("HidePosX"), compound.getInt("HidePosY"), compound.getInt("HidePosZ"));
        } else {
            this.hidePos = null;
        }

        this.stolenFromPlayerUuid = compound.hasUUID("StolenFromPlayerUuid") ? compound.getUUID("StolenFromPlayerUuid") : null;
        this.trustedPlayerUuid = compound.hasUUID("TrustedPlayerUuid") ? compound.getUUID("TrustedPlayerUuid") : null;
        this.returnTargetPlayerUuid = compound.hasUUID("ReturnTargetPlayerUuid") ? compound.getUUID("ReturnTargetPlayerUuid") : null;

        this.setSneaking(this.sneakTicks > 0);
        this.setSleeping(this.sleepTicks > SLEEP_START_TICKS);
        this.setAggressive(this.attackAnimationTicks > 0);
    }
}