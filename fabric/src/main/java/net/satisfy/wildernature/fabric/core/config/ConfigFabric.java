package net.satisfy.wildernature.fabric.core.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import net.satisfy.wildernature.WilderNature;

@Config(name = WilderNature.MOD_ID)
public class ConfigFabric implements ConfigData {
    public boolean removeSavannaAnimals = true;
    public boolean removeSwampAnimals = true;
    public boolean removeJungleAnimals = true;
    public boolean removeForestAnimals = true;
    public boolean addJungleAnimals = true;
    public boolean spawnHazelnutBush = true;

    public int GiraffeSpawnWeight = 13;
    public int GiraffeMinGroupSize = 2;
    public int GiraffeMaxGroupSize = 4;
    public int DeerSpawnWeight = 13;
    public int DeerMinGroupSize = 2;
    public int DeerMaxGroupSize = 4;
    public int RaccoonSpawnWeight = 12;
    public int RaccoonMinGroupSize = 2;
    public int RaccoonMaxGroupSize = 3;
    public int SquirrelSpawnWeight = 10;
    public int SquirrelMinGroupSize = 2;
    public int SquirrelMaxGroupSize = 2;
    public int SwiftfoxSpawnWeight = 12;
    public int SwiftfoxMinGroupSize = 2;
    public int SwiftfoxMaxGroupSize = 4;
    public int OwlSpawnWeight = 12;
    public int OwlMinGroupSize = 2;
    public int OwlMaxGroupSize = 3;
    public int BoarSpawnWeight = 14;
    public int BoarMinGroupSize = 4;
    public int BoarMaxGroupSize = 5;
    public int BisonSpawnWeight = 14;
    public int BisonMinGroupSize = 3;
    public int BisonMaxGroupSize = 6;
    public int TurkeySpawnWeight = 12;
    public int TurkeyMinGroupSize = 3;
    public int TurkeyMaxGroupSize = 5;
    public int DogSpawnWeight = 5;
    public int DogMinGroupSize = 1;
    public int DogMaxGroupSize = 1;
    public int MiniSheepSpawnWeight = 13;
    public int MiniSheepMinGroupSize = 3;
    public int MiniSheepMaxGroupSize = 6;
    public int CassowarySpawnWeight = 7;
    public int CassowaryMinGroupSize = 3;
    public int CassowaryMaxGroupSize = 5;
    public int HedgehogSpawnWeight = 13;
    public int HedgehogMinGroupSize = 1;
    public int HedgehogMaxGroupSize = 3;
    public int HippoSpawnWeight = 14;
    public int HippoMinGroupSize = 3;
    public int HippoMaxGroupSize = 6;
    public int ElephantSpawnWeight = 10;
    public int ElephantMinGroupSize = 3;
    public int ElephantMaxGroupSize = 5;
    public int BeaverSpawnWeight = 11;
    public int BeaverMinGroupSize = 2;
    public int BeaverMaxGroupSize = 3;
}