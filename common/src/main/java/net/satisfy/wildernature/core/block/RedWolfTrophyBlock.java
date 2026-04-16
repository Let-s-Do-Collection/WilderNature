package net.satisfy.wildernature.core.block;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.satisfy.wildernature.core.entity.fx.GlowingBlock;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.TagsRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class RedWolfTrophyBlock extends WallDecorationBlock {

    private static final int COOLDOWN_TICKS = 200;
    private static final double RANGE = 32.0;

    private static final int COLOR_CONTAINER  = 0xFFFFFF;
    private static final int COLOR_SPAWNER    = 0x9B30FF;
    private static final int COLOR_TRAPPED    = 0xFF2020;
    private static final int COLOR_CRAFTING   = 0x99CCFF;

    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.9375, 1, 0.8125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.28125, 0.5625, 0.75, 0.65625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.5625, 0.65625, 0.8125, 0.75, 0.90625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.25, 0.65625, 0.8125, 0.4375, 0.90625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.28125, 0.375, 0.625, 0.46875, 0.5625), BooleanOp.OR);
        return shape;
    };

    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, WilderNatureUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    public RedWolfTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) world;
        RedWolfTrophyData data = RedWolfTrophyData.get(serverLevel);
        long currentTime = world.getGameTime();
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;

        if (currentTime - data.getLastUsed(pos) < COOLDOWN_TICKS) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 8, 0.2, 0.2, 0.2, 0.01);
            world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 0.6f, 0.5f);
            return InteractionResult.FAIL;
        }

        data.setLastUsed(pos, currentTime);

        Vector3d center = new Vector3d(cx, cy, cz);
        AABB searchBox = new AABB(
                center.x - RANGE, center.y - RANGE, center.z - RANGE,
                center.x + RANGE, center.y + RANGE, center.z + RANGE
        );

        List<BlockPos> found = BlockPos.betweenClosedStream(searchBox)
                .map(BlockPos::immutable)
                .filter(p -> world.getBlockState(p).is(TagsRegistry.MAKES_BLOCK_GLOW))
                .toList();

        for (BlockPos blockPos : found) {
            BlockState blockState = world.getBlockState(blockPos);
            int color = resolveColor(blockState);

            GlowingBlock glowEntity = new GlowingBlock(EntityTypeRegistry.GLOWING_BLOCK.get(), world);
            glowEntity.setGlowColor(color);
            glowEntity.moveTo(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, 0, 0);
            world.addFreshEntity(glowEntity);

            String teamName = "glow_" + Integer.toHexString(color);
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam team = scoreboard.getPlayerTeam(teamName);
            if (team == null) {
                team = scoreboard.addPlayerTeam(teamName);
                team.setColor(resolveTextColor(color));
            }
            scoreboard.addPlayerToTeam(glowEntity.getStringUUID(), team);
        }

        serverLevel.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 20, 0.3, 0.3, 0.3, 0.05);
        world.playSound(null, pos, SoundEvents.AMBIENT_CAVE.value(), SoundSource.BLOCKS, 0.5f, 1.2f);
        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.4f, 0.9f);

        return InteractionResult.SUCCESS;
    }

    private static int resolveColor(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof SpawnerBlock) return COLOR_SPAWNER;
        if (block instanceof TrappedChestBlock) return COLOR_TRAPPED;
        if (block instanceof ChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock) return COLOR_CONTAINER;
        if (block instanceof CraftingTableBlock) return COLOR_CRAFTING;
        return COLOR_CONTAINER;
    }

    private static ChatFormatting resolveTextColor(int color) {
        if (color == COLOR_SPAWNER) return ChatFormatting.LIGHT_PURPLE;
        if (color == COLOR_TRAPPED) return ChatFormatting.RED;
        if (color == COLOR_CRAFTING) return ChatFormatting.AQUA;
        return ChatFormatting.WHITE;
    }

}