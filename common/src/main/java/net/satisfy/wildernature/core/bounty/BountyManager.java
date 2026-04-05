package net.satisfy.wildernature.core.bounty;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    public static Optional<BountyDefinition> getBountyByIndex(ServerPlayer serverPlayer, int bountyIndex) {
        List<BountyDefinition> dailyBounties = getDailyBounties(serverPlayer.serverLevel());
        if (bountyIndex < 0 || bountyIndex >= dailyBounties.size()) {
            return Optional.empty();
        }

        return Optional.of(dailyBounties.get(bountyIndex));
    }

    public static boolean canPlayerSeeBounty(ServerPlayer serverPlayer, BountyDefinition bountyDefinition) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        return !playerBountyData.hasAbandoned(bountyDefinition.id());
    }

    public static boolean acceptBounty(ServerPlayer serverPlayer, UUID bountyId) {
        if (!canAcceptBounty(serverPlayer, bountyId)) {
            return false;
        }

        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
        Optional<BountyDefinition> optionalBountyDefinition = savedData.getBounty(bountyId);
        if (optionalBountyDefinition.isEmpty()) {
            return false;
        }

        BountyDefinition bountyDefinition = optionalBountyDefinition.get();
        ItemStack contractStack = createContractStack(bountyDefinition);

        if (!serverPlayer.addItem(contractStack)) {
            serverPlayer.drop(contractStack, false);
        }

        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        playerBountyData.setActiveBounty(bountyDefinition);
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
    }

    public static boolean canAcceptBounty(ServerPlayer serverPlayer, UUID bountyId) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        if (playerBountyData.hasActiveBounty()) {
            return false;
        }
        if (playerBountyData.hasAbandoned(bountyId)) {
            return false;
        }

        BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
        savedData.ensureCurrentBounties(serverPlayer.serverLevel());
        return savedData.getBounty(bountyId).isPresent();
    }



    public static boolean acceptBountyByIndex(ServerPlayer serverPlayer, int bountyIndex) {
        Optional<BountyDefinition> optionalBountyDefinition = getBountyByIndex(serverPlayer, bountyIndex);
        if (optionalBountyDefinition.isEmpty()) {
            return false;
        }

        return acceptBounty(serverPlayer, optionalBountyDefinition.get().id());
    }

    public static boolean abandonActiveBounty(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        if (!playerBountyData.hasActiveBounty()) {
            return false;
        }

        BountyDefinition activeBounty = playerBountyData.getActiveBounty();
        if (activeBounty != null) {
            removeContractItem(serverPlayer, activeBounty.id());
        }

        playerBountyData.abandonActiveBounty();
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
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
        playerBountyData.clearActiveBounty();
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
    }

    public static int getCurrentProgress(ServerPlayer serverPlayer) {
        return getPlayerBountyData(serverPlayer).getCurrentProgress();
    }

    public static int getRequiredProgress(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        if (playerBountyData.getActiveBounty() == null) {
            return 0;
        }

        return playerBountyData.getActiveBounty().requiredKills();
    }

    public static float getProgressRatio(ServerPlayer serverPlayer) {
        int requiredProgress = getRequiredProgress(serverPlayer);
        if (requiredProgress <= 0) {
            return 0.0F;
        }

        return Math.min(1.0F, (float) getCurrentProgress(serverPlayer) / (float) requiredProgress);
    }

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
}