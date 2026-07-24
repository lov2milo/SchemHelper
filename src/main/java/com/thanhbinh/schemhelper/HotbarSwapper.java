package com.thanhbinh.schemhelper;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;

public final class HotbarSwapper {

    private HotbarSwapper() {}

    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_END = 9;
    private static final int MAIN_INV_START = 9;
    private static final int MAIN_INV_END = 36;

    public static boolean ensureHolding(Item wantedItem) {
        if (wantedItem == null || isLocked(wantedItem)) {
            return false;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null || mc.getNetworkHandler() == null) {
            return false;
        }

        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.getItem() == wantedItem) {
            return true;
        }

        PlayerInventory inv = player.getInventory();

        for (int i = HOTBAR_START; i < HOTBAR_END; i++) {
            if (inv.getStack(i).getItem() == wantedItem) {
                inv.setSelectedSlot(i);
                mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(i));
                return true;
            }
        }

        int targetHotbarSlot = inv.getSelectedSlot();
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

        return false;
    }

    private static boolean isLocked(Item item) {
        if (item == null) {
            return false;
        }
        return new ItemStack(item).isIn(ItemTags.PICKAXES);
    }
}
