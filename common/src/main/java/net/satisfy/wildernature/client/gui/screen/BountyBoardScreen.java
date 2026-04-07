package net.satisfy.wildernature.client.gui.screen;

import com.mojang.math.Axis;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.satisfy.wildernature.WilderNature;
import net.satisfy.wildernature.core.bounty.BountyCategory;
import net.satisfy.wildernature.core.bounty.BountyDefinition;
import net.satisfy.wildernature.core.bounty.BountyManager;
import net.satisfy.wildernature.core.gui.handler.BountyBoardMenu;
import net.satisfy.wildernature.core.network.BountyBoardNetworking;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class BountyBoardScreen extends AbstractContainerScreen<BountyBoardMenu> {
    private static final ResourceLocation TEXTURE = WilderNature.identifier("textures/gui/bounty_board/bounty_board.png");
    private static final ResourceLocation SCROLLER_TEXTURE = WilderNature.identifier("textures/gui/widgets/scroller.png");
    private static final ResourceLocation SCROLLER_DISABLED_TEXTURE = WilderNature.identifier("textures/gui/widgets/scroller_disabled.png");
    private static final int RENDER_WIDTH = 276;
    private static final int RENDER_HEIGHT = 166;
    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int LIST_START_X = 7;
    private static final int LIST_START_Y = 18;
    private static final int LIST_ENTRY_WIDTH = 89;
    private static final int LIST_ENTRY_HEIGHT = 20;
    private static final int LIST_VISIBLE_ENTRIES = 7;
    private static final int SCROLLER_X = 94;
    private static final int SCROLLER_Y = 18;
    private static final int SCROLL_BAR_HEIGHT = 139;
    private static final int SCROLLER_WIDTH = 6;
    private static final int SCROLLER_HANDLE_HEIGHT = 27;
    private static final int TITLE_X = 165;
    private static final int TITLE_Y = 14;
    private static final int OBJECTIVE_X = 165;
    private static final int OBJECTIVE_Y = 25;
    private static final int REWARD_LABEL_X = 165;
    private static final int REWARD_LABEL_Y = 40;
    private static final int REWARD_ITEM_X = 166;
    private static final int REWARD_ITEM_Y = 50;
    private static final int REWARD_XP_ICON_X = 184;
    private static final int REWARD_XP_ICON_Y = 50;
    private static final int CONTRACT_ICON_OFFSET_X = 6;
    private static final int CONTRACT_ICON_OFFSET_Y = 2;
    private static final int REWARD_ICON_OFFSET_X = 68;
    private static final int REWARD_ICON_OFFSET_Y = 2;
    private static final int ABANDON_BUTTON_X = 255;
    private static final int ABANDON_BUTTON_Y = 39;
    private static final int ABANDON_BUTTON_WIDTH = 14;
    private static final int ABANDON_BUTTON_HEIGHT = 14;
    private static final int ABANDON_BUTTON_U = 277;
    private static final int ABANDON_BUTTON_V = 74;
    private static final int ABANDON_BUTTON_HOVERED_U = 291;
    private static final int ABANDON_BUTTON_HOVERED_V = 74;
    private static final int CONTRACT_SLOT_X = 232;
    private static final int CONTRACT_SLOT_Y = 50;
    private static final int LOCK_ICON_U = 277;
    private static final int LOCK_ICON_V = 88;
    private static final int LOCK_ICON_WIDTH = 10;
    private static final int LOCK_ICON_HEIGHT = 14;
    private static final int UNLOCK_ICON_U = 287;
    private static final int UNLOCK_ICON_V = 88;
    private static final int UNLOCK_ICON_SIZE = 14;
    private static final int UNLOCK_ANIMATION_DURATION = 18;
    private int startIndex;
    private double scrollOff;
    private boolean isDragging;
    private int animationTickCounter;
    private int unlockAnimationTicks;
    private boolean hadRewardSlotsUnlockedLastTick;

    public BountyBoardScreen(BountyBoardMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = RENDER_WIDTH;
        this.imageHeight = RENDER_HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        this.clampScrollState();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.animationTickCounter++;
        this.clampScrollState();
        this.updateUnlockAnimationState();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.isDragging = false;

        int scrollerLeft = this.leftPos + SCROLLER_X;
        int scrollerTop = this.topPos + SCROLLER_Y;

        if (this.canScroll() && mouseX > scrollerLeft && mouseX < scrollerLeft + SCROLLER_WIDTH && mouseY > scrollerTop && mouseY <= scrollerTop + SCROLL_BAR_HEIGHT + 1) {
            this.isDragging = true;
            return true;
        }

        int abandonButtonX = this.leftPos + ABANDON_BUTTON_X;
        int abandonButtonY = this.topPos + ABANDON_BUTTON_Y;

        if (this.menu.hasActiveBounty()
                && mouseX >= abandonButtonX && mouseX < abandonButtonX + ABANDON_BUTTON_WIDTH
                && mouseY >= abandonButtonY && mouseY < abandonButtonY + ABANDON_BUTTON_HEIGHT) {
            BountyBoardNetworking.sendAbandon();
            return true;
        }

        int contractSlotX = this.leftPos + CONTRACT_SLOT_X;
        int contractSlotY = this.topPos + CONTRACT_SLOT_Y;

        if (mouseX >= contractSlotX && mouseX < contractSlotX + 16 && mouseY >= contractSlotY && mouseY < contractSlotY + 16) {
            if (this.menu.hasTurnInContractInserted()) {
                return true;
            }

            if (this.menu.hasRestoreContractAvailable()) {
                if (!this.menu.getCarried().isEmpty()) {
                    return super.mouseClicked(mouseX, mouseY, button);
                }
                return true;
            }

            if (this.menu.hasContractPreviewItem()) {
                Optional<BountyDefinition> targetBounty = this.menu.hasActiveBounty() ? this.menu.getActiveBounty() : this.menu.getSelectedBounty();
                if (targetBounty.isPresent() && !this.menu.isBountyAbandoned(targetBounty.get().id())) {
                    BountyBoardNetworking.sendAccept();
                    return true;
                }
            }
        }

        if (this.menu.hasActiveBounty()) {
            return super.mouseClicked(mouseX, mouseY, button);
        }

        int listStartX = this.leftPos + LIST_START_X;
        int listStartY = this.topPos + LIST_START_Y;
        int visibleEntries = Math.min(this.menu.getVisibleBounties().size() - this.startIndex, LIST_VISIBLE_ENTRIES);

        for (int entryIndex = 0; entryIndex < visibleEntries; entryIndex++) {
            int entryY = listStartY + entryIndex * LIST_ENTRY_HEIGHT;
            int absoluteIndex = this.startIndex + entryIndex;
            BountyDefinition bountyDefinition = this.menu.getVisibleBounties().get(absoluteIndex);
            boolean isUnavailable = this.menu.isBountyAbandoned(bountyDefinition.id());

            if (mouseX >= listStartX && mouseX <= listStartX + LIST_ENTRY_WIDTH && mouseY >= entryY && mouseY <= entryY + 18) {
                if (!isUnavailable) {
                    this.menu.setSelectedBountyIndex(absoluteIndex);
                    BountyBoardNetworking.sendSelect(absoluteIndex);
                }
                return true;
            }

            int contractX = listStartX + CONTRACT_ICON_OFFSET_X;
            int contractY = entryY + CONTRACT_ICON_OFFSET_Y;

            if (mouseX >= contractX && mouseX < contractX + 16 && mouseY >= contractY && mouseY < contractY + 16) {
                if (!isUnavailable) {
                    this.menu.setSelectedBountyIndex(absoluteIndex);
                    BountyBoardNetworking.sendSelect(absoluteIndex);
                    BountyBoardNetworking.sendAccept();
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int bountyCount = this.menu.getVisibleBounties().size();

        if (this.isDragging && this.canScroll()) {
            int scrollerTop = this.topPos + SCROLLER_Y;
            int scrollerBottom = scrollerTop + SCROLL_BAR_HEIGHT;
            int hiddenEntries = bountyCount - LIST_VISIBLE_ENTRIES;

            float scrollValue = ((float) mouseY - (float) scrollerTop - (float) (SCROLLER_HANDLE_HEIGHT / 2)) / ((float) (scrollerBottom - scrollerTop) - (float) SCROLLER_HANDLE_HEIGHT);
            scrollValue = scrollValue * (float) hiddenEntries + 0.5F;

            this.startIndex = Mth.clamp((int) scrollValue, 0, hiddenEntries);

            if (hiddenEntries > 0) {
                this.scrollOff = Mth.clamp((double) this.startIndex / (double) hiddenEntries, 0.0D, 1.0D);
            } else {
                this.scrollOff = 0.0D;
            }

            return true;
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.canScroll()) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        int hiddenEntries = this.menu.getVisibleBounties().size() - LIST_VISIBLE_ENTRIES;
        this.scrollOff = Mth.clamp(this.scrollOff - scrollY / (double) hiddenEntries, 0.0D, 1.0D);
        this.updateStartIndexFromScroll();
        return true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderHoveredItemTooltips(guiGraphics, mouseX, mouseY);

        if (!this.isHoveringRestoreContractSlot(mouseX, mouseY)) {
            this.renderTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        this.renderBountyList(guiGraphics);
        this.renderScroller(guiGraphics);
        this.renderTargetEntity(guiGraphics, mouseX, mouseY);
        this.renderDetailRewardIcons(guiGraphics);
        this.renderAbandonButton(guiGraphics, mouseX, mouseY);
    }

    private void renderAbandonButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.menu.getActiveBounty().isEmpty()) {
            return;
        }

        int abandonButtonX = this.leftPos + ABANDON_BUTTON_X;
        int abandonButtonY = this.topPos + ABANDON_BUTTON_Y;
        boolean hovered = mouseX >= abandonButtonX && mouseX < abandonButtonX + ABANDON_BUTTON_WIDTH
                && mouseY >= abandonButtonY && mouseY < abandonButtonY + ABANDON_BUTTON_HEIGHT;

        int textureU = hovered ? ABANDON_BUTTON_HOVERED_U : ABANDON_BUTTON_U;
        int textureV = hovered ? ABANDON_BUTTON_HOVERED_V : ABANDON_BUTTON_V;

        guiGraphics.blit(TEXTURE, abandonButtonX, abandonButtonY, textureU, textureV, ABANDON_BUTTON_WIDTH, ABANDON_BUTTON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private void renderHoveredItemTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int abandonButtonX = this.leftPos + ABANDON_BUTTON_X;
        int abandonButtonY = this.topPos + ABANDON_BUTTON_Y;

        if (this.menu.hasActiveBounty()
                && mouseX >= abandonButtonX && mouseX < abandonButtonX + ABANDON_BUTTON_WIDTH
                && mouseY >= abandonButtonY && mouseY < abandonButtonY + ABANDON_BUTTON_HEIGHT) {
            guiGraphics.renderTooltip(this.font, Component.translatable("gui.wildernature.bounty_board.abandon"), mouseX, mouseY);
            return;
        }

        if (this.isHoveringRestoreContractSlot(mouseX, mouseY)) {
            this.renderRestoreContractTooltip(guiGraphics, mouseX, mouseY);
            return;
        }

        List<BountyDefinition> visibleBounties = this.menu.getVisibleBounties();
        int listStartX = this.leftPos + LIST_START_X - 2;
        int listStartY = this.topPos + LIST_START_Y;
        int visibleEntries = Math.min(visibleBounties.size() - this.startIndex, LIST_VISIBLE_ENTRIES);

        for (int entryIndex = 0; entryIndex < visibleEntries; entryIndex++) {
            int entryY = listStartY + entryIndex * LIST_ENTRY_HEIGHT;
            int absoluteIndex = this.startIndex + entryIndex;
            BountyDefinition bountyDefinition = visibleBounties.get(absoluteIndex);

            int contractX = listStartX + CONTRACT_ICON_OFFSET_X;
            int contractY = entryY + CONTRACT_ICON_OFFSET_Y;
            int rewardX = listStartX + REWARD_ICON_OFFSET_X;
            int rewardY = entryY + REWARD_ICON_OFFSET_Y;

            if (mouseX >= contractX && mouseX < contractX + 16 && mouseY >= contractY && mouseY < contractY + 16) {
                guiGraphics.renderTooltip(this.font, this.getContractIcon(bountyDefinition), mouseX, mouseY);
                return;
            }

            if (mouseX >= rewardX && mouseX < rewardX + 16 && mouseY >= rewardY && mouseY < rewardY + 16) {
                ItemStack rewardPreviewStack = this.getRewardPreviewIcon(bountyDefinition);
                int count = bountyDefinition.reward().previewCount();
                rewardPreviewStack.setCount(count);
                guiGraphics.renderTooltip(this.font, rewardPreviewStack, mouseX, mouseY);
                return;
            }
        }

        if (this.menu.areRewardSlotsUnlocked()) {
            return;
        }

        Optional<BountyDefinition> detailBounty = this.getDisplayedDetailBounty();
        if (detailBounty.isEmpty()) {
            return;
        }

        boolean locked = this.menu.hasActiveBounty();
        Component activeBountyName = this.getActiveBountyDisplayName();

        int detailRewardX = this.leftPos + REWARD_ITEM_X;
        int detailRewardY = this.topPos + REWARD_ITEM_Y;
        int detailXpX = this.leftPos + REWARD_XP_ICON_X;
        int detailXpY = this.topPos + REWARD_XP_ICON_Y;

        if (mouseX >= detailRewardX && mouseX < detailRewardX + 16 && mouseY >= detailRewardY && mouseY < detailRewardY + 16) {
            ItemStack rewardPreviewStack = this.getRewardPreviewIcon(detailBounty.get());
            int count = detailBounty.get().reward().previewCount();
            rewardPreviewStack.setCount(count);

            if (locked) {
                guiGraphics.renderTooltip(
                        this.font,
                        List.of(
                                Component.translatable("gui.wildernature.bounty_board.reward_locked"),
                                Component.translatable("gui.wildernature.bounty_board.reward_locked_desc", activeBountyName)
                        ),
                        Optional.empty(),
                        mouseX,
                        mouseY
                );
                return;
            }

            guiGraphics.renderTooltip(this.font, rewardPreviewStack, mouseX, mouseY);
            return;
        }

        if (mouseX >= detailXpX && mouseX < detailXpX + 16 && mouseY >= detailXpY && mouseY < detailXpY + 16) {
            ItemStack experienceStack = BountyManager.createExperienceBurstStack(detailBounty.get().reward().experienceReward());

            if (locked) {
                guiGraphics.renderTooltip(
                        this.font,
                        List.of(
                                Component.translatable("gui.wildernature.bounty_board.reward_locked"),
                                Component.translatable("gui.wildernature.bounty_board.reward_locked_desc", activeBountyName)
                        ),
                        Optional.empty(),
                        mouseX,
                        mouseY
                );
                return;
            }

            guiGraphics.renderTooltip(this.font, experienceStack, mouseX, mouseY);
        }
    }

    private void renderRestoreContractTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        ItemStack contractStack = this.menu.getActiveBounty().map(this::getContractIcon).orElseGet(() -> new ItemStack(ObjectRegistry.TRACKING_ORDER.get()));
        ItemStack emeraldStack = new ItemStack(Items.EMERALD);

        Component finishedTitle = Component.translatable("gui.wildernature.bounty_board.turn_in_title");
        Component finishedDescription = Component.translatable("gui.wildernature.bounty_board.turn_in_desc");
        Component restoreTitle = Component.translatable("gui.wildernature.bounty_board.contract_restore");
        Component restoreDescription = Component.translatable("gui.wildernature.bounty_board.contract_restore_cost");

        int padding = 8;
        int iconSize = 16;
        int iconGap = 6;
        int lineHeight = 10;
        int sectionGap = 6;

        int textWidth = Math.max(
                Math.max(this.font.width(finishedTitle), this.font.width(restoreTitle)),
                Math.max(this.font.width(finishedDescription), this.font.width(restoreDescription)) + iconSize + iconGap
        );

        int tooltipWidth = padding * 2 + textWidth;
        int tooltipHeight = padding * 2 + lineHeight + iconSize + sectionGap + lineHeight + iconSize;

        int tooltipX = mouseX + 12;
        int tooltipY = mouseY - 12;

        if (tooltipX + tooltipWidth + 4 > this.width) {
            tooltipX = mouseX - 12 - tooltipWidth;
        }

        if (tooltipY + tooltipHeight + 4 > this.height) {
            tooltipY = this.height - tooltipHeight - 4;
        }

        if (tooltipX < 4) {
            tooltipX = 4;
        }

        if (tooltipY < 4) {
            tooltipY = 4;
        }

        int backgroundColor = 0xF0100010;
        int borderColorStart = 0x505000FF;
        int borderColorEnd = 0x5028007F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);

        guiGraphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, backgroundColor, backgroundColor);
        guiGraphics.fillGradient(tooltipX - 4, tooltipY - 3, tooltipX - 3, tooltipY + tooltipHeight + 3, borderColorStart, borderColorEnd);
        guiGraphics.fillGradient(tooltipX + tooltipWidth + 3, tooltipY - 3, tooltipX + tooltipWidth + 4, tooltipY + tooltipHeight + 3, borderColorStart, borderColorEnd);
        guiGraphics.fillGradient(tooltipX - 3, tooltipY - 4, tooltipX + tooltipWidth + 3, tooltipY - 3, borderColorStart, borderColorStart);
        guiGraphics.fillGradient(tooltipX - 3, tooltipY + tooltipHeight + 3, tooltipX + tooltipWidth + 3, tooltipY + tooltipHeight + 4, borderColorEnd, borderColorEnd);

        int currentY = tooltipY + padding;

        guiGraphics.drawString(this.font, finishedTitle, tooltipX + padding, currentY, 0xFFEFEFEF, false);
        currentY += lineHeight + 2;

        guiGraphics.renderItem(contractStack, tooltipX + padding, currentY - 1);
        guiGraphics.drawString(this.font, finishedDescription, tooltipX + padding + iconSize + iconGap, currentY + 4, 0xFFEFEFEF, false);
        currentY += iconSize + sectionGap;

        guiGraphics.drawString(this.font, restoreTitle, tooltipX + padding, currentY, 0xFFEFEFEF, false);
        currentY += lineHeight + 2;

        guiGraphics.renderItem(emeraldStack, tooltipX + padding, currentY - 1);
        guiGraphics.drawString(this.font, restoreDescription, tooltipX + padding + iconSize + iconGap, currentY + 4, 0xFFEFEFEF, false);

        guiGraphics.pose().popPose();
    }



    private boolean isHoveringRestoreContractSlot(int mouseX, int mouseY) {
        int contractSlotX = this.leftPos + CONTRACT_SLOT_X;
        int contractSlotY = this.topPos + CONTRACT_SLOT_Y;
        return this.menu.hasRestoreContractAvailable()
                && mouseX >= contractSlotX && mouseX < contractSlotX + 16
                && mouseY >= contractSlotY && mouseY < contractSlotY + 16;
    }

    private void renderBountyList(GuiGraphics guiGraphics) {
        List<BountyDefinition> visibleBounties = this.menu.getVisibleBounties();
        int listStartX = this.leftPos + LIST_START_X - 2;
        int listStartY = this.topPos + LIST_START_Y;
        int visibleEntries = Math.min(visibleBounties.size() - this.startIndex, LIST_VISIBLE_ENTRIES);
        Optional<BountyDefinition> activeBounty = this.menu.getActiveBounty();
        int selectedIndex = this.menu.getSelectedBountyIndex();

        for (int entryIndex = 0; entryIndex < visibleEntries; entryIndex++) {
            int entryY = listStartY + entryIndex * LIST_ENTRY_HEIGHT;
            int absoluteIndex = this.startIndex + entryIndex;
            BountyDefinition bountyDefinition = visibleBounties.get(absoluteIndex);

            boolean isActive = activeBounty.isPresent() && activeBounty.get().id().equals(bountyDefinition.id());
            boolean isSelected = absoluteIndex == selectedIndex;
            boolean isAbandoned = this.menu.isBountyAbandoned(bountyDefinition.id());
            boolean isLocked = this.menu.hasActiveBounty() && !isActive;

            int textureU;
            int textureV;

            if (isActive) {
                textureU = 277;
                textureV = 25;
            } else if (isSelected) {
                textureU = 277;
                textureV = 25;
            } else if (isAbandoned) {
                textureU = 277;
                textureV = 45;
            } else if (this.menu.hasActiveBounty()) {
                textureU = 277;
                textureV = 45;
            } else {
                textureU = 277;
                textureV = 5;
            }

            guiGraphics.blit(TEXTURE, listStartX, entryY, textureU, textureV, 88, 20, TEXTURE_WIDTH, TEXTURE_HEIGHT);

            int contractX = listStartX + CONTRACT_ICON_OFFSET_X;
            int contractY = entryY + CONTRACT_ICON_OFFSET_Y;

            guiGraphics.pose().pushPose();
            if (isLocked || isAbandoned) {
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            }
            guiGraphics.pose().translate(contractX, contractY, 0.0F);
            guiGraphics.renderItem(this.getContractIcon(bountyDefinition), 0, 0);
            guiGraphics.pose().popPose();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            int arrowX = listStartX + (LIST_ENTRY_WIDTH / 2) - 5;
            int arrowY = entryY + (LIST_ENTRY_HEIGHT / 2) - 5;
            int arrowU = isAbandoned ? 288 : 277;

            if (isLocked || isAbandoned) {
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            }
            guiGraphics.blit(TEXTURE, arrowX, arrowY, arrowU, 65, 10, 9, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            int rewardX = listStartX + REWARD_ICON_OFFSET_X;
            int rewardY = entryY + REWARD_ICON_OFFSET_Y;
            ItemStack rewardPreviewStack = this.getRewardPreviewIcon(bountyDefinition);
            int previewCount = bountyDefinition.reward().previewCount();

            guiGraphics.pose().pushPose();
            if (isLocked || isAbandoned) {
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 0.5F);
            }
            guiGraphics.pose().translate(rewardX, rewardY, 0.0F);
            guiGraphics.renderItem(rewardPreviewStack, 0, 0);
            guiGraphics.pose().popPose();
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

            if (previewCount > 1) {
                rewardPreviewStack.setCount(previewCount);
                guiGraphics.renderItemDecorations(this.font, rewardPreviewStack, rewardX, rewardY - 2);
            }
        }
    }

    private void renderTargetEntity(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Optional<BountyDefinition> detailBounty = this.getDisplayedDetailBounty();
        if (detailBounty.isEmpty() || this.minecraft == null || this.minecraft.level == null) {
            return;
        }

        BountyDefinition bountyDefinition = detailBounty.get();

        if (bountyDefinition.targetType() == BountyDefinition.BountyTargetType.ENTITY) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(bountyDefinition.targetId());
            var entity = entityType.create(this.minecraft.level);
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            int entityX = this.leftPos + 130;
            int entityY = this.topPos + 43;
            float entityScale = 16.0F;

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

            InventoryScreen.renderEntityInInventory(guiGraphics, entityX, entityY, entityScale, translation, bodyRotation, cameraRotation, livingEntity);

            livingEntity.yBodyRot = previousBodyRot;
            livingEntity.setYRot(previousYRot);
            livingEntity.setXRot(previousXRot);
            livingEntity.yHeadRotO = previousYHeadRotO;
            livingEntity.yHeadRot = previousYHeadRot;
            return;
        }

        ItemStack previewStack = BountyManager.createPreviewStack(bountyDefinition);

        int itemX = this.leftPos + 122;
        int itemY = this.topPos + 35;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(itemX, itemY, 0.0F);
        guiGraphics.pose().scale(1.0F, 1.0F, 1.0F);
        guiGraphics.renderItem(previewStack, 0, 0);
        guiGraphics.pose().popPose();
    }

    private void renderScroller(GuiGraphics guiGraphics) {
        int scrollerX = this.leftPos + SCROLLER_X;
        int scrollerY = this.topPos + SCROLLER_Y;
        int hiddenEntries = this.menu.getVisibleBounties().size() - LIST_VISIBLE_ENTRIES;

        if (hiddenEntries > 0) {
            int scrollRange = SCROLL_BAR_HEIGHT - SCROLLER_HANDLE_HEIGHT;
            int handleY = scrollerY + Math.min(scrollRange, this.startIndex * scrollRange / hiddenEntries);
            guiGraphics.blit(SCROLLER_TEXTURE, scrollerX, handleY, 0, 0, SCROLLER_WIDTH, SCROLLER_HANDLE_HEIGHT, SCROLLER_WIDTH, SCROLLER_HANDLE_HEIGHT);
        } else {
            guiGraphics.blit(SCROLLER_DISABLED_TEXTURE, scrollerX, scrollerY, 0, 0, SCROLLER_WIDTH, SCROLLER_HANDLE_HEIGHT, SCROLLER_WIDTH, SCROLLER_HANDLE_HEIGHT);
        }
    }

    private void renderDetailRewardIcons(GuiGraphics guiGraphics) {
        if (this.menu.areRewardSlotsUnlocked()) {
            if (this.unlockAnimationTicks > 0) {
                this.renderUnlockEffect(guiGraphics, this.leftPos + REWARD_ITEM_X, this.topPos + REWARD_ITEM_Y);
                this.renderUnlockEffect(guiGraphics, this.leftPos + REWARD_XP_ICON_X, this.topPos + REWARD_XP_ICON_Y);
            }
            return;
        }

        Optional<BountyDefinition> detailBounty = this.getDisplayedDetailBounty();
        if (detailBounty.isEmpty()) {
            return;
        }

        ItemStack rewardPreviewStack = this.getRewardPreviewIcon(detailBounty.get());
        int rewardPreviewCount = detailBounty.get().reward().previewCount();

        guiGraphics.renderItem(rewardPreviewStack, this.leftPos + REWARD_ITEM_X, this.topPos + REWARD_ITEM_Y);
        if (rewardPreviewCount > 1) {
            rewardPreviewStack.setCount(rewardPreviewCount);
            guiGraphics.renderItemDecorations(this.font, rewardPreviewStack, this.leftPos + REWARD_ITEM_X, this.topPos + REWARD_ITEM_Y);
        }

        ItemStack experienceStack = BountyManager.createExperienceBurstStack(detailBounty.get().reward().experienceReward());
        guiGraphics.renderItem(experienceStack, this.leftPos + REWARD_XP_ICON_X, this.topPos + REWARD_XP_ICON_Y);

        if (this.menu.hasActiveBounty()) {
            this.renderLockOverlay(guiGraphics, this.leftPos + REWARD_ITEM_X, this.topPos + REWARD_ITEM_Y);
            this.renderLockOverlay(guiGraphics, this.leftPos + REWARD_XP_ICON_X, this.topPos + REWARD_XP_ICON_Y);
        }
    }

    private void renderLockOverlay(GuiGraphics guiGraphics, int slotX, int slotY) {
        float scale = 0.94F + 0.12F * (0.5F + 0.5F * Mth.sin(this.animationTickCounter * 0.18F));
        float alpha = 0.78F + 0.22F * (0.5F + 0.5F * Mth.sin(this.animationTickCounter * 0.18F));
        float iconCenterX = slotX + 8.0F;
        float iconCenterY = slotY + 8.0F;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(iconCenterX, iconCenterY, 300.0F);
        guiGraphics.pose().scale(scale, scale, 1.0F);
        guiGraphics.pose().translate(-5.0F, -7.0F, 0.0F);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(TEXTURE, 0, 0, LOCK_ICON_U, LOCK_ICON_V, LOCK_ICON_WIDTH, LOCK_ICON_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.pose().popPose();
    }

    private void renderUnlockEffect(GuiGraphics guiGraphics, int slotX, int slotY) {
        float progress = 1.0F - (float) this.unlockAnimationTicks / (float) UNLOCK_ANIMATION_DURATION;
        float alpha = 1.0F - progress;
        int iconX = slotX + 1;
        int iconY = slotY + 1;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0.0F, 0.0F, 400.0F);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(TEXTURE, iconX, iconY, UNLOCK_ICON_U, UNLOCK_ICON_V, UNLOCK_ICON_SIZE, UNLOCK_ICON_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderSpark(guiGraphics, slotX + 8, slotY + 8, progress, alpha);
        guiGraphics.pose().popPose();
    }

    private void renderSpark(GuiGraphics guiGraphics, int centerX, int centerY, float progress, float alpha) {
        int primaryColor = ((int) (alpha * 255.0F) << 24) | 0xF4E6A1;
        int secondaryColor = ((int) (alpha * 220.0F) << 24) | 0xFFFDF0;
        int distance = 2 + (int) (progress * 7.0F);

        this.renderSparkleShape(guiGraphics, centerX, centerY - distance, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX + distance, centerY, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX, centerY + distance, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX - distance, centerY, primaryColor, secondaryColor);

        int diagonalDistance = 1 + (int) (progress * 5.0F);
        this.renderSparkleShape(guiGraphics, centerX + diagonalDistance, centerY - diagonalDistance, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX + diagonalDistance, centerY + diagonalDistance, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX - diagonalDistance, centerY + diagonalDistance, primaryColor, secondaryColor);
        this.renderSparkleShape(guiGraphics, centerX - diagonalDistance, centerY - diagonalDistance, primaryColor, secondaryColor);
    }

    private void renderSparkleShape(GuiGraphics guiGraphics, int centerX, int centerY, int primaryColor, int secondaryColor) {
        guiGraphics.fill(centerX, centerY - 1, centerX + 1, centerY + 2, primaryColor);
        guiGraphics.fill(centerX - 1, centerY, centerX + 2, centerY + 1, primaryColor);
        guiGraphics.fill(centerX, centerY, centerX + 1, centerY + 1, secondaryColor);
    }

    private void updateUnlockAnimationState() {
        boolean rewardSlotsUnlocked = this.menu.areRewardSlotsUnlocked();

        if (rewardSlotsUnlocked && !this.hadRewardSlotsUnlockedLastTick) {
            this.unlockAnimationTicks = UNLOCK_ANIMATION_DURATION;
        }

        if (this.unlockAnimationTicks > 0) {
            this.unlockAnimationTicks--;
        }

        this.hadRewardSlotsUnlockedLastTick = rewardSlotsUnlocked;
    }

    private ItemStack getContractIcon(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.type()) {
            case HUNT -> bountyDefinition.category() == BountyCategory.BOSS
                    ? new ItemStack(ObjectRegistry.ELITE_BOUNTY.get())
                    : new ItemStack(ObjectRegistry.TRACKING_ORDER.get());
            case GATHER -> new ItemStack(ObjectRegistry.PROVISION_REQUEST.get());
            case OBSERVE -> new ItemStack(ObjectRegistry.FIELD_NOTES.get());
            case EXPLORE -> new ItemStack(ObjectRegistry.PATHFINDERS_CALL.get());
        };
    }

    private ItemStack getRewardPreviewIcon(BountyDefinition bountyDefinition) {
        return new ItemStack(BuiltInRegistries.ITEM.get(bountyDefinition.reward().previewItemId()));
    }

    private boolean canScroll() {
        return this.menu.getVisibleBounties().size() > LIST_VISIBLE_ENTRIES;
    }

    private void updateStartIndexFromScroll() {
        int hiddenEntries = this.menu.getVisibleBounties().size() - LIST_VISIBLE_ENTRIES;
        this.startIndex = Mth.clamp((int) Math.round(this.scrollOff * hiddenEntries), 0, Math.max(0, hiddenEntries));
    }

    private void clampScrollState() {
        List<BountyDefinition> visibleBounties = this.menu.getVisibleBounties();
        int maxStartIndex = Math.max(0, visibleBounties.size() - LIST_VISIBLE_ENTRIES);

        this.startIndex = Mth.clamp(this.startIndex, 0, maxStartIndex);

        if (!this.canScroll()) {
            this.scrollOff = 0.0D;
            this.startIndex = 0;
            return;
        }

        if (maxStartIndex > 0) {
            this.scrollOff = Mth.clamp((double) this.startIndex / (double) maxStartIndex, 0.0D, 1.0D);
        } else {
            this.scrollOff = 0.0D;
        }
    }

    private Optional<BountyDefinition> getDisplayedDetailBounty() {
        if (this.menu.getActiveBounty().isPresent()) {
            return this.menu.getActiveBounty();
        }

        return this.menu.getSelectedBounty();
    }

    private Component getTargetDisplayName(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.targetType()) {
            case ENTITY -> {
                EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(bountyDefinition.targetId());
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(bountyDefinition.targetId())) {
                    yield entityType.getDescription();
                }
                yield Component.literal(bountyDefinition.targetId().getPath());
            }
            case ITEM -> new ItemStack(BuiltInRegistries.ITEM.get(bountyDefinition.targetId())).getHoverName();
            case BIOME -> Component.translatable("biome." + bountyDefinition.targetId().getNamespace() + "." + bountyDefinition.targetId().getPath());
        };
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 8, 6, 4210752, false);
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 108, 72, 4210752, false);

        Optional<BountyDefinition> detailBounty = this.getDisplayedDetailBounty();
        if (detailBounty.isEmpty()) {
            return;
        }

        BountyDefinition bountyDefinition = detailBounty.get();
        Component titleComponent = this.getBountyTitleComponent(bountyDefinition);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(1.1F, 1.1F, 1.0F);
        guiGraphics.drawString(this.font, titleComponent, (int) (TITLE_X / 1.1F), (int) (TITLE_Y / 1.1F), 4210752, false);
        guiGraphics.pose().popPose();

        guiGraphics.drawString(this.font, this.getObjectiveComponent(bountyDefinition), OBJECTIVE_X, OBJECTIVE_Y, 4210752, false);
        guiGraphics.drawString(this.font, Component.translatable("gui.wildernature.bounty_board.reward"), REWARD_LABEL_X, REWARD_LABEL_Y, 4210752, false);
    }

    private Component getActiveBountyDisplayName() {
        Optional<BountyDefinition> activeBounty = this.menu.getActiveBounty();
        return activeBounty.map(bountyDefinition -> {
            Component title = this.getBountyTitleComponent(bountyDefinition).copy().withStyle(style -> style.withColor(0xE6D38A));
            return Component.literal("")
                    .append(Component.literal("[").withStyle(style -> style.withColor(0xE6D38A)))
                    .append(title)
                    .append(Component.literal("]").withStyle(style -> style.withColor(0xE6D38A)));
        }).orElseGet(Component::empty);
    }

    private Component getBountyTitleComponent(BountyDefinition bountyDefinition) {
        return switch (bountyDefinition.type()) {
            case HUNT -> Component.translatable("gui.wildernature.bounty_board.title.hunt");
            case GATHER -> Component.translatable("gui.wildernature.bounty_board.title.gather");
            case OBSERVE -> Component.translatable("gui.wildernature.bounty_board.title.observe");
            case EXPLORE -> Component.translatable("gui.wildernature.bounty_board.title.explore");
        };
    }

    private Component getObjectiveComponent(BountyDefinition bountyDefinition) {
        Component targetDisplayName = this.getTargetDisplayName(bountyDefinition);
        int amount = bountyDefinition.requiredAmount();

        return switch (bountyDefinition.type()) {
            case HUNT -> amount == 1
                    ? Component.translatable("gui.wildernature.bounty_board.objective.hunt.single", targetDisplayName)
                    : Component.translatable("gui.wildernature.bounty_board.objective.hunt.plural", amount, targetDisplayName);
            case GATHER -> Component.translatable("gui.wildernature.bounty_board.objective.gather", targetDisplayName);
            case OBSERVE -> Component.translatable("gui.wildernature.bounty_board.objective.observe", targetDisplayName);
            case EXPLORE -> Component.translatable("gui.wildernature.bounty_board.objective.explore", targetDisplayName);
        };
    }
}