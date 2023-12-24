package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.liopyu.entityjs.entities.BaseEntityJS;

public interface EntityJSEvent {
    EventGroup GROUP = EventGroup.of("EntityJS");

    EventHandler ENTITYREGISTRY = GROUP.startup("register", () -> EntityModificationEventJS.class);
}
