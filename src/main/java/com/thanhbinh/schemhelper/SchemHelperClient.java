package com.thanhbinh.schemhelper;

import fi.dy.masa.litematica.util.RayTraceUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

public class SchemHelperClient implements ClientModInitializer {

    /** How far (in blocks) to raycast into the schematic - matches typical player reach. */
    private static final double REACH_DISTANCE = 6.0;

    private static boolean enabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.schemhelper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN, // unbound by default - set in Controls menu
                "category.schemhelper"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SchemHelperClient::onClientTick);
    }

    private static void onClientTick(MinecraftClient mc) {
        // Handle the toggle keybind.
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[SchemHelper] " + (enabled ? "Enabled" : "Disabled")),
                        true
                );
            }
        }

        if (!enabled) {
            return;
        }

        // Don't do anything while a GUI (inventory, chat, etc.) is open,
        // to avoid interfering with the player's own clicks.
        if (mc.currentScreen != null) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) {
            return;
        }

        BlockState targetState = SchematicTargetHelper.getTargetedSchematicBlock(player, REACH_DISTANCE);
        if (targetState == null || targetState.isAir()) {
            return;
        }

        Item wantedItem = targetState.getBlock().asItem();
        if (wantedItem == net.minecraft.item.Items.AIR) {
            return; // block has no direct item form (e.g. water, fire) - nothing to hold
        }

        HotbarSwapper.ensureHolding(wantedItem);
    }
}
