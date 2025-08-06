package net.satisfy.wildernature.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Random;

public class BoarEntity extends Animal {
    private static final Ingredient FOOD_ITEMS;

    static {
        FOOD_ITEMS = Ingredient.of(Items.BEEF, Items.CHICKEN, Items.BEETROOT, Items.SWEET_BERRIES, Items.POTATO, Items.COOKED_COD, Items.COOKED_SALMON, Items.CARROT);
    }

    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;
    private int digAnimationTick;
    private DigIntoGrassGoal digintoBlockGoal;
    private boolean isDigging = false;

    public BoarEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 12.0).add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.ATTACK_DAMAGE, 2.0D);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            setupAnimationStates();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
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

    protected void registerGoals() {
        this.digintoBlockGoal = new DigIntoGrassGoal(this);
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, Ingredient.of(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, FOOD_ITEMS, false));
        this.goalSelector.addGoal(5, this.digintoBlockGoal);
        this.goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    protected void customServerAiStep() {
        this.digAnimationTick = this.digintoBlockGoal.getEatAnimationTick();
        super.customServerAiStep();
    }

    public boolean isDigging() {
        return isDigging;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 10) {
            this.isDigging = true;
            this.digAnimationTick = 40;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            if (this.digAnimationTick > 0) {
                --this.digAnimationTick;
                if (this.digAnimationTick == 0) {
                    this.isDigging = false;
                }
            }
        }
    }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions entityDimensions) {
        return this.isBaby() ? entityDimensions.height * 0.2F : entityDimensions.height * 0.3F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.BOAR_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.BOAR_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.BOAR_DEATH.get();
    }

    @Nullable
    public BoarEntity getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityRegistry.BOAR.get().create(serverLevel);
    }

    @Override
    public boolean isFood(@NotNull ItemStack itemStack) {
        return FOOD_ITEMS.test(itemStack);
    }

    public class DigIntoGrassGoal extends Goal {
        private static final int EAT_ANIMATION_TICKS = 40;
        private static final BlockStatePredicate IS_GRASS_BLOCK = BlockStatePredicate.forBlock(Blocks.GRASS_BLOCK);
        private final Mob mob;
        private final Level level;
        private int eatAnimationTick;

        public DigIntoGrassGoal(Mob mob) {
            this.mob = mob;
            this.level = mob.level();
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        public boolean canUse() {
            if (this.mob.getRandom().nextInt(this.mob.isBaby() ? 50 : 1000) != 0) {
                return false;
            } else {
                BlockPos blockPos = this.mob.blockPosition();
                return IS_GRASS_BLOCK.test(this.level.getBlockState(blockPos.below()));
            }
        }

        public void start() {
            this.eatAnimationTick = this.adjustedTickDelay(EAT_ANIMATION_TICKS);
            this.level.broadcastEntityEvent(this.mob, (byte) 10);
            this.mob.getNavigation().stop();
        }

        public void stop() {
            this.eatAnimationTick = 0;
        }

        public boolean canContinueToUse() {
            return this.eatAnimationTick > 0;
        }

        public int getEatAnimationTick() {
            return this.eatAnimationTick;
        }

        public void tick() {
            this.eatAnimationTick = Math.max(0, this.eatAnimationTick - 1);
            if (this.eatAnimationTick == this.adjustedTickDelay(4)) {
                BlockPos blockPos = this.mob.blockPosition().below();
                if (IS_GRASS_BLOCK.test(this.level.getBlockState(blockPos))) {
                    if (this.level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                        this.level.levelEvent(2001, blockPos, Block.getId(Blocks.GRASS_BLOCK.defaultBlockState()));
                        this.level.setBlock(blockPos, Blocks.COARSE_DIRT.defaultBlockState(), 2);
                        spawnEatParticlesAndDrops((ServerLevel) this.level, blockPos);
                    }
                    this.mob.ate();
                }
            }
        }

        private void spawnEatParticlesAndDrops(ServerLevel serverLevel, BlockPos blockPos) {
            BlockState blockState = Blocks.GRASS_BLOCK.defaultBlockState();
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                    blockPos.getX() + 0.5, blockPos.getY() + 1.0, blockPos.getZ() + 0.5,
                    10, 0.25, 0.25, 0.25, 0.5);

            Random random = new Random();
            int chance = random.nextInt(100);
            if (chance < 10) {
                spawnItem(serverLevel, blockPos, new ItemStack(Items.BROWN_MUSHROOM));
            } else if (chance < 15) {
                spawnItem(serverLevel, blockPos, new ItemStack(Items.RED_MUSHROOM));
            } else if (chance < 30) {
                spawnItem(serverLevel, blockPos, new ItemStack(Items.HANGING_ROOTS));
            }
        }

        private void spawnItem(ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack) {
            Block.popResource(serverLevel, blockPos, itemStack);
        }
    }
}
