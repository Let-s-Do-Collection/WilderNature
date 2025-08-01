package net.satisfy.wildernature.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.Villager;
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

import java.util.EnumSet;
import java.util.Objects;

@SuppressWarnings("unused")
public class RaccoonEntity extends Animal {
    private static final Ingredient FOOD_ITEMS;
    private static final EntityDataAccessor<Integer> DATA_TYPE_ID;
    private static final EntityDataAccessor<Integer> DATA_FLAGS_ID;
    private static final int FLAG_WASHING = 0b00000010;
    private static final int FLAG_RUNNING = 0x00000100;
    private static final int FLAG_OPENDOOR = 0x00001000;

    public final AnimationState walkState = new AnimationState();
    public final AnimationState runState = new AnimationState();
    public final AnimationState washingState = new AnimationState();
    public final AnimationState openDoorState = new AnimationState();

    public int openDoorAnimationTimeout = 0;

    static {
        DATA_TYPE_ID = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.INT);
        DATA_FLAGS_ID = SynchedEntityData.defineId(RaccoonEntity.class, EntityDataSerializers.INT);
        FOOD_ITEMS = Ingredient.of(Items.APPLE, Items.BEETROOT, Items.SWEET_BERRIES, Items.POTATO, Items.COOKED_COD, Items.COOKED_SALMON, Items.CARROT);
    }


    private int ticksSinceEaten;
    private int washTicks;

    public RaccoonEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
        getBoundingBox().deflate(1);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().getNodeEvaluator().setCanOpenDoors(true);
        this.getNavigation().getNodeEvaluator().setCanPassDoors(true);
    }


    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.20000001192092896).add(Attributes.MAX_HEALTH, 6.0).add(Attributes.ATTACK_DAMAGE, 1.5);
    }

    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_TYPE_ID, 0);
        builder.define(DATA_FLAGS_ID, 0);
    }

    @Override
    protected void registerGoals() {
        int i = 0;
        this.goalSelector.addGoal(++i, new FloatGoal(this));
        this.goalSelector.addGoal(++i, new PanicGoal(this, 1.4));
        this.goalSelector.addGoal(++i, new RaccoonDoorInteractGoal(this));
        this.goalSelector.addGoal(++i, new RaccoonAvoidEntityGoal<>(this, Player.class));
        this.goalSelector.addGoal(++i, new RaccoonAvoidEntityGoal<>(this, Villager.class));
        this.goalSelector.addGoal(++i, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(++i, new TemptGoal(this, 1.0, FOOD_ITEMS, false));
        this.goalSelector.addGoal(++i, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(++i, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(++i, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(++i, new RandomActionGoal(new RandomAction() {
            @Override
            public boolean isInterruptable() {
                return true;
            }

            @Override
            public void onStop() {
                stopWash();
            }

            @Override
            public void onStart() {
                startWash();
            }

            @Override
            public boolean isPossible() {
                return !RaccoonEntity.this.isRaccoonRunning();
            }

            @Override
            public int duration() {
                return 48;
            }

            @Override
            public float chance() {
                return 0.01f;
            }

            @Override
            public AttributeInstance getAttribute(Attribute movementSpeed) {
                return RaccoonEntity.this.getAttribute(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(movementSpeed));
            }

        }));

    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.openDoorState.animateWhen(isOpeningDoor(), this.tickCount);
            this.washingState.animateWhen(isWashing(), this.tickCount);
        }
    }

    private boolean isOpeningDoor() {
        return getFlag(FLAG_OPENDOOR);
    }


    public void aiStep() {

        if (!this.level().isClientSide && this.isAlive() && this.isEffectiveAi()) {
            ++this.ticksSinceEaten;
            ItemStack itemStack = this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (this.isFood(itemStack)) {
                if (this.ticksSinceEaten > 600) {
                    ItemStack itemStack2 = itemStack.finishUsingItem(this.level(), this);
                    if (!itemStack2.isEmpty()) {
                        this.setItemSlot(EquipmentSlot.MAINHAND, itemStack2);
                    }

                    this.ticksSinceEaten = 0;
                } else if (this.ticksSinceEaten > 560 && this.random.nextFloat() < 0.1F) {
                    this.playSound(this.getEatingSound(itemStack), 1.0F, 1.0F);
                    this.level().broadcastEntityEvent(this, (byte) 45);
                }
            }
        }

        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.xxa = 0.0F;
            this.zza = 0.0F;
        }

        super.aiStep();
        if (this.isDefending() && this.random.nextFloat() < 0.05F) {
            this.playSound(SoundRegistry.RACCOON_AMBIENT.get(), 1.0F, 1.0F);
        } else {
            washTicks = 0;
        }
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.RACCOON_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.RACCOON_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.RACCOON_DEATH.get();
    }

    @Nullable
    public RaccoonEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.RACCOON.get().create(serverLevel);
    }

    boolean isDefending() {
        return this.getFlag(128);
    }

    public boolean isSleeping() {
        return this.getFlag(32);
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

    @Override
    public boolean isFood(ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    public void startWash() {
        this.setFlag(FLAG_WASHING, true);
    }

    public void stopWash() {
        this.setFlag(FLAG_WASHING, false);
    }

    public boolean isWashing() {
        return getFlag(FLAG_WASHING);
    }

    public boolean isRaccoonRunning() {
        return getFlag(FLAG_RUNNING);
    }

    public void startRunningAnim() {
        this.setFlag(FLAG_RUNNING, true);
    }

    public void stopRunningAnim() {
        this.setFlag(FLAG_RUNNING, false);
    }

    public void startOpenDoorAnim() {
        setFlag(FLAG_OPENDOOR, true);
    }

    public void stopOpenDoorAnim() {
        setFlag(FLAG_OPENDOOR, false);
    }

    public static class RaccoonAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final RaccoonEntity raccoon;

        public RaccoonAvoidEntityGoal(RaccoonEntity raccoon, Class<T> tClass) {
            super(raccoon, tClass, 16.0F, 2, 2);
            this.raccoon = raccoon;
        }

        @Override
        public void start() {
            raccoon.startRunningAnim();
            super.start();
        }

        @Override
        public void stop() {
            raccoon.stopRunningAnim();
            super.stop();
        }
    }

    public static class RaccoonDoorInteractGoal extends DoorInteractGoal {

        private final RaccoonEntity raccoon;
        public static final AttributeModifier modifier = new AttributeModifier(ResourceLocation.parse("racoon_door_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);
        int counter = 0;

        public RaccoonDoorInteractGoal(RaccoonEntity raccoon) {
            super(raccoon);
            this.raccoon = raccoon;
            setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return super.canUse();
        }

        @Override
        public void start() {
            counter = 0;
            Objects.requireNonNull(raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(modifier);
            super.start();
            raccoon.startOpenDoorAnim();
        }

        @Override
        public boolean canContinueToUse() {
            return counter > 0 && counter < ServerAnimationDurations.raccoon_opening_door_length && (!isOpen() || counter >= ServerAnimationDurations.raccoon_opening_door_tick);
        }

        @Override
        public void tick() {
            raccoon.level().getBlockState(doorPos).getShape(raccoon.level().getChunk(doorPos), doorPos).bounds().getCenter();
            if (canContinueToUse()) {
                raccoon.startOpenDoorAnim();
            } else {
                raccoon.stopOpenDoorAnim();
            }
            if (counter < ServerAnimationDurations.raccoon_opening_door_length) {
                counter++;
            }
            if (counter == ServerAnimationDurations.raccoon_opening_door_tick) {
                setOpen(true);
            }
            if (counter == ServerAnimationDurations.raccoon_opening_door_length) {
                super.tick();
            }
        }

        @Override
        public void stop() {
            Objects.requireNonNull(raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(modifier);
            counter = 0;
            super.stop();
            setOpen(true);
            raccoon.stopOpenDoorAnim();
        }
    }
}