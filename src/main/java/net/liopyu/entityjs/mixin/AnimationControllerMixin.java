package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.util.implementation.IAnimationControllerJS;
import net.liopyu.entityjs.util.overrides.CallbackInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.state.BoneSnapshot;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;
import java.util.function.Function;

@Mixin(value = AnimationController.class, remap = false)
public class AnimationControllerMixin<T extends GeoAnimatable> implements IAnimationControllerJS {
    @Unique
    private double entityJs$currentAnimationTick = 0;
    @Unique
    private AnimationController<T> entityJs$self = (AnimationController<T>) (Object) this;
    @Unique
    private Function<T, Double> entityJs$lastAnimationSpeedModifier;
    @Unique
    private Function<T, Double> entityJs$cachedAnimationSpeedModifier;
    @Shadow
    protected boolean shouldResetTick;
    @Shadow
    protected Function<T, Double> animationSpeedModifier;
    @Final
    @Shadow
    protected T animatable;
    @Shadow
    protected double tickOffset;

    @Unique
    protected double entityJs$adjustTick(double tick) {
        if (!this.shouldResetTick)
            return this.entityJs$getAnimationSpeedModifier().apply(this.animatable) * Math.max(tick - this.tickOffset, 0);
        return 0;
    }

    @Unique
    private Function<T, Double> entityJs$getAnimationSpeedModifier() {
        if (this.animationSpeedModifier != this.entityJs$lastAnimationSpeedModifier) {
            this.entityJs$lastAnimationSpeedModifier = this.animationSpeedModifier;
            this.entityJs$cachedAnimationSpeedModifier = CallbackInvoker.wrapFunction(this.animationSpeedModifier);
        }
        return this.entityJs$cachedAnimationSpeedModifier;
    }

    @Inject(method = "process", at = @At(value = "HEAD"))
    private void entityJs$onProcess(GeoModel<T> model, AnimationState<T> state, Map<String, GeoBone> bones,
                                    Map<String, BoneSnapshot> snapshots, final double seekTime,
                                    boolean crashWhenCantFindBone, CallbackInfo ci) {

        this.entityJs$currentAnimationTick = this.entityJs$adjustTick(seekTime);
    }

    public double entityJs$getCurrentAnimationTick() {
        return this.entityJs$currentAnimationTick;
    }


}


