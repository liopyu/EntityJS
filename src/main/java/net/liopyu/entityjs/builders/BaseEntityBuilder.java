package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Consumer;

public abstract class BaseEntityBuilder<T extends Entity> extends BuilderBase<EntityType<T>> {

    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;

    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        clientTrackingRange = 5;
        updateInterval = 3;
        mobCategory = MobCategory.MISC;
        width = 1.0f;
        height = 1.5f;
    }

    @Info(value = """
            Sets the hit box of the entity type.
                        
            @param width The width of the entity. Defaults to 1.0.
            @param height The height of the entity. Defaults to 1.5.
                        
            Example usage:
            ```javascript
            entityBuilder.sized(1.0f, 1.5f);
            ```
            """)
    public BaseEntityBuilder<T> sized(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }


    @Info(value = """
            Sets the client tracking range. Defaults to 5.
                        
            @param trackingRange The client tracking range.
                        
            Example usage:
            ```javascript
            entityBuilder.clientTrackingRange(8);
            ```
            """)
    public BaseEntityBuilder<T> clientTrackingRange(int trackingRange) {
        this.clientTrackingRange = trackingRange;
        return this;
    }


    @Info(value = """
            Sets the update interval in ticks of the entity. Defaults to 3.
                        
            @param updateInterval The update interval in ticks.
                        
            Example usage:
            ```javascript
            entityBuilder.updateInterval(5);
            ```
            """)
    public BaseEntityBuilder<T> updateInterval(int updateInterval) {
        this.updateInterval = updateInterval;
        return this;
    }


    @Info(value = """
            Sets the mob category for the entity.
            Available options: 'monster', 'creature', 'ambient', 'water_creature', 'misc'.
            Defaults to 'misc'.
                        
            Example usage:
            ```javascript
            entityBuilder.mobCategory('monster');
            ```
            """)
    public BaseEntityBuilder<T> mobCategory(String category) {
        mobCategory = BaseLivingEntityBuilder.stringToMobCategory(category);
        return this;
    }

    @Info(value = "Sets the lerpTo behavior with parameters (x, y, z, yaw, pitch, posRotationIncrements, teleport).")
    public BaseEntityBuilder<T> lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }

    public abstract EntityType.EntityFactory<T> factory();

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }


}
