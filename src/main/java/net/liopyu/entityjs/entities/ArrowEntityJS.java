package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ArrowEntityJSBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class ArrowEntityJS extends AbstractArrow {


    protected final ArrowEntityJSBuilder builder;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }
}
