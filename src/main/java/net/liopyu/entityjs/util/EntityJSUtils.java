package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface EntityJSUtils {
    @Info("Ground entity path navigation")
    static GroundPathNavigation createGroundPathNavigation(Mob pMob, Level pLevel) {
        return new GroundPathNavigation(pMob, pLevel);
    }

    @Info("Flying entity path navigation")
    static FlyingPathNavigation createFlyingPathNavigation(Mob pMob, Level pLevel) {
        return new FlyingPathNavigation(pMob, pLevel);
    }

    @Info("Amphibious entity path navigation")
    static AmphibiousPathNavigation createAmphibiousPathNavigation(Mob pMob, Level pLevel) {
        return new AmphibiousPathNavigation(pMob, pLevel);
    }

    @Info("Wall climbing entity path navigation")
    static WallClimberNavigation createWallClimberNavigation(Mob pMob, Level pLevel) {
        return new WallClimberNavigation(pMob, pLevel);
    }

    @Info("Water bound entity path navigation")
    static WaterBoundPathNavigation createWaterBoundPathNavigation(Mob pMob, Level pLevel) {
        return new WaterBoundPathNavigation(pMob, pLevel);
    }
}