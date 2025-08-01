package net.satisfy.wildernature.client.gui.handlers;

import dev.architectury.networking.NetworkManager;
import dev.architectury.platform.Platform;
import dev.architectury.registry.menu.MenuRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.block.entity.BountyBoardBlockEntity;
import net.satisfy.wildernature.event.EventManager;
import net.satisfy.wildernature.item.ContractItem;
import net.satisfy.wildernature.network.BountyBlockNetworking;
import net.satisfy.wildernature.util.BountyBoardTier;
import net.satisfy.wildernature.util.contract.Contract;
import net.satisfy.wildernature.util.contract.ContractInProgress;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BountyBlockScreenHandler extends AbstractContainerMenu {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(WilderNature.MOD_ID, net.minecraft.core.registries.Registries.MENU);

    public static final RegistrySupplier<MenuType<BountyBlockScreenHandler>> BOUNTY_BLOCK = MENUS.register("bounty_menu",
            () -> MenuRegistry.ofExtended((id, inventory, buf) -> {
                BountyBlockScreenHandler handler = new BountyBlockScreenHandler(id, inventory, null);
                handler.onServerUpdate(buf);
                return handler;
            })
    );

    public static void registerMenuTypes() {
        MENUS.register();
    }

    public final BountyBoardBlockEntity targetEntity;

    public BountyBlockScreenHandler(int id, Inventory inventory, BountyBoardBlockEntity targetEntity) {
        super(BOUNTY_BLOCK.get(), id);
        this.targetEntity = targetEntity;

        if (targetEntity != null) {
            targetEntity.onTick.subscribe(() -> {
                if (targetEntity.rerollCooldownLeft % 20 == 0 && inventory.player.containerMenu == this) {
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, BountyBlockNetworking.MAX_SIZE), inventory.player.registryAccess());
                    writeBlockDataChange(buf, targetEntity.rerollsLeft, targetEntity.rerollCooldownLeft, targetEntity.boardId, targetEntity.tier, targetEntity.xp);
                    NetworkManager.sendToPlayer((ServerPlayer) inventory.player, BountyBlockNetworking.ID_SCREEN_UPDATE, buf);
                }
            });

            targetEntity.onBlockDataChange.subscribe(() -> {
                if (inventory.player.containerMenu == this) {
                    RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, BountyBlockNetworking.MAX_SIZE), inventory.player.registryAccess());
                    buf.writeEnum(BountyBlockNetworking.BountyServerUpdateType.MULTI);
                    buf.writeShort(3);
                    writeBlockDataChange(buf, targetEntity.rerollsLeft, targetEntity.rerollCooldownLeft, targetEntity.boardId, targetEntity.tier, targetEntity.xp);
                    writeUpdateContracts(buf, targetEntity);
                    writeActiveContractInfo(buf, (ServerPlayer) inventory.player);
                    NetworkManager.sendToPlayer((ServerPlayer) inventory.player, BountyBlockNetworking.ID_SCREEN_UPDATE, buf);
                }
            });
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlot(new Slot(inventory, i * 9 + j + 9, j * 18 + 8, i * 18 + 86));
            }
        }
        for (int j = 0; j < 9; j++) {
            addSlot(new Slot(inventory, j, j * 18 + 8, 144));
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public static void writeBlockDataChange(FriendlyByteBuf buf, int rerolls, int time, long boardId, ResourceLocation tierId, int xp) {
        buf.writeEnum(BountyBlockNetworking.BountyServerUpdateType.SEND_BOARD_DATA);
        buf.writeInt(time);
        buf.writeByte(rerolls);
        buf.writeLong(boardId);
        buf.writeResourceLocation(tierId);
        buf.writeFloat(BountyBoardTier.byId(tierId)
                .orElseThrow(() -> new RuntimeException("Tier not found: " + tierId))
                .progress(xp));
    }

    public static void writeActiveContractInfo(FriendlyByteBuf buf, ServerPlayer player) {
        var contractProgress = ContractInProgress.progressPerPlayer.get(player.getUUID());
        if (contractProgress == null) {
            buf.writeEnum(BountyBlockNetworking.BountyServerUpdateType.CLEAR_ACTIVE_CONTRACT);
        } else {
            buf.writeEnum(BountyBlockNetworking.BountyServerUpdateType.SET_ACTIVE_CONTRACT);
            buf.writeNbt((CompoundTag) ContractInProgress.SERVER_CODEC.encode(contractProgress, NbtOps.INSTANCE, new CompoundTag()).getOrThrow(
                    string -> new RuntimeException("Failed to encode contract progress")
            ));
            buf.writeNbt((CompoundTag) Contract.CODEC.encode(contractProgress.getContract(), NbtOps.INSTANCE, new CompoundTag()).getOrThrow(
                    string -> new RuntimeException("Failed to encode contract")
            ));
        }
    }

    public static void writeUpdateContracts(FriendlyByteBuf buf, BountyBoardBlockEntity blockEntity) {
        buf.writeEnum(BountyBlockNetworking.BountyServerUpdateType.UPDATE_CONTRACTS);
        buf.writeNbt(blockEntity.getContractsNbt());
    }

    public void handleClientAction(ServerPlayer player, FriendlyByteBuf buf) {
        var action = buf.readEnum(BountyBlockNetworking.BountyClientActionType.class);
        switch (action) {
            case REROLL:
                targetEntity.tryReroll();
                break;
            case CONFIRM_CONTRACT:
                handleConfirmContract(player, buf);
                break;
            case FINISH_CONTRACT:
                handleFinishContract(player);
                break;
            case DELETE_CONTRACT:
                handleDeleteContract(player);
                break;
        }
        targetEntity.setChanged();
    }

    private void handleConfirmContract(ServerPlayer player, FriendlyByteBuf buf) {
        var playerContract = ContractInProgress.progressPerPlayer.get(player.getUUID());
        if (playerContract != null) {
            player.sendSystemMessage(Component.literal("Error: you already have a contract"));
            return;
        }

        var id = buf.readByte();
        var contract = targetEntity.getContracts()[id];
        targetEntity.setRandomContractInSlot(id);

        var stack = Contract.fromId(contract).contractStack();
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putString(ContractItem.TAG_CONTRACT_ID, contract.toString());
        tag.putUUID(ContractItem.TAG_PLAYER, player.getUUID());

        long startTick = Objects.requireNonNull(Objects.requireNonNull(player.getServer()).overworld(), "Server level not available").getGameTime();
        long expiryTick = startTick + ContractInProgress.EXPIRY_TICKS;
        tag.putLong(ContractItem.TAG_EXPIRY_TICK, expiryTick);

        player.getInventory().add(stack);
        ContractInProgress.progressPerPlayer.put(player.getUUID(), ContractInProgress.newInstance(contract, Contract.fromId(contract).count(), targetEntity.boardId, startTick));

        updatePlayerScreen(player);
    }

    private void handleFinishContract(ServerPlayer player) {
        var contract = ContractInProgress.progressPerPlayer.get(player.getUUID());
        if (contract == null) {
            player.sendSystemMessage(Component.literal("Error: player does not have a contract"));
            return;
        }
        if (!contract.isFinished()) {
            player.sendSystemMessage(Component.literal("Error: contract is not finished"));
            return;
        }

        player.sendSystemMessage(Component.translatable("text.gui.wildernature.bounty.finished", Component.translatable(contract.getContract().name())));
        ContractInProgress.progressPerPlayer.remove(player.getUUID());
        contract.onFinish(player);

        updatePlayerScreen(player);
    }

    private void handleDeleteContract(ServerPlayer player) {
        var contract = ContractInProgress.progressPerPlayer.get(player.getUUID());
        if (contract == null) {
            player.sendSystemMessage(Component.literal("Error: you do not have a contract to delete."));
            return;
        }
        ContractInProgress.progressPerPlayer.remove(player.getUUID());
        updatePlayerScreen(player);
    }

    private void updatePlayerScreen(ServerPlayer player) {
        RegistryFriendlyByteBuf newBuf = new RegistryFriendlyByteBuf(new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, BountyBlockNetworking.MAX_SIZE), player.registryAccess());
        writeActiveContractInfo(newBuf, player);
        writeUpdateContracts(newBuf, targetEntity);
        NetworkManager.sendToPlayer(player, BountyBlockNetworking.ID_SCREEN_UPDATE, newBuf);
    }

    public static AbstractContainerMenu createServer(int id, Inventory inventory, BountyBoardBlockEntity bountyBoardBlockEntity) {
        return new BountyBlockScreenHandler(id, inventory, bountyBoardBlockEntity);
    }

    public void onServerUpdate(FriendlyByteBuf buf) {
        var updateType = buf.readEnum(BountyBlockNetworking.BountyServerUpdateType.class);
        try {
            if (updateType == BountyBlockNetworking.BountyServerUpdateType.MULTI) {
                int count = buf.readShort();
                for (int i = 0; i < count; i++) {
                    onServerUpdate(buf);
                }
            }
            if (updateType == BountyBlockNetworking.BountyServerUpdateType.UPDATE_CONTRACTS) {
                contracts = Contract.CODEC.listOf()
                        .decode(NbtOps.INSTANCE, Objects.requireNonNull(buf.readNbt()).get("list"))
                        .getOrThrow(error -> { throw new RuntimeException(error); })
                        .getFirst()
                        .toArray(new Contract[3]);
                onContractUpdate.invoke();
            }
            if (updateType == BountyBlockNetworking.BountyServerUpdateType.SEND_BOARD_DATA) {
                time = buf.readInt();
                rerolls = buf.readByte();
                boardId = buf.readLong();
                tierId = buf.readResourceLocation();
                progress = buf.readFloat();
            }
            if (updateType == BountyBlockNetworking.BountyServerUpdateType.SET_ACTIVE_CONTRACT) {
                Platform.isDevelopmentEnvironment();
                var nbt = buf.readNbt();
                activeContractProgress = ContractInProgress.SERVER_CODEC.decode(NbtOps.INSTANCE, nbt)
                        .getOrThrow(error -> { throw new RuntimeException(error); })
                        .getFirst();
                nbt = buf.readNbt();
                activeContract = Contract.CODEC.decode(NbtOps.INSTANCE, nbt)
                        .getOrThrow(error -> { throw new RuntimeException(error); })
                        .getFirst();
            }
            if (updateType == BountyBlockNetworking.BountyServerUpdateType.CLEAR_ACTIVE_CONTRACT) {
                activeContractProgress = null;
                activeContract = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public EventManager onContractUpdate = new EventManager();
    public ContractInProgress activeContractProgress;
    public Contract activeContract;

    public long boardId;
    public Contract[] contracts;
    public int time;
    public int rerolls;
    public float progress;
    public ResourceLocation tierId;
}
