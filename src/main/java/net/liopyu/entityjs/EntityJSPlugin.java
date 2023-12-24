package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;

public class EntityJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        EntityJSEvent.GROUP.register();
    }
}
