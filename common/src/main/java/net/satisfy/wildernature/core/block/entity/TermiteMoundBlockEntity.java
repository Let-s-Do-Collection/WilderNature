package net.satisfy.wildernature.core.block.entity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.satisfy.wildernature.core.entity.animal.passive.TermiteEntity;
import net.satisfy.wildernature.core.registry.EntityTypeRegistry;
import net.satisfy.wildernature.core.registry.ObjectRegistry;
import org.jetbrains.annotations.Nullable;

public class TermiteMoundBlockEntity extends BlockEntity {
    public static final int MAX_CONNECTED_MOUND_BLOCKS = 5;
    public static final int TERMITES_PER_MOUND_BLOCK = 2;
    public static final int MAX_TOTAL_TERMITES = 12;
    public static final int MIN_TERMITES_FOR_REPRODUCTION = 3;
    public static final int REPRODUCTION_CHECK_INTERVAL = 1200;
    public static final float REPRODUCTION_CHANCE = 0.10F;
    public static final int MAX_ACTIVE_INFESTATIONS = 2;
    public static final int MIN_RELEASE_COOLDOWN = 80;
    public static final int MAX_RELEASE_COOLDOWN = 180;
    public static final double ACTIVE_TERMITE_RADIUS = 16.0D;

    private int storedTermites;
    private int releaseCooldown;
    private int reproductionCooldown;
    private int nextInfestationId = 1;
    private boolean initialized;
    private final Set<Integer> activeInfestationIds = new HashSet<>();

    public TermiteMoundBlockEntity(BlockPos pos, BlockState state) {
        super(EntityTypeRegistry.TERMITE_MOUND_BLOCK_ENTITY.get(), pos, state);
        this.reproductionCooldown = REPRODUCTION_CHECK_INTERVAL;
    }

    public static void tick(ServerLevel level, BlockPos pos, TermiteMoundBlockEntity blockEntity) {
        BlockPos entrancePos = findEntrancePos(level, pos);
        if (!pos.equals(entrancePos)) {
            return;
        }

        int moundBlockCount = countConnectedMoundBlocks(level, entrancePos);
        int capacity = getCapacity(moundBlockCount);

        if (!blockEntity.initialized) {
            blockEntity.initialized = true;
            blockEntity.storedTermites = capacity;
            blockEntity.releaseCooldown = MIN_RELEASE_COOLDOWN;
            blockEntity.reproductionCooldown = REPRODUCTION_CHECK_INTERVAL;
            blockEntity.setChanged();
        }

        if (blockEntity.storedTermites > capacity) {
            blockEntity.storedTermites = capacity;
            blockEntity.setChanged();
        }

        if (blockEntity.reproductionCooldown > 0) {
            blockEntity.reproductionCooldown--;
        } else {
            blockEntity.reproductionCooldown = REPRODUCTION_CHECK_INTERVAL;
            if (blockEntity.storedTermites >= MIN_TERMITES_FOR_REPRODUCTION && blockEntity.storedTermites < capacity && level.getRandom().nextFloat() < REPRODUCTION_CHANCE) {
                blockEntity.storedTermites++;
                blockEntity.setChanged();
            }
        }

        if (blockEntity.releaseCooldown > 0) {
            blockEntity.releaseCooldown--;
            blockEntity.setChanged();
            return;
        }

        int activeOutsideCount = countNearbyAssignedTermites(level, entrancePos);
        int desiredOutsideCount = Math.min(moundBlockCount, Math.max(1, capacity / 2));

        if (blockEntity.storedTermites <= 0 || activeOutsideCount >= desiredOutsideCount) {
            blockEntity.releaseCooldown = getRandomCooldown(level);
            blockEntity.setChanged();
            return;
        }

        if (releaseTermite(level, entrancePos)) {
            blockEntity.storedTermites--;
            blockEntity.releaseCooldown = getRandomCooldown(level);
            blockEntity.setChanged();
        }
    }

