package net.liopyu.entityjs.util;

import dev.latvian.mods.rhino.util.RemapPrefixForJS;
import net.minecraft.world.entity.ai.goal.GoalSelector;

@RemapPrefixForJS("entityJS$")
public interface IMobMethods {
    GoalSelector entityJS$getGoalSelector();

    GoalSelector entityJS$getTargetSelector();
}
