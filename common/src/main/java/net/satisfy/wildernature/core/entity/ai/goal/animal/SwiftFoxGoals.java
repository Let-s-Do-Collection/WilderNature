package net.satisfy.wildernature.core.entity.ai.goal.animal;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.passive.HedgehogEntity;
import net.satisfy.wildernature.core.entity.animal.neutral.SwiftFoxEntity;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import org.jetbrains.annotations.Nullable;

public class SwiftFoxGoals {
    public static class SwiftFoxHuntSmallCritterGoal extends Goal {
        private static final double SEARCH_RANGE = 10.0D;

        private final SwiftFoxEntity swiftFox;
        @Nullable
        private Animal targetPrey;

        public SwiftFoxHuntSmallCritterGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (this.swiftFox.getTarget() != null || !this.swiftFox.getMainHandItem().isEmpty() || this.swiftFox.hasHiddenItem()) {
                return false;
            }
            this.targetPrey = this.findPrey();
            return this.targetPrey != null;
        }

        @Override
        public void start() {
            this.swiftFox.setTarget(this.targetPrey);
        }

        @Override
        public void tick() {
            if (this.targetPrey == null || !this.targetPrey.isAlive()) {
                return;
            }

            this.swiftFox.getNavigation().moveTo(this.targetPrey, 1.1D);
            this.swiftFox.getLookControl().setLookAt(this.targetPrey, 30.0F, 30.0F);

            if (this.swiftFox.distanceToSqr(this.targetPrey) < 2.2D && this.swiftFox.canStartAttackAnimation()) {
                this.swiftFox.triggerAttackAnimation();
                this.swiftFox.doHurtTarget(this.targetPrey);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPrey != null && this.targetPrey.isAlive() && this.isValidPrey(this.targetPrey);
        }

        @Override
        public void stop() {
            this.targetPrey = null;
            this.swiftFox.setTarget(null);
        }

        @Nullable
        private Animal findPrey() {
            List<Animal> animals = this.swiftFox.level().getNearbyEntities(Animal.class, TargetingConditions.forNonCombat(), this.swiftFox, this.swiftFox.getBoundingBox().inflate(SEARCH_RANGE));
            Animal closestAnimal = null;
            double closestDistance = Double.MAX_VALUE;

            for (Animal animal : animals) {
                if (!this.isValidPrey(animal)) {
                    continue;
                }

                double checkedDistance = this.swiftFox.distanceToSqr(animal);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestAnimal = animal;
                }
            }

            return closestAnimal;
        }

