package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ArrowEntityJS extends Arrow implements IArrowEntityJS {

    protected final ArrowEntityJSBuilder builder;
    @NotNull
    protected ItemStack pickUpStack;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends Arrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }


    @Override
    public ArrowEntityBuilder<?> getBuilder() {
        return builder;
    }

    @Override
    public void setPickUpItem(ItemStack stack) {
        pickUpStack = stack;

    }

    /*@Override
    protected boolean tryPickup(Player p_150121_) {
        if (builder.tryPickup != null) {
            if (builder.tryPickup.getAsBoolean()) {
                if (!p_150121_.getAbilities().instabuild) {
                    p_150121_.getInventory().add(this.getPickupItem());
                    return builder.tryPickup.getAsBoolean();
                }
            } else return builder.tryPickup.getAsBoolean();
        }
        return super.tryPickup(p_150121_);
    }*/

    @Override
    protected ItemStack getPickupItem() {
        return pickUpStack;
    }

}
