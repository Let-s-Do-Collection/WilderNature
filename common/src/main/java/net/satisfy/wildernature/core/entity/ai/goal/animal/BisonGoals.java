package net.satisfy.wildernature.core.entity.ai.goal.animal;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.satisfy.wildernature.core.entity.animal.defensive.BisonEntity;
import net.satisfy.wildernature.core.registry.SoundRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BisonGoals {
    public static class BisonPanicGoal extends PanicGoal {
        public BisonPanicGoal(BisonEntity bison) {
            super(bison, 1.2D);
        }

        @Override
        public boolean canUse() {
            return this.mob.isBaby() && super.canUse();
        }
    }

    public static class BisonHerdRunGoal extends Goal {
        private record Wave(Vec3 pos, Vec3 dir, long start, long until, double radius) {
        }

        private static final Map<ResourceKey<Level>, List<Wave>> WAVES = new HashMap<>();

        private final BisonEntity target;
        private int duration;
        private int recalc;
        private int scanCooldown;
        private Vec3 heading;

        public BisonHerdRunGoal(BisonEntity mob) {
            this.target = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.target.getTarget() != null) {
                return false;
            }
            if (this.target.isBaby()) {
                return false;
            }
            if (this.target.isAlert() || this.target.isSnorting() || this.target.isAngry()) {
                return false;
            }
            if (!this.target.level().getBiome(this.target.blockPosition()).is(BiomeTags.IS_SAVANNA)) {
                return false;
            }
            if (this.consumeWave()) {
                return true;
            }
            if (this.scanCooldown > 0) {
                this.scanCooldown--;
                return false;
            }
            this.scanCooldown = 40 + this.target.getRandom().nextInt(40);
            boolean randomTrigger = this.target.getRandom().nextFloat() < 0.04F;
            AABB boundingBox = this.target.getBoundingBox().inflate(12.0D, 4.0D, 12.0D);
            List<BisonEntity> herd = this.target.level().getEntitiesOfClass(BisonEntity.class, boundingBox, entity -> entity != this.target && !entity.isAngry());
            boolean neighborFast = false;
            for (BisonEntity bisonEntity : herd) {
                if (bisonEntity.getDeltaMovement().horizontalDistanceSqr() > 0.03D) {
                    neighborFast = true;
                    break;
                }
            }
            if (!(neighborFast || randomTrigger) && herd.size() < 3) {
                return false;
            }
            Vec3 velocity = Vec3.ZERO;
            for (BisonEntity bisonEntity : herd) {
                Vec3 deltaMovement = bisonEntity.getDeltaMovement();
                velocity = velocity.add(deltaMovement.x, 0.0D, deltaMovement.z);
            }
            if (velocity.lengthSqr() < 1.0E-3D) {
                float angle = this.target.getRandom().nextFloat() * Mth.TWO_PI;
                velocity = new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
            } else {
                velocity = velocity.normalize();
            }
            long gameTime = this.target.level().getGameTime();
            addWave(this.target.level().dimension(), new Wave(this.target.position(), velocity, gameTime + 2L, gameTime + 140L, 12.0D));
            return false;
        }

        private boolean consumeWave() {
            ResourceKey<Level> levelKey = this.target.level().dimension();
            List<Wave> waveList = WAVES.get(levelKey);
            if (waveList == null) {
                return false;
            }
            long gameTime = this.target.level().getGameTime();
            Vec3 bestDirection = null;
            double bestDistance = Double.MAX_VALUE;
            var iterator = waveList.iterator();
            while (iterator.hasNext()) {
                Wave wave = iterator.next();
                if (wave.until < gameTime) {
                    iterator.remove();
                    continue;
                }
                double distance = wave.pos.distanceToSqr(this.target.position());
                if (distance <= wave.radius * wave.radius) {
                    if (gameTime >= wave.start && distance < bestDistance) {
                        bestDistance = distance;
                        bestDirection = wave.dir;
                    }
                }
            }
            if (bestDirection != null) {
                this.heading = bestDirection;
                return true;
            }
            return false;
        }

        private static void addWave(ResourceKey<Level> key, Wave wave) {
            List<Wave> waveList = WAVES.computeIfAbsent(key, unused -> new ArrayList<>());
            waveList.add(wave);
        }

        @Override
        public void start() {
            this.duration = 80 + this.target.getRandom().nextInt(60);
            this.recalc = 0;
            this.target.setSprinting(true);
        }

        @Override
        public boolean canContinueToUse() {
            if (this.target.getTarget() != null) {
                return false;
            }
            if (this.target.isAngry()) {
                return false;
            }
            return this.duration > 0 && this.target.level().getBiome(this.target.blockPosition()).is(BiomeTags.IS_SAVANNA);
        }

        @Override
        public void tick() {
            this.duration--;
            this.recalc--;
            if (this.recalc <= 0) {
                this.recalc = 10;
                Vec3 ahead = this.target.position().add(this.heading.scale(12.0D));
                this.target.getNavigation().moveTo(ahead.x, ahead.y, ahead.z, 1.6D);
            }
        }

        @Override
        public void stop() {
            this.target.setSprinting(false);
            this.target.getNavigation().stop();
        }
    }

    public static class BisonHerdAwareStrollGoal extends Goal {
        private final BisonEntity mob;
        private final double speed;
        private final int interval;
        private final float herdRadius;
        private Vec3 wanted;

        public BisonHerdAwareStrollGoal(BisonEntity mob, double speed, int interval, float herdRadius) {
            this.mob = mob;
            this.speed = speed;
            this.interval = interval;
            this.herdRadius = herdRadius;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.mob.getTarget() != null) {
                return false;
            }
            if (this.mob.isAngry() || this.mob.isAlert() || this.mob.isSnorting() || this.mob.isSleeping() || this.mob.isGrazing()) {
                return false;
            }
            if (!this.mob.getNavigation().isDone()) {
                return false;
            }
            if (this.mob.getRandom().nextInt(this.interval) != 0) {
                return false;
            }
            AABB herdBox = this.mob.getBoundingBox().inflate(this.herdRadius, 4.0D, this.herdRadius);
            List<BisonEntity> herd = this.mob.level().getEntitiesOfClass(BisonEntity.class, herdBox, entity -> entity != this.mob && !entity.isAngry());
            Vec3 direction;
            if (herd.size() >= 2) {
                double centerX = 0.0D;
                double centerZ = 0.0D;
                for (BisonEntity bisonEntity : herd) {
                    centerX += bisonEntity.getX();
                    centerZ += bisonEntity.getZ();
                }
                centerX /= herd.size();
                centerZ /= herd.size();
                double deltaX = centerX - this.mob.getX();
                double deltaZ = centerZ - this.mob.getZ();
                double distanceSquared = deltaX * deltaX + deltaZ * deltaZ;
                if (distanceSquared > 100.0D) {
                    double distance = Math.sqrt(distanceSquared);
                    direction = new Vec3(deltaX / distance, 0.0D, deltaZ / distance);
                } else {
                    float angle = this.mob.getRandom().nextFloat() * Mth.TWO_PI;
                    Vec3 randomDirection = new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
                    double distance = Math.sqrt(distanceSquared);
                    Vec3 centerDirection = distance > 1.0E-4D ? new Vec3(deltaX / distance, 0.0D, deltaZ / distance) : randomDirection;
                    direction = randomDirection.scale(0.4D).add(centerDirection.scale(0.6D)).normalize();
                }
            } else {
                float angle = this.mob.getRandom().nextFloat() * Mth.TWO_PI;
                direction = new Vec3(Mth.cos(angle), 0.0D, Mth.sin(angle));
            }
            double travelDistance = 6.0D + this.mob.getRandom().nextInt(7);
            this.wanted = this.mob.position().add(direction.scale(travelDistance));
            return true;
        }

        @Override
        public void start() {
            this.mob.getNavigation().moveTo(this.wanted.x, this.wanted.y, this.wanted.z, this.speed);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    public static class BisonChargeGoal extends Goal {
        private final BisonEntity bison;
        @Nullable
        private Player targetPlayer;
        private int recalcTicks;

        public BisonChargeGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (!this.bison.isAngry() || !this.bison.canStartCharge()) {
                return false;
            }
            if (!(this.bison.getTarget() instanceof Player player) || !this.bison.isValidThreat(player)) {
                return false;
            }
            this.targetPlayer = player;
            return true;
        }

        @Override
        public void start() {
            this.recalcTicks = 0;
            this.bison.startCharge();
        }

        @Override
        public boolean canContinueToUse() {
            return this.targetPlayer != null && this.bison.isValidThreat(this.targetPlayer) && this.bison.isCharging() && this.bison.getChargeTicks() > 0 && this.bison.distanceToSqr(this.targetPlayer) <= BisonEntity.CHARGE_BREAK_RANGE * BisonEntity.CHARGE_BREAK_RANGE;
        }

        @Override
        public void tick() {
            if (this.targetPlayer == null) {
                System.out.println("BISON CHARGE: targetPlayer null");
                return;
            }

            this.recalcTicks--;
            this.bison.getLookControl().setLookAt(this.targetPlayer, 30.0F, 30.0F);

            if (this.recalcTicks <= 0) {
                this.recalcTicks = 4;
                System.out.println("BISON CHARGE: moveTo target");
                this.bison.getNavigation().moveTo(this.targetPlayer, BisonEntity.CHARGE_SPEED);
            }

            double distanceToTarget = this.bison.distanceToSqr(this.targetPlayer);
            System.out.println("BISON CHARGE DISTANCE: " + distanceToTarget);

            if (distanceToTarget <= 6.25D) {
                System.out.println("BISON CHARGE: attempting hit");
                boolean hitSuccessful = this.bison.doHurtTarget(this.targetPlayer);
                System.out.println("BISON CHARGE HIT RESULT: " + hitSuccessful);
                this.bison.stopCharge();
            }
        }

        @Override
        public void stop() {
            this.targetPlayer = null;
            this.bison.stopCharge();
            this.bison.setAttacking(false);
            this.bison.getNavigation().stop();
        }
    }

    public static class BisonHerdGuardGoal extends Goal {
        private final BisonEntity bison;
        @Nullable
        private Player threatPlayer;
        private int phaseTicks;
        private int phase;

        public BisonHerdGuardGoal(BisonEntity bison) {
            this.bison = bison;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (this.bison.isBaby() || this.bison.isCharging() || this.bison.getTarget() != null || this.bison.isAngry()) {
                return false;
            }
            this.threatPlayer = this.bison.findNearestThreatPlayer();
            return this.threatPlayer != null;
        }

        @Override
        public void start() {
            this.phase = 0;
            this.phaseTicks = BisonEntity.ALERT_DURATION;
            this.bison.startAlertPhase();
            this.bison.getNavigation().stop();
        }

        @Override
        public boolean canContinueToUse() {
            return this.threatPlayer != null && this.bison.isValidThreat(this.threatPlayer) && this.bison.distanceToSqr(this.threatPlayer) <= this.bison.getProtectRange() * this.bison.getProtectRange() && !this.bison.isCharging() && this.bison.getTarget() == null;
        }

        @Override
        public void tick() {
            if (this.threatPlayer == null) {
                return;
            }

            this.bison.getNavigation().stop();
            this.bison.getLookControl().setLookAt(this.threatPlayer, 30.0F, 30.0F);
            this.phaseTicks--;

            if (this.phase == 0) {
                if (this.phaseTicks <= 0) {
                    this.phase = 1;
                    this.phaseTicks = BisonEntity.SNORT_DURATION;
                    this.bison.startSnortPhase();
                    this.bison.playSound(SoundRegistry.BISON_ANGRY.get(), 1.0F, 0.9F + this.bison.getRandom().nextFloat() * 0.1F);
                }
                return;
            }

            if (this.phaseTicks <= 0) {
                this.bison.setAngry(true);
                this.bison.setTarget(this.threatPlayer);
                this.bison.setCalmDown(BisonEntity.CALM_DOWN_DURATION);
            }
        }

        @Override
        public void stop() {
            this.threatPlayer = null;
            this.phase = 0;
            this.phaseTicks = 0;
            if (!this.bison.isAngry()) {
                this.bison.stopAlertStates();
            }
        }
    }
}