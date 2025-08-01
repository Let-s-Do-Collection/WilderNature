package net.satisfy.wildernature.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.entity.ai.RandomAction;
import net.satisfy.wildernature.entity.ai.RandomActionGoal;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlamingoEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public AnimationState standAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> STANDING = SynchedEntityData.defineId(FlamingoEntity.class, EntityDataSerializers.BOOLEAN);

    public FlamingoEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.23000000417232513).add(Attributes.MAX_HEALTH, 10.0);
    }

    @Nullable
    public FlamingoEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.FLAMINGO.get().create(serverLevel);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(ItemTags.FISHES), false));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 3f));
        this.goalSelector.addGoal(6, new PanicGoal(this, 2.0D));

        this.goalSelector.addGoal(7, new RandomActionGoal(new RandomAction() {
            @Override
            public boolean isInterruptable() {
                return false;
            }

            @Override
            public void onStart() {
                setStanding(true);
            }

            @Override
            public void onStop() {
                setStanding(false);
            }

            @Override
            public boolean isPossible() {
                return true;
            }

            @Override
            public int duration() {
                return (int) (ServerAnimationDurations.pelican_stand * 20);
            }

            @Override
            public float chance() {
                return 0.005f;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeed) {
                return FlamingoEntity.this.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(movementSpeed));
            }
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        standAnimationState.animateWhen(this.isStanding(), tickCount);

        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    private boolean isStanding() {
        return this.entityData.get(STANDING);
    }

    public void setStanding(boolean standing) {
        this.entityData.set(STANDING, standing);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STANDING, false);
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
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.FLAMINGO_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.FLAMINGO_DEATH.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.FLAMINGO_AMBIENT.get();
    }


    protected float getSoundVolume() {
        return 0.3F;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }
}