        private boolean isValidPrey(Animal animal) {
            if (!animal.isAlive() || animal.isBaby()) {
                return false;
            }
            if (!animal.getType().is(TagsRegistry.SWIFT_FOX_TARGETS)) {
                return false;
            }
            if (animal instanceof HedgehogEntity hedgehogEntity) {
                return !hedgehogEntity.isCurled();
            }
            return true;
        }
    }

    public static class SwiftFoxStealPlayerItemGoal extends Goal {
        private static final double RANGE = 10.0D;
        private static final double STEAL_RANGE = 2.2D;

        private final SwiftFoxEntity swiftFox;
        @Nullable
        private Player targetPlayer;
        private int sneakTicks;
        private boolean escaping;

        public SwiftFoxStealPlayerItemGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.swiftFox.isSleeping() || !this.swiftFox.canStealNow()) {
                return false;
            }
            if (this.swiftFox.getRandom().nextInt(4) != 0) {
                return false;
            }

            Player player = this.swiftFox.level().getNearestPlayer(this.swiftFox, RANGE);
            if (player == null || player.isCreative() || player.isSpectator() || !this.swiftFox.shouldAvoidPlayer(player)) {
                return false;
            }
            if (player.getMainHandItem().isEmpty() && player.getOffhandItem().isEmpty()) {
                return false;
            }

            this.targetPlayer = player;
            return true;
        }

        @Override
        public void start() {
            this.sneakTicks = 20;
            this.escaping = false;
            this.swiftFox.setTryingToStealFromPlayer(true);
            this.swiftFox.startSneaking();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.swiftFox.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (!this.escaping) {
                if (this.sneakTicks > 0) {
                    this.sneakTicks--;
                    this.swiftFox.getNavigation().moveTo(this.targetPlayer, 0.9D);
                    return;
                }

                this.swiftFox.getNavigation().moveTo(this.targetPlayer, 1.2D);

                if (this.swiftFox.distanceToSqr(this.targetPlayer) <= STEAL_RANGE * STEAL_RANGE) {
                    ItemStack mainHandItem = this.targetPlayer.getMainHandItem();
                    ItemStack offhandItem = this.targetPlayer.getOffhandItem();
                    ItemStack itemToSteal = !offhandItem.isEmpty() ? offhandItem : mainHandItem;

                    if (!itemToSteal.isEmpty()) {
                        ItemStack takenItem = itemToSteal.split(1);
                        this.swiftFox.onItemStolenFromPlayer(this.targetPlayer, takenItem);
                        this.escaping = true;
                        this.swiftFox.stopSneaking();
                    }
                }
                return;
            }

            Vec3 awayFromPlayer = this.swiftFox.position().subtract(this.targetPlayer.position()).normalize();
            Vec3 escapePos = this.swiftFox.position().add(awayFromPlayer.scale(8.0D));
            this.swiftFox.getNavigation().moveTo(escapePos.x, escapePos.y, escapePos.z, 1.5D);
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.targetPlayer.isAlive() && (!this.swiftFox.getMainHandItem().isEmpty() || !this.escaping);
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.escaping = false;
            this.sneakTicks = 0;
            this.swiftFox.setTryingToStealFromPlayer(false);
            this.swiftFox.stopSneaking();
            this.swiftFox.getNavigation().stop();
        }
    }

    public static class SwiftFoxStealGroundItemGoal extends Goal {
        private static final double SEARCH_RANGE = 8.0D;

        private final SwiftFoxEntity swiftFox;
        @Nullable
        private ItemEntity targetItem;

        public SwiftFoxStealGroundItemGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.swiftFox.isSleeping() || !this.swiftFox.canStealNow()) {
                return false;
            }
            this.targetItem = this.findTargetItem();
            return this.targetItem != null;
        }

        @Override
        public void tick() {
            if (this.targetItem == null || !this.targetItem.isAlive()) {
                return;
            }

            this.swiftFox.getNavigation().moveTo(this.targetItem, 1.15D);
            this.swiftFox.getLookControl().setLookAt(this.targetItem, 30.0F, 30.0F);

            if (this.swiftFox.distanceToSqr(this.targetItem) <= 2.0D) {
                ItemStack groundStack = this.targetItem.getItem();
                if (!groundStack.isEmpty()) {
                    ItemStack takenItem = groundStack.split(1);
                    this.swiftFox.setItemSlot(EquipmentSlot.MAINHAND, takenItem);
                    this.swiftFox.setStolenFromPlayer(null);
                    this.swiftFox.resetStealCooldown();
                    if (groundStack.isEmpty()) {
                        this.targetItem.discard();
                    } else {
                        this.targetItem.setItem(groundStack);
                    }
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetItem != null && this.targetItem.isAlive() && this.swiftFox.getMainHandItem().isEmpty();
        }

        @Override
        public void stop() {
            this.targetItem = null;
            this.swiftFox.getNavigation().stop();
        }

        @Nullable
        private ItemEntity findTargetItem() {
            List<ItemEntity> nearbyItems = this.swiftFox.level().getEntitiesOfClass(ItemEntity.class, this.swiftFox.getBoundingBox().inflate(SEARCH_RANGE));
            ItemEntity closestItem = null;
            double closestDistance = Double.MAX_VALUE;

            for (ItemEntity itemEntity : nearbyItems) {
                if (!itemEntity.isAlive() || itemEntity.hasPickUpDelay() || !itemEntity.onGround() || itemEntity.getItem().isEmpty()) {
                    continue;
                }

                double checkedDistance = this.swiftFox.distanceToSqr(itemEntity);
                if (checkedDistance < closestDistance) {
                    closestDistance = checkedDistance;
                    closestItem = itemEntity;
                }
            }

            return closestItem;
        }
    }

    public static class SwiftFoxHideCarriedItemGoal extends Goal {
        private final SwiftFoxEntity swiftFox;
        @Nullable
        private BlockPos hidePos;
        private int hideTicks;

        public SwiftFoxHideCarriedItemGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.swiftFox.getMainHandItem().isEmpty() || this.swiftFox.hasHiddenItem() || this.swiftFox.canGiftNow()) {
                return false;
            }
            this.hidePos = this.swiftFox.findHidePos(8);
            return this.hidePos != null;
        }

        @Override
        public void start() {
            this.hideTicks = 0;
        }

        @Override
        public void tick() {
            if (this.hidePos == null) {
                return;
            }

            this.swiftFox.getNavigation().moveTo(this.hidePos.getX() + 0.5D, this.hidePos.getY(), this.hidePos.getZ() + 0.5D, 1.3D);
            this.swiftFox.getLookControl().setLookAt(this.hidePos.getX() + 0.5D, this.hidePos.getY() + 0.5D, this.hidePos.getZ() + 0.5D);

            if (!this.hidePos.closerToCenterThan(this.swiftFox.position(), 1.5D)) {
                return;
            }

            this.swiftFox.getNavigation().stop();
            this.hideTicks++;

            if (this.swiftFox.level() instanceof ServerLevel serverLevel && this.hideTicks % 4 == 0) {
                serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, this.swiftFox.getMainHandItem()), this.hidePos.getX() + 0.5D, this.hidePos.getY() + 0.3D, this.hidePos.getZ() + 0.5D, 1, 0.08D, 0.03D, 0.08D, 0.0D);
            }

            if (this.hideTicks >= 20) {
                this.swiftFox.hideCurrentItem(this.hidePos);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.hidePos != null && !this.swiftFox.getMainHandItem().isEmpty() && !this.swiftFox.hasHiddenItem();
        }

        @Override
        public void stop() {
            this.hidePos = null;
            this.hideTicks = 0;
            this.swiftFox.getNavigation().stop();
        }
    }

    public static class SwiftFoxReturnItemGoal extends Goal {
        private static final double DROP_RANGE = 2.0D;

        private final SwiftFoxEntity swiftFox;
        @Nullable
        private Player targetPlayer;

        public SwiftFoxReturnItemGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.swiftFox.hasHiddenItem() || !this.swiftFox.canReturnNow()) {
                return false;
            }
            this.targetPlayer = this.swiftFox.getReturnTargetPlayer();
            return this.targetPlayer != null && this.targetPlayer.isAlive();
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            if (this.swiftFox.getMainHandItem().isEmpty()) {
                BlockPos hiddenItemPos = this.swiftFox.getHidePos();
                if (hiddenItemPos != null && !hiddenItemPos.closerToCenterThan(this.swiftFox.position(), 1.5D)) {
                    this.swiftFox.getNavigation().moveTo(hiddenItemPos.getX() + 0.5D, hiddenItemPos.getY(), hiddenItemPos.getZ() + 0.5D, 1.2D);
                    this.swiftFox.getLookControl().setLookAt(hiddenItemPos.getX() + 0.5D, hiddenItemPos.getY() + 0.5D, hiddenItemPos.getZ() + 0.5D);
                    return;
                }

                ItemStack hiddenItem = this.swiftFox.getHiddenItem();
                if (!hiddenItem.isEmpty()) {
                    this.swiftFox.setItemSlot(EquipmentSlot.MAINHAND, hiddenItem.copy());
                    this.swiftFox.setHiddenItem(ItemStack.EMPTY);
                    this.swiftFox.setHidePos(null);
                }
            }

            this.swiftFox.getNavigation().moveTo(this.targetPlayer, 1.2D);
            this.swiftFox.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.swiftFox.distanceToSqr(this.targetPlayer) <= DROP_RANGE * DROP_RANGE) {
                ItemStack carriedItem = this.swiftFox.getMainHandItem().copy();
                this.swiftFox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.swiftFox.setStolenFromPlayer(null);
                if (!carriedItem.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(this.swiftFox.level(), this.targetPlayer.getX(), this.targetPlayer.getY() + 0.3D, this.targetPlayer.getZ(), carriedItem);
                    itemEntity.setDeltaMovement(0.0D, 0.12D, 0.0D);
                    this.swiftFox.level().addFreshEntity(itemEntity);
                }
                this.swiftFox.addTrust(10);
                this.swiftFox.resetReturnCooldown();
                this.swiftFox.stopReturningHiddenItem();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.targetPlayer.isAlive() && (this.swiftFox.hasHiddenItem() || !this.swiftFox.getMainHandItem().isEmpty());
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.swiftFox.getNavigation().stop();
        }
    }

    public static class SwiftFoxGiftGoal extends Goal {
        private static final double GIVE_RANGE = 2.0D;

        private final SwiftFoxEntity swiftFox;
        @Nullable
        private Player targetPlayer;
        private ItemStack giftItem = ItemStack.EMPTY;

        public SwiftFoxGiftGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.swiftFox.canGiftNow()) {
                return false;
            }
            Player trustedPlayer = this.swiftFox.getTrustedPlayer();
            if (trustedPlayer == null || !trustedPlayer.isAlive() || this.swiftFox.distanceToSqr(trustedPlayer) > 144.0D) {
                return false;
            }
            if (this.swiftFox.getRandom().nextInt(500) != 0) {
                return false;
            }

            this.giftItem = this.swiftFox.createGiftItem();
            if (this.giftItem.isEmpty()) {
                return false;
            }

            this.targetPlayer = trustedPlayer;
            return true;
        }

        @Override
        public void start() {
            this.swiftFox.setItemSlot(EquipmentSlot.MAINHAND, this.giftItem.copy());
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                return;
            }

            this.swiftFox.getNavigation().moveTo(this.targetPlayer, 1.1D);
            this.swiftFox.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.swiftFox.distanceToSqr(this.targetPlayer) <= GIVE_RANGE * GIVE_RANGE) {
                ItemStack carriedItem = this.swiftFox.getMainHandItem().copy();
                this.swiftFox.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                if (!carriedItem.isEmpty()) {
                    ItemEntity itemEntity = new ItemEntity(this.swiftFox.level(), this.targetPlayer.getX(), this.targetPlayer.getY() + 0.3D, this.targetPlayer.getZ(), carriedItem);
                    itemEntity.setDeltaMovement(0.0D, 0.12D, 0.0D);
                    this.swiftFox.level().addFreshEntity(itemEntity);
                }
                this.swiftFox.addTrust(4);
                this.swiftFox.resetGiftCooldown();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.targetPlayer.isAlive() && !this.swiftFox.getMainHandItem().isEmpty();
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.giftItem = ItemStack.EMPTY;
            this.swiftFox.getNavigation().stop();
        }
    }

    public static class SwiftFoxSensitivePlayerReactionGoal extends Goal {
        private final SwiftFoxEntity swiftFox;

        public SwiftFoxSensitivePlayerReactionGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.swiftFox.isTryingToStealFromPlayer() || !this.swiftFox.getMainHandItem().isEmpty() || this.swiftFox.hasHiddenItem() || this.swiftFox.canReturnNow() || this.swiftFox.canGiftNow() || this.swiftFox.getTarget() != null) {
                return false;
            }

            List<Player> nearbyPlayers = this.swiftFox.level().getNearbyEntities(Player.class, TargetingConditions.forNonCombat(), this.swiftFox, this.swiftFox.getBoundingBox().inflate(6.0D));
            for (Player player : nearbyPlayers) {
                if (this.swiftFox.shouldAvoidPlayer(player)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            if (this.swiftFox.isTryingToStealFromPlayer() || !this.swiftFox.getMainHandItem().isEmpty() || this.swiftFox.hasHiddenItem() || this.swiftFox.canReturnNow() || this.swiftFox.canGiftNow() || this.swiftFox.getTarget() != null) {
                return false;
            }

            List<Player> nearbyPlayers = this.swiftFox.level().getNearbyEntities(Player.class, TargetingConditions.forNonCombat(), this.swiftFox, this.swiftFox.getBoundingBox().inflate(6.0D));
            for (Player player : nearbyPlayers) {
                if (this.swiftFox.shouldAvoidPlayer(player)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void tick() {
            Player nearestPlayer = this.swiftFox.level().getNearestPlayer(this.swiftFox, 6.0D);
            if (nearestPlayer == null || !this.swiftFox.shouldAvoidPlayer(nearestPlayer)) {
                return;
            }

            Vec3 awayFromPlayer = this.swiftFox.position().subtract(nearestPlayer.position()).normalize();
            Vec3 runTo = this.swiftFox.position().add(awayFromPlayer.scale(7.0D));
            this.swiftFox.getNavigation().moveTo(runTo.x, runTo.y, runTo.z, 1.35D);
        }

        @Override
        public void stop() {
            this.swiftFox.getNavigation().stop();
        }
    }

    public static class SwiftFoxAmbientGoal extends Goal {
        private final SwiftFoxEntity swiftFox;

        public SwiftFoxAmbientGoal(SwiftFoxEntity swiftFox) {
            this.swiftFox = swiftFox;
        }

        @Override
        public boolean canUse() {
            return this.swiftFox.getRandom().nextInt(400) == 0;
        }

        @Override
        public void start() {
            this.swiftFox.playAmbientSound();
        }
    }
}