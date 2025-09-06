package net.satisfy.wildernature.core.block;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.satisfy.wildernature.core.entity.TermiteEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;

public class TermiteBlock extends Block {
    public TermiteBlock(Block base, Properties properties) {
        super(properties.destroyTime(base.defaultDestroyTime() / 2.0F).explosionResistance(0.75F).randomTicks());
    }

    private TermiteEntity spawnInfestation(ServerLevel level, BlockPos pos) {
        TermiteEntity e = new TermiteEntity(EntityTypeRegistry.TERMITE.get(), level);
        e.setHome(pos);
        e.moveTo(pos.getX() + 0.4, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.addFreshEntity(e);
        e.spawnAnim();
        return e;
    }

    private int activeCount(Level level, BlockPos pos) {
        AABB box = new AABB(pos).inflate(6.0);
        int c = 0;
        for (TermiteEntity e : level.getEntitiesOfClass(TermiteEntity.class, box)) {
            if (pos.equals(e.getHome())) {
                c++;
                if (c >= 2) break;
            }
        }
        return c;
    }

    private TermiteEntity findNearestHomeTermite(Level level, BlockPos moundPos) {
        AABB box = new AABB(moundPos).inflate(6.0);
        List<TermiteEntity> list = level.getEntitiesOfClass(TermiteEntity.class, box);
        for (TermiteEntity e : list) if (moundPos.equals(e.getHome())) return e;
        return null;
    }

    public static void triggerFromLog(ServerLevel level, BlockPos logPos) {
        BlockPos.betweenClosedStream(logPos.offset(-6, -6, -6), logPos.offset(6, 6, 6)).forEach(p -> {
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof TermiteBlock b) {
                TermiteEntity e = b.findNearestHomeTermite(level, p);
                if (e == null && b.activeCount(level, p) < 2) e = b.spawnInfestation(level, p);
                if (e != null) e.forceTarget(logPos, 200);
            }
        });
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (activeCount(level, pos) >= 2) return;
        if (level.isDay()) return;
        if (level.isRaining()) return;
        if (random.nextInt(200) == 0) spawnInfestation(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.isClientSide && level.getGameTime() % 8L == 0L) {
            if (activeCount(level, pos) >= 2) {
                BlockParticleOption dust = new BlockParticleOption(ParticleTypes.FALLING_DUST, state);
                level.addAlwaysVisibleParticle(
                        dust,
                        pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
                        pos.getY() + 0.9,
                        pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.6,
                        0.0, 0.02, 0.0
                );
            }
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level instanceof ServerLevel server) {
            if (fromPos.closerThan(pos, 7.0)) {
                RegistryAccess access = server.registryAccess();
                HolderSet.Named<Block> logs = access.registryOrThrow(Registries.BLOCK).getTag(BlockTags.LOGS).orElse(null);
                if (logs != null && logs.contains(server.getBlockState(fromPos).getBlockHolder())) {
                    TermiteEntity e = findNearestHomeTermite(server, pos);
                    if (e == null && activeCount(server, pos) < 2) e = spawnInfestation(server, pos);
                    if (e != null) e.forceTarget(fromPos, 200);
                }
            }
        }
    }

    @Override
    public void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.world.item.ItemStack stack, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience);
        if (level.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOBLOCKDROPS) && net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), stack) == 0) {
            if (level.random.nextFloat() < 0.5F) {
                int numTermites = level.random.nextInt(2) + 3;
                for (int i = 0; i < numTermites; i++) spawnInfestation(level, pos);
            }
        }
    }
}
