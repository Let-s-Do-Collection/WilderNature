[1.1.5]

This update makes WilderNature entities feel more alive, adding distinct behaviors, daily routines, sleep cycles, and interactions with the world around them.

**Fixed**
* Idle animations for most entities are now much smoother and more natural
* Hazelnut Bushes no longer turn into Sweetberry Bushes when bone mealed

**Changed**
* Recolored Hazelnut Bushes
* Recolored Fish Oil
* BountyBoard got a texture & model redone from scratch
* Turkeys now lay Turkey Eggs instead of regular Eggs
* RedWolf has been renamed to SwiftFox

**Bounty Board**
* Added a daily bounty system with fresh objectives every day
* Added Hunt, Gather, Observe, and Explore bounty types
* Rewards now scale with difficulty and task size
* Larger tasks grant better rewards
* Added Guild Commissions as special high-value contracts
* Added biome exploration bounties
* Added observation bounties using the spyglass
* Added reward previews for bounties

**Hippo**
* Say hello to the new Hippos!
* Hippos react to nearby players and build up threat over time
* Hippos warn before attacking with a threatening display and yawn
* Feeding a Hippo calms it down
* Hippos defend their young aggressively
* Hippos snap at fish and attack boats
* Hippos graze on land and seek water during the day
* Added animations for idle, walking, swimming, eating, threatening and attacking

**Giraffe**
* Added Giraffes
* Giraffes live in small herds and prefer staying together
* They are peaceful and will avoid players when possible
* If threatened, they flee quickly over long distances
* Giraffes can defend themselves with powerful kicks
* Babies stay close to adults and follow the herd

**Scorpion**
* Scorpions live hidden beneath the surface and burrow into Dirt, Coarse Dirt and Sand
* They ambush nearby targets, striking quickly with poison before retreating
* When threatened, they rely on stealth and quick repositioning instead of direct fights
* A trust based system allows players to carefully calm and tame them
* Fermented Spider Eyes can be used to gain their trust over time
* Their glowing eyes reveal their presence in the dark just before they strike

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

**Cassowary Rework**
* Added territorial behavior with alert, threatening and attack phases
* Cassowaries now react more aggressively when babies are nearby
* Babies now have their own model
* Added warning phase before attacking players
* Improved targeting and chase behavior
* Added alert particles and threatening sounds
* Cassowaries now maintain visual focus on threats during escalation
* Added attack effect slowing players briefly on hit
* Improved overall threat logic and state transitions
* Cassowaries are now classified as neutral mobs
* They only attack when provoked or when defending their territory
* Improved escalation flow to make warning behavior more readable

**Bison Rework**
* Improved herd behavior and group reactions
* Added coordinated panic behavior across nearby Bisons
* Panic can now spread naturally through the herd
* Added rolling behavior
* Rolling Bisons can damage nearby entities
* Rolling can affect the terrain around them
* Grass can be trampled into Dirt and Coarse Dirt during rolls
* Added block and dust particles while rolling
* Improved threat response and movement during panic states

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

**Lion**
* Added Lions roaming the Savanna
* Lions live in small prides and stay close together
* Lions rest and sleep during the day
* Lions become active when disturbed or during hunting moments
* They defend their group if threatened

**Termites & Mounds**
* Added Termites and Termite Mounds to the Savanna
* Termites build colonies and live inside their mounds
* You may find hidden storage chambers inside some mounds
* Termites collect resources from nearby logs and bring them back home
* Sometimes you can catch them carrying Woodmeal
* Killing a termite at the right moment might reward you

**Rotten Logs**
* Logs near termite mounds can become Rotten Logs over time
* Rotten Logs can be hollowed out and infested by termites
* Infested logs can be cleaned with a shovel
* Cleaning them may reward you with Woodmeal
* Rotten Logs can be filled with Dirt
* Rotten Logs filled with Dirt can be transformed into Farmland

**Added**
* Hollow Cache, a Tree Stump spawning in Forest and Plains Biomes
* Mushroom Colonies, a renewable source for mushrooms
* Burrow, created by Dogs for shared storage
* Bones can now be thrown by using Shift-Right-Click
* BeaverDam, placed by Beavers near or inside water. Good source for sticks.
* Termite Mounds, a Savanna structure inhabited by Termites
* Rotten Logs, created and maintained by nearby Termites
* Woodmeal, produced through termite activity and stored in mounds
* Field Journal - once known as Animal Compendium - showing really basic informations about certain animals
* ThickLeather - dropped by Elephants, Hippos and other Entities. Right-Click with a Shear to transform it into Leather

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