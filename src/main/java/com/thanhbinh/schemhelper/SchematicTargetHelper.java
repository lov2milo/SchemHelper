package com.thanhbinh.schemhelper;

import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public final class SchematicTargetHelper {

    private SchematicTargetHelper() {}

    public static BlockState getTargetedSchematicBlock(ClientPlayerEntity player, double reach) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (player == null || mc.world == null) {
            return null;
        }

        RayTraceUtils.RayTraceWrapper wrapper =
                RayTraceUtils.getSchematicWorldTraceWrapperIfClosest(mc.world, player, reach);

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
