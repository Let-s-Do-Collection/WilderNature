[1.1.5]

This update makes WilderNature entities feel more alive, adding distinct behaviors, daily routines, sleep cycles, and interactions with the world around them.

**Fixed**
* Idle animations for most entities are now much smoother and more natural
* Hazelnut Bushes no longer turn into Sweetberry Bushes when bone mealed

**Changed**
* Recolored Hazelnut Bushes
* Recolored Fish Oil
* Turkeys now lay Turkey Eggs instead of regular Eggs
* RedWolf has been renamed to SwiftFox

**SwiftFox**
* Added player item stealing behavior with sneak and escape logic
* SwiftFoxes can steal items directly from player hands
* Added ground item stealing behavior
* SwiftFoxes can hide stolen items in the world
* Hidden items are remembered and can be retrieved later
* Added return behavior for trusted players
* SwiftFoxes can return stolen items when trust is gained
* Added gift system for trusted players
* SwiftFoxes can deliver items as gifts
* Added trust system influencing behavior towards players
* SwiftFoxes avoid untrusted players and react dynamically
* Added sneak, attack, idle and sleep animation states
* SwiftFoxes now sleep when idle for longer periods
* Added sleeping particles above the head
* Improved interaction flow between stealing, hiding and returning items

**Owl Rework**
* Completely overhauled owl behavior and AI
* Owls now look for natural perches before going to sleep
* Added sleep preparation with randomized timing
* Improved wake up logic with better threat detection
* Refined flying and landing to properly align with perches
* Owls now hunt at night with randomized hunting windows
* They actively target undead mobs during hunts
* Owls can now find and eat dropped rotten flesh

**Deer Rework**
* Added awareness system reacting to player movement, sprinting and held items
* Added panic system with herd wide reactions
* Improved herd behavior with leader following and persistence
* Added sleeping behavior with preparation and wake up triggers
* Deer prefer sleeping on grass blocks
* Deer seek shelter during the night
* Deer correctly ignore Creative and Spectator players

**Boar Rework**
* Reworked rooting behavior with smarter targeting
* Boars search for valid grass blocks before digging
* Added preparation phase before rooting
* Truffles can encourage rooting with a small chance
* Rooting now uses a loot table system
* Added cooldowns to prevent excessive rooting
* Boars can eat from caches when injured
* Rooting works on Dirt and Coarse Dirt with reduced loot chance
* Respects mobGriefing gamerule

**Raccoon Rework**
* Raccoons can loot containers and store items in caches
* Added consistent inventory handling
* Raccoons deposit items into caches
* Chance to generate additional loot bags when storing
* Added crop nibbling at night without destroying crops
* Respects mobGriefing gamerule
* Raccoons can eat from caches when injured

**Squirrel Rework**
* Added trust system based on item interactions
* Squirrels inspect and accept or reject items
* Accepted items increase trust
* Squirrels can forage from Hazelnut Bushes
* They store collected nuts in their inventory
* Respects mobGriefing gamerule
* Squirrels can use hollow caches
* Trusted squirrels can deliver gifts

**Dog Rework**
* Dogs now hunt skeletons and react to their drops
* Dogs can pick up, carry and store items
* Added a burrow storage system for dogs
* Dogs can deposit stored items into burrows
* Added fetch and delivery behavior
* Dogs can bring items back to their owner
* Dogs react to nearby raccoons and creepers
* Dogs now seek shelter during rain
* Dogs can rest, lie down and sleep near their owner
* Dogs are now tamed and fed with cooked meat instead of bones

**Hedgehog Rework**
* Added defensive curl behavior reducing damage
* Curled hedgehogs damage nearby entities
* Aggressive players trigger defensive reactions
* Added daytime sleeping behavior
* Hedgehogs can regenerate health while resting
* Added sniffing behavior
* Hedgehogs interact with mushrooms and berries

**MiniSheep Rework**
* MiniSheeps now form herds and follow a leader
* MiniSheeps sleep at night
* They wake up when players or threats are nearby
* When attacked, nearby MiniSheeps defend each other
* MiniSheeps only attack defensively
* They return to their meadow home after chasing enemies
* Added running behavior and animation
* Spawns have been reduced to Meadow only

**Added**
* Hollow Cache, a Tree Stump spawning in Forest and Plains Biomes
* Mushroom Colonies, a renewable source for mushrooms
* Burrow, created by Dogs for shared storage
* Bones can now be thrown by using Shift-Right-Click

**Farm And Charm Compat**
* Boars can transform Dirt and Coarse Dirt into Fertilized Soil
* Raccoons steal eggs from ChickenCoops and ChickenNests

***

[1.1.4]

**Fixed**
* Fixed NeoForge config registration and syncing
* Added proper breeding food items for bison, cassowary, minisheep, penguin and red wolf

**Changed**
* TermiteSpawnEgg texture

***

[1.1.3]

**Added**
* White deer now apply Bad Omen and Slowness to the player who kills them

**Changed**
* Normal deer deaths no longer trigger debuffs

***

[1.1.2]

**Fixed**
* Fixed crash when breaking the Bounty Board

***

[1.1.1]

**Fixed**
* Fixed dedicated server crash caused by ContractItem

***

[1.1.0]

**Ported to 1.21.1**

Note: This version is not feature complete yet.