package net.satisfy.wildernature.core.item;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.core.entity.ThrownTurkeyEgg;
import org.jetbrains.annotations.NotNull;

public class TurkeyEggItem extends Item implements ProjectileItem {
    public TurkeyEggItem(Item.Properties properties) {
        super(properties);
    }

    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemStack = player.getItemInHand(usedHand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownTurkeyEgg thrownTurkeyEgg = new ThrownTurkeyEgg(level, player);
            thrownTurkeyEgg.setItem(itemStack);
            thrownTurkeyEgg.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            level.addFreshEntity(thrownTurkeyEgg);
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        itemStack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }

    public @NotNull Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction) {
        ThrownTurkeyEgg thrownTurkeyEgg = new ThrownTurkeyEgg(level, position.x(), position.y(), position.z());
        thrownTurkeyEgg.setItem(itemStack);
        return thrownTurkeyEgg;
    }
}