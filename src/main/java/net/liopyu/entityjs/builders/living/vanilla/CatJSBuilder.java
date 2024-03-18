package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.TameableMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.BatEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.CatEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CatJSBuilder extends TameableMobBuilder<CatEntityJS> {
    public transient Boolean defaultGoals;

    public CatJSBuilder(ResourceLocation i) {
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
    public CatJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<CatEntityJS> factory() {
        return (type, level) -> new CatEntityJS(this, type, level);
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
