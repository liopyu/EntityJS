package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;

public class ThunderHitContext {
    public final MobEntityJS entity;
    public final ServerLevel level;
    public final LightningBolt lightningBolt;

    public ThunderHitContext(ServerLevel level, LightningBolt lightningBolt, MobEntityJS entity) {
        this.level = level;
        this.lightningBolt = lightningBolt;
        this.entity = entity;
    }
}
