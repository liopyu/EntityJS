package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.nonliving.ArrowEntityBuilder;
import net.minecraft.world.item.ItemStack;

public interface IArrowEntityJS {
    ArrowEntityBuilder<?> getArrowBuilder();

    void setPickUpItem(ItemStack stack);
}
