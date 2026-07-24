package net.liopyu.entityjs.common.util.overrides;

import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.util.HideFromJS;

/**
 * Common hook for script-visible callback contexts that need their Rhino scope
 * attached by the cached direct-call path.
 */
public interface ScriptableCallbackContext extends Scriptable {
    @HideFromJS
    void entityJs$attachScriptScope(Scriptable parentScope, Scriptable objectPrototype, Scriptable functionPrototype);
}
