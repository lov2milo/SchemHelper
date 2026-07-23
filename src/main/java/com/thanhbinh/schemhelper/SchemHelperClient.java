package com.thanhbinh.schemhelper;

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
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class SchemHelperClient implements ClientModInitializer {

    private static final double REACH_DISTANCE = 6.0;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("schemhelper", "main"));

    private static boolean enabled = false;
    private static KeyBinding toggleKey;

    private static net.minecraft.util.math.BlockPos lastTargetPos = null;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.schemhelper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SchemHelperClient::onClientTick);
    }

    private static void onClientTick(MinecraftClient mc) {
        HotbarSwapper.tick();

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

        if (mc.currentScreen != null) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null) {
            lastTargetPos = null;
            return;
        }

        var wrapper = SchematicTargetHelper.getTargetedSchematicHit(player, REACH_DISTANCE);
        if (wrapper == null) {
            lastTargetPos = null;
            return;
        }

        net.minecraft.util.math.BlockPos pos = wrapper.getBlockHitResult().getBlockPos();

        if (pos.equals(lastTargetPos)) {
            return;
        }

        BlockState targetState = SchematicTargetHelper.resolveBlockState(pos);
        if (targetState == null || targetState.isAir()) {
            return;
        }

        Item wantedItem = targetState.getBlock().asItem();
        if (wantedItem == net.minecraft.item.Items.AIR) {
            return;
        }

        if (HotbarSwapper.ensureHolding(wantedItem)) {
            lastTargetPos = pos;
        }
    }
}
