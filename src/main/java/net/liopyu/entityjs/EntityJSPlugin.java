package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.EntityTypeBuilderJS;

public class EntityJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        RegistryInfo.ENTITY_TYPE.addType("monster", BaseEntityJSBuilder.class, BaseEntityJSBuilder::new);
    }
}
