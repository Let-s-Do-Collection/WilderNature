package net.satisfy.wildernature.core.bounty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public final class BountyManager {
    private BountyManager() {
    }

    public static List<BountyDefinition> getDailyBounties(ServerLevel serverLevel) {
        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverLevel);
        savedData.ensureCurrentBounties(serverLevel);
        return savedData.getDailyBounties();
    }

    public static PlayerBountyData getPlayerBountyData(ServerPlayer serverPlayer) {
        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
        savedData.ensureCurrentBounties(serverPlayer.serverLevel());
        return savedData.getPlayerBountyData(serverPlayer.getUUID());
    }

    public static void savePlayerBountyData(ServerPlayer serverPlayer, PlayerBountyData playerBountyData) {
        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
        savedData.ensureCurrentBounties(serverPlayer.serverLevel());
        savedData.setPlayerBountyData(serverPlayer.getUUID(), playerBountyData);
    }

    public static boolean acceptBounty(ServerPlayer serverPlayer, UUID bountyId) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);

        if (playerBountyData.hasActiveBounty()) {
            BountyDefinition activeBounty = playerBountyData.getActiveBounty();
            return activeBounty != null && activeBounty.id().equals(bountyId);
        }

        if (playerBountyData.hasAbandoned(bountyId)) {
            return false;
        }

        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
        Optional<BountyDefinition> optionalBountyDefinition = savedData.getBounty(bountyId);
        if (optionalBountyDefinition.isEmpty()) {
            return false;
        }

        playerBountyData.setActiveBounty(optionalBountyDefinition.get());
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
    }

    public static boolean hasContractItem(ServerPlayer serverPlayer, UUID bountyId) {
        for (int slotIndex = 0; slotIndex < serverPlayer.getInventory().getContainerSize(); slotIndex++) {
            ItemStack itemStack = serverPlayer.getInventory().getItem(slotIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                continue;
            }

            CompoundTag customDataTag = customData.copyTag();
            if (customDataTag.hasUUID("BountyId") && customDataTag.getUUID("BountyId").equals(bountyId)) {
                return true;
            }
        }

        return false;
    }

    public static ItemStack createContractStack(BountyDefinition bountyDefinition) {
        ItemStack contractStack = new ItemStack(getContractItem(bountyDefinition));
        CompoundTag customDataTag = new CompoundTag();
        customDataTag.putUUID("BountyId", bountyDefinition.id());
        customDataTag.putString("EntityId", bountyDefinition.entityId().toString());
        customDataTag.putInt("RequiredKills", bountyDefinition.requiredKills());
        customDataTag.putInt("ExperienceReward", bountyDefinition.reward().experienceReward());
        customDataTag.putString("PreviewItemId", bountyDefinition.reward().previewItemId().toString());
        customDataTag.putInt("PreviewCount", bountyDefinition.reward().previewCount());
        customDataTag.putInt("Progress", 0);
        contractStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        return contractStack;
    }

    private static Item getContractItem(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.category()) {
            case NEUTRAL -> ObjectRegistry.COMMON_CONTRACT.get();
            case DEFENSIVE -> ObjectRegistry.UNCOMMON_CONTRACT.get();
            case AGGRESSIVE -> ObjectRegistry.RARE_CONTRACT.get();
            case BOSS -> ObjectRegistry.LEVELING_CONTRACT.get();
        };
    }

    public static void removeContractItem(ServerPlayer serverPlayer, UUID bountyId) {
        for (int slotIndex = 0; slotIndex < serverPlayer.getInventory().getContainerSize(); slotIndex++) {
            ItemStack itemStack = serverPlayer.getInventory().getItem(slotIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                continue;
            }

            CompoundTag customDataTag = customData.copyTag();
            if (customDataTag.hasUUID("BountyId") && customDataTag.getUUID("BountyId").equals(bountyId)) {
                serverPlayer.getInventory().setItem(slotIndex, ItemStack.EMPTY);
                return;
            }
        }
    }

    public static boolean claimActiveBounty(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        if (!playerBountyData.isCompleted() || playerBountyData.getActiveBounty() == null) {
            return false;
        }

        BountyDefinition activeBounty = playerBountyData.getActiveBounty();

        ItemStack rewardStack = new ItemStack(BuiltInRegistries.ITEM.get(activeBounty.reward().previewItemId()), activeBounty.reward().previewCount());
        if (!serverPlayer.addItem(rewardStack)) {
            serverPlayer.drop(rewardStack, false);
        }

        int experienceReward = activeBounty.reward().experienceReward();
        if (experienceReward > 0) {
            serverPlayer.giveExperiencePoints(experienceReward);
        }

        removeContractItem(serverPlayer, activeBounty.id());
        playerBountyData.getAbandonedBounties().add(activeBounty.id());
        playerBountyData.clearActiveBounty();
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
    }

    @SuppressWarnings("deprecation")
    public static void handleEntityKilled(Level level, Entity killedEntity, List<UUID> assistingPlayers) {
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (UUID playerId : assistingPlayers) {
            ServerPlayer serverPlayer = serverLevel.getServer().getPlayerList().getPlayer(playerId);
            if (serverPlayer == null) {
                continue;
            }

            PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
            BountyDefinition activeBounty = playerBountyData.getActiveBounty();

            if (activeBounty == null || playerBountyData.isCompleted()) {
                continue;
            }

            if (killedEntity.getType().builtInRegistryHolder().key().location().equals(activeBounty.entityId())) {
                playerBountyData.incrementProgress();
                updateContractProgress(serverPlayer, activeBounty.id(), playerBountyData.getCurrentProgress());
                savePlayerBountyData(serverPlayer, playerBountyData);
            }
        }
    }

    private static void updateContractProgress(ServerPlayer serverPlayer, UUID bountyId, int progress) {
        for (int slotIndex = 0; slotIndex < serverPlayer.getInventory().getContainerSize(); slotIndex++) {
            ItemStack itemStack = serverPlayer.getInventory().getItem(slotIndex);
            if (itemStack.isEmpty()) {
                continue;
            }

            CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
            if (customData == null) {
                continue;
            }

            CompoundTag customDataTag = customData.copyTag();
            if (!customDataTag.hasUUID("BountyId") || !customDataTag.getUUID("BountyId").equals(bountyId)) {
                continue;
            }

            customDataTag.putInt("Progress", progress);
            itemStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
            return;
        }
    }

    public static void giveOrRestoreContract(ServerPlayer serverPlayer, BountyDefinition bountyDefinition, int progress) {
        removeContractItem(serverPlayer, bountyDefinition.id());

        ItemStack contractStack = createContractStack(bountyDefinition);
        CustomData customData = contractStack.get(DataComponents.CUSTOM_DATA);

        if (customData != null) {
            CompoundTag customDataTag = customData.copyTag();
            customDataTag.putInt("Progress", progress);
            contractStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        }

        if (!serverPlayer.addItem(contractStack)) {
            serverPlayer.drop(contractStack, false);
        }
    }
}