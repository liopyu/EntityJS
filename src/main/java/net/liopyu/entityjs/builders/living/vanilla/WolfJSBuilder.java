package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.TameableMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.WolfEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.ZombieEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class WolfJSBuilder extends TameableMobBuilder<WolfEntityJS> {
    public transient Boolean defaultGoals;

    public WolfJSBuilder(ResourceLocation i) {
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
    public WolfJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<WolfEntityJS> factory() {
        return (type, level) -> new WolfEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return WolfEntityJS.createAttributes();
    }
}