package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.living.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.living.modification.ModifyMobBuilder;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.IModifyEntityJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = Mob.class, remap = false)
public class MobMixin implements IModifyEntityJS {
    @Unique
    private ModifyMobBuilder entityJs$builder;

    @Override
    public ModifyEntityBuilder entityJs$getBuilder() {
        return entityJs$builder;
    }

    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private Mob entityJs$getLivingEntity() {
        return (Mob) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onMobInit(EntityType<LivingEntity> pEntityType, Level pLevel, CallbackInfo ci) {
        EntityModificationEventJS event = EntityModificationEventJS.create(entityJs$getLivingEntity());
        entityJs$builder = (ModifyMobBuilder) event.getEvent();
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(event);
        }
    }

    @Inject(method = "getControllingPassenger", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        if (entityJs$builder.controlledByFirstPassenger != null) {
            if (!entityJs$builder.controlledByFirstPassenger) return;
            Entity var2 = entityJs$getLivingEntity().getFirstPassenger();
            LivingEntity var10000;
            if (var2 instanceof LivingEntity entity) {
                var10000 = entity;
            } else {
                var10000 = null;
            }
            cir.setReturnValue(var10000);
        }
    }

    @Inject(method = "mobInteract", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void mobInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder.onInteract != null) {
            final ContextUtils.MobInteractContext context = new ContextUtils.MobInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onInteract.");
        }
    }

    @Inject(method = "doHurtTarget", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void doHurtTarget(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder.onHurtTarget != null) {
            final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
        }
    }

    @Inject(method = "aiStep", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void aiStep(CallbackInfo ci) {
        if (entityJs$builder.aiStep != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.aiStep, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: aiStep.");
        }
    }

    @Inject(method = "tickLeash", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void tickLeash(CallbackInfo ci) {
        if (entityJs$builder.tickLeash != null) {
            Player $$0 = (Player) entityJs$getLivingEntity().getLeashHolder();
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext($$0, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.tickLeash, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tickLeash.");
        }
    }

    @Inject(method = "setTarget", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void setTarget(LivingEntity pTarget, CallbackInfo ci) {
        if (entityJs$builder.onTargetChanged != null) {
            final ContextUtils.TargetChangeContext context = new ContextUtils.TargetChangeContext(pTarget, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.onTargetChanged, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onTargetChanged.");
        }
    }

    @Inject(method = "ate", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void ate(CallbackInfo ci) {
        if (entityJs$builder.ate != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.ate, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: ate.");

        }
    }

    @Inject(method = "createNavigation", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void createNavigation(Level pLevel, CallbackInfoReturnable<PathNavigation> cir) {
        if (entityJs$builder == null || entityJs$builder.createNavigation == null) return;
        final ContextUtils.EntityLevelContext context = new ContextUtils.EntityLevelContext(pLevel, entityJs$getLivingEntity());
        Object obj = entityJs$builder.createNavigation.apply(context);
        if (obj instanceof PathNavigation p) {
            cir.setReturnValue(p);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for createNavigation from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be PathNavigation. Defaulting to super method.");
    }

    @Inject(method = "canBeLeashed", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canBeLeashed(Player pPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canBeLeashed != null) {
            final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(pPlayer, entityJs$getLivingEntity());
            Object obj = entityJs$builder.canBeLeashed.apply(context);
            if (obj instanceof Boolean b) {
                cir.setReturnValue(b);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeLeashed from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "getMainArm", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getMainArm(CallbackInfoReturnable<HumanoidArm> cir) {
        if (entityJs$builder.mainArm != null) cir.setReturnValue((HumanoidArm) entityJs$builder.mainArm);
    }


    @Inject(method = "getAmbientSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void getAmbientSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder.setAmbientSound != null) {
            cir.setReturnValue(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.setAmbientSound));
        }
    }

    @Inject(method = "canHoldItem", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canHoldItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.canHoldItem != null) {
            final ContextUtils.EntityItemStackContext context = new ContextUtils.EntityItemStackContext(pStack, entityJs$getLivingEntity());
            Object obj = entityJs$builder.canHoldItem.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canHoldItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "shouldDespawnInPeaceful", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> cir) {

        cir.setReturnValue(entityJs$builder.shouldDespawnInPeaceful == null ? cir.getReturnValue() : entityJs$builder.shouldDespawnInPeaceful);
    }

    @Inject(method = "isPersistenceRequired", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isPersistenceRequired(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(entityJs$builder.isPersistenceRequired == null ? cir.getReturnValue() : entityJs$builder.isPersistenceRequired);
    }

    @Inject(method = "getMeleeAttackRangeSqr", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getMeleeAttackRangeSqr(LivingEntity pEntity, CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder.meleeAttackRangeSqr != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity()), "double");
            if (obj != null) {
                cir.setReturnValue((double) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for meleeAttackRangeSqr from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.meleeAttackRangeSqr.apply(entityJs$getLivingEntity()) + ". Must be a double. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "getAmbientSoundInterval", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getAmbientSoundInterval(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder.ambientSoundInterval != null)
            cir.setReturnValue((int) entityJs$builder.ambientSoundInterval);
    }

    @Inject(method = "removeWhenFarAway", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void removeWhenFarAway(double pDistanceToClosestPlayer, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder.removeWhenFarAway == null) {
            return;
        }
        final ContextUtils.EntityDistanceToPlayerContext context = new ContextUtils.EntityDistanceToPlayerContext(pDistanceToClosestPlayer, entityJs$getLivingEntity());
        Object obj = entityJs$builder.removeWhenFarAway.apply(context);
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for removeWhenFarAway from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

    }
}
