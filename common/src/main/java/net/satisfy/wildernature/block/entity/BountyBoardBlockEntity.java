package net.satisfy.wildernature.block.entity;

import dev.architectury.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.satisfy.wildernature.client.gui.handlers.BountyBlockScreenHandler;
import net.satisfy.wildernature.event.EventManager;
import net.satisfy.wildernature.registry.EntityRegistry;
import net.satisfy.wildernature.util.BountyBoardTier;
import net.satisfy.wildernature.util.WilderNatureIdentifier;
import net.satisfy.wildernature.util.contract.Contract;
import net.satisfy.wildernature.util.contract.ContractReloader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Random;

public class BountyBoardBlockEntity extends BlockEntity implements MenuProvider {
    private static final String KEY_CONTRACTS = "contracts";
    private static final int REROLL_COOLDOWN = 15 * 60 * 20;
    public int rerollCooldownLeft = 0;
    private static final String KEY_REROLL_COOLDOWN_LEFT = "reroll_cooldown_left";

    public static final int MAX_REROLLS = 3;
    public int rerollsLeft;
    private static final String KEY_REROLLS_LEFT = "rerolls_left";
    private static final String KEY_TIER = "tier";
    private static final String KEY_EXP = "experience";
    private static final String KEY_LONGID = "longid";

    public EventManager onTick = new EventManager();
    public EventManager onBlockDataChange = new EventManager();
    public long boardId = new Random().nextInt();
    private ResourceLocation[] contracts = new ResourceLocation[3];
    public ResourceLocation tier = WilderNatureIdentifier.of("tier1");
    public int xp = 0;

    public ResourceLocation[] getContracts() {
        if (contracts == null || contracts.length != 3) {
            contracts = new ResourceLocation[3];
            fillWithRandomContracts();
        }
        for (int i = 0; i < 3; i++) {
            if (contracts[i] == null) {
                contracts[i] = getRandomContract();
            }
        }
        return contracts.clone();
    }

    public void setContracts(ResourceLocation[] contracts) {
        this.contracts = contracts.clone();
    }

    public BountyBoardBlockEntity(BlockPos pos, BlockState state) {
        super(EntityRegistry.BOUNTY_BOARD_ENTITY.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        this.rerollCooldownLeft = compoundTag.contains(KEY_REROLL_COOLDOWN_LEFT) ? compoundTag.getInt(KEY_REROLL_COOLDOWN_LEFT) : 0;
        this.rerollsLeft = compoundTag.contains(KEY_REROLLS_LEFT) ? compoundTag.getInt(KEY_REROLLS_LEFT) : MAX_REROLLS;
        this.boardId = compoundTag.contains(KEY_LONGID) ? compoundTag.getLong(KEY_LONGID) : new Random().nextInt();
        this.tier = compoundTag.contains(KEY_TIER) ? ResourceLocation.parse(compoundTag.getString(KEY_TIER)) : WilderNatureIdentifier.of("");
        this.xp = compoundTag.contains(KEY_EXP) ? compoundTag.getInt(KEY_EXP) : 0;
        if (compoundTag.contains(KEY_CONTRACTS)) {
            setContracts(ResourceLocation.CODEC.listOf()
                    .parse(NbtOps.INSTANCE, compoundTag.get(KEY_CONTRACTS))
                    .getOrThrow(error -> { throw new RuntimeException(error); })
                    .toArray(new ResourceLocation[3]));
        } else {
            fillWithRandomContracts();
        }
    }

    public void tryReroll() {
        if (rerollsLeft <= 0) {
            return;
        }
        if (rerollsLeft == MAX_REROLLS) {
            rerollCooldownLeft = REROLL_COOLDOWN;
        }
        rerollsLeft--;
        fillWithRandomContracts();
        onBlockDataChange.invoke();
    }

    private void fillWithRandomContracts() {
        for (int i = 0; i < 3; i++) {
            setRandomContractInSlot(i);
        }
    }

    public void setRandomContractInSlot(int i) {
        var contracts = getContracts();
        contracts[i] = getRandomContract();
        setContracts(contracts);
    }

    private ResourceLocation getRandomContract() {
        return ContractReloader.getRandomContractOfTier(this.tier);
    }

    @Override
    public void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        compoundTag.putInt(KEY_REROLL_COOLDOWN_LEFT, rerollCooldownLeft);
        compoundTag.putInt(KEY_REROLLS_LEFT, rerollsLeft);
        compoundTag.putLong(KEY_LONGID, boardId);
        compoundTag.putString(KEY_TIER, tier.toString());
        compoundTag.putInt(KEY_EXP, xp);
        compoundTag.put(
                KEY_CONTRACTS,
                ResourceLocation.CODEC.listOf()
                        .encode(Arrays.stream(getContracts()).toList(), NbtOps.INSTANCE, new ListTag())
                        .getOrThrow(error -> { throw new RuntimeException(error); })
        );
    }

    public CompoundTag getContractsNbt() {
        var encodedContracts = Contract.CODEC.listOf()
                .encode(Arrays.stream(getContracts()).map(Contract::fromId).toList(), NbtOps.INSTANCE, new ListTag())
                .getOrThrow(error -> { throw new RuntimeException(error); });

        var tag = new CompoundTag();
        tag.put("list", encodedContracts);
        return tag;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return BountyBlockScreenHandler.createServer(id, inventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BountyBoardBlockEntity entity) {
        entity.serverTick(level, pos, state);
    }

    @SuppressWarnings("unused")
    private void serverTick(Level level, BlockPos pos, BlockState state) {
        rerollCooldownLeft--;
        if (rerollCooldownLeft < 0) {
            rerollsLeft = MAX_REROLLS;
        }
        onTick.invoke();
    }

    public BountyBoardTier getTier() {
        return BountyBoardTier.byId(this.tier)
                .orElseThrow(() -> new RuntimeException("BountyBoardTier not found for tier: " + this.tier));
    }

    public void addXp(int additionalXp) {
        var nextTierXp = getTier().experience();
        this.xp += additionalXp;
        if (this.xp >= nextTierXp) {
            additionalXp = xp - nextTierXp;
            this.xp = 0;

            if (getTier().nextTier().isEmpty()) {
                if (Platform.isDevelopmentEnvironment()) {
                    Objects.requireNonNull(Objects.requireNonNull(this.getLevel()).getServer()).getPlayerList().broadcastSystemMessage(
                            Component.literal("_info: next tier is empty, impossible to upgrade"), true);
                }
                return;
            }

            this.tier = getTier().nextTier().get();
            onBlockDataChange.invoke();
            addXp(additionalXp);
        }
    }
}
