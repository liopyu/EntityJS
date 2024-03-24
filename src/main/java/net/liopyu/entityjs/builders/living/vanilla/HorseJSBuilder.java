package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.TameableMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.GoatEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.HorseEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

public class HorseJSBuilder extends AnimalEntityBuilder<HorseEntityJS> {
    public transient Consumer<ContextUtils.PlayerEntityContext> onTamed;
    public transient Consumer<ContextUtils.PlayerEntityContext> tameOverride;
    public transient Boolean defaultGoals;
    public transient Boolean defaultBehaviourGoals;

    public HorseJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
        defaultBehaviourGoals = true;
    }

    @Info(value = """  
            @param defaultBehaviourGoals Sets whether the mob should inherit it's goal behavior from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultBehaviourGoals(false);
            ```
            """)
    public HorseJSBuilder defaultBehaviourGoals(boolean defaultBehaviourGoals) {
        this.defaultBehaviourGoals = defaultBehaviourGoals;
        return this;
    }

    @Info(value = """
            Sets a Consumer invoked after the entity is tamed
            and replaces the logic used to set the UUID of the owner
            with the parameter of ContextUtils.PlayerEntityContext callback
                        
            @param tameOverride A Consumer responsible for determining the uuid to set when the entity is tamed.
                        
            Example usage:
            ```javascript
            builder.tameOverride(context => {
                const {entity,player} = context
                // Mimic the vanilla way of setting the uuid when the entity is tamed.
                entity.setOwnerUUID(player.getUUID());
            });
            ```
            """)
    public HorseJSBuilder tameOverride(Consumer<ContextUtils.PlayerEntityContext> tameOverride) {
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
            builder.onTamed(entity => {
                // Do stuff when the entity is tamed.
            });
            ```
            """)
    public HorseJSBuilder onTamed(Consumer<ContextUtils.PlayerEntityContext> onTamed) {
        this.onTamed = onTamed;
        return this;
    }

    @Info(value = """  
            @param defaultGoals Sets whether the mob should inherit it's goals from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultGoals(false);
            ```
            """)
    public HorseJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }


    @Override
    public EntityType.EntityFactory<HorseEntityJS> factory() {
        return (type, level) -> new HorseEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return MobEntityJS.createMobAttributes()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.LUCK)
                .add(Attributes.MOVEMENT_SPEED);
    }
}
