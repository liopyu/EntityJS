package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.CustomJavaToJsWrappersEvent;
import net.liopyu.root.EntityJsEvent;

public class EntityJsPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        // Register the 'entityjs' event group
        EventGroup.of("entityjs").register();
        // Register the EntityJsEvent within the 'entityjs' event group
        EntityJsEvent.registerEvents();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        // Add any custom Java to JS bindings here
    }

    @Override
    public void registerCustomJavaToJsWrappers(CustomJavaToJsWrappersEvent event) {
        // Add any custom Java to JS wrappers here if needed
    }

    // Other methods...
}
