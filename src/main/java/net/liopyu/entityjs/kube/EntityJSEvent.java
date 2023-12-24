package net.liopyu.entityjs.kube;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface EntityJSEvent {
    EventGroup GROUP = EventGroup.of("EntityJS");

    EventHandler ENTITYREGISTRY = GROUP.startup("register", () -> EntityModificationEventJS.class);
}
