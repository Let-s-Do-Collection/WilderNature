package net.satisfy.wildernature.core.world.tree.decorator;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.registry.WorldgenRegistry;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TermiteMoundDecorator extends TreeDecorator {
    public static final TermiteMoundDecorator INSTANCE = new TermiteMoundDecorator();
    public static final MapCodec<TermiteMoundDecorator> CODEC = MapCodec.unit(INSTANCE);

    @Override
    protected TreeDecoratorType<?> type() {
        return WorldgenRegistry.TERMITE_MOUND_DECORATOR.get();
    }

    @Override
    public void place(Context context) {
        List<BlockPos> candidates = new ArrayList<>(context.logs());
        if (candidates.isEmpty()) {
            return;
        }

        int lowestY = candidates.stream().mapToInt(BlockPos::getY).min().orElse(candidates.get(0).getY());
        List<BlockPos> lowestCandidates = candidates.stream()
                .filter(pos -> pos.getY() == lowestY)
                .sorted(Comparator.comparingInt((BlockPos pos) -> pos.getX()).thenComparingInt(pos -> pos.getZ()))
                .toList();

        RandomSource randomSource = context.random();
        BlockPos targetPos = lowestCandidates.get(randomSource.nextInt(lowestCandidates.size()));
        context.setBlock(targetPos, ObjectRegistry.TERMITE_MOUND_STORAGE.get().defaultBlockState());
    }
}