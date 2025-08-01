package net.satisfy.wildernature.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.entity.BulletEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("unused")
public class AmmunitionItem extends Item {
    private final int damage;

    public AmmunitionItem(Properties properties, int damage) {
        super(properties);
        this.damage = damage;
    }

    public static void onLivingEntityHit(LivingEntity target, @Nullable Entity shooter, Level world) {
    }

    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        BulletEntity entity = new BulletEntity(world, shooter);
        entity.setItem(stack);
        entity.setDamage(damage);
        return entity;
    }

    public void consume(ItemStack stack, Player player) {
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.getInventory().removeItem(stack);
        }
    }

    @Override
    public void appendHoverText(ItemStack itemStack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag) {
        tooltip.add(Component.translatable("tooltip.wildernature.ammunition", damage).withStyle(ChatFormatting.DARK_GREEN));
    }

    public boolean hasAmmo(ItemStack stack) {
        return !stack.isEmpty();
    }

    public double modifyDamage(double damage, BulletEntity projectile, Entity target, @Nullable Entity shooter, Level world) {
        return damage;
    }
}
