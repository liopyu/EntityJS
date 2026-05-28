package net.liopyu.entityjs.builders.living.entityjs;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.living.entityjs.MobEntityJS;
import net.liopyu.entityjs.entities.living.entityjs.WaterEntityJS;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.function.Function;

public class WaterEntityJSBuilder extends PathfinderMobBuilder<WaterEntityJS> {
    public transient Function<LivingEntity, Object> bucketItemStack;
    public transient boolean defaultGoals = true;
    public transient boolean canBeBucketed = false;

    public WaterEntityJSBuilder(Identifier i) {
        super(i);
    }

    @Info(value = """
            Whether or not the fish can be bucketed, if true it is recommended to set the
            bucketItemStack function in the builder otherwise it will give an empty itemstack
            and the bucket will be lost.
            Defaults to false
            Example usage:
            ```javascript
            builder.setCanBeBucketed(true)
            ```
            """)
    public WaterEntityJSBuilder setCanBeBucketed(boolean canBeBucketed) {
        this.canBeBucketed = canBeBucketed;
        return this;
    }

    @Info(value = """
            Whether or not the fish retains default swimming goals.
            Defaults to True
            Example usage:
            ```javascript
            builder.setDefaultGoals(false)
            ```
            """)
    public WaterEntityJSBuilder setDefaultGoals(boolean defaultGoals) {
        this.defaultGoals = defaultGoals;
        return this;
    }

    @Info(value = """
            @param bucketItemStack Function returning the itemstack to receive when bucketed
            Defaults to Empty Itemstack
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
        return WaterEntityJS.createAttributes()
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