    public static boolean tryStoreTermite(ServerLevel level, BlockPos moundPos) {
        TermiteMoundBlockEntity blockEntity = getEntranceBlockEntity(level, moundPos);
        if (blockEntity == null) return false;

        int moundBlockCount = countConnectedMoundBlocks(level, blockEntity.getBlockPos());
        int capacity = getCapacity(moundBlockCount);

        if (!blockEntity.initialized) blockEntity.initialized = true;
        if (blockEntity.storedTermites >= capacity) return false;

        blockEntity.storedTermites++;
        blockEntity.releaseCooldown = getRandomCooldown(level);
        blockEntity.setChanged();

        double x = moundPos.getX() + 0.5D;
        double y = moundPos.getY() + 0.3D;
        double z = moundPos.getZ() + 0.5D;

        level.playSound(null, moundPos, net.minecraft.sounds.SoundEvents.BEEHIVE_ENTER, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.35F + level.getRandom().nextFloat() * 0.1F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, x, y, z, 6, 0.2D, 0.1D, 0.2D, 0.02D);

        return true;
    }

    public static int requestInfestationId(ServerLevel level, BlockPos moundPos) {
        TermiteMoundBlockEntity blockEntity = getEntranceBlockEntity(level, moundPos);
        if (blockEntity == null) {
            return -1;
        }

        if (blockEntity.activeInfestationIds.size() >= MAX_ACTIVE_INFESTATIONS) {
            return -1;
        }

        int infestationId = blockEntity.nextInfestationId++;
        blockEntity.activeInfestationIds.add(infestationId);
        blockEntity.setChanged();
        return infestationId;
    }

    public static void releaseInfestationId(ServerLevel level, BlockPos moundPos, int infestationId) {
        if (infestationId <= 0) {
            return;
        }

        TermiteMoundBlockEntity blockEntity = getEntranceBlockEntity(level, moundPos);
        if (blockEntity == null) {
            return;
        }

        if (blockEntity.activeInfestationIds.remove(infestationId)) {
            blockEntity.setChanged();
        }
    }

    @Nullable
    private static TermiteMoundBlockEntity getEntranceBlockEntity(Level level, BlockPos moundPos) {
        BlockPos entrancePos = findEntrancePos(level, moundPos);
        if (!(level.getBlockEntity(entrancePos) instanceof TermiteMoundBlockEntity termiteMoundBlockEntity)) {
            return null;
        }
        return termiteMoundBlockEntity;
    }

    public static BlockPos findEntrancePos(Level level, BlockPos originPos) {
        Set<BlockPos> connectedPositions = scanConnectedMoundPositions(level, originPos);
        BlockPos entrancePos = originPos;

        for (BlockPos currentPos : connectedPositions) {
            if (currentPos.getY() < entrancePos.getY()) {
                entrancePos = currentPos;
                continue;
            }

            if (currentPos.getY() == entrancePos.getY()) {
                if (currentPos.getX() < entrancePos.getX()) {
                    entrancePos = currentPos;
                    continue;
                }

                if (currentPos.getX() == entrancePos.getX() && currentPos.getZ() < entrancePos.getZ()) {
                    entrancePos = currentPos;
                }
            }
        }

        return entrancePos;
    }

    public static int countConnectedMoundBlocks(Level level, BlockPos originPos) {
        return scanConnectedMoundPositions(level, originPos).size();
    }

    private static Set<BlockPos> scanConnectedMoundPositions(Level level, BlockPos originPos) {
        Set<BlockPos> visitedPositions = new HashSet<>();
        ArrayDeque<BlockPos> pendingPositions = new ArrayDeque<>();
        pendingPositions.add(originPos);

        while (!pendingPositions.isEmpty() && visitedPositions.size() < MAX_CONNECTED_MOUND_BLOCKS) {
            BlockPos currentPos = pendingPositions.removeFirst();
            if (!visitedPositions.add(currentPos)) {
                continue;
            }

            if (!level.getBlockState(currentPos).is(ObjectRegistry.TERMITE_MOUND.get())) {
                visitedPositions.remove(currentPos);
                continue;
            }

            pendingPositions.addLast(currentPos.above());
            pendingPositions.addLast(currentPos.below());
            pendingPositions.addLast(currentPos.north());
            pendingPositions.addLast(currentPos.south());
            pendingPositions.addLast(currentPos.east());
            pendingPositions.addLast(currentPos.west());
        }

        return visitedPositions;
    }

