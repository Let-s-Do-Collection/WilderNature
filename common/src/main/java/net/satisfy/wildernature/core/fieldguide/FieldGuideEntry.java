package net.satisfy.wildernature.core.fieldguide;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public record FieldGuideEntry(
        ResourceLocation id,
        ResourceLocation entityId,
        ResourceLocation ambientSound,
        boolean friendly,
        boolean neutral,
        boolean defensive,
        boolean tameable,
        boolean male,
        String food,
        List<ResourceLocation> biomes
) {
    public static FieldGuideEntry fromJson(ResourceLocation id, JsonObject jsonObject) {
        ResourceLocation entityId = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "entity"));
        ResourceLocation ambientSound = jsonObject.has("ambient_sound") ? ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "ambient_sound")) : null;
        boolean friendly = GsonHelper.getAsBoolean(jsonObject, "friendly", false);
        boolean neutral = GsonHelper.getAsBoolean(jsonObject, "neutral", false);
        boolean defensive = GsonHelper.getAsBoolean(jsonObject, "defensive", false);
        boolean tameable = GsonHelper.getAsBoolean(jsonObject, "tameable", false);
        boolean male = GsonHelper.getAsBoolean(jsonObject, "male", false);
        String food = jsonObject.has("food") ? GsonHelper.getAsString(jsonObject, "food") : null;

        JsonObject spawnObject = GsonHelper.getAsJsonObject(jsonObject, "spawn", new JsonObject());
        List<ResourceLocation> biomes = parseResourceLocationList(GsonHelper.getAsJsonArray(spawnObject, "biomes", new JsonArray()));

        return new FieldGuideEntry(id, entityId, ambientSound, friendly, neutral, defensive, tameable, male, food, List.copyOf(biomes));
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(this.id);
        friendlyByteBuf.writeResourceLocation(this.entityId);
        friendlyByteBuf.writeBoolean(this.ambientSound != null);
        if (this.ambientSound != null) {
            friendlyByteBuf.writeResourceLocation(this.ambientSound);
        }
        friendlyByteBuf.writeBoolean(this.friendly);
        friendlyByteBuf.writeBoolean(this.neutral);
        friendlyByteBuf.writeBoolean(this.defensive);
        friendlyByteBuf.writeBoolean(this.tameable);
        friendlyByteBuf.writeBoolean(this.male);
        friendlyByteBuf.writeBoolean(this.food != null);
        if (this.food != null) {
            friendlyByteBuf.writeUtf(this.food);
        }
        friendlyByteBuf.writeCollection(this.biomes, FriendlyByteBuf::writeResourceLocation);
    }

    public static FieldGuideEntry read(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation id = friendlyByteBuf.readResourceLocation();
        ResourceLocation entityId = friendlyByteBuf.readResourceLocation();
        ResourceLocation ambientSound = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readResourceLocation() : null;
        boolean friendly = friendlyByteBuf.readBoolean();
        boolean neutral = friendlyByteBuf.readBoolean();
        boolean defensive = friendlyByteBuf.readBoolean();
        boolean tameable = friendlyByteBuf.readBoolean();
        boolean male = friendlyByteBuf.readBoolean();
        String food = friendlyByteBuf.readBoolean() ? friendlyByteBuf.readUtf() : null;
        List<ResourceLocation> biomes = friendlyByteBuf.readList(FriendlyByteBuf::readResourceLocation);

        return new FieldGuideEntry(id, entityId, ambientSound, friendly, neutral, defensive, tameable, male, food, List.copyOf(biomes));
    }

    public boolean hasFood() {
        return this.food != null && !this.food.isBlank();
    }

    public boolean foodIsTag() {
        return this.hasFood() && this.food.startsWith("#");
    }

    private static List<ResourceLocation> parseResourceLocationList(JsonArray jsonArray) {
        List<ResourceLocation> resourceLocations = new ArrayList<>();

        for (int index = 0; index < jsonArray.size(); index++) {
            resourceLocations.add(ResourceLocation.parse(jsonArray.get(index).getAsString()));
        }

        return resourceLocations;
    }
}