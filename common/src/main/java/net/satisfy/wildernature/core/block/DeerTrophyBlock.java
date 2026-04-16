package net.satisfy.wildernature.core.block;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.satisfy.wildernature.core.registry.MobEffectRegistry;
import net.satisfy.wildernature.core.util.WilderNatureUtil;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class DeerTrophyBlock extends WallDecorationBlock {
    private static final int COOLDOWN_TICKS = 200;
    private static final int EFFECT_DURATION = 600;
    private static final double RANGE = 32.0;

    private static final Supplier<VoxelShape> voxelShapeSupplier = () -> {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0.1875, 0.9375, 1, 0.8125, 1), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.3125, 0.5, 0.6875, 0.625, 0.9375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.375, 0.375, 0.3125, 0.625, 0.5625, 0.5), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.4375, 0.8125, 0.3125, 0.625, 0.875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.4375, 0.8125, 0.875, 0.625, 0.875), BooleanOp.OR);
        return shape;
    };

    public static final Map<Direction, VoxelShape> SHAPE = Util.make(new HashMap<>(), map -> {
        for (Direction direction : Direction.Plane.HORIZONTAL.stream().toList()) {
            map.put(direction, WilderNatureUtil.rotateShape(Direction.NORTH, direction, voxelShapeSupplier.get()));
        }
    });

    public DeerTrophyBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE.get(state.getValue(FACING));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) world;
        DeerTrophyData data = DeerTrophyData.get(serverLevel);
        long currentTime = world.getGameTime();
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;

        if (currentTime - data.getLastUsed(pos) < COOLDOWN_TICKS) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 8, 0.2, 0.2, 0.2, 0.01);
            world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 0.6f, 0.5f);
            return InteractionResult.FAIL;
        }

        int charges = data.getCharges(pos);
        if (charges <= 0) {
            serverLevel.sendParticles(ParticleTypes.ASH, cx, cy, cz, 12, 0.3, 0.3, 0.3, 0.02);
            world.playSound(null, pos, SoundEvents.NOTE_BLOCK_DIDGERIDOO.value(), SoundSource.BLOCKS, 0.6f, 0.3f);
            return InteractionResult.FAIL;
        }

        data.setLastUsed(pos, currentTime);
        data.setCharges(pos, charges - 1);

        Vector3d center = new Vector3d(cx, cy, cz);
        List<Animal> animals = world.getEntitiesOfClass(Animal.class, new AABB(
                center.x - RANGE, center.y - RANGE, center.z - RANGE,
                center.x + RANGE, center.y + RANGE, center.z + RANGE
        ));

        for (Animal animal : animals) {
            animal.addEffect(new MobEffectInstance(MobEffectRegistry.markedPreyHolder(), EFFECT_DURATION, 0));
        }

        player.addEffect(new MobEffectInstance(MobEffectRegistry.huntersSenseHolder(), EFFECT_DURATION, 0));

        serverLevel.sendParticles(ParticleTypes.ENCHANT, cx, cy, cz, 30, 0.4, 0.4, 0.4, 0.05);
        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, cx, cy + 0.5, cz, charges * 3, 0.3, 0.2, 0.3, 0.01);
        world.playSound(null, pos, SoundEvents.AMBIENT_CAVE.value(), SoundSource.BLOCKS, 0.6f, 0.75f);
        world.playSound(null, pos, SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.BLOCKS, 0.4f, 1.2f);

        return InteractionResult.SUCCESS;
    }
}