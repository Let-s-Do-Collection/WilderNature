package net.satisfy.wildernature.core.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

public class DeerEntity extends Animal {
    private static final int FLAG_RUNNING = 0x00000100;
    private static final int FLAG_EATING = 0x00010000;
    private static final int FLAG_LOOKING_AROUND = 0x00000010;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID;
    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID;
    private static final int CALL_COOLDOWN_TICKS = 200;
    private static final double CALL_RADIUS = 24.0;
    private static final EntityDataAccessor<Boolean> DATA_WHITE;
    private int callCooldown = 0;
    private int alarmTicks = 0;
    @Nullable private Vec3 fleeFrom = null;

    static {
        DATA_TYPE_ID = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.INT);
        DATA_FLAGS_ID = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.INT);
        DATA_WHITE = SynchedEntityData.defineId(DeerEntity.class, EntityDataSerializers.BOOLEAN);
    }


    public final AnimationState idleState = new AnimationState();
    public final AnimationState lookAroundState = new AnimationState();
    public final AnimationState eatingState = new AnimationState();
    public int globalCooldown = 0;

    public DeerEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.27000001192092896).add(Attributes.MAX_HEALTH, 10.0).add(Attributes.ATTACK_DAMAGE, 1.5);
    }

    protected SoundEvent getCallSound() {
        return SoundRegistry.DEER_HURT.get();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE_ID, 0);
        builder.define(DATA_FLAGS_ID, 0);
        builder.define(DATA_WHITE, false);
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

    @Override
    protected void registerGoals() {
        int i = 0;
        this.goalSelector.addGoal(++i, new DeerAvoidEntityGoal<>(this, Villager.class));
        this.goalSelector.addGoal(++i, new DeerAvoidEntityGoal<>(this, Pillager.class));
        this.goalSelector.addGoal(++i, new DeerAvoidEntityGoal<>(this, Player.class));
        this.goalSelector.addGoal(++i, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreedGoal(this, 1.15D));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.SHORT_GRASS), false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new DeerEatingGoal(this));
        this.goalSelector.addGoal(7, new DeerLookAroundGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
        SpawnGroupData out = super.finalizeSpawn(level, difficulty, reason, spawnData);
        if (random.nextFloat() < 0.01F) entityData.set(DATA_WHITE, true);
        return out;
    }

    public boolean isWhite() {
        return entityData.get(DATA_WHITE);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!isWhite()) return;

        Entity killer = source.getEntity();
        if (killer instanceof Projectile proj) {
            killer = proj.getOwner();
        }
        if (killer instanceof Player p) {
            p.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 72000, 0));
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 6000, 1));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.DEER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.DEER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.DEER_DEATH.get();
    }

    @Nullable
    @Override
    public DeerEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityTypeRegistry.DEER.get().create(serverLevel);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(Items.SHORT_GRASS);
    }

    public void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        boolean idleAllowed = !moving && !this.isDeerRunning() && !this.isEating() && !this.isLookingAround();

        if (idleAllowed) {
            this.idleState.startIfStopped(this.tickCount);
        } else {
            this.idleState.stop();
        }
    }

    private void setFlag(int i, boolean bl) {
        if (bl) {
            this.entityData.set(DATA_FLAGS_ID, (this.entityData.get(DATA_FLAGS_ID) | i));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (this.entityData.get(DATA_FLAGS_ID) & ~i));
        }
    }

    private boolean getFlag(int i) {
        return (this.entityData.get(DATA_FLAGS_ID) & i) != 0;
    }

    public void startEating() {
        this.setFlag(FLAG_EATING, true);
    }

    public void stopEating() {
        this.setFlag(FLAG_EATING, false);
    }

    public boolean isEating() {
        return getFlag(FLAG_EATING);
    }

    public void startLookingAround() {
        this.setFlag(FLAG_LOOKING_AROUND, true);
    }

    public void stopLookingAround() {
        this.setFlag(FLAG_LOOKING_AROUND, false);
    }

    public boolean isLookingAround() {
        return getFlag(FLAG_LOOKING_AROUND);
    }

    public boolean isDeerRunning() {
        return getFlag(FLAG_RUNNING);
    }

    public void startRunningAnim() {
        this.setFlag(FLAG_RUNNING, true);
    }

    public void stopRunningAnim() {
        this.setFlag(FLAG_RUNNING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (globalCooldown > 0) {
            globalCooldown--;
        }
        this.eatingState.animateWhen(isEating(), this.tickCount);
        this.lookAroundState.animateWhen(isLookingAround(), this.tickCount);
        if (this.level().isClientSide) {
            this.setupAnimationStates();
        }

        if (callCooldown > 0) callCooldown--;
        if (alarmTicks > 0) {
            alarmTicks--;
            if (fleeFrom != null && !level().isClientSide) {
                Vec3 dir = position().subtract(fleeFrom).normalize();
                if (dir.lengthSqr() > 0.001) {
                    Vec3 target = position().add(dir.scale(8.0));
                    getNavigation().moveTo(target.x, target.y, target.z, 1.6D);
                }
            }
            if (alarmTicks == 0) {
                stopRunningAnim();
                fleeFrom = null;
            }
        }
    }

    private void broadcastCall(boolean warning, @Nullable Entity threat) {
        if (callCooldown > 0) return;
        if (level().isClientSide) return;
        callCooldown = CALL_COOLDOWN_TICKS;
        SoundEvent snd = warning ? getHurtSound(damageSources().generic()) : getCallSound();
        level().playSound(null, getX(), getY(), getZ(), snd, getSoundSource(), 1.0F, 1.0F);
        for (DeerEntity d : level().getEntitiesOfClass(DeerEntity.class, getBoundingBox().inflate(CALL_RADIUS))) {
            if (d != this) d.onHeardCall(this, warning, threat);
        }
    }

    private void onHeardCall(DeerEntity source, boolean warning, @Nullable Entity threat) {
        if (warning) {
            triggerPanic(threat != null ? threat.position() : source.position());
        } else {
            if (!isDeerRunning() && random.nextFloat() < 0.35F && globalCooldown == 0) startLookingAround();
        }
    }

    private void triggerPanic(Vec3 threatPos) {
        startRunningAnim();
        alarmTicks = 100;
        fleeFrom = threatPos;
    }

    public static class DeerAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final DeerEntity deer;

        public DeerAvoidEntityGoal(DeerEntity deer, Class<T> tClass) {
            super(deer, tClass, 16.0F, 2, 2);
            this.deer = deer;
        }

        @Override
        public boolean canUse() {
            if (this.toAvoid instanceof Player && this.toAvoid.isCrouching()) {
                return false;
            }
            return super.canUse();
        }

        @Override
        public void start() {
            deer.startRunningAnim();
            deer.broadcastCall(true, this.toAvoid);
            super.start();
        }

        @Override
        public void stop() {
            deer.stopRunningAnim();
            super.stop();
        }
    }

    public static class DeerEatingGoal extends Goal {
        private final DeerEntity target;
        private int counter;
        private static final int EATING_DURATION_TICKS = (int) (1.8667F * 20);
        private static final int COOLDOWN_TICKS = 400;

        public DeerEatingGoal(DeerEntity mob) {
            this.target = mob;
            setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }

        @Override
        public boolean canUse() {
            var r = new Random().nextFloat();
            return r < 0.01f && !target.isDeerRunning() && target.globalCooldown == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return counter < EATING_DURATION_TICKS && !target.isDeerRunning();
        }

        @Override
        public void tick() {
            counter++;
        }

        public static final AttributeModifier modifier = new AttributeModifier(ResourceLocation.parse("deer_eat_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        @Override
        public void start() {
            counter = 0;
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(modifier);
            target.startEating();
            super.start();
        }

        @Override
        public void stop() {
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(modifier);
            target.stopEating();
            target.globalCooldown = COOLDOWN_TICKS;
            super.stop();
        }
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        Entity e = damageSource.getEntity();
        broadcastCall(true, e);
        return super.hurt(damageSource, amount);
    }

    public static class DeerLookAroundGoal extends Goal {
        private final DeerEntity target;
        private int counter;
        private static final int LOOK_AROUND_DURATION_TICKS = (int) (3.5F * 20);
        private static final int COOLDOWN_TICKS = 400;

        public DeerLookAroundGoal(DeerEntity mob) {
            this.target = mob;
            setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean isInterruptable() {
            return true;
        }

        @Override
        public boolean canUse() {
            var r = new Random().nextFloat();
            return r < 0.01f && !target.isDeerRunning() && target.globalCooldown == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return counter < LOOK_AROUND_DURATION_TICKS && !target.isDeerRunning();
        }

        @Override
        public void tick() {
            counter++;
        }

        public static final AttributeModifier modifier = new AttributeModifier(ResourceLocation.parse("deer_look_around_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        @Override
        public void start() {
            counter = 0;
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(modifier);
            target.startLookingAround();
            if (target.random.nextFloat() < 0.15F) target.broadcastCall(false, null);
            super.start();
        }

        @Override
        public void stop() {
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(modifier);
            target.stopLookingAround();
            target.globalCooldown = COOLDOWN_TICKS;
            super.stop();
        }
    }
}
