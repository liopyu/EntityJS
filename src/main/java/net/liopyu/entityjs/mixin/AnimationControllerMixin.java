package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.util.implementation.IAnimationControllerJS;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.state.AnimationTest;
import com.geckolib.animation.state.BoneSnapshot;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;

import java.util.Map;
import java.util.function.Function;

@Mixin(value = AnimationController.class, remap = false)
public class AnimationControllerMixin<T extends GeoAnimatable> implements IAnimationControllerJS {
    @Unique
    private double entityJs$currentAnimationTick = 0;
    @Unique
    private AnimationController<T> entityJs$self = (AnimationController<T>) (Object) this;
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
            return this.animationSpeedModifier.apply(this.animatable) * Math.max(tick - this.tickOffset, 0);
        return 0;
    }

    @Inject(method = "process", at = @At(value = "HEAD"))
    private void entityJs$onProcess(GeoModel<T> model, AnimationTest<T> state, Map<String, GeoBone> bones,
                                    Map<String, BoneSnapshot> snapshots, final double seekTime,
                                    boolean crashWhenCantFindBone, CallbackInfo ci) {

        this.entityJs$currentAnimationTick = this.entityJs$adjustTick(seekTime);
    }

    public double entityJs$getCurrentAnimationTick() {
        return this.entityJs$currentAnimationTick;
    }


}


