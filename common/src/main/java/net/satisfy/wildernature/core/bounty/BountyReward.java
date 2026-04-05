package net.satisfy.wildernature.core.bounty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public record BountyReward(ResourceLocation lootTableId, int experienceReward, ResourceLocation previewItemId, int previewCount) {
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("LootTable", this.lootTableId.toString());
        tag.putInt("Experience", this.experienceReward);
        tag.putString("PreviewItem", this.previewItemId.toString());
        tag.putInt("PreviewCount", this.previewCount);
        return tag;
    }

    public static BountyReward load(CompoundTag tag) {
        ResourceLocation lootTableId = ResourceLocation.parse(tag.getString("LootTable"));
        int experienceReward = tag.getInt("Experience");
        ResourceLocation previewItemId = tag.contains("PreviewItem") ? ResourceLocation.parse(tag.getString("PreviewItem")) : ResourceLocation.withDefaultNamespace("paper");
        int previewCount = tag.contains("PreviewCount") ? tag.getInt("PreviewCount") : 1;
        return new BountyReward(lootTableId, experienceReward, previewItemId, previewCount);
    }
}