package net.liopyu.entityjs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.border.WorldBorder;

public class ExitPortalInfo {
    public final ServerLevel serverLevel;
    public final BlockPos blockPos;
    public final boolean flag;
    public final WorldBorder worldBorder;

    public ExitPortalInfo(ServerLevel serverLevel, BlockPos blockPos, boolean flag, WorldBorder worldBorder) {
        this.serverLevel = serverLevel;
        this.blockPos = blockPos;
        this.flag = flag;
        this.worldBorder = worldBorder;
    }
}

