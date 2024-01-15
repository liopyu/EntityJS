package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.*;
import net.liopyu.entityjs.item.ArrowItemBuilder;
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
        if (builder.getPickupItem != null) {
            final ArrowEntityBuilder<?> parent = this.builder.getPickupItem.parent;
            final ItemStack stack = new ArrowItemBuilder(this.builder.getPickupItem.id, parent).createObject().getDefaultInstance();
            ConsoleJS.STARTUP.info("Created pickup item " + stack);
            return stack;
        } else {
            ConsoleJS.STARTUP.error("No ArrowEntityJSBuilder found for " + builder.id);
            return ItemStack.EMPTY;
        }

    }

}
