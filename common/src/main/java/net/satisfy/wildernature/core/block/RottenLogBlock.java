package net.satisfy.wildernature.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.core.block.entity.RottenLogBlockEntity;
import net.satisfy.wildernature.core.block.entity.TermiteMoundBlockEntity;
import net.satisfy.wildernature.core.entity.animal.passive.TermiteEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RottenLogBlock extends RotatedPillarBlock implements EntityBlock {
    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);
    public static final IntegerProperty MOISTURE = IntegerProperty.create("moisture", 0, 7);
    private static final int MAX_CONNECTED_INFESTED_LOGS = 128;

    public RottenLogBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.Y).setValue(STAGE, Stage.HOLLOW).setValue(MOISTURE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STAGE, MOISTURE);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        Stage stage = state.getValue(STAGE);
        Direction.Axis axis = state.getValue(AXIS);

        if (stage == Stage.INFESTED && held.getItem() instanceof ShovelItem) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                ReleaseData releaseData = releaseInfestedCluster(serverLevel, pos, false);
                level.playSound(null, pos, SoundEvents.GRAVEL_BREAK, SoundSource.BLOCKS, 1.0F, 1.0F);
                spawnCenteredBlockParticles(serverLevel, pos, state, 16, 0.28D);

                EquipmentSlot equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                held.hurtAndBreak(1, player, equipmentSlot);

                spawnTermites(serverLevel, pos, releaseData.termiteCount(), releaseData.moundPos());
                spawnWoodmeal(serverLevel, pos, player, 3 + serverLevel.getRandom().nextInt(5));

                if (releaseData.moundPos() != null && releaseData.infestationId() > 0) {
                    TermiteMoundBlockEntity.releaseInfestationId(serverLevel, releaseData.moundPos(), releaseData.infestationId());
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stage == Stage.HOLLOW && axis == Direction.Axis.Y && held.is(Items.DIRT)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                level.setBlock(pos, state.setValue(STAGE, Stage.SOIL).setValue(MOISTURE, 0), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                spawnCenteredBlockParticles(serverLevel, pos, Blocks.DIRT.defaultBlockState(), 14, 0.26D);
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        if (stage == Stage.SOIL && axis == Direction.Axis.Y && held.getItem() instanceof HoeItem) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                int moisture = hasNearbyWater(level, pos) || level.isRainingAt(pos.above()) ? 7 : 0;
                level.setBlock(pos, state.setValue(STAGE, Stage.FARMLAND).setValue(MOISTURE, moisture), Block.UPDATE_ALL);
                level.playSound(null, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                spawnTopBlockParticles(serverLevel, pos, Blocks.DIRT.defaultBlockState());
                EquipmentSlot equipmentSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                held.hurtAndBreak(1, player, equipmentSlot);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && state.is(ObjectRegistry.ROTTEN_LOG.get()) && state.getValue(STAGE) == Stage.INFESTED) {
            ReleaseData releaseData = releaseInfestedCluster(serverLevel, pos, true);
            spawnTermites(serverLevel, pos, releaseData.termiteCount(), releaseData.moundPos());
            if (releaseData.moundPos() != null && releaseData.infestationId() > 0) {
                TermiteMoundBlockEntity.releaseInfestationId(serverLevel, releaseData.moundPos(), releaseData.infestationId());
            }
        }
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(STAGE) == Stage.FARMLAND || state.getValue(STAGE) == Stage.INFESTED;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == Stage.FARMLAND) {
            int moisture = state.getValue(MOISTURE);
            boolean hasNearbyWater = hasNearbyWater(level, pos);
            boolean isRainingAbove = level.isRainingAt(pos.above());

            if (hasNearbyWater || isRainingAbove) {
                if (moisture < 7) {
                    level.setBlock(pos, state.setValue(MOISTURE, 7), Block.UPDATE_ALL);
                }
                return;
            }

            if (moisture > 0) {
                level.setBlock(pos, state.setValue(MOISTURE, moisture - 1), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(STAGE) != Stage.FARMLAND) {
            super.entityInside(state, level, pos, entity);
        }
    }

    @Override
    protected @NotNull VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape baseShape = switch (state.getValue(STAGE)) {
            case SOIL -> Shapes.block();
            case FARMLAND -> createFarmlandShape();
            case INFESTED -> createInfestedShape();
            case HOLLOW -> createHollowShape();
        };
        return rotateShape(baseShape, state.getValue(AXIS));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RottenLogBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == EntityTypeRegistry.ROTTEN_LOG_BLOCK_ENTITY.get() ? (currentLevel, currentPos, currentState, blockEntity) -> RottenLogBlockEntity.tick((ServerLevel) currentLevel, currentPos, currentState, (RottenLogBlockEntity) blockEntity) : null;
    }

    public static boolean isFarmland(BlockState state) {
        return state.is(ObjectRegistry.ROTTEN_LOG.get())
                && state.getValue(STAGE) == Stage.FARMLAND
                && state.getValue(AXIS) == Direction.Axis.Y;
    }

    public static BlockState createInfestedState(BlockState originalState) {
        Direction.Axis axis = originalState.hasProperty(AXIS) ? originalState.getValue(AXIS) : Direction.Axis.Y;
        return ObjectRegistry.ROTTEN_LOG.get().defaultBlockState().setValue(AXIS, axis).setValue(STAGE, Stage.INFESTED).setValue(MOISTURE, 0);
    }

    public static BlockState createHollowState(BlockState originalState) {
        Direction.Axis axis = originalState.hasProperty(AXIS) ? originalState.getValue(AXIS) : Direction.Axis.Y;
        return ObjectRegistry.ROTTEN_LOG.get().defaultBlockState().setValue(AXIS, axis).setValue(STAGE, Stage.HOLLOW).setValue(MOISTURE, 0);
    }

    private static ReleaseData releaseInfestedCluster(ServerLevel level, BlockPos originPos, boolean keepOriginBroken) {
        Set<BlockPos> visitedPositions = new HashSet<>();
        ArrayDeque<BlockPos> pendingPositions = new ArrayDeque<>();
        pendingPositions.add(originPos);

        int releasedTermiteCount = 0;
        int infestationId = 0;
        BlockPos moundPos = null;

        while (!pendingPositions.isEmpty() && visitedPositions.size() < MAX_CONNECTED_INFESTED_LOGS) {
            BlockPos currentPos = pendingPositions.removeFirst();
            if (!visitedPositions.add(currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            if (!currentState.is(ObjectRegistry.ROTTEN_LOG.get()) || currentState.getValue(STAGE) != Stage.INFESTED) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(currentPos);
            if (blockEntity instanceof RottenLogBlockEntity rottenLogBlockEntity) {
                releasedTermiteCount += rottenLogBlockEntity.getTermiteCount();
                if (infestationId == 0) {
                    infestationId = rottenLogBlockEntity.getInfestationId();
                }
                if (moundPos == null) {
                    moundPos = rottenLogBlockEntity.getMoundPos();
                }
            } else {
                releasedTermiteCount += 1;
            }

            if (!keepOriginBroken || !currentPos.equals(originPos)) {
                level.setBlock(currentPos, createHollowState(currentState), Block.UPDATE_ALL);
            }

            for (Direction direction : Direction.values()) {
                pendingPositions.addLast(currentPos.relative(direction));
            }
        }

        return new ReleaseData(releasedTermiteCount, infestationId, moundPos);
    }

    private static void spawnTermites(ServerLevel level, BlockPos pos, int termiteCount, @Nullable BlockPos moundPos) {
        List<BlockPos> spawnPositions = new ArrayList<>();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos candidatePos = pos.relative(direction);
            if (level.getBlockState(candidatePos).isAir() && level.getBlockState(candidatePos.above()).isAir()) {
                spawnPositions.add(candidatePos);
            }
        }

        if (spawnPositions.isEmpty()) {
            BlockPos abovePos = pos.above();
            if (level.getBlockState(abovePos).isAir() && level.getBlockState(abovePos.above()).isAir()) {
                spawnPositions.add(abovePos);
            } else {
                spawnPositions.add(pos);
            }
        }

        for (int termiteIndex = 0; termiteIndex < termiteCount; termiteIndex++) {
            TermiteEntity termite = EntityTypeRegistry.TERMITE.get().create(level);
            if (termite == null) {
                continue;
            }

            BlockPos spawnPos = spawnPositions.get(level.getRandom().nextInt(spawnPositions.size()));
            double offsetX = (level.getRandom().nextDouble() - 0.5D) * 0.4D;
            double offsetZ = (level.getRandom().nextDouble() - 0.5D) * 0.4D;

            termite.moveTo(spawnPos.getX() + 0.5D + offsetX, spawnPos.getY() + 0.1D, spawnPos.getZ() + 0.5D + offsetZ, level.getRandom().nextFloat() * 360.0F, 0.0F);
            termite.setMoundPos(moundPos);
            termite.setReturningToMound(moundPos != null);
            termite.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.TRIGGERED, null);
            level.addFreshEntity(termite);
        }
    }

    private static void spawnWoodmeal(ServerLevel level, BlockPos pos, Player player, int amount) {
        ItemStack itemStack = new ItemStack(ObjectRegistry.WOODMEAL.get(), amount);
        double spawnX = pos.getX() + 0.5D;
        double spawnY = pos.getY() + 0.35D;
        double spawnZ = pos.getZ() + 0.5D;

        ItemEntity itemEntity = new ItemEntity(level, spawnX, spawnY, spawnZ, itemStack);

        double motionX = player.getX() - spawnX;
        double motionY = player.getEyeY() - spawnY;
        double motionZ = player.getZ() - spawnZ;
        double motionLength = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

        if (motionLength > 0.0D) {
            double strength = 0.35D;
            itemEntity.setDeltaMovement(
                    motionX / motionLength * strength,
                    motionY / motionLength * strength + 0.08D,
                    motionZ / motionLength * strength
            );
        }

        itemEntity.setDefaultPickUpDelay();
        level.addFreshEntity(itemEntity);
    }

    private static boolean hasNearbyWater(Level level, BlockPos pos) {
        for (BlockPos nearbyPos : BlockPos.betweenClosed(pos.offset(-4, 0, -4), pos.offset(4, 1, 4))) {
            if (level.getFluidState(nearbyPos).is(FluidTags.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static void spawnCenteredBlockParticles(ServerLevel level, BlockPos pos, BlockState particleState, int count, double speed) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, particleState), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, count, 0.3D, 0.25D, 0.3D, speed);
    }

    private static void spawnTopBlockParticles(ServerLevel level, BlockPos pos, BlockState particleState) {
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, particleState), pos.getX() + 0.5D, pos.getY() + 1.02D, pos.getZ() + 0.5D, 12, 0.24D, 0.04D, 0.24D, 0.02D);
    }

    private static VoxelShape createHollowShape() {
        return joinBoxes(
                Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D),
                Shapes.box(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D),
                Shapes.box(0.875D, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D),
                Shapes.box(0.0D, 0.0D, 0.125D, 0.125D, 1.0D, 0.875D)
        );
    }

    private static VoxelShape createInfestedShape() {
        return joinBoxes(
                Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D),
                Shapes.box(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D),
                Shapes.box(0.875D, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D),
                Shapes.box(0.0D, 0.0D, 0.125D, 0.125D, 1.0D, 0.875D),
                Shapes.box(0.125D, 0.0625D, 0.125D, 0.875D, 0.9375D, 0.875D)
        );
    }

    private static VoxelShape createFarmlandShape() {
        return joinBoxes(
                Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D),
                Shapes.box(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D),
                Shapes.box(0.875D, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D),
                Shapes.box(0.0D, 0.0D, 0.125D, 0.125D, 1.0D, 0.875D),
                Shapes.box(0.125D, 0.0D, 0.125D, 0.875D, 0.9375D, 0.875D)
        );
    }

    private static VoxelShape joinBoxes(VoxelShape... shapes) {
        VoxelShape combinedShape = Shapes.empty();
        for (VoxelShape shape : shapes) {
            combinedShape = Shapes.join(combinedShape, shape, BooleanOp.OR);
        }
        return combinedShape;
    }

    private static VoxelShape rotateShape(VoxelShape shape, Direction.Axis axis) {
        if (axis == Direction.Axis.Y) {
            return shape;
        }

        VoxelShape rotatedShape = Shapes.empty();
        for (var boundingBox : shape.toAabbs()) {
            if (axis == Direction.Axis.X) {
                rotatedShape = Shapes.join(rotatedShape, Shapes.box(1.0D - boundingBox.maxY, boundingBox.minX, boundingBox.minZ, 1.0D - boundingBox.minY, boundingBox.maxX, boundingBox.maxZ), BooleanOp.OR);
            } else {
                rotatedShape = Shapes.join(rotatedShape, Shapes.box(boundingBox.minX, 1.0D - boundingBox.maxZ, boundingBox.minY, boundingBox.maxX, 1.0D - boundingBox.minZ, boundingBox.maxY), BooleanOp.OR);
            }
        }
        return rotatedShape;
    }

    private record ReleaseData(int termiteCount, int infestationId, @Nullable BlockPos moundPos) {
    }

    public enum Stage implements StringRepresentable {
        INFESTED("infested"),
        HOLLOW("hollow"),
        SOIL("soil"),
        FARMLAND("farmland");

        private final String name;

        Stage(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}