package net.liopyu.entityjs.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;

public class ThunderHitContext {
    public final LivingEntity entity;
    public final ServerLevel level;
    public final LightningBolt lightningBolt;

    public ThunderHitContext(ServerLevel level, LightningBolt lightningBolt, LivingEntity entity) {
        this.level = level;
        this.lightningBolt = lightningBolt;
        this.entity = entity;
    }
}