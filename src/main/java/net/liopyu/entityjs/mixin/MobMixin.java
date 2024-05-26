package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.util.IMobMethods;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Mob.class)
public abstract class MobMixin implements IMobMethods {
    @Unique
    private Object mobObject = this;

    @Unique
    private Mob getMob() {
        return (Mob) mobObject;
    }

    @Unique
    public GoalSelector entityJS$getGoalSelector() {
        return getMob().goalSelector;
    }

    @Unique
    public GoalSelector entityJS$getTargetSelector() {
        return getMob().targetSelector;
    }
    /*@Inject(method = "registerGoals", at = @At("HEAD"))
    private void entityjs$onRegisterGoals(CallbackInfo ci) {
        if (EventHandlers.addGoalTargets.hasListeners()) {
            EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(getMob(), getMob().targetSelector), getMob().getType());
        }
        if (EventHandlers.addGoalSelectors.hasListeners()) {
            EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(getMob(), getMob().goalSelector), getMob().getType());
        }
    }*/
}