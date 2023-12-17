package net.liopyu.entityjs;

import net.liopyu.root.EntityJsRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod(EntityJs.MOD_ID)
public class EntityJs {
    public static final String MOD_ID = "entityjs";

    // Call this method during mod initialization
    public static void init() {
        EntityJsRegistry.registerEvents();
    }

    // Method to register entities using a more concise syntax
    public static void register(EntityJsBindings.EntityJsCallback callback) {
        EntityJsRegistry.getEntityJsBindings().register(callback);
    }

    // Example of additional methods or logic...

    // Your other methods or logic can go here...
}
