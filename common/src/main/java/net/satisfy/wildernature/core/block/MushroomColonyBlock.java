package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class MushroomColonyBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<MushroomColonyBlock> CODEC = simpleCodec(MushroomColonyBlock::new);
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 4);
    public static final IntegerProperty COLONY_AGE = AGE;

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 3.0D, 12.0D),
            Block.box(3.5D, 0.0D, 3.5D, 12.5D, 4.0D, 12.5D),
            Block.box(3.0D, 0.0D, 3.0D, 13.0D, 5.0D, 13.0D),
            Block.box(2.5D, 0.0D, 2.5D, 13.5D, 6.0D, 13.5D),
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 7.0D, 14.0D)
    };

    public MushroomColonyBlock(BlockBehaviour.Properties properties) {
        super(properties.noCollission().instabreak().sound(SoundType.CROP).randomTicks());
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected @NotNull MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || this.isFarmAndCharmFertilizedSoil(state);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.mayPlaceOn(level.getBlockState(pos.below()), level, pos.below());
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(AGE)];
    }

    @Override
    protected @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);

        if (age < this.getMaxAge() && this.canGrowNaturally(level, pos) && random.nextFloat() < this.getGrowthChance(level, pos)) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            return;
        }

        if (age >= this.getMaxAge() && this.canSpread(level, pos) && random.nextFloat() < this.getSpreadChance(level, pos)) {
            this.trySpread(level, pos, random);
        }
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, BlockHitResult hitResult) {
        if (state.getValue(AGE) < this.getMaxAge()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide) {
            int brownCount = 1 + level.random.nextInt(2);
            int redCount = level.random.nextInt(3) == 0 ? 1 : 0;

            popResource(level, pos, new ItemStack(Items.BROWN_MUSHROOM, brownCount));
            if (redCount > 0) {
                popResource(level, pos, new ItemStack(Items.RED_MUSHROOM, redCount));
            }

            level.setBlock(pos, state.setValue(AGE, 2), 2);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < this.getMaxAge();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < this.getMaxAge();
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int currentAge = state.getValue(AGE);
        int ageIncrease = this.isFarmAndCharmFertilizedSoil(level.getBlockState(pos.below())) ? 2 : 1;
        int newAge = Math.min(this.getMaxAge(), currentAge + ageIncrease);
        level.setBlock(pos, state.setValue(AGE, newAge), 2);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!this.isFarmAndCharmFertilizedSoil(level.getBlockState(pos.below()))) {
            return;
        }

        if (random.nextInt(14) == 0) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + 0.2D + random.nextDouble() * 0.6D, pos.getY() + 0.15D + random.nextDouble() * 0.35D, pos.getZ() + 0.2D + random.nextDouble() * 0.6D, 0.0D, 0.01D, 0.0D);
        }
    }

    public int getMaxAge() {
        return 4;
    }

    private boolean canGrowNaturally(ServerLevel level, BlockPos pos) {
        return this.getLightLevel(level, pos) <= 7;
    }

    private float getGrowthChance(ServerLevel level, BlockPos pos) {
        int lightLevel = this.getLightLevel(level, pos);
        if (lightLevel > 7) {
            return 0.0F;
        }

        float growthChance = 0.05F;
        BlockState belowState = level.getBlockState(pos.below());

        if (lightLevel <= 5) {
            growthChance += 0.05F;
        } else {
            growthChance += 0.02F;
        }

        if (belowState.is(Blocks.PODZOL)) {
            growthChance += 0.04F;
        } else if (belowState.is(Blocks.MYCELIUM)) {
            growthChance += 0.05F;
        } else if (this.isFarmAndCharmFertilizedSoil(belowState)) {
            growthChance += 0.07F;
        }

        if (this.hasNearbyWater(level, pos)) {
            growthChance += 0.03F;
        }

        if (level.isRainingAt(pos.above())) {
            growthChance += 0.02F;
        }

        if (level.getBiome(pos).is(BiomeTags.IS_FOREST) || level.getBiome(pos).is(BiomeTags.IS_TAIGA) || level.getBiome(pos).is(Biomes.MUSHROOM_FIELDS)) {
            growthChance += 0.03F;
        }

        return growthChance;
    }

    private boolean canSpread(ServerLevel level, BlockPos pos) {
        return this.getLightLevel(level, pos) <= 5;
    }

    private float getSpreadChance(ServerLevel level, BlockPos pos) {
        float spreadChance = 0.015F;
        BlockState belowState = level.getBlockState(pos.below());

        if (belowState.is(Blocks.PODZOL)) {
            spreadChance += 0.01F;
        } else if (belowState.is(Blocks.MYCELIUM)) {
            spreadChance += 0.015F;
        } else if (this.isFarmAndCharmFertilizedSoil(belowState)) {
            spreadChance += 0.02F;
        }

        if (this.hasNearbyWater(level, pos)) {
            spreadChance += 0.01F;
        }

        if (level.isRainingAt(pos.above())) {
            spreadChance += 0.005F;
        }

        if (level.getBiome(pos).is(BiomeTags.IS_FOREST) || level.getBiome(pos).is(BiomeTags.IS_TAIGA) || level.getBiome(pos).is(Biomes.MUSHROOM_FIELDS)) {
            spreadChance += 0.01F;
        }

        return spreadChance;
    }

    private void trySpread(ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.countNearbyColonies(level, pos) >= 3) {
            return;
        }

        for (int attempt = 0; attempt < 4; attempt++) {
            BlockPos targetGroundPos = pos.offset(random.nextInt(5) - 2, 0, random.nextInt(5) - 2);
            BlockPos targetPos = targetGroundPos.above();

            if (targetPos.equals(pos)) {
                continue;
            }

            BlockState groundState = level.getBlockState(targetGroundPos);
            BlockState targetState = level.getBlockState(targetPos);

            if (!targetState.isAir()) {
                continue;
            }

            if (!groundState.is(Blocks.PODZOL) && !groundState.is(Blocks.MYCELIUM) && !this.isFarmAndCharmFertilizedSoil(groundState)) {
                continue;
            }

            if (this.getLightLevel(level, targetPos) > 5) {
                continue;
            }

            level.setBlock(targetPos, this.defaultBlockState().setValue(AGE, 0), 2);
            return;
        }
    }

    private int countNearbyColonies(ServerLevel level, BlockPos centerPos) {
        int colonyCount = 0;

        for (BlockPos nearbyPos : BlockPos.betweenClosed(centerPos.offset(-3, -1, -3), centerPos.offset(3, 1, 3))) {
            if (level.getBlockState(nearbyPos).is(this)) {
                colonyCount++;
            }
        }

        return colonyCount;
    }

    private boolean hasNearbyWater(LevelReader level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(nearbyPos).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    private int getLightLevel(LevelReader level, BlockPos pos) {
        return level.getMaxLocalRawBrightness(pos);
    }

    private boolean isFarmAndCharmFertilizedSoil(BlockState state) {
        if (!Platform.isModLoaded("farm_and_charm")) {
            return false;
        }

        ResourceLocation fertilizedSoilId = ResourceLocation.tryParse("farm_and_charm:fertilized_soil");
        if (fertilizedSoilId == null) {
            return false;
        }

        return BuiltInRegistries.BLOCK.getOptional(fertilizedSoilId).filter(block -> block != Blocks.AIR).map(state::is).orElse(false);
    }
}