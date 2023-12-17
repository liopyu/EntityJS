package net.liopyu.root;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.ScriptTypePredicate;
import net.liopyu.entityjs.EntityJsBindings;

public class EntityJsRegistry {
    private static EntityJsBindings entityJsBindings;

    public static void registerEvents() {
        // Register the EntityJs event within the 'entityjs' event group
        EventGroup.of("entityjs").add("EntityJs", ScriptType.STARTUP, () -> {
            ScriptManager scriptManager = ScriptType.STARTUP.manager.get();
            BindingsEvent event = new BindingsEvent(scriptManager, scriptManager.topLevelScope);

            // Initialize EntityJsBindings if not already initialized
            if (entityJsBindings == null) {
                entityJsBindings = new EntityJsBindings(event);
            }

            // Add any necessary bindings for EntityJs
            event.add("EntityJs", entityJsBindings);

            // Add additional registrations if needed

            return null;
        });
    }

    public static EntityJsBindings getEntityJsBindings() {
        return entityJsBindings;
    }
}
