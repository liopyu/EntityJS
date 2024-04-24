package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.vanilla.BlazeEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.PiglinEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Consumer;
import java.util.function.Function;

public class PiglinJSBuilder extends PathfinderMobBuilder<PiglinEntityJS> {
    public transient Boolean defaultGoals;
    public transient Function<LivingEntity, Object> isConverting;
    public transient Consumer<ContextUtils.EntityServerLevelContext> finishConversion;

    public PiglinJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
    }

    @Info(value = """
            Sets a consumer responsible for spawning an entity after the mob has converted.
                        
            @param finishConversion A Function accepting an entity parameter
                        
            Example usage:
            ```javascript
            mobBuilder.finishConversion(entity => {
                //Convert to a ghast instead of a zombified piglin when in the overworld
                let EntityType = Java.loadClass("net.minecraft.world.entity.EntityType");
                entity.convertTo(EntityType.GHAST, true);
            });
            ```
            """)
    public PiglinJSBuilder finishConversion(Consumer<ContextUtils.EntityServerLevelContext> finishConversion) {
        this.finishConversion = finishConversion;
        return this;
    }

    @Info(value = """
            Sets a function to determine if the entity is converting.
                        
            @param isConverting A Function accepting an entity parameter
                        
            Example usage:
            ```javascript
            mobBuilder.isConverting(entity => {
                return entity.age > 500;
            });
            ```
            """)
    public PiglinJSBuilder isConverting(Function<LivingEntity, Object> isConverting) {
        this.isConverting = isConverting;
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
    public PiglinJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }


    @Override
    public EntityType.EntityFactory<PiglinEntityJS> factory() {
        return (type, level) -> new PiglinEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return PiglinEntityJS.createAttributes();
    }
}