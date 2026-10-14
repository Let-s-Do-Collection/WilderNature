package net.satisfy.wildernature.core.world.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.satisfy.wildernature.core.registry.WorldgenRegistry;
import net.satisfy.wildernature.platform.PlatformHelper;
import org.jetbrains.annotations.NotNull;

public class ConfigRarityFilter extends PlacementFilter {
    public static final MapCodec<ConfigRarityFilter> CODEC = Option.CODEC.fieldOf("option").xmap(ConfigRarityFilter::new, filter -> filter.option);

    private final Option option;

    public ConfigRarityFilter(Option option) {
        this.option = option;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        int rarity = switch (this.option) {
            case TERMITE_MOUND -> PlatformHelper.getTermiteMoundRarity();
            case HOLLOW_CACHE -> PlatformHelper.getHollowCacheRarity();
        };
        return random.nextFloat() < 1.0F / Math.max(1, rarity);
    }

    @Override
    public @NotNull PlacementModifierType<?> type() {
        return WorldgenRegistry.CONFIG_RARITY_FILTER.get();
    }

    public enum Option implements StringRepresentable {
        TERMITE_MOUND("termite_mound"),
        HOLLOW_CACHE("hollow_cache");

        public static final Codec<Option> CODEC = StringRepresentable.fromEnum(Option::values);

        private final String name;

        Option(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