    private static int countNearbyAssignedTermites(ServerLevel level, BlockPos entrancePos) {
        return level.getEntitiesOfClass(TermiteEntity.class, new AABB(entrancePos).inflate(ACTIVE_TERMITE_RADIUS), termite -> termite.isAlive() && entrancePos.equals(termite.getMoundPos())).size();
    }

    private static boolean releaseTermite(ServerLevel level, BlockPos entrancePos) {
        TermiteEntity termite = EntityTypeRegistry.TERMITE.get().create(level);
        if (termite == null) {
            return false;
        }

        BlockPos exitPos = findExitPos(level, entrancePos);
        double spawnX = exitPos.getX() + 0.5D;
        double spawnY = exitPos.getY() + 0.1D;
        double spawnZ = exitPos.getZ() + 0.5D;

        termite.moveTo(spawnX, spawnY, spawnZ, level.getRandom().nextFloat() * 360.0F, 0.0F);
        termite.setMoundPos(entrancePos);
        termite.setReturningToMound(false);
        termite.finalizeSpawn(level, level.getCurrentDifficultyAt(exitPos), MobSpawnType.SPAWNER, null);

        level.playSound(null, entrancePos, net.minecraft.sounds.SoundEvents.BEEHIVE_EXIT, net.minecraft.sounds.SoundSource.BLOCKS, 0.9F, 0.35F + level.getRandom().nextFloat() * 0.1F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, spawnX, spawnY + 0.2D, spawnZ, 6, 0.2D, 0.1D, 0.2D, 0.02D);

        return level.addFreshEntity(termite);
    }

    private static BlockPos findExitPos(ServerLevel level, BlockPos entrancePos) {
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = entrancePos.relative(direction);
            if (level.getBlockState(sidePos).isAir() && level.getBlockState(sidePos.above()).isAir()) {
                return sidePos;
            }
        }

        BlockPos abovePos = entrancePos.above();
        if (level.getBlockState(abovePos).isAir() && level.getBlockState(abovePos.above()).isAir()) {
            return abovePos;
        }

        return abovePos;
    }

    private static int getCapacity(int moundBlockCount) {
        return Math.min(moundBlockCount * TERMITES_PER_MOUND_BLOCK, MAX_TOTAL_TERMITES);
    }

    private static int getRandomCooldown(ServerLevel level) {
        return MIN_RELEASE_COOLDOWN + level.getRandom().nextInt(MAX_RELEASE_COOLDOWN - MIN_RELEASE_COOLDOWN + 1);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("StoredTermites", this.storedTermites);
        tag.putInt("ReleaseCooldown", this.releaseCooldown);
        tag.putInt("ReproductionCooldown", this.reproductionCooldown);
        tag.putInt("NextInfestationId", this.nextInfestationId);
        tag.putBoolean("Initialized", this.initialized);
        tag.put("ActiveInfestationIds", new IntArrayTag(this.activeInfestationIds.stream().mapToInt(Integer::intValue).toArray()));
    }

    @Override
    public void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.storedTermites = tag.getInt("StoredTermites");
        this.releaseCooldown = tag.getInt("ReleaseCooldown");
        this.reproductionCooldown = tag.contains("ReproductionCooldown") ? tag.getInt("ReproductionCooldown") : REPRODUCTION_CHECK_INTERVAL;
        this.nextInfestationId = Math.max(1, tag.getInt("NextInfestationId"));
        this.initialized = tag.getBoolean("Initialized");
        this.activeInfestationIds.clear();
        for (int infestationId : tag.getIntArray("ActiveInfestationIds")) {
            this.activeInfestationIds.add(infestationId);
        }
    }
}