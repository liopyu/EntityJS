package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.script.BindingsEvent;

public class EntityJsBindings {
    private final BindingsEvent event;

    public EntityJsBindings(BindingsEvent event) {
        this.event = event;
    }

    public void register(EntityJsCallback callback) {
        callback.invoke(new RegisterFunction(event));
    }

    public interface EntityJsCallback {
        void invoke(RegisterFunction registerFunction);
    }

    public static class RegisterFunction {
        private final BindingsEvent event;

        public RegisterFunction(BindingsEvent event) {
            this.event = event;
        }

        public void add(String name, MobType mobType) {
            // Your logic to register the entity
            // Example: EntityJsPlugin.registerEntity(name, mobType);
            System.out.println("Registered entity: " + name + ", MobType: " + mobType);
        }

        // Add more methods as needed for configuring the entity, e.g., size, attributes, etc.
    }
}
