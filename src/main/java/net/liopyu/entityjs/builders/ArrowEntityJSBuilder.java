package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.liopyu.entityjs.item.ArrowItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.function.Consumer;

public class ArrowEntityJSBuilder extends ArrowEntityBuilder<ArrowEntityJS> {

    public transient ArrowItemBuilder item;

    public ArrowEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<ArrowEntityJS> factory() {
        return (type, level) -> new ArrowEntityJS(this, type, level);
    }

    @Info(value = "Creates the arrow item for this entity type")
    @Generics(value = ArrowEntityBuilder.class)
    public ArrowEntityJSBuilder item(Consumer<ArrowItemBuilder> item) {

        this.item = new ArrowItemBuilder(id, this);
        item.accept(this.item);

        return this;
    }

    @Override
    public void createAdditionalObjects() {
        if (item != null) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }
}
