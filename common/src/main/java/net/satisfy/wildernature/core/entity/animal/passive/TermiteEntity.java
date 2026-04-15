package net.satisfy.wildernature.core.entity.animal.passive;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.entity.ai.goal.animal.TermiteGoals;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TermiteEntity extends Monster {
    public static final double MOUND_ALERT_RADIUS = 8.0D;
    public static final double MAX_MOUND_DISTANCE = 16.0D;
    public static final double LOG_SEARCH_RANGE = 12.0D;
    public static final int EAT_DURATION_TICKS = 60;
    private static final int MOUND_DISTANCE_CHECK_INTERVAL = 20;

    private static final EntityDataAccessor<Boolean> DATA_EATING = SynchedEntityData.defineId(TermiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING_TO_MOUND = SynchedEntityData.defineId(TermiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_HAS_WOODMEAL = SynchedEntityData.defineId(TermiteEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_MOUND_POS = SynchedEntityData.defineId(TermiteEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public final AnimationState idleState = new AnimationState();
    public final AnimationState walkState = new AnimationState();
    public final AnimationState eatState = new AnimationState();

    private int moundDistanceCheckCooldown = MOUND_DISTANCE_CHECK_INTERVAL;

    public TermiteEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 4.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.ATTACK_DAMAGE, 1.0D)
                .add(Attributes.FOLLOW_RANGE, 16.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_EATING, false);
        builder.define(DATA_RETURNING_TO_MOUND, false);
        builder.define(DATA_HAS_WOODMEAL, false);
        builder.define(DATA_MOUND_POS, Optional.empty());
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(2, new TermiteGoals.ReturnWoodmealToStorageGoal(this));
        this.goalSelector.addGoal(3, new TermiteGoals.FetchWoodmealFromInfestedLogGoal(this));
        this.goalSelector.addGoal(4, new TermiteGoals.ReturnToMoundGoal(this));
        this.goalSelector.addGoal(5, new TermiteGoals.TermiteEatLogGoal(this));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new TermiteGoals.TermiteMoundDefenseGoal(this));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            this.tickMoundDistanceCheck();
        }

        if (this.level().isClientSide()) {
            boolean isMoving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4D;
            boolean isEating = this.isEating();
            this.idleState.animateWhen(!isMoving && !isEating, this.tickCount);
            this.walkState.animateWhen(isMoving && !isEating, this.tickCount);
            this.eatState.animateWhen(isEating, this.tickCount);
        }
    }

    private void tickMoundDistanceCheck() {
        if (this.moundDistanceCheckCooldown > 0) {
            this.moundDistanceCheckCooldown--;
            return;
        }

        this.moundDistanceCheckCooldown = MOUND_DISTANCE_CHECK_INTERVAL;

        if (this.getTarget() != null || this.isEating() || this.isReturningToMound() || this.hasWoodmeal()) {
            return;
        }

        if (this.getMoundPos() == null) {
            return;
        }

        double moundCenterX = this.getMoundPos().getX() + 0.5D;
        double moundCenterY = this.getMoundPos().getY() + 0.5D;
        double moundCenterZ = this.getMoundPos().getZ() + 0.5D;
        if (this.distanceToSqr(moundCenterX, moundCenterY, moundCenterZ) > MAX_MOUND_DISTANCE * MAX_MOUND_DISTANCE) {
            this.setReturningToMound(true);
        }
    }

    public boolean isEating() {
        return this.entityData.get(DATA_EATING);
    }

    public void setEating(boolean eating) {
        this.entityData.set(DATA_EATING, eating);
    }

    public boolean isReturningToMound() {
        return this.entityData.get(DATA_RETURNING_TO_MOUND);
    }

    public void setReturningToMound(boolean returningToMound) {
        this.entityData.set(DATA_RETURNING_TO_MOUND, returningToMound);
    }

    public boolean hasWoodmeal() {
        return this.entityData.get(DATA_HAS_WOODMEAL);
    }

    public void setHasWoodmeal(boolean hasWoodmeal) {
        this.entityData.set(DATA_HAS_WOODMEAL, hasWoodmeal);
    }

    @Nullable
    public BlockPos getMoundPos() {
        return this.entityData.get(DATA_MOUND_POS).orElse(null);
    }

    public void setMoundPos(@Nullable BlockPos pos) {
        this.entityData.set(DATA_MOUND_POS, Optional.ofNullable(pos));
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEventRegistry.TERMITE_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEventRegistry.TERMITE_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEventRegistry.TERMITE_DEATH.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.65F;
    }

    @Override
    public float getVoicePitch() {
        return 0.6F + this.random.nextFloat() * 0.1F;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        if (recentlyHit && this.hasWoodmeal()) {
            this.spawnAtLocation(new ItemStack(ObjectRegistry.WOODMEAL.get()));
            this.setHasWoodmeal(false);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("ReturningToMound", this.isReturningToMound());
        tag.putBoolean("HasWoodmeal", this.hasWoodmeal());
        if (this.getMoundPos() != null) {
            tag.putInt("MoundX", this.getMoundPos().getX());
            tag.putInt("MoundY", this.getMoundPos().getY());
            tag.putInt("MoundZ", this.getMoundPos().getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setReturningToMound(tag.getBoolean("ReturningToMound"));
        this.setHasWoodmeal(tag.getBoolean("HasWoodmeal"));

        if (tag.contains("MoundX")) {
            this.setMoundPos(new BlockPos(tag.getInt("MoundX"), tag.getInt("MoundY"), tag.getInt("MoundZ")));
        }

        if (this.hasWoodmeal()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ObjectRegistry.WOODMEAL.get()));
        } else {
            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}