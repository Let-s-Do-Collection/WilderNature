package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.satisfy.wildernature.core.block.entity.BurrowBlockEntity;
import net.satisfy.wildernature.core.entity.animal.neutral.RaccoonEntity;
import net.satisfy.wildernature.core.entity.animal.tameable.DogEntity;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.ParticleTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

public class DogGoals {
    public static class GoAfterCatGoal extends Goal {
        private static final int CAT_SEARCH_INTERVAL = 20;
        private static final double CAT_DETECTION_RANGE_SQR = 256.0D;

        private final DogEntity dog;
        private List<Cat> catList;
        private int lastCatUpdate;
        private Cat targetCat;
        private int barkCooldownTicks;
        private int sprintTicks;

        public GoAfterCatGoal(DogEntity dogEntity) {
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
            this.dog = dogEntity;
        }

        @Override
        public boolean canUse() {
            return !this.dog.isTame() && !this.dog.isOrderedToSit() && !this.dog.isResting() && !this.dog.isAttacking() && !this.dog.isFetching() && !this.dog.isDigging() && !this.getNearbyCats().isEmpty();
        }

        @Override
        public void start() {
            this.updateTargetCat();
            this.barkCooldownTicks = 0;
            this.sprintTicks = 0;
        }

        @Override
        public void tick() {
            this.updateTargetCat();
            if (this.targetCat == null) {
                return;
            }

            this.dog.getLookControl().setLookAt(this.targetCat, 30.0F, 30.0F);

            if (this.sprintTicks > 0) {
                this.sprintTicks--;
                this.dog.getNavigation().moveTo(this.targetCat, 1.8D);
            } else {
                this.dog.getNavigation().moveTo(this.targetCat, 1.5D);
                if (this.dog.getRandom().nextInt(40) == 0) {
                    this.sprintTicks = 14;
                }
            }

            if (this.barkCooldownTicks > 0) {
                this.barkCooldownTicks--;
            } else if (this.dog.getRandom().nextInt(45) == 0) {
                this.dog.level().playSound(null, this.dog, SoundEventRegistry.DOG_AMBIENT.get(), SoundSource.NEUTRAL, 0.8F, 1.15F);
                this.barkCooldownTicks = 30;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.dog.isTame() && !this.dog.isOrderedToSit() && !this.dog.isResting() && !this.dog.isAttacking() && !this.dog.isDigging() && this.targetCat != null && this.targetCat.isAlive() && this.targetCat.distanceToSqr(this.dog) <= CAT_DETECTION_RANGE_SQR;
        }

        @Override
        public void stop() {
            this.targetCat = null;
            this.barkCooldownTicks = 0;
            this.sprintTicks = 0;
            this.dog.getNavigation().stop();
        }

        private List<Cat> getNearbyCats() {
            if (this.catList == null || this.dog.tickCount - this.lastCatUpdate >= CAT_SEARCH_INTERVAL) {
                this.catList = this.dog.level().getNearbyEntities(Cat.class, TargetingConditions.forNonCombat(), this.dog, this.dog.getBoundingBox().inflate(16.0D));
                this.lastCatUpdate = this.dog.tickCount;
            }
            return this.catList;
        }

        private void updateTargetCat() {
            if (this.targetCat == null || this.targetCat.distanceToSqr(this.dog) > CAT_DETECTION_RANGE_SQR) {
                double closestDistance = Double.MAX_VALUE;
                Cat closestCat = null;

                for (Cat catEntity : this.getNearbyCats()) {
                    double distance = catEntity.distanceToSqr(this.dog);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestCat = catEntity;
                    }
                }
                this.targetCat = closestCat;
            }
        }
    }

    public static class CreeperAlertGoal extends Goal {
        private static final double ALERT_RANGE = 20.0D;
        private static final int ALERT_DURATION = 40;
        private static final int COOLDOWN_TICKS = 2400;

        private final DogEntity dog;
        @Nullable
        private Creeper targetCreeper;
        private int alertTicks;
        private int cooldownTicks;

        public CreeperAlertGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.cooldownTicks > 0) {
                this.cooldownTicks--;
                return false;
            }

            if (!this.dog.canGuard()) {
                return false;
            }

            if (!this.dog.canSearchForCreepers()) {
                return false;
            }

