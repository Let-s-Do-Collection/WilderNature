package net.satisfy.wildernature.core.bounty;

import net.minecraft.nbt.CompoundTag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerBountyData {
    private BountyDefinition activeBounty;
    private int currentProgress;
    private int trackedStartAmount;
    private int trackedHighestAmount;
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
        this.trackedStartAmount = 0;
        this.trackedHighestAmount = 0;
    }

    public void clearActiveBounty() {
        this.activeBounty = null;
        this.currentProgress = 0;
        this.trackedStartAmount = 0;
        this.trackedHighestAmount = 0;
    }

    public void abandonActiveBounty() {
        if (this.activeBounty != null) {
            this.abandonedBounties.add(this.activeBounty.id());
        }
        this.clearActiveBounty();
    }

    public void incrementProgress(int amount) {
        if (this.activeBounty != null && amount > 0 && this.currentProgress < this.activeBounty.requiredAmount()) {
            this.currentProgress = Math.min(this.activeBounty.requiredAmount(), this.currentProgress + amount);
        }
    }

    public void setProgress(int progress) {
        if (this.activeBounty != null) {
            this.currentProgress = Math.max(0, Math.min(this.activeBounty.requiredAmount(), progress));
        }
    }

    public boolean isCompleted() {
        return this.activeBounty != null && this.currentProgress >= this.activeBounty.requiredAmount();
    }

    public CompoundTag save() {
        CompoundTag compoundTag = new CompoundTag();

        if (this.activeBounty != null) {
            compoundTag.put("active_bounty", this.activeBounty.save());
            compoundTag.putInt("current_progress", this.currentProgress);
            compoundTag.putInt("tracked_start_amount", this.trackedStartAmount);
            compoundTag.putInt("tracked_highest_amount", this.trackedHighestAmount);
        }

        int abandonedIndex = 0;
        for (UUID bountyId : this.abandonedBounties) {
            compoundTag.putUUID("abandoned_" + abandonedIndex, bountyId);
            abandonedIndex++;
        }

        compoundTag.putInt("abandoned_size", abandonedIndex);
        return compoundTag;
    }

    public static PlayerBountyData load(CompoundTag compoundTag) {
        PlayerBountyData playerBountyData = new PlayerBountyData();

        if (compoundTag.contains("active_bounty")) {
            playerBountyData.activeBounty = BountyDefinition.load(compoundTag.getCompound("active_bounty"));
            playerBountyData.currentProgress = compoundTag.getInt("current_progress");
            playerBountyData.trackedStartAmount = compoundTag.getInt("tracked_start_amount");
            playerBountyData.trackedHighestAmount = compoundTag.getInt("tracked_highest_amount");
        }

        int abandonedSize = compoundTag.getInt("abandoned_size");
        for (int index = 0; index < abandonedSize; index++) {
            playerBountyData.abandonedBounties.add(compoundTag.getUUID("abandoned_" + index));
        }

        return playerBountyData;
    }
}