package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.nonliving.entityjs.ProjectileEntityJS;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;


public class ProjectileEntityJSBuilder extends ProjectileEntityBuilder<ProjectileEntityJS> {

    public transient ProjectileItemBuilder item;
    public transient boolean noItem;

    public ProjectileEntityJSBuilder(ResourceLocation i) {
        super(i);
        this.item = (ProjectileItemBuilder) new ProjectileItemBuilder(id, this)
                .canThrow(true)
                .texture(i.getNamespace() + ":item/" + i.getPath());
    }

    public transient Level level;


    @Override
    public EntityType.EntityFactory<ProjectileEntityJS> factory() {
        return (type, level) -> new ProjectileEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return null;
    }

    @Info(value = "Indicates that no projectile item should be created for this entity type")
    public ProjectileEntityJSBuilder noItem() {
        this.noItem = true;
        return this;
    }

    @Info(value = "Creates the arrow item for this entity type")
    @Generics(value = BaseEntityBuilder.class)
    public ProjectileEntityJSBuilder item(Consumer<ProjectileItemBuilder> item) {

        this.item = new ProjectileItemBuilder(id, this);
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
