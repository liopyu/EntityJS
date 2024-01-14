package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.effect.MobEffectInstance;

public class OnEffectContext {
    public final MobEntityJS entity;
    public final MobEffectInstance effect;

    public OnEffectContext(MobEffectInstance effect, MobEntityJS entity) {
        this.entity = entity;
        this.effect = effect;
    }
}
