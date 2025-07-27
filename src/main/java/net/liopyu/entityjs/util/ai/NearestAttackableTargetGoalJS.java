package net.liopyu.entityjs.util.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class NearestAttackableTargetGoalJS<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    private final AABB radius;

    public NearestAttackableTargetGoalJS(Mob pMob,
                                         Class<T> pTargetType,
                                         int pRandomInterval,
                                         boolean pMustSee,
                                         boolean pMustReach,
                                         @Nullable Predicate<LivingEntity> pTargetPredicate,
                                         AABB radius) {
        super(pMob, pTargetType, pRandomInterval, pMustSee, pMustReach, pTargetPredicate);
        this.radius = radius;
    }

    @Override
    protected AABB getTargetSearchArea(double pTargetDistance) {
        return radius.move(mob.position());
    }
}