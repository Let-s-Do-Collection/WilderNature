package net.satisfy.wildernature.core.block;

import com.mojang.serialization.MapCodec;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.satisfy.wildernature.core.block.entity.BountyBoardBlockEntity;
import net.satisfy.wildernature.core.bounty.BountyBoardSavedData;
import net.satisfy.wildernature.core.bounty.BountyDefinition;
import net.satisfy.wildernature.core.bounty.BountyManager;
import net.satisfy.wildernature.core.bounty.PlayerBountyData;
import net.satisfy.wildernature.core.gui.handler.BountyBoardMenu;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BountyBoardBlock extends BaseEntityBlock {
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE_BOTTOM_LEFT = makeBottomLeftShape();
    private static final VoxelShape SHAPE_BOTTOM_RIGHT = makeBottomRightShape();
    private static final VoxelShape SHAPE_TOP_LEFT = makeTopLeftShape();
    private static final VoxelShape SHAPE_TOP_RIGHT = makeTopRightShape();
    public static final MapCodec<BountyBoardBlock> CODEC = simpleCodec(BountyBoardBlock::new);
    public static final Map<Direction, Map<Part, VoxelShape>> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            Map<Part, VoxelShape> partShapeMap = new HashMap<>();
            partShapeMap.put(Part.BOTTOM_LEFT, WilderNatureUtil.rotateShape(Direction.NORTH, direction, SHAPE_BOTTOM_LEFT));
            partShapeMap.put(Part.BOTTOM_RIGHT, WilderNatureUtil.rotateShape(Direction.NORTH, direction, SHAPE_BOTTOM_RIGHT));
            partShapeMap.put(Part.TOP_LEFT, WilderNatureUtil.rotateShape(Direction.NORTH, direction, SHAPE_TOP_LEFT));
            partShapeMap.put(Part.TOP_RIGHT, WilderNatureUtil.rotateShape(Direction.NORTH, direction, SHAPE_TOP_RIGHT));
            map.put(direction, partShapeMap);
        }
    });

    public BountyBoardBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PART, Part.BOTTOM_LEFT).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    private static VoxelShape makeBottomLeftShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.4375, 0.125, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.5, 0.4375, 1, 1, 0.5625), BooleanOp.OR);
        return shape;
    }

    private static VoxelShape makeBottomRightShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0.875, 0, 0.4375, 1, 1, 0.5625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 0.5, 0.4375, 0.875, 1, 0.5625), BooleanOp.OR);
        return shape;
    }

    private static VoxelShape makeTopLeftShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.4375, 1, 0.875, 0.5625), BooleanOp.OR);
        return shape;
    }

    private static VoxelShape makeTopRightShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.4375, 1, 0.875, 0.5625), BooleanOp.OR);
        return shape;
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level world = context.getLevel();
        Direction direction = context.getHorizontalDirection().getOpposite();

        if (!canPlaceAt(world, pos, direction)) {
            return null;
        }

        return this.defaultBlockState().setValue(PART, Part.BOTTOM_LEFT).setValue(FACING, direction);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction direction = state.getValue(FACING);

        world.setBlock(pos.above(), this.defaultBlockState().setValue(PART, Part.TOP_LEFT).setValue(FACING, direction), 3);
        world.setBlock(pos.relative(direction.getClockWise()), this.defaultBlockState().setValue(PART, Part.BOTTOM_RIGHT).setValue(FACING, direction), 3);
        world.setBlock(pos.relative(direction.getClockWise()).above(), this.defaultBlockState().setValue(PART, Part.TOP_RIGHT).setValue(FACING, direction), 3);

        world.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        world.playSound(null, pos, SoundEvents.CHERRY_WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);

        if (placer instanceof Player player && !player.isCreative()) {
            stack.shrink(1);
        }
    }

    private boolean canPlaceAt(Level world, BlockPos pos, Direction direction) {
        return world.getBlockState(pos).canBeReplaced()
                && world.getBlockState(pos.above()).canBeReplaced()
                && world.getBlockState(pos.relative(direction.getClockWise())).canBeReplaced()
                && world.getBlockState(pos.relative(direction.getClockWise()).above()).canBeReplaced();
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        state.getBlock();
        newState.getBlock();
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private BlockPos getBasePos(BlockState state, BlockPos pos) {
        Part part = state.getValue(PART);
        Direction direction = state.getValue(FACING);
        return switch (part) {
            case BOTTOM_LEFT -> pos;
            case TOP_LEFT -> pos.below();
            case BOTTOM_RIGHT -> pos.relative(direction.getCounterClockWise(), 1);
            case TOP_RIGHT -> pos.relative(direction.getCounterClockWise(), 1).below();
        };
    }

    private void destroyAdjacentBlocks(Level world, BlockPos basePos) {
        BlockState blockState = world.getBlockState(basePos);
        Direction facing = blockState.getValue(FACING);

        world.removeBlock(basePos, false);
        world.removeBlock(basePos.above(), false);
        world.removeBlock(basePos.relative(facing.getClockWise()), false);
        world.removeBlock(basePos.relative(facing.getClockWise()).above(), false);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        Part part = state.getValue(PART);
        return SHAPE.get(direction).get(part);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos basePos = getBasePos(level.getBlockState(pos), pos);
            ItemStack stack = new ItemStack(ObjectRegistry.BOUNTY_BOARD.get());
            level.addFreshEntity(new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), stack));
            destroyAdjacentBlocks(level, basePos);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.CONSUME;
        }

        BlockPos basePos = getBasePos(state, pos);
        BlockEntity blockEntity = world.getBlockEntity(basePos);

        if (!(blockEntity instanceof MenuProvider menuProvider)) {
            return InteractionResult.PASS;
        }

        MenuRegistry.openExtendedMenu(serverPlayer, menuProvider, buffer -> {
            BountyBoardSavedData savedData = BountyBoardSavedData.get(serverPlayer.serverLevel());
            PlayerBountyData playerBountyData = savedData.getPlayerBountyData(serverPlayer.getUUID());
            List<BountyDefinition> dailyBounties = savedData.getDailyBounties();

            BountyBoardMenu.writeBounties(buffer, dailyBounties, new ArrayList<>(playerBountyData.getAbandonedBounties()));

            int activeBountyIndex = -1;
            if (playerBountyData.hasActiveBounty() && playerBountyData.getActiveBounty() != null) {
                UUID activeBountyId = playerBountyData.getActiveBounty().id();
                for (int index = 0; index < dailyBounties.size(); index++) {
                    if (dailyBounties.get(index).id().equals(activeBountyId)) {
                        activeBountyIndex = index;
                        break;
                    }
                }
            }

            int selectedBountyIndex = activeBountyIndex;
            if (selectedBountyIndex < 0) {
                for (int index = 0; index < dailyBounties.size(); index++) {
                    if (!playerBountyData.getAbandonedBounties().contains(dailyBounties.get(index).id())) {
                        selectedBountyIndex = index;
                        break;
                    }
                }
            }

            boolean hasActiveBounty = playerBountyData.hasActiveBounty() && playerBountyData.getActiveBounty() != null;
            boolean activeCompleted = playerBountyData.isCompleted();
            boolean restoreContractAvailable = hasActiveBounty && !activeCompleted && !serverPlayer.getInventory().contains(BountyManager.createContractStack(playerBountyData.getActiveBounty()));

            buffer.writeVarInt(selectedBountyIndex);
            buffer.writeVarInt(hasActiveBounty ? 1 : 0);
            buffer.writeVarInt(activeBountyIndex);
            buffer.writeVarInt(playerBountyData.getCurrentProgress());
            buffer.writeVarInt(hasActiveBounty ? playerBountyData.getActiveBounty().requiredAmount() : 0);
            buffer.writeVarInt(activeCompleted ? 1 : 0);
            buffer.writeVarInt(restoreContractAvailable ? 1 : 0);
            buffer.writeVarInt(0);
        });

        return InteractionResult.CONSUME;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (!blockPos.equals(getBasePos(blockState, blockPos))) {
            return null;
        }

        return new BountyBoardBlockEntity(blockPos, blockState);
    }

    public enum Part implements StringRepresentable {
        BOTTOM_LEFT("bottom_left"),
        BOTTOM_RIGHT("bottom_right"),
        TOP_LEFT("top_left"),
        TOP_RIGHT("top_right");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(Component.translatable("tooltip.wildernature.canbeplaced").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
    }
}