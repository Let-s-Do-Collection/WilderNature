package net.satisfy.wildernature.client.util;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.entity.animal.defensive.LionEntity;
import net.satisfy.wildernature.core.fieldguide.FieldGuideEntry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;

public class WilderNatureClientUtil {
    public static void init() {
        if (Platform.isFabric() && Platform.getEnvironment() == Env.CLIENT) {
            initClient();
        }
    }

    private static void initClient() {
        makeHorn(ObjectRegistry.BISON_HORN.get());
    }

    public static void makeHorn(Item item) {
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("blowing"), (itemStack, clientLevel, livingEntity, seed) -> {
            if (livingEntity == null) {
                return 0.0F;
            }

            if (livingEntity.getUseItem() != itemStack) {
                return 0.0F;
            }

            return (float) (itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks()) / 20.0F;
        });

        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("using"), (itemStack, clientLevel, livingEntity, seed) -> livingEntity != null && livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack ? 1.0F : 0.0F);
    }

    public static LivingEntity createLivingEntity(FieldGuideEntry entry) {
        LivingEntity livingEntity = createLivingEntity(entry.entityId());
        if (livingEntity instanceof LionEntity lion) {
            lion.setMale(entry.male());
        }
        return livingEntity;
    }

    public static LivingEntity createLivingEntity(ResourceLocation entityId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
        var entity = entityType.create(minecraft.level);
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        }

        return null;
    }

    public static float getMaxHealth(FieldGuideEntry entry) {
        LivingEntity livingEntity = createLivingEntity(entry);
        if (livingEntity == null) {
            return 0.0F;
        }

        return livingEntity.getMaxHealth();
    }

    public static int[] getCenteredSlotIndexes(int count) {
        return switch (count) {
            case 1 -> new int[]{2};
            case 2 -> new int[]{1, 3};
            case 3 -> new int[]{1, 2, 3};
            case 4 -> new int[]{0, 1, 3, 4};
            default -> new int[]{0, 1, 2, 3, 4};
        };
    }

    public static ResourceLocation getBiomeTexture(ResourceLocation biomeId) {
        String biomePath = biomeId.getPath();
        return switch (biomePath) {
            case "plains", "sunflower_plains", "birch_forest", "dark_forest", "beach", "desert", "forest", "river", "savanna", "taiga", "meadow", "frozen_peaks" -> WilderNature.identifier("textures/gui/icons/" + biomePath + ".png");
            default -> null;
        };
    }

    public static ItemStack getFoodPreviewStack(FieldGuideEntry entry) {
        if (entry.food() == null || entry.food().isBlank()) {
            return ItemStack.EMPTY;
        }

        if (!entry.food().startsWith("#")) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.food()));
            return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        }

        ResourceLocation tagId = ResourceLocation.parse(entry.food().substring(1));
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        var optionalTag = BuiltInRegistries.ITEM.getTag(tagKey);

        if (optionalTag.isEmpty()) {
            return ItemStack.EMPTY;
        }

        for (Holder<Item> holder : optionalTag.get()) {
            Item item = holder.value();
            if (item != Items.AIR) {
                return new ItemStack(item);
            }
        }

        return ItemStack.EMPTY;
    }

    public static Component getFoodTooltip(FieldGuideEntry entry) {
        if (entry.food() == null || entry.food().isBlank()) {
            return null;
        }

        if (!entry.food().startsWith("#")) {
            ItemStack itemStack = getFoodPreviewStack(entry);
            return itemStack.isEmpty() ? null : Component.translatable("tooltip.wildernature.eats", itemStack.getHoverName());
        }

        return Component.translatable("tooltip.wildernature.eats_tag", entry.food());
    }
}