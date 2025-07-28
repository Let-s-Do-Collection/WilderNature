package net.satisfy.wildernature.forge.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.satisfy.wildernature.util.Truffling;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {

    @Shadow
    public void eat(int foodLevel, float saturationModifier) {}

    @Inject(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;eat(IF)V"), cancellable = true)
    private void onEat(Item item, ItemStack pStack, LivingEntity entity, CallbackInfo ci) {

        if (!Truffling.isTruffled(pStack)) {
            return;
        }

        FoodProperties foodProperties = pStack.getFoodProperties(entity);

        Truffling.FoodValue additionalFoodValues = Truffling.getAdditionalFoodValue();

        if (foodProperties != null) {
            eat(foodProperties.getNutrition() + additionalFoodValues.nutrition(), foodProperties.getSaturationModifier() + additionalFoodValues.saturationModifier());
        }

        ci.cancel();
    }
}
