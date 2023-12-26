package net.liopyu.entityjs.kube;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.BaseEntityBuilder;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.builders.EntityTypeBuilderJS;
import net.liopyu.entityjs.entities.BaseEntityJS;
import net.minecraft.resources.ResourceLocation;

import static org.apache.commons.lang3.function.Failable.test;

public class EntityModificationEventJS extends EventJS {
    @Info("Registers an entity type.")
    public static void build(ResourceLocation i, EntityTypeBuilderJS.Factory<BaseEntityJS> factory) {
        var builder = new BaseEntityJSBuilder(i);
    }

}
