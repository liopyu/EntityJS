package net.liopyu.entityjs.util;

import net.liopyu.entityjs.entities.MobEntityJS;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class OnEffectContext {
    public final LivingEntity entity;
    public final MobEffectInstance effect;

    public OnEffectContext(MobEffectInstance effect, LivingEntity entity) {
        this.entity = entity;
        this.effect = effect;
    }
}
