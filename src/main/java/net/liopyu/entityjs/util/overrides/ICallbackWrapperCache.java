package net.liopyu.entityjs.util.overrides;

import dev.latvian.mods.rhino.util.HideFromJS;

public interface ICallbackWrapperCache {
    @HideFromJS
    Object entityJs$getCachedCallbackWrapper(Object key);

    @HideFromJS
    void entityJs$putCachedCallbackWrapper(Object key, Object wrapper);
}
