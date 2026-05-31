# Minecraft Archipelago

> A [Fabric](https://fabricmc.net/) mod and [Archipelago](https://archipelago.gg/) world integration for **Minecraft 1.21.1**. 

---

## Table of Contents

- [What Is This?](#what-is-this)
- [Requirements](#requirements)
- [Installation](#installation)
    - [APWorld (Server Side)](#apworld-server-side)
    - [Mod (Client / Host Side)](#mod-client--host-side)
- [Generating a Game](#generating-a-game)
    - [Minimal YAML](#minimal-yaml)
    - [All Options](#all-options)
- [Connecting In-Game](#connecting-in-game)
- [Gameplay Overview](#gameplay-overview)
    - [Item Locking](#item-locking)
    - [Location Types](#location-types)
    - [Lootable Checks](#lootable-checks)
    - [Item Collections](#item-collections)
- [Win Conditions](#win-conditions)
- [HUD Guide](#hud-guide)
- [Commands](#commands)
- [Known Limitations](#known-limitations)
- [Troubleshooting](#troubleshooting)
- [Credits](#credits)

---

## What Is This?

Minecraft Archipelago turns a standard Minecraft playthrough into an Archipelago multiworld game.
Your gear, tools, and game mechanics are locked from the start and must be **received as items** from the Archipelago server.
In return, completing advancements, killing bosses, and looting structures sends **items to other players** in your multiworld session.

At least one win condition must be configured. The game ends when all enabled conditions are satisfied.

---

## Requirements

| Component     | Version   |
|---------------|-----------|
| Minecraft     | 1.21.1    |
| Fabric Loader | ≥ 0.18.4  |
| Fabric API    | ≥ 0.116.8 |
| Archipelago   | ≥ 0.6.7   |
| Java          | 21        |

> **Single-player only.** Multiplayer support is not yet implemented.

---

## Installation

### APWorld (Server Side)

The APWorld is installed on the machine running the Archipelago server and is used during game generation.

1. Download `minecraft_archipelago.apworld` from the [Releases](#) page.
2. Place it in your Archipelago `worlds/` folder:
    - **Windows:** `%AppData%\Archipelago\worlds\`
    - **Linux/Mac:** `~/archipelago/worlds/`
3. Restart the Archipelago Launcher or server. `Minecraft Archipelago` should now appear as an available game.

### Mod (Client / Host Side)

The Fabric mod must be installed by anyone playing the game.


1. Download [Prism Launcher](https://prismlauncher.org/download/windows/) (or any other mod launcher)
2. Create a new Minecraft instance for version 1.21.1 with the Fabric mod loader
3. Download `minecraft-archipelago-x.x.x.jar` from the [Releases](#) page.
4. Place it in your Minecraft `mods/` folder.
5. Make sure **Fabric API** is also installed.
6. Launch Minecraft 1.21.1 with the Fabric profile.

> The mod only works in a **singleplayer world**. Open to LAN and dedicated server support are not yet available.

---

## Generating a Game

Create a `.yaml` file for your slot and place it in the Archipelago `Players/` folder before generating.

### Minimal YAML

```yaml
name: YourName
game: Minecraft Archipelago
Minecraft Archipelago:
  advancement_goal: 70
```

This enables a single win condition: complete 70% of all advancements.

### All Options

```yaml
name: YourName
game: Minecraft Archipelago
Minecraft Archipelago:

  # Percentage of advancements required to win (0 = disabled).
  # At least one win condition must be active.
  advancement_goal: 70           # 0–100, default 70

  # Enable Death Link — when you die, everyone dies. When someone else with Death Link dies you die too
  death_link: false              # true/false, default false

  # How many Archipelago Loot items are hidden in structure chests.
  lootable_checks: 20            # 0–42, default 20

  # Which bosses must be killed to satisfy the boss kill win condition.
  # Leave empty to disable. Valid values: ender_dragon, wither,
  # elder_guardian, warden
  required_boss_kills:
    - ender_dragon
    - wither

  # How many lootable checks must be claimed to win (0 = disabled).
  # Cannot exceed lootable_checks.
  required_lootable_checks: 0    # 0–42, default 0

  # Which item collection sets must be fully completed to win.
  # Leave empty to disable. Valid values listed below.
  required_item_collections: []
  # Available collections:
  #   all_music_discs      — All 19 music discs
  #   all_armor_sets       — Full sets of all 7 armor materials:
  #                        (Leather, Chainmail, Iron, Gold, Diamond, Netherite, and Turtle Helmet: 25 pieces total)
  #   all_pottery_sherds   — All 23 pottery sherds
  #   all_trims            — All 19 armor trim smithing templates
  #   rare_items           — Mace, Elytra, Trident, Enchanted Golden Apple,
  #                          Totem of Undying, Conduit
  #   all_flowers          — All 19 flower types
  #   all_heads            — All 6 mob heads
  #   all_dyes             — All 16 dyes
  #   all_weapons          — Diamond Sword, Bow, Crossbow, Trident, Mace, Shield
```

> **At least one win condition must be active.** A game with `advancement_goal: 0`, no `required_boss_kills`, `required_lootable_checks: 0`, and no `required_item_collections` will fail to generate.

Generate your game from the Archipelago Launcher

---

## Connecting In-Game

1. Create or load a **singleplayer world** in Minecraft 1.21.1.
2. Start the Archipelago server with your generated game.
3. Connect using the chat command:

```
/archipelago join <host> <port> <slotname> [password]
```

**Example:**
```
/archipelago join localhost 38281 YourName
```

To disconnect:
```
/archipelago leave
```

To check connection status:
```
/archipelago status
```

> Credentials are saved automatically. The mod will reconnect when you reload the world.

---

## Gameplay Overview

### Item Locking

When you load into a world, almost everything is locked. Items from your inventory will not function until unlocked by receiving the corresponding item from the Archipelago server. This includes:

- **Tools** — Stone through Netherite (4 progressive tiers)
- **Armor** — Leather through Netherite (4 progressive tiers)
- **Weapons** — Bow, Crossbow, Trident, Mace, Shield
- **Utility items** — Buckets, Potions, Elytra, Shears, Flint and Steel, and more
- **Craftable blocks** — Furnace, Anvil, Brewing Stand, Ender Chest, Smithing Table, and more
- **Gamerule changes** — Keep Inventory, Raids, Wandering Traders, etc.

> Wooden tools and crafting are always available.

### Location Types

There are three types of checks in the location pool:

| Type            | Count | How to check                                                |
|-----------------|-------|-------------------------------------------------------------|
| Advancements    | 117   | Complete any vanilla advancement                            |
| Boss Kills      | 4     | Kill the Ender Dragon, Wither, Elder Guardian, or Warden    |
| Lootable Checks | 0–42  | Find and claim Archipelago Loot items from structure chests |

### Lootable Checks

Structure chests have a 30% chance to contain an **Archipelago Loot** item. When you find one:

1. Pick it up. It will be assigned a check number automatically.
2. The item shows what's in store once the AP server is scouted.
3. **Right-click** the item in your inventory to claim it and send the check.

Lootable items are found in dungeons, villages, mineshafts, bastions, end cities, and many other structures.

### Item Collections

If any `required_item_collections` are configured, you must have had each item in the set in your inventory **at least once** during the playthrough. Items do not need to be held simultaneously, collecting and losing an item still counts.

Progress is tracked automatically whenever an item enters your inventory by any means.

---

## Win Conditions

All enabled win conditions must be satisfied simultaneously to win. At least one must be active.

| Condition            | How it works                                                                     |
|----------------------|----------------------------------------------------------------------------------|
| **Advancement Goal** | Check at least X% of the 117 vanilla advancements                                |
| **Boss Kills**       | Kill each configured boss at least once                                          |
| **Lootable Checks**  | Claim at least N lootable check items                                            |
| **Item Collections** | Have each item in the configured collection sets in your inventory at least once |

When all conditions are met, a victory message is broadcast and the goal is sent to the Archipelago server.

---

## HUD Guide

The mod includes two draggable overlay panels.

### Main HUD — toggle with `H`

Displays real-time game state:

| Section | Shows |
|---|---|
| Connection | Server address, slot name, connected / disconnected |
| Advancements | Checked / total with progress bar and goal marker |
| Boss Kills | Each boss with ✓ killed / ✦ required / ○ optional |
| Lootable Checks | Found / total with progress bar *(hidden if lootable_checks = 0)* |
| Equipment | Current armor and tool tier |

### Win Conditions Panel — toggle with `G`

Shows each active win condition with its own progress bar. When a condition is met, its bar turns gold.

- Press **`V`** while the panel is open to expand collection details, listing every item still needed.
- **Alt+drag** either panel to reposition it. Positions are saved between sessions.

---

## Commands

| Command                                             | Description                      |
|-----------------------------------------------------|----------------------------------|
| `/archipelago join <host> <port> <slot> [password]` | Connect to an Archipelago server |
| `/archipelago leave`                                | Disconnect from the server       |
| `/archipelago status`                               | Show current connection status   |

---

## Known Limitations

- **Single-player only.** Open to LAN and dedicated server multiplayer are not supported in this version.
- **Item collections do not retroactively count.** Items held before connecting to Archipelago are not tracked. Connect before starting your playthrough.
- The game is for  **Java Edition 1.21.1** with the Fabric mod loader only.
- Other mods are currently not supported but any client side mods are likely not to cause any issues. Use with caution.

---

## Troubleshooting

**I connected but no items are locked.**  
Make sure you loaded or created the world *after* installing the mod. Base rules are applied when a player joins for the first time.

**My advancement checked but nothing happened.**  
Verify the connection is active with `/archipelago status`. If disconnected, reconnect and all pending checks will be sent automatically.

**The Archipelago Loot item says "Connect to Archipelago first."**  
Pick up the item after connecting. Once connected, the item will be assigned a check number on the next inventory tick.

**I can't use my furnace / brewing stand / anvil.**  
These blocks are locked until you receive the corresponding unlock item (Furnace, Brewing Stand, Anvil) from the Archipelago server.

**Win condition panel shows "No active win conditions."**  
The mod hasn't received slot data yet. Connect to your Archipelago server first.

---

## Credits

Made by Corey Stinson
- Built with [Fabric](https://fabricmc.net/) for Minecraft 1.21.1
- Archipelago integration via [archipelago.mw Java client](https://github.com/ArchipelagoMW)
- Inspired by the Archipelago community

