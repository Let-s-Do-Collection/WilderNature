package net.satisfy.wildernature.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.entity.ai.RandomAction;
import net.satisfy.wildernature.entity.ai.RandomActionGoal;
import net.satisfy.wildernature.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HedgehogEntity extends Animal {
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    public AnimationState sniffAnimationState = new AnimationState();

    private static final EntityDataAccessor<Boolean> SNIFFING = SynchedEntityData.defineId(HedgehogEntity.class, EntityDataSerializers.BOOLEAN);

    public HedgehogEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.13000000417232513).add(Attributes.MAX_HEALTH, 4.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.2D, Ingredient.of(Items.ROTTEN_FLESH), true));
        this.goalSelector.addGoal(3, new FollowParentGoal(this, 1.1D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
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
                setSniffing(true);
            }

            @Override
            public void onStop() {
                setSniffing(false);
            }

            @Override
            public boolean isPossible() {
                return true;
            }

            @Override
            public int duration() {
                return (int) (ServerAnimationDurations.hedgehog_sniffing * 20);
            }

            @Override
            public float chance() {
                return 0.005f;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeed) {
                return HedgehogEntity.this.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(movementSpeed));
            }
        }));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(0.5D, 0.5D, 0.5D));
            for (Entity entity : entities) {
                if (entity instanceof Player player) {
                    if (player.fallDistance > 1.0F && player.getY() > this.getY() + 1.0) {
                        this.level().addParticle(ParticleTypes.POOF, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
                        this.kill();
                        player.hurt(this.level().damageSources().generic(), 3.0F);
                        break;
                    }
                }
            }
        }
        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        sniffAnimationState.animateWhen(this.isSniffing(), tickCount);

        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
    }

    private boolean isSniffing() {
        return this.entityData.get(SNIFFING);
    }

    public void setSniffing(boolean sniffing) {
        this.entityData.set(SNIFFING, sniffing);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.HEDGEHOG.get().create(serverLevel);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(SNIFFING, false);
    }

    @Override
    protected void updateWalkAnimation(float pPartialTick) {
        super.updateWalkAnimation(pPartialTick);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.HEDGEHOG_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.HEDGEHOG_DEATH.get();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.HEDGEHOG_AMBIENT.get();
    }

    protected float getSoundVolume() {
        return 0.1F;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.ROTTEN_FLESH);
    }

    @Override
    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(Items.ROTTEN_FLESH)) {
            if (this.isFood(itemstack) && this.getAge() == 0 && !this.level().isClientSide) {
                this.usePlayerItem(player, hand, itemstack);
                this.setInLove(player);
                return InteractionResult.SUCCESS;
            }
        } else if (itemstack.is(Items.MILK_BUCKET)) {
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
                player.addItem(new ItemStack(Items.BUCKET));
            }
            this.addEffect(new MobEffectInstance(MobEffects.POISON, 240, 1));
            return InteractionResult.SUCCESS;
        }

        return super.mobInteract(player, hand);
    }
}
