package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.ZombieEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;


public class ZombieJSBuilder extends MobBuilder<ZombieEntityJS> {
    public transient Boolean defaultBehaviourGoals;
    public transient Boolean defaultGoals;

    public ZombieJSBuilder(ResourceLocation i) {
        super(i);
        defaultBehaviourGoals = true;
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
    public ZombieJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Info(value = """  
            @param defaultBehaviourGoals Sets whether the mob should inherit it's goal behavior from it's superclass
            Defaults to true.
                        
            Example usage:
            ```javascript
            builder.defaultBehaviourGoals(false);
            ```
            """)
    public ZombieJSBuilder defaultBehaviourGoals(boolean defaultBehaviourGoals) {
        this.defaultBehaviourGoals = defaultBehaviourGoals;
        return this;
    }

    @Override
    public EntityType.EntityFactory<ZombieEntityJS> factory() {
        return (type, level) -> new ZombieEntityJS(this, type, level);
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
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(Attributes.MOVEMENT_SPEED);
    }
}
