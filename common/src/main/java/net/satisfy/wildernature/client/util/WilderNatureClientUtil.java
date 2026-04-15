package net.satisfy.wildernature.client.util;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.satisfy.wildernature.WilderNature;
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
        return createLivingEntity(entry.entityId());
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
            case "plains", "sunflower_plains", "birch_forest", "dark_forest", "beach", "desert", "forest", "river", "savanna", "taiga" -> WilderNature.identifier("textures/gui/icons/" + biomePath + ".png");
            default -> null;
        };
    }
}