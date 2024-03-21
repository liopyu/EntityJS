package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.BeeEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.ChickenEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Function;

public class ChickenJSBuilder extends AnimalEntityBuilder<ChickenEntityJS> {
    public transient Boolean defaultGoals;
    public transient Function<LivingEntity, Object> eggTime;

    public ChickenJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
    }

    @Info(value = """  
            @param defaultGoals Sets whether the mob should inherit it's goals from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultGoals(false);
            ```
            """)
    public ChickenJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Info(value = """ 
            @param eggTime Sets a function to determine the laying egg time of the entity
                        
            Example usage:
            ```javascript
            mobBuilder.eggTime(entity => {
                return 100 // returning 100 here will result in the entity laying an egg every 100 ticks
            });
            ```
            """)
    public ChickenJSBuilder eggTime(Function<LivingEntity, Object> eggTime) {
        this.eggTime = eggTime;
        return this;
    }

    @Override
    public EntityType.EntityFactory<ChickenEntityJS> factory() {
        return (type, level) -> new ChickenEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return ChickenEntityJS.createAttributes();
    }
}

