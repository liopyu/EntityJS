package net.liopyu.entityjs.entities;

import com.mojang.logging.LogUtils;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.*;
import net.liopyu.entityjs.item.ArrowItemBuilder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.logging.Logger;

public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {

    public static final org.slf4j.Logger LOGGER = LogUtils.getLogger();
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
    }


    @Override
    protected ItemStack getPickupItem() {

        return new ItemStack(Items.ARROW);
    }

}
