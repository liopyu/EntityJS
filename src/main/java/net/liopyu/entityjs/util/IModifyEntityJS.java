package net.liopyu.entityjs.util;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.liopyu.entityjs.builders.living.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.TestModifyEntityBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;

@RemapPrefixForJS("entityJs$")
public interface IModifyEntityJS {
    ModifyEntityBuilder entityJs$getBuilder();
}
