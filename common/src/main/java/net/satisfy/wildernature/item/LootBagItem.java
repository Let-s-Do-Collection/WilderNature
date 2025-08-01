package net.satisfy.wildernature.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.registry.ObjectRegistry;
import net.satisfy.wildernature.registry.TagsRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LootBagItem extends Item {
    public LootBagItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide || user.isCrouching()) {
            return super.use(world, user, hand);
        }

        ItemStack itemStack = user.getItemInHand(hand);
        ItemStack spawnedItem;
        int randomNumber = world.random.nextInt(10000);

        if (randomNumber < 5) {
            spawnedItem = new ItemStack(Items.DIAMOND);
        } else if (randomNumber < 105) {
            spawnedItem = new ItemStack(Items.EMERALD);
        } else if (randomNumber < 2105) {
            spawnedItem = new ItemStack(ObjectRegistry.HAZELNUT.get());
        } else {
            List<Item> edibleItems = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item.getDefaultInstance().has(DataComponents.FOOD) && !item.builtInRegistryHolder().is(TagsRegistry.LOOT_BAG_BLACKLIST)) {
                    edibleItems.add(item);
                }
            }
            if (!edibleItems.isEmpty()) {
                spawnedItem = new ItemStack(edibleItems.get(world.random.nextInt(edibleItems.size())));
            } else {
                spawnedItem = new ItemStack(Items.APPLE);
            }
        }

        world.addFreshEntity(new ItemEntity(world, user.getX(), user.getY(), user.getZ(), spawnedItem));

        if (!user.isCreative()) {
            itemStack.shrink(1);
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 1.0F, 1.0F);
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide());
    }
}
