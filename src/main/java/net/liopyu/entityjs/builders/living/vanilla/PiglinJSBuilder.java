package net.liopyu.entityjs.builders.living.vanilla;

import net.liopyu.entityjs.util.BooleanCallback;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.vanilla.BlazeEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.PiglinEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

import java.util.function.Consumer;
import java.util.function.Function;

public class PiglinJSBuilder extends PathfinderMobBuilder<PiglinEntityJS> {
    public transient Boolean defaultGoals;
    public transient BooleanCallback<LivingEntity> isConverting;
    public transient Consumer<ContextUtils.EntityServerLevelContext> finishConversion;
    public transient EntityType<? extends Mob> conversionType;

    public PiglinJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
    }

    @Info(value = """
            Overrides the mob's conversion behavior. When set, vanilla conversion is not called automatically.
                        
            @param finishConversion A Function accepting an entity parameter
                        
            Example usage:
            ```javascript
            mobBuilder.finishConversion(context => {
                //Convert to a ghast instead of a zombified piglin when in the overworld
                let EntityType = Java.loadClass("net.minecraft.world.entity.EntityType");
                context.entity.convertTo(EntityType.GHAST, true);
            });
            ```
            """)
    public PiglinJSBuilder finishConversion(Consumer<ContextUtils.EntityServerLevelContext> finishConversion) {
        this.finishConversion = finishConversion;
        return this;
    }

    @Info(value = """
            Sets what mob the entity should convert to when zombifying.
            Defaults to "minecraft:zombified_piglin".

            Example usage:
            ```javascript
            builder.setConversionType("minecraft:chicken");
            ```
            """)
    public PiglinJSBuilder setConversionType(EntityType<? extends Mob> conversionType) {
        this.conversionType = conversionType;
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
    public PiglinJSBuilder isConverting(BooleanCallback<LivingEntity> isConverting) {
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
