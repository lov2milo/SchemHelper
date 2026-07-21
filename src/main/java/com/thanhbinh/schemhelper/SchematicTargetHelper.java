package com.thanhbinh.schemhelper;

import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

/**
 * Reads the block the player is currently looking at *inside the loaded
 * Litematica schematic* - i.e. the same information Litematica itself
 * uses for its Easy Place feature and the corner-of-screen block info HUD.
 *
 * This does NOT place, break or move any block. It only reads data that
 * Litematica already computes every tick for its own overlay.
 */
public final class SchematicTargetHelper {

    private SchematicTargetHelper() {}

    /**
     * @param player the client player doing the raycast
     * @param reach  how far to trace, in blocks (use the player's normal
     *               reach distance, typically ~4.5-6.0)
     * @return the BlockState the schematic wants at the targeted position,
     *         or null if the player isn't looking at a schematic block.
     */
    public static BlockState getTargetedSchematicBlock(ClientPlayerEntity player, double reach) {
        if (player == null) {
            return null;
        }

        RayTraceUtils.RayTraceWrapper wrapper =
                RayTraceUtils.getSchematicWorldTraceWrapperIfClosest(player, reach);

        if (wrapper == null
                || wrapper.getHitType() != RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK) {
            return null;
        }

        BlockHitResult hit = wrapper.getBlockHitResult();
        if (hit == null) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();

        WorldSchematic schematicWorld = SchematicWorldHandler.INSTANCE.getSchematicWorld();
        if (schematicWorld == null) {
            return null;
        }

        return schematicWorld.getBlockState(pos);
    }
}
