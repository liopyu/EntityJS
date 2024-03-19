package net.liopyu.entityjs.builders.nonliving.vanilla;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.entities.nonliving.entityjs.IAnimatableJSNL;
import net.liopyu.entityjs.entities.nonliving.vanilla.BoatEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.vehicle.Boat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public abstract class BoatEntityBuilder<T extends Entity & IAnimatableJSNL> extends BaseEntityBuilder<T> {
    public transient Function<Boat, Object> getDropItem;
    public transient float setShadowRadius;
    public static final List<BoatEntityBuilder<?>> thisList = new ArrayList<>();

    public BoatEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        this.setShadowRadius = 0.3F;
    }

    @Info(value = """
            Sets the shadow radius of the entity.
            Defaults to 0.3.
            Example usage:
            ```javascript
            builder.setShadowRadius(0.8);
            ```
            """)
    public BoatEntityBuilder<T> setShadowRadius(float f) {
        this.setShadowRadius = f;
        return this;
    }

    @Info(value = """
            Sets a function to determine the Item the entity drops when it
            turns back into an item.
            Defaults to Boat super method.
            Example usage:
            ```javascript
            builder.getDropItem(entity => {
                // Use information about the entity provided by the context.
                return Item.of('amethyst_block').item // Some Item
            });
            ```
            """)
    public BoatEntityBuilder<T> getDropItem(Function<Boat, Object> function) {
        this.getDropItem = function;
        return this;
    }

}
