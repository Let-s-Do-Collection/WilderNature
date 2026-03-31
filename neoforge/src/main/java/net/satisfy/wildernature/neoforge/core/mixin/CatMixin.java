package net.satisfy.wildernature.neoforge.core.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.entity.animal.DogEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Cat.class)
public abstract class CatMixin extends Mob {
    protected CatMixin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "registerGoals", at = @At("HEAD"))
    void registerRunAway(CallbackInfo ci) {
        this.goalSelector.addGoal(0, new AvoidEntityGoal<>((Cat) (Object) this, DogEntity.class, 16, 1.2, 1.2));
    }

}
