package net.satisfy.wildernature.core.entity.animal.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.goal.animal.BeaverGoals;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BeaverEntity extends Animal {
    public static final int INVENTORY_SIZE = 64;
    public static final int WORK_SEARCH_RANGE = 6;
    private static final int NOD_DURATION_TICKS = 16;
    private static final int SLEEPING_PARTICLE_INTERVAL_TICKS = 14;

    private static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(BeaverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_GNAWING = SynchedEntityData.defineId(BeaverEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_NODDING = SynchedEntityData.defineId(BeaverEntity.class, EntityDataSerializers.BOOLEAN);

    public final AnimationState walkState = new AnimationState();
    public final AnimationState gnawState = new AnimationState();
    public final AnimationState idleState = new AnimationState();
    public final AnimationState nodState = new AnimationState();
    public final AnimationState sleepState = new AnimationState();

    private NonNullList<ItemStack> beaverInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int nodTicks;
    private int damCooldownTicks;
    private int sleepPreparationTicks;
    private int requiredSleepPreparationTicks;
    private int sleepCooldownTicks;

    public BeaverEntity(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanFloat(true);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.22D).add(Attributes.MAX_HEALTH, 10.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_GNAWING, false);
        builder.define(DATA_NODDING, false);
        builder.define(DATA_SLEEPING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BeaverGoals.BeaverPlaceDamGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new net.satisfy.wildernature.core.entity.ai.goal.FollowParentAtDistanceGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(Items.STICK), false));
        this.goalSelector.addGoal(5, new BeaverGoals.BeaverGnawLogGoal(this, 1.1D));
        this.goalSelector.addGoal(6, new BeaverGoals.BeaverPreferWaterGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BeaverEntity.this.isSleeping();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BeaverEntity.this.isSleeping();
            }
        });
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this) {
            @Override
            public boolean canUse() {
                return super.canUse() && !BeaverEntity.this.isSleeping();
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && !BeaverEntity.this.isSleeping();
            }
        });
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.updateNodding();
            if (this.damCooldownTicks > 0) {
                this.damCooldownTicks--;
            }
            this.updateSleep();
        }

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            boolean isSleeping = this.isSleeping();

            this.walkState.animateWhen(isMoving && !this.isGnawing() && !isSleeping, this.tickCount);
            this.gnawState.animateWhen(this.isGnawing() && !isSleeping, this.tickCount);
            this.idleState.animateWhen(!isMoving && !this.isGnawing() && !isSleeping, this.tickCount);
            this.nodState.animateWhen(this.isNodding() && !isSleeping, this.tickCount);
            this.sleepState.animateWhen(isSleeping, this.tickCount);
        }
    }

    private void updateSleep() {
        if (this.sleepCooldownTicks > 0) {
            this.sleepCooldownTicks--;
        }

        boolean isNight = !this.level().isDay();
        boolean isStill = this.getDeltaMovement().horizontalDistanceSqr() < 0.002D;
        boolean isDry = !this.isInWaterOrRain();

        if (isNight && isDry && isStill && !this.isGnawing() && this.sleepCooldownTicks <= 0) {
            this.sleepPreparationTicks++;
            if (this.sleepPreparationTicks > this.requiredSleepPreparationTicks && !this.isSleeping()) {
                this.startSleeping();
            }
        } else {
            if (this.isSleeping()) {
                this.wakeUp();
            } else if (this.sleepPreparationTicks > 0) {
                this.sleepPreparationTicks = Math.max(0, this.sleepPreparationTicks - 25);
            }
        }

        if (this.isSleeping()) {
            if (this.hasWakeUpTriggerNearby()) {
                this.wakeUp();
            }

            if (this.level() instanceof ServerLevel && this.tickCount % SLEEPING_PARTICLE_INTERVAL_TICKS == 0) {
                this.spawnSleepingParticle();
            }

            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    private void startSleeping() {
        this.setSleeping(true);
        this.sleepPreparationTicks = 0;
        this.getNavigation().stop();
        this.setDeltaMovement(Vec3.ZERO);
    }

    private void wakeUp() {
        if (this.isSleeping()) {
            this.setSleeping(false);
            this.sleepCooldownTicks = 200 + this.random.nextInt(200);
        }
        this.sleepPreparationTicks = 0;
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
    }

    private boolean hasWakeUpTriggerNearby() {
        Player player = this.level().getNearestPlayer(this, 6.0D);
        if (player == null) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        return !player.isCrouching();
    }

    private void spawnSleepingParticle() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(net.satisfy.wildernature.core.registry.ParticleTypeRegistry.SLEEPING.get(), this.getX() + (this.random.nextDouble() - 0.5D) * 0.35D, this.getY() + this.getBbHeight() * 0.7D, this.getZ() + (this.random.nextDouble() - 0.5D) * 0.35D, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public boolean isSleeping() {
        return this.entityData.get(DATA_SLEEPING);
    }

    public void setSleeping(boolean sleeping) {
        this.entityData.set(DATA_SLEEPING, sleeping);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.OFF_HAND) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);

        if (heldStack.is(ItemTags.LOGS)) {
            int freeSlots = this.countFreeInventorySlots();
            if (freeSlots <= 0) {
                if (!this.level().isClientSide()) {
                    this.playSound(SoundEventRegistry.BEAVER_TRADE.get(), 0.6F, 1.2F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            int transferCount = Math.min(heldStack.getCount(), freeSlots);
            if (!this.level().isClientSide()) {
                this.storeLogsInInventory(heldStack.copyWithCount(transferCount));

                if (!player.isCreative()) {
                    heldStack.shrink(transferCount);
                }

                this.startNodding();
                this.playSound(SoundEvents.FOX_EAT, 0.6F, 1.1F);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        if (heldStack.is(TagsRegistry.BEAVER_PAYMENT)) {
            int strippedLogCount = this.countStrippedLogs();
            if (strippedLogCount <= 0) {
                if (!this.level().isClientSide()) {
                    this.playSound(SoundEventRegistry.BEAVER_TRADE.get(), 0.6F, 1.2F);
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide());
            }

            if (!this.level().isClientSide()) {
                this.dropAllStrippedLogs();

                if (!player.isCreative()) {
                    heldStack.shrink(1);
                }

                this.startNodding();
                this.playSound(SoundEventRegistry.BEAVER_TRADE.get(), 0.8F, 1.0F);
            }

            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(player, hand);
    }

    private void updateNodding() {
        if (this.nodTicks > 0) {
            this.nodTicks--;
            if (this.nodTicks <= 0) {
                this.setNodding(false);
            }
        }
    }

    public void startNodding() {
        this.nodTicks = NOD_DURATION_TICKS;
        this.setNodding(true);
    }

    public boolean isNodding() {
        return this.entityData.get(DATA_NODDING);
    }

    public void setNodding(boolean nodding) {
        this.entityData.set(DATA_NODDING, nodding);
    }

    public int getDamCooldownTicks() {
        return this.damCooldownTicks;
    }

    public void setDamCooldownTicks(int damCooldownTicks) {
        this.damCooldownTicks = damCooldownTicks;
    }

    public void storeLogsInInventory(ItemStack stack) {
        int remaining = stack.getCount();
        ItemStack singleItem = stack.copyWithCount(1);

        for (int inventorySlot = 0; inventorySlot < this.beaverInventory.size() && remaining > 0; inventorySlot++) {
            if (this.beaverInventory.get(inventorySlot).isEmpty()) {
                this.beaverInventory.set(inventorySlot, singleItem.copy());
                remaining--;
            }
        }
    }

    public int countFreeInventorySlots() {
        int freeSlots = 0;
        for (ItemStack inventoryStack : this.beaverInventory) {
            if (inventoryStack.isEmpty()) {
                freeSlots++;
            }
        }
        return freeSlots;
    }

    @Nullable
    public static BlockState getStrippedState(BlockState state) {
        Block strippedBlock = AxeItem.STRIPPABLES.get(state.getBlock());
        if (strippedBlock == null) {
            return null;
        }

        BlockState strippedState = strippedBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS) && strippedState.hasProperty(RotatedPillarBlock.AXIS)) {
            strippedState = strippedState.setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
        }
        return strippedState;
    }

    public static ItemStack stripBlockAndGetItem(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState strippedState = getStrippedState(state);
        if (strippedState == null) {
            return ItemStack.EMPTY;
        }

        level.setBlock(pos, strippedState, Block.UPDATE_ALL);
        level.levelEvent(2001, pos, Block.getId(state));

        return new ItemStack(strippedState.getBlock());
    }

    public boolean isGnawing() {
        return this.entityData.get(DATA_GNAWING);
    }

    public void setGnawing(boolean gnawing) {
        this.entityData.set(DATA_GNAWING, gnawing);
    }

    private boolean isRawLog(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!stack.is(ItemTags.LOGS)) {
            return false;
        }

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        return AxeItem.STRIPPABLES.containsKey(blockItem.getBlock());
    }

    private boolean isStrippedLog(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return false;
        }

        return AxeItem.STRIPPABLES.containsValue(blockItem.getBlock()) && !AxeItem.STRIPPABLES.containsKey(blockItem.getBlock());
    }

    public boolean hasRawLogs() {
        for (ItemStack inventoryStack : this.beaverInventory) {
            if (this.isRawLog(inventoryStack)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack takeOneRawLog() {
        for (int inventorySlot = 0; inventorySlot < this.beaverInventory.size(); inventorySlot++) {
            ItemStack inventoryStack = this.beaverInventory.get(inventorySlot);
            if (this.isRawLog(inventoryStack)) {
                this.beaverInventory.set(inventorySlot, ItemStack.EMPTY);
                return inventoryStack;
            }
        }
        return ItemStack.EMPTY;
    }

    public boolean storeOneItem(ItemStack itemStack) {
        for (int inventorySlot = 0; inventorySlot < this.beaverInventory.size(); inventorySlot++) {
            if (this.beaverInventory.get(inventorySlot).isEmpty()) {
                this.beaverInventory.set(inventorySlot, itemStack.copyWithCount(1));
                return true;
            }
        }
        return false;
    }

    public int countStrippedLogs() {
        int strippedLogCount = 0;

        for (ItemStack inventoryStack : this.beaverInventory) {
            if (this.isStrippedLog(inventoryStack)) {
                strippedLogCount += inventoryStack.getCount();
            }
        }

        return strippedLogCount;
    }

    public void dropAllStrippedLogs() {
        for (int inventorySlot = 0; inventorySlot < this.beaverInventory.size(); inventorySlot++) {
            ItemStack inventoryStack = this.beaverInventory.get(inventorySlot);
            if (this.isStrippedLog(inventoryStack)) {
                this.spawnAtLocation(inventoryStack.copy());
                this.beaverInventory.set(inventorySlot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.BEAVER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEventRegistry.BEAVER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.BEAVER_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("NodTicks", this.nodTicks);
        tag.putBoolean("Nodding", this.isNodding());
        tag.putInt("DamCooldownTicks", this.damCooldownTicks);
        tag.putBoolean("Sleeping", this.isSleeping());
        tag.putInt("SleepPreparationTicks", this.sleepPreparationTicks);
        tag.putInt("RequiredSleepPreparationTicks", this.requiredSleepPreparationTicks);
        tag.putInt("SleepCooldownTicks", this.sleepCooldownTicks);
        ContainerHelper.saveAllItems(tag, this.beaverInventory, this.registryAccess());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.nodTicks = tag.getInt("NodTicks");
        if (tag.contains("Nodding")) {
            this.setNodding(tag.getBoolean("Nodding"));
        }
        this.damCooldownTicks = tag.getInt("DamCooldownTicks");
        if (tag.contains("Sleeping")) {
            this.setSleeping(tag.getBoolean("Sleeping"));
        }
        this.sleepPreparationTicks = tag.getInt("SleepPreparationTicks");
        this.requiredSleepPreparationTicks = tag.contains("RequiredSleepPreparationTicks") ? tag.getInt("RequiredSleepPreparationTicks") : 100 + this.random.nextInt(120);
        this.sleepCooldownTicks = tag.getInt("SleepCooldownTicks");
        this.beaverInventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.beaverInventory, this.registryAccess());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.STICK);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return EntityTypeRegistry.BEAVER.get().create(level);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData) {
        this.damCooldownTicks = 24000 + this.random.nextInt(24001);
        this.requiredSleepPreparationTicks = 100 + this.random.nextInt(120);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData);
    }
}