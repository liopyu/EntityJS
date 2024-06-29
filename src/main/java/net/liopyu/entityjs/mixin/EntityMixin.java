package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.living.modification.*;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.IModifyEntityJS;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.liopyu.entityjs.events.EntityModificationEventJS.eventMap;
import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = Entity.class, remap = false)
public class EntityMixin implements IModifyEntityJS {
    @Unique
    private Object entityJs$builder;

    @Override
    public ModifyEntityBuilder entityJs$getBuilder() {
        return null;//(ModifyEntityBuilder) entityJs$builder;
    }

    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private Entity entityJs$getLivingEntity() {
        return (Entity) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        Object entity = entityJs$getLivingEntity();
        if (EventHandlers.modifyEntity.hasListeners()) {
            EventHandlers.modifyEntity.post(getOrCreate(entityJs$getLivingEntity().getType()));
        }
        if (eventMap.containsKey(entityJs$getLivingEntity().getType())) {
            Object builder = EntityModificationEventJS.getOrCreate(entityJs$getLivingEntity().getType()).getBuilder();
            if (entity instanceof TamableAnimal) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyTamableAnimal Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof Animal) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyAnimal Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof AgeableMob) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyAgeableMob Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof PathfinderMob) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyPathfinderMob Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof Mob) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyMob Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof LivingEntity) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyLivingEntity Builder Chosen");
                entityJs$builder = builder;
            } else if (entity instanceof Entity) {
                EntityJSHelperClass.logWarningMessageOnce("ModifyEntity Builder Chosen");
                entityJs$builder = builder;
            } else throw new IllegalStateException("Unknown builder in EntityMixin: " + builder.getClass());
        }
    }

    /*@Inject(method = "getMyRidingOffset", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getMyRidingOffset(CallbackInfoReturnable<Double> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.myRidingOffset == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.myRidingOffset.apply(entityJs$getLivingEntity()), "double");
        if (obj != null) {
            cir.setReturnValue((double) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.myRidingOffset.apply(entityJs$getLivingEntity()) + ". Must be a double. Defaulting to " + cir.getReturnValue());

    }


    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo.");
        }
    }*/

    @Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void tick(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.tick != null) {
                EntityJSHelperClass.logWarningMessageOnce("tick is not null");
                EntityJSHelperClass.consumerCallback(builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick.");
            }
        }
    }

    /*@Inject(method = "move", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void move(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.move != null) {
            final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.move, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: move.");
        }
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void playerTouch(Player pPlayer, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$builder.playerTouch != null) {
            final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(pPlayer, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.playerTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playerTouch.");
        }
    }

    @Inject(method = "onRemovedFromWorld", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onRemovedFromWorld(CallbackInfo ci) {

        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$builder.onRemovedFromWorld != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onRemovedFromWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onRemovedFromWorld.");
        }
    }

    @Inject(method = "thunderHit", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.thunderHit != null) {
            final ContextUtils.EThunderHitContext context = new ContextUtils.EThunderHitContext(pLevel, pLightning, entityJs$getLivingEntity());
            EntityJSHelperClass.consumerCallback(entityJs$builder.thunderHit, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: thunderHit.");
        }
    }

    @Inject(method = "causeFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onFall != null) {
            final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
            EntityJSHelperClass.consumerCallback(entityJs$builder.onFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
        }
    }

    @Inject(method = "onAddedToWorld", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onAddedToWorld(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onAddedToWorld != null && !entityJs$getLivingEntity().level().isClientSide()) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onAddedToWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onAddedToWorld.");
        }
    }

    @Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onSprint != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint.");
        }
    }


    @Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void stopRiding(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onStopRiding != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding.");
        }
    }


    @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void rideTick(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.rideTick != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick.");
        }
    }

    @Inject(method = "onClientRemoval", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onClientRemoval(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onClientRemoval != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onClientRemoval, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onClientRemoval.");
        }
    }


    @Inject(method = "lavaHurt", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void lavaHurt(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.lavaHurt != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.lavaHurt, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lavaHurt.");
        }
    }


    @Inject(method = "onFlap", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void onFlap(CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.onFlap != null) {
            EntityJSHelperClass.consumerCallback(entityJs$builder.onFlap, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onFlap.");
        }
    }


    @Inject(method = "shouldRenderAtSqrDistance", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void shouldRenderAtSqrDistance(double pDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.shouldRenderAtSqrDistance != null) {
            final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(pDistance, entityJs$getLivingEntity());
            Object obj = entityJs$builder.shouldRenderAtSqrDistance.apply(context);
            if (obj instanceof Boolean b) {
                cir.setReturnValue(b);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + cir.getReturnValue());
        }
    }

    @Inject(method = "isAttackable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isAttackable(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        cir.setReturnValue(entityJs$builder.isAttackable == null ? cir.getReturnValue() : entityJs$builder.isAttackable);
    }


    @Inject(method = "getControllingPassenger", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
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

    @Inject(method = "canCollideWith", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canCollideWith(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.canCollideWith != null) {
            final ContextUtils.ECollidingEntityContext context = new ContextUtils.ECollidingEntityContext(entityJs$getLivingEntity(), pEntity);
            Object obj = entityJs$builder.canCollideWith.apply(context);
            if (obj instanceof Boolean b) {
                cir.setReturnValue(b);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "getBlockJumpFactor", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void getBlockJumpFactor(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.setBlockJumpFactor == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
    }

    @Inject(method = "isPushable", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        cir.setReturnValue(entityJs$builder.isPushable == null ? cir.getReturnValue() : entityJs$builder.isPushable);
    }

    @Inject(method = "getBlockSpeedFactor", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.blockSpeedFactor == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
        if (obj != null) {
            cir.setReturnValue((float) obj);
        } else {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback, CallbackInfo ci) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.positionRider != null) {
            final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(entityJs$getLivingEntity(), pPassenger, pCallback);
            EntityJSHelperClass.consumerCallback(entityJs$builder.positionRider, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: positionRider.");
            ci.cancel();
        }
    }

    @Inject(method = "canAddPassenger", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void canAddPassenger(Entity pPassenger, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.canAddPassenger == null) {
            return;
        }
        final ContextUtils.EPassengerEntityContext context = new ContextUtils.EPassengerEntityContext(pPassenger, entityJs$getLivingEntity());
        Object obj = entityJs$builder.canAddPassenger.apply(context);
        if (obj instanceof Boolean) {
            cir.setReturnValue((boolean) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());
    }


    @Inject(method = "isFlapping", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void isFlapping(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.isFlapping != null) {
            Object obj = entityJs$builder.isFlapping.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "repositionEntityAfterLoad", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void repositionEntityAfterLoad(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        cir.setReturnValue(entityJs$builder.repositionEntityAfterLoad == null ? cir.getReturnValue() : entityJs$builder.repositionEntityAfterLoad);
    }

    @Inject(method = "nextStep", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void nextStep(CallbackInfoReturnable<Float> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.nextStep != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.nextStep.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for nextStep from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.nextStep.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "getSwimSplashSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void getSwimSplashSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.setSwimSplashSound == null) return;
        cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.setSwimSplashSound)));
    }


    @Inject(method = "getSwimSound", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    protected void getSwimSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.setSwimSound == null) return;
        cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) entityJs$builder.setSwimSound)));

    }


    @Inject(method = "canFreeze", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.canFreeze != null) {
            Object obj = entityJs$builder.canFreeze.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "isFreezing", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isFreezing(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.isFreezing != null) {
            Object obj = entityJs$builder.isFreezing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder != null && entityJs$builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
            Object obj = entityJs$builder.isCurrentlyGlowing.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "dampensVibrations", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void dampensVibrations(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.dampensVibrations != null) {
            Object obj = entityJs$builder.dampensVibrations.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "showVehicleHealth", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void showVehicleHealth(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.showVehicleHealth != null) {
            Object obj = entityJs$builder.showVehicleHealth.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "isInvulnerableTo", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void isInvulnerableTo(DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.isInvulnerableTo != null) {
            final ContextUtils.EDamageContext context = new ContextUtils.EDamageContext(entityJs$getLivingEntity(), pSource);
            Object obj = entityJs$builder.isInvulnerableTo.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "canChangeDimensions", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.canChangeDimensions != null) {
            Object obj = entityJs$builder.canChangeDimensions.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "mayInteract", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void mayInteract(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.mayInteract != null) {
            final ContextUtils.EMayInteractContext context = new ContextUtils.EMayInteractContext(pLevel, pPos, entityJs$getLivingEntity());
            Object obj = entityJs$builder.mayInteract.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "canTrample", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canTrample(BlockState state, BlockPos pos, float fallDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.canTrample != null) {
            final ContextUtils.ECanTrampleContext context = new ContextUtils.ECanTrampleContext(state, pos, fallDistance, entityJs$getLivingEntity());
            Object obj = entityJs$builder.canTrample.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "getMaxFallDistance", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void getMaxFallDistance(CallbackInfoReturnable<Integer> cir) {
        if (!eventMap.containsKey(entityJs$getLivingEntity().getType())) return;
        if (entityJs$builder.setMaxFallDistance == null) return;
        Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$builder.setMaxFallDistance.apply(entityJs$getLivingEntity()), "integer");
        if (obj != null) {
            cir.setReturnValue((int) obj);
        } else
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityJs$entityName() + ". Value: " + entityJs$builder.setMaxFallDistance.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + cir.getReturnValue());
    }*/
}
