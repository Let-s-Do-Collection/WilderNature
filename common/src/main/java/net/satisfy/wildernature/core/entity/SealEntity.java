package com.letsdo.wildernature.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

public class SealEntity extends Animal {

    private int fishInMouthTimer = 0;
    private boolean hasFish = false;

    public SealEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25D));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, PolarBear.class, 12.0F, 1.2D, 1.4D));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.1D, Ingredient.of(ItemTags.FISHES), false));
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0D, 20));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, AbstractFish.class, true));
    }

    @Override
    public void tick() {
        super.tick();
        if (hasFish) {
            if (--fishInMouthTimer <= 0) {
                hasFish = false;
            }
        }
    }
  
    @Override
    public boolean doHurtTarget(Entity target) {
        boolean flag = super.doHurtTarget(target);
        if (flag && target instanceof AbstractFish) {
            hasFish = true;
            fishInMouthTimer = 250; // 10 seconds at 20 ticks/sec
            target.discard(); // instantly consume fish entity
        }
        return flag;
    }

    public boolean hasFishInMouth() {
        return hasFish;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ItemTags.FISHES);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob partner) {
        return EntityTypeRegistry.SEAL.get().create(serverLevel);
    }

    @Override
    protected float getSoundVolume() {
        return 0.6F;
    }
  
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.SEAL_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.SEAL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.SEAL_DEATH.get();
    }
}
