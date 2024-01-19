package net.liopyu.entityjs.entities;

import com.google.common.collect.Sets;
import net.liopyu.entityjs.builders.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {


    protected final ArrowEntityJSBuilder builder;
    @NotNull
    protected ItemStack pickUpStack;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }

    public ArrowEntityJS(Level pLevel, LivingEntity pShooter, ArrowEntityJSBuilder builder) {
        super(EntityType.SPECTRAL_ARROW, pShooter, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }

    /*public ArrowEntityJS(Level pLevel, LivingEntity pShooter, ArrowEntityJSBuilder builder) {
        super(pLevel,pShooter);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }*/


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
