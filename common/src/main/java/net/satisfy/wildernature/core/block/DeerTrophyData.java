package net.satisfy.wildernature.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class DeerTrophyData extends SavedData {
    private static final String DATA_NAME = "deer_trophy_data";
    private static final int MAX_CHARGES = 3;

    private final Map<BlockPos, Integer> charges = new HashMap<>();
    private final Map<BlockPos, Long> lastUsed = new HashMap<>();

    public static DeerTrophyData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DeerTrophyData::new, DeerTrophyData::load, DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES),
                DATA_NAME
        );
    }

    public int getCharges(BlockPos pos) {
        return charges.getOrDefault(pos, MAX_CHARGES);
    }

    public void setCharges(BlockPos pos, int value) {
        charges.put(pos, value);
        setDirty();
    }

    public long getLastUsed(BlockPos pos) {
        return lastUsed.getOrDefault(pos, 0L);
    }

    public void setLastUsed(BlockPos pos, long time) {
        lastUsed.put(pos, time);
        setDirty();
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag chargesTag = new CompoundTag();
        charges.forEach((pos, value) -> chargesTag.putInt(posToKey(pos), value));
        tag.put("charges", chargesTag);

        CompoundTag lastUsedTag = new CompoundTag();
        lastUsed.forEach((pos, time) -> lastUsedTag.putLong(posToKey(pos), time));
        tag.put("lastUsed", lastUsedTag);

        return tag;
    }

    private static DeerTrophyData load(CompoundTag tag, HolderLookup.Provider provider) {
        DeerTrophyData data = new DeerTrophyData();

        CompoundTag chargesTag = tag.getCompound("charges");
        for (String key : chargesTag.getAllKeys()) {
            data.charges.put(keyToPos(key), chargesTag.getInt(key));
        }

        CompoundTag lastUsedTag = tag.getCompound("lastUsed");
        for (String key : lastUsedTag.getAllKeys()) {
            data.lastUsed.put(keyToPos(key), lastUsedTag.getLong(key));
        }

        return data;
    }

    private static String posToKey(BlockPos pos) {
        return pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
    }

    private static BlockPos keyToPos(String key) {
        String[] parts = key.split("_");
        return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }
}