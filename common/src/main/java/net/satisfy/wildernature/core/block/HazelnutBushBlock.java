package net.satisfy.wildernature.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.wildernature.platform.PlatformHelper;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class HazelnutBushBlock extends SweetBerryBushBlock {

    public HazelnutBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(ObjectRegistry.HAZELNUT.get());
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int age = blockState.getValue(AGE);

        if (itemStack.is(Items.BONE_MEAL) && age < 3) {
            if (!level.isClientSide) {
                int grownAge = Math.min(3, age + 1 + level.random.nextInt(2));
                BlockState grownState = blockState.setValue(AGE, grownAge);
                level.setBlock(blockPos, grownState, 2);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(player, grownState));
                level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, blockPos, 15);

                if (!player.getAbilities().instabuild) {
                    itemStack.shrink(1);
                }
            }

            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        boolean fullyGrown = age == 3;
        if (age > 1) {
            if (!level.isClientSide) {
                int dropCount = 1 + level.random.nextInt(2);
                popResource(level, blockPos, new ItemStack(ObjectRegistry.HAZELNUT.get(), dropCount + (fullyGrown ? 1 : 0)));

                BlockState resetState = blockState.setValue(AGE, 1);
                level.setBlock(blockPos, resetState, 2);
                level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(player, resetState));

                if (PlatformHelper.isAnimationsEnabled() && level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState), blockPos.getX() + 0.5D, blockPos.getY() + 0.6D, blockPos.getZ() + 0.5D, 12, 0.3D, 0.25D, 0.3D, 0.05D);
                }
            }

            level.playSound(null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
    }

    @Override
    public void entityInside(BlockState blockState, Level level, BlockPos blockPos, Entity entity) {
    }
}
