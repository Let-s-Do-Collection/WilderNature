package net.satisfy.wildernature.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
@Environment(EnvType.CLIENT)
public abstract class CompendiumScreen extends Screen {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            WilderNatureIdentifier.of("textures/gui/compendium/page1.png"),
            WilderNatureIdentifier.of("textures/gui/compendium/page2.png"),
            WilderNatureIdentifier.of("textures/gui/compendium/page3.png"),
            WilderNatureIdentifier.of("textures/gui/compendium/page4.png"),
            WilderNatureIdentifier.of("textures/gui/compendium/page5.png"),
            WilderNatureIdentifier.of("textures/gui/compendium/page6.png")
    };

    private static final PageData[] PAGES = new PageData[]{
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page1_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page1_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page1_left_spawns", 54, 139, 111, 156),
                            new TooltipData("compendium.wildernature.page1_right_health", 184, 103, 281, 113),
                            new TooltipData("compendium.wildernature.page1_right_alignment", 184, 27, 201, 89),
                            new TooltipData("compendium.wildernature.page1_right_spawns", 214, 139, 251, 156)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page1_squirrel", 62, 126),
                            new FixedTextData("compendium.wildernature.page1_raccoon", 212, 126)
                    }
            ),
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page2_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page2_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page2_left_spawns", 74, 139, 91, 156),
                            new TooltipData("compendium.wildernature.page2_right_health", 184, 103, 281, 113),
                            new TooltipData("compendium.wildernature.page2_right_alignment", 184, 27, 201, 89),
                            new TooltipData("compendium.wildernature.page2_right_spawns", 224, 139, 241, 156)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page2_red_wolf", 62, 126),
                            new FixedTextData("compendium.wildernature.page2_minisheep", 212, 126)
                    }
            ),
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page3_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page3_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page3_left_spawns", 64, 139, 101, 156),
                            new TooltipData("compendium.wildernature.page3_right_health", 184, 103, 281, 113),
                            new TooltipData("compendium.wildernature.page3_right_alignment", 184, 27, 201, 89),
                            new TooltipData("compendium.wildernature.page3_right_spawns", 224, 139, 241, 156)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page3_boar", 72, 126),
                            new FixedTextData("compendium.wildernature.page3_owl", 225, 126)
                    }
            ),
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page4_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page4_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page4_left_spawns", 74, 139, 91, 156),
                            new TooltipData("compendium.wildernature.page4_right_health", 184, 103, 281, 113),
                            new TooltipData("compendium.wildernature.page4_right_alignment", 184, 27, 201, 89)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page4_deer", 72, 126),
                            new FixedTextData("compendium.wildernature.page4_dog", 225, 126)
                    }
            ),
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page5_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page5_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page5_left_spawns", 54, 139, 111, 156),
                            new TooltipData("compendium.wildernature.page5_right_health", 184, 103, 281, 113),
                            new TooltipData("compendium.wildernature.page5_right_alignment", 184, 27, 201, 89),
                            new TooltipData("compendium.wildernature.page5_right_spawns", 224, 139, 241, 156)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page5_turkey", 65, 126),
                            new FixedTextData("compendium.wildernature.page5_bison", 220, 126)
                    }
            ),
            new PageData(
                    new TooltipData[]{
                            new TooltipData("compendium.wildernature.page6_left_health", 34, 101, 131, 113),
                            new TooltipData("compendium.wildernature.page6_left_alignment", 34, 27, 51, 80),
                            new TooltipData("compendium.wildernature.page6_left_spawns", 64, 139, 101, 156)
                    },
                    new FixedTextData[]{
                            new FixedTextData("compendium.wildernature.page6_pelican", 65, 126)
                    }
            )
    };

    private int currentPage;

    protected CompendiumScreen() {
        super(Component.translatable("compendium.wildernature.title"));
        this.currentPage = 0;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        int textureWidth = 316;
        int textureHeight = 190;
        int x = (this.width - textureWidth) / 2;
        int y = (this.height - textureHeight) / 2;
        guiGraphics.blit(TEXTURES[currentPage], x, y, 0, 0, textureWidth, textureHeight, textureWidth, textureHeight);

        PageData pageData = PAGES[currentPage];

        for (TooltipData tooltipData : pageData.tooltips()) {
            if (isMouseOverArea(mouseX, mouseY, x + tooltipData.x1(), y + tooltipData.y1(), x + tooltipData.x2(), y + tooltipData.y2())) {
                Component tooltipText = Component.translatable(tooltipData.text());
                guiGraphics.renderTooltip(this.font, tooltipText, mouseX, mouseY);
            }
        }

        for (FixedTextData fixedTextData : pageData.fixedTexts()) {
            Component fixedText = Component.translatable(fixedTextData.text());
            guiGraphics.drawString(this.font, fixedText, x + fixedTextData.x(), y + fixedTextData.y(), 0xFFFFFF);
        }

        if (currentPage < TEXTURES.length - 1 && isMouseOverArea(mouseX, mouseY, 356, 43, 371, 227)) {
            guiGraphics.fill(356, 43, 371, 227, 0x80000000);
        }

        if (currentPage > 0 && isMouseOverArea(mouseX, mouseY, 61, 43, 76, 227)) {
            guiGraphics.fill(61, 43, 76, 227, 0x80000000);
        }

        super.render(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (currentPage < TEXTURES.length - 1 && isMouseOverArea((int) mouseX, (int) mouseY, 356, 43, 371, 227)) {
            nextPage();
            return true;
        }
        if (currentPage > 0 && isMouseOverArea((int) mouseX, (int) mouseY, 61, 43, 76, 227)) {
            previousPage();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverArea(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }

    @Override
    public @NotNull Component getTitle() {
        return Component.translatable("compendium.wildernature.title");
    }

    public void nextPage() {
        if (currentPage < TEXTURES.length - 1) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }

    private record PageData(TooltipData[] tooltips, FixedTextData[] fixedTexts) {
    }

    private record TooltipData(String text, int x1, int y1, int x2, int y2) {
    }

    private record FixedTextData(String text, int x, int y) {
    }
}
