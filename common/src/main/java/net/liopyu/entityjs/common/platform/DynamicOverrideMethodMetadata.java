package net.liopyu.entityjs.common.platform;

import java.util.Map;

/**
 * Stable script-facing identity and parameter metadata for a runtime Minecraft method.
 * Runtime owners, names, and descriptors remain loader-specific and are used only as
 * lookup keys by the platform metadata indexes.
 */
public record DynamicOverrideMethodMetadata(
        String scriptOwner,
        String scriptName,
        String scriptDescriptor,
        Map<Integer, String> parameterNamesBySlot
) {
    public DynamicOverrideMethodMetadata {
        parameterNamesBySlot = Map.copyOf(parameterNamesBySlot);
    }
}
