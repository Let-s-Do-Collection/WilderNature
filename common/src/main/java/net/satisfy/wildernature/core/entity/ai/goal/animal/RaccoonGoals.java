package net.satisfy.wildernature.core.entity.ai.goal.animal;

import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.entity.animal.neutral.RaccoonEntity;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Objects;

public class RaccoonGoals {

    public static class RaccoonFloatGoal extends FloatGoal {
        public RaccoonFloatGoal(RaccoonEntity raccoon) {
            super(raccoon);
        }
    }

    public static class RaccoonPanicGoal extends PanicGoal {
        private final RaccoonEntity raccoon;

        public RaccoonPanicGoal(RaccoonEntity raccoon, double speedModifier) {
            super(raccoon, speedModifier);
            this.raccoon = raccoon;
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.startRunningAnim();
            super.start();
        }

        @Override
        public void stop() {
            this.raccoon.stopRunningAnim();
            super.stop();
        }
    }

    public static class RaccoonAvoidEntityGoal<T extends LivingEntity> extends AvoidEntityGoal<T> {
        private final RaccoonEntity raccoon;
        private final Class<T> targetClass;

        public RaccoonAvoidEntityGoal(RaccoonEntity raccoon, Class<T> targetClass) {
            super(raccoon, targetClass, 18.0F, 1.6D, 2.0D);
            this.raccoon = raccoon;
            this.targetClass = targetClass;
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot()) {
                return false;
            }

            if (!super.canUse()) {
                return false;
            }

            return this.targetClass != Villager.class || !(this.toAvoid instanceof Villager villager) || !villager.isSleeping();
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.startRunningAnim();
            super.start();
        }

