package net.satisfy.wildernature.core.bounty;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.satisfy.wildernature.WilderNature;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BountyBoardSavedData extends SavedData {
    public static final String DATA_NAME = WilderNature.MOD_ID + "_bounty_board";
    private static final int DAILY_BOUNTY_COUNT = 15;

    private LocalDate currentDay = LocalDate.MIN;
    private final List<BountyDefinition> dailyBounties = new ArrayList<>();
    private final Map<UUID, PlayerBountyData> playerBountyDataMap = new LinkedHashMap<>();

    public static BountyBoardSavedData get(ServerLevel serverLevel) {
        return serverLevel.getServer().overworld().getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(BountyBoardSavedData::new, BountyBoardSavedData::load, null),
                DATA_NAME
        );
    }

    public List<BountyDefinition> getDailyBounties() {
        return this.dailyBounties;
    }

    public PlayerBountyData getPlayerBountyData(UUID playerId) {
        return this.playerBountyDataMap.computeIfAbsent(playerId, ignoredPlayerId -> {
            this.setDirty();
            return new PlayerBountyData();
        });
    }

    public void setPlayerBountyData(UUID playerId, PlayerBountyData playerBountyData) {
        this.playerBountyDataMap.put(playerId, playerBountyData);
        this.setDirty();
    }

    public void ensureCurrentBounties(ServerLevel serverLevel) {
        LocalDate currentDate = LocalDate.now();

        if (!currentDate.equals(this.currentDay) || this.dailyBounties.size() != DAILY_BOUNTY_COUNT) {
            this.currentDay = currentDate;
            this.dailyBounties.clear();
            this.dailyBounties.addAll(BountyGenerator.generateDailyBounties(serverLevel));
            this.setDirty();
        }
    }

    public Optional<BountyDefinition> getBounty(UUID bountyId) {
        return this.dailyBounties.stream().filter(bountyDefinition -> bountyDefinition.id().equals(bountyId)).findFirst();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putString("current_day", this.currentDay.toString());
        tag.putInt("daily_bounty_size", this.dailyBounties.size());

        for (int index = 0; index < this.dailyBounties.size(); index++) {
            tag.put("daily_bounty_" + index, this.dailyBounties.get(index).save());
        }

        tag.putInt("player_bounty_data_size", this.playerBountyDataMap.size());

        int playerIndex = 0;
        for (Map.Entry<UUID, PlayerBountyData> entry : this.playerBountyDataMap.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("player_id", entry.getKey());
            playerTag.put("data", entry.getValue().save());
            tag.put("player_bounty_data_" + playerIndex, playerTag);
            playerIndex++;
        }

        return tag;
    }

    public static BountyBoardSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        BountyBoardSavedData savedData = new BountyBoardSavedData();

        if (tag.contains("current_day")) {
            savedData.currentDay = LocalDate.parse(tag.getString("current_day"));
        }

        int dailyBountySize = tag.getInt("daily_bounty_size");
        for (int index = 0; index < dailyBountySize; index++) {
            savedData.dailyBounties.add(BountyDefinition.load(tag.getCompound("daily_bounty_" + index)));
        }

        int playerBountyDataSize = tag.getInt("player_bounty_data_size");
        for (int index = 0; index < playerBountyDataSize; index++) {
            CompoundTag playerTag = tag.getCompound("player_bounty_data_" + index);
            UUID playerId = playerTag.getUUID("player_id");
            PlayerBountyData playerBountyData = PlayerBountyData.load(playerTag.getCompound("data"));
            savedData.playerBountyDataMap.put(playerId, playerBountyData);
        }

        return savedData;
    }
}