[1.1.5]

**Fixed**
* Idle animations for most entities are now much smoother and more natural.
* Hazelnut Bushes no longer turn into Sweetberry Bushes when bone mealed.

**Changed**
* Recolored Hazelnut Bushes.
* Recolored Fish Oil.
* Turkeys now lay Turkey Eggs instead of regular Eggs.

**Owl Rework**
* Completely overhauled owl behavior and AI.
* Owls now look for natural perches before going to sleep.
* Added realistic sleep preparation with randomized timing.
* Improved wake*up logic, including better threat detection and visual feedback.
* Refined flying and landing so they properly align with perches.
* Owls now hunt at night with randomized hunting windows.
* They will actively target undead mobs during hunts.
* Added interaction with dropped rotten flesh — owls can now find and eat it.
* Overall AI flow feels much smoother with better state transitions.
* Reduced repetitive behavior using cooldowns and more randomness.
* Improved animations for sleeping, flying, movement, and idle actions.

**Deer Rework**
* Deer now have an awareness system that reacts to player movement, sprinting, and held items.
* They gradually calm down over time if no threat is nearby.
* Added a proper panic system with herd*wide reactions and more natural fleeing behavior.
* Improved herd logic — they now follow a leader and the group persists better after reloading the world.
* Added dynamic sleeping behavior with a preparation phase and proper wake*up triggers.
* Deer prefer sleeping on grass blocks.
* They now seek shelter during the night.
* Deer react correctly to players in Creative and Spectator mode.
* Panic pathfinding has been improved to prevent deer from bunching up or blocking each other.
* Added particle effects for awareness and alert states.

**Boar Rework**
* Completely reworked rooting behavior with better pathfinding and smarter target selection.
* Boars now properly search for valid grass blocks before digging.
* Added a visible preparation phase before they start rooting.
* You can now use Truffles to encourage boars to root.
* Rooting now uses a loot table system, making drops fully configurable.
* Added cooldowns so they don’t root excessively.
* Rooting now properly respects the `mobGriefing` gamerule.

**Squirrel Rework**
* Fully reworked squirrel behavior and AI.
* Added a trust system based on item interactions.
* Squirrels inspect offered items before deciding to accept or reject them.
* Accepted items raise trust, while rejected ones are dropped with visual feedback.
* You can now see the item they’re holding while they evaluate or carry it.
* Squirrels now forage from Hazelnut Bushes and pick up dropped Hazelnuts.
* They can harvest Hazelnut Bushes and store the nuts in their own inventory.
* Added interaction with hollow caches — squirrels can deposit stored items into nearby ones.
* Highly trusted squirrels can now give you gifts by delivery.
* Better overall AI priority handling between foraging, sheltering, storing, and gifting.

**Added**
* Hollow Cache - a Tree Stump spawning in Forest and Plains Biomes. 

***

[1.1.4]

**Fixed**
* Fixed NeoForge config registration and syncing
* Added proper breeding food items for bison, cassowary, minisheep, penguin and red wolf

**Changed**
* `TermiteSpawnEgg` Texture
  
***

[1.1.3]

**Added**
* White deer now apply **Bad Omen** (60 min) and **Slowness II** (5 min) to the player who kills them (directly or with projectiles).

**Changed**
* Normal deer deaths no longer trigger any player debuffs.

***

[1.1.2]

**Fixed:**
* Fixed a crash when breaking the **Bounty Board**. Dropped items no longer contain invalid/empty BlockEntityTag data.

***

[1.1.1]

**Fixed:**
* Fixed dedicated server crash caused by `ContractItem` using client*only `Minecraft` reference.

***

[1.1.0]

**Ported to 1.21.1**

_Note: This version is not feature complete yet._

***
