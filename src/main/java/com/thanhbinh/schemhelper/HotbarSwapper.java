package com.thanhbinh.schemhelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Makes sure the player is holding a given item, using the exact same
 * client actions a human does by hand:
 *   - switching the selected hotbar slot (number keys / scroll wheel), or
 *   - swapping an item from the main inventory into the hotbar (shift/number
 *     click in the inventory screen).
 *
 * This never places, breaks, or duplicates anything - it only rearranges
 * items the player already owns, through normal server-validated packets.
 */
public final class HotbarSwapper {

    private HotbarSwapper() {}

    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_END = 9;   // exclusive
    private static final int MAIN_INV_START = 9;
    private static final int MAIN_INV_END = 36; // exclusive

    /**
     * @return true if the player is now holding (or already was holding)
     *         the wanted item; false if they don't have any in their
     *         inventory at all.
     */
    public static boolean ensureHolding(Item wantedItem) {
        if (wantedItem == null) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null || mc.getNetworkHandler() == null) {
            return false;
        }

        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty() && mainHand.getItem() == wantedItem) {
            return true; // already holding it
        }

        PlayerInventory inv = player.getInventory();

        // 1) Already on the hotbar somewhere -> just switch selected slot.
        for (int i = HOTBAR_START; i < HOTBAR_END; i++) {
            if (inv.getStack(i).getItem() == wantedItem) {
                inv.selectedSlot = i;
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(i));
                return true;
            }
        }

        // 2) Somewhere in the main inventory -> swap it into the current
        //    hotbar slot (equivalent to pressing that number key while
        //    hovering the item in the inventory screen).
        int targetHotbarSlot = inv.selectedSlot;
        for (int i = MAIN_INV_START; i < MAIN_INV_END; i++) {
            if (inv.getStack(i).getItem() == wantedItem) {
                mc.interactionManager.clickSlot(
                        player.currentScreenHandler.syncId,
                        i,
                        targetHotbarSlot,
                        SlotActionType.SWAP,
                        player
                );
                return true;
            }
        }

        // 3) Player has none of this item.
        return false;
    }
}
