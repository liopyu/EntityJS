package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.misc.CustomEntityJSBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntityJSUtils;
import net.liopyu.entityjs.common.util.EntitySerializerType;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.common.util.implementation.IEntityJS;
import net.liopyu.entityjs.common.util.overrides.CallbackUtils;
import net.liopyu.entityjs.util.overrides.data.*;
import net.liopyu.entityjs.common.util.overrides.data.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static net.liopyu.entityjs.events.EntityModificationEventJS.*;

@Mixin(value = Entity.class, remap = true)
public abstract class EntityMixin implements IEntityJS {
    @Shadow
    protected abstract void playStepSound(BlockPos pPos, BlockState pState);

    @Unique
    private Object entityJs$builder;

    /*@Override
    public ModifyEntityBuilder entityJs$getBuilder() {
        Object obj = getOrCreate(entityJs$getLivingEntity().getType(), entityJs$getLivingEntity()).getBuilder();
        if (obj instanceof ModifyEntityBuilder builder) {
            return builder;
        }
        return entityJs$builder instanceof ModifyEntityBuilder ? (ModifyEntityBuilder) entityJs$builder : null;
    }*/

    @Unique
    private Object entityJs$entityObject = this;
    @Unique
    private EntityJSHelperClass.EntityMovementTracker entityJs$movementTracker;
    private boolean entityJs$isMoving = false;

    @Unique
    private Entity entityJs$getLivingEntity() {
        return (Entity) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    private void entityJs$withAlreadyCalled(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.withAlreadyCalled(fieldName, ci, null, callback);
    }

    @Unique
    private void entityJs$withMixinFallback(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.with(fieldName, ci, () -> null, callback);
    }

    @Unique
    private Object entityJs$withReturnFallback(String fieldName, CallbackInfoReturnable<?> cir, Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, cir, cir::getReturnValue, callback);
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EntityJSUtils.handlesOwnEntityJsCallbacks(entityJs$getLivingEntity())) {
            entityJs$builder = null;
            entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
            return;
        }

