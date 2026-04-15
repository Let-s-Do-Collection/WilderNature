package net.satisfy.wildernature.client.gui.screen;

import com.mojang.math.Axis;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.client.util.WilderNatureClientUtil;
import net.satisfy.wildernature.core.fieldguide.FieldGuideEntry;
import net.satisfy.wildernature.core.gui.handler.FieldGuideMenu;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FieldGuideScreen extends AbstractContainerScreen<FieldGuideMenu> {
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("wildernature:textures/gui/field_guide/field_guide.png");
    private static final ResourceLocation FRIENDLY_TEXTURE = WilderNature.identifier("textures/gui/icons/friendly.png");
    private static final ResourceLocation FRIENDLY_GRAYSCALE_TEXTURE = WilderNature.identifier("textures/gui/icons/friendly_grayscale.png");
    private static final ResourceLocation NEUTRAL_TEXTURE = WilderNature.identifier("textures/gui/icons/neutral.png");
    private static final ResourceLocation NEUTRAL_GRAYSCALE_TEXTURE = WilderNature.identifier("textures/gui/icons/neutral_grayscale.png");
    private static final ResourceLocation DEFENSIVE_TEXTURE = WilderNature.identifier("textures/gui/icons/defensive.png");
    private static final ResourceLocation DEFENSIVE_GRAYSCALE_TEXTURE = WilderNature.identifier("textures/gui/icons/defensive_grayscale.png");
    private static final ResourceLocation HEART_TEXTURE = WilderNature.identifier("textures/gui/icons/heart.png");

    private static final int TEXTURE_WIDTH = 316;
    private static final int TEXTURE_HEIGHT = 190;

    private static final int LIST_LEFT_OFFSET = 12;
    private static final int LIST_TOP_OFFSET = 18;
    private static final int LIST_WIDTH = 111;
    private static final int ENTRY_HEIGHT = 22;
    private static final int VISIBLE_ENTRY_COUNT = 7;

    private static final int TRAIT_ICON_WIDTH = 12;
    private static final int TRAIT_ICON_HEIGHT = 12;
    private static final int BIOME_ICON_SIZE = 16;

    private static final int LEFT_ENTRY_FRIENDLY_ICON_X = 37;
    private static final int RIGHT_ENTRY_FRIENDLY_ICON_X = 187;
    private static final int ENTRY_FRIENDLY_ICON_Y = 46;

    private static final int LEFT_ENTRY_NEUTRAL_ICON_X = 37;
    private static final int RIGHT_ENTRY_NEUTRAL_ICON_X = 187;
    private static final int ENTRY_NEUTRAL_ICON_Y = 68;

    private static final int LEFT_ENTRY_DEFENSIVE_ICON_X = 37;
    private static final int RIGHT_ENTRY_DEFENSIVE_ICON_X = 187;
    private static final int ENTRY_DEFENSIVE_ICON_Y = 90;

    private static final int LEFT_ENTITY_PREVIEW_X = 68;
    private static final int RIGHT_ENTITY_PREVIEW_X = 218;
    private static final int ENTITY_PREVIEW_Y = 44;
    private static final int ENTITY_PREVIEW_CENTER_X = 31;
    private static final int ENTITY_PREVIEW_BASELINE_Y = 38;
    private static final float ENTITY_PREVIEW_SCALE = 22.0F;

    private static final int LEFT_HEALTH_AREA_X = 35;
    private static final int RIGHT_HEALTH_AREA_X = 185;
    private static final int HEALTH_AREA_Y = 118;
    private static final int HEALTH_AREA_WIDTH = 96;
    private static final int HEALTH_AREA_HEIGHT = 11;
    private static final int HEART_WIDTH = 9;
    private static final int HEART_HEIGHT = 9;

    private static final int BIOME_ICON_Y = 140;
    private static final int[] LEFT_BIOME_SLOTS = new int[]{35, 55, 75, 95, 115};
    private static final int[] RIGHT_BIOME_SLOTS = new int[]{185, 205, 225, 245, 265};

    private static final int LEFT_TITLE_FIELD_X = 46;
    private static final int RIGHT_TITLE_FIELD_X = 196;
    private static final int TITLE_FIELD_Y = 28;
    private static final int TITLE_FIELD_WIDTH = 74;
    private static final int TITLE_FIELD_HEIGHT = 11;

    private static final int TITLE_COLOR = 0xFFA48165;

    private final List<FieldGuideEntry> entries;
    private int selectedIndex;
    private int scrollOffset;

    public FieldGuideScreen(FieldGuideMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, Component.empty());
        this.imageWidth = TEXTURE_WIDTH;
        this.imageHeight = TEXTURE_HEIGHT;
        this.entries = new ArrayList<>(menu.getEntries());
        this.selectedIndex = this.entries.isEmpty() ? -1 : 0;
        this.scrollOffset = 0;
    }

    @Override
    protected void init() {
        super.init();

        if (!this.entries.isEmpty() && (this.selectedIndex < 0 || this.selectedIndex >= this.entries.size())) {
            this.selectedIndex = 0;
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int maxScroll = Math.max(0, this.entries.size() - VISIBLE_ENTRY_COUNT);

        if (scrollY < 0.0D && this.scrollOffset < maxScroll) {
            this.scrollOffset++;
            return true;
        }

        if (scrollY > 0.0D && this.scrollOffset > 0) {
            this.scrollOffset--;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int listLeft = this.leftPos + LIST_LEFT_OFFSET;
        int listTop = this.topPos + LIST_TOP_OFFSET;

        if (mouseX >= listLeft && mouseX <= listLeft + LIST_WIDTH && mouseY >= listTop && mouseY <= listTop + VISIBLE_ENTRY_COUNT * ENTRY_HEIGHT) {
            int clickedIndex = (int) ((mouseY - listTop) / ENTRY_HEIGHT) + this.scrollOffset;
            if (clickedIndex >= 0 && clickedIndex < this.entries.size()) {
                this.selectedIndex = clickedIndex;
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        this.renderBg(guiGraphics, partialTick, mouseX, mouseY);
        this.renderTooltips(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.renderEntries(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    private void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.entries.size()) {
            return;
        }

        this.renderEntry(guiGraphics, mouseX, mouseY, this.entries.get(this.selectedIndex), true);

        if (this.selectedIndex + 1 < this.entries.size()) {
            this.renderEntry(guiGraphics, mouseX, mouseY, this.entries.get(this.selectedIndex + 1), false);
        }
    }

    private void renderEntry(GuiGraphics guiGraphics, int mouseX, int mouseY, FieldGuideEntry entry, boolean leftPage) {
        int entityPreviewX = this.leftPos + (leftPage ? LEFT_ENTITY_PREVIEW_X : RIGHT_ENTITY_PREVIEW_X);
        int friendlyIconX = this.leftPos + (leftPage ? LEFT_ENTRY_FRIENDLY_ICON_X : RIGHT_ENTRY_FRIENDLY_ICON_X);
        int neutralIconX = this.leftPos + (leftPage ? LEFT_ENTRY_NEUTRAL_ICON_X : RIGHT_ENTRY_NEUTRAL_ICON_X);
        int defensiveIconX = this.leftPos + (leftPage ? LEFT_ENTRY_DEFENSIVE_ICON_X : RIGHT_ENTRY_DEFENSIVE_ICON_X);
        int healthAreaX = this.leftPos + (leftPage ? LEFT_HEALTH_AREA_X : RIGHT_HEALTH_AREA_X);
        int titleFieldX = this.leftPos + (leftPage ? LEFT_TITLE_FIELD_X : RIGHT_TITLE_FIELD_X);
        int[] biomeSlots = leftPage ? LEFT_BIOME_SLOTS : RIGHT_BIOME_SLOTS;

        this.renderEntityPreview(guiGraphics, entry, mouseX, mouseY, entityPreviewX, this.topPos + ENTITY_PREVIEW_Y);
        this.renderFriendlyIcon(guiGraphics, entry, friendlyIconX, this.topPos + ENTRY_FRIENDLY_ICON_Y);
        this.renderNeutralIcon(guiGraphics, entry, neutralIconX, this.topPos + ENTRY_NEUTRAL_ICON_Y);
        this.renderDefensiveIcon(guiGraphics, entry, defensiveIconX, this.topPos + ENTRY_DEFENSIVE_ICON_Y);
        this.renderHealth(guiGraphics, entry, healthAreaX, this.topPos + HEALTH_AREA_Y);
        this.renderTitle(guiGraphics, entry, titleFieldX, this.topPos + TITLE_FIELD_Y);
        this.renderBiomeIcons(guiGraphics, entry, biomeSlots);
    }

    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.entries.size()) {
            return;
        }

        if (this.renderTooltipsForPage(guiGraphics, mouseX, mouseY, this.entries.get(this.selectedIndex), true)) {
            return;
        }

        if (this.selectedIndex + 1 < this.entries.size()) {
            this.renderTooltipsForPage(guiGraphics, mouseX, mouseY, this.entries.get(this.selectedIndex + 1), false);
        }
    }

    private boolean renderTooltipsForPage(GuiGraphics guiGraphics, int mouseX, int mouseY, FieldGuideEntry entry, boolean leftPage) {
        int friendlyIconX = this.leftPos + (leftPage ? LEFT_ENTRY_FRIENDLY_ICON_X : RIGHT_ENTRY_FRIENDLY_ICON_X);
        int neutralIconX = this.leftPos + (leftPage ? LEFT_ENTRY_NEUTRAL_ICON_X : RIGHT_ENTRY_NEUTRAL_ICON_X);
        int defensiveIconX = this.leftPos + (leftPage ? LEFT_ENTRY_DEFENSIVE_ICON_X : RIGHT_ENTRY_DEFENSIVE_ICON_X);
        int healthAreaX = this.leftPos + (leftPage ? LEFT_HEALTH_AREA_X : RIGHT_HEALTH_AREA_X);
        Component entityName = BuiltInRegistries.ENTITY_TYPE.get(entry.entityId()).getDescription();

        if (entry.friendly() && this.isHovering(mouseX, mouseY, friendlyIconX, this.topPos + ENTRY_FRIENDLY_ICON_Y, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.wildernature.friendly", entityName), mouseX, mouseY);
            return true;
        }

        if (entry.neutral() && this.isHovering(mouseX, mouseY, neutralIconX, this.topPos + ENTRY_NEUTRAL_ICON_Y, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.wildernature.neutral", entityName), mouseX, mouseY);
            return true;
        }

        if (entry.defensive() && this.isHovering(mouseX, mouseY, defensiveIconX, this.topPos + ENTRY_DEFENSIVE_ICON_Y, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT)) {
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.wildernature.defensive", entityName), mouseX, mouseY);
            return true;
        }

        if (this.isHovering(mouseX, mouseY, healthAreaX, this.topPos + HEALTH_AREA_Y, HEALTH_AREA_WIDTH, HEALTH_AREA_HEIGHT)) {
            int hearts = Mth.ceil(WilderNatureClientUtil.getMaxHealth(entry) / 2.0F);
            guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.wildernature.health", entityName, hearts), mouseX, mouseY);
            return true;
        }

        int[] biomeSlots = leftPage ? LEFT_BIOME_SLOTS : RIGHT_BIOME_SLOTS;
        return this.renderBiomeTooltip(guiGraphics, mouseX, mouseY, entry, biomeSlots);
    }

    private boolean renderBiomeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, FieldGuideEntry entry, int[] biomeSlots) {
        List<ResourceLocation> biomeIds = entry.biomes();
        if (biomeIds.isEmpty()) {
            return false;
        }

        List<ResourceLocation> displayedBiomes = biomeIds.subList(0, Math.min(biomeIds.size(), biomeSlots.length));
        int[] slotIndexes = WilderNatureClientUtil.getCenteredSlotIndexes(displayedBiomes.size());
        Component entityName = BuiltInRegistries.ENTITY_TYPE.get(entry.entityId()).getDescription();

        for (int index = 0; index < displayedBiomes.size(); index++) {
            int iconX = this.leftPos + biomeSlots[slotIndexes[index]];
            int iconY = this.topPos + BIOME_ICON_Y;

            if (this.isHovering(mouseX, mouseY, iconX, iconY, BIOME_ICON_SIZE, BIOME_ICON_SIZE)) {
                ResourceLocation biomeId = displayedBiomes.get(index);
                Component biomeName = Component.translatable("biome." + biomeId.getNamespace() + "." + biomeId.getPath());
                guiGraphics.renderTooltip(this.font, Component.translatable("tooltip.wildernature.spawn", entityName, biomeName), mouseX, mouseY);
                return true;
            }
        }

        return false;
    }

    private void renderFriendlyIcon(GuiGraphics guiGraphics, FieldGuideEntry entry, int iconX, int iconY) {
        ResourceLocation texture = entry.friendly() ? FRIENDLY_TEXTURE : FRIENDLY_GRAYSCALE_TEXTURE;
        guiGraphics.blit(texture, iconX, iconY, 0, 0, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT);
    }

    private void renderNeutralIcon(GuiGraphics guiGraphics, FieldGuideEntry entry, int iconX, int iconY) {
        ResourceLocation texture = entry.neutral() ? NEUTRAL_TEXTURE : NEUTRAL_GRAYSCALE_TEXTURE;
        guiGraphics.blit(texture, iconX, iconY, 0, 0, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT);
    }

    private void renderDefensiveIcon(GuiGraphics guiGraphics, FieldGuideEntry entry, int iconX, int iconY) {
        ResourceLocation texture = entry.defensive() ? DEFENSIVE_TEXTURE : DEFENSIVE_GRAYSCALE_TEXTURE;
        guiGraphics.blit(texture, iconX, iconY, 0, 0, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT, TRAIT_ICON_WIDTH, TRAIT_ICON_HEIGHT);
    }

    private void renderEntityPreview(GuiGraphics guiGraphics, FieldGuideEntry entry, int mouseX, int mouseY, int areaX, int areaY) {
        LivingEntity livingEntity = WilderNatureClientUtil.createLivingEntity(entry);
        if (livingEntity == null) {
            return;
        }

        int entityX = areaX + ENTITY_PREVIEW_CENTER_X;
        int entityY = areaY + ENTITY_PREVIEW_BASELINE_Y;

        float yawOffset = (float) Math.atan((entityX - mouseX) / 40.0F);
        float pitchOffset = (float) Math.atan((entityY - mouseY) / 40.0F);

        Quaternionf bodyRotation = Axis.ZP.rotationDegrees(180.0F);
        Quaternionf pitchRotation = Axis.XP.rotationDegrees(pitchOffset * 20.0F);
        bodyRotation.mul(pitchRotation);

        float previousBodyRot = livingEntity.yBodyRot;
        float previousYRot = livingEntity.getYRot();
        float previousXRot = livingEntity.getXRot();
        float previousYHeadRotO = livingEntity.yHeadRotO;
        float previousYHeadRot = livingEntity.yHeadRot;

        livingEntity.yBodyRot = 180.0F + yawOffset * 20.0F;
        livingEntity.setYRot(180.0F + yawOffset * 40.0F);
        livingEntity.setXRot(-pitchOffset * 20.0F);
        livingEntity.yHeadRot = livingEntity.getYRot();
        livingEntity.yHeadRotO = livingEntity.getYRot();

        Vector3f translation = new Vector3f(0.0F, livingEntity.getBbHeight() * 0.5F, 0.0F);
        Quaternionf cameraRotation = pitchRotation.conjugate(new Quaternionf());

        guiGraphics.flush();
        InventoryScreen.renderEntityInInventory(guiGraphics, entityX, entityY, ENTITY_PREVIEW_SCALE, translation, bodyRotation, cameraRotation, livingEntity);
        guiGraphics.flush();

        livingEntity.yBodyRot = previousBodyRot;
        livingEntity.setYRot(previousYRot);
        livingEntity.setXRot(previousXRot);
        livingEntity.yHeadRotO = previousYHeadRotO;
        livingEntity.yHeadRot = previousYHeadRot;
    }

    private void renderHealth(GuiGraphics guiGraphics, FieldGuideEntry entry, int areaX, int areaY) {
        int heartCount = Mth.ceil(WilderNatureClientUtil.getMaxHealth(entry) / 2.0F);
        if (heartCount <= 0) {
            return;
        }

        int heartSpacing = Math.min(HEART_WIDTH, Math.max(1, HEALTH_AREA_WIDTH / heartCount));
        int totalWidth = HEART_WIDTH + (heartCount - 1) * heartSpacing;
        int startX = areaX + (HEALTH_AREA_WIDTH - totalWidth) / 2;
        int startY = areaY + (HEALTH_AREA_HEIGHT - HEART_HEIGHT) / 2;

        for (int index = 0; index < heartCount; index++) {
            guiGraphics.blit(HEART_TEXTURE, startX + index * heartSpacing, startY, 0, 0, HEART_WIDTH, HEART_HEIGHT, HEART_WIDTH, HEART_HEIGHT);
        }
    }

    private void renderTitle(GuiGraphics guiGraphics, FieldGuideEntry entry, int fieldX, int fieldY) {
        String title = BuiltInRegistries.ENTITY_TYPE.get(entry.entityId()).getDescription().getString();
        int textWidth = this.font.width(title);
        int textX = fieldX + (TITLE_FIELD_WIDTH - textWidth) / 2;
        int textY = fieldY + (TITLE_FIELD_HEIGHT - this.font.lineHeight) / 2;
        guiGraphics.drawString(this.font, title, textX, textY, TITLE_COLOR, false);
    }

    private void renderBiomeIcons(GuiGraphics guiGraphics, FieldGuideEntry entry, int[] biomeSlots) {
        List<ResourceLocation> biomeIds = entry.biomes();
        if (biomeIds.isEmpty()) {
            return;
        }

        List<ResourceLocation> displayedBiomes = biomeIds.subList(0, Math.min(biomeIds.size(), biomeSlots.length));
        int[] slotIndexes = WilderNatureClientUtil.getCenteredSlotIndexes(displayedBiomes.size());

        for (int index = 0; index < displayedBiomes.size(); index++) {
            ResourceLocation texture = WilderNatureClientUtil.getBiomeTexture(displayedBiomes.get(index));
            if (texture == null) {
                continue;
            }

            int iconX = this.leftPos + biomeSlots[slotIndexes[index]];
            int iconY = this.topPos + BIOME_ICON_Y;
            guiGraphics.blit(texture, iconX, iconY, 0, 0, BIOME_ICON_SIZE, BIOME_ICON_SIZE, BIOME_ICON_SIZE, BIOME_ICON_SIZE);
        }
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}