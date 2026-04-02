package net.satisfy.wildernature.core.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.core.block.HollowCacheBlock;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import org.jetbrains.annotations.NotNull;

public class HollowCacheBlockEntity extends RandomizableContainerBlockEntity {
    private NonNullList<ItemStack> inventory;
    private final ContainerOpenersCounter stateManager;
    private float openProgress;
    private float previousOpenProgress;
    private int pendingCloseParticlesTicks;

    public HollowCacheBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.HOLLOW_CACHE_BLOCK_ENTITY.get(), pos, state);
        this.inventory = NonNullList.withSize(18, ItemStack.EMPTY);
        this.stateManager = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level level, BlockPos pos, BlockState state) {
                HollowCacheBlock.setOpen(state, level, pos, true);
                level.playSound(null, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
            }

            @Override
            protected void onClose(Level level, BlockPos pos, BlockState state) {
                HollowCacheBlock.setOpen(state, level, pos, false);
                level.playSound(null, pos, SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.2F);
                HollowCacheBlockEntity.this.scheduleCloseParticles();
            }

            @Override
            protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            }

            @Override
            protected boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof ChestMenu chestMenu) {
                    Container container = chestMenu.getContainer();
                    return container == HollowCacheBlockEntity.this;
                }
                return false;
            }
        };
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, HollowCacheBlockEntity blockEntity) {
        blockEntity.previousOpenProgress = blockEntity.openProgress;
        float targetProgress = state.getValue(HollowCacheBlock.OPEN) ? 1.0F : 0.0F;
        if (blockEntity.openProgress < targetProgress) {
            blockEntity.openProgress = Math.min(1.0F, blockEntity.openProgress + 0.12F);
        } else if (blockEntity.openProgress > targetProgress) {
            blockEntity.openProgress = Math.max(0.0F, blockEntity.openProgress - 0.12F);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HollowCacheBlockEntity blockEntity) {
        if (blockEntity.pendingCloseParticlesTicks > 0) {
            blockEntity.pendingCloseParticlesTicks--;
            if (blockEntity.pendingCloseParticlesTicks == 0 && level instanceof ServerLevel serverLevel) {
                HollowCacheBlock.spawnCacheCloseParticles(serverLevel, pos);
            }
        }
    }

    public float getOpenProgress(float partialTick) {
        return this.previousOpenProgress + (this.openProgress - this.previousOpenProgress) * partialTick;
    }

    public void scheduleCloseParticles() {
        this.pendingCloseParticlesTicks = 20;
        this.setChanged();
    }

    public boolean hasFreeSlot() {
        for (ItemStack itemStack : this.inventory) {
            if (itemStack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public boolean tryAddItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        }

        for (int slotIndex = 0; slotIndex < this.inventory.size(); slotIndex++) {
            ItemStack existingStack = this.inventory.get(slotIndex);
            if (existingStack.isEmpty()) {
                this.inventory.set(slotIndex, itemStack.copy());
                this.setChanged();
                return true;
            }

            if (ItemStack.isSameItemSameComponents(existingStack, itemStack) && existingStack.getCount() < existingStack.getMaxStackSize()) {
                int transferableCount = Math.min(itemStack.getCount(), existingStack.getMaxStackSize() - existingStack.getCount());
                if (transferableCount > 0) {
                    existingStack.grow(transferableCount);
                    this.setChanged();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        if (!this.trySaveLootTable(compoundTag)) {
            ContainerHelper.saveAllItems(compoundTag, this.inventory, provider);
        }
        compoundTag.putInt("PendingCloseParticlesTicks", this.pendingCloseParticlesTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        this.inventory = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(compoundTag)) {
            ContainerHelper.loadAllItems(compoundTag, this.inventory, provider);
        }
        this.pendingCloseParticlesTicks = compoundTag.getInt("PendingCloseParticlesTicks");
    }

    @Override
    public int getContainerSize() {
        return 18;
    }

    @Override
    protected @NotNull NonNullList<ItemStack> getItems() {
        return this.inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> list) {
        this.inventory = list;
    }

    public boolean hasItem(Item item) {
        for (ItemStack itemStack : this.inventory) {
            if (!itemStack.isEmpty() && itemStack.is(item)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack extractItem(Item item, int count) {
        for (int slotIndex = 0; slotIndex < this.inventory.size(); slotIndex++) {
            ItemStack itemStack = this.inventory.get(slotIndex);
            if (!itemStack.isEmpty() && itemStack.is(item)) {
                int extractedCount = Math.min(count, itemStack.getCount());
                ItemStack extractedStack = itemStack.copyWithCount(extractedCount);
                itemStack.shrink(extractedCount);
                if (itemStack.isEmpty()) {
                    this.inventory.set(slotIndex, ItemStack.EMPTY);
                }
                this.setChanged();
                return extractedStack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    protected @NotNull Component getDefaultName() {
        return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
    }

    @Override
    protected @NotNull AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
        return new ChestMenu(MenuType.GENERIC_9x2, syncId, playerInventory, this, 2);
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.stateManager.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.stateManager.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }
}