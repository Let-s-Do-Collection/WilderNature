package net.satisfy.wildernature.core.event;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class VanillaBoneThrowEvent {
    public static void init() {
        InteractionEvent.RIGHT_CLICK_ITEM.register(VanillaBoneThrowEvent::onRightClickItem);
    }

    private static CompoundEventResult<ItemStack> onRightClickItem(Player player, InteractionHand hand) {
        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.is(Items.BONE)) {
            return CompoundEventResult.pass();
        }
        if (!player.isShiftKeyDown()) {
            return CompoundEventResult.pass();
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return CompoundEventResult.interruptFalse(heldStack);
        }

        ItemStack thrownStack = heldStack.copyWithCount(1);
        ItemEntity thrownBoneEntity = new ItemEntity(serverLevel, player.getX(), player.getEyeY() - 0.2D, player.getZ(), thrownStack);
        thrownBoneEntity.setPickUpDelay(20);
        thrownBoneEntity.setThrower(player);
        thrownBoneEntity.setDeltaMovement(player.getLookAngle().scale(0.9D).add(0.0D, 0.18D, 0.0D));
        serverLevel.addFreshEntity(thrownBoneEntity);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 0.9F + player.getRandom().nextFloat() * 0.2F);

        if (!player.getAbilities().instabuild) {
            heldStack.shrink(1);
        }

        return CompoundEventResult.interruptTrue(player.getItemInHand(hand));
    }

    private VanillaBoneThrowEvent() {
    }
}