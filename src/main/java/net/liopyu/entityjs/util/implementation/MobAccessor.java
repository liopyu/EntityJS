package net.liopyu.entityjs.util.implementation;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

@RemapPrefixForJS("entityJs$")
public interface MobAccessor {
    void entityJs$setNavigation(PathNavigation nav);

    void entityJs$setMoveControl(MoveControl control);
}