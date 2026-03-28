package net.satisfy.wildernature.core.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.ai.AnimationAttackGoal;
import net.satisfy.wildernature.core.entity.ai.EntityWithAttackAnimation;
import net.satisfy.wildernature.core.entity.animation.ServerAnimationDurations;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

public class BisonEntity extends Animal implements EntityWithAttackAnimation {
    private static final EntityDataAccessor<Integer> ANGER_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ATTACKING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ROLLING = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ANGRY = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Long> LAST_HURT_TIME = SynchedEntityData.defineId(BisonEntity.class, EntityDataSerializers.LONG);
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public AnimationState rollingAnimationState = new AnimationState();

    public BisonEntity(EntityType<? extends Animal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Items.SHORT_GRASS);
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
        if (!this.level().isClientSide()) {
            var dv = this.getDeltaMovement();
            if (this.isSprinting() && dv.horizontalDistanceSqr() > 0.003) {
                var server = (ServerLevel) this.level();
                var state = this.level().getBlockState(this.getOnPos());
                double len = Math.sqrt(dv.x * dv.x + dv.z * dv.z);
                if (len > 1.0E-4) {
                    double nx = dv.x / len;
                    double nz = dv.z / len;
                    double bx = this.getX() - nx * 0.6;
                    double by = this.getY() + 0.1;
                    double bz = this.getZ() - nz * 0.6;
                    server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), bx, by, bz, 40, this.getBbWidth() * 0.4, 0.05, this.getBbWidth() * 0.4, 0.12);
                    server.sendParticles(ParticleTypes.CLOUD, bx, by + 0.05, bz, 12, 0.25, 0.01, 0.25, 0.01);
                }
            }
        }
    }

    private void setupAnimationStates() {
        boolean moving = this.getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
        boolean idleAllowed = !moving && !this.isAttacking() && !this.isRolling();

        if (idleAllowed) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        } else {
            this.idleAnimationState.stop();
        }

        this.attackAnimationState.animateWhen(this.isAttacking(), this.tickCount);
        this.rollingAnimationState.animateWhen(this.isRolling(), this.tickCount);
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
        return this.distanceToSqr(target);
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
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Wolf.class, 12.0F, 1.35, 1.6));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Creeper.class, 10.0F, 1.35, 1.6));
        this.goalSelector.addGoal(2, new BisonHerdRunGoal(this));
        this.goalSelector.addGoal(2, new BisonRollingGoal(this));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, Ingredient.of(Items.SHORT_GRASS), false));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new BisonHerdAwareStrollGoal(this, 1.0, 20, 16.0F));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(8, new LeapAtTargetGoal(this, 0.3F));
        this.goalSelector.addGoal(9, new MeleeAttackGoal(this, 1.4D, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel pLevel, AgeableMob pOtherParent) {
        return EntityTypeRegistry.BISON.get().create(pLevel);
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
        private final List<BlockPos> toConvert = new ArrayList<>();
        private boolean conversionDone;

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
            var r = target.getRandom().nextFloat();
            return r < 0.001f && !target.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return counter > 0 && counter < ServerAnimationDurations.bison_roll * 20;
        }

        @Override
        public void tick() {
            counter++;
            if (!target.level().isClientSide()) {
                if (!conversionDone && counter == 1) {
                    for (var p : toConvert) {
                        var s = target.level().getBlockState(p);
                        if (s.is(Blocks.GRASS_BLOCK)) {
                            target.level().setBlock(p, Blocks.DIRT.defaultBlockState(), 3);
                        } else if (s.is(Blocks.DIRT)) {
                            target.level().setBlock(p, Blocks.COARSE_DIRT.defaultBlockState(), 3);
                        }
                    }
                    conversionDone = true;
                }
                var server = (ServerLevel) target.level();
                var state = target.level().getBlockState(target.getOnPos());
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), target.getX(), target.getY() + 0.2, target.getZ(), 40, target.getBbWidth() * 0.6, 0.1, target.getBbWidth() * 0.6, 0.15);
            }
        }

        public static final AttributeModifier modifier = new AttributeModifier(ResourceLocation.parse("bison_roll_do_not_move"), -1000, AttributeModifier.Operation.ADD_VALUE);

        @Override
        public void start() {
            counter = 0;
            conversionDone = false;
            toConvert.clear();
            var level = target.level();
            if (!level.isClientSide()) {
                var center = target.getOnPos();
                int r = Mth.ceil(target.getBbWidth() * 0.5F) + 1;
                var rand = target.getRandom();
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (rand.nextFloat() < 0.33333334F) {
                            var p = center.offset(dx, 0, dz);
                            var s = level.getBlockState(p);
                            if (s.is(Blocks.GRASS_BLOCK) || s.is(Blocks.DIRT)) {
                                toConvert.add(p.immutable());
                            }
                        }
                    }
                }
            }
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(modifier);
            target.setRolling(true);
            if (!target.level().isClientSide()) {
                var server = (ServerLevel) target.level();
                var state = target.level().getBlockState(target.getOnPos());
                server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), target.getX(), target.getY() + 0.1, target.getZ(), 80, target.getBbWidth(), 0.2, target.getBbWidth(), 0.2);
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.3, target.getZ(), 24, 0.6, 0.03, 0.6, 0.02);
            }
            super.start();
        }

        @Override
        public void stop() {
            Objects.requireNonNull(target.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(modifier);
            target.setRolling(false);
            if (!target.level().isClientSide()) {
                var server = (ServerLevel) target.level();
                server.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY() + 0.4, target.getZ(), 18, 0.25, 0.02, 0.25, 0.02);
            }
            super.stop();
        }
    }

    public static class BisonHerdRunGoal extends Goal {
        private static final class Wave {
            final Vec3 pos;
            final Vec3 dir;
            final long start;
            final long until;
            final double radius;
            Wave(Vec3 pos, Vec3 dir, long start, long until, double radius) { this.pos = pos; this.dir = dir; this.start = start; this.until = until; this.radius = radius; }
        }
        private static final Map<ResourceKey<Level>, List<Wave>> WAVES = new HashMap<>();

        private final BisonEntity target;
        private int duration;
        private int recalc;
        private int scanCooldown;
        private Vec3 heading;

        public BisonHerdRunGoal(BisonEntity mob) {
            this.target = mob;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (target.getTarget() != null) return false;
            if (target.isBaby()) return false;
            if (target.entityData.get(ROLLING)) return false;
            if (!target.level().getBiome(target.blockPosition()).is(BiomeTags.IS_SAVANNA)) return false;
            if (consumeWave()) return true;
            if (scanCooldown > 0) { scanCooldown--; return false; }
            scanCooldown = 40 + target.getRandom().nextInt(40);
            boolean randomTrigger = target.getRandom().nextFloat() < 0.04F;
            AABB box = target.getBoundingBox().inflate(12.0, 4.0, 12.0);
            var herd = target.level().getEntitiesOfClass(BisonEntity.class, box, e -> e != target && !e.isAngry() && !e.entityData.get(ROLLING));
            boolean neighborFast = false;
            for (var b : herd) {
                if (b.getDeltaMovement().horizontalDistanceSqr() > 0.03) { neighborFast = true; break; }
            }
            if (!(neighborFast || randomTrigger) && herd.size() < 3) return false;
            Vec3 v = Vec3.ZERO;
            for (var b : herd) {
                var dv = b.getDeltaMovement();
                v = v.add(dv.x, 0, dv.z);
            }
            if (v.lengthSqr() < 1.0E-3) {
                float a = target.getRandom().nextFloat() * Mth.TWO_PI;
                v = new Vec3(Mth.cos(a), 0, Mth.sin(a));
            } else {
                v = v.normalize();
            }
            long now = target.level().getGameTime();
            addWave(target.level().dimension(), new Wave(target.position(), v, now + 2, now + 140, 12.0));
            return false;
        }

        private boolean consumeWave() {
            var key = target.level().dimension();
            var list = WAVES.get(key);
            if (list == null) return false;
            long now = target.level().getGameTime();
            Vec3 bestDir = null;
            double bestD2 = Double.MAX_VALUE;
            var it = list.iterator();
            while (it.hasNext()) {
                var w = it.next();
                if (w.until < now) { it.remove(); continue; }
                double d2 = w.pos.distanceToSqr(target.position());
                if (d2 <= w.radius * w.radius) {
                    if (now >= w.start && d2 < bestD2) {
                        bestD2 = d2;
                        bestDir = w.dir;
                    }
                }
            }
            if (bestDir != null) {
                heading = bestDir;
                return true;
            }
            return false;
        }

        private static void addWave(ResourceKey<Level> key, Wave w) {
            var list = WAVES.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(w);
        }

        @Override
        public void start() {
            duration = 80 + target.getRandom().nextInt(60);
            recalc = 0;
            target.setSprinting(true);
        }

        @Override
        public boolean canContinueToUse() {
            if (target.getTarget() != null) return false;
            if (target.entityData.get(ROLLING)) return false;
            return duration > 0 && target.level().getBiome(target.blockPosition()).is(BiomeTags.IS_SAVANNA);
        }

        @Override
        public void tick() {
            duration--;
            recalc--;
            if (recalc <= 0) {
                recalc = 10;
                var ahead = target.position().add(heading.scale(12.0));
                target.getNavigation().moveTo(ahead.x, ahead.y, ahead.z, 1.6);
            }
        }

        @Override
        public void stop() {
            target.setSprinting(false);
            target.getNavigation().stop();
        }
    }

    public static class BisonHerdAwareStrollGoal extends Goal {
        private final BisonEntity mob;
        private final double speed;
        private final int interval;
        private final float herdRadius;
        private Vec3 wanted;

        public BisonHerdAwareStrollGoal(BisonEntity mob, double speed, int interval, float herdRadius) {
            this.mob = mob;
            this.speed = speed;
            this.interval = interval;
            this.herdRadius = herdRadius;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (mob.getTarget() != null) return false;
            if (mob.entityData.get(ROLLING)) return false;
            if (mob.isAngry()) return false;
            if (!mob.getNavigation().isDone()) return false;
            if (mob.getRandom().nextInt(interval) != 0) return false;
            var box = mob.getBoundingBox().inflate(herdRadius, 4.0, herdRadius);
            var herd = mob.level().getEntitiesOfClass(BisonEntity.class, box, e -> e != mob && !e.isAngry() && !e.entityData.get(ROLLING));
            Vec3 dir;
            if (herd.size() >= 2) {
                double cx = 0.0;
                double cz = 0.0;
                for (var b : herd) { cx += b.getX(); cz += b.getZ(); }
                cx /= herd.size();
                cz /= herd.size();
                double dx = cx - mob.getX();
                double dz = cz - mob.getZ();
                double d2 = dx * dx + dz * dz;
                if (d2 > 100.0) {
                    double inv = Math.sqrt(d2);
                    dir = new Vec3(dx / inv, 0.0, dz / inv);
                } else {
                    float a = mob.getRandom().nextFloat() * Mth.TWO_PI;
                    var randDir = new Vec3(Mth.cos(a), 0.0, Mth.sin(a));
                    double inv = Math.sqrt(d2);
                    var toCenter = inv > 1.0E-4 ? new Vec3(dx / inv, 0.0, dz / inv) : randDir;
                    dir = randDir.scale(0.4).add(toCenter.scale(0.6)).normalize();
                }
            } else {
                float a = mob.getRandom().nextFloat() * Mth.TWO_PI;
                dir = new Vec3(Mth.cos(a), 0.0, Mth.sin(a));
            }
            double dist = 6.0 + mob.getRandom().nextInt(7);
            wanted = mob.position().add(dir.scale(dist));
            return true;
        }

        @Override
        public void start() {
            mob.getNavigation().moveTo(wanted.x, wanted.y, wanted.z, speed);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }
}
