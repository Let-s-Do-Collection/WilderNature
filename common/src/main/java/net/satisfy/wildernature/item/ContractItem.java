package net.satisfy.wildernature.item;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.util.contract.ContractInProgress;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ContractItem extends Item {
    public static final String TAG_PLAYER = "player_uuid";
    public static final String TAG_CONTRACT_ID = "contract_id";
    public static final String TAG_NAME = "contract_name";
    public static final String TAG_DESCRIPTION = "contract_description";
    public static final String TAG_COUNT_LEFT = "count_left";
    public static final String TAG_COUNT_TOTAL = "count_total";
    public static final String TAG_EXPIRY_TICK = "expiry_tick";

    public ContractItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        if (!itemStack.has(DataComponents.CUSTOM_DATA)) {
            list.add(Component.translatable("tooltip.wildernature.contract_error"));
            return;
        }

        var data = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        var name = Component.translatable(data.copyTag().getString(TAG_NAME));
        var description = Component.translatable(data.copyTag().getString(TAG_DESCRIPTION));
        var progress = Component.translatable("text.gui.wildernature.bounty.progress",
                data.copyTag().getInt(TAG_COUNT_TOTAL) - data.copyTag().getInt(TAG_COUNT_LEFT),
                data.copyTag().getInt(TAG_COUNT_TOTAL));

        list.add(name);
        list.add(description);
        list.add(progress);

        var level =  Minecraft.getInstance().level;
        if (data.contains(TAG_EXPIRY_TICK) && level != null) {
            long expiryTick = data.copyTag().getLong(TAG_EXPIRY_TICK);
            long currentTick = level.getGameTime();
            long remainingTicks = expiryTick - currentTick;
            if (remainingTicks > 0) {
                long remainingSeconds = remainingTicks / 20;
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                list.add(Component.empty());
                list.add(Component.translatable("text.gui.wildernature.bounty.time_remaining", minutes, seconds));
            } else {
                list.add(Component.translatable("text.gui.wildernature.bounty.time_remaining", 0, 0));
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        var data = itemStack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (level.isClientSide() || data.copyTag() == null) {
            return;
        }

        if (entity instanceof Player player) {
            if (!player.getUUID().equals(data.copyTag().getUUID(TAG_PLAYER))) {
                return;
            }

            var progress = ContractInProgress.progressPerPlayer.get(player.getUUID());
            if (progress == null) {
                itemStack.setCount(0);
                return;
            }

            data.copyTag().putString(TAG_CONTRACT_ID, progress.contractResource.toString());
            data.copyTag().putString(TAG_NAME, progress.getContract().name());
            data.copyTag().putString(TAG_DESCRIPTION, progress.getContract().description());
            data.copyTag().putInt(TAG_COUNT_TOTAL, progress.getContract().count());
            data.copyTag().putInt(TAG_COUNT_LEFT, progress.count);
            itemStack.set(DataComponents.CUSTOM_DATA, data);
        }
    }
}
