package net.liopyu.entityjs.entities.nonliving.entityjs;

import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.minecraft.world.item.ItemStack;

public interface IArrowEntityJS {
    ArrowEntityBuilder<?> getArrowBuilder();

    void setPickUpItem(ItemStack stack);
}
