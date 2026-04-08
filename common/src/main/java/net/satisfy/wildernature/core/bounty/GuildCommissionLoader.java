package net.satisfy.wildernature.core.bounty;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public final class GuildCommissionLoader {
    

    private GuildCommissionLoader() {
    }

    public static List<GuildCommissionDefinition> load(ResourceManager resourceManager) {
        List<GuildCommissionDefinition> commissions = new ArrayList<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("guild_commissions", path -> path.getPath().endsWith(".json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            Resource resource = entry.getValue();

            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.open()))) {
                JsonElement jsonElement = JsonParser.parseReader(bufferedReader);
                if (!jsonElement.isJsonObject()) {
                    continue;
                }

                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (!jsonObject.has("enabled") || !jsonObject.get("enabled").getAsBoolean()) {
                    continue;
                }

                commissions.add(GuildCommissionDefinition.fromJson(jsonObject));
            } catch (Exception ignored) {
            }
        }

        return commissions;
    }
}