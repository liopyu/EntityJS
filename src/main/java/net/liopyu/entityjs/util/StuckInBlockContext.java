package net.liopyu.entityjs.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StuckInBlockContext {
    public final LivingEntity entity;
    public final BlockState blockState;
    public final Vec3 vec3;

    public StuckInBlockContext(BlockState blockState, Vec3 vec3, LivingEntity entity) {
        this.entity = entity;
        this.vec3 = vec3;
        this.blockState = blockState;
    }
}