            this.dog.resetCreeperSearchCooldown();
            this.targetCreeper = this.findNearestCreeper();
            return this.targetCreeper != null;
        }

        @Override
        public void start() {
            this.alertTicks = ALERT_DURATION;
            this.dog.clearRestState();
            this.dog.setHowling(true);
            this.dog.level().playSound(null, this.dog, SoundEventRegistry.DOG_AMBIENT.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }

        @Override
        public void tick() {
            if (this.targetCreeper == null) {
                return;
            }

            this.dog.getLookControl().setLookAt(this.targetCreeper, 30.0F, 30.0F);
            this.alertTicks--;
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetCreeper != null
                    && this.targetCreeper.isAlive()
                    && this.dog.distanceToSqr(this.targetCreeper) <= ALERT_RANGE * ALERT_RANGE
                    && this.alertTicks > 0
                    && this.dog.canGuard();
        }

        @Override
        public void stop() {
            this.dog.setHowling(false);
            this.targetCreeper = null;
            this.alertTicks = 0;
            this.cooldownTicks = COOLDOWN_TICKS;
        }

        @Nullable
        private Creeper findNearestCreeper() {
            List<Creeper> nearbyCreepers = this.dog.level().getEntitiesOfClass(Creeper.class, this.dog.getBoundingBox().inflate(ALERT_RANGE));
            Creeper closest = null;
            double closestDistance = Double.MAX_VALUE;

            for (Creeper creeper : nearbyCreepers) {
                if (!creeper.isAlive()) {
                    continue;
                }

                double distance = this.dog.distanceToSqr(creeper);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = creeper;
                }
            }

            return closest;
        }
    }

    public static class SeekShelterWhenRainingGoal extends Goal {
        private static final int SEARCH_RADIUS = 10;

        private final DogEntity dog;
        private final double speedModifier;
        @Nullable
        private BlockPos shelterPos;

        public SeekShelterWhenRainingGoal(DogEntity dog, double speedModifier) {
            this.dog = dog;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.canAct()) {
                return false;
            }
            if (!this.dog.level().isRaining()) {
                return false;
            }
            if (this.dog.level().canSeeSky(this.dog.blockPosition())) {
                this.shelterPos = this.findShelterPos();
                return this.shelterPos != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return this.shelterPos != null && this.dog.level().isRaining() && this.dog.level().canSeeSky(this.dog.blockPosition()) && this.dog.canAct();
        }

        @Override
        public void start() {
            this.dog.clearRestState();
        }

        @Override
        public void tick() {
            if (this.shelterPos == null) {
                return;
            }
            this.dog.getNavigation().moveTo(this.shelterPos.getX() + 0.5D, this.shelterPos.getY(), this.shelterPos.getZ() + 0.5D, this.speedModifier);
        }

        @Override
        public void stop() {
            this.shelterPos = null;
        }

        @Nullable
        private BlockPos findShelterPos() {
            BlockPos originPos = this.dog.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestShelterPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -SEARCH_RADIUS; offsetX <= SEARCH_RADIUS; offsetX++) {
                for (int offsetY = -3; offsetY <= 3; offsetY++) {
                    for (int offsetZ = -SEARCH_RADIUS; offsetZ <= SEARCH_RADIUS; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);

                        if (!this.dog.level().isEmptyBlock(mutableBlockPos)) {
                            continue;
                        }
                        if (!this.dog.level().isEmptyBlock(mutableBlockPos.above())) {
                            continue;
                        }
                        if (this.dog.level().canSeeSky(mutableBlockPos)) {
                            continue;
                        }
                        if (!this.hasSolidSupport(mutableBlockPos.below())) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestShelterPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestShelterPos;
        }

        private boolean hasSolidSupport(BlockPos floorPos) {
            return this.dog.level().getBlockState(floorPos).isSolidRender(this.dog.level(), floorPos) || this.dog.level().getBlockState(floorPos).is(BlockTags.LEAVES) || this.dog.level().getBlockState(floorPos).is(BlockTags.LOGS);
        }
    }

    public static class RaccoonGuardGoal extends Goal {
        private static final double GUARD_RANGE = 14.0D;
        private static final double STOP_DISTANCE = 4.0D;

        private final DogEntity dog;
        @Nullable
        private RaccoonEntity targetRaccoon;
        private int barkCooldownTicks;

        public RaccoonGuardGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.isTame() || this.dog.isDigging()) {
                return false;
            }
            this.targetRaccoon = this.findNearestRaccoon();
            return this.targetRaccoon != null;
        }

        @Override
        public void start() {
            if (this.dog.isOrderedToSit()) {
                this.dog.returnSitPos = this.dog.blockPosition().immutable();
                this.dog.shouldReturnToSit = true;
                this.dog.setOrderedToSit(false);
            }
            this.dog.clearRestState();
            this.barkCooldownTicks = 30 + this.dog.getRandom().nextInt(40);
        }

        @Override
        public void tick() {
            if (this.targetRaccoon == null) {
                return;
            }

            this.dog.getLookControl().setLookAt(this.targetRaccoon, 30.0F, 30.0F);

            if (this.dog.distanceToSqr(this.targetRaccoon) > STOP_DISTANCE * STOP_DISTANCE) {
                this.dog.getNavigation().moveTo(this.targetRaccoon, 1.25D);
            } else {
                this.dog.getNavigation().stop();
                double pushX = this.targetRaccoon.getX() - this.dog.getX();
                double pushZ = this.targetRaccoon.getZ() - this.dog.getZ();
                double length = Math.sqrt(pushX * pushX + pushZ * pushZ);
                if (length > 1.0E-4D) {
                    this.targetRaccoon.setDeltaMovement(this.targetRaccoon.getDeltaMovement().add(pushX / length * 0.24D, 0.08D, pushZ / length * 0.24D));
                }
            }

            if (this.barkCooldownTicks > 0) {
                this.barkCooldownTicks--;
            } else {
                this.dog.level().playSound(null, this.dog, SoundEventRegistry.DOG_AMBIENT.get(), SoundSource.NEUTRAL, 0.8F, 1.1F);
                this.barkCooldownTicks = 55 + this.dog.getRandom().nextInt(50);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetRaccoon != null && this.targetRaccoon.isAlive() && this.dog.distanceToSqr(this.targetRaccoon) <= GUARD_RANGE * GUARD_RANGE && !this.dog.isDigging();
        }

        @Override
        public void stop() {
            this.targetRaccoon = null;
            this.barkCooldownTicks = 0;
            if (this.dog.getTarget() instanceof RaccoonEntity) {
                this.dog.setTarget(null);
            }
            this.dog.getNavigation().stop();
        }

        @Nullable
        private RaccoonEntity findNearestRaccoon() {
            List<RaccoonEntity> nearbyRaccoons = this.dog.level().getEntitiesOfClass(RaccoonEntity.class, this.dog.getBoundingBox().inflate(GUARD_RANGE));
            RaccoonEntity closestRaccoon = null;
            double closestDistance = Double.MAX_VALUE;

            for (RaccoonEntity nearbyRaccoon : nearbyRaccoons) {
                if (!nearbyRaccoon.isAlive()) {
                    continue;
                }
                double checkedDistance = this.dog.distanceToSqr(nearbyRaccoon);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestRaccoon = nearbyRaccoon;
                }
            }

            return closestRaccoon;
        }
    }

    public static class ReturnToSitGoal extends Goal {
        private final DogEntity dog;

        public ReturnToSitGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return this.dog.shouldReturnToSit && this.dog.returnSitPos != null && !this.dog.isOrderedToSit() && !this.dog.isAttacking() && !this.dog.isFetching() && !this.dog.isDigging() && this.dog.getTarget() == null;
        }

        @Override
        public void tick() {
            if (this.dog.returnSitPos == null) {
                return;
            }
            if (this.dog.returnSitPos.closerToCenterThan(this.dog.position(), 1.5D)) {
                this.dog.getNavigation().stop();
                this.dog.setOrderedToSit(true);
                this.dog.shouldReturnToSit = false;
                return;
            }
            this.dog.getNavigation().moveTo(this.dog.returnSitPos.getX() + 0.5D, this.dog.returnSitPos.getY(), this.dog.returnSitPos.getZ() + 0.5D, 1.15D);
        }

        @Override
        public boolean canContinueToUse() {
            return this.canUse();
        }

        @Override
        public void stop() {
            if (!this.dog.shouldReturnToSit) {
                this.dog.returnSitPos = null;
            }
        }
    }

    public static class DogCollectBoneGoal extends Goal {
        private static final double SEARCH_RANGE = 10.0D;

        private final DogEntity dog;
        @Nullable
        private ItemEntity targetItemEntity;

        public DogCollectBoneGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.canUseBoneCollectGoal()) {
                return false;
            }
            if (!this.dog.canSearchForBones()) {
                return false;
            }

            this.targetItemEntity = this.findNearestBone();
            this.dog.resetBoneSearchCooldown();
            return this.targetItemEntity != null;
        }

        @Override
        public void start() {
            this.dog.startJob();
            this.dog.setFetching(true);
        }

        @Override
        public void tick() {
            if (this.targetItemEntity == null) {
                return;
            }

            this.dog.getNavigation().moveTo(this.targetItemEntity, 1.2D);
            this.dog.getLookControl().setLookAt(this.targetItemEntity, 30.0F, 30.0F);

            if (this.dog.distanceToSqr(this.targetItemEntity) <= 2.0D) {
                ItemStack foundStack = this.targetItemEntity.getItem();
                if (!foundStack.isEmpty() && foundStack.is(Items.BONE) && this.dog.tryStoreItem(foundStack)) {
                    if (foundStack.isEmpty()) {
                        this.targetItemEntity.discard();
                    } else {
                        this.targetItemEntity.setItem(foundStack);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetItemEntity != null
                    && this.targetItemEntity.isAlive()
                    && this.targetItemEntity.getItem().is(Items.BONE)
                    && this.dog.hasFreeInventorySlot()
                    && this.dog.canAct();
        }

        @Override
        public void stop() {
            this.targetItemEntity = null;
            this.dog.stopJob();
        }

        @Nullable
        private ItemEntity findNearestBone() {
            List<ItemEntity> nearbyItems = this.dog.level().getEntitiesOfClass(ItemEntity.class, this.dog.getBoundingBox().inflate(SEARCH_RANGE));
            ItemEntity closestItem = null;
            double closestDistance = Double.MAX_VALUE;

            for (ItemEntity nearbyItem : nearbyItems) {
                if (!nearbyItem.isAlive()) {
                    continue;
                }
                if (!nearbyItem.onGround()) {
                    continue;
                }
                if (nearbyItem.hasPickUpDelay()) {
                    continue;
                }
                if (!nearbyItem.getItem().is(Items.BONE)) {
                    continue;
                }
                double checkedDistance = this.dog.distanceToSqr(nearbyItem);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestItem = nearbyItem;
                }
            }

            return closestItem;
        }
    }

    public static class DogDigBurrowGoal extends Goal {
        private static final int SEARCH_RADIUS = 8;
        private static final int DIG_TICKS = 32;

        private final DogEntity dog;
        @Nullable
        private BlockPos targetPos;
        private int digTicks;

        public DogDigBurrowGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.canUseBurrowGoal()) {
                return false;
            }
            if (!this.dog.hasBoneInInventory()) {
                return false;
            }

            if (this.dog.hasBurrow() && this.dog.hasValidBurrow()) {
                return false;
            }

            if (!this.dog.canSearchForBurrow()) {
                return false;
            }

            BlockPos existingBurrowPos = this.dog.findNearbyBurrow(SEARCH_RADIUS);
            this.dog.resetBurrowSearchCooldown();

            if (existingBurrowPos != null) {
                this.dog.setBurrowPos(existingBurrowPos);
                return false;
            }

            this.targetPos = this.findTargetPos();
            return this.targetPos != null;
        }

        @Override
        public void start() {
            this.dog.startJob();
            this.dog.setDigging(true);
            this.digTicks = 0;
        }

        @Override
        public void tick() {
            if (this.targetPos == null) {
                return;
            }

            BlockPos standPos = this.targetPos.above();

            if (!standPos.closerToCenterThan(this.dog.position(), 1.5D)) {
                this.dog.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY() + 0.5D, standPos.getZ() + 0.5D, 1.15D);
                return;
            }

            this.dog.getNavigation().stop();
            this.dog.setDeltaMovement(0.0D, this.dog.getDeltaMovement().y, 0.0D);
            this.dog.xxa = 0.0F;
            this.dog.zza = 0.0F;
            this.dog.getLookControl().setLookAt(this.targetPos.getX() + 0.5D, this.targetPos.getY() + 0.5D, this.targetPos.getZ() + 0.5D);
            this.digTicks++;

            this.dog.spawnDigParticles(this.targetPos);

            if (this.digTicks % 4 == 0) {
                this.dog.level().playSound(null, this.targetPos, SoundEvents.GRASS_BREAK, SoundSource.NEUTRAL, 0.55F, 0.9F);
            }

            if (this.digTicks >= DIG_TICKS) {
                this.dog.level().setBlock(this.targetPos, ObjectRegistry.BURROW.get().defaultBlockState(), 3);
                this.dog.setBurrowPos(this.targetPos.immutable());
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPos != null
                    && !this.dog.hasBurrow()
                    && this.dog.hasBoneInInventory()
                    && !this.dog.isOrderedToSit()
                    && !this.dog.isResting()
                    && !this.dog.isAttacking()
                    && !this.dog.isFetching()
                    && !this.dog.isPanicking()
                    && this.dog.getTarget() == null;
        }

        @Override
        public void stop() {
            this.targetPos = null;
            this.digTicks = 0;
            this.dog.stopJob();
        }

        @Nullable
        private BlockPos findTargetPos() {
            BlockPos originPos = this.dog.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            BlockPos closestPos = null;
            double closestDistance = Double.MAX_VALUE;

            for (int offsetX = -SEARCH_RADIUS; offsetX <= SEARCH_RADIUS; offsetX++) {
                for (int offsetY = -2; offsetY <= 2; offsetY++) {
                    for (int offsetZ = -SEARCH_RADIUS; offsetZ <= SEARCH_RADIUS; offsetZ++) {
                        mutableBlockPos.set(originPos.getX() + offsetX, originPos.getY() + offsetY, originPos.getZ() + offsetZ);
                        if (!this.dog.level().getBlockState(mutableBlockPos).is(Blocks.GRASS_BLOCK)) {
                            continue;
                        }
                        if (!this.dog.level().isEmptyBlock(mutableBlockPos.above())) {
                            continue;
                        }

                        double checkedDistance = mutableBlockPos.distSqr(originPos);
                        if (checkedDistance < closestDistance) {
                            closestDistance = checkedDistance;
                            closestPos = mutableBlockPos.immutable();
                        }
                    }
                }
            }

            return closestPos;
        }
    }

    public static class DogStoreInBurrowGoal extends Goal {
        private static final int STORE_TICKS = 20;

        private final DogEntity dog;
        private int storeTicks;

        public DogStoreInBurrowGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.isTame()) {
                return false;
            }
            if (!this.dog.hasValidBurrow()) {
                return false;
            }
            if (!this.dog.canAct()) {
                return false;
            }
            return this.dog.getInventoryItemCount() > 0;
        }

        @Override
        public void start() {
            this.dog.startJob();
            this.dog.setDigging(true);
            this.storeTicks = 0;
        }

        @Override
        public void tick() {
            if (!this.dog.hasValidBurrow()) {
                this.stop();
                return;
            }

            BlockPos burrowPos = this.dog.getBurrowPos();
            if (burrowPos == null) {
                return;
            }

            this.dog.getNavigation().moveTo(burrowPos.getX() + 0.5D, burrowPos.getY(), burrowPos.getZ() + 0.5D, 1.1D);

            if (!burrowPos.closerToCenterThan(this.dog.position(), 1.5D)) {
                return;
            }

            this.dog.getNavigation().stop();
            this.dog.setDeltaMovement(0.0D, this.dog.getDeltaMovement().y, 0.0D);
            this.dog.xxa = 0.0F;
            this.dog.zza = 0.0F;
            this.dog.getLookControl().setLookAt(burrowPos.getX() + 0.5D, burrowPos.getY() + 0.5D, burrowPos.getZ() + 0.5D);
            this.dog.spawnDigParticles(burrowPos);
            this.storeTicks++;

            if (this.storeTicks % 4 == 0) {
                this.dog.level().playSound(null, burrowPos, SoundEvents.GRAVEL_PLACE, SoundSource.NEUTRAL, 0.6F, 0.8F);
            }

            if (this.storeTicks < STORE_TICKS) {
                return;
            }

            BlockEntity blockEntity = this.dog.level().getBlockEntity(burrowPos);
            if (!(blockEntity instanceof BurrowBlockEntity burrowBlockEntity)) {
                this.stop();
                return;
            }

            for (int slotIndex = 0; slotIndex < this.dog.getInventorySize(); slotIndex++) {
                ItemStack stack = this.dog.getInventoryItem(slotIndex);
                if (stack.isEmpty()) {
                    continue;
                }

                ItemStack stackToInsert = stack.copyWithCount(1);
                if (burrowBlockEntity.tryAddItem(stackToInsert)) {
                    stack.shrink(1);
                    this.dog.setBurrowStoredBones(this.dog.getBurrowStoredBones() + 1);
                    if (stack.isEmpty()) {
                        this.dog.setInventoryItem(slotIndex, ItemStack.EMPTY);
                    } else {
                        this.dog.setInventoryItem(slotIndex, stack);
                    }
                    this.stop();
                    return;
                }
            }

            if (this.dog.getOwner() != null) {
                this.dog.triggerOwnerDelivery(this.dog.blockPosition());
            }

            this.stop();
        }

        @Override
        public boolean canContinueToUse() {
            return this.dog.hasValidBurrow()
                    && this.dog.getInventoryItemCount() > 0
                    && this.dog.canAct()
                    && this.storeTicks < STORE_TICKS + 40;
        }

        @Override
        public void stop() {
            this.storeTicks = 0;
            this.dog.stopJob();
        }
    }

    public static class DeliverSkeletonLootGoal extends Goal {
        private static final double SEARCH_RANGE = 8.0D;
        private static final double DROP_RANGE = 2.25D;

        private final DogEntity dog;
        @Nullable
        private ItemEntity targetBone;

        public DeliverSkeletonLootGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.canDeliverSkeletonLoot()) {
                return false;
            }
            if (!this.dog.getMainHandItem().isEmpty()) {
                return true;
            }
            this.targetBone = this.findNearestBone();
            return this.targetBone != null;
        }

        @Override
        public void start() {
            this.dog.clearRestState();
            this.dog.setFetching(true);
        }

        @Override
        public void tick() {
            if (this.dog.getMainHandItem().isEmpty()) {
                if (this.targetBone == null || !this.targetBone.isAlive()) {
                    return;
                }

                this.dog.getNavigation().moveTo(this.targetBone, 1.25D);
                this.dog.getLookControl().setLookAt(this.targetBone, 30.0F, 30.0F);

                if (this.dog.distanceToSqr(this.targetBone) <= 2.0D) {
                    ItemStack foundStack = this.targetBone.getItem();
                    if (foundStack.is(Items.BONE) && !foundStack.isEmpty()) {
                        ItemStack carriedStack = foundStack.split(1);
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, carriedStack);
                        if (foundStack.isEmpty()) {
                            this.targetBone.discard();
                        } else {
                            this.targetBone.setItem(foundStack);
                        }
                    }
                }
                return;
            }

            this.dog.spawnCarryParticles();

            if (this.dog.shouldDeliverSkeletonLootToOwner() && this.dog.getOwner() != null) {
                this.dog.getNavigation().moveTo(this.dog.getOwner(), 1.25D);
                this.dog.getLookControl().setLookAt(this.dog.getOwner(), 30.0F, 30.0F);

                if (this.dog.distanceToSqr(this.dog.getOwner()) <= DROP_RANGE * DROP_RANGE) {
                    this.dropCarriedItem(this.dog.getOwner().blockPosition().above());
                    this.dog.clearSkeletonDelivery();
                }
                return;
            }

            if (!this.dog.hasValidBurrow()) {
                this.dog.clearSkeletonDelivery();
                return;
            }

            BlockPos burrowPos = this.dog.getBurrowPos();
            if (burrowPos == null) {
                this.dog.clearSkeletonDelivery();
                return;
            }

            BlockPos standPos = burrowPos.above();
            this.dog.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY() + 0.5D, standPos.getZ() + 0.5D, 1.25D);
            this.dog.getLookControl().setLookAt(burrowPos.getX() + 0.5D, burrowPos.getY() + 0.5D, burrowPos.getZ() + 0.5D);

            if (!standPos.closerToCenterThan(this.dog.position(), 1.5D)) {
                return;
            }

            BlockEntity blockEntity = this.dog.level().getBlockEntity(burrowPos);
            if (!(blockEntity instanceof BurrowBlockEntity burrowBlockEntity)) {
                this.dog.clearSkeletonDelivery();
                return;
            }

            ItemStack carriedStack = this.dog.getMainHandItem();
            if (carriedStack.is(Items.BONE) && !carriedStack.isEmpty()) {
                ItemStack stackToInsert = carriedStack.copyWithCount(1);
                if (burrowBlockEntity.tryAddItem(stackToInsert)) {
                    carriedStack.shrink(1);
                    this.dog.setBurrowStoredBones(this.dog.getBurrowStoredBones() + 1);
                    if (carriedStack.isEmpty()) {
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, carriedStack);
                    }
                    this.dog.clearSkeletonDelivery();
                    return;
                }
            }

            this.dropCarriedItem(standPos);
            this.dog.clearSkeletonDelivery();
        }

        @Override
        public boolean canContinueToUse() {
            return this.dog.canDeliverSkeletonLoot() && ((!this.dog.getMainHandItem().isEmpty()) || (this.targetBone != null && this.targetBone.isAlive()));
        }

        @Override
        public void stop() {
            this.targetBone = null;
            this.dog.setFetching(false);
        }

        @Nullable
        private ItemEntity findNearestBone() {
            BlockPos originPos = this.dog.getSkeletonDeliveryOrigin();
            if (originPos == null) {
                return null;
            }

            List<ItemEntity> nearbyItems = this.dog.level().getEntitiesOfClass(ItemEntity.class, this.dog.getBoundingBox().inflate(SEARCH_RANGE));
            ItemEntity closestBone = null;
            double closestDistance = Double.MAX_VALUE;

            for (ItemEntity nearbyItem : nearbyItems) {
                if (!nearbyItem.isAlive()) {
                    continue;
                }
                if (!nearbyItem.getItem().is(Items.BONE)) {
                    continue;
                }
                double checkedDistance = nearbyItem.blockPosition().distSqr(originPos);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestBone = nearbyItem;
                }
            }

            return closestBone;
        }

        private void dropCarriedItem(BlockPos dropPos) {
            ItemStack carriedStack = this.dog.getMainHandItem().copy();
            this.dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!carriedStack.isEmpty()) {
                ItemEntity droppedItem = new ItemEntity(this.dog.level(), dropPos.getX() + 0.5D, dropPos.getY() + 0.3D, dropPos.getZ() + 0.5D, carriedStack);
                this.dog.level().addFreshEntity(droppedItem);
            }
        }
    }

    public static class FetchThrownBoneGoal extends Goal {
        private static final double SEARCH_RANGE = 16.0D;
        private static final double PICKUP_RANGE = 4.0D;
        private static final double DROP_RANGE = 2.25D;

        private final DogEntity dog;
        @Nullable
        private ItemEntity targetBone;
        private int lostTargetTicks;
        private boolean returnToOwner;

        public FetchThrownBoneGoal(DogEntity dog) {
            this.dog = dog;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.dog.isTame()) return false;
            if (this.dog.isOrderedToSit() || this.dog.isResting() || this.dog.isAttacking() || this.dog.isFetching() || this.dog.isDigging() || this.dog.isPanicking()) return false;
            if (this.dog.getOwner() == null) return false;
            if (this.dog.distanceToSqr(this.dog.getOwner()) > 144.0D) return false;
            if (!this.dog.getMainHandItem().isEmpty()) return false;
            this.targetBone = this.findNearestThrownBone();
            return this.targetBone != null;
        }

        @Override
        public void start() {
            this.dog.setFetching(true);
            this.lostTargetTicks = 0;
            this.returnToOwner = !this.dog.hasValidBurrow() || this.dog.getRandom().nextFloat() < 0.65F;
            this.spawnAlertParticles();
        }

        @Override
        public void tick() {
            if (this.dog.getMainHandItem().isEmpty()) {
                if (this.targetBone == null || !this.targetBone.isAlive()) {
                    this.targetBone = this.findNearestThrownBone();
                    this.lostTargetTicks++;

                    if (this.targetBone == null) {
                        if (this.lostTargetTicks % 20 == 0) {
                            this.spawnQuestionParticles();
                        }
                        this.dog.getNavigation().stop();
                        return;
                    }

                    this.lostTargetTicks = 0;
                }

                this.dog.getNavigation().moveTo(this.targetBone, 1.4D);
                this.dog.getLookControl().setLookAt(this.targetBone, 30.0F, 30.0F);

                if (this.dog.distanceToSqr(this.targetBone) <= PICKUP_RANGE) {
                    this.dog.getNavigation().stop();
                    ItemStack targetStack = this.targetBone.getItem();
                    if (!targetStack.isEmpty() && targetStack.is(Items.BONE)) {
                        ItemStack takenStack = targetStack.split(1);
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, takenStack);
                        if (targetStack.isEmpty()) {
                            this.targetBone.discard();
                        } else {
                            this.targetBone.setItem(targetStack);
                        }
                        this.returnToOwner = !this.dog.hasValidBurrow() || this.dog.getRandom().nextFloat() < 0.65F;
                    }
                }
                return;
            }

            this.dog.spawnCarryParticles();

            if (this.returnToOwner && this.dog.getOwner() != null) {
                this.dog.getNavigation().moveTo(this.dog.getOwner(), 1.3D);
                this.dog.getLookControl().setLookAt(this.dog.getOwner(), 30.0F, 30.0F);

                if (this.dog.distanceToSqr(this.dog.getOwner()) <= DROP_RANGE * DROP_RANGE) {
                    this.dropCarriedItem(this.dog.getOwner().blockPosition().above());
                    this.targetBone = null;
                }
                return;
            }

            if (!this.dog.hasValidBurrow()) {
                if (this.dog.getOwner() != null) {
                    this.dog.getNavigation().moveTo(this.dog.getOwner(), 1.3D);
                    this.dog.getLookControl().setLookAt(this.dog.getOwner(), 30.0F, 30.0F);

                    if (this.dog.distanceToSqr(this.dog.getOwner()) <= DROP_RANGE * DROP_RANGE) {
                        this.dropCarriedItem(this.dog.getOwner().blockPosition().above());
                        this.targetBone = null;
                    }
                }
                return;
            }

            BlockPos burrowPos = this.dog.getBurrowPos();
            if (burrowPos == null) {
                if (this.dog.getOwner() != null) {
                    this.dog.getNavigation().moveTo(this.dog.getOwner(), 1.3D);
                    this.dog.getLookControl().setLookAt(this.dog.getOwner(), 30.0F, 30.0F);

                    if (this.dog.distanceToSqr(this.dog.getOwner()) <= DROP_RANGE * DROP_RANGE) {
                        this.dropCarriedItem(this.dog.getOwner().blockPosition().above());
                        this.targetBone = null;
                    }
                }
                return;
            }

            BlockPos standPos = burrowPos.above();
            this.dog.getNavigation().moveTo(standPos.getX() + 0.5D, standPos.getY() + 0.5D, standPos.getZ() + 0.5D, 1.25D);
            this.dog.getLookControl().setLookAt(burrowPos.getX() + 0.5D, burrowPos.getY() + 0.5D, burrowPos.getZ() + 0.5D);

            if (!standPos.closerToCenterThan(this.dog.position(), 1.5D)) return;

            BlockEntity blockEntity = this.dog.level().getBlockEntity(burrowPos);
            if (!(blockEntity instanceof BurrowBlockEntity burrowBlockEntity)) {
                if (this.dog.getOwner() != null) {
                    this.returnToOwner = true;
                }
                return;
            }

            ItemStack carriedStack = this.dog.getMainHandItem();
            if (carriedStack.is(Items.BONE) && !carriedStack.isEmpty()) {
                ItemStack insertStack = carriedStack.copyWithCount(1);
                if (burrowBlockEntity.tryAddItem(insertStack)) {
                    carriedStack.shrink(1);
                    this.dog.setBurrowStoredBones(this.dog.getBurrowStoredBones() + 1);
                    if (carriedStack.isEmpty()) {
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        this.dog.setItemSlot(EquipmentSlot.MAINHAND, carriedStack);
                    }
                    this.targetBone = null;
                    return;
                }
            }

            this.dropCarriedItem(standPos);
            this.targetBone = null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.dog.isFetching()
                    && !this.dog.isOrderedToSit()
                    && !this.dog.isResting()
                    && !this.dog.isAttacking()
                    && !this.dog.isDigging()
                    && !this.dog.isPanicking()
                    && (this.dog.getMainHandItem().is(Items.BONE) || this.targetBone != null && this.targetBone.isAlive() || this.lostTargetTicks < 40);
        }

        @Override
        public void stop() {
            this.targetBone = null;
            this.lostTargetTicks = 0;
            this.returnToOwner = false;
            this.dog.getNavigation().stop();
            this.dog.setFetching(false);
        }

        @Nullable
        private ItemEntity findNearestThrownBone() {
            List<ItemEntity> itemEntities = this.dog.level().getEntitiesOfClass(ItemEntity.class, this.dog.getBoundingBox().inflate(SEARCH_RANGE));
            ItemEntity closestBoneEntity = null;
            double closestDistance = Double.MAX_VALUE;

            for (ItemEntity itemEntity : itemEntities) {
                if (!itemEntity.isAlive()) continue;
                if (!itemEntity.getItem().is(Items.BONE)) continue;
                if (!itemEntity.onGround()) continue;
                if (itemEntity.hasPickUpDelay()) continue;

                double checkedDistance = this.dog.distanceToSqr(itemEntity);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestBoneEntity = itemEntity;
                }
            }

            return closestBoneEntity;
        }

        private void dropCarriedItem(BlockPos pos) {
            ItemStack carriedStack = this.dog.getMainHandItem().copy();
            this.dog.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            if (!carriedStack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(this.dog.level(), pos.getX() + 0.5D, pos.getY() + 0.3D, pos.getZ() + 0.5D, carriedStack);
                this.dog.level().addFreshEntity(itemEntity);
            }
        }

        private void spawnAlertParticles() {
            if (!(this.dog.level() instanceof ServerLevel serverLevel)) return;
            serverLevel.sendParticles(ParticleTypeRegistry.ALERT.get(), this.dog.getX(), this.dog.getY() + 1.0D, this.dog.getZ(), 4, 0.2D, 0.2D, 0.2D, 0.0D);
        }

        private void spawnQuestionParticles() {
            if (!(this.dog.level() instanceof ServerLevel serverLevel)) return;
            serverLevel.sendParticles(ParticleTypeRegistry.QUESTION.get(), this.dog.getX(), this.dog.getY() + 1.0D, this.dog.getZ(), 2, 0.15D, 0.15D, 0.15D, 0.0D);
        }
    }
}