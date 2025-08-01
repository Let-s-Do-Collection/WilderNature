package net.satisfy.wildernature.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.phys.BlockHitResult;
import net.satisfy.wildernature.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class HazelnutBushBlock extends SweetBerryBushBlock {

    public HazelnutBushBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
        return new ItemStack(ObjectRegistry.HAZELNUT.get());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        int i = blockState.getValue(AGE);
        boolean flag = i == 3;
        if (!flag && player.getItemInHand(interactionHand).is(Items.BONE_MEAL)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else if (i > 1) {
            int j = 1 + level.random.nextInt(2);
            if (!player.getAbilities().instabuild) {// @author wdog5 - check only drop when player is survival mode
                popResource(level, blockPos, new ItemStack(ObjectRegistry.HAZELNUT.get(), j + (flag ? 1 : 0)));
            }
            level.playSound(null, blockPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
            BlockState blockstate = blockState.setValue(AGE, 1);
            level.setBlock(blockPos, blockstate, 2);
            level.gameEvent(GameEvent.BLOCK_CHANGE, blockPos, Context.of(player, blockstate));
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        } else {
            return super.useItemOn(itemStack, blockState, level, blockPos, player, interactionHand, blockHitResult);
        }
    }

    @Override
    public void entityInside(BlockState arg, Level arg2, BlockPos arg3, Entity arg4) {
    }
}
