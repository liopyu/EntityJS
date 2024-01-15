package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {


    protected final ArrowEntityJSBuilder builder;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
    }

    @Override
    public ArrowEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }
}
