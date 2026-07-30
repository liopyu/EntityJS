package net.liopyu.entityjs.entities.living.entityjs;

import net.liopyu.entityjs.builders.living.entityjs.TameableMobJSBuilder;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.common.util.overrides.OverrideUtils;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

/** Forge leaf containing only behavior supplied by Forge's patched Entity API. */
public final class TameableMobJS extends TameableMobJSBase {
    public TameableMobJS(TameableMobJSBuilder builder, EntityType<? extends TamableAnimal> entityType, Level level) {
        super(builder, entityType, level);
    }

    @Override
    public boolean isMultipartEntity() {
        return entityJs$getPartEntities().length > 0;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return entityJs$getPartEntities();
    }

    @Override
    protected void playCombinationStepSounds(BlockState primaryState, BlockState secondaryState, BlockPos primaryPos, BlockPos secondaryPos) {
        if (builder.playCombinationStepSounds != null) {
            var context = new ContextUtils.PlayCombinationStepSoundsContext(this, primaryState, secondaryState, primaryPos, secondaryPos);
            OverrideUtils.replaceFallback(() -> super.playCombinationStepSounds(primaryState, secondaryState, primaryPos, secondaryPos), () ->
                    EntityJSHelperClass.consumerCallback(builder.playCombinationStepSounds, context, "[EntityJS]: Error in " + entityName() + " builder for field: playCombinationStepSounds."));
            return;
        }
        super.playCombinationStepSounds(primaryState, secondaryState, primaryPos, secondaryPos);
    }

    @Override
    protected void playMuffledStepSound(BlockState blockState, BlockPos pos) {
        if (builder.playMuffledStepSound != null) {
            var context = new ContextUtils.PlayMuffledStepSoundContext(this, blockState, pos);
            OverrideUtils.replaceFallback(() -> super.playMuffledStepSound(blockState, pos), () ->
                    EntityJSHelperClass.consumerCallback(builder.playMuffledStepSound, context, "[EntityJS]: Error in " + entityName() + " builder for field: playMuffledStepSound."));
            return;
        }
        super.playMuffledStepSound(blockState, pos);
    }

    @Override
    public boolean shouldRiderFaceForward(@NotNull Player player) {
        if (builder.shouldRiderFaceForward != null) {
            var context = new ContextUtils.PlayerEntityContext(player, this);
            Object result = OverrideUtils.with(() -> super.shouldRiderFaceForward(player), () -> builder.shouldRiderFaceForward.test(context));
            if (result instanceof Boolean value) {
                return value;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.shouldRiderFaceForward(player));
        }
        return super.shouldRiderFaceForward(player);
    }

    @Override
    public boolean canTrample(@NotNull BlockState state, @NotNull BlockPos pos, float fallDistance) {
        if (builder.canTrample != null) {
            var context = new ContextUtils.CanTrampleContext(state, pos, fallDistance, this);
            Object result = OverrideUtils.with(() -> super.canTrample(state, pos, fallDistance), () -> builder.canTrample.test(context));
            if (result instanceof Boolean value) {
                return value;
            }
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + super.canTrample(state, pos, fallDistance));
        }
        return super.canTrample(state, pos, fallDistance);
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

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        entityJs$onAddedToWorld();
    }

    @Override
    public void onRemovedFromWorld() {
        if (builder.onRemovedFromWorld != null) {
            OverrideUtils.beforeFallback(super::onRemovedFromWorld, this::entityJs$onRemovedFromWorld);
            return;
        }
        super.onRemovedFromWorld();
    }
}
