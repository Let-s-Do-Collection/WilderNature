package net.satisfy.wildernature.core.bounty;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record BountyDefinition(UUID id, BountyCategory category, ResourceLocation entityId, int requiredKills, BountyReward reward) {

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("id", this.id);
        tag.putString("category", this.category.getName());
        tag.putString("entity_id", this.entityId.toString());
        tag.putInt("required_kills", this.requiredKills);
        tag.put("reward", this.reward.save());
        return tag;
    }

    public static BountyDefinition load(CompoundTag tag) {
        return new BountyDefinition(
                tag.getUUID("id"),
                BountyCategory.byName(tag.getString("category")),
                ResourceLocation.parse(tag.getString("entity_id")),
                tag.getInt("required_kills"),
                BountyReward.load(tag.getCompound("reward"))
        );
    }
}