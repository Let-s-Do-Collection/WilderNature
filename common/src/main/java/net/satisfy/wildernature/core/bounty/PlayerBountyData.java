package net.satisfy.wildernature.core.bounty;

import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerBountyData {
    private BountyDefinition activeBounty;
    private int currentProgress;
    private final Set<UUID> abandonedBounties = new HashSet<>();

    public BountyDefinition getActiveBounty() {
        return this.activeBounty;
    }

    public int getCurrentProgress() {
        return this.currentProgress;
    }

    public boolean hasActiveBounty() {
        return this.activeBounty != null;
    }

    public boolean hasAbandoned(UUID bountyId) {
        return this.abandonedBounties.contains(bountyId);
    }

    public Set<UUID> getAbandonedBounties() {
        return this.abandonedBounties;
    }

    public void setActiveBounty(BountyDefinition activeBounty) {
        this.activeBounty = activeBounty;
        this.currentProgress = 0;
    }

    public void clearActiveBounty() {
        this.activeBounty = null;
        this.currentProgress = 0;
    }

    public void abandonActiveBounty() {
        if (this.activeBounty != null) {
            this.abandonedBounties.add(this.activeBounty.id());
        }
        this.clearActiveBounty();
    }

    public void incrementProgress() {
        if (this.activeBounty != null && this.currentProgress < this.activeBounty.requiredKills()) {
            this.currentProgress++;
        }
    }

    public boolean isCompleted() {
        return this.activeBounty != null && this.currentProgress >= this.activeBounty.requiredKills();
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (this.activeBounty != null) {
            tag.put("active_bounty", this.activeBounty.save());
            tag.putInt("current_progress", this.currentProgress);
        }

        int abandonedIndex = 0;
        for (UUID bountyId : this.abandonedBounties) {
            tag.putUUID("abandoned_" + abandonedIndex, bountyId);
            abandonedIndex++;
        }
        tag.putInt("abandoned_size", abandonedIndex);
        return tag;
    }

    public static PlayerBountyData load(CompoundTag tag) {
        PlayerBountyData data = new PlayerBountyData();

        if (tag.contains("active_bounty")) {
            data.activeBounty = BountyDefinition.load(tag.getCompound("active_bounty"));
            data.currentProgress = tag.getInt("current_progress");
        }

        int abandonedSize = tag.getInt("abandoned_size");
        for (int index = 0; index < abandonedSize; index++) {
            data.abandonedBounties.add(tag.getUUID("abandoned_" + index));
        }

        return data;
    }
}