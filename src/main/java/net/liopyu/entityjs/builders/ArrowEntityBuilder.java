package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
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

    public transient Consumer<LivingEntity> doPostHurtEffects;
    public transient Function<ContextUtils.ArrowVec3Context, EntityHitResult> findHitEntity;
    public transient Predicate<Entity> canHitEntity;
    public transient Predicate<Player> tryPickup;
    public transient DoubleConsumer setBaseDamage;
    public transient IntConsumer setKnockback;
    public transient Float setWaterInertia;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        textureLocation = t -> t.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
    }

    @Info(value = """
            Sets a function to determine the texture resource for the entity.
            The provided Function accepts a parameter of type T (the entity),
            allowing changing the texture based on information about the entity.
            The default behavior returns <namespace>:textures/model/entity/<path>.png.
                        
            Example usage:
            ```javascript
            entityBuilder.textureLocation(entity => {
                // Define logic to determine the texture resource for the entity
                // Use information about the entity provided by the context.
                return // Some ResourceLocation representing the texture resource;
            });
            ```
            """)
    public BaseEntityBuilder<T> textureLocation(Function<T, ResourceLocation> textureCallback) {
        textureLocation = textureCallback;
        return this;
    }


    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    @Info(value = """
            Sets the sound event for the arrow entity using a string representation.
                        
            @param soundEventString A string representing the sound event.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.setSoundEvent("minecraft:entity.arrow.shoot");
            ```
            """)
    public ArrowEntityBuilder<T> setSoundEvent(String soundEventString) {
        setSoundEvent = new ResourceLocation(soundEventString);
        return this;
    }


    @Info(value = """
            Sets a consumer to be called during each tick to handle arrow entity despawn logic.
                        
            @param consumer The consumer to handle the arrow entity tick despawn logic.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.tickDespawn(arrow -> {
                // Custom logic to handle arrow entity despawn during each tick
            });
            ```
            """)
    public ArrowEntityBuilder<T> tickDespawn(Consumer<AbstractArrow> consumer) {
        tickDespawn = consumer;
        return this;
    }


    @Info(value = """
            Sets a consumer to be called when the arrow entity hits another entity.
                        
            @param consumer The consumer to handle the arrow entity hit context.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onHitEntity(context -> {
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
                        
            @param consumer The consumer to handle the arrow block hit context.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.onHitBlock(context -> {
                // Custom logic to handle the arrow hitting a block
            });
            ```
            """)
    public ArrowEntityBuilder<T> onHitBlock(Consumer<ContextUtils.ArrowBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }


    @Info(value = """
            Sets the default sound event played when the arrow hits the ground using a string representation.
                        
            @param soundEventString A string representing the ResourceLocation of the sound event.
                        
            Example usage:
            ```javascript
            // Example to set a custom sound event for the arrow hitting the ground.
            arrowEntityBuilder.defaultHitGroundSoundEvent("minecraft:entity.arrow.hit");
            ```
            """)
    public ArrowEntityBuilder<T> defaultHitGroundSoundEvent(String soundEventString) {
        defaultHitGroundSoundEvent = new ResourceLocation(soundEventString);
        return this;
    }


    @Info(value = """
            Sets a consumer to perform additional effects after the arrow successfully hurts a living entity.
                        
            @param consumer The consumer to perform additional effects.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.doPostHurtEffects(livingEntity -> {
                // Custom logic to perform additional effects after the arrow hurts a living entity.
            });
            ```
            """)
    public ArrowEntityBuilder<T> doPostHurtEffects(Consumer<LivingEntity> consumer) {
        doPostHurtEffects = consumer;
        return this;
    }


    @Info(value = """
            Sets a function to find the entity hit by the arrow based on a context containing start and end vectors.
                        
            @param function The function to find the hit entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.findHitEntity(context -> {
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
            Sets a predicate to determine if the arrow entity can hit a specific entity.
                        
            @param predicate The predicate to check if the arrow can hit the entity.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.canHitEntity(entity -> {
                // Custom logic to determine if the arrow can hit the specified entity
                // Return true if the arrow can hit, false otherwise.
            });
            ```
            """)
    public ArrowEntityBuilder<T> canHitEntity(Predicate<Entity> predicate) {
        canHitEntity = predicate;
        return this;
    }


    @Info(value = """
            Sets a predicate to determine if a player can pick up the arrow entity.
                        
            @param predicate The predicate to check if a player can pick up the arrow.
                        
            Example usage:
            ```javascript
            arrowEntityBuilder.tryPickup(player -> {
                // Custom logic to determine if the player can pick up the arrow
                // Return true if the player can pick up, false otherwise.
            });
            ```
            """)
    public ArrowEntityBuilder<T> tryPickup(Predicate<Player> predicate) {
        tryPickup = predicate;
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
                        
            @param knockback The knockback value to be set.
                        
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
                        
            @param waterInertia The water inertia value to be set.
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


}

