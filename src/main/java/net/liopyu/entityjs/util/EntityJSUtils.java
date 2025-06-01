package net.liopyu.entityjs.util;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.builders.living.BaseLivingEntityBuilder;
import net.liopyu.entityjs.builders.misc.*;
import net.liopyu.entityjs.builders.nonliving.BaseEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ArrowEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.entityjs.ProjectileEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.BoatEntityBuilder;
import net.liopyu.entityjs.builders.nonliving.vanilla.EyeOfEnderEntityBuilder;
import net.liopyu.entityjs.util.ai.JumpControlJS;
import net.liopyu.entityjs.util.ai.LookControlJS;
import net.liopyu.entityjs.util.ai.MoveControlJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public interface EntityJSUtils {
    @Info("Helper method to get the entity's builder for the type.")
    static <T extends BuilderBase<?>> T getEntityBuilder(EntityType<?> type) {
        for (ArrowEntityBuilder<?> builder : ArrowEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (ProjectileEntityBuilder<?> builder : ProjectileEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (EyeOfEnderEntityBuilder<?> builder : EyeOfEnderEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (CustomEntityJSBuilder builder : CustomEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (BoatEntityBuilder<?> builder : BoatEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (BaseEntityBuilder<?> builder : BaseEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        for (BaseLivingEntityBuilder<?> builder : BaseLivingEntityBuilder.thisList) {
            if (builder.get() == type) return (T) builder;
        }
        return null;
    }

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
