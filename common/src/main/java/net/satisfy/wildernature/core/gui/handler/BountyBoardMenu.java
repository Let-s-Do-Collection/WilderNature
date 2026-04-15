package net.satisfy.wildernature.core.gui.handler;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.satisfy.wildernature.core.bounty.*;
import net.satisfy.wildernature.core.network.BountyBoardNetworking;
import net.satisfy.wildernature.core.registry.MenuTypeRegistry;
import net.satisfy.wildernature.core.registry.SoundEventRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BountyBoardMenu extends AbstractContainerMenu {
    private final SimpleContainer contractPreviewContainer = new SimpleContainer(1);
    private final SimpleContainer rewardContainer = new SimpleContainer(2);
    private final Inventory playerInventory;
    private List<BountyDefinition> bounties;
    private int selectedBountyIndex = -1;
    private final DataSlot activeProgress = DataSlot.standalone();
    private final DataSlot activeRequiredKills = DataSlot.standalone();
    private final DataSlot activeCompleted = DataSlot.standalone();
    private final DataSlot hasActiveBounty = DataSlot.standalone();
    private final DataSlot activeBountyIndex = DataSlot.standalone();
    private final DataSlot restoreContractAvailable = DataSlot.standalone();
    private final DataSlot rewardsUnlocked = DataSlot.standalone();
    private final List<UUID> abandonedBountyIds = new ArrayList<>();

    public BountyBoardMenu(int containerId, Inventory playerInventory) {
        super(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.bounties = this.resolveBounties();
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.addContractPreviewSlot();
        this.addRewardSlots();
        this.addPlayerInventorySlots(playerInventory);
        this.addDataSlot(this.hasActiveBounty);
        this.addDataSlot(this.activeBountyIndex);
        this.addDataSlot(this.activeProgress);
        this.addDataSlot(this.activeRequiredKills);
        this.addDataSlot(this.activeCompleted);
        this.addDataSlot(this.restoreContractAvailable);
        this.addDataSlot(this.rewardsUnlocked);
        this.updateActiveData();
        this.updateContractPreviewSlot();
    }

    public BountyBoardMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buffer) {
        super(MenuTypeRegistry.BOUNTY_BOARD_MENU.get(), containerId);
        this.playerInventory = playerInventory;
        this.bounties = this.readBounties(buffer);
        this.selectedBountyIndex = buffer.readVarInt();
        this.hasActiveBounty.set(buffer.readVarInt());
        this.activeBountyIndex.set(buffer.readVarInt());
        this.activeProgress.set(buffer.readVarInt());
        this.activeRequiredKills.set(buffer.readVarInt());
        this.activeCompleted.set(buffer.readVarInt());
        this.restoreContractAvailable.set(buffer.readVarInt());
        this.rewardsUnlocked.set(buffer.readVarInt());

        if (this.hasActiveBounty()) {
            this.selectedBountyIndex = this.activeBountyIndex.get();
        }

        if (this.selectedBountyIndex < 0 || this.selectedBountyIndex >= this.bounties.size()) {
            this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        }

        this.addContractPreviewSlot();
        this.addRewardSlots();
        this.addPlayerInventorySlots(playerInventory);
        this.addDataSlot(this.hasActiveBounty);
        this.addDataSlot(this.activeBountyIndex);
        this.addDataSlot(this.activeProgress);
        this.addDataSlot(this.activeRequiredKills);
        this.addDataSlot(this.activeCompleted);
        this.addDataSlot(this.restoreContractAvailable);
        this.addDataSlot(this.rewardsUnlocked);
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
                if (BountyBoardMenu.this.hasTurnInContractInserted()) {
                    return false;
                }

                return BountyBoardMenu.this.canTurnInActiveContract(itemStack) || BountyBoardMenu.this.canRestoreActiveContract(itemStack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
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

                ItemStack slotStack = this.getItem();
                if (BountyBoardMenu.this.canTurnInActiveContract(slotStack)) {
                    BountyBoardMenu.this.unlockRewardSlots(serverPlayer);
                    return;
                }

                if (BountyBoardMenu.this.canRestoreActiveContract(slotStack)) {
                    BountyBoardMenu.this.restoreActiveContract(serverPlayer);
                }
            }
        });
    }

    private void addRewardSlots() {
        this.addSlot(new Slot(this.rewardContainer, 0, 166, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return BountyBoardMenu.this.areRewardSlotsUnlocked() && this.hasItem();
            }

            @Override
            public void setChanged() {
                super.setChanged();
                BountyBoardMenu.this.tryFinalizeUnlockedRewards();
            }
        });

        this.addSlot(new Slot(this.rewardContainer, 1, 184, 50) {
            @Override
            public boolean mayPlace(@NotNull ItemStack itemStack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player player) {
                return BountyBoardMenu.this.areRewardSlotsUnlocked() && this.hasItem();
            }

            @Override
            public void setChanged() {
                super.setChanged();
                BountyBoardMenu.this.tryFinalizeUnlockedRewards();
            }
        });
    }

    public boolean hasTurnInContractInserted() {
        if (!this.hasActiveBounty() || !this.hasCompletedActiveBounty()) {
            return false;
        }

        ItemStack contractStack = this.contractPreviewContainer.getItem(0);
        return this.canTurnInActiveContract(contractStack);
    }

    private boolean canTurnInActiveContract(ItemStack itemStack) {
        if (!this.hasCompletedActiveBounty() || this.areRewardSlotsUnlocked()) {
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

    private boolean canRestoreActiveContract(ItemStack itemStack) {
        if (!this.hasRestoreContractAvailable()) {
            return false;
        }

        if (!itemStack.is(Items.EMERALD) || itemStack.isEmpty()) {
            return false;
        }

        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty() || this.hasCompletedActiveBounty()) {
            return false;
        }

        return !this.hasPlayerContractItem(activeBounty.get().id());
    }

    private void restoreActiveContract(ServerPlayer serverPlayer) {
        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty()) {
            return;
        }

        if (this.hasPlayerContractItem(activeBounty.get().id())) {
            return;
        }

        ItemStack slotStack = this.contractPreviewContainer.getItem(0);
        if (!this.canRestoreActiveContract(slotStack)) {
            return;
        }

        PlayerBountyData playerBountyData = BountyManager.getPlayerBountyData(serverPlayer);
        ItemStack restoredContractStack = BountyManager.createContractStack(activeBounty.get());
        CustomData customData = restoredContractStack.get(DataComponents.CUSTOM_DATA);

        if (customData != null) {
            CompoundTag customDataTag = customData.copyTag();
            customDataTag.putInt("Progress", playerBountyData.getCurrentProgress());
            restoredContractStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        }

        slotStack.shrink(1);
        this.contractPreviewContainer.setItem(0, restoredContractStack);
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEventRegistry.BOUNTY_ACCEPTED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.updateActiveData();
        this.syncMenuState(serverPlayer);
    }

    private boolean restoreActiveContractFromInventorySlot(ServerPlayer serverPlayer, Slot sourceSlot) {
        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty()) {
            return false;
        }

        if (this.hasPlayerContractItem(activeBounty.get().id())) {
            return false;
        }

        if (!this.contractPreviewContainer.getItem(0).isEmpty()) {
            return false;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        if (!sourceStack.is(Items.EMERALD) || sourceStack.isEmpty()) {
            return false;
        }

        PlayerBountyData playerBountyData = BountyManager.getPlayerBountyData(serverPlayer);
        ItemStack restoredContractStack = BountyManager.createContractStack(activeBounty.get());
        CustomData customData = restoredContractStack.get(DataComponents.CUSTOM_DATA);

        if (customData != null) {
            CompoundTag customDataTag = customData.copyTag();
            customDataTag.putInt("Progress", playerBountyData.getCurrentProgress());
            restoredContractStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        }

        sourceStack.shrink(1);
        sourceSlot.setChanged();
        this.contractPreviewContainer.setItem(0, restoredContractStack);
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEventRegistry.BOUNTY_ACCEPTED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.updateActiveData();
        this.syncMenuState(serverPlayer);
        return true;
    }

    private boolean moveCompletedContractIntoTurnInSlot(Slot sourceSlot) {
        ItemStack sourceStack = sourceSlot.getItem();
        if (!this.canTurnInActiveContract(sourceStack)) {
            return false;
        }

        ItemStack turnInStack = sourceStack.copyWithCount(1);
        this.contractPreviewContainer.setItem(0, turnInStack);
        sourceStack.shrink(1);
        sourceSlot.setChanged();
        this.getSlot(0).setChanged();
        return true;
    }

    private void unlockRewardSlots(ServerPlayer serverPlayer) {
        Optional<BountyDefinition> activeBounty = this.getActiveBounty();
        if (activeBounty.isEmpty()) {
            return;
        }

        ItemStack contractStack = this.contractPreviewContainer.getItem(0);
        if (!this.canTurnInActiveContract(contractStack)) {
            return;
        }

        ItemStack rewardItemStack = new ItemStack(BuiltInRegistries.ITEM.get(activeBounty.get().reward().previewItemId()), activeBounty.get().reward().previewCount());
        ItemStack rewardExperienceStack = BountyManager.createExperienceBurstStack(activeBounty.get().reward().experienceReward());

        this.rewardContainer.setItem(0, rewardItemStack);
        this.rewardContainer.setItem(1, rewardExperienceStack);
        this.rewardsUnlocked.set(1);
        this.updateActiveData();
        this.syncMenuState(serverPlayer);
    }

    private void tryFinalizeUnlockedRewards() {
        if (!(this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (!this.areRewardSlotsUnlocked()) {
            return;
        }

        if (!this.rewardContainer.getItem(0).isEmpty() || !this.rewardContainer.getItem(1).isEmpty()) {
            return;
        }

        if (!BountyManager.completeActiveBountyWithoutRewards(serverPlayer)) {
            return;
        }

        this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
        this.rewardContainer.setItem(0, ItemStack.EMPTY);
        this.rewardContainer.setItem(1, ItemStack.EMPTY);
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.updateActiveData();
        this.syncMenuState(serverPlayer);
    }

    private void syncMenuState(ServerPlayer serverPlayer) {
        this.updateContractPreviewSlot();
        BountyBoardNetworking.sendSync(
                serverPlayer,
                this.selectedBountyIndex,
                this.hasActiveBounty(),
                this.getActiveBounty().map(activeBounty -> this.bounties.indexOf(activeBounty)).orElse(-1),
                this.getActiveProgress(),
                this.getActiveRequiredKills(),
                this.hasCompletedActiveBounty(),
                this.hasRestoreContractAvailable(),
                new ArrayList<>(this.abandonedBountyIds)
        );
    }

    private List<BountyDefinition> resolveBounties() {
        if (this.playerInventory.player instanceof ServerPlayer serverPlayer) {
            return BountyManager.getDailyBounties(serverPlayer.serverLevel());
        }

        return List.of();
    }

    public static void writeBounties(FriendlyByteBuf friendlyByteBuf, List<BountyDefinition> bountyDefinitions, List<UUID> abandonedBountyIds) {
        friendlyByteBuf.writeVarInt(bountyDefinitions.size());
        friendlyByteBuf.writeVarInt(abandonedBountyIds.size());

        for (UUID abandonedBountyId : abandonedBountyIds) {
            friendlyByteBuf.writeUUID(abandonedBountyId);
        }

        for (BountyDefinition bountyDefinition : bountyDefinitions) {
            friendlyByteBuf.writeUUID(bountyDefinition.id());
            friendlyByteBuf.writeUtf(bountyDefinition.type().getSerializedName());
            friendlyByteBuf.writeUtf(bountyDefinition.targetType().getSerializedName());
            friendlyByteBuf.writeUtf(bountyDefinition.category().getName());
            friendlyByteBuf.writeUtf(bountyDefinition.targetId().toString());
            friendlyByteBuf.writeVarInt(bountyDefinition.requiredAmount());
            friendlyByteBuf.writeUtf(bountyDefinition.reward().lootTableId().toString());
            friendlyByteBuf.writeVarInt(bountyDefinition.reward().experienceReward());
            friendlyByteBuf.writeUtf(bountyDefinition.reward().previewItemId().toString());
            friendlyByteBuf.writeVarInt(bountyDefinition.reward().previewCount());
            friendlyByteBuf.writeBoolean(bountyDefinition.guildCommission());
        }
    }

    private List<BountyDefinition> readBounties(FriendlyByteBuf friendlyByteBuf) {
        int bountyCount = friendlyByteBuf.readVarInt();
        List<BountyDefinition> syncedBounties = new ArrayList<>(bountyCount);

        this.abandonedBountyIds.clear();
        int abandonedCount = friendlyByteBuf.readVarInt();
        for (int index = 0; index < abandonedCount; index++) {
            this.abandonedBountyIds.add(friendlyByteBuf.readUUID());
        }

        for (int index = 0; index < bountyCount; index++) {
            UUID bountyId = friendlyByteBuf.readUUID();
            String typeName = friendlyByteBuf.readUtf();
            String targetTypeName = friendlyByteBuf.readUtf();
            String categoryName = friendlyByteBuf.readUtf();
            String targetIdString = friendlyByteBuf.readUtf();
            int requiredAmount = friendlyByteBuf.readVarInt();
            String lootTableIdString = friendlyByteBuf.readUtf();
            int experienceReward = friendlyByteBuf.readVarInt();
            String previewItemIdString = friendlyByteBuf.readUtf();
            int previewCount = friendlyByteBuf.readVarInt();
            boolean guildCommission = friendlyByteBuf.readBoolean();

            syncedBounties.add(new BountyDefinition(bountyId, BountyDefinition.BountyType.byName(typeName), BountyDefinition.BountyTargetType.byName(targetTypeName), BountyCategory.byName(categoryName), ResourceLocation.parse(targetIdString), requiredAmount, new BountyReward(ResourceLocation.parse(lootTableIdString), experienceReward, ResourceLocation.parse(previewItemIdString), previewCount), guildCommission));
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
        return this.restoreContractAvailable.get() == 1;
    }

    public boolean areRewardSlotsUnlocked() {
        return this.rewardsUnlocked.get() == 1;
    }

    public void acceptSelectedBounty() {
        if (!(this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (this.hasTurnInContractInserted()) {
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
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEventRegistry.BOUNTY_ACCEPTED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.updateActiveData();
        this.syncMenuState(serverPlayer);
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
        serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEventRegistry.BOUNTY_CANCELED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

        this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
        this.rewardContainer.setItem(0, ItemStack.EMPTY);
        this.rewardContainer.setItem(1, ItemStack.EMPTY);
        this.syncAbandonedBountyIds();
        this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        this.updateActiveData();
        this.syncMenuState(serverPlayer);
    }

    public void claimActiveBounty() {
        if (!(this.playerInventory.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean claimed = BountyManager.claimActiveBounty(serverPlayer);
        if (claimed) {
            serverPlayer.serverLevel().playSound(null, serverPlayer.blockPosition(), SoundEventRegistry.BOUNTY_COMPLETED.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
            this.updateActiveData();
            this.syncMenuState(serverPlayer);
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
                this.restoreContractAvailable.set(0);
                this.rewardsUnlocked.set(0);

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

            boolean hasInsertedTurnInContract = this.canTurnInActiveContract(this.contractPreviewContainer.getItem(0));
            boolean rewardsAreUnlocked = !this.rewardContainer.getItem(0).isEmpty() || !this.rewardContainer.getItem(1).isEmpty();

            if (playerBountyData.isCompleted() && hasInsertedTurnInContract && !rewardsAreUnlocked) {
                ItemStack rewardItemStack = new ItemStack(BuiltInRegistries.ITEM.get(activeBounty.reward().previewItemId()), activeBounty.reward().previewCount());
                ItemStack rewardExperienceStack = BountyManager.createExperienceBurstStack(activeBounty.reward().experienceReward());
                this.rewardContainer.setItem(0, rewardItemStack);
                this.rewardContainer.setItem(1, rewardExperienceStack);
                rewardsAreUnlocked = true;
            }

            this.selectedBountyIndex = resolvedActiveBountyIndex;
            this.hasActiveBounty.set(1);
            this.activeBountyIndex.set(resolvedActiveBountyIndex);
            this.activeProgress.set(playerBountyData.getCurrentProgress());
            this.activeRequiredKills.set(activeBounty.requiredAmount());
            this.activeCompleted.set(playerBountyData.isCompleted() ? 1 : 0);
            this.rewardsUnlocked.set(rewardsAreUnlocked ? 1 : 0);
            this.restoreContractAvailable.set(!playerBountyData.isCompleted() && !rewardsAreUnlocked && !this.hasPlayerContractItem(activeBounty.id()) ? 1 : 0);
        }

        this.updateContractPreviewSlot();
    }

    private void updateContractPreviewSlot() {
        if (this.hasActiveBounty()) {
            Optional<BountyDefinition> activeBounty = this.getActiveBounty();
            if (activeBounty.isPresent()) {
                if (this.hasTurnInContractInserted()) {
                    return;
                }

                if (this.hasCompletedActiveBounty()) {
                    this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
                    return;
                }

                if (this.hasRestoreContractAvailable()) {
                    this.contractPreviewContainer.setItem(0, new ItemStack(Items.EMERALD));
                    return;
                }

                this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
                return;
            }
        }

        Optional<BountyDefinition> selectedBounty = this.getSelectedBounty();
        if (selectedBounty.isPresent()) {
            this.contractPreviewContainer.setItem(0, BountyManager.createContractStack(selectedBounty.get()));
            return;
        }

        this.contractPreviewContainer.setItem(0, ItemStack.EMPTY);
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

    public void applySyncFromServer(int selectedBountyIndex, boolean hasActiveBounty, int activeBountyIndex, int activeProgress, int activeRequiredKills, boolean activeCompleted, boolean restoreContractAvailable, List<UUID> abandonedBountyIds) {
        this.abandonedBountyIds.clear();
        this.abandonedBountyIds.addAll(abandonedBountyIds);

        this.selectedBountyIndex = hasActiveBounty ? activeBountyIndex : selectedBountyIndex;
        if (this.selectedBountyIndex < 0 || this.selectedBountyIndex >= this.bounties.size() || (!hasActiveBounty && this.isBountyAbandoned(this.bounties.get(this.selectedBountyIndex).id()))) {
            this.selectedBountyIndex = this.findFirstSelectableBountyIndex();
        }

        this.hasActiveBounty.set(hasActiveBounty ? 1 : 0);
        this.activeBountyIndex.set(activeBountyIndex);
        this.activeProgress.set(activeProgress);
        this.activeRequiredKills.set(activeRequiredKills);
        this.activeCompleted.set(activeCompleted ? 1 : 0);
        this.restoreContractAvailable.set(restoreContractAvailable ? 1 : 0);
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
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return ItemStack.EMPTY;
        }

        if (index < 0 || index >= this.slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack originalStack = sourceStack.copy();

        if (index == 0) {
            return ItemStack.EMPTY;
        }

        if ((index == 1 || index == 2) && this.areRewardSlotsUnlocked()) {
            if (!this.moveItemStackTo(sourceStack, 3, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }

            if (sourceStack.isEmpty()) {
                sourceSlot.set(ItemStack.EMPTY);
            } else {
                sourceSlot.setChanged();
            }

            return originalStack;
        }

        if (!this.hasTurnInContractInserted() && this.hasCompletedActiveBounty() && !this.areRewardSlotsUnlocked() && this.moveCompletedContractIntoTurnInSlot(sourceSlot)) {
            return originalStack;
        }

        if (!this.hasTurnInContractInserted() && this.hasRestoreContractAvailable() && sourceStack.is(Items.EMERALD)) {
            if (this.restoreActiveContractFromInventorySlot(serverPlayer, sourceSlot)) {
                return originalStack;
            }

            return ItemStack.EMPTY;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}