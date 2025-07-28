package net.satisfy.wildernature.client.gui.screens;

import dev.architectury.networking.NetworkManager;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Inventory;
import net.satisfy.wildernature.client.gui.components.ContractButton;
import net.satisfy.wildernature.client.gui.handlers.BountyBlockScreenHandler;
import net.satisfy.wildernature.network.BountyBlockNetworking;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import net.satisfy.wildernature.util.contract.Contract;

public class BountyBlockScreen extends AbstractContainerScreen<BountyBlockScreenHandler> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 169;

    private static final ResourceLocation TEX_BACKGROUND = WilderNatureIdentifier.of("textures/gui/bounty_board/background.png");
    private static final ResourceLocation TEX_REROLL = WilderNatureIdentifier.of("textures/gui/bounty_board/reroll.png");
    private static final ResourceLocation TEX_DELETE = WilderNatureIdentifier.of("textures/gui/bounty_board/delete.png");
    private static final ResourceLocation TEX_ACCEPT = WilderNatureIdentifier.of("textures/gui/bounty_board/accept.png");
    private static final ResourceLocation TEX_FINISHEDSLOT = WilderNatureIdentifier.of("textures/gui/bounty_board/finished_bg.png");
    private static final ResourceLocation TEX_BAR = WilderNatureIdentifier.of("textures/gui/bounty_board/bar.png");

    private ImageButton rerollButton;
    private ImageButton acceptButton;
    private ImageButton finishButton;
    private ImageButton deleteButton;
    private final ContractButton[] contractButtons = new ContractButton[3];
    private ContractButton targetContractButton;

    public BountyBlockScreen(BountyBlockScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        initializeButtons();
        initializeContractButtons();
        setupCallbacks();
    }

    private void initializeButtons() {
        var centerX = width / 2;
        var centerY = height / 2;

        rerollButton = createImageButton(centerX - 74, centerY - 52, TEX_REROLL, this::onReroll);
        acceptButton = createImageButton(centerX - GUI_WIDTH / 2 + 135, centerY - GUI_HEIGHT / 2 + 51, TEX_ACCEPT, this::onAccept);
        finishButton = createImageButton(centerX - GUI_WIDTH / 2 + 135, centerY - GUI_HEIGHT / 2 + 51, TEX_ACCEPT, this::onFinish);
        finishButton.setTooltip(Tooltip.create(Component.translatable("text.gui.wildernature.bounty.finish")));
        deleteButton = createImageButton(centerX - 58, centerY - 52, TEX_DELETE, this::onDeleteContract);
        deleteButton.setTooltip(Tooltip.create(Component.translatable("text.gui.wildernature.bounty.delete_contract")));

        addRenderableWidget(rerollButton);
        addRenderableWidget(acceptButton);
        addRenderableWidget(finishButton);
        addRenderableWidget(deleteButton);
    }

    private ImageButton createImageButton(int x, int y, ResourceLocation texture, Button.OnPress onPress) {
        return new ImageButton(x, y, new WidgetSprites(texture, texture), onPress, Component.empty());
    }

    private void initializeContractButtons() {
        var centerX = width / 2;
        var centerY = height / 2;
        targetContractButton = new ContractButton(centerX - GUI_WIDTH / 2 + 97, centerY - GUI_HEIGHT / 2 + 49, null, (button) -> {});

        for (int i = 0; i < 3; i++) {
            var contract = menu.contracts[i];
            contractButtons[i] = addRenderableWidget(new ContractButton(centerX - GUI_WIDTH / 2 + 25 + i * 18, centerY - GUI_HEIGHT / 2 + 49, contract, (button) ->
                    this.setSelectedContract(((ContractButton) button).getContract())));
        }
        addRenderableWidget(targetContractButton);
    }

    private void setupCallbacks() {
        menu.onContractUpdate.subscribe(() -> {
            targetContractButton.setContract(null);
            finishButton.visible = menu.activeContractProgress != null && menu.activeContractProgress.isFinished();
            for (int i = 0; i < 3; i++) {
                contractButtons[i].setContract(menu.contracts[i]);
            }
        });

        addRenderableOnly((guiGraphics, mx, my, f) -> renderFinishedSlot(guiGraphics));
        addRenderableOnly((guiGraphics, mx, my, f) -> renderWarningTooltip(guiGraphics));
        addRenderableOnly((guiGraphics, i, j, f) -> renderProgressBar(guiGraphics));
    }

    private void renderFinishedSlot(GuiGraphics guiGraphics) {
        if (menu.activeContractProgress != null && menu.activeContractProgress.isFinished()) {
            var centerX = width / 2;
            var centerY = height / 2;
            int alpha = 128;
            int color = FastColor.ARGB32.color(alpha, 255, 255, 255);
            guiGraphics.blit(TEX_FINISHEDSLOT, centerX - GUI_WIDTH / 2 + 95, centerY - GUI_HEIGHT / 2 + 47, 0, 0, 22, 22, 22, 22, color);
        }
    }

    private void renderWarningTooltip(GuiGraphics guiGraphics) {
        var centerX = width / 2;
        var centerY = height / 2;
        int xPos = centerX + GUI_WIDTH / 2 - 4;
        int yPos = centerY - GUI_HEIGHT / 2 + 29;

        if (menu.activeContractProgress != null && menu.boardId != menu.activeContractProgress.boardId) {
            var tooltipTextWidth = 111;
            assert minecraft != null;
            var tip = minecraft.font.split(Component.translatable("text.gui.wildernature.bounty.finish.warning"), tooltipTextWidth);
            guiGraphics.renderTooltip(minecraft.font, tip, xPos, yPos);
        }
    }

    private void renderProgressBar(GuiGraphics guiGraphics) {
        var guiX = width / 2 - GUI_WIDTH / 2;
        var guiY = height / 2 - GUI_HEIGHT / 2;
        guiGraphics.blit(TEX_BAR, guiX + 12, guiY + 11, 0f, 0f, (int) (153 * menu.progress), 5, 153, 5);
    }

    private void onAccept(Button button) {
        for (int i = 0; i < 3; i++) {
            if (contractButtons[i].getContract() == targetContractButton.getContract()) {
                RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);// TODO currently null
                buf.writeEnum(BountyBlockNetworking.BountyClientActionType.CONFIRM_CONTRACT);
                buf.writeByte(i);
                NetworkManager.sendToServer(BountyBlockNetworking.ID_SCREEN_ACTION, buf);

                contractButtons[i].setContract(null);
                contractButtons[i].active = false;
                return;
            }
        }
        throw new RuntimeException("No Contract Found");
    }


    private void setSelectedContract(Contract contract) {
        targetContractButton.setContractSelected(contract, true);
    }

    private void onReroll(Button button) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);// TODO currently null
        buf.writeEnum(BountyBlockNetworking.BountyClientActionType.REROLL);
        NetworkManager.sendToServer(BountyBlockNetworking.ID_SCREEN_ACTION, buf);
    }

    private void onDeleteContract(Button button) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(Unpooled.buffer(), null);// TODO currently null
        buf.writeEnum(BountyBlockNetworking.BountyClientActionType.DELETE_CONTRACT);
        NetworkManager.sendToServer(BountyBlockNetworking.ID_SCREEN_ACTION, buf);
    }

    private void onFinish(Button button) {
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(new UnpooledHeapByteBuf(ByteBufAllocator.DEFAULT, 0, BountyBlockNetworking.MAX_SIZE), null);// TODO currently null
        buf.writeEnum(BountyBlockNetworking.BountyClientActionType.FINISH_CONTRACT);
        NetworkManager.sendToServer(BountyBlockNetworking.ID_SCREEN_ACTION, buf);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mx, int my, float f) {
        if (menu.activeContractProgress != null) {
            targetContractButton.setContractProgress(menu.activeContract, menu.activeContractProgress);
            if (!menu.activeContractProgress.isFinished()) {
                targetContractButton.setContract(null);
            }
        }
        if (menu.activeContractProgress == null && targetContractButton.progress != null) {
            targetContractButton.setContract(null);
        }
        finishButton.visible = menu.activeContractProgress != null && menu.activeContractProgress.isFinished();
        updateTooltip();
        acceptButton.visible = menu.activeContractProgress == null && (targetContractButton != null && targetContractButton.getContract() != null);
        acceptButton.active = menu.activeContractProgress == null;
        deleteButton.visible = menu.activeContractProgress != null;
        super.render(guiGraphics, mx, my, f);
        renderTooltip(guiGraphics, mx, my);
    }

    private void updateTooltip() {
        MutableComponent comp = Component.translatable("text.gui.wildernature.bounty.reroll.left", menu.rerolls);
        if (menu.time > 0) {
            comp = comp.append("\n").append(Component.translatable("text.gui.wildernature.bounty.reroll.time", menu.time / 20 / 60, menu.time / 20 % 60));
        }
        Tooltip tooltip = Tooltip.create(comp);
        rerollButton.setTooltip(tooltip);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int i, int j) {
        guiGraphics.fill(0, 0, width, height, FastColor.ARGB32.color(64, 0, 0, 0));
        guiGraphics.blit(TEX_BACKGROUND, width / 2 - (GUI_WIDTH / 2), height / 2 - (GUI_HEIGHT / 2), 0, 0, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int i, int j) {
    }
}
