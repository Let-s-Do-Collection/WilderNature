package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.entity.animal.passive.BeaverEntity;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class BeaverGoals {
    public static class BeaverGnawLogGoal extends Goal {
        private enum State {IDLE, COOLDOWN, MOVING_TO_SPOT, PLACING, GNAWING}

        private static final int STRIP_AT_TICK = 20;
        private static final int BREAK_AT_TICK = 40;
        private static final int NO_SPOT_COOLDOWN = 40;
        private static final int CYCLE_COOLDOWN_MIN = 15;
        private static final int CYCLE_COOLDOWN_MAX = 25;

        private final BeaverEntity beaver;
        private final double speedModifier;

        private State state = State.IDLE;

        @Nullable
        private ItemStack currentLog = null;
        @Nullable
        private BlockPos workPos = null;

        private int gnawTicksElapsed = 0;
        private int noSpotCooldown = 0;
        private int cycleCooldown = 0;
        private boolean strippedPlacedLog = false;

        public BeaverGnawLogGoal(BeaverEntity beaver, double speedModifier) {
            this.beaver = beaver;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.beaver.hasRawLogs() && this.noSpotCooldown <= 0;
        }

        @Override
        public boolean canContinueToUse() {
            return this.state != State.IDLE || this.beaver.hasRawLogs();
        }

        @Override
        public void start() {
            this.state = State.IDLE;
            this.currentLog = null;
            this.workPos = null;
            this.gnawTicksElapsed = 0;
            this.strippedPlacedLog = false;
        }

        @Override
        public void stop() {
            this.beaver.setGnawing(false);
            this.beaver.getNavigation().stop();
            this.clearBreakProgress();

            if (this.currentLog != null && !this.currentLog.isEmpty()) {
                this.beaver.storeOneItem(this.currentLog);
                this.currentLog = null;
            }

            if (this.workPos != null && this.beaver.level() instanceof ServerLevel serverLevel) {
                BlockState placedState = serverLevel.getBlockState(this.workPos);
                if (!placedState.isAir()) {
                    serverLevel.destroyBlock(this.workPos, false, this.beaver);
                }
                this.workPos = null;
            }

            this.state = State.IDLE;
            this.gnawTicksElapsed = 0;
            this.strippedPlacedLog = false;
        }

        @Override
        public void tick() {
            if (this.noSpotCooldown > 0) {
                this.noSpotCooldown--;
            }

            switch (this.state) {
                case IDLE -> this.tickIdle();
                case COOLDOWN -> this.tickCooldown();
                case MOVING_TO_SPOT -> this.tickMoving();
                case PLACING -> this.tickPlacing();
                case GNAWING -> this.tickGnawing();
            }
        }

        private void tickIdle() {
            if (!this.beaver.hasRawLogs()) {
                return;
            }

            this.currentLog = this.beaver.takeOneRawLog();
            if (this.currentLog.isEmpty()) {
                return;
            }

            this.workPos = this.findFreeGroundBlock();
            if (this.workPos == null) {
                this.beaver.storeOneItem(this.currentLog);
                this.currentLog = null;
                this.noSpotCooldown = NO_SPOT_COOLDOWN;
                return;
            }

            this.state = State.MOVING_TO_SPOT;
            this.beaver.getNavigation().moveTo(this.workPos.getX() + 0.5D, this.workPos.getY(), this.workPos.getZ() + 0.5D, this.speedModifier);
        }

        private void tickCooldown() {
            if (--this.cycleCooldown <= 0) {
                this.state = State.IDLE;
            }
        }

        private void tickMoving() {
            if (this.workPos == null) {
                this.state = State.IDLE;
                return;
            }

            if (!this.isPosFree(this.workPos)) {
                this.workPos = this.findFreeGroundBlock();
                if (this.workPos == null) {
                    this.returnLogToInventory();
                    this.noSpotCooldown = NO_SPOT_COOLDOWN;
                    this.state = State.IDLE;
                    return;
                }
                this.beaver.getNavigation().moveTo(this.workPos.getX() + 0.5D, this.workPos.getY(), this.workPos.getZ() + 0.5D, this.speedModifier);
            }

            this.beaver.getLookControl().setLookAt(this.workPos.getX() + 0.5D, this.workPos.getY() + 0.5D, this.workPos.getZ() + 0.5D);

            if (this.workPos.closerToCenterThan(this.beaver.position(), 2.0D)) {
                this.beaver.getNavigation().stop();
                this.state = State.PLACING;
            }
        }

        private void tickPlacing() {
            if (this.workPos == null || this.currentLog == null || this.currentLog.isEmpty()) {
                this.state = State.IDLE;
                return;
            }

            if (!(this.beaver.level() instanceof ServerLevel serverLevel)) {
                this.state = State.IDLE;
                return;
            }

            if (!(this.currentLog.getItem() instanceof BlockItem blockItem) || !AxeItem.STRIPPABLES.containsKey(blockItem.getBlock())) {
                this.returnLogToInventory();
                this.state = State.IDLE;
                return;
            }

            BlockState placedState = blockItem.getBlock().defaultBlockState();
            serverLevel.setBlock(this.workPos, placedState, Block.UPDATE_ALL);
            serverLevel.playSound(null, this.workPos, placedState.getSoundType().getPlaceSound(), this.beaver.getSoundSource(), 1.0F, 1.0F);

            this.gnawTicksElapsed = 0;
            this.strippedPlacedLog = false;
            this.beaver.setGnawing(true);
            this.state = State.GNAWING;
        }

        private void tickGnawing() {
            if (this.workPos == null) {
                this.beaver.setGnawing(false);
                this.state = State.IDLE;
                return;
            }

            this.beaver.getLookControl().setLookAt(this.workPos.getX() + 0.5D, this.workPos.getY() + 0.5D, this.workPos.getZ() + 0.5D);

            this.gnawTicksElapsed++;

            if (this.beaver.level() instanceof ServerLevel serverLevel) {
                int stage = Math.min(9, (int) ((this.gnawTicksElapsed / (float) BREAK_AT_TICK) * 10.0F));
                serverLevel.destroyBlockProgress(this.beaver.getId(), this.workPos, stage);
            }

            if (this.gnawTicksElapsed % 8 == 0) {
                this.beaver.playSound(SoundEvents.AXE_STRIP, 0.5F, 0.9F + this.beaver.getRandom().nextFloat() * 0.2F);
            }

            if (!this.strippedPlacedLog && this.gnawTicksElapsed >= STRIP_AT_TICK && this.beaver.level() instanceof ServerLevel serverLevel) {
                ItemStack strippedLog = BeaverEntity.stripBlockAndGetItem(serverLevel, this.workPos);
                if (!strippedLog.isEmpty()) {
                    this.currentLog = strippedLog;
                    this.strippedPlacedLog = true;
                }
            }

            if (this.gnawTicksElapsed < BREAK_AT_TICK) {
                return;
            }

            if (this.beaver.level() instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlock(this.workPos, false, this.beaver);

                if (this.currentLog != null && !this.currentLog.isEmpty()) {
                    if (!this.beaver.storeOneItem(this.currentLog)) {
                        this.beaver.spawnAtLocation(this.currentLog);
                    }
                    this.currentLog = null;
                }
            }

            this.beaver.setGnawing(false);
            this.clearBreakProgress();

            this.workPos = null;
            this.gnawTicksElapsed = 0;
            this.strippedPlacedLog = false;
            this.cycleCooldown = CYCLE_COOLDOWN_MIN + this.beaver.getRandom().nextInt(CYCLE_COOLDOWN_MAX - CYCLE_COOLDOWN_MIN + 1);
            this.state = State.COOLDOWN;
        }

        private void clearBreakProgress() {
            if (this.workPos != null && this.beaver.level() instanceof ServerLevel serverLevel) {
                serverLevel.destroyBlockProgress(this.beaver.getId(), this.workPos, -1);
            }
        }

        @Nullable
        private BlockPos findFreeGroundBlock() {
            BlockPos origin = this.beaver.blockPosition();
            int range = BeaverEntity.WORK_SEARCH_RANGE;
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int xOffset = -range; xOffset <= range; xOffset++) {
                for (int zOffset = -range; zOffset <= range; zOffset++) {
                    for (int yOffset = -1; yOffset <= 1; yOffset++) {
                        mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                        if (mutable.equals(origin) || !this.isPosFree(mutable)) {
                            continue;
                        }

                        double distance = mutable.distSqr(origin);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = mutable.immutable();
                        }
                    }
                }
            }

            return best;
        }

        private boolean isPosFree(BlockPos pos) {
            return this.beaver.level().getBlockState(pos).isAir() && this.beaver.level().getBlockState(pos.below()).isSolid();
        }

        private void returnLogToInventory() {
            if (this.currentLog != null && !this.currentLog.isEmpty()) {
                this.beaver.storeOneItem(this.currentLog);
                this.currentLog = null;
            }
        }
    }

    public static class BeaverPreferWaterGoal extends Goal {
        private static final int SEARCH_RADIUS = 12;
        private static final int SEARCH_HEIGHT = 3;
        private static final int RECHECK_DELAY_MIN = 80;
        private static final int RECHECK_DELAY_MAX = 140;
        private static final int MIN_TARGET_SCORE = 6;

        private final BeaverEntity beaver;
        private final double speedModifier;

        @Nullable
        private BlockPos targetPos;
        private int recalcCooldown;

        public BeaverPreferWaterGoal(BeaverEntity beaver, double speedModifier) {
            this.beaver = beaver;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.recalcCooldown > 0) {
                this.recalcCooldown--;
                return false;
            }

            if (this.beaver.isGnawing()) {
                return false;
            }

            int currentScore = this.scorePosition(this.beaver.blockPosition());
            if (currentScore >= MIN_TARGET_SCORE && this.beaver.getRandom().nextInt(4) != 0) {
                this.recalcCooldown = RECHECK_DELAY_MIN + this.beaver.getRandom().nextInt(RECHECK_DELAY_MAX - RECHECK_DELAY_MIN + 1);
                return false;
            }

            this.targetPos = this.findBestWaterPos();
            if (this.targetPos == null) {
                this.recalcCooldown = RECHECK_DELAY_MIN + this.beaver.getRandom().nextInt(RECHECK_DELAY_MAX - RECHECK_DELAY_MIN + 1);
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPos != null && !this.beaver.getNavigation().isDone() && !this.beaver.isGnawing();
        }

        @Override
        public void start() {
            if (this.targetPos != null) {
                this.beaver.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.targetPos = null;
            this.recalcCooldown = RECHECK_DELAY_MIN + this.beaver.getRandom().nextInt(RECHECK_DELAY_MAX - RECHECK_DELAY_MIN + 1);
        }

        @Override
        public void tick() {
            if (this.targetPos != null) {
                this.beaver.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D);
            }
        }

        @Nullable
        private BlockPos findBestWaterPos() {
            BlockPos origin = this.beaver.blockPosition();
            BlockPos bestPos = null;
            int bestScore = Integer.MIN_VALUE;
            double bestDistance = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int xOffset = -SEARCH_RADIUS; xOffset <= SEARCH_RADIUS; xOffset++) {
                for (int zOffset = -SEARCH_RADIUS; zOffset <= SEARCH_RADIUS; zOffset++) {
                    for (int yOffset = -SEARCH_HEIGHT; yOffset <= SEARCH_HEIGHT; yOffset++) {
                        mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                        if (!this.isStandableNearWater(mutable)) {
                            continue;
                        }

                        int score = this.scorePosition(mutable);
                        if (score < MIN_TARGET_SCORE) {
                            continue;
                        }

                        double distance = mutable.distSqr(origin);
                        if (score > bestScore || score == bestScore && distance < bestDistance) {
                            bestScore = score;
                            bestDistance = distance;
                            bestPos = mutable.immutable();
                        }
                    }
                }
            }

            return bestPos;
        }

        private int scorePosition(BlockPos pos) {
            int nearbyWater = 0;

            for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 1, 2))) {
                if (this.beaver.level().getFluidState(checkPos).is(FluidTags.WATER)) {
                    nearbyWater++;
                }
            }

            int skyLight = this.beaver.level().getBrightness(LightLayer.SKY, pos);
            int blockLight = this.beaver.level().getBrightness(LightLayer.BLOCK, pos);
            return nearbyWater * 2 - skyLight - blockLight;
        }

        private boolean isStandableNearWater(BlockPos pos) {
            return this.beaver.level().getBlockState(pos).isAir()
                    && this.beaver.level().getBlockState(pos.above()).isAir()
                    && this.beaver.level().getBlockState(pos.below()).isSolid()
                    && this.hasAdjacentWater(pos);
        }

        private boolean hasAdjacentWater(BlockPos pos) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (this.beaver.level().getFluidState(pos.relative(direction)).is(FluidTags.WATER)) {
                    return true;
                }
            }
            return this.beaver.level().getFluidState(pos.below()).is(FluidTags.WATER);
        }
    }

    public static class BeaverPlaceDamGoal extends Goal {
        private static final int MIN_DAM_COOLDOWN = 24000;
        private static final int MAX_DAM_COOLDOWN = 48000;
        private static final int SEARCH_RADIUS = 8;

        private final BeaverEntity beaver;
        private final double speedModifier;

        @Nullable
        private BlockPos targetPos;

        public BeaverPlaceDamGoal(BeaverEntity beaver, double speedModifier) {
            this.beaver = beaver;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.beaver.isGnawing() || !this.beaver.hasRawLogs() || this.beaver.getDamCooldownTicks() > 0) {
                return false;
            }

            this.targetPos = this.findDamSpot();
            return this.targetPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPos != null && !this.beaver.isGnawing();
        }

        @Override
        public void start() {
            if (this.targetPos != null) {
                this.beaver.getNavigation().moveTo(this.targetPos.getX() + 0.5D, this.targetPos.getY(), this.targetPos.getZ() + 0.5D, this.speedModifier);
            }
        }

        @Override
        public void stop() {
            this.targetPos = null;
            this.beaver.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (this.targetPos == null) {
                return;
            }

            this.beaver.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D);

            if (!this.targetPos.closerToCenterThan(this.beaver.position(), 2.0D)) {
                return;
            }

            if (!(this.beaver.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            if (!this.isValidDamSpot(this.targetPos)) {
                this.stop();
                return;
            }

            ItemStack rawLog = this.beaver.takeOneRawLog();
            if (rawLog.isEmpty()) {
                this.stop();
                return;
            }

            BlockState damState = ObjectRegistry.BEAVER_DAM.get().defaultBlockState();
            if (damState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                damState = damState.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
            }

            serverLevel.setBlock(this.targetPos, damState, Block.UPDATE_ALL);
            serverLevel.playSound(null, this.targetPos, damState.getSoundType().getPlaceSound(), this.beaver.getSoundSource(), 1.0F, 0.9F + this.beaver.getRandom().nextFloat() * 0.2F);
            this.beaver.startNodding();
            this.beaver.setDamCooldownTicks(MIN_DAM_COOLDOWN + this.beaver.getRandom().nextInt(MAX_DAM_COOLDOWN - MIN_DAM_COOLDOWN + 1));
            this.stop();
        }

        @Nullable
        private BlockPos findDamSpot() {
            BlockPos origin = this.beaver.blockPosition();
            BlockPos bestPos = null;
            double bestDistance = Double.MAX_VALUE;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int xOffset = -SEARCH_RADIUS; xOffset <= SEARCH_RADIUS; xOffset++) {
                for (int zOffset = -SEARCH_RADIUS; zOffset <= SEARCH_RADIUS; zOffset++) {
                    for (int yOffset = -2; yOffset <= 2; yOffset++) {
                        mutable.set(origin.getX() + xOffset, origin.getY() + yOffset, origin.getZ() + zOffset);
                        if (!this.isValidDamSpot(mutable)) {
                            continue;
                        }

                        double distance = mutable.distSqr(origin);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            bestPos = mutable.immutable();
                        }
                    }
                }
            }

            return bestPos;
        }

        private boolean isValidDamSpot(BlockPos pos) {
            if (!this.beaver.level().getFluidState(pos).is(FluidTags.WATER)) {
                return false;
            }

            if (!this.beaver.level().getBlockState(pos).canBeReplaced()) {
                return false;
            }

            int nearbyDamBlocks = 0;
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockState adjacentState = this.beaver.level().getBlockState(pos.relative(direction));
                if (adjacentState.is(ObjectRegistry.BEAVER_DAM.get())) {
                    nearbyDamBlocks++;
                }
            }

            return nearbyDamBlocks < 3;
        }
    }
}