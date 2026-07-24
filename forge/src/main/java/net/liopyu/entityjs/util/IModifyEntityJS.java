package net.liopyu.entityjs.util;

import net.liopyu.entityjs.common.util.*;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;

@RemapPrefixForJS("entityJs$")
public interface IModifyEntityJS {
    ModifyEntityBuilder entityJs$getBuilder();
}
