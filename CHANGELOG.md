[1.1.5]

This update focuses on making the WilderNature entities more soulful and adding features and general behaviours. They now have unique behaviours, daily plans and sleep schedules, and they can eat from certain blocks or help the ecosystem in your world.

**Fixed**
* Idle animations for most entities are now much smoother and more natural
* Hazelnut Bushes no longer turn into Sweetberry Bushes when bone mealed

**Changed**
* Recolored Hazelnut Bushes
* Recolored Fish Oil
* Turkeys now lay Turkey Eggs instead of regular Eggs

**Owl Rework**
* Completely overhauled owl behavior and AI
* Owls now look for natural perches before going to sleep
* Added realistic sleep preparation with randomized timing
* Improved wake up logic, including better threat detection and visual feedback
* Refined flying and landing so they properly align with perches
* Owls now hunt at night with randomized hunting windows
* They will actively target undead mobs during hunts
* Added interaction with dropped rotten flesh, owls can now find and eat it
* Overall AI flow feels much smoother with better state transitions
* Reduced repetitive behavior using cooldowns and more randomness
* Improved animations for sleeping, flying, movement, and idle actions

**Deer Rework**
* Deer now have an awareness system that reacts to player movement, sprinting, and held items
* They gradually calm down over time if no threat is nearby
* Added a proper panic system with herd wide reactions and more natural fleeing behavior
* Improved herd logic, they now follow a leader and the group persists better after reloading the world
* Added dynamic sleeping behavior with a preparation phase and proper wake up triggers
* Deer prefer sleeping on grass blocks
* They now seek shelter during the night
* Deer react correctly to players in Creative and Spectator mode
* Panic pathfinding has been improved to prevent deer from bunching up or blocking each other
* Added particle effects for awareness and alert states

**Boar Rework**
* Completely reworked rooting behavior with better pathfinding and smarter target selection
* Boars now properly search for valid grass blocks before digging
* Added a visible preparation phase before they start rooting
* You can now use Truffles to encourage boars to root - only with a slight  chance though
* Rooting now uses a loot table system, making drops fully configurable
* Added cooldowns so they don’t root excessively anymore
* Added eating behavior from caches when injured
* Rooting is now also allowed for Dirt and Coarse Dirt - but with a much lower percentage for loot. 
* Rooting now properly respects the mobGriefing gamerule

**Raccoon Rework**
* Fully reworked raccoon behavior and AI
* Raccoons can now loot containers, carry items, and store them in nearby hollow caches
* Added proper inventory handling with more consistent item behavior
* Raccoons will deposit collected items into caches with a short interaction phase
* Added chance to generate additional loot bags when storing items in caches
* Improved interaction priorities between looting, storing, sheltering, and washing
* Added crop nibbling behavior during the night
* Crops are no longer destroyed, instead their growth stage is reduced
* Crop interaction now respects the mobGriefing gamerule
* Added eating behavior from caches when injured
* Improved overall AI flow with better state transitions and reduced conflicts

**Squirrel Rework**
* Fully reworked squirrel behavior and AI
* Added a trust system based on item interactions
* Squirrels inspect offered items before deciding to accept or reject them
* Accepted items raise trust, while rejected ones are dropped with visual feedback
* You can now see the item they’re holding while they evaluate or carry it
* Squirrels now forage from Hazelnut Bushes and pick up dropped Hazelnuts
* They can harvest Hazelnut Bushes and store the nuts in their own inventory
* Harvesting now respects the mobGriefing gamerule
* Added interaction with hollow caches, squirrels can deposit stored items into nearby ones
* Added eating behavior from caches when injured
* Highly trusted squirrels can now give you gifts by delivery
* Better overall AI priority handling between foraging, sheltering, storing, and gifting

**Dog Rework**
* Added a fully reworked dog with active utility and personality driven behavior
* Dogs will now hunt skeletons and interact with their drops
* After killing a skeleton, dogs may deliver bones either to their owner or to their burrow depending on context
* Introduced a small inventory system, allowing dogs to carry and manage collected items
* Dogs can pick up bones from the ground and store them for later use
* Added burrow system, dogs can dig their own burrow in grass blocks
* Digging includes a visible digging phase with particles and sound feedback
* Dogs will deposit collected items into their burrow with a short interaction phase
* Burrows act as shared storage and can be used by other entities
* Added carrying behavior, dogs visibly hold and transport items in their mouth
* Added proud delivery behavior when bringing items back to the player
* Dogs will bark and react to nearby raccoons, helping to protect the area
* Added creeper alert behavior with cooldown, dogs will howl when danger is nearby
* Dogs now seek shelter when it is raining instead of standing in the open
* Added digging, carrying, and interaction animations for better visual feedback
* Overall behavior feels more intentional, reactive, and useful as a companion

**Hedgehog Rework**
* Added defensive curl behavior when threats are nearby, greatly reducing incoming damage
* Curled hedgehogs now damage nearby entities on contact, similar to thorns
* Players taking aggressive actions or holding weapons will trigger defensive reactions
* Added fall interaction, jumping onto a hedgehog can damage the player but kill the hedgehog
* Introduced dynamic sleeping behavior during the day with proper wake up conditions
* Hedgehogs now occasionally rest to regenerate health when injured
* Added sniffing behavior for more life like idle activity
* Added food interaction with mushrooms and sweet berries directly from the world
* Hedgehogs can now convert mushrooms into Mushroom Colonies under the right conditions
* Improved animation flow with sniffing, sleeping, defensive, and idle states working together
* Overall behavior is more reactive, with better threat awareness and smoother transitions

**Added**
* Hollow Cache, a Tree Stump spawning in Forest and Plains Biomes
* Mushroom Colonies - a renewable source for brown and red mushrooms
* Burrow, a hole digged by Dogs - used by multiple entities to store and hide loot

**Farm And Charm Compat**
* If installed, Boars can transform Dirt & Coarse Dirt into Fertilized Soil 
* If installed, Raccoons will steal Eggs out of ChickenCoops and ChickenNests

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
