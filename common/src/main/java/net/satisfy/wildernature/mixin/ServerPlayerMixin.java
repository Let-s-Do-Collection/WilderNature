package net.satisfy.wildernature.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.util.contract.ContractInProgress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    void add(CompoundTag compoundTag, CallbackInfo ci) {
        var wilderNature$contract = ContractInProgress.progressPerPlayer.get(this.uuid);
        if (wilderNature$contract != null)
            compoundTag.put(
                    "wn_contract",
                    ContractInProgress.SERVER_CODEC
                            .encode(
                                    wilderNature$contract,
                                    NbtOps.INSTANCE,
                                    new CompoundTag()
                            )
                            .getOrThrow(
                                    (err) -> {
                                        throw new RuntimeException(err);
                                    }
                            )
            );
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    @SuppressWarnings("all")
    void read(CompoundTag compoundTag, CallbackInfo ci) {
        try {
            if (compoundTag.contains("wn_contract")) {
                var tag = compoundTag.get(
                        "wn_contract"
                );

                var contract = ContractInProgress.SERVER_CODEC
                        .parse(NbtOps.INSTANCE, tag)
                        .getOrThrow(
                                (err) -> {
                                    throw new RuntimeException(err);
                                }
                        );
                ContractInProgress.progressPerPlayer.put(this.uuid, contract);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
