package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.ProjectileEntityBuilder;
import net.minecraft.world.item.ItemStack;

public interface IArrowEntityJS {
    ProjectileEntityBuilder<?> getBuilder();

    void setPickUpItem(ItemStack stack);

    ItemStack getPickupItem();
}
