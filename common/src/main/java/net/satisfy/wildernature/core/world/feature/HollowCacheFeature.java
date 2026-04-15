package net.satisfy.wildernature.core.world.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.storage.loot.LootTable;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.block.entity.HollowCacheBlockEntity;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public class HollowCacheFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceKey<LootTable> HOLLOW_CACHE_LOOT_TABLE = ResourceKey.create(Registries.LOOT_TABLE, WilderNature.identifier("chests/hollow_cache"));

    public HollowCacheFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource randomSource = context.random();
        BlockPos blockPos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.origin());

        if (!level.isEmptyBlock(blockPos)) return false;

        BlockState groundState = level.getBlockState(blockPos.below());
        if (!(groundState.is(Blocks.DIRT) || groundState.is(Blocks.COARSE_DIRT) || groundState.is(Blocks.GRASS_BLOCK))) return false;

        BlockState blockState = ObjectRegistry.HOLLOW_CACHE.get().defaultBlockState().setValue(HollowCacheBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(randomSource)).setValue(HollowCacheBlock.OPEN, false);
        if (!level.setBlock(blockPos, blockState, 3)) return false;

        if (level.getBlockEntity(blockPos) instanceof HollowCacheBlockEntity hollowCacheBlockEntity) {
            hollowCacheBlockEntity.setLootTable(HOLLOW_CACHE_LOOT_TABLE, randomSource.nextLong());
            hollowCacheBlockEntity.setChanged();
        }

        return true;
    }
}