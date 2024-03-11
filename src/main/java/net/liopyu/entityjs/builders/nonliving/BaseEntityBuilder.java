package net.liopyu.entityjs.builders.nonliving;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseEntityBuilder<T extends Entity> extends BuilderBase<EntityType<T>> {

    public transient float width;
    public transient float height;
    public transient int clientTrackingRange;
    public transient int updateInterval;
    public transient MobCategory mobCategory;
    public transient Consumer<ContextUtils.LerpToContext> lerpTo;
    public transient Consumer<ContextUtils.EntityPlayerContext> playerTouch;
    public transient Function<ContextUtils.EntitySqrDistanceContext, Object> shouldRenderAtSqrDistance;
    public transient Consumer<Entity> tick;
    public transient Consumer<ContextUtils.MovementContext> move;
    public transient Boolean isAttackable;

    public BaseEntityBuilder(ResourceLocation i) {
        super(i);
        clientTrackingRange = 5;
        updateInterval = 1;
        mobCategory = MobCategory.MISC;
        width = 0.5f;
        height = 0.5f;
    }

    @Info(value = """
            Sets the hit box of the entity type.
                        
            @param width The width of the entity. Defaults to 0.5.
            @param height The height of the entity. Defaults to 0.5.
                        
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
            Sets the update interval in ticks of the entity. 
            Defaults to 1 tick.
                        
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

    @Info(value = """
            Sets a consumer to handle lerping (linear interpolation) of the entity's position.
                        
            @param consumer Consumer accepting a {@link ContextUtils.LerpToContext} parameter,
                            providing information and control over the lerping process.
                        
            Example usage:
            ```javascript
            entityBuilder.lerpTo(context => {
                // Custom logic for lerping the entity's position
                // Access information about the lerping process using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> lerpTo(Consumer<ContextUtils.LerpToContext> consumer) {
        lerpTo = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to determine whether the entity should render at a squared distance.
                        
            @param function Function accepting a {@link ContextUtils.EntitySqrDistanceContext} parameter,
                             defining the conditions under which the entity should render.
                        
            Example usage:
            ```javascript
            entityBuilder.shouldRenderAtSqrDistance(context => {
                // Custom logic to determine whether the entity should render
                // Access information about the distance using the provided context.
                return true;
            });
            ```
            """)
    public BaseEntityBuilder<T> shouldRenderAtSqrDistance(Function<ContextUtils.EntitySqrDistanceContext, Object> func) {
        shouldRenderAtSqrDistance = func;
        return this;
    }


    @Info(value = """
            Sets whether the entity is attackable or not.
                        
            @param b Boolean value indicating whether the entity is attackable.
                        
            Example usage:
            ```javascript
            entityBuilder.isAttackable(true);
            ```
            """)
    public BaseEntityBuilder<T> isAttackable(boolean b) {
        isAttackable = b;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when a player touches the entity.
            The provided Consumer accepts a {@link ContextUtils.EntityPlayerContext} parameter,
            representing the context of the player's interaction with the entity.
                        
            Example usage:
            ```javascript
            entityBuilder.playerTouch(context => {
                // Custom logic to handle the player's touch interaction with the entity
                // Access information about the interaction using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> playerTouch(Consumer<ContextUtils.EntityPlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed when the entity performs a movement action.
            The provided Consumer accepts a {@link ContextUtils.MovementContext} parameter,
            representing the context of the entity's movement.
                        
            Example usage:
            ```javascript
            entityBuilder.move(context => {
                // Custom logic to handle the entity's movement action
                // Access information about the movement using the provided context.
            });
            ```
            """)
    public BaseEntityBuilder<T> move(Consumer<ContextUtils.MovementContext> consumer) {
        move = consumer;
        return this;
    }


    @Info(value = """
            Sets a callback function to be executed on each tick for the entity.
                        
            @param consumer A Consumer accepting a {@link Entity} parameter, defining the behavior to be executed on each tick.
                        
            Example usage:
            ```javascript
            entityBuilder.tick(entity => {
                // Custom logic to be executed on each tick of the entity.
                // Access information about the entity using the provided parameter.
            });
            ```
            """)
    public BaseEntityBuilder<T> tick(Consumer<Entity> consumer) {
        tick = consumer;
        return this;
    }


    public abstract EntityType.EntityFactory<T> factory();

    @Override
    public RegistryInfo getRegistryType() {
        return RegistryInfo.ENTITY_TYPE;
    }


}
