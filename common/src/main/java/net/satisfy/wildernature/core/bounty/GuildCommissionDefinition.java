package net.satisfy.wildernature.core.bounty;

import com.google.gson.JsonObject;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public record GuildCommissionDefinition(UUID id, String titleKey, String descriptionKey, BountyDefinition.BountyType type, ResourceLocation target, int amount, boolean allowPeaceful, ResourceLocation icon, ResourceLocation rewardItem, int rewardCount, int rewardXp, int weight, boolean repeatable) {
    public static GuildCommissionDefinition fromJson(JsonObject jsonObject) {
        String idString = jsonObject.get("id").getAsString();
        String title = jsonObject.get("title").getAsString();
        String description = jsonObject.get("description").getAsString();
        BountyDefinition.BountyType type = BountyDefinition.BountyType.valueOf(jsonObject.get("type").getAsString().toUpperCase());
        ResourceLocation target = ResourceLocation.parse(jsonObject.get("target").getAsString());

        int amount = jsonObject.get("amount").getAsInt();

        boolean allowPeaceful = jsonObject.has("allow_peaceful") && jsonObject.get("allow_peaceful").getAsBoolean();

        ResourceLocation icon = jsonObject.get("icon").isJsonObject() ? ResourceLocation.parse(jsonObject.getAsJsonObject("icon").get("value").getAsString()) : ResourceLocation.parse(jsonObject.get("icon").getAsString());
        JsonObject rewardObject = jsonObject.getAsJsonObject("reward");
        ResourceLocation rewardItem = ResourceLocation.parse(rewardObject.get("item").getAsString());
        int rewardCount = rewardObject.get("count").getAsInt();
        int rewardXp = rewardObject.has("xp") ? rewardObject.get("xp").getAsInt() : rewardObject.has("experience") ? rewardObject.get("experience").getAsInt() : 0;
        int weight = jsonObject.has("weight") ? jsonObject.get("weight").getAsInt() : 1;

        boolean repeatable = !jsonObject.has("repeatable") || jsonObject.get("repeatable").getAsBoolean();
        UUID id = UUID.nameUUIDFromBytes(("guild_commission:" + idString).getBytes(java.nio.charset.StandardCharsets.UTF_8));

        return new GuildCommissionDefinition(id, title, description, type, target, amount, allowPeaceful, icon, rewardItem, rewardCount, rewardXp, weight, repeatable);
    }
}