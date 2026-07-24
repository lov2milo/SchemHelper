package com.thanhbinh.schemhelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.glfw.GLFW;

public class SchemHelperClient implements ClientModInitializer {

    private static final double REACH_DISTANCE = 6.0;

    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("schemhelper", "main"));

    private static boolean enabled = false;
    private static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.schemhelper.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(SchemHelperClient::onClientTick);
        UseBlockCallback.EVENT.register(SchemHelperClient::onUseBlock);
    }

    private static void onClientTick(MinecraftClient mc) {
        while (toggleKey.wasPressed()) {
            enabled = !enabled;
            if (mc.player != null) {
                mc.player.sendMessage(
                        Text.literal("[SchemHelper] " + (enabled ? "Enabled" : "Disabled")),
                        true
                );
            }
        }
    }

    private static ActionResult onUseBlock(PlayerEntity player, net.minecraft.world.World world, Hand hand,
                                            net.minecraft.util.hit.BlockHitResult hitResult) {
        if (!enabled || hand != Hand.MAIN_HAND) {
            return ActionResult.PASS;
        }

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null || !(player instanceof ClientPlayerEntity clientPlayer)) {
            return ActionResult.PASS;
        }

        var wrapper = SchematicTargetHelper.getTargetedSchematicHit(clientPlayer, REACH_DISTANCE);
        if (wrapper == null) {
            return ActionResult.PASS;
        }

        BlockPos pos = wrapper.getBlockHitResult().getBlockPos();
        BlockState targetState = SchematicTargetHelper.resolveBlockState(pos);
        if (targetState == null || targetState.isAir()) {
            return ActionResult.PASS;
        }

        Item wantedItem = targetState.getBlock().asItem();
        if (wantedItem == net.minecraft.item.Items.AIR) {
            return ActionResult.PASS;
        }

        if (clientPlayer.getMainHandStack().getItem() == wantedItem) {
            return ActionResult.PASS;
        }

        HotbarSwapper.ensureHolding(wantedItem);
        return ActionResult.FAIL;
    }
}
