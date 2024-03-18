package net.liopyu.entityjs.builders.nonliving.entityjs;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.nonliving.entityjs.ArrowEntityJS;
import net.liopyu.entityjs.item.ArrowItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.function.Consumer;

public class ArrowEntityJSBuilder extends ArrowEntityBuilder<ArrowEntityJS> {

    public transient ArrowItemBuilder item;
    public transient boolean noItem;

    public ArrowEntityJSBuilder(ResourceLocation i) {
        super(i);
        this.item = (ArrowItemBuilder) new ArrowItemBuilder(id, this)
                .canBePickedup(true)
                .texture(i.getNamespace() + ":item/" + i.getPath());
    }


    @Override
    public EntityType.EntityFactory<ArrowEntityJS> factory() {
        return (type, level) -> new ArrowEntityJS(this, type, level);
    }

    @Info(value = "Indicates that no arrow item should be created for this entity type")
    public ArrowEntityJSBuilder noItem() {
        this.noItem = true;
        return this;
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
        if (!noItem) {
            RegistryInfo.ITEM.addBuilder(item);
        }
    }
}
