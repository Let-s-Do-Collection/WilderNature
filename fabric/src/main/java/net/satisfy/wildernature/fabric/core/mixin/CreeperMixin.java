package net.satisfy.wildernature.fabric.core.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.satisfy.wildernature.core.mixin.MobAccessor;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import net.satisfy.wildernature.fabric.api.FurCloakTrinket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Unique
    private boolean wilderNature$addedGoal = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onEntityTick(CallbackInfo ci) {
        Creeper creeper = (Creeper) (Object) this;
        if (!wilderNature$addedGoal) {
            ((MobAccessor) creeper).getGoalSelector().addGoal(3, new AvoidEntityGoal<>(creeper, Player.class, 6.0F, 1.0, 1.2, target ->
                    target != null && (FurCloakTrinket.isEquippedBy((Player) target) || TrinketsApi.getTrinketComponent((Player) target).map(component -> component.isEquipped(ObjectRegistry.FUR_CLOAK.get())).orElse(false))
                    && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target)));
            wilderNature$addedGoal = true;
        }
    }
}