package net.liopyu.entityjs.util.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FollowPlayerBehindGoal extends Goal {
    private final PathfinderMob mob;
    private final LivingEntity target;
    private final PathNavigation navigation;

    public FollowPlayerBehindGoal(PathfinderMob mob, LivingEntity target) {
        this.mob = mob;
        this.target = target;
        this.navigation = mob.getNavigation();
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // Check if the target is valid and the mob is not already close behind
        return target != null && mob.distanceToSqr(target) > 4.0 && mob.hasLineOfSight(target);
    }

    @Override
    public boolean canContinueToUse() {
        // Continue using this goal as long as the target is valid and the mob is not close behind
        return target != null && mob.distanceToSqr(target) > 4.0 && mob.hasLineOfSight(target);
    }

    @Override
    public void start() {
        // Start moving towards a position behind the target
        Vec3 targetPosition = target.position();
        double angle = Math.atan2(mob.getZ() - targetPosition.z, mob.getX() - targetPosition.x);
        double x = targetPosition.x + Math.cos(angle + Math.PI) * 2.0; // Adjust distance as needed
        double z = targetPosition.z + Math.sin(angle + Math.PI) * 2.0; // Adjust distance as needed
        navigation.moveTo(x, targetPosition.y, z, 1.0); // Adjust speed as needed
    }

    @Override
    public void stop() {
        // Stop moving
        navigation.stop();
    }

    @Override
    public void tick() {
        // Update movement or behavior during execution
    }
}
