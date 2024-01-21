package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.minecraft.world.item.ItemStack;

public interface IArrowEntityJS {
    ArrowEntityBuilder<?> getBuilder();

    void setPickUpItem(ItemStack stack);
}
