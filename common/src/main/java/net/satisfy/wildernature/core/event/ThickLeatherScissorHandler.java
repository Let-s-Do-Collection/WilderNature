package net.satisfy.wildernature.core.event;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

import java.util.List;
import java.util.Optional;

public class ThickLeatherScissorHandler {
    private static final double REACH = 4.0D;
    private static final int LEATHER_COUNT = 3;

    public static void init() {
        InteractionEvent.RIGHT_CLICK_ITEM.register(ThickLeatherScissorHandler::onRightClickItem);
    }

    private static CompoundEventResult<ItemStack> onRightClickItem(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);

        if (!heldStack.is(Items.SHEARS)) {
            return CompoundEventResult.pass();
        }

        if (player.level().isClientSide()) {
            return CompoundEventResult.pass();
        }

        Optional<ItemEntity> foundEntity = findThickLeatherInSight(player);
        if (foundEntity.isEmpty()) {
            return CompoundEventResult.pass();
        }

        ItemEntity thickLeatherEntity = foundEntity.get();
        ServerLevel serverLevel = (ServerLevel) player.level();

        ItemStack leatherPileStack = thickLeatherEntity.getItem();
        if (leatherPileStack.getCount() > 1) {
            leatherPileStack.shrink(1);
            thickLeatherEntity.setItem(leatherPileStack);
        } else {
            thickLeatherEntity.discard();
        }

        serverLevel.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.LEATHER)),
                thickLeatherEntity.getX(),
                thickLeatherEntity.getY() + 0.2D,
                thickLeatherEntity.getZ(),
                12,
                0.15D, 0.1D, 0.15D,
                0.08D);

        serverLevel.playSound(
                null,
                thickLeatherEntity.blockPosition(),
                SoundEvents.SHEEP_SHEAR,
                SoundSource.PLAYERS,
                0.8F,
                0.9F + serverLevel.getRandom().nextFloat() * 0.2F);

        ItemEntity spawnedLeatherEntity = new ItemEntity(
                serverLevel,
                thickLeatherEntity.getX(),
                thickLeatherEntity.getY() + 0.1D,
                thickLeatherEntity.getZ(),
                new ItemStack(Items.LEATHER, LEATHER_COUNT));
        spawnedLeatherEntity.setDefaultPickUpDelay();
        serverLevel.addFreshEntity(spawnedLeatherEntity);

        EquipmentSlot shearsSlot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND
                : EquipmentSlot.OFFHAND;
        heldStack.hurtAndBreak(1, player, shearsSlot);

        return CompoundEventResult.interruptTrue(heldStack);
    }

    private static Optional<ItemEntity> findThickLeatherInSight(Player player) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookDir = player.getLookAngle();
        Vec3 reachEnd = eyePos.add(lookDir.scale(REACH));

        AABB searchBox = new AABB(eyePos, reachEnd).inflate(1.0D);

        List<ItemEntity> candidates = player.level().getEntitiesOfClass(
                ItemEntity.class,
                searchBox,
                itemEntity -> itemEntity.getItem().is(ObjectRegistry.THICK_LEATHER.get()));

        ItemEntity closest = null;
        double closestDist = Double.MAX_VALUE;

        for (ItemEntity candidate : candidates) {
            AABB entityBox = candidate.getBoundingBox().inflate(0.3D);
            Optional<Vec3> hit = entityBox.clip(eyePos, reachEnd);
            if (hit.isPresent()) {
                double dist = eyePos.distanceTo(hit.get());
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = candidate;
                }
            }
        }

        return Optional.ofNullable(closest);
    }
}