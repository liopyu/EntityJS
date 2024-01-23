package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseEntityBuilder<T extends Entity> extends BuilderBase<EntityType<T>> {

    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;

    public BaseEntityBuilder(ResourceLocation i) {
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
    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    @Info(value = "Sets the client tracking range, defaults to 5")
    public BaseEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public BaseEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public BaseEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }


    public abstract EntityType.EntityFactory<T> factory();


    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }


}
