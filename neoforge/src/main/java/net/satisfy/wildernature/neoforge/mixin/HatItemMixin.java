package net.satisfy.wildernature.neoforge.mixin;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.satisfy.wildernature.item.StylinPurpleHatItem;
import net.satisfy.wildernature.registry.ArmorRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

@Mixin(StylinPurpleHatItem.class)
public abstract class HatItemMixin extends ArmorItem
{
    @Shadow @Final
    private ResourceLocation hatTexture;

    public HatItemMixin(Holder<ArmorMaterial> arg, Type arg2, Properties arg3) {
        super(arg, arg2, arg3);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(
                new IClientItemExtensions() {
                    @Override
                    public @NotNull Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original)
                    {
                        return ArmorRegistry.getHatModel(itemStack.getItem(), original.getHead());
                    }
                }
        );
    }

    public @Nullable String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return hatTexture.toString();
    }
}