package net.liopyu.entityjs.builders.living.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.entityjs.MobBuilder;
import net.liopyu.entityjs.builders.living.entityjs.PathfinderMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.EvokerEntityJS;
import net.liopyu.entityjs.entities.living.vanilla.IllusionerEntityJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class IllusionerJSBuilder extends PathfinderMobBuilder<IllusionerEntityJS> {
    public transient Boolean defaultGoals;
    public transient Object setCelebrateSound;

    public IllusionerJSBuilder(ResourceLocation i) {
        super(i);
        defaultGoals = true;
    }

    @Info(value = """
            Sets the sound to play when the entity is celebrating using either a string representation or a ResourceLocation object.
                        
            Example usage:
            ```javascript
            mobBuilder.setCelebrateSound("minecraft:entity.zombie.ambient");
            ```
            """)
    public IllusionerJSBuilder setCelebrateSound(Object ambientSound) {
        if (ambientSound instanceof String) {
            this.setCelebrateSound = new ResourceLocation((String) ambientSound);
        } else if (ambientSound instanceof ResourceLocation resourceLocation) {
            this.setCelebrateSound = resourceLocation;
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for setCelebrateSound. Value: " + ambientSound + ". Must be a ResourceLocation or String. Example: \"minecraft:entity.zombie.ambient\"");
            this.setCelebrateSound = null;
        }
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
    public IllusionerJSBuilder defaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }


    @Override
    public EntityType.EntityFactory<IllusionerEntityJS> factory() {
        return (type, level) -> new IllusionerEntityJS(this, type, level);
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