        var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
        entityJs$builder = eventJS.getBuilder();
        var customBuilder = EntityJSUtils.getEntityBuilder(pEntityType);
        if (!(entityJs$getLivingEntity() instanceof LivingEntity) && customBuilder instanceof CustomEntityJSBuilder) {
            var rl = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            applyCustomModifierIfNeeded(rl, (ModifyEntityBuilder) entityJs$builder);
        }
        eventJS.postModifyEventIfNeeded();
        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
    }

    @Unique
    private boolean entityJs$definedOnce;

    @Unique
    private static EntitySerializerType entityjs$inferType(Object value) {
        if (value instanceof Byte) return EntitySerializerType.BYTE;
        if (value instanceof Integer) return EntitySerializerType.INT;
        if (value instanceof Long) return EntitySerializerType.LONG;
        if (value instanceof Float || value instanceof Double) return EntitySerializerType.FLOAT;
        if (value instanceof UUID) return EntitySerializerType.UUID;
        if (value instanceof Boolean) return EntitySerializerType.BOOLEAN;
        if (value instanceof net.minecraft.nbt.CompoundTag) return EntitySerializerType.COMPOUND_TAG;
        if (value instanceof org.joml.Vector3f || value instanceof org.joml.Vector3d || value instanceof net.minecraft.world.phys.Vec3 || value instanceof net.minecraft.core.Vec3i) {
            return EntitySerializerType.VECTOR3;
        }
        if (value instanceof org.joml.Quaternionf) return EntitySerializerType.QUATERNION;
        return EntitySerializerType.STRING;
    }

    @Unique
    private static boolean entityjs$isServerSyncedDataSide(Entity entity) {
        return !entity.level().isClientSide;
    }

    @Unique
    public void entityJs$addSyncedData(EntitySerializerType type, String name, Object initial) {
        Entity self = (Entity) (Object) this;
        if (!entityjs$isServerSyncedDataSide(self)) return;
        Tag tag = NbtConvert.toTag(type, initial);
        ServerCache.ensure(self, name, tag, type);
    }

    @Unique
    public void entityJs$setSyncedData(String name, Object value) {
        Entity self = (Entity) (Object) this;
        if (!entityjs$isServerSyncedDataSide(self)) return;
        UUID id = self.getUUID();

        var optType = SavedDataJS.get((ServerLevel) self.level()).getType(id, name);

        EntitySerializerType type = optType.orElseGet(() -> entityjs$inferType(value));
        Tag tag = NbtConvert.toTag(type, value);

        if (optType.isEmpty()) {
            ServerCache.ensure(self, name, tag, type);
        } else {
            ServerCache.set(self, name, tag);
        }
    }

    @Unique
    public Object entityJs$getSyncedData(String name) {
        Entity self = (Entity) (Object) this;
        UUID id = self.getUUID();

        if (self.level().isClientSide) {
            var opt = ClientCache.getType(id, name);
            var tag = ClientCache.get(id, name);
            if (tag == null) return null;
            if (opt.isEmpty()) return tag;
            try {
                return NbtConvert.fromTag(opt.get(), tag);
            } catch (ClassCastException ex) {
                return tag;
            }
        }

        var opt = SavedDataJS.get((ServerLevel) self.level()).getType(id, name);
        var tag = ServerCache.get(self, name);
        if (tag == null) return null;
        if (opt.isEmpty()) return tag;
        try {
            return NbtConvert.fromTag(opt.get(), tag);
        } catch (ClassCastException ex) {
            return tag;
        }
    }

    @Override
    public boolean entityJs$isMoving() {
        return this.entityJs$isMoving;
    }

    @Inject(method = "ignoreExplosion", at = @At("RETURN"), remap = true, cancellable = true)
    public void entityJs$ignoreExplosion(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.ignoreExplosion != null) {
                Object obj = entityJs$withReturnFallback("ignoreExplosion", cir, () -> builder.ignoreExplosion.test((Entity) (Object) this));
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid ignoreExplosion return value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue() + ".");
                }
            }
        }
    }

    /* @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
     private void entityjs$isAlliedTo(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
         if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
             if (entityJs$builder != null && builder.isAlliedTo != null) {
                 final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entityJs$getLivingEntity(), entityJs$getLivingEntity());
                 var b = builder.isAlliedTo.apply(context);
                 if (b instanceof Boolean bool) {
                     cir.setReturnValue(bool);
                 } else
                     EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAlliedTo from entity: " + entityJs$entityName() + ". Value: " + b + ". Must be a boolean. Defaulting to super.");
             }
         }
     }
 */
    @Inject(method = "interact", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void onInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onInteract != null) {
                final ContextUtils.EntityInteractContext context = new ContextUtils.EntityInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
                Object obj = entityJs$withReturnFallback("onInteract", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.onInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onInteract.");
                    return cir.getReturnValue();
                });
                if (obj instanceof InteractionResult result) {
                    cir.setReturnValue(result);
                }
            }
        }

    }

    //@Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), cancellable = true)
    @Inject(method = "tick", at = @At("HEAD"), remap = true, cancellable = true)
    public void entityJs$tick(CallbackInfo ci) {
        entityJs$isMoving = entityJs$movementTracker.isMoving(entityJs$getLivingEntity());
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.tick != null) {
                entityJs$withAlreadyCalled("tick", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick."));
            }
        }

    }

    @Inject(method = "getMyRidingOffset", at = @At("RETURN"), remap = true, cancellable = true)
    public void getMyRidingOffset(CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.myRidingOffset == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("myRidingOffset", cir, () -> builder.myRidingOffset.apply(entityJs$getLivingEntity())), "double");
            if (obj != null) {
                cir.setReturnValue((double) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a double. Defaulting to " + cir.getReturnValue());
        }
    }


    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.lerpTo != null) {
                final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, entityJs$getLivingEntity());
                entityJs$withMixinFallback("lerpTo", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo."));
            }
        }
    }


    @Inject(method = "move", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void move(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.move != null) {
                final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, entityJs$getLivingEntity());
                entityJs$withMixinFallback("move", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.move, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: move."));
            }
        }
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void playerTouch(Player pPlayer, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (entityJs$builder != null && builder.playerTouch != null) {
                final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(pPlayer, entityJs$getLivingEntity());
                entityJs$withMixinFallback("playerTouch", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playerTouch."));
            }
        }
    }

    @Inject(method = "onRemovedFromWorld", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onRemovedFromWorld(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onRemovedFromWorld != null) {
                entityJs$withMixinFallback("onRemovedFromWorld", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onRemovedFromWorld."));
            }
        }
    }

    @Inject(method = "thunderHit", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.thunderHit != null) {
                final ContextUtils.EThunderHitContext context = new ContextUtils.EThunderHitContext(pLevel, pLightning, entityJs$getLivingEntity());
                entityJs$withMixinFallback("thunderHit", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: thunderHit."));
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At("RETURN"), remap = true, cancellable = true)
    public void causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onFall != null) {
                final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
                Object obj = entityJs$withReturnFallback("onFall", cir, () -> {
                    EntityJSHelperClass.consumerCallback(builder.onFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
                    return cir.getReturnValue();
                });
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                }
            }
        }
    }

    @Inject(method = "onAddedToWorld", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onAddedToWorld(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onAddedToWorld != null && !entityJs$getLivingEntity().level().isClientSide()) {
                entityJs$withAlreadyCalled("onAddedToWorld", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onAddedToWorld."));
            }
        }
        if (!(entityJs$getLivingEntity() instanceof IAnimatableJS)) {
            if (entityJs$getLivingEntity() instanceof Mob m) {
                if (EventHandlers.addGoalTargets.hasListeners()) {
                    EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(m, m.targetSelector), entityJs$getTypeId());
                }
                if (EventHandlers.addGoalSelectors.hasListeners()) {
                    EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(m, m.goalSelector), entityJs$getTypeId());
                }
            }
        }
        Entity self = (Entity) (Object) this;
        if (entityJs$definedOnce) return;
        if (!entityjs$isServerSyncedDataSide(self)) return;
        if (!(entityJs$builder instanceof ModifyEntityBuilder builder)) return;
        if (builder.defineSyncedData == null) return;

        InitDecl.begin(self);
        builder.defineSyncedData.accept(entityJs$getLivingEntity());
        entityJs$definedOnce = true;
        ((ServerLevel) self.level()).getServer().execute(() -> InitDecl.finalizeFor(self));
    }

    @Unique
    public String entityJs$getTypeId() {
        return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(entityJs$getLivingEntity().getType())).toString();
    }

    @Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onSprint != null) {
                entityJs$withMixinFallback("onSprint", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint."));
            }
        }
    }


    @Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void stopRiding(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onStopRiding != null) {
                entityJs$withMixinFallback("onStopRiding", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding."));
            }
        }
    }


    @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void rideTick(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.rideTick != null) {
                entityJs$withMixinFallback("rideTick", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick."));
            }
        }
    }

    @Inject(method = "onClientRemoval", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void onClientRemoval(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onClientRemoval != null) {
                entityJs$withMixinFallback("onClientRemoval", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onClientRemoval, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onClientRemoval."));
            }
        }
    }


    @Inject(method = "lavaHurt", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void lavaHurt(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.lavaHurt != null) {
                entityJs$withMixinFallback("lavaHurt", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.lavaHurt, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lavaHurt."));
            }
        }
    }


    @Inject(method = "onFlap", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void onFlap(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onFlap != null) {
                entityJs$withMixinFallback("onFlap", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onFlap, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onFlap."));
            }
        }
    }


    @Inject(method = "shouldRenderAtSqrDistance", at = @At("RETURN"), remap = true, cancellable = true)
    public void shouldRenderAtSqrDistance(double pDistance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.shouldRenderAtSqrDistance != null) {
                final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(pDistance, entityJs$getLivingEntity());
                Object obj = entityJs$withReturnFallback("shouldRenderAtSqrDistance", cir, () -> builder.shouldRenderAtSqrDistance.test(context));
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isAttackable", at = @At("RETURN"), remap = true, cancellable = true)
    public void isAttackable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isAttackable == null) return;
            cir.setReturnValue(builder.isAttackable);
        }
    }


    @Inject(method = "getControllingPassenger", at = @At("RETURN"), remap = true, cancellable = true)
    public void getControllingPassenger(CallbackInfoReturnable<LivingEntity> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.controlledByFirstPassenger != null) {
                if (!builder.controlledByFirstPassenger) return;
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
    }

    @Inject(method = "canCollideWith", at = @At("RETURN"), remap = true, cancellable = true)
    public void canCollideWith(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canCollideWith != null) {
                final ContextUtils.ECollidingEntityContext context = new ContextUtils.ECollidingEntityContext(entityJs$getLivingEntity(), pEntity);
                Object obj = entityJs$withReturnFallback("canCollideWith", cir, () -> builder.canCollideWith.test(context));
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "getBlockJumpFactor", at = @At("RETURN"), remap = true, cancellable = true)
    protected void getBlockJumpFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setBlockJumpFactor == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("setBlockJumpFactor", cir, () -> builder.setBlockJumpFactor.apply(entityJs$getLivingEntity())), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a float. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isPushable", at = @At("RETURN"), remap = true, cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isPushable == null) return;
            cir.setReturnValue(builder.isPushable);
        }
    }

    @Inject(method = "isPickable", at = @At("RETURN"), remap = true, cancellable = true)
    public void isPickable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isPickable == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("isPickable", cir, () -> builder.isPickable.apply(entityJs$getLivingEntity())), "boolean");
            if (obj != null) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isPickable from entity: " + entityJs$entityName() + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "canBeHitByProjectile", at = @At("RETURN"), remap = true, cancellable = true)
    public void canBeHitByProjectile(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canBeHitByProjectile == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("canBeHitByProjectile", cir, () -> builder.canBeHitByProjectile.test(entityJs$getLivingEntity())), "boolean");
            if (obj != null) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeHitByProjectile from entity: " + entityJs$entityName() + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), remap = true, cancellable = true)
    public void push(Entity pEntity, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onEntityCollision != null) {
                final ContextUtils.CollidingProjectileEntityContext context = new ContextUtils.CollidingProjectileEntityContext(entityJs$getLivingEntity(), pEntity);
                entityJs$withMixinFallback("onEntityCollision", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.onEntityCollision, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEntityCollision."));
            }
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), remap = true, cancellable = true)
    protected void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.blockSpeedFactor == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("blockSpeedFactor", cir, () -> builder.blockSpeedFactor.apply(entityJs$getLivingEntity())), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a float, defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.positionRider != null) {
                final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(entityJs$getLivingEntity(), pPassenger, pCallback);
                entityJs$withMixinFallback("positionRider", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.positionRider, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: positionRider."));
                ci.cancel();
            }
        }
    }

    @Inject(method = "canAddPassenger", at = @At("RETURN"), remap = true, cancellable = true)
    protected void canAddPassenger(Entity pPassenger, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canAddPassenger == null) {
                return;
            }
            final ContextUtils.EPassengerEntityContext context = new ContextUtils.EPassengerEntityContext(pPassenger, entityJs$getLivingEntity());
            Object obj = entityJs$withReturnFallback("canAddPassenger", cir, () -> builder.canAddPassenger.test(context));
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());

        }
    }


    @Inject(method = "isFlapping", at = @At("RETURN"), remap = true, cancellable = true)
    protected void isFlapping(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isFlapping != null) {
                Object obj = entityJs$withReturnFallback("isFlapping", cir, () -> builder.isFlapping.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "processFlappingMovement", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void processFlappingMovement(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.processFlappingMovement != null) {
                entityJs$withMixinFallback("processFlappingMovement", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.processFlappingMovement, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: processFlappingMovement."));
                ci.cancel();
            }
        }
    }


    @Inject(method = "repositionEntityAfterLoad", at = @At("RETURN"), remap = true, cancellable = true)
    protected void repositionEntityAfterLoad(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.repositionEntityAfterLoad == null) return;
            cir.setReturnValue(builder.repositionEntityAfterLoad);
        }
    }


    @Inject(method = "getSwimSplashSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getSwimSplashSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setSwimSplashSound == null) return;
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSplashSound)));

        }
    }

    @Inject(method = "doWaterSplashEffect", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void doWaterSplashEffect(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.doWaterSplashEffect != null) {
                entityJs$withMixinFallback("doWaterSplashEffect", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.doWaterSplashEffect, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: doWaterSplashEffect."));
                ci.cancel();
            }
        }
    }


    @Inject(method = "getSwimSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getSwimSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setSwimSound == null) return;
            cir.setReturnValue(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue((ResourceLocation) builder.setSwimSound)));

        }
    }

    @Inject(method = "playSwimSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void playSwimSound(float volume, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.playSwimSound != null) {
                final ContextUtils.PlaySwimSoundContext context = new ContextUtils.PlaySwimSoundContext(entityJs$getLivingEntity(), volume);
                entityJs$withMixinFallback("playSwimSound", ci, () ->
                        EntityJSHelperClass.consumerCallback(builder.playSwimSound, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playSwimSound."));
                ci.cancel();
            }
        }
    }


    @Inject(method = "canFreeze", at = @At("RETURN"), remap = true, cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canFreeze != null) {
                Object obj = entityJs$withReturnFallback("canFreeze", cir, () -> builder.canFreeze.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isFreezing", at = @At("RETURN"), remap = true, cancellable = true)
    public void isFreezing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isFreezing != null) {
                Object obj = entityJs$withReturnFallback("isFreezing", cir, () -> builder.isFreezing.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At("RETURN"), remap = true, cancellable = true)
    public void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (entityJs$builder != null && builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
                Object obj = entityJs$withReturnFallback("isCurrentlyGlowing", cir, () -> builder.isCurrentlyGlowing.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "dampensVibrations", at = @At("RETURN"), remap = true, cancellable = true)
    public void dampensVibrations(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.dampensVibrations != null) {
                Object obj = entityJs$withReturnFallback("dampensVibrations", cir, () -> builder.dampensVibrations.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "showVehicleHealth", at = @At("RETURN"), remap = true, cancellable = true)
    public void showVehicleHealth(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.showVehicleHealth != null) {
                Object obj = entityJs$withReturnFallback("showVehicleHealth", cir, () -> builder.showVehicleHealth.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isInvulnerableTo", at = @At("RETURN"), remap = true, cancellable = true)
    public void isInvulnerableTo(DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isInvulnerableTo != null) {
                final ContextUtils.EDamageContext context = new ContextUtils.EDamageContext(entityJs$getLivingEntity(), pSource);
                Object obj = entityJs$withReturnFallback("isInvulnerableTo", cir, () -> builder.isInvulnerableTo.test(context));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "canChangeDimensions", at = @At("RETURN"), remap = true, cancellable = true)
    public void canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canChangeDimensions != null) {
                Object obj = entityJs$withReturnFallback("canChangeDimensions", cir, () -> builder.canChangeDimensions.test(entityJs$getLivingEntity()));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "mayInteract", at = @At("RETURN"), remap = true, cancellable = true)
    public void mayInteract(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.mayInteract != null) {
                final ContextUtils.EMayInteractContext context = new ContextUtils.EMayInteractContext(pLevel, pPos, entityJs$getLivingEntity());
                Object obj = entityJs$withReturnFallback("mayInteract", cir, () -> builder.mayInteract.test(context));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "canTrample", at = @At("RETURN"), remap = false, cancellable = true)
    public void canTrample(BlockState state, BlockPos pos, float fallDistance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canTrample != null) {
                final ContextUtils.ECanTrampleContext context = new ContextUtils.ECanTrampleContext(state, pos, fallDistance, entityJs$getLivingEntity());
                Object obj = entityJs$withReturnFallback("canTrample", cir, () -> builder.canTrample.test(context));
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Redirect(method = "walkingStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), remap = true)
    protected void entityJs$playStepSound(Entity entity, BlockPos pos, BlockState blockState) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playStepSound != null) {
            final ContextUtils.PlayStepSoundContext context = new ContextUtils.PlayStepSoundContext(entityJs$getLivingEntity(), pos, blockState);
            CallbackUtils.with("playStepSound", null, () -> {
                this.playStepSound(pos, blockState);
                return null;
            }, () ->
                    EntityJSHelperClass.consumerCallback(builder.playStepSound, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playStepSound."));
            return;
        }
        this.playStepSound(pos, blockState);
    }

    // Forge patches BlockPos into this vanilla method signature, but MCP mappings only cover the original signature.
    @Inject(method = "playMuffledStepSound", at = @At("HEAD"), remap = false, cancellable = true)
    protected void entityJs$playMuffledStepSound(BlockState blockState, BlockPos pos, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playMuffledStepSound != null) {
            final ContextUtils.PlayMuffledStepSoundContext context = new ContextUtils.PlayMuffledStepSoundContext(entityJs$getLivingEntity(), blockState, pos);
            entityJs$withMixinFallback("playMuffledStepSound", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.playMuffledStepSound, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playMuffledStepSound."));
            ci.cancel();
        }
    }

    // Forge patches BlockPos into this vanilla method signature, but MCP mappings only cover the original signature.
    @Inject(method = "playCombinationStepSounds", at = @At("HEAD"), remap = false, cancellable = true)
    protected void entityJs$playCombinationStepSounds(BlockState primaryStepSound, BlockState secondaryStepSound, BlockPos primaryPos, BlockPos secondaryPos, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playCombinationStepSounds != null) {
            final ContextUtils.PlayCombinationStepSoundsContext context = new ContextUtils.PlayCombinationStepSoundsContext(entityJs$getLivingEntity(), primaryStepSound, secondaryStepSound, primaryPos, secondaryPos);
            entityJs$withMixinFallback("playCombinationStepSounds", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.playCombinationStepSounds, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playCombinationStepSounds."));
            ci.cancel();
        }
    }

    @Inject(method = "getMaxFallDistance", at = @At("RETURN"), remap = true, cancellable = true)
    public void getMaxFallDistance(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setMaxFallDistance == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(entityJs$withReturnFallback("setMaxFallDistance", cir, () -> builder.setMaxFallDistance.apply(entityJs$getLivingEntity())), "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be an integer. Defaulting to " + cir.getReturnValue());
        }
    }


}
