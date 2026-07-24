package net.liopyu.entityjs.item;

import com.mrcrayfish.guns.item.AmmoItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.liopyu.entityjs.builders.nonliving.modded.CGMProjectileEntityJSBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


public class CGMProjectileItemBuilder extends ItemBuilder {
    public static final List<CGMProjectileItemBuilder> thisList = new ArrayList<>();
    public transient final CGMProjectileEntityJSBuilder parent;

    public CGMProjectileItemBuilder(ResourceLocation i, CGMProjectileEntityJSBuilder parent) {
        super(i);
        thisList.add(this);
        this.parent = parent;
        texture = i.getNamespace() + ":item/" + i.getPath();
    }

    @Override
    public AmmoItem createObject() {
        return new AmmoItem(createItemProperties());
    }
}