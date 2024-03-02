package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.entities.ArrowEntityJS;
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
    public transient Consumer<AbstractArrow> tickDespawn;
    public transient Consumer<ContextUtils.ArrowEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ArrowBlockHitContext> onHitBlock;
    public transient Object defaultHitGroundSoundEvent;
    public transient Consumer<ContextUtils.ArrowLivingEntityContext> doPostHurtEffects;
    public transient Function<Entity, Object> canHitEntity;
    public transient Function<ContextUtils.ArrowPlayerContext, Object> tryPickup;
    public transient double setBaseDamage;
    public transient Function<Entity, Object> setDamageFunction;
    public transient Integer setKnockback;
    public transient Float setWaterInertia;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        textureLocation = t -> t.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
        setBaseDamage = 2;
        setKnockback = 1;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

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
                return "kubejs:textures/entity/projectiles/arrow.png" // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public ArrowEntityBuilder<T> textureLocation(Function<T, Object> function) {
        textureLocation = entity -> {
            Object obj = function.apply(entity);
            if (obj instanceof String && !obj.toString().equals("undefined")) {
                return new ResourceLocation((String) obj);
            } else if (obj instanceof ResourceLocation) {
                return (ResourceLocation) obj;
            } else {
                EntityJSHelperClass.logWarningMessageOnce("Invalid texture resource in arrow builder: " + obj + "Defaulting to " + entity.getArrowBuilder().newID("textures/entity/projectiles/", ".png"));
                return entity.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
            }
        };
        return this;
    }

    //Arrow Overrides
    @Info(value = """
            Sets a function to determine if a player can pick up the arrow entity.
                        
            @param tryPickup The function to check if a player can pick up the arrow.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.tryPickup(context => {
                // Custom logic to determine if the player can pick up the arrow
                // Return true if the player can pick up, false otherwise.
            });
            ```
            """)
    public ArrowEntityBuilder<T> tryPickup(Function<ContextUtils.ArrowPlayerContext, Object> function) {
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
    public ArrowEntityBuilder<T> setBaseDamage(double baseDamage) {
        setBaseDamage = baseDamage;
        return this;
    }

    @Info(value = """
            Sets the base damage value with a function for the arrow entity for more control.
                        
            @param setDamageFunction Function which returns a double.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setBaseDamage(entity => {
                return 10; //Some double based off entity context.
            });
            ```
            """)
    public ArrowEntityBuilder<T> setDamageFunction(Function<Entity, Object> baseDamage) {
        setDamageFunction = baseDamage;
        return this;
    }


    @Info(value = """
            Sets the knockback value for the arrow entity when a bow has Punch Enchantment.
                        
            @param setKnockback The knockback value of the Punch Enchantment to be set.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setKnockback(2);
            ```
            """)
    public ArrowEntityBuilder<T> setKnockback(int knockback) {
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
    public ArrowEntityBuilder<T> setWaterInertia(float waterInertia) {
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
