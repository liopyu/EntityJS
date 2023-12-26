package net.liopyu.entityjs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.liopyu.entityjs.kube.EntityJSEvent;

public class EntityJSPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        EntityJSEvent.GROUP.register();
    }

}
