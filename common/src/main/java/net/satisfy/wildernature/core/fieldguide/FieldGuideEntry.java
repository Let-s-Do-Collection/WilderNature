package net.satisfy.wildernature.core.fieldguide;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record FieldGuideEntry(
        ResourceLocation id,
        ResourceLocation entityId,
        boolean friendly,
        boolean neutral,
        boolean defensive,
        List<ResourceLocation> biomes
) {
    public static FieldGuideEntry fromJson(ResourceLocation id, JsonObject jsonObject) {
        ResourceLocation entityId = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "entity"));
        boolean friendly = GsonHelper.getAsBoolean(jsonObject, "friendly", false);
        boolean neutral = GsonHelper.getAsBoolean(jsonObject, "neutral", false);
        boolean defensive = GsonHelper.getAsBoolean(jsonObject, "defensive", false);

        JsonObject spawnObject = GsonHelper.getAsJsonObject(jsonObject, "spawn", new JsonObject());
        List<ResourceLocation> biomes = parseResourceLocationList(GsonHelper.getAsJsonArray(spawnObject, "biomes", new JsonArray()));

        return new FieldGuideEntry(id, entityId, friendly, neutral, defensive, List.copyOf(biomes));
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(this.id);
        friendlyByteBuf.writeResourceLocation(this.entityId);
        friendlyByteBuf.writeBoolean(this.friendly);
        friendlyByteBuf.writeBoolean(this.neutral);
        friendlyByteBuf.writeBoolean(this.defensive);
        friendlyByteBuf.writeCollection(this.biomes, FriendlyByteBuf::writeResourceLocation);
    }

    public static FieldGuideEntry read(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation id = friendlyByteBuf.readResourceLocation();
        ResourceLocation entityId = friendlyByteBuf.readResourceLocation();
        boolean friendly = friendlyByteBuf.readBoolean();
        boolean neutral = friendlyByteBuf.readBoolean();
        boolean defensive = friendlyByteBuf.readBoolean();
        List<ResourceLocation> biomes = friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation);
        return new FieldGuideEntry(id, entityId, friendly, neutral, defensive, List.copyOf(biomes));
    }

    private static List<ResourceLocation> parseResourceLocationList(JsonArray jsonArray) {
        List<ResourceLocation> resourceLocations = new ArrayList<>();

        for (int index = 0; index < jsonArray.size(); index++) {
            resourceLocations.add(ResourceLocation.parse(jsonArray.get(index).getAsString()));
        }

        return resourceLocations;
    }
}