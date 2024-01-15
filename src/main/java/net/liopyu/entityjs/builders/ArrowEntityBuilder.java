package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.entities.ArrowEntityJS;
import net.liopyu.entityjs.entities.IAnimatableJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.AbstractArrow;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public abstract class ArrowEntityBuilder<T extends AbstractArrow> extends BuilderBase<EntityType<T>> {


    @Override
    public EntityType<T> createObject() {
        ArrowEntityBuilder<?> builder;
        return null;
    }


    public static final List<ArrowEntityBuilder<?>> thisList = new ArrayList<>();
    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;

    public transient Function<T, ResourceLocation> getTextureLocation;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        width = 1;
        height = 1;
        getTextureLocation = t -> new ResourceLocation("textures/entity/projectiles", ".png");
    }

    @Info(value = "Sets the hit box of the entity type", params = {
            @Param(name = "width", value = "The width of the entity, defaults to 1"),
            @Param(name = "height", value = "The height if the entity, defaults to 1")
    })
    public ArrowEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    @Info(value = "Sets the client tracking range, defaults to 5")
    public ArrowEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public ArrowEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public ArrowEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/model/entity/<path>.png
            """)
    public ArrowEntityBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }

    public abstract EntityType.EntityFactory<ArrowEntityJS> factory();
}
