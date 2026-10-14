package net.satisfy.wildernature.platform.neoforge;

import net.satisfy.wildernature.neoforge.core.config.WilderNatureNeoForgeConfig;

public class PlatformHelperImpl {
    public static boolean isTermiteLogEatingEnabled() {
        return WilderNatureNeoForgeConfig.enableTermiteLogEating;
    }

    public static boolean isBeaverDamsEnabled() {
        return WilderNatureNeoForgeConfig.enableBeaverDams;
    }

    public static boolean isRaccoonStealingEnabled() {
        return WilderNatureNeoForgeConfig.enableRaccoonStealing;
    }

    public static boolean isRaccoonDoorsEnabled() {
        return WilderNatureNeoForgeConfig.enableRaccoonDoors;
    }

    public static boolean isBoarRootingEnabled() {
        return WilderNatureNeoForgeConfig.enableBoarRooting;
    }

    public static boolean isDogDiggingEnabled() {
        return WilderNatureNeoForgeConfig.enableDogDigging;
    }

    public static boolean isSquirrelGiftsEnabled() {
        return WilderNatureNeoForgeConfig.enableSquirrelGifts;
    }

    public static boolean isBisonChargeEnabled() {
        return WilderNatureNeoForgeConfig.enableBisonCharge;
    }

    public static boolean isBountyBoardEnabled() {
        return WilderNatureNeoForgeConfig.enableBountyBoard;
    }

    public static int getBountyMinDaily() {
        return WilderNatureNeoForgeConfig.bountyMinDaily;
    }

    public static int getBountyMaxDaily() {
        return WilderNatureNeoForgeConfig.bountyMaxDaily;
    }

    public static boolean isEmoteParticlesEnabled() {
        return WilderNatureNeoForgeConfig.enableEmoteParticles;
    }

    public static boolean isAnimationsEnabled() {
        return WilderNatureNeoForgeConfig.animations;
    }

    public static int getTermiteMoundRarity() {
        return WilderNatureNeoForgeConfig.termiteMoundRarity;
    }

    public static int getHollowCacheRarity() {
        return WilderNatureNeoForgeConfig.hollowCacheRarity;
    }

    public static double getBountyExperienceMultiplier() {
        return WilderNatureNeoForgeConfig.bountyExperienceMultiplier;
    }
}
