package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Generics;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.ProjectileEntityJS;
import net.liopyu.entityjs.item.ProjectileItemBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;


public class ProjectileEntityJSBuilder extends ProjectileEntityBuilder<ProjectileEntityJS> {

    public transient ProjectileItemBuilder item;

    public ProjectileEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    public transient Level level;


    @Override
    public EntityType.EntityFactory<ProjectileEntityJS> factory() {
        return (type, level) -> new ProjectileEntityJS(this, type, level);
    }

    @Info(value = "Creates the arrow item for this entity type")
    @Generics(value = BaseProjectileBuilder.class)
    public ProjectileEntityJSBuilder item(Consumer<ProjectileItemBuilder> item) {

        this.item = new ProjectileItemBuilder(id, this);
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
