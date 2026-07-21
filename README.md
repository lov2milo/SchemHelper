# SchemHelper

Client-side Fabric mod (MC 1.21.11) that auto-swaps the correct item into
your hand based on the Litematica schematic block you're looking at.
It never places, breaks, or duplicates blocks - it only rearranges items
already in your own inventory, using the same actions a player does by
hand (selecting a hotbar slot / swapping an item into the hotbar).

## Requirements (player side)
- Fabric Loader >= 0.18.0
- Fabric API
- MaLiLib
- Litematica

## Build (no PC needed)
Push to `main` on GitHub - Actions will build `schemhelper-<version>.jar`
and attach it as a workflow artifact. Download it from the Actions tab
and drop it in your `mods` folder alongside Litematica and MaLiLib.

## Usage
1. Load a schematic in Litematica as usual (Easy Place / overlay mode).
2. Press the "Toggle SchemHelper" key (unbound by default - set it in
   Controls > SchemHelper) to turn the assist on.
3. Look at a block position in the schematic - if you have the matching
   item somewhere in your inventory, it gets swapped into your hand.

## Notes
- Compiled against Litematica 0.26.12 / MaLiLib 0.27.8 for 1.21.11.
  Litematica's internal classes change between versions relatively
  often, so if it fails to load after a Litematica update, the class
  names in `SchematicTargetHelper.java` may need adjusting.
- Some servers restrict client-side build-assist mods. Check server
  rules before using this on a live server.