        @Override
        public void stop() {
            this.raccoon.stopRunningAnim();
            super.stop();
        }
    }

    public static class RaccoonDoorInteractGoal extends Goal {
        private static final int OPEN_AFTER_TICKS = 8;
        private static final int MAX_USE_TICKS = 12;

        private final RaccoonEntity raccoon;
        private BlockPos targetDoorPos;
        private int useTicks;
        private boolean openedDoor;

        public RaccoonDoorInteractGoal(RaccoonEntity raccoon) {
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isWashing() || this.raccoon.isRaccoonRunning()) {
                return false;
            }

            this.targetDoorPos = this.findTargetDoor();
            return this.targetDoorPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetDoorPos != null
                    && !this.openedDoor
                    && this.useTicks < MAX_USE_TICKS
                    && !this.raccoon.isSleeping()
                    && !this.raccoon.isPanicking()
                    && this.raccoon.distanceToSqr(Vec3.atCenterOf(this.targetDoorPos)) <= 6.25D
                    && this.isWoodenDoor(this.targetDoorPos)
                    && this.isClosedWoodenDoor(this.targetDoorPos);
        }

        @Override
        public void start() {
            this.useTicks = 0;
            this.openedDoor = false;
            this.raccoon.wakeUp();
            Objects.requireNonNull(this.raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(RaccoonEntity.DOOR_DO_NOT_MOVE_MODIFIER.id());
            Objects.requireNonNull(this.raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).addTransientModifier(RaccoonEntity.DOOR_DO_NOT_MOVE_MODIFIER);
            this.raccoon.startOpenDoorAnim();
        }

        @Override
        public void tick() {
            if (this.targetDoorPos == null) {
                return;
            }

            this.raccoon.getLookControl().setLookAt(this.targetDoorPos.getX() + 0.5D, this.targetDoorPos.getY() + 0.5D, this.targetDoorPos.getZ() + 0.5D);
            this.useTicks++;

            if (this.useTicks >= OPEN_AFTER_TICKS && !this.openedDoor) {
                this.setDoorOpen(this.targetDoorPos);
                this.openedDoor = true;
            }
        }

        @Override
        public void stop() {
            Objects.requireNonNull(this.raccoon.getAttribute(Attributes.MOVEMENT_SPEED)).removeModifier(RaccoonEntity.DOOR_DO_NOT_MOVE_MODIFIER.id());
            this.useTicks = 0;
            this.openedDoor = false;
            this.targetDoorPos = null;
            this.raccoon.stopOpenDoorAnim();
        }

        @Nullable
        private BlockPos findTargetDoor() {
            BlockPos entityBlockPos = this.raccoon.blockPosition();
            BlockPos navigationTargetPos = this.raccoon.getNavigation().getTargetPos();
            BlockPos nearestDoorPos = null;
            double nearestDistance = Double.MAX_VALUE;

            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                for (int offsetX = -1; offsetX <= 1; offsetX++) {
                    for (int offsetZ = -1; offsetZ <= 1; offsetZ++) {
                        BlockPos checkedPos = entityBlockPos.offset(offsetX, offsetY, offsetZ);
                        BlockPos lowerDoorPos = this.toLowerDoorPos(checkedPos);
                        if (lowerDoorPos == null || !this.isClosedWoodenDoor(lowerDoorPos)) {
                            continue;
                        }

                        double checkedDistance = this.raccoon.distanceToSqr(Vec3.atCenterOf(lowerDoorPos));
                        if (checkedDistance > 6.25D) {
                            continue;
                        }

                        if (navigationTargetPos != null) {
                            double doorToTargetDistance = lowerDoorPos.distSqr(navigationTargetPos);
                            double entityToTargetDistance = entityBlockPos.distSqr(navigationTargetPos);
                            if (doorToTargetDistance > entityToTargetDistance + 2.0D) {
                                continue;
                            }
                        }

                        if (checkedDistance < nearestDistance) {
                            nearestDistance = checkedDistance;
                            nearestDoorPos = lowerDoorPos;
                        }
                    }
                }
            }

            if (nearestDoorPos != null) {
                return nearestDoorPos;
            }

            Direction movementDirection = this.raccoon.getMotionDirection();
            if (movementDirection.getAxis().isHorizontal()) {
                BlockPos forwardPos = entityBlockPos.relative(movementDirection);
                BlockPos lowerDoorPos = this.toLowerDoorPos(forwardPos);
                if (lowerDoorPos != null && this.isClosedWoodenDoor(lowerDoorPos)) {
                    return lowerDoorPos;
                }
            }

            return null;
        }

        @Nullable
        private BlockPos toLowerDoorPos(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof DoorBlock) || !blockState.is(BlockTags.WOODEN_DOORS)) {
                return null;
            }

            if (blockState.hasProperty(DoorBlock.HALF) && blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER) {
                return blockPos.below();
            }

            return blockPos;
        }

        private boolean isWoodenDoor(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            return blockState.getBlock() instanceof DoorBlock && blockState.is(BlockTags.WOODEN_DOORS);
        }

        private boolean isClosedWoodenDoor(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            return blockState.getBlock() instanceof DoorBlock && blockState.is(BlockTags.WOODEN_DOORS) && !blockState.getValue(DoorBlock.OPEN);
        }

        private void setDoorOpen(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof DoorBlock doorBlock) || !blockState.is(BlockTags.WOODEN_DOORS) || blockState.getValue(DoorBlock.OPEN)) {
                return;
            }

            doorBlock.setOpen(this.raccoon, this.raccoon.level(), blockState, blockPos, true);
        }
    }
    
    public static class HarvestBerryBushGoal extends MoveToBlockGoal {
        private final RaccoonEntity raccoon;
        
    public HarvestBerryBushGoal(RaccoonEntity raccoon, double speed) {
        super(raccoon, speed, 8);
        this.raccoon = raccoon;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) >= 2;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isReachedTarget()) {
            BlockState state = raccoon.level().getBlockState(this.blockPos);
            if (state.is(Blocks.SWEET_BERRY_BUSH) && state.getValue(SweetBerryBushBlock.AGE) >= 2) {
                int dropCount = 1 + raccoon.getRandom().nextInt(2);
                ItemStack berries = new ItemStack(Items.SWEET_BERRIES, dropCount);

                if (raccoon.getMainHandItem().isEmpty()) {
                    raccoon.setItemSlot(EquipmentSlot.MAINHAND, berries);
                } else {
                    Block.popResource(raccoon.level(), blockPos, berries); // drop them
                }

                raccoon.level().setBlock(blockPos, state.setValue(SweetBerryBushBlock.AGE, 1), 2);
                raccoon.playSound(SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, 1.0F, 1.0F);
            }
        }
      }
   }
    
    public static class RaccoonVillageStrollGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 40;
        private static final int SEARCH_COOLDOWN_MAX = 90;
        private static final int STUCK_TIMEOUT_TICKS = 50;
        private static final int PATH_RECHECK_INTERVAL_TICKS = 20;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetDoorPos;
        private int searchCooldownTicks;
        private int stuckTicks;
        private int pathRecheckCooldownTicks;
        private Vec3 lastPosition = Vec3.ZERO;
        private boolean cachedCanReachTarget;
        private BlockPos cachedPathTargetPos;

        public RaccoonVillageStrollGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.level().isDay() || this.raccoon.isPanicking() || this.raccoon.isSheltering() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isSleeping()) {
                return false;
            }

            if (this.targetDoorPos != null && this.isValidDoor(this.targetDoorPos) && this.canReachCached(this.targetDoorPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetDoorPos = this.findNearestDoor();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            if (this.targetDoorPos == null) {
                return false;
            }

            if (!this.canReachCached(this.targetDoorPos)) {
                this.targetDoorPos = null;
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.raccoon.level().isDay()
                    && !this.raccoon.isPanicking()
                    && !this.raccoon.isSleeping()
                    && !this.raccoon.isOpeningDoor()
                    && this.targetDoorPos != null
                    && this.isValidDoor(this.targetDoorPos)
                    && this.canContinueWithCachedPath(this.targetDoorPos);
        }

        @Override
        public void start() {
            this.stuckTicks = 0;
            this.pathRecheckCooldownTicks = PATH_RECHECK_INTERVAL_TICKS;
            this.lastPosition = this.raccoon.position();
        }

        @Override
        public void stop() {
            this.raccoon.getNavigation().stop();
            this.targetDoorPos = null;
            this.searchCooldownTicks = 0;
            this.stuckTicks = 0;
            this.pathRecheckCooldownTicks = 0;
            this.lastPosition = Vec3.ZERO;
            this.cachedCanReachTarget = false;
            this.cachedPathTargetPos = null;
        }

        @Override
        public void tick() {
            if (this.targetDoorPos == null) {
                return;
            }

            if (!this.isValidDoor(this.targetDoorPos) || !this.canContinueWithCachedPath(this.targetDoorPos)) {
                this.targetDoorPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                this.stuckTicks = 0;
                return;
            }

            this.raccoon.getNavigation().moveTo(this.targetDoorPos.getX() + 0.5D, this.targetDoorPos.getY(), this.targetDoorPos.getZ() + 0.5D, this.speedModifier);

            Vec3 currentPosition = this.raccoon.position();
            if (currentPosition.distanceToSqr(this.lastPosition) < 0.01D) {
                this.stuckTicks++;
            } else {
                this.stuckTicks = 0;
                this.lastPosition = currentPosition;
            }

            if (this.stuckTicks >= STUCK_TIMEOUT_TICKS || this.raccoon.getNavigation().isDone()) {
                this.raccoon.getNavigation().stop();
                this.targetDoorPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                this.stuckTicks = 0;
                this.cachedCanReachTarget = false;
                this.cachedPathTargetPos = null;
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestDoor() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestDoorPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.VILLAGE_STROLL_RANGE; offsetX <= RaccoonEntity.VILLAGE_STROLL_RANGE; offsetX++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.VILLAGE_STROLL_RANGE; offsetZ <= RaccoonEntity.VILLAGE_STROLL_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.isValidDoor(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestDoorPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestDoorPos;
        }

        private boolean isValidDoor(BlockPos blockPos) {
            return this.raccoon.level().getBlockState(blockPos).is(BlockTags.WOODEN_DOORS);
        }

        private boolean canReachCached(BlockPos blockPos) {
            if (blockPos.equals(this.cachedPathTargetPos)) {
                return this.cachedCanReachTarget;
            }

            Path path = this.raccoon.getNavigation().createPath(blockPos, 0);
            this.cachedPathTargetPos = blockPos.immutable();
            this.cachedCanReachTarget = path != null && path.canReach();
            this.pathRecheckCooldownTicks = PATH_RECHECK_INTERVAL_TICKS;
            return this.cachedCanReachTarget;
        }

        private boolean canContinueWithCachedPath(BlockPos blockPos) {
            if (!blockPos.equals(this.cachedPathTargetPos)) {
                return this.canReachCached(blockPos);
            }

            if (this.pathRecheckCooldownTicks > 0) {
                this.pathRecheckCooldownTicks--;
                return this.cachedCanReachTarget;
            }

            return this.canReachCached(blockPos);
        }
    }

    public static class RaccoonOpenContainerGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 30;
        private static final int SEARCH_COOLDOWN_MAX = 80;
        private static final int LOOTING_DURATION = 22;
        private static final int STUCK_TIMEOUT_TICKS = 50;
        private static final int PATH_RECHECK_INTERVAL_TICKS = 20;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetContainerPos;
        private int searchCooldownTicks;
        private int lootingTicks;
        private boolean containerOpened;
        private int stuckTicks;
        private int pathRecheckCooldownTicks;
        private Vec3 lastPosition = Vec3.ZERO;
        private boolean cachedCanReachTarget;
        private BlockPos cachedPathTargetPos;

        public RaccoonOpenContainerGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.raccoon.canLootContainers()) {
                return false;
            }

            if (this.targetContainerPos != null && this.isValidContainer(this.targetContainerPos) && this.canReachCached(this.targetContainerPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetContainerPos = this.findNearestContainer();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            if (this.targetContainerPos == null) {
                return false;
            }

            if (!this.canReachCached(this.targetContainerPos)) {
                this.targetContainerPos = null;
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.raccoon.canLootContainers()
                    && this.targetContainerPos != null
                    && this.isValidContainer(this.targetContainerPos)
                    && this.canContinueWithCachedPath(this.targetContainerPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.setContainerLooting(true);
            this.containerOpened = false;
            this.lootingTicks = 0;
            this.stuckTicks = 0;
            this.pathRecheckCooldownTicks = PATH_RECHECK_INTERVAL_TICKS;
            this.lastPosition = this.raccoon.position();
        }

        @Override
        public void stop() {
            this.closeContainer();
            this.raccoon.setContainerLooting(false);
            this.raccoon.getNavigation().stop();
            this.targetContainerPos = null;
            this.searchCooldownTicks = 0;
            this.lootingTicks = 0;
            this.stuckTicks = 0;
            this.pathRecheckCooldownTicks = 0;
            this.lastPosition = Vec3.ZERO;
            this.cachedCanReachTarget = false;
            this.cachedPathTargetPos = null;
        }

        @Override
        public void tick() {
            if (this.targetContainerPos == null) {
                return;
            }

            if (!this.isValidContainer(this.targetContainerPos) || !this.canContinueWithCachedPath(this.targetContainerPos)) {
                this.closeContainer();
                this.targetContainerPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                this.lootingTicks = 0;
                this.stuckTicks = 0;
                return;
            }

            if (!this.targetContainerPos.closerToCenterThan(this.raccoon.position(), 2.25D)) {
                this.closeContainer();
                this.raccoon.getNavigation().moveTo(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY(), this.targetContainerPos.getZ() + 0.5D, this.speedModifier);
                this.lootingTicks = 0;

                Vec3 currentPosition = this.raccoon.position();
                if (currentPosition.distanceToSqr(this.lastPosition) < 0.01D) {
                    this.stuckTicks++;
                } else {
                    this.stuckTicks = 0;
                    this.lastPosition = currentPosition;
                }

                if (this.stuckTicks >= STUCK_TIMEOUT_TICKS || this.raccoon.getNavigation().isDone()) {
                    this.raccoon.getNavigation().stop();
                    this.targetContainerPos = null;
                    this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                    this.stuckTicks = 0;
                    this.cachedCanReachTarget = false;
                    this.cachedPathTargetPos = null;
                }

                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY() + 0.5D, this.targetContainerPos.getZ() + 0.5D);
            this.stuckTicks = 0;
            this.lastPosition = this.raccoon.position();

            if (!this.containerOpened) {
                this.openContainer();
                this.containerOpened = true;
            }

            if (this.lootingTicks < LOOTING_DURATION) {
                this.lootingTicks++;
                return;
            }

            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);
            if (blockEntity instanceof Container container) {
                boolean itemTaken = this.raccoon.tryTakeItemFromContainer(container);

                if (itemTaken) {
                    this.closeContainer();
                    this.targetContainerPos = null;
                    this.searchCooldownTicks = this.getNextSearchCooldown();
                    this.lootingTicks = 0;
                    return;
                }
            }

            this.closeContainer();
            this.targetContainerPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
            this.lootingTicks = 0;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestContainer() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestContainerPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.isValidContainer(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestContainerPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestContainerPos;
        }

        private boolean isValidContainer(BlockPos blockPos) {
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(blockPos);
            if (!(blockEntity instanceof Container container)) {
                return false;
            }
            if (blockEntity instanceof HollowCacheBlockEntity) {
                return false;
            }

            for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
                ItemStack itemStack = container.getItem(slotIndex);
                if (!itemStack.isEmpty() && (this.raccoon.isFood(itemStack) || itemStack.is(ItemTags.VILLAGER_PLANTABLE_SEEDS))) {
                    return true;
                }
            }
            return false;
        }

        private boolean canReachCached(BlockPos blockPos) {
            if (blockPos.equals(this.cachedPathTargetPos)) {
                return this.cachedCanReachTarget;
            }

            Path path = this.raccoon.getNavigation().createPath(blockPos, 0);
            this.cachedPathTargetPos = blockPos.immutable();
            this.cachedCanReachTarget = path != null && path.canReach();
            this.pathRecheckCooldownTicks = PATH_RECHECK_INTERVAL_TICKS;
            return this.cachedCanReachTarget;
        }

        private boolean canContinueWithCachedPath(BlockPos blockPos) {
            if (!blockPos.equals(this.cachedPathTargetPos)) {
                return this.canReachCached(blockPos);
            }

            if (this.pathRecheckCooldownTicks > 0) {
                this.pathRecheckCooldownTicks--;
                return this.cachedCanReachTarget;
            }

            return this.canReachCached(blockPos);
        }

        private void openContainer() {
            if (this.targetContainerPos == null) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetContainerPos);
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);

            if (blockEntity != null) {
                blockEntity.triggerEvent(1, 1);
            }

            this.raccoon.level().blockEvent(this.targetContainerPos, blockState.getBlock(), 1, 1);
        }

        private void closeContainer() {
            if (this.targetContainerPos == null || !this.containerOpened) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetContainerPos);
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);

            if (blockEntity != null) {
                blockEntity.triggerEvent(1, 0);
            }

            this.raccoon.level().blockEvent(this.targetContainerPos, blockState.getBlock(), 1, 0);
            this.containerOpened = false;
        }
    }

    public static class RaccoonStealEggGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 60;
        private static final int SEARCH_COOLDOWN_MAX = 140;
        private static final int STEAL_DURATION = 18;
        private static final ResourceLocation CHICKEN_COOP_ID = ResourceLocation.parse("farm_and_charm:chicken_coop");
        private static final ResourceLocation CHICKEN_NEST_ID = ResourceLocation.parse("farm_and_charm:chicken_nest");

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetContainerPos;
        private int searchCooldownTicks;
        private int stealingTicks;
        private boolean containerOpened;

        public RaccoonStealEggGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!Platform.isModLoaded("farm_and_charm")) {
                return false;
            }
            if (this.raccoon.level().isDay()) {
                return false;
            }
            if (!(this.raccoon.level() instanceof ServerLevel serverLevel) || !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isSheltering() || this.raccoon.isBaby()) {
                return false;
            }
            if (!this.raccoon.getMainHandItem().isEmpty() && !this.raccoon.hasFreeInventorySlot()) {
                return false;
            }
            if (this.raccoon.getStoreCooldownTicks() > 0) {
                return false;
            }
            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetContainerPos = this.findNearestEggContainer();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetContainerPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return Platform.isModLoaded("farm_and_charm")
                    && !this.raccoon.level().isDay()
                    && !this.raccoon.isSleeping()
                    && !this.raccoon.isPanicking()
                    && !this.raccoon.isOpeningDoor()
                    && !this.raccoon.isStoringLoot()
                    && !this.raccoon.isWashing()
                    && this.targetContainerPos != null
                    && this.isValidEggContainer(this.targetContainerPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.raccoon.setContainerLooting(true);
            this.containerOpened = false;
            this.stealingTicks = 0;
        }

        @Override
        public void stop() {
            this.closeContainer();
            this.raccoon.setContainerLooting(false);
            this.raccoon.getNavigation().stop();
            this.targetContainerPos = null;
            this.stealingTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetContainerPos == null) {
                return;
            }

            if (!this.isValidEggContainer(this.targetContainerPos)) {
                this.closeContainer();
                this.targetContainerPos = null;
                this.stealingTicks = 0;
                this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
                return;
            }

            if (!this.targetContainerPos.closerToCenterThan(this.raccoon.position(), 2.25D)) {
                this.closeContainer();
                this.raccoon.getNavigation().moveTo(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY(), this.targetContainerPos.getZ() + 0.5D, this.speedModifier);
                this.stealingTicks = 0;
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetContainerPos.getX() + 0.5D, this.targetContainerPos.getY() + 0.5D, this.targetContainerPos.getZ() + 0.5D);

            if (!this.containerOpened) {
                this.openContainer();
                this.containerOpened = true;
            }

            if (this.stealingTicks < STEAL_DURATION) {
                this.stealingTicks++;
                return;
            }

            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);
            if (blockEntity instanceof Container container && this.tryTakeEgg(container)) {
                this.closeContainer();
                this.raccoon.startContainerLootCooldown();
                this.raccoon.startStoreLootCooldown();
                this.targetContainerPos = null;
                this.stealingTicks = 0;
                return;
            }

            this.closeContainer();
            this.targetContainerPos = null;
            this.stealingTicks = 0;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestEggContainer() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestContainerPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -4; offsetY <= 4; offsetY++) {
                    for (int offsetZ = -RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ <= RaccoonEntity.CONTAINER_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.isValidEggContainer(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestContainerPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestContainerPos;
        }

        private boolean isValidEggContainer(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            if (!CHICKEN_COOP_ID.equals(blockId) && !CHICKEN_NEST_ID.equals(blockId)) {
                return false;
            }

            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(blockPos);
            if (!(blockEntity instanceof Container container)) {
                return false;
            }

            for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
                if (container.getItem(slotIndex).is(Items.EGG)) {
                    return true;
                }
            }

            return false;
        }

        private boolean tryTakeEgg(Container container) {
            for (int slotIndex = 0; slotIndex < container.getContainerSize(); slotIndex++) {
                ItemStack slotStack = container.getItem(slotIndex);
                if (!slotStack.is(Items.EGG)) {
                    continue;
                }

                ItemStack extractedStack = container.removeItem(slotIndex, 1);
                if (extractedStack.isEmpty()) {
                    return false;
                }

                if (this.raccoon.getMainHandItem().isEmpty()) {
                    this.raccoon.setItemSlot(EquipmentSlot.MAINHAND, extractedStack);
                    container.setChanged();
                    return true;
                }

                if (this.raccoon.hasFreeInventorySlot()) {
                    for (int inventorySlotIndex = 0; inventorySlotIndex < RaccoonEntity.INVENTORY_SIZE; inventorySlotIndex++) {
                        ItemStack inventoryStack = this.raccoon.getItemBySlot(EquipmentSlot.MAINHAND);
                        if (inventoryStack.isEmpty()) {
                            break;
                        }
                    }
                }

                container.setItem(slotIndex, extractedStack);
                return false;
            }

            return false;
        }

        private void openContainer() {
            if (this.targetContainerPos == null) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetContainerPos);
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);

            if (blockEntity != null) {
                blockEntity.triggerEvent(1, 1);
            }

            this.raccoon.level().blockEvent(this.targetContainerPos, blockState.getBlock(), 1, 1);
        }

        private void closeContainer() {
            if (this.targetContainerPos == null || !this.containerOpened) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetContainerPos);
            BlockEntity blockEntity = this.raccoon.level().getBlockEntity(this.targetContainerPos);

            if (blockEntity != null) {
                blockEntity.triggerEvent(1, 0);
            }

            this.raccoon.level().blockEvent(this.targetContainerPos, blockState.getBlock(), 1, 0);
            this.containerOpened = false;
        }
    }

    public static class RaccoonWashSelfGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 220;
        private static final int SEARCH_COOLDOWN_MAX = 420;

        private final RaccoonEntity raccoon;
        private int washTicks;
        private int searchCooldownTicks;

        public RaccoonWashSelfGoal(RaccoonEntity raccoon) {
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isSheltering()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            if (this.raccoon.getRandom().nextFloat() >= 0.025F) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.washTicks > 0 && !this.raccoon.isPanicking() && !this.raccoon.isSleeping();
        }

        @Override
        public void start() {
            this.raccoon.getNavigation().stop();
            this.raccoon.startWash();
            this.washTicks = 40 + this.raccoon.getRandom().nextInt(30);
        }

        @Override
        public void stop() {
            this.raccoon.stopWash();
            this.washTicks = 0;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        @Override
        public void tick() {
            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.raccoon.getX(), this.raccoon.getEyeY(), this.raccoon.getZ());

            if (this.washTicks > 0) {
                this.washTicks--;
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }
    }

    public static class RaccoonCuriosityGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 160;
        private static final double CURIOSITY_RADIUS = 7.0D;

        private final RaccoonEntity raccoon;
        private Player targetPlayer;
        private int curiosityTicks;
        private int searchCooldownTicks;

        public RaccoonCuriosityGoal(RaccoonEntity raccoon) {
            this.raccoon = raccoon;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isRaccoonRunning()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            Player nearestPlayer = this.raccoon.level().getNearestPlayer(this.raccoon, CURIOSITY_RADIUS);
            if (nearestPlayer == null || nearestPlayer.isCreative() || nearestPlayer.isSpectator() || nearestPlayer.isSprinting()) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            if (this.raccoon.getRandom().nextFloat() >= 0.12F) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            this.targetPlayer = nearestPlayer;
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.targetPlayer.isAlive() && !this.targetPlayer.isCreative() && !this.targetPlayer.isSpectator() && !this.targetPlayer.isSprinting() && this.raccoon.distanceToSqr(this.targetPlayer) <= CURIOSITY_RADIUS * CURIOSITY_RADIUS && this.curiosityTicks > 0 && !this.raccoon.isPanicking() && !this.raccoon.isSleeping();
        }

        @Override
        public void start() {
            this.raccoon.getNavigation().stop();
            this.curiosityTicks = 30 + this.raccoon.getRandom().nextInt(30);
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.curiosityTicks = 0;
            this.searchCooldownTicks = this.getNextSearchCooldown();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.curiosityTicks > 0) {
                this.curiosityTicks--;
            }
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }
    }

    public static class RaccoonNibbleCropGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 60;
        private static final int SEARCH_COOLDOWN_MAX = 120;
        private static final int NIBBLE_DURATION_MIN = 12;
        private static final int NIBBLE_DURATION_MAX = 24;
        private static final int CROP_SEARCH_RANGE = 8;

        private final RaccoonEntity raccoon;
        private final double speedModifier;
        private BlockPos targetCropPos;
        private int searchCooldownTicks;
        private int nibbleTicks;

        public RaccoonNibbleCropGoal(RaccoonEntity raccoon, double speedModifier) {
            this.raccoon = raccoon;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.raccoon.level().isDay()) {
                return false;
            }

            if (!(this.raccoon.level() instanceof ServerLevel serverLevel) || !serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                return false;
            }

            if (!this.raccoon.canNibbleMoreCropsThisNight()) {
                return false;
            }

            if (this.raccoon.isSleeping() || this.raccoon.isPanicking() || this.raccoon.isOpeningDoor() || this.raccoon.isContainerLooting() || this.raccoon.isStoringLoot() || this.raccoon.isWashing() || this.raccoon.isSheltering() || this.raccoon.isBaby()) {
                return false;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            if (this.raccoon.getRandom().nextFloat() >= 0.22F) {
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return false;
            }

            this.targetCropPos = this.findNearestCrop();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetCropPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetCropPos != null && !this.raccoon.level().isDay() && !this.raccoon.isPanicking() && !this.raccoon.isSleeping() && this.raccoon.canNibbleMoreCropsThisNight() && this.isValidCrop(this.targetCropPos);
        }

        @Override
        public void start() {
            this.raccoon.wakeUp();
            this.nibbleTicks = 0;
        }

        @Override
        public void stop() {
            this.raccoon.getNavigation().stop();
            this.targetCropPos = null;
            this.nibbleTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetCropPos == null) {
                return;
            }

            if (!this.isValidCrop(this.targetCropPos) || !this.raccoon.canNibbleMoreCropsThisNight()) {
                this.targetCropPos = null;
                return;
            }

            if (!this.targetCropPos.closerToCenterThan(this.raccoon.position(), 2.0D)) {
                this.raccoon.getNavigation().moveTo(this.targetCropPos.getX() + 0.5D, this.targetCropPos.getY(), this.targetCropPos.getZ() + 0.5D, this.speedModifier);
                return;
            }

            this.raccoon.getNavigation().stop();
            this.raccoon.getLookControl().setLookAt(this.targetCropPos.getX() + 0.5D, this.targetCropPos.getY() + 0.5D, this.targetCropPos.getZ() + 0.5D);

            if (this.nibbleTicks <= 0) {
                this.nibbleTicks = NIBBLE_DURATION_MIN + this.raccoon.getRandom().nextInt(NIBBLE_DURATION_MAX - NIBBLE_DURATION_MIN + 1);
                return;
            }

            this.nibbleTicks--;
            if (this.nibbleTicks > 0) {
                return;
            }

            BlockState blockState = this.raccoon.level().getBlockState(this.targetCropPos);
            if (blockState.getBlock() instanceof CropBlock cropBlock) {
                IntegerProperty ageProperty = cropBlock.getAgeProperty();
                int currentAge = blockState.getValue(ageProperty);
                if (currentAge > 0) {
                    this.raccoon.level().setBlock(this.targetCropPos, blockState.setValue(ageProperty, currentAge - 1), 2);
                    this.raccoon.markCropNibbled();

                    if (this.raccoon.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), this.targetCropPos.getX() + 0.5D, this.targetCropPos.getY() + 0.5D, this.targetCropPos.getZ() + 0.5D, 8, 0.2D, 0.2D, 0.2D, 0.02D);
                    }
                }
            }

            this.targetCropPos = null;
            this.searchCooldownTicks = this.getNextSearchCooldown() * 2;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.raccoon.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private BlockPos findNearestCrop() {
            BlockPos originPos = this.raccoon.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestCropPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -CROP_SEARCH_RANGE; offsetX <= CROP_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -2; offsetY <= 2; offsetY++) {
                    for (int offsetZ = -CROP_SEARCH_RANGE; offsetZ <= CROP_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.isValidCrop(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestCropPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestCropPos;
        }

        private boolean isValidCrop(BlockPos blockPos) {
            BlockState blockState = this.raccoon.level().getBlockState(blockPos);
            if (!(blockState.getBlock() instanceof CropBlock cropBlock)) {
                return false;
            }

            IntegerProperty ageProperty = cropBlock.getAgeProperty();
            return blockState.getValue(ageProperty) > 0;
        }
    }
}
