package net.satisfy.wildernature.core.entity;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Blocks;
import net.satisfy.wildernature.core.event.TermiteEvents;
import org.jetbrains.annotations.NotNull;

public class TermiteEntity extends Animal {
    private static final Map<Long, Long> STRIP_COOLDOWN = new HashMap<>();
    private BlockPos homePos;
    private int outsideTicks;
    private boolean returning;
    private BlockPos forcedTarget;
    private int forceTicks;
    private static final int MAX_OUTSIDE_TICKS = 900;
    public final AnimationState idleAnimationState = new AnimationState();
    private int idleAnimationTimeout = 0;

    public TermiteEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation nav) nav.setCanFloat(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.@NotNull Builder createMobAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.16).add(Attributes.MAX_HEALTH, 1.0).add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    public BlockPos getHome() {
        return homePos;
    }

    public void setHome(BlockPos pos) {
        homePos = pos.immutable();
    }

    public void forceTarget(BlockPos pos, int ticks) {
        forcedTarget = pos.immutable();
        forceTicks = Math.max(ticks, 0);
        returning = false;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new ReturnHomeGoal());
        goalSelector.addGoal(2, new StripLogGoal());
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.9));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (idleAnimationTimeout <= 0) {
                idleAnimationTimeout = random.nextInt(40) + 80;
                idleAnimationState.start(tickCount);
            } else {
                idleAnimationTimeout--;
            }
        } else {
            if (forceTicks > 0) forceTicks--;
            outsideTicks++;
            if (outsideTicks > MAX_OUTSIDE_TICKS) returning = true;
            if (homePos == null) homePos = blockPosition().immutable();
        }
    }

    @Override
    public boolean isAggressive() {
        return false;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(Blocks.OAK_PLANKS.asItem()) || stack.is(Blocks.SPRUCE_PLANKS.asItem()) || stack.is(Blocks.BIRCH_PLANKS.asItem()) || stack.is(Blocks.JUNGLE_PLANKS.asItem()) || stack.is(Blocks.ACACIA_PLANKS.asItem()) || stack.is(Blocks.DARK_OAK_PLANKS.asItem()) || stack.is(Blocks.MANGROVE_PLANKS.asItem()) || stack.is(Blocks.CHERRY_PLANKS.asItem()) || stack.is(Blocks.BAMBOO_PLANKS.asItem()) || stack.is(Blocks.CRIMSON_PLANKS.asItem()) || stack.is(Blocks.WARPED_PLANKS.asItem());
    }

    class ReturnHomeGoal extends Goal {
        @Override
        public boolean canUse() {
            return homePos != null && returning;
        }

        @Override
        public void start() {
            getNavigation().moveTo(homePos.getX() + 0.5, homePos.getY() + 0.1, homePos.getZ() + 0.5, 1.0);
        }

        @Override
        public boolean canContinueToUse() {
            return homePos != null && returning && distanceToSqr(homePos.getX() + 0.5, homePos.getY() + 0.1, homePos.getZ() + 0.5) > 0.64;
        }

        @Override
        public void tick() {
            if (homePos != null) {
                double d = distanceToSqr(homePos.getX() + 0.5, homePos.getY() + 0.1, homePos.getZ() + 0.5);
                if (d <= 0.64) {
                    returning = false;
                    outsideTicks = 0;
                    if (!getNavigation().isDone()) getNavigation().stop();
                } else if (tickCount % 10 == 0 || getNavigation().isDone()) {
                    getNavigation().moveTo(homePos.getX() + 0.5, homePos.getY() + 0.1, homePos.getZ() + 0.5, 1.0);
                }
            }
        }

        @Override
        public void stop() {
            returning = false;
            outsideTicks = 0;
        }
    }

    class StripLogGoal extends Goal {
        private BlockPos logPos;
        private BlockPos approachPos;

        public StripLogGoal() {
            setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (returning) return false;
            if (forcedTarget != null && forceTicks > 0) {
                logPos = forcedTarget.immutable();
                approachPos = computeApproach(logPos);
                return approachPos != null && isStrippable(logPos);
            }
            if (tickCount % 6 != 0) return false;
            Optional<BlockPos> found = findNearestUnstrippedLog();
            if (found.isPresent()) {
                logPos = found.get();
                approachPos = computeApproach(logPos);
                return approachPos != null;
            }
            return false;
        }

        @Override
        public void start() {
            if (approachPos != null) getNavigation().moveTo(approachPos.getX() + 0.5, approachPos.getY() + 0.1, approachPos.getZ() + 0.5, 1.05);
        }

        @Override
        public boolean canContinueToUse() {
            if (returning) return false;
            if (logPos == null || approachPos == null) return false;
            if (forceTicks <= 0 && forcedTarget != null && logPos.equals(forcedTarget)) return false;
            return true;
        }

        @Override
        public void tick() {
            if (logPos == null) return;
            double d = distanceToSqr(logPos.getX() + 0.5, logPos.getY() + 0.1, logPos.getZ() + 0.5);
            if (d <= 2.75) {
                boolean didStrip = false;
                if (level() instanceof ServerLevel server) {
                    var state = server.getBlockState(logPos);
                    var res = TermiteEvents.STRIP_LOG.invoker().strip(server, logPos, state);
                    if (res == dev.architectury.event.EventResult.interruptTrue()) {
                        didStrip = true;
                    } else if (res != dev.architectury.event.EventResult.interruptFalse()) {
                        var stripped = AxeItem.STRIPPABLES.get(state.getBlock());
                        if (stripped != null && canStrip(server, logPos)) {
                            var axis = state.hasProperty(RotatedPillarBlock.AXIS) ? state.getValue(RotatedPillarBlock.AXIS) : Direction.Axis.Y;
                            var newState = stripped.defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
                            server.setBlockAndUpdate(logPos, newState);
                            server.playSound(null, logPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);
                            setCooldown(server, logPos);
                            didStrip = true;
                        }
                    }
                }
                if (forcedTarget != null && logPos.equals(forcedTarget)) forcedTarget = null;
                if (didStrip) {
                    outsideTicks = 0;
                    returning = true;
                } else {
                    outsideTicks = Math.min(outsideTicks + 40, MAX_OUTSIDE_TICKS);
                }
                logPos = null;
                approachPos = null;
            } else if (tickCount % 10 == 0 || getNavigation().isDone()) {
                if (approachPos == null || distanceToSqr(approachPos.getX() + 0.5, approachPos.getY() + 0.1, approachPos.getZ() + 0.5) <= 0.64) {
                    approachPos = computeApproach(logPos);
                }
                if (approachPos != null) getNavigation().moveTo(approachPos.getX() + 0.5, approachPos.getY() + 0.1, approachPos.getZ() + 0.5, 1.05);
            }
        }

        @Override
        public void stop() {
            logPos = null;
            approachPos = null;
        }

        private Optional<BlockPos> findNearestUnstrippedLog() {
            int r = 16;
            BlockPos origin = blockPosition();
            ServerLevel server = level() instanceof ServerLevel s ? s : null;
            if (server == null) return Optional.empty();
            return BlockPos.betweenClosedStream(origin.offset(-r, -3, -r), origin.offset(r, 3, r))
                    .map(BlockPos::immutable)
                    .filter(p -> isStrippable(p) && computeApproach(p) != null)
                    .min(Comparator.comparingDouble(p -> p.distSqr(origin)));
        }

        private boolean isStrippable(BlockPos pos) {
            if (!(level() instanceof ServerLevel server)) return false;
            var st = server.getBlockState(pos);
            return AxeItem.STRIPPABLES.get(st.getBlock()) != null;
        }

        private BlockPos computeApproach(BlockPos target) {
            if (!(level() instanceof ServerLevel server)) return null;
            BlockPos[] around = new BlockPos[] { target.west(), target.east(), target.north(), target.south(), target.west().below(), target.east().below(), target.north().below(), target.south().below() };
            for (BlockPos a : around) {
                var below = a.below();
                if (!server.getBlockState(below).isAir() && server.getBlockState(a).isAir()) {
                    if (getNavigation().createPath(a, 0) != null) return a.immutable();
                }
            }
            return null;
        }
    }

    private static long key(ServerLevel level, BlockPos pos) {
        return (((long) level.dimension().location().hashCode()) << 32) ^ pos.asLong();
    }

    private static boolean canStrip(ServerLevel level, BlockPos pos) {
        long k = key(level, pos);
        long now = level.getGameTime();
        long last = STRIP_COOLDOWN.getOrDefault(k, Long.MIN_VALUE);
        return now - last >= 600L;
    }

    private static void setCooldown(ServerLevel level, BlockPos pos) {
        long k = key(level, pos);
        STRIP_COOLDOWN.put(k, level.getGameTime());
    }
}
