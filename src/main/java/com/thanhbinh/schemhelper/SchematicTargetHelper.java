package com.thanhbinh.schemhelper;

import fi.dy.masa.litematica.util.RayTraceUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.BlockPos;

public final class SchematicTargetHelper {

    private SchematicTargetHelper() {}

    public static RayTraceUtils.RayTraceWrapper getTargetedSchematicHit(ClientPlayerEntity player, double reach) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (player == null || mc.world == null) {
            return null;
        }

        RayTraceUtils.RayTraceWrapper wrapper =
                RayTraceUtils.getSchematicWorldTraceWrapperIfClosest(mc.world, player, reach);

        if (wrapper == null
                || wrapper.getHitType() != RayTraceUtils.RayTraceWrapper.HitType.SCHEMATIC_BLOCK
                || wrapper.getBlockHitResult() == null) {
            return null;
        }

        return wrapper;
    }

    public static BlockState resolveBlockState(BlockPos pos) {
        WorldSchematic schematicWorld = SchematicWorldHandler.INSTANCE.getSchematicWorld();
        if (schematicWorld == null) {
            return null;
        }
        return schematicWorld.getBlockState(pos);
    }

    public static BlockState getTargetedSchematicBlock(ClientPlayerEntity player, double reach) {
        RayTraceUtils.RayTraceWrapper wrapper = getTargetedSchematicHit(player, reach);
        if (wrapper == null) {
            return null;
        }
        return resolveBlockState(wrapper.getBlockHitResult().getBlockPos());
    }
}
