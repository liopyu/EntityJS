package net.liopyu.entityjs.util;

import net.liopyu.entityjs.builders.living.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;

public interface IModifyEntityJS {
    EntityModificationEventJS entityJs$getBuilder();
}
