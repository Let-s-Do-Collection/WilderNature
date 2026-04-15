package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.HazelnutBushBlock;
import net.satisfy.wildernature.core.entity.animal.neutral.SquirrelEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class SquirrelGoals {

    public static class SquirrelGiftTriggerGoal extends Goal {
        private final SquirrelEntity squirrel;

        public SquirrelGiftTriggerGoal(SquirrelEntity squirrel) {
            this.squirrel = squirrel;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.squirrel.hasMaximumTrust()) {
                return false;
            }

            if (this.squirrel.level().isNight()) {
                return false;
            }

            if (this.squirrel.isSheltering() || this.squirrel.isDeliveringGift() || this.squirrel.isWiggling() || this.squirrel.isBaby() || this.squirrel.isPanicking() || this.squirrel.isForaging()) {
                return false;
            }

            if (this.squirrel.getGiftCooldownTicks() > 0 || this.squirrel.getGiftFailCooldownTicks() > 0) {
                return false;
            }

            if (!this.squirrel.getMainHandItem().isEmpty() || !this.squirrel.getPendingGiftStack().isEmpty()) {
                return false;
            }

            if (this.squirrel.getRandom().nextInt(SquirrelEntity.GIFT_TRIGGER_CHANCE) != 0) {
                return false;
            }

            if (!this.squirrel.tryTriggerGift()) {
                this.squirrel.startGiftFailCooldown();
                return false;
            }

            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    public static class SquirrelDeliverGiftGoal extends Goal {
        private final SquirrelEntity squirrel;
        private Player targetPlayer;
        private boolean giftDropped;

        public SquirrelDeliverGiftGoal(SquirrelEntity squirrel) {
            this.squirrel = squirrel;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return !this.squirrel.getPendingGiftStack().isEmpty() && this.squirrel.getGiftTargetPlayerUuid() != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.targetPlayer == null || !this.targetPlayer.isAlive() || this.targetPlayer.isSpectator()) {
                return false;
            }

            if (this.squirrel.getMainHandItem().isEmpty()) {
                return false;
            }

            return this.squirrel.distanceToSqr(this.targetPlayer) <= SquirrelEntity.GIFT_RANGE * SquirrelEntity.GIFT_RANGE;
        }

        @Override
        public void start() {
            this.giftDropped = false;
            this.squirrel.setDeliveringGift(true);
            this.squirrel.setGiftWiggleTicks(0);

            UUID targetPlayerUuid = this.squirrel.getGiftTargetPlayerUuid();
            this.targetPlayer = targetPlayerUuid != null ? this.squirrel.level().getPlayerByUUID(targetPlayerUuid) : null;

            if (!this.squirrel.getPendingGiftStack().isEmpty()) {
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, this.squirrel.getPendingGiftStack().copy());
            }
        }

        @Override
        public void stop() {
            if (!this.giftDropped) {
                ItemStack giftStack = this.squirrel.getMainHandItem().copy();
                if (!giftStack.isEmpty()) {
                    this.squirrel.dropGift(giftStack);
                }
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.squirrel.startGiftFailCooldown();
            }

            this.squirrel.setPendingGiftStack(ItemStack.EMPTY);
            this.squirrel.getNavigation().stop();
            this.squirrel.setDeliveringGift(false);
            this.squirrel.setGiftWiggleTicks(0);
            this.squirrel.setGiftTargetPlayerUuid(null);
            this.targetPlayer = null;
            this.giftDropped = false;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            double distanceToPlayer = this.squirrel.distanceToSqr(this.targetPlayer);

            if (distanceToPlayer > SquirrelEntity.GIFT_STOP_DISTANCE * SquirrelEntity.GIFT_STOP_DISTANCE && this.squirrel.getGiftWiggleTicks() <= 0) {
                this.squirrel.getNavigation().moveTo(this.targetPlayer, 1.1D);
                return;
            }

            this.squirrel.getNavigation().stop();
            this.squirrel.lookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.squirrel.getGiftWiggleTicks() <= 0) {
                this.squirrel.setGiftWiggleTicks(SquirrelEntity.GIFT_WIGGLE_DURATION);
                this.squirrel.startWiggle(SquirrelEntity.GIFT_WIGGLE_DURATION);
                return;
            }

            this.squirrel.setGiftWiggleTicks(this.squirrel.getGiftWiggleTicks() - 1);

            if (this.squirrel.getGiftWiggleTicks() > 0) {
                return;
            }

            ItemStack giftStack = this.squirrel.getMainHandItem().copy();
            if (!giftStack.isEmpty()) {
                this.squirrel.dropGift(giftStack);
                this.squirrel.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.squirrel.setPendingGiftStack(ItemStack.EMPTY);
                this.squirrel.startGiftCooldown();
                this.giftDropped = true;
            }

            this.squirrel.setDeliveringGift(false);
            this.squirrel.setGiftTargetPlayerUuid(null);
        }
    }

    public static class SquirrelForageGoal extends Goal {
        private static final int SEARCH_COOLDOWN_MIN = 80;
        private static final int SEARCH_COOLDOWN_MAX = 140;

        private final SquirrelEntity squirrel;
        private final double speedModifier;
        private BlockPos targetBushPos;
        private ItemEntity targetItemEntity;
        private int searchCooldownTicks;
        private int harvestDelayTicks;

        public SquirrelForageGoal(SquirrelEntity squirrel, double speedModifier) {
            this.squirrel = squirrel;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.squirrel.isPanicking() || this.squirrel.isWiggling() || this.squirrel.isDeliveringGift() || this.squirrel.isSheltering() || this.squirrel.isBaby() || this.squirrel.isForaging()) {
                return false;
            }

            if (this.squirrel.level().isNight()) {
                return false;
            }

            if (this.squirrel.getForageCooldownTicks() > 0 || !this.squirrel.hasFreeInventorySlot()) {
                return false;
            }

            if (this.targetItemEntity != null && this.targetItemEntity.isAlive() && this.isValidForageItem(this.targetItemEntity)) {
                return true;
            }

            if (this.targetBushPos != null && this.isValidBush(this.targetBushPos)) {
                return true;
            }

            if (this.searchCooldownTicks > 0) {
                this.searchCooldownTicks--;
                return false;
            }

            this.targetItemEntity = this.findNearbyItem();
            if (this.targetItemEntity != null) {
                this.targetBushPos = null;
                this.searchCooldownTicks = this.getNextSearchCooldown();
                return true;
            }

            this.targetBushPos = this.findNearestBush();
            this.searchCooldownTicks = this.getNextSearchCooldown();
            return this.targetBushPos != null;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.squirrel.isPanicking() || this.squirrel.level().isNight() || !this.squirrel.hasFreeInventorySlot()) {
                return false;
            }

            if (this.targetItemEntity != null) {
                return this.targetItemEntity.isAlive() && this.isValidForageItem(this.targetItemEntity);
            }

            return this.targetBushPos != null && this.isValidBush(this.targetBushPos);
        }

        @Override
        public void start() {
            this.squirrel.setForaging(true);
            this.harvestDelayTicks = 0;
        }

        @Override
        public void stop() {
            this.squirrel.setForaging(false);
            this.squirrel.getNavigation().stop();
            this.targetBushPos = null;
            this.targetItemEntity = null;
            this.harvestDelayTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetItemEntity != null && this.targetItemEntity.isAlive() && this.isValidForageItem(this.targetItemEntity)) {
                this.squirrel.getNavigation().moveTo(this.targetItemEntity, this.speedModifier);

                if (this.squirrel.distanceToSqr(this.targetItemEntity) < 2.0D) {
                    ItemStack itemStack = this.targetItemEntity.getItem();
                    if (!itemStack.isEmpty() && this.squirrel.canForageItem(itemStack) && this.squirrel.tryStoreSingleItem(itemStack)) {
                        this.squirrel.startForageCooldown();
                    }

                    if (itemStack.isEmpty()) {
                        this.targetItemEntity.discard();
                    }

                    this.targetItemEntity = null;
                }
                return;
            }

            if (this.targetBushPos == null) {
                return;
            }

            if (!this.targetBushPos.closerToCenterThan(this.squirrel.position(), 1.8D)) {
                this.squirrel.getNavigation().moveTo(this.targetBushPos.getX() + 0.5D, this.targetBushPos.getY(), this.targetBushPos.getZ() + 0.5D, this.speedModifier);
                return;
            }

            this.squirrel.getNavigation().stop();
            this.squirrel.getLookControl().setLookAt(this.targetBushPos.getX() + 0.5D, this.targetBushPos.getY(), this.targetBushPos.getZ() + 0.5D);

            if (this.harvestDelayTicks <= 0) {
                this.harvestDelayTicks = SquirrelEntity.FORAGE_WIGGLE_DURATION;
                this.squirrel.startWiggle(SquirrelEntity.FORAGE_WIGGLE_DURATION);
                return;
            }

            this.harvestDelayTicks--;

            if (this.harvestDelayTicks > 0) {
                return;
            }

            if (this.isValidBush(this.targetBushPos) && this.squirrel.level() instanceof ServerLevel serverLevel) {
                this.squirrel.harvestHazelnutBush(serverLevel, this.targetBushPos, this.squirrel.level().getBlockState(this.targetBushPos));
                this.squirrel.startForageCooldown();
            }

            this.targetBushPos = null;
        }

        private int getNextSearchCooldown() {
            return SEARCH_COOLDOWN_MIN + this.squirrel.getRandom().nextInt(SEARCH_COOLDOWN_MAX - SEARCH_COOLDOWN_MIN + 1);
        }

        @Nullable
        private ItemEntity findNearbyItem() {
            List<ItemEntity> nearbyItemEntities = this.squirrel.level().getEntitiesOfClass(ItemEntity.class, this.squirrel.getBoundingBox().inflate(SquirrelEntity.FORAGE_ITEM_SEARCH_RANGE), this::isValidForageItem);
            if (nearbyItemEntities.isEmpty()) {
                return null;
            }

            return nearbyItemEntities.stream().min(Comparator.comparingDouble(this.squirrel::distanceToSqr)).orElse(null);
        }

        private boolean isValidForageItem(ItemEntity itemEntity) {
            return itemEntity.isAlive() && this.squirrel.canForageItem(itemEntity.getItem());
        }

        @Nullable
        private BlockPos findNearestBush() {
            BlockPos originPos = this.squirrel.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestBushPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -SquirrelEntity.FORAGE_BUSH_SEARCH_RANGE; offsetX <= SquirrelEntity.FORAGE_BUSH_SEARCH_RANGE; offsetX++) {
                for (int offsetY = -3; offsetY <= 3; offsetY++) {
                    for (int offsetZ = -SquirrelEntity.FORAGE_BUSH_SEARCH_RANGE; offsetZ <= SquirrelEntity.FORAGE_BUSH_SEARCH_RANGE; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.isValidBush(mutableBlockPos)) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestBushPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestBushPos;
        }

        private boolean isValidBush(BlockPos blockPos) {
            BlockState blockState = this.squirrel.level().getBlockState(blockPos);
            return blockState.getBlock() instanceof HazelnutBushBlock && blockState.hasProperty(HazelnutBushBlock.AGE) && blockState.getValue(HazelnutBushBlock.AGE) >= 2;
        }
    }
}