package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;


public abstract class ArrowEntityBuilder<T extends AbstractArrow & IArrowEntityJS> extends BaseEntityBuilder<T> {
    public static final List<ArrowEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Function<T, ResourceLocation> textureLocation;
    public transient ResourceLocation setSoundEvent;
    public transient Consumer<AbstractArrow> tickDespawn;
    public transient Consumer<ContextUtils.ArrowEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ArrowBlockHitContext> onHitBlock;
    public transient ResourceLocation defaultHitGroundSoundEvent;
    public transient Consumer<ContextUtils.ArrowLivingEntityContext> doPostHurtEffects;
    public transient Function<ContextUtils.ArrowVec3Context, EntityHitResult> findHitEntity;
    public transient Function<Entity, Object> canHitEntity;
    public transient Function<Player, Object> tryPickup;
    public transient DoubleConsumer setBaseDamage;
    public transient IntConsumer setKnockback;
    public transient Float setWaterInertia;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        textureLocation = t -> t.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    //Arrow Overrides
    @Info(value = """
            Sets a function to determine if a player can pick up the arrow entity.
                        
            @param tryPickup The function to check if a player can pick up the arrow.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.tryPickup(player => {
                // Custom logic to determine if the player can pick up the arrow
                // Return true if the player can pick up, false otherwise.
            });
            ```
            """)
    public ArrowEntityBuilder<T> tryPickup(Function<Player, Object> function) {
        tryPickup = function;
        return this;
    }


    @Info(value = """
            Sets the base damage value for the arrow entity.
                        
            @param baseDamage The base damage value to be set.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setBaseDamage(8.0);
            ```
            """)
    public ArrowEntityBuilder<T> setBaseDamage(DoubleConsumer baseDamage) {
        setBaseDamage = baseDamage;
        return this;
    }


    @Info(value = """
            Sets the knockback value for the arrow entity.
                        
            @param setKnockback The knockback value to be set.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setKnockback(2);
            ```
            """)
    public ArrowEntityBuilder<T> setKnockback(IntConsumer knockback) {
        setKnockback = knockback;
        return this;
    }


    @Info(value = """
            Sets the water inertia value for the arrow entity.
                        
            @param setWaterInertia The water inertia value to be set.
            Defaults to 0.6 for AbstractArrow
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setWaterInertia(0.5);
            ```
            """)
    public ArrowEntityBuilder<T> setWaterInertia(Float waterInertia) {
        setWaterInertia = waterInertia;
        return this;
    }

    @Info(value = """
            Sets a consumer to perform additional effects after the arrow successfully hurts a living entity.
                        
            @param doPostHurtEffects The consumer to perform additional effects.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.doPostHurtEffects(context => {
                // Custom logic to perform additional effects after the arrow hurts a living entity.
            });
            ```
            """)
    public ArrowEntityBuilder<T> doPostHurtEffects(Consumer<ContextUtils.ArrowLivingEntityContext> consumer) {
        doPostHurtEffects = consumer;
        return this;
    }

    @Info(value = """
            Sets a function to find the entity hit by the arrow based on a context containing start and end vectors.
                        
            @param findHitEntity The function to find the hit entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.findHitEntity(context => {
                // Custom logic to find the entity hit by the arrow based on the context
                // ContextUtils.ArrowVec3Context provides start and end vectors.
                // Return an EntityHitResult containing the hit entity or null if no entity is hit.
            });
            ```
            """)
    public ArrowEntityBuilder<T> findHitEntity(Function<ContextUtils.ArrowVec3Context, EntityHitResult> function) {
        findHitEntity = function;
        return this;
    }

    @Info(value = """
            Sets the sound event for the arrow entity using a resource location.
                        
            @param setSoundEvent A resource location representing the sound event.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setSoundEvent("minecraft:entity.arrow.shoot");
            ```
            """)
    public ArrowEntityBuilder<T> setSoundEvent(Object sound) {
        if (sound instanceof String) setSoundEvent = new ResourceLocation((String) sound);
        else if (sound instanceof ResourceLocation) setSoundEvent = (ResourceLocation) sound;
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setSoundEvent. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.arrow.shoot\"");
        return this;
    }

    @Info(value = """
            Sets the default sound event played when the arrow hits the ground using a string representation.
                        
            @param defaultHitGroundSoundEvent A string representing the ResourceLocation of the sound event.
                        
            Example usage:
            ```javascript
            // Example to set a custom sound event for the arrow hitting the ground.
            arrowEntityBuilder.defaultHitGroundSoundEvent("minecraft:entity.arrow.hit");
            ```
            """)
    public ArrowEntityBuilder<T> defaultHitGroundSoundEvent(Object sound) {
        if (sound instanceof String) defaultHitGroundSoundEvent = new ResourceLocation((String) sound);
        else if (sound instanceof ResourceLocation) defaultHitGroundSoundEvent = (ResourceLocation) sound;
        else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for defaultHitGroundSoundEvent. Value: " + sound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.arrow.hit\"");
        return this;
    }

    @Info(value = """
            Sets a consumer to be called during each tick to handle arrow entity despawn logic.
                        
            @param tickDespawn The consumer to handle the arrow entity tick despawn logic.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.tickDespawn(arrow => {
                // Custom logic to handle arrow entity despawn during each tick
            });
            ```
            """)
    public ArrowEntityBuilder<T> tickDespawn(Consumer<AbstractArrow> consumer) {
        tickDespawn = consumer;
        return this;
    }

    //Projectile Overrides
    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/entity/projectiles/<path>.png.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.textureResource(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public ArrowEntityBuilder<T> textureLocation(Function<T, Object> function) {
        textureLocation = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logErrorMessageOnce("Invalid texture resource in arrow builder: " + obj + "Defaulting to " + entity.getArrowBuilder().newID("textures/entity/projectiles/", ".png"));
                return entity.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
            }
        };
        return this;
    }

    @Info(value = """
            Sets a consumer to be called when the arrow entity hits another entity.
                        
            @param onHitEntity The consumer to handle the arrow entity hit context.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onHitEntity(context => {
                // Custom logic to handle the arrow hitting another entity
            });
            ```
            """)
    public ArrowEntityBuilder<T> onHitEntity(Consumer<ContextUtils.ArrowEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }

    @Info(value = """
            Sets a consumer to be called when the arrow entity hits a block.
                        
            @param onHitBlock The consumer to handle the arrow block hit context.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onHitBlock(context => {
                // Custom logic to handle the arrow hitting a block
            });
            ```
            """)
    public ArrowEntityBuilder<T> onHitBlock(Consumer<ContextUtils.ArrowBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the arrow entity can hit a specific entity.
                        
            @param canHitEntity Function to check if the arrow can hit the entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.canHitEntity(entity => {
                // Custom logic to determine if the arrow can hit the specified entity
                // Return true if the arrow can hit, false otherwise.
            });
            ```
            """)
    public ArrowEntityBuilder<T> canHitEntity(Function<Entity, Object> function) {
        canHitEntity = function;
        return this;
    }
}

