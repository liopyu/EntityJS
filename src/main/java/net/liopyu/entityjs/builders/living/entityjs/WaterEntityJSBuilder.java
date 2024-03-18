package net.liopyu.entityjs.builders.living.entityjs;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.entityjs.WaterEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Function;

public class WaterEntityJSBuilder extends PathfinderMobBuilder<WaterEntityJS> {
    public transient Function<LivingEntity, Object> bucketItemStack;

    public WaterEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Info(value = """
            @param bucketItemStack Function returning the itemstack to receive when bucketed
            Defaults to Axolotl Bucket
            Example usage:
            ```javascript
            builder.bucketItemStack(entity => {
                // Use information about the entity to return an ItemStack.
                return Item.of('minecraft:diamond')
            })
            ```
            """)
    public WaterEntityJSBuilder bucketItemStack(Function<LivingEntity, Object> function) {
        this.bucketItemStack = function;
        return this;
    }

    @Override
    public EntityType.EntityFactory<WaterEntityJS> factory() {
        return (type, level) -> new WaterEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return MobEntityJS.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 3)
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
