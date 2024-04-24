package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.vanilla.BlazeEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.WitherEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

public class WitherJSBuilder extends PathfinderMobBuilder<WitherEntityJS> {
    public transient Boolean defaultGoals;
    public transient String attackProjectile;
    public transient boolean customServerAiStep;

    public WitherJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
        customServerAiStep = true;
    }

    @Info(value = """  
            @param attackProjectile Sets the projectile shot by the wither.
            Defaults to a wither skull.
                        
            Example usage:
            ```javascript
            builder.attackProjectile("minecraft:arrow");
            ```
            """)
    public WitherJSBuilder attackProjectile(String attackProjectile) {
        this.attackProjectile = attackProjectile;
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
    public WitherJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Info(value = """  
            @param customServerAiStep Sets whether the mob has its default custom server ai step behavior
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.customServerAiStep(false);
            ```
            """)
    public WitherJSBuilder customServerAiStep(boolean customServerAiStep) {
        this.customServerAiStep = customServerAiStep;
        return this;
    }


    @Override
    public EntityType.EntityFactory<WitherEntityJS> factory() {
        return (type, level) -> new WitherEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return WitherEntityJS.createAttributes();
    }
}
