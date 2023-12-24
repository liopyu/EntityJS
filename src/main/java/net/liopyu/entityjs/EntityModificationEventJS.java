package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.event.EventJS;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.EntityTypeBuilderJS;
import net.liopyu.entityjs.entities.BaseEntityJS;

public class EntityModificationEventJS extends EventJS {

    public EntityModificationEventJS() {

        EntityTypeBuilderJS.Factory<BaseEntityJS> factory = (builder, type, level) -> {
            if (builder instanceof BaseEntityJSBuilder) {
                return new BaseEntityJS((BaseEntityJSBuilder) builder, type, level);
            } else {
                // Handle the case when the builder is not an instance of BaseEntityJSBuilder
                return null; // or throw an exception, depending on your requirements
            }
        };
    }
}
