package net.satisfy.wildernature.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class StylinPurpleHatItem extends ArmorItem {
    private final ResourceLocation hatTexture;

    public StylinPurpleHatItem(ArmorMaterial armorMaterial, Type type, Properties properties, ResourceLocation hatTexture) {
        super(BuiltInRegistries.ARMOR_MATERIAL.wrapAsHolder(armorMaterial), type, properties);
        this.hatTexture = hatTexture;
    }


    public ResourceLocation getHatTexture() {
        return hatTexture;
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }
}
