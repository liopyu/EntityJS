package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public record StuckInBlockContext(BlockState blockState, Vec3 vec3, MobEntityJS entity) {
}
