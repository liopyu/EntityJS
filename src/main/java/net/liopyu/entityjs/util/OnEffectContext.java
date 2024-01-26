package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.effect.MobEffectInstance;

public record OnEffectContext(MobEffectInstance effect, MobEntityJS entity) {
}
