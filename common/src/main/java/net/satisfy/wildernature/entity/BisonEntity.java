package net.satisfy.wildernature.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.entity.ai.EntityWithAttackAnimation;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Random;

public class BisonEntity extends Animal implements EntityWithAttackAnimation {
    private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROLLING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> LAST_HURT_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.LONG);
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public AnimationState rollingAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public BisonEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public static @NotNull AttributeSupplier.Builder createMobAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.ATTACK_DAMAGE, 1.5F)
                .add(Attributes.ATTACK_SPEED, 1.25)
                .add(Attributes.MOVEMENT_SPEED, 0.1800009838F)
                .add(Attributes.ARMOR_TOUGHNESS, 0.0177774783F)
                .add(Attributes.ATTACK_KNOCKBACK, 2F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.isAngry() && this.level().getGameTime() - this.getLastHurtTime() > 300) {
            this.setAngry(false);
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        attackAnimationState.animateWhen(this.isAttacking(), tickCount);
        rollingAnimationState.animateWhen(this.isRolling(), tickCount);
    }

    private boolean isRolling() {
        return this.entityData.get(ROLLING);
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

    public boolean isAttacking() {
        return this.entityData.get(ATTACKING);
    }

    @Override
    public LivingEntity getTarget_() {
        return getTarget();
    }

    @Override
    public double getMeleeAttackRangeSqr_(LivingEntity target) {
        return target.distanceTo(target);
    }

    public void setAttacking_(boolean attacking) {
        this.entityData.set(ATTACKING, attacking);
    }

    @Override
    public Vec3 getPosition_(int i) {
        return super.getPosition(i);
    }

    @Override
    public void doHurtTarget_(LivingEntity targetEntity) {
        super.doHurtTarget(targetEntity);
    }

    public boolean isAngry() {
        return this.entityData.get(ANGRY);
    }

    public void setAngry(boolean angry) {
        this.entityData.set(ANGRY, angry);
    }

    public long getLastHurtTime() {
        return this.entityData.get(LAST_HURT_TIME);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACKING, false);
        builder.define(ROLLING, false);
        builder.define(ANGER_TIME, 0);
        builder.define(ANGRY, false);
        builder.define(LAST_HURT_TIME, 0L);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AnimationAttackGoal(this, 1.0D, true, (int) (ServerAnimationDurations.bison_attack * 20), 5));
        this.goalSelector.addGoal(1, new BisonPanicGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.SHORT_GRASS), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(9, new MeleeAttackGoal(this, 1.4D, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new BisonRollingGoal(this));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return EntityRegistry.BISON.get().create(pLevel);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isAngry() ? SoundRegistry.BISON_ANGRY.get() : SoundRegistry.BISON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.BISON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.BISON_DEATH.get();
    }

    @Override
    public int getMaxHeadYRot() {
        return 35;
    }

    public void setRolling(boolean rolling) {
        this.entityData.set(ROLLING, rolling);
    }

    static class BisonPanicGoal extends PanicGoal {
        public BisonPanicGoal(BisonEntity bison) {
            super(bison, 1.2D);
        }

        @Override
        public boolean canUse() {
            return this.mob.isBaby() && super.canUse();
        }
    }

    public static class BisonRollingGoal extends Goal {
        private final BisonEntity target;
        int counter;

        public BisonRollingGoal(BisonEntity mob) {
            this.target = mob;
            setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }


        @Override
        public boolean canUse() {
            var r = new Random().nextFloat();
            return r < 0.001f && !target.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return counter > 0 && counter < ServerAnimationDurations.bison_roll * 20;
        }

        @Override
        public void tick() {
            counter++;
        }

        public static final AttributeModifier modifier = new AttributeModifier(ResourceLocation.parse("bison_roll_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        @Override
        public void start() {
            counter = 0;
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(modifier);
            target.setRolling(true);
            super.start();
        }

        @Override
        public void stop() {
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(modifier);
            target.setRolling(false);
            super.stop();
        }
    }
}
