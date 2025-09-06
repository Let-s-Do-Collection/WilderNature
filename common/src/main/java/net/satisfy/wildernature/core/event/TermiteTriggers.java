package net.satisfy.wildernature.core.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.TermiteBlock;

public final class TermiteTriggers {
    public static void init() {
        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            var level = player.level();
            var stack = player.getItemInHand(hand);
            if (!(stack.getItem() instanceof BlockItem bi)) return EventResult.pass();
            BlockState place = bi.getBlock().defaultBlockState();
            if (!place.is(BlockTags.LOGS)) return EventResult.pass();
            if (face == null) return EventResult.pass();
            BlockPos target = pos.relative(face);
            if (!level.isClientSide && level instanceof ServerLevel server) {
                if (server.isEmptyBlock(target)) {
                    TermiteBlock.triggerFromLog(server, target);
                }
            }
            return EventResult.pass();
        });
    }
}
