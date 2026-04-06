package net.satisfy.wildernature.core.gui.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.wildernature.core.bounty.BountyCategory;
import net.satisfy.wildernature.core.bounty.BountyDefinition;
import net.satisfy.wildernature.core.bounty.BountyManager;
import net.satisfy.wildernature.core.bounty.BountyReward;
import net.satisfy.wildernature.core.bounty.PlayerBountyData;
import net.satisfy.wildernature.core.network.BountyBoardNetworking;
import net.satisfy.wildernature.core.registry.MenuTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;

public class BountyBoardMenu extends AbstractContainerMenu {
    private final SimpleContainer contractPreviewContainer = new SimpleContainer(1);
    private final Inventory playerInventory;
    private List<BountyDefinition> bounties;
    private int selectedBountyIndex = -1;
    private final DataSlot activeProgress = DataSlot.standalone();
    private final DataSlot activeRequiredKills = DataSlot.standalone();
    private final DataSlot activeCompleted = DataSlot.standalone();
    private final DataSlot hasActiveBounty = DataSlot.standalone();
    private final DataSlot activeBountyIndex = DataSlot.standalone();
    private final List<UUID> abandonedBountyIds = new ArrayList<>();

    public BountyBoardMenu(int containerId, Inventory playerInventory) {
        super(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.bounties = this.resolveBounties();
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.addContractPreviewSlot();
        this.addPlayerInventorySlots(playerInventory);
        this.addDataSlot(this.hasActiveBounty);
        this.addDataSlot(this.activeBountyIndex);
        this.addDataSlot(this.activeProgress);
        this.addDataSlot(this.activeRequiredKills);
        this.addDataSlot(this.activeCompleted);
        this.updateActiveData();
        this.updateContractPreviewSlot();
    }

    public BountyBoardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.bounties = this.readBounties(buffer);
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.addContractPreviewSlot();
        this.addPlayerInventorySlots(playerInventory);
        this.addDataSlot(this.hasActiveBounty);
        this.addDataSlot(this.activeBountyIndex);
        this.addDataSlot(this.activeProgress);
        this.addDataSlot(this.activeRequiredKills);
        this.addDataSlot(this.activeCompleted);
        this.updateActiveData();
        this.updateContractPreviewSlot();
    }

