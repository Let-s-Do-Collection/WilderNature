package net.satisfy.wildernature.fabric.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.RottenLogBlock;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CropBlock.class)
public abstract class CropBlockMixin {
    @Inject(method = "mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void wildernature$mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (state.is(ObjectRegistry.ROTTEN_LOG.get()) && state.getValue(RottenLogBlock.STAGE) == RottenLogBlock.Stage.FARMLAND && state.getValue(RottenLogBlock.AXIS) == Direction.Axis.Y) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getGrowthSpeed", at = @At("RETURN"), cancellable = true)
    private static void wildernature$getGrowthSpeed(Block block, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState below = level.getBlockState(pos.below());
        if (RottenLogBlock.isFarmland(below)) {
            float bonus = below.getValue(RottenLogBlock.MOISTURE) > 0 ? 3.0F : 1.0F;
            cir.setReturnValue(cir.getReturnValueF() + bonus);
        }
    }
}
