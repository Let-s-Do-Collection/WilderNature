package net.satisfy.wildernature.core.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class TermiteEvents {
    public interface StripLog {
        EventResult strip(ServerLevel level, BlockPos pos, BlockState state);
    }
    public static final Event<StripLog> STRIP_LOG = EventFactory.createEventResult();
    private TermiteEvents() {}
}
