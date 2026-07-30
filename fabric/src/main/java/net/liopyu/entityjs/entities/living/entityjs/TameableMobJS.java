package net.liopyu.entityjs.entities.living.entityjs;

import net.liopyu.entityjs.builders.living.entityjs.TameableMobJSBuilder;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntity;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;

/** Fabric leaf for the common tameable implementation. */
public final class TameableMobJS extends TameableMobJSBase {
    public TameableMobJS(TameableMobJSBuilder builder, EntityType<? extends TamableAnimal> entityType, Level level) {
        super(builder, entityType, level);
    }
    public PartEntity<?>[] getParts() {
        return entityJs$getPartEntities();
    }

    @Override
    protected void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState) {
        if (builder.playCombinationStepSounds != null) {
            var pos = blockPosition();
            var context = new ContextUtils.PlayCombinationStepSoundsContext(this, primaryState, secondaryState, pos, pos);
            OverrideUtils.replaceFallback(() -> super.playCombinationStepSounds(primaryState, secondaryState), () ->
                    EntityJSHelperClass.consumerCallback(builder.playCombinationStepSounds, context, "[EntityJS]: Error in " + entityName() + " builder for field: playCombinationStepSounds."));
            return;
        }
        super.playCombinationStepSounds(primaryState, secondaryState);
    }

    @Override
    protected void playMuffledStepSound(BlockState blockState) {
        if (builder.playMuffledStepSound != null) {
            var context = new ContextUtils.PlayMuffledStepSoundContext(this, blockState, blockPosition());
            OverrideUtils.replaceFallback(() -> super.playMuffledStepSound(blockState), () ->
                    EntityJSHelperClass.consumerCallback(builder.playMuffledStepSound, context, "[EntityJS]: Error in " + entityName() + " builder for field: playMuffledStepSound."));
            return;
        }
        super.playMuffledStepSound(blockState);
    }

    @Override
    public boolean canBeCollidedWith() {
        if (builder.canBeCollidedWith == null) {
            return super.canBeCollidedWith();
        }
        Object result = OverrideUtils.with(super::canBeCollidedWith, () -> builder.canBeCollidedWith.test(this));
        if (result instanceof Boolean value) {
            return value;
        }
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeCollidedWith from entity: " + entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.canBeCollidedWith());
        return super.canBeCollidedWith();
    }
}
