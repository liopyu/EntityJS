package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.entities.IProjectileEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public abstract class BaseProjectileBuilder<T extends Entity> extends BuilderBase<EntityType<T>> {


    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;


    public BaseProjectileBuilder(ResourceLocation i) {
        super(i);
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        width = 0.5f;
        height = 0.5f;


    }

    @Info(value = "Sets the hit box of the entity type", params = {
            @Param(name = "width", value = "The width of the entity, defaults to 0.5 for arrows"),
            @Param(name = "height", value = "The height if the entity, defaults to 0.5 for arrows")
    })
    public BaseProjectileBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    @Info(value = "Sets the client tracking range, defaults to 5")
    public BaseProjectileBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public BaseProjectileBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public BaseProjectileBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }


    public abstract EntityType.EntityFactory<T> factory();


    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }
}
