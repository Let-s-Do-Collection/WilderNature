package net.satisfy.wildernature.core.bounty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

@SuppressWarnings("deprecation")
public final class BountyManager {
    private BountyManager() {
    }

    public static ItemStack createPreviewStack(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.type()) {
            case GATHER -> new ItemStack(BuiltInRegistries.ITEM.get(bountyDefinition.targetId()));
            case EXPLORE -> new ItemStack(Items.COMPASS);
            case OBSERVE -> new ItemStack(Items.SPYGLASS);
            case HUNT -> new ItemStack(getContractItem(bountyDefinition));
        };
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

    public static ItemStack createContractStack(BountyDefinition bountyDefinition) {
        ItemStack contractStack = new ItemStack(getContractItem(bountyDefinition));
        CompoundTag customDataTag = new CompoundTag();
        customDataTag.putUUID("BountyId", bountyDefinition.id());
        customDataTag.putString("BountyType", bountyDefinition.type().getSerializedName());
        customDataTag.putString("TargetType", bountyDefinition.targetType().getSerializedName());
        customDataTag.putString("TargetId", bountyDefinition.targetId().toString());
        customDataTag.putString("Category", bountyDefinition.category().getName());
        customDataTag.putInt("RequiredAmount", bountyDefinition.requiredAmount());
        customDataTag.putInt("ExperienceReward", bountyDefinition.reward().experienceReward());
        customDataTag.putString("PreviewItemId", bountyDefinition.reward().previewItemId().toString());
        customDataTag.putInt("PreviewCount", bountyDefinition.reward().previewCount());
        customDataTag.putInt("Progress", 0);
        contractStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        return contractStack;
    }

    private static Item getContractItem(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.type()) {
            case HUNT -> bountyDefinition.category() == BountyCategory.BOSS
                    ? ObjectRegistry.ELITE_BOUNTY.get()
                    : ObjectRegistry.TRACKING_ORDER.get();
            case GATHER -> ObjectRegistry.PROVISION_REQUEST.get();
            case OBSERVE -> ObjectRegistry.FIELD_NOTES.get();
            case EXPLORE -> ObjectRegistry.PATHFINDERS_CALL.get();
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

    public static boolean completeActiveBountyWithoutRewards(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        if (!playerBountyData.isCompleted() || playerBountyData.getActiveBounty() == null) {
            return false;
        }

        BountyDefinition activeBounty = playerBountyData.getActiveBounty();
        removeContractItem(serverPlayer, activeBounty.id());
        playerBountyData.getAbandonedBounties().add(activeBounty.id());
        playerBountyData.clearActiveBounty();
        savePlayerBountyData(serverPlayer, playerBountyData);
        return true;
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

    public static ItemStack createExperienceBurstStack(int experienceAmount) {
        ItemStack burstStack = new ItemStack(ObjectRegistry.BURST_OF_EXPERIENCE.get());
        CompoundTag customDataTag = new CompoundTag();
        customDataTag.putInt("ExperienceAmount", experienceAmount);
        burstStack.set(DataComponents.CUSTOM_DATA, CustomData.of(customDataTag));
        return burstStack;
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

            if (activeBounty == null || playerBountyData.isCompleted() || activeBounty.type() != BountyDefinition.BountyType.HUNT) {
                continue;
            }

            if (killedEntity.getType().builtInRegistryHolder().key().location().equals(activeBounty.targetId())) {
                playerBountyData.incrementProgress(1);
                updateContractProgress(serverPlayer, activeBounty.id(), playerBountyData.getCurrentProgress());
                savePlayerBountyData(serverPlayer, playerBountyData);
            }
        }
    }

    public static void handlePlayerTick(ServerPlayer serverPlayer) {
        PlayerBountyData playerBountyData = getPlayerBountyData(serverPlayer);
        BountyDefinition activeBounty = playerBountyData.getActiveBounty();

        if (activeBounty == null || playerBountyData.isCompleted()) {
            return;
        }

        switch (activeBounty.type()) {
            case GATHER -> handleGatherTick(serverPlayer, playerBountyData, activeBounty);
            case OBSERVE -> handleObserveTick(serverPlayer, playerBountyData, activeBounty);
            case EXPLORE -> handleExploreTick(serverPlayer, playerBountyData, activeBounty);
            default -> {
            }
        }
    }

    private static void handleGatherTick(ServerPlayer serverPlayer, PlayerBountyData playerBountyData, BountyDefinition activeBounty) {
        int matchingItemCount = 0;

        for (int slotIndex = 0; slotIndex < serverPlayer.getInventory().getContainerSize(); slotIndex++) {
            ItemStack inventoryStack = serverPlayer.getInventory().getItem(slotIndex);
            if (inventoryStack.isEmpty()) {
                continue;
            }

            ResourceLocation inventoryItemId = BuiltInRegistries.ITEM.getKey(inventoryStack.getItem());
            if (inventoryItemId.equals(activeBounty.targetId())) {
                matchingItemCount += inventoryStack.getCount();
            }
        }

        int targetProgress = Math.min(activeBounty.requiredAmount(), matchingItemCount);
        thisSetProgress(serverPlayer, playerBountyData, activeBounty, targetProgress);
    }

    private static void handleObserveTick(ServerPlayer serverPlayer, PlayerBountyData playerBountyData, BountyDefinition activeBounty) {
        if (!serverPlayer.isUsingItem() || !serverPlayer.getUseItem().is(Items.SPYGLASS)) {
            return;
        }

        Vec3 eyePosition = serverPlayer.getEyePosition();
        Vec3 viewVector = serverPlayer.getViewVector(1.0F);
        Vec3 reachEnd = eyePosition.add(viewVector.scale(32.0D));
        AABB searchBox = serverPlayer.getBoundingBox().expandTowards(viewVector.scale(32.0D)).inflate(1.5D);

        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                serverPlayer,
                eyePosition,
                reachEnd,
                searchBox,
                targetEntity -> !targetEntity.isSpectator() && targetEntity.isPickable(),
                1024.0D
        );

        if (entityHitResult == null) {
            return;
        }

        Entity observedEntity = entityHitResult.getEntity();
        ResourceLocation observedEntityId = observedEntity.getType().builtInRegistryHolder().key().location();
        if (!observedEntityId.equals(activeBounty.targetId())) {
            return;
        }

        thisSetProgress(serverPlayer, playerBountyData, activeBounty, activeBounty.requiredAmount());
    }

    private static void handleExploreTick(ServerPlayer serverPlayer, PlayerBountyData playerBountyData, BountyDefinition activeBounty) {
        Holder<Biome> biomeHolder = serverPlayer.serverLevel().getBiome(serverPlayer.blockPosition());
        Optional<ResourceKey<Biome>> biomeKey = biomeHolder.unwrapKey();

        if (biomeKey.isEmpty()) {
            return;
        }

        if (!biomeKey.get().location().equals(activeBounty.targetId())) {
            return;
        }

        thisSetProgress(serverPlayer, playerBountyData, activeBounty, activeBounty.requiredAmount());
    }

    private static void thisSetProgress(ServerPlayer serverPlayer, PlayerBountyData playerBountyData, BountyDefinition activeBounty, int targetProgress) {
        int currentProgress = playerBountyData.getCurrentProgress();
        int clampedProgress = Math.min(activeBounty.requiredAmount(), targetProgress);

        if (clampedProgress <= currentProgress) {
            return;
        }

        playerBountyData.incrementProgress(clampedProgress - currentProgress);
        updateContractProgress(serverPlayer, activeBounty.id(), playerBountyData.getCurrentProgress());
        savePlayerBountyData(serverPlayer, playerBountyData);
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