    private int findFirstSelectableBountyIndex() {
        for (int index = 0; index < this.bounties.size(); index++) {
            if (!this.isBountyAbandoned(this.bounties.get(index).id())) {
                return index;
            }
        }

        return this.bounties.isEmpty() ? -1 : 0;
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        int inventoryStartX = 108;
        int inventoryStartY = 84;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9 + 9;
                int slotX = inventoryStartX + column * 18;
                int slotY = inventoryStartY + row * 18;
                this.addSlot(new Slot(playerInventory, slotIndex, slotX, slotY));
            }
        }

        int hotbarY = 142;

        for (int column = 0; column < 9; column++) {
            int slotX = inventoryStartX + column * 18;
            this.addSlot(new Slot(playerInventory, column, slotX, hotbarY));
        }
    }

    private void addContractPreviewSlot() {
        this.addSlot(new Slot(this.contractPreviewContainer, 0, 232, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack itemStack) {
                return BountyBoardMenu.this.canTurnInActiveContract(itemStack);
            }

            @Override
            public boolean mayPickup(Player player) {
                return false;
            }

            @Override
            public void setChanged() {
                super.setChanged();

                if (!(BountyBoardMenu.this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
                    return;
                }

                ItemStack contractStack = this.getItem();
                if (!BountyBoardMenu.this.canTurnInActiveContract(contractStack)) {
                    return;
                }

                boolean claimed = BountyManager.claimActiveBounty(serverPlayer);
                if (!claimed) {
                    return;
                }

                serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundRegistry.BOUNTY_COMPLETED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                BountyBoardMenu.this.selectedBountyIndex = BountyBoardMenu.this.findFirstSelectableBountyIndex();
                this.container.setItem(0, ItemStack.EMPTY);
                BountyBoardMenu.this.updateActiveData();
                BountyBoardMenu.this.updateContractPreviewSlot();
                BountyBoardNetworking.sendSync(
                        serverPlayer,
                        BountyBoardMenu.this.selectedBountyIndex,
                        BountyBoardMenu.this.hasActiveBounty(),
                        BountyBoardMenu.this.getActiveBounty().map(activeBounty -> BountyBoardMenu.this.bounties.indexOf(activeBounty)).orElse(-1),
                        BountyBoardMenu.this.getActiveProgress(),
                        BountyBoardMenu.this.getActiveRequiredKills(),
                        BountyBoardMenu.this.hasCompletedActiveBounty(),
                        new ArrayList<>(BountyBoardMenu.this.abandonedBountyIds)
                );
            }
        });
    }

    private boolean canTurnInActiveContract(ItemStack itemStack) {
        if (!this.hasCompletedActiveBounty()) {
            return false;
        }

        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty()) {
            return false;
        }

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag customDataTag = customData.copyTag();
        if (!customDataTag.hasUUID("BountyId")) {
            return false;
        }

        return customDataTag.getUUID("BountyId").equals(activeBounty.get().id());
    }

    private List<BountyDefinition> resolveBounties() {
        if (this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            return BountyManager.getDailyBounties(serverPlayer.serverLevel());
        }

        return List.of();
    }

    public static void writeBounties(FriendlyByteBuf buffer, List<BountyDefinition> bounties, List<UUID> abandonedBountyIds) {
        buffer.writeVarInt(bounties.size());
        buffer.writeVarInt(abandonedBountyIds.size());

        for (UUID abandonedBountyId : abandonedBountyIds) {
            buffer.writeUUID(abandonedBountyId);
        }

        for (BountyDefinition bountyDefinition : bounties) {
            buffer.writeUUID(bountyDefinition.id());
            buffer.writeUtf(bountyDefinition.category().getName());
            buffer.writeUtf(bountyDefinition.entityId().toString());
            buffer.writeVarInt(bountyDefinition.requiredKills());
            buffer.writeUtf(bountyDefinition.reward().lootTableId().toString());
            buffer.writeVarInt(bountyDefinition.reward().experienceReward());
            buffer.writeUtf(bountyDefinition.reward().previewItemId().toString());
            buffer.writeVarInt(bountyDefinition.reward().previewCount());
        }
    }

    private List<BountyDefinition> readBounties(FriendlyByteBuf buffer) {
        int bountyCount = buffer.readVarInt();
        List<BountyDefinition> syncedBounties = new ArrayList<>(bountyCount);

        this.abandonedBountyIds.clear();
        int abandonedCount = buffer.readVarInt();
        for (int index = 0; index < abandonedCount; index++) {
            this.abandonedBountyIds.add(buffer.readUUID());
        }

        for (int index = 0; index < bountyCount; index++) {
            UUID bountyId = buffer.readUUID();
            String categoryName = buffer.readUtf();
            String entityId = buffer.readUtf();
            int requiredKills = buffer.readVarInt();
            String lootTableId = buffer.readUtf();
            int experienceReward = buffer.readVarInt();
            String previewItemId = buffer.readUtf();
            int previewCount = buffer.readVarInt();

            syncedBounties.add(new BountyDefinition(
                    bountyId,
                    BountyCategory.byName(categoryName),
                    ResourceLocation.parse(entityId),
                    requiredKills,
                    new BountyReward(
                            ResourceLocation.parse(lootTableId),
                            experienceReward,
                            ResourceLocation.parse(previewItemId),
                            previewCount
                    )
            ));
        }

        return syncedBounties;
    }

    public boolean isBountyAbandoned(UUID bountyId) {
        return this.abandonedBountyIds.contains(bountyId);
    }

    public List<BountyDefinition> getVisibleBounties() {
        return this.bounties;
    }

    public void setSelectedBountyIndex(int selectedBountyIndex) {
        if (selectedBountyIndex >= 0 && selectedBountyIndex < this.bounties.size() && !this.isBountyAbandoned(this.bounties.get(selectedBountyIndex).id())) {
            this.selectedBountyIndex = selectedBountyIndex;
            this.updateContractPreviewSlot();
        }
    }

    public int getSelectedBountyIndex() {
        return this.selectedBountyIndex;
    }

    public Optional<BountyDefinition> getSelectedBounty() {
        if (this.selectedBountyIndex >= 0 && this.selectedBountyIndex < this.bounties.size()) {
            return Optional.of(this.bounties.get(this.selectedBountyIndex));
        }

        return Optional.empty();
    }

    public PlayerBountyData getPlayerBountyData() {
        if (this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            return BountyManager.getPlayerBountyData(serverPlayer);
        }

        return new PlayerBountyData();
    }

    public Optional<BountyDefinition> getActiveBounty() {
        int syncedActiveBountyIndex = this.activeBountyIndex.get();
        if (syncedActiveBountyIndex >= 0 && syncedActiveBountyIndex < this.bounties.size()) {
            return Optional.of(this.bounties.get(syncedActiveBountyIndex));
        }

        return Optional.empty();
    }

    public boolean hasActiveBounty() {
        return this.hasActiveBounty.get() == 1;
    }

    public boolean hasCompletedActiveBounty() {
        return this.activeCompleted.get() == 1;
    }

    public int getActiveProgress() {
        return this.activeProgress.get();
    }

    public int getActiveRequiredKills() {
        return this.activeRequiredKills.get();
    }

    public boolean hasContractPreviewItem() {
        return !this.contractPreviewContainer.getItem(0).isEmpty();
    }

    public boolean hasRestoreContractAvailable() {
        if (!this.hasActiveBounty() || this.hasCompletedActiveBounty()) {
            return false;
        }

        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty()) {
            return false;
        }

        return !this.hasPlayerContractItem(activeBounty.get().id());
    }

    private boolean hasPlayerContractItem(UUID bountyId) {
        for (int slotIndex = 0; slotIndex < this.playerInventory.getContainerSize(); slotIndex++) {
            ItemStack itemStack = this.playerInventory.getItem(slotIndex);
            if (this.isContractForBounty(itemStack, bountyId)) {
                return true;
            }
        }

        return this.isContractForBounty(this.getCarried(), bountyId);
    }

    private boolean isContractForBounty(ItemStack itemStack, UUID bountyId) {
        if (itemStack.isEmpty()) {
            return false;
        }

        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return false;
        }

        CompoundTag customDataTag = customData.copyTag();
        return customDataTag.hasUUID("BountyId") && customDataTag.getUUID("BountyId").equals(bountyId);
    }

    public void acceptSelectedBounty() {
        if (!(this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        Optional<BountyDefinition> targetBounty = this.hasActiveBounty() ? this.getActiveBounty() : this.getSelectedBounty();
        if (targetBounty.isEmpty()) {
            return;
        }

        boolean accepted = BountyManager.acceptBounty(serverPlayer, targetBounty.get().id());
        if (!accepted) {
            return;
        }

        PlayerBountyData playerBountyData = BountyManager.getPlayerBountyData(serverPlayer);
        BountyManager.giveOrRestoreContract(serverPlayer, targetBounty.get(), playerBountyData.getCurrentProgress());
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundRegistry.BOUNTY_ACCEPTED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.updateActiveData();
        this.updateContractPreviewSlot();
        BountyBoardNetworking.sendSync(
                serverPlayer,
                this.selectedBountyIndex,
                this.hasActiveBounty(),
                this.getActiveBounty().map(activeBounty -> this.bounties.indexOf(activeBounty)).orElse(-1),
                this.getActiveProgress(),
                this.getActiveRequiredKills(),
                this.hasCompletedActiveBounty(),
                new ArrayList<>(this.abandonedBountyIds)
        );
    }

    public void abandonActiveBounty(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = BountyManager.getPlayerBountyData(serverPlayer);
        if (!playerBountyData.hasActiveBounty()) {
            return;
        }

        BountyDefinition currentActiveBounty = playerBountyData.getActiveBounty();
        if (currentActiveBounty != null) {
            BountyManager.removeContractItem(serverPlayer, currentActiveBounty.id());
        }

        playerBountyData.abandonActiveBounty();
        BountyManager.savePlayerBountyData(serverPlayer, playerBountyData);
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundRegistry.BOUNTY_CANCELED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.syncAbandonedBountyIds();
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.updateActiveData();
        this.updateContractPreviewSlot();
        BountyBoardNetworking.sendSync(
                serverPlayer,
                this.selectedBountyIndex,
                this.hasActiveBounty(),
                this.getActiveBounty().map(activeBounty -> this.bounties.indexOf(activeBounty)).orElse(-1),
                this.getActiveProgress(),
                this.getActiveRequiredKills(),
                this.hasCompletedActiveBounty(),
                new ArrayList<>(this.abandonedBountyIds)
        );
    }

    public void claimActiveBounty() {
        if (!(this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean claimed = BountyManager.claimActiveBounty(serverPlayer);
        if (claimed) {
            serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundRegistry.BOUNTY_COMPLETED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
            this.updateActiveData();
            this.updateContractPreviewSlot();
            BountyBoardNetworking.sendSync(
                    serverPlayer,
                    this.selectedBountyIndex,
                    this.hasActiveBounty(),
                    this.getActiveBounty().map(activeBounty -> this.bounties.indexOf(activeBounty)).orElse(-1),
                    this.getActiveProgress(),
                    this.getActiveRequiredKills(),
                    this.hasCompletedActiveBounty(),
                    new ArrayList<>(this.abandonedBountyIds)
            );
        }
    }

    private void syncAbandonedBountyIds() {
        if (this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            PlayerBountyData playerBountyData = BountyManager.getPlayerBountyData(serverPlayer);
            this.abandonedBountyIds.clear();
            this.abandonedBountyIds.addAll(playerBountyData.getAbandonedBounties());
        }
    }

    private void updateActiveData() {
        if (this.playerInventory.player instanceof ServerPlayer) {
            this.syncAbandonedBountyIds();
            PlayerBountyData playerBountyData = this.getPlayerBountyData();

            if (!playerBountyData.hasActiveBounty() || playerBountyData.getActiveBounty() == null) {
                this.hasActiveBounty.set(0);
                this.activeBountyIndex.set(-1);
                this.activeProgress.set(0);
                this.activeRequiredKills.set(0);
                this.activeCompleted.set(0);

                if (this.selectedBountyIndex < 0 || this.selectedBountyIndex >= this.bounties.size() || this.isBountyAbandoned(this.bounties.get(this.selectedBountyIndex).id())) {
                    this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
                }

                this.updateContractPreviewSlot();
                return;
            }

            BountyDefinition activeBounty = playerBountyData.getActiveBounty();
            int resolvedActiveBountyIndex = -1;

            for (int index = 0; index < this.bounties.size(); index++) {
                if (this.bounties.get(index).id().equals(activeBounty.id())) {
                    resolvedActiveBountyIndex = index;
                    break;
                }
            }

            this.hasActiveBounty.set(1);
            this.activeBountyIndex.set(resolvedActiveBountyIndex);
            this.activeProgress.set(playerBountyData.getCurrentProgress());
            this.activeRequiredKills.set(activeBounty.requiredKills());
            this.activeCompleted.set(playerBountyData.isCompleted() ? 1 : 0);
        }

        this.updateContractPreviewSlot();
    }

    private void updateContractPreviewSlot() {
        if (this.hasActiveBounty()) {
            this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
            return;
        }

        Optional<BountyDefinition> selectedBounty = this.getSelectedBounty();
        if (selectedBounty.isPresent()) {
            this.contractPreviewContainer.setItem(0, BountyManager.createContractStack(selectedBounty.get()));
            return;
        }

        this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
    }

    public void applySyncFromServer(int selectedBountyIndex, boolean hasActiveBounty, int activeBountyIndex, int activeProgress, int activeRequiredKills, boolean activeCompleted, List<UUID> abandonedBountyIds) {
        this.abandonedBountyIds.clear();
        this.abandonedBountyIds.addAll(abandonedBountyIds);

        this.selectedBountyIndex = selectedBountyIndex;
        if (this.selectedBountyIndex < 0 || this.selectedBountyIndex >= this.bounties.size() || this.isBountyAbandoned(this.bounties.get(this.selectedBountyIndex).id())) {
            this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        }

        this.hasActiveBounty.set(hasActiveBounty ? 1 : 0);
        this.activeBountyIndex.set(activeBountyIndex);
        this.activeProgress.set(activeProgress);
        this.activeRequiredKills.set(activeRequiredKills);
        this.activeCompleted.set(activeCompleted ? 1 : 0);
        this.updateContractPreviewSlot();
    }

    public List<UUID> getAbandonedBountyIds() {
        return this.abandonedBountyIds;
    }

    @Override
    public void broadcastChanges() {
        if (this.playerInventory.player instanceof ServerPlayer) {
            this.bounties = this.resolveBounties();
            this.syncAbandonedBountyIds();
            this.updateActiveData();
            this.updateContractPreviewSlot();
        }

        super.broadcastChanges();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}