package net.liopyu.entityjs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;

public record ExitPortalInfo(ServerLevel serverLevel, BlockPos blockPos, boolean flag, WorldBorder worldBorder) {
}

