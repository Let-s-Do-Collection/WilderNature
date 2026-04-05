package net.satisfy.wildernature.core.bounty;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.registry.TagsRegistry;

public enum BountyCategory {
    NEUTRAL("neutral", 12, 30, TagsRegistry.NEUTRAL),
    DEFENSIVE("defensive", 8, 18, TagsRegistry.DEFENSIVE),
    AGGRESSIVE("aggressive", 6, 14, TagsRegistry.AGGRESSIVE),
    BOSS("boss", 1, 1, TagsRegistry.BOSS);

    private final String name;
    private final int minimumKills;
    private final int maximumKills;
    private final TagKey<EntityType<?>> entityTag;

    BountyCategory(String name, int minimumKills, int maximumKills, TagKey<EntityType<?>> entityTag) {
        this.name = name;
        this.minimumKills = minimumKills;
        this.maximumKills = maximumKills;
        this.entityTag = entityTag;
    }

    public String getName() {
        return this.name;
    }

    public int getMinimumKills() {
        return this.minimumKills;
    }

    public int getMaximumKills() {
        return this.maximumKills;
    }

    public TagKey<EntityType<?>> getEntityTag() {
        return this.entityTag;
    }

    public ResourceLocation getLootTableId() {
        return WilderNature.identifier("gameplay/bounty_rewards/" + this.name);
    }

    public static BountyCategory byName(String name) {
        for (BountyCategory category : values()) {
            if (category.name.equals(name)) {
                return category;
            }
        }
        return NEUTRAL;
    }
}