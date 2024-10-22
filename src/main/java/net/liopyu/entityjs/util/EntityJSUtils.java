package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.misc.JumpControlJSBuilder;
import net.liopyu.entityjs.builders.misc.LookControlJSBuilder;
import net.liopyu.entityjs.builders.misc.MoveControlJSBuilder;
import net.liopyu.entityjs.util.ai.JumpControlJS;
import net.liopyu.entityjs.util.ai.LookControlJS;
import net.liopyu.entityjs.util.ai.MoveControlJS;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public interface EntityJSUtils {
    @Info("Creates a custom Jump Control builder and returns it.")
    static JumpControlJS createJumpControl(Mob pMob, Consumer<JumpControlJSBuilder> consumer) {
        var builder = new JumpControlJSBuilder();
        EntityJSHelperClass.consumerCallback(consumer, builder, "[EntityJS]: Error in " + pMob.getType() + "builder for field: createJumpControl.");
        return new JumpControlJS(pMob, builder);
    }

    @Info("Creates a custom Move Control builder and returns it.")
    static MoveControlJS createMoveControl(Mob pMob, Consumer<MoveControlJSBuilder> consumer) {
        var builder = new MoveControlJSBuilder();
        EntityJSHelperClass.consumerCallback(consumer, builder, "[EntityJS]: Error in " + pMob.getType() + "builder for field: createMoveControl.");
        return new MoveControlJS(pMob, builder);
    }

    @Info("Creates a custom Look Control builder and returns it.")
    static LookControlJS createLookControl(Mob pMob, Consumer<LookControlJSBuilder> consumer) {
        var builder = new LookControlJSBuilder();
        EntityJSHelperClass.consumerCallback(consumer, builder, "[EntityJS]: Error in " + pMob.getType() + "builder for field: createLookControl.");
        return new LookControlJS(pMob, builder);
    }

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