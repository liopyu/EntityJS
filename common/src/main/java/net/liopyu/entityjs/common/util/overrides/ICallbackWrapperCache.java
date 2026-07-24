package net.liopyu.entityjs.common.util.overrides;

import dev.latvian.mods.rhino.util.HideFromJS;

public interface ICallbackWrapperCache {
    @HideFromJS
    Object entityJs$getCachedCallbackWrapper(Object key);

    @HideFromJS
    void entityJs$putCachedCallbackWrapper(Object key, Object wrapper);
}
