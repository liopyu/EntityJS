package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;

public record ThunderHitContext(ServerLevel level, LightningBolt lightningBolt, MobEntityJS entity) {
}
