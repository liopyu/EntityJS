package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.BatEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.CamelEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Consumer;

public class CamelJSBuilder extends AnimalEntityBuilder<CamelEntityJS> {
    public transient boolean defaultGoals;
    public transient Consumer<ContextUtils.PlayerEntityContext> onTamed;
    public transient Consumer<ContextUtils.PlayerEntityContext> tameOverride;

    public CamelJSBuilder(ResourceLocation i) {
        super(i);
        this.defaultGoals = true;
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
    public CamelJSBuilder tameOverride(Consumer<ContextUtils.PlayerEntityContext> tameOverride) {
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
    public CamelJSBuilder onTamed(Consumer<ContextUtils.PlayerEntityContext> onTamed) {
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
    public CamelJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<CamelEntityJS> factory() {
        return (type, level) -> new CamelEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return CamelEntityJS.createAttributes();
    }
}
