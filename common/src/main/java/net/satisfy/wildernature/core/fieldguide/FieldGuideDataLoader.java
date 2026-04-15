package net.satisfy.wildernature.core.fieldguide;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FieldGuideDataLoader extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new GsonBuilder().create();
    public static final FieldGuideDataLoader INSTANCE = new FieldGuideDataLoader();
    private List<FieldGuideEntry> entries = List.of();

    private FieldGuideDataLoader() {
        super(GSON, "field_guide");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        List<FieldGuideEntry> loadedEntries = new ArrayList<>();

        for (Map.Entry<ResourceLocation, JsonElement> entry : object.entrySet()) {
            ResourceLocation entryId = entry.getKey();
            JsonObject jsonObject = GsonHelper.convertToJsonObject(entry.getValue(), "field_guide");

            if (!jsonObject.has("entity")) {
                continue;
            }

            if (!GsonHelper.getAsBoolean(jsonObject, "enabled", true)) {
                continue;
            }

            loadedEntries.add(FieldGuideEntry.fromJson(entryId, jsonObject));
        }

        loadedEntries.sort(Comparator.comparing(fieldGuideEntry -> fieldGuideEntry.entityId().toString(), String.CASE_INSENSITIVE_ORDER));
        this.entries = List.copyOf(loadedEntries);
    }

    public List<FieldGuideEntry> getEntries() {
        return this.entries;
    }
}