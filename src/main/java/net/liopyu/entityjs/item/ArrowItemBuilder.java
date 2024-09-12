package net.liopyu.entityjs.item;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityJSBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;


public class ArrowItemBuilder extends ItemBuilder {
    public transient final ArrowEntityJSBuilder parent;
    public transient boolean canBePickedUp;

    public ArrowItemBuilder(ResourceLocation i, ArrowEntityJSBuilder parent) {
        super(i);
        this.parent = parent;
        canBePickedUp = true;
        baseTexture = i.getNamespace() + ":item/" + i.getPath();
    }

    @Info(value = "Sets if the arrow can be picked up")
    public ArrowItemBuilder canBePickedup(boolean canBePickedUp) {
        this.canBePickedUp = canBePickedUp;
        return this;
    }

    @Override
    public Item createObject() {
        return new ArrowItem(createItemProperties()) {
            @Override
            public AbstractArrow createArrow(Level pLevel, ItemStack pStack, LivingEntity pShooter, @Nullable ItemStack weapon) {
                final ArrowEntityJS arrow = new ArrowEntityJS(parent, pLevel, pShooter, pStack, weapon);
                if (canBePickedUp) {
                    final ItemStack stack = new ItemStack(pStack.getItem());
                    arrow.setPickUpItem(stack);
                }
                return arrow;
            }
        };
    }
}
