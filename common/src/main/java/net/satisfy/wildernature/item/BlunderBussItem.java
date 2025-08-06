package net.satisfy.wildernature.item;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.satisfy.wildernature.entity.BulletEntity;
import net.satisfy.wildernature.registry.SoundRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class BlunderBussItem extends ProjectileWeaponItem {
    private static final int DEFAULT_BONUS_DAMAGE = 0;
    private static final double DEFAULT_DAMAGE_MULTIPLIER = 1.0;
    private static final int DEFAULT_FIRE_DELAY = 30;
    private static final double DEFAULT_INACCURACY = 2.25;
    private static final double DEFAULT_PROJECTILE_SPEED = 4.0;
    private static final int DEFAULT_DURABILITY = 128;
    private static final Ingredient DEFAULT_REPAIR_MATERIAL = Ingredient.of(Items.IRON_INGOT);
    private static final Predicate<ItemStack> BULLETS = (stack) -> stack.getItem() instanceof AmmunitionItem && ((AmmunitionItem) stack.getItem()).hasAmmo(stack);

    private final int bonusDamage;
    private final double damageMultiplier;
    private final int fireDelay;
    private final double inaccuracy;
    private final double projectileSpeed;
    private final boolean ignoreInvulnerability;
    private final Ingredient repairMaterial;

    public BlunderBussItem() {
        super(new Properties().durability(DEFAULT_DURABILITY));
        this.bonusDamage = DEFAULT_BONUS_DAMAGE;
        this.damageMultiplier = DEFAULT_DAMAGE_MULTIPLIER;
        this.fireDelay = DEFAULT_FIRE_DELAY;
        this.inaccuracy = DEFAULT_INACCURACY;
        this.projectileSpeed = DEFAULT_PROJECTILE_SPEED;
        this.ignoreInvulnerability = false;
        this.repairMaterial = DEFAULT_REPAIR_MATERIAL;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack gun = player.getItemInHand(hand);
        ItemStack ammo = player.getProjectile(gun);

        if (!ammo.isEmpty() || player.getAbilities().instabuild) {
            if (ammo.isEmpty()) {
                ammo = new ItemStack(Items.IRON_INGOT);
            }

            if (ammo.getItem() instanceof AmmunitionItem bulletItem) {
                if (!world.isClientSide) {
                    boolean bulletFree = player.getAbilities().instabuild || !shouldConsumeAmmo();

                    shoot(world, player, ammo, bulletItem);

                    gun.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    if (!bulletFree) bulletItem.consume(ammo, player);
                }

                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundRegistry.BLUNDERBUSS_SHOOT.get(), SoundSource.PLAYERS, 1.0F,
                        world.getRandom().nextFloat() * 5F + 1.0F);

                player.awardStat(Stats.ITEM_USED.get(this));
                player.getCooldowns().addCooldown(this, getFireDelay());

                if (world instanceof ServerLevel serverWorld) {
                    double particleX = player.getX() + player.getLookAngle().x * 2.5;
                    double particleY = player.getY() + player.getEyeHeight() - 0.3;
                    double particleZ = player.getZ() + player.getLookAngle().z * 1.5;

                    serverWorld.sendParticles(ParticleTypes.SMALL_FLAME, particleX, particleY, particleZ, 5, 0.1, 0.1, 0.1, 0.01);
                    serverWorld.sendParticles(ParticleTypes.SMOKE, particleX, particleY, particleZ, 10, 0.1, 0.1, 0.1, 0.01);
                    serverWorld.getServer().tell(new TickTask(serverWorld.getServer().getTickCount() + 40,
                            () -> world.playSound(null, player.getX(), player.getY(), player.getZ(),
                                    SoundRegistry.BLUNDERBUSS_LOAD.get(), SoundSource.PLAYERS, 0.5F, 1.0F)));
                }

                return InteractionResultHolder.consume(gun);
            }
        } else {
            return InteractionResultHolder.fail(gun);
        }
        return InteractionResultHolder.pass(gun);
    }

    protected void shoot(Level world, Player player, ItemStack ammo, AmmunitionItem bulletItem) {
        if (bulletItem instanceof FlintAmmunitionItem) {
            int pelletCount = 4 + world.getRandom().nextInt(5);
            for (int i = 0; i < pelletCount; i++) {
                BulletEntity pellet = bulletItem.createProjectile(world, ammo.copy(), player);

                float spread = (float) (getInaccuracy() * 3.5);
                pellet.shootFromRotation(player, player.getXRot(), player.getYRot(),
                        0, (float) getProjectileSpeed() * 0.6f, spread);

                double distScale = Math.max(0.2, 1.0 - (player.distanceTo(pellet) / 12.0));
                pellet.setDamage(((bulletItem.getDamage() + 3) * getDamageMultiplier()) * distScale);

                pellet.setIgnoreInvulnerability(ignoreInvulnerability);
                pellet.setLifeTicks(12 + world.getRandom().nextInt(4));
                world.addFreshEntity(pellet);
            }
        } else {
            BulletEntity shot = bulletItem.createProjectile(world, ammo, player);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0, (float) getProjectileSpeed(), (float) getInaccuracy());
            shot.setDamage((shot.getDamage() + getBonusDamage()) * getDamageMultiplier());
            shot.setIgnoreInvulnerability(ignoreInvulnerability);
            world.addFreshEntity(shot);
        }

        if (world instanceof ServerLevel serverWorld) {
            double particleX = player.getX() + player.getLookAngle().x * 0.5;
            double particleY = player.getY() + player.getEyeHeight() - 0.1;
            double particleZ = player.getZ() + player.getLookAngle().z * 0.5;
            serverWorld.sendParticles(ParticleTypes.SMALL_FLAME, particleX, particleY, particleZ, 5, 0.1, 0.1, 0.1, 0.01);
            serverWorld.sendParticles(ParticleTypes.SMOKE, particleX, particleY, particleZ, 10, 0.1, 0.1, 0.1, 0.01);
        }
    }

    public boolean shouldConsumeAmmo() {
        return true;
    }

    public double getBonusDamage() {
        return bonusDamage;
    }

    public double getDamageMultiplier() {
        return damageMultiplier;
    }

    public int getFireDelay() {
        return fireDelay;
    }

    public double getInaccuracy() {
        return inaccuracy;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return BULLETS;
    }

    @Override
    public int getDefaultProjectileRange() {
        return 15;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repairMaterial.test(repair) || super.isValidRepairItem(toRepair, repair);
    }
}
