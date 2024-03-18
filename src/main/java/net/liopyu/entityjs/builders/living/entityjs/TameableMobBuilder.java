package net.liopyu.entityjs.builders.living.entityjs;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TameableMobBuilder<T extends TamableAnimal & IAnimatableJS> extends AnimalEntityBuilder<T> {
    public transient Ingredient tamableFood;
    public transient Function<ContextUtils.EntityItemStackContext, Object> tamableFoodPredicate;
    public transient Consumer<ContextUtils.PlayerEntityContext> onTamed;
    public transient Consumer<ContextUtils.PlayerEntityContext> tameOverride;

    public TameableMobBuilder(ResourceLocation i) {
        super(i);
    }

    @Info(value = """
            Sets a Consumer invoked after the entity is tamed
            and replaces the logic used to set the UUID of the owner
            with the parameter of ContextUtils.PlayerEntityContext callback
                        
            @param tameOverride A Consumer responsible for determining the uuid to set when the entity is tamed.
                        
            Example usage:
            ```javascript
            mobBuilder.tameOverride(context => {
                const {entity,player} = context
                // Mimic the vanilla way of setting the uuid when the entity is tamed.
                entity.setOwnerUUID(player.getUUID());
            });
            ```
            """)
    public MobBuilder<T> tameOverride(Consumer<ContextUtils.PlayerEntityContext> tameOverride) {
        this.tameOverride = tameOverride;
        return this;
    }

    @Info(value = """
            Sets a Consumer with the parameter of ContextUtils.PlayerEntityContext callback
            This is fired after the entity is tamed and all tame logic has already taken place.
            Useful if you don't want to mess with the UUID logic in the tameOverride method.
                        
            @param onTamed A Consumer that fires when the entity is tamed.
                        
            Example usage:
            ```javascript
            mobBuilder.onTamed(entity => {
                // Do stuff when the entity is tamed.
            });
            ```
            """)
    public MobBuilder<T> onTamed(Consumer<ContextUtils.PlayerEntityContext> onTamed) {
        this.onTamed = onTamed;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the player's current itemstack will tame the mob.
                        
            @param tamableFoodPredicate A Function accepting a ContextUtils.EntityItemStackContext parameter
                        
            Example usage:
            ```javascript
            mobBuilder.tamableFood([
                'minecraft:diamond',
                'minecraft:wheat'
            ]);
            ```
            """)
    public MobBuilder<T> tamableFood(Ingredient tamableFood) {
        this.tamableFood = tamableFood;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the player's current itemstack will tame the mob.
                        
            @param tamableFoodPredicate A Function accepting a ContextUtils.EntityItemStackContext parameter
                        
            Example usage:
            ```javascript
            mobBuilder.tamableFoodPredicate(context => {
                const { entity, item } = context
                return item.id == 'minecraft:diamond' // Return true if the player's current itemstack will tame the mob.
            });
            ```
            """)
    public MobBuilder<T> tamableFoodPredicate(Function<ContextUtils.EntityItemStackContext, Object> tamableFoodPredicate) {
        this.tamableFoodPredicate = tamableFoodPredicate;
        return this;
    }
}