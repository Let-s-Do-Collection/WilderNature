package net.satisfy.wildernature.core.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.satisfy.foundation.armor.Wearing;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.NotNull;

public class FurCloakItem extends Item implements Equipable {

    protected final ArmorItem.Type type;

    public FurCloakItem(ArmorMaterial pMaterial, ArmorItem.Type pType, Properties pProperties) {
        super(pProperties.durability(pMaterial.getDefense(pType)));
        this.type = pType;
    }

    public static boolean isVisibleOn(LivingEntity entity) {
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof FurCloakItem) {
            return true;
        }
        return chest.isEmpty() && Wearing.isWearing(entity, ObjectRegistry.FUR_CLOAK.get());
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }
}
