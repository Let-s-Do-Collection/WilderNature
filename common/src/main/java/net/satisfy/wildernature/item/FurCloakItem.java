package net.satisfy.wildernature.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;

public class FurCloakItem extends Item implements Equipable {

    protected final ArmorItem.Type type;

    public FurCloakItem(ArmorMaterial pMaterial, ArmorItem.Type pType, Properties pProperties) {
        super(pProperties.durability(pMaterial.getDefense(pType)));
        this.type = pType;
    }

    public static boolean isEquippedBy(Player player) {
        for (ItemStack itemStack : player.getArmorSlots()) {
            if (itemStack.getItem() instanceof FurCloakItem) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NotNull EquipmentSlot getEquipmentSlot() {
        return this.type.getSlot();
    }
}
