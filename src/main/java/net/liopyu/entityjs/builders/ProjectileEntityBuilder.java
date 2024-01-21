package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

// TODO: Change this to a generic projectile builder, iirc items have to ba handled differently for arrows and other types
public abstract class ProjectileEntityBuilder<T extends Projectile & IArrowEntityJS> extends BuilderBase<EntityType<T>> {

    public static final List<ProjectileEntityBuilder<?>> thisList = new ArrayList<>();
    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;

    public transient BooleanSupplier tryPickup;

    public transient Function<T, ResourceLocation> getTextureLocation;

    public ProjectileEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        width = 0.5f;
        height = 0.5f;
        getTextureLocation = t -> t.getBuilder().newID("textures/entity/projectiles/", ".png");

    }

    @Info(value = "Sets the hit box of the entity type", params = {
            @Param(name = "width", value = "The width of the entity, defaults to 0.5 for arrows"),
            @Param(name = "height", value = "The height if the entity, defaults to 0.5 for arrows")
    })
    public ProjectileEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    public ProjectileEntityBuilder<T> tryPickup(BooleanSupplier tryPickup) {
        this.tryPickup = tryPickup;
        return this;
    }

    @Info(value = "Sets the client tracking range, defaults to 5")
    public ProjectileEntityBuilder<T> clientTrackingRange(int i) {
        clientTrackingRange = i;
        return this;
    }

    @Info(value = "Sets the update interval in ticks of the entity, defaults to 3")
    public ProjectileEntityBuilder<T> updateInterval(int i) {
        updateInterval = i;
        return this;
    }

    @Info(value = "Sets the mob category, defaults to 'misc'")
    public ProjectileEntityBuilder<T> mobCategory(MobCategory category) {
        mobCategory = category;
        return this;
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/entity/projectiles/<path>.png
            """)
    public ProjectileEntityBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }

    public abstract EntityType.EntityFactory<T> factory();

    /*@Override
    public EntityType<T> createObject() {
        return new ArrowEntityBuilder<>(this).get();
    }*/

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }
}
