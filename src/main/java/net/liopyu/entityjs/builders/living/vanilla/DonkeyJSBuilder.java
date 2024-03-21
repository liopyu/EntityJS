package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.AnimalEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.DolphinEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.DonkeyEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class DonkeyJSBuilder extends AnimalEntityBuilder<DonkeyEntityJS> {
    public transient boolean defaultGoals;

    public DonkeyJSBuilder(ResourceLocation i) {
        super(i);
        this.defaultGoals = true;
    }

    @Info(value = """  
            @param defaultGoals Sets whether the mob should inherit it's goals from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultGoals(false);
            ```
            """)
    public DonkeyJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<DonkeyEntityJS> factory() {
        return (type, level) -> new DonkeyEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return DonkeyEntityJS.createBaseHorseAttributes();
    }
}
