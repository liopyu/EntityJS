package net.liopyu.entityjs.util;

import net.liopyu.entityjs.builders.living.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;

public interface IModifyEntityJS {
    ModifyEntityBuilder entityJs$getBuilder();
}
