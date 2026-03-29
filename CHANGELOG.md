[1.1.5]

**Fixed**
* Idle Animation for most entities are now more fluent
* HazelnutBushes transforming into SweetberryBushes when being bone mealed

**Changed**
* Recolored HazelnutBushes
* Recolored FishOil
* Turkeys now lay TurkeyEggs instead of Eggs

* **Owl Rework**
  * Completely overhauled owl behavior and AI
  * Owls now search for natural perches before sleeping
  * Added realistic sleep preparation with randomized timing
  * Improved wake-up logic with threat detection and visual feedback
  * Refined flying and landing behavior for proper perch alignment
  * Introduced dynamic night hunting with randomized hunt windows
  * Owls now target undead mobs during hunting phases
  * Added interaction with dropped rotten flesh, including consuming behavior
  * Improved overall AI flow with smoother transitions between states
  * Reduced repetitive behavior through cooldowns and randomness
  * Enhanced animation handling for sleep, movement, and ambient actions

* **Deer Rework**
  * Added awareness system reacting to player movement, sprinting, and held items
  * Deer now gradually calm down over time when no threat is present
  * Implemented panic system with herd-wide reaction and natural flee behavior
  * Improved herd logic with leader following and persistence across reloads
  * Added dynamic sleeping behavior with preparation phase and wake-up triggers
  * Deer now react correctly to players in creative and spectator mode
  * Improved pathfinding during panic to prevent clustering and blocking
  * Added visual feedback using particles for awareness and alert states
  * Enhanced animation handling including proper sleep animation integration

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
