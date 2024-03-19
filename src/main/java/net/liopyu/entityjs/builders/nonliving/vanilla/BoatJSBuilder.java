package net.liopyu.entityjs.builders.nonliving.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.vanilla.BoatEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.function.Function;

public class BoatJSBuilder extends BaseEntityBuilder<BoatEntityJS> {
    public transient Function<Boat, Object> getItem;

    public BoatJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Info(value = """
            Sets a function to determine the Item the entity drops when it
            turns back into an item.
            Defaults to Boat super method.
            Example usage:
            ```javascript
            builder.getItem(entity => {
                // Use information about the entity provided by the context.
                return Item.of('amethyst_block').item // Some Item
            });
            ```
            """)
    public BoatJSBuilder getItem(Function<Boat, Object> function) {
        this.getItem = function;
        return this;
    }

    @Override
    public EntityType.EntityFactory<BoatEntityJS> factory() {
        return (type, level) -> new BoatEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return null;
    }
}
