package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.platform.PlatformHelper;
import net.satisfy.wildernature.core.block.entity.TermiteMoundBlockEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TermiteMoundBlock extends BaseEntityBlock {
    public TermiteMoundBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TermiteMoundBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TermiteMoundBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type, EntityTypeRegistry.TERMITE_MOUND_BLOCK_ENTITY.get(), (lvl, pos, blockState, be) -> TermiteMoundBlockEntity.tick((ServerLevel) lvl, pos, be));
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!PlatformHelper.isAnimationsEnabled()) {
            return;
        }
        if (random.nextInt(8) != 0) {
            return;
        }
        Direction direction = Direction.getRandom(random);
        if (direction == Direction.DOWN) {
            return;
        }
        BlockPos sidePos = pos.relative(direction);
        if (level.getBlockState(sidePos).isFaceSturdy(level, sidePos, direction.getOpposite())) {
            return;
        }
        double x = pos.getX() + 0.5D + direction.getStepX() * 0.52D + (direction.getStepX() == 0 ? random.nextDouble() - 0.5D : 0.0D);
        double y = pos.getY() + 0.5D + direction.getStepY() * 0.52D + (direction.getStepY() == 0 ? random.nextDouble() - 0.5D : 0.0D);
        double z = pos.getZ() + 0.5D + direction.getStepZ() * 0.52D + (direction.getStepZ() == 0 ? random.nextDouble() - 0.5D : 0.0D);
        level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, state), x, y, z, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}