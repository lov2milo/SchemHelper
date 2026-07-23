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

    private static final int UNLOCK_GRACE_TICKS = 6;

    private static boolean wasBreakingLastTick = false;
    private static int unlockGraceRemaining = 0;

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean breakingNow = mc.interactionManager != null && mc.interactionManager.isBreakingBlock();

        if (wasBreakingLastTick && !breakingNow) {
            unlockGraceRemaining = UNLOCK_GRACE_TICKS;
        } else if (unlockGraceRemaining > 0) {
            unlockGraceRemaining--;
        }

        wasBreakingLastTick = breakingNow;
    }

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

        if (!mainHand.isEmpty() && isLocked(mainHand.getItem()) && unlockGraceRemaining <= 0) {
            return false;
        }

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
