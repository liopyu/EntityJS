package net.liopyu.entityjs.mixin;

import dev.latvian.mods.kubejs.util.ConsoleJS;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntity;
import net.liopyu.entityjs.entities.nonliving.entityjs.PartEntityJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.fabric.FabricSyncedData;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.common.util.EntityJSHelperClass;
import net.liopyu.entityjs.common.util.EntitySerializerType;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.common.util.implementation.IEntityJS;
import net.liopyu.entityjs.common.util.overrides.CallbackInvoker;
import net.liopyu.entityjs.common.util.overrides.CallbackUtils;
import net.liopyu.entityjs.common.util.overrides.ICallbackWrapperCache;
import net.liopyu.entityjs.common.util.overrides.data.ClientCache;
import net.liopyu.entityjs.common.util.overrides.data.NbtConvert;
import net.liopyu.entityjs.common.util.overrides.data.SavedDataJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.liopyu.entityjs.events.EntityModificationEventJS.*;

@Mixin(value = Entity.class, remap = true)
public abstract class EntityMixin implements IEntityJS, ICallbackWrapperCache {
    @Shadow
    protected abstract void playStepSound(BlockPos pos, BlockState state);

    @Unique
    private Object entityJs$builder;
    @Unique
    private Map<Object, Object> entityJs$callbackWrappers;
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

    @Unique
    private Object entityJs$withReturnFallback(String fieldName, CallbackInfoReturnable<?> cir, Supplier<Object> callback) {
        return CallbackUtils.with(fieldName, cir, cir::getReturnValue, callback);
    }

    @Unique
    private void entityJs$withAlreadyCalled(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.withAlreadyCalled(fieldName, ci, null, callback);
    }

    @Unique
    private void entityJs$withMixinFallback(String fieldName, CallbackInfo ci, Runnable callback) {
        CallbackUtils.with(fieldName, ci, () -> null, callback);
    }

    @Inject(method = "ignoreExplosion", at = @At("RETURN"), cancellable = true)
    private void entityJs$ignoreExplosion(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.ignoreExplosion != null) {
            Object result = entityJs$withReturnFallback("ignoreExplosion", cir,
                    () -> builder.ignoreExplosion.test(entityJs$getLivingEntity()));
            if (result instanceof Boolean value) {
                cir.setReturnValue(value);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid ignoreExplosion return value: "
                        + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue() + ".");
            }
        }
    }

    @Inject(method = "isAlliedTo(Lnet/minecraft/world/entity/Entity;)Z", at = @At("RETURN"), cancellable = true)
    private void entityJs$isAlliedTo(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyLivingEntityBuilder builder
                && entityJs$getLivingEntity() instanceof LivingEntity livingEntity
                && builder.isAlliedTo != null) {
            ContextUtils.LineOfSightContext context =
                    new ContextUtils.LineOfSightContext(target, livingEntity);
            Object result = entityJs$withReturnFallback("isAlliedTo", cir,
                    () -> builder.isAlliedTo.test(context));
            if (result instanceof Boolean value) {
                cir.setReturnValue(value);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAlliedTo from entity: "
                        + entityJs$entityName() + ". Value: " + result
                        + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void entityJs$onInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.onInteract != null) {
            ContextUtils.EntityInteractContext context = new ContextUtils.EntityInteractContext(entityJs$getLivingEntity(), player, hand);
            Object result = entityJs$withReturnFallback("onInteract", cir, () -> {
                EntityJSHelperClass.consumerCallback(builder.onInteract, context,
                        "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onInteract.");
                return cir.getReturnValue();
            });
            if (result instanceof InteractionResult interactionResult) {
                cir.setReturnValue(interactionResult);
            }
        }
    }

    @Inject(method = "isPickable", at = @At("RETURN"), cancellable = true)
    private void entityJs$isPickable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.isPickable != null) {
            Object result = EntityJSHelperClass.convertObjectToDesired(
                    entityJs$withReturnFallback("isPickable", cir, () -> builder.isPickable.apply(entityJs$getLivingEntity())),
                    "boolean");
            if (result != null) {
                cir.setReturnValue((boolean) result);
            }
        }
    }

    @Inject(method = "canBeHitByProjectile", at = @At("RETURN"), cancellable = true)
    private void entityJs$canBeHitByProjectile(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.canBeHitByProjectile != null) {
            Object result = EntityJSHelperClass.convertObjectToDesired(
                    entityJs$withReturnFallback("canBeHitByProjectile", cir,
                            () -> builder.canBeHitByProjectile.test(entityJs$getLivingEntity())), "boolean");
            if (result != null) {
                cir.setReturnValue((boolean) result);
            }
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void entityJs$onEntityCollision(Entity target, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.onEntityCollision != null) {
            ContextUtils.CollidingProjectileEntityContext context =
                    new ContextUtils.CollidingProjectileEntityContext(entityJs$getLivingEntity(), target);
            entityJs$withMixinFallback("onEntityCollision", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.onEntityCollision, context,
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEntityCollision."));
        }
    }

    @Inject(method = "processFlappingMovement", at = @At("HEAD"), cancellable = true)
    private void entityJs$processFlappingMovement(CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.processFlappingMovement != null) {
            entityJs$withMixinFallback("processFlappingMovement", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.processFlappingMovement, entityJs$getLivingEntity(),
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: processFlappingMovement."));
            ci.cancel();
        }
    }

    @Inject(method = "doWaterSplashEffect", at = @At("HEAD"), cancellable = true)
    private void entityJs$doWaterSplashEffect(CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.doWaterSplashEffect != null) {
            entityJs$withMixinFallback("doWaterSplashEffect", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.doWaterSplashEffect, entityJs$getLivingEntity(),
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: doWaterSplashEffect."));
            ci.cancel();
        }
    }

    @Inject(method = "playSwimSound", at = @At("HEAD"), cancellable = true)
    private void entityJs$playSwimSound(float volume, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playSwimSound != null) {
            ContextUtils.PlaySwimSoundContext context = new ContextUtils.PlaySwimSoundContext(entityJs$getLivingEntity(), volume);
            entityJs$withMixinFallback("playSwimSound", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.playSwimSound, context,
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playSwimSound."));
            ci.cancel();
        }
    }

    @Redirect(method = "walkingStepSound", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;playStepSound(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void entityJs$playStepSound(Entity entity, BlockPos pos, BlockState state) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playStepSound != null) {
            ContextUtils.PlayStepSoundContext context = new ContextUtils.PlayStepSoundContext(entityJs$getLivingEntity(), pos, state);
            CallbackUtils.with("playStepSound", null, () -> {
                this.playStepSound(pos, state);
                return null;
            }, () ->
                    EntityJSHelperClass.consumerCallback(builder.playStepSound, context,
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playStepSound."));
            return;
        }
        this.playStepSound(pos, state);
    }

    @Inject(method = "playMuffledStepSound", at = @At("HEAD"), cancellable = true)
    private void entityJs$playMuffledStepSound(BlockState state, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playMuffledStepSound != null) {
            ContextUtils.PlayMuffledStepSoundContext context =
                    new ContextUtils.PlayMuffledStepSoundContext(entityJs$getLivingEntity(), state, entityJs$getLivingEntity().blockPosition());
            entityJs$withMixinFallback("playMuffledStepSound", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.playMuffledStepSound, context,
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playMuffledStepSound."));
            ci.cancel();
        }
    }

    @Inject(method = "playCombinationStepSounds", at = @At("HEAD"), cancellable = true)
    private void entityJs$playCombinationStepSounds(BlockState primary, BlockState secondary, CallbackInfo ci) {
        if (entityJs$builder instanceof ModifyEntityBuilder builder && builder.playCombinationStepSounds != null) {
            BlockPos pos = entityJs$getLivingEntity().blockPosition();
            ContextUtils.PlayCombinationStepSoundsContext context =
                    new ContextUtils.PlayCombinationStepSoundsContext(entityJs$getLivingEntity(), primary, secondary, pos, pos);
            entityJs$withMixinFallback("playCombinationStepSounds", ci, () ->
                    EntityJSHelperClass.consumerCallback(builder.playCombinationStepSounds, context,
                            "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playCombinationStepSounds."));
            ci.cancel();
        }
    }

    @Unique
    private EntityJSHelperClass.EntityMovementTracker entityJs$movementTracker;
    private boolean entityJs$isMoving = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EventHandlers.modifyEntity.hasListeners() || EntityModificationEventJS.hasCustomModifier(entityType)) {
            var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
            EntityModificationEventJS.applyCustomModifierIfNeeded(entityType, (ModifyEntityBuilder) eventJS.getBuilder());
            eventJS.postModifyEventIfNeeded();
            entityJs$builder = eventJS.getBuilder();
            CallbackInvoker.initCallbackFields(entityJs$builder, entityJs$getLivingEntity());
            if (!pLevel.isClientSide && entityJs$builder instanceof ModifyEntityBuilder builder && builder.defineSyncedData != null) {
                EntityJSHelperClass.consumerCallback(builder.defineSyncedData, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: defineSyncedData.");
            }
        }
        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
    }

    @Override
    public Object entityJs$getCachedCallbackWrapper(Object key) {
        return entityJs$callbackWrappers == null ? null : entityJs$callbackWrappers.get(key);
    }

    @Override
    public void entityJs$putCachedCallbackWrapper(Object key, Object wrapper) {
        if (entityJs$callbackWrappers == null) {
            entityJs$callbackWrappers = new WeakHashMap<>();
        }
        entityJs$callbackWrappers.put(key, wrapper);
    }

    @Override
    public boolean entityJs$isMoving() {
        return this.entityJs$isMoving;
    }

    @Override
    public void entityJs$addSyncedData(EntitySerializerType type, String key, Object value) {
        Entity entity = entityJs$getLivingEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        net.minecraft.nbt.Tag tag = NbtConvert.toTag(type, value);
        SavedDataJS.get(level).putWithType(entity.getUUID(), key, tag, type);
        FabricSyncedData.sendValue(entity, key, tag, type.ordinal());
    }

    @Override
    public void entityJs$setSyncedData(String key, Object value) {
        Entity entity = entityJs$getLivingEntity();
        if (!(entity.level() instanceof ServerLevel level)) return;
        SavedDataJS data = SavedDataJS.get(level);
        EntitySerializerType type = data.getType(entity.getUUID(), key).orElseGet(() -> EntitySerializerType.fromObject(value));
        if (data.getType(entity.getUUID(), key).isEmpty()) {
            net.minecraft.nbt.Tag tag = NbtConvert.toTag(type, value);
            data.putWithType(entity.getUUID(), key, tag, type);
            FabricSyncedData.sendValue(entity, key, tag, type.ordinal());
        } else {
            net.minecraft.nbt.Tag tag = NbtConvert.toTag(type, value);
            data.put(entity.getUUID(), key, tag);
            FabricSyncedData.sendValue(entity, key, tag, type.ordinal());
        }
    }

    @Override
    public Object entityJs$getSyncedData(String key) {
        Entity entity = entityJs$getLivingEntity();
        UUID id = entity.getUUID();
        if (entity.level().isClientSide) {
            var type = ClientCache.getType(id, key);
            var value = ClientCache.get(id, key);
            return value == null ? null : entityJs$decodeSyncedData(type, value);
        }
        if (!(entity.level() instanceof ServerLevel level)) return null;
        SavedDataJS data = SavedDataJS.get(level);
        var value = data.get(id, key);
        if (value == null) return null;
        return entityJs$decodeSyncedData(data.getType(id, key), value);
    }

    @Unique
    private Object entityJs$decodeSyncedData(java.util.Optional<EntitySerializerType> type, net.minecraft.nbt.Tag value) {
        if (type.isEmpty()) return value;
        try {
            return NbtConvert.fromTag(type.get(), value);
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    @Unique
    public boolean entityJs$isRemovedFromWorld = false;
    @Unique
    public boolean entityJs$isAddedToWorld = false;


    @Unique
    public void onRemovedFromWorld() {
        if (entityJs$getLivingEntity().level().isClientSide) {
            ClientCache.remove(entityJs$getLivingEntity().getUUID());
        }
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onRemovedFromWorld != null) {
                CallbackUtils.withAlreadyCalled("onRemovedFromWorld", null, null, () ->
                        EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onRemovedFromWorld."));
            }
        }
    }

    @Unique
    public void onAddedToWorld() {
       /* if (entityJs$getLivingEntity() instanceof IAnimatableJS animatableJS) {
            if (animatableJS.isMultipartEntity()) {
                for (PartEntity<?> part : animatableJS.getParts()) {
                    entityJs$getLivingEntity().level().addFreshEntity(part);
                }
            }
        }*/
        /*if (!(entityJs$getLivingEntity() instanceof IAnimatableJS)) {
            if (entityJs$getLivingEntity() instanceof Mob mob) {
                ConsoleJS.STARTUP.info("shouldfire");
                if (EventHandlers.addGoalTargets.hasListeners()) {
                    EventHandlers.addGoalTargets.post(new AddGoalTargetsEventJS<>(mob, mob.targetSelector), entityJs$getTypeId());
                }
                if (EventHandlers.addGoalSelectors.hasListeners()) {
                    EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(mob, mob.goalSelector), entityJs$getTypeId());
                }
            }
        }*/
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onAddedToWorld != null && !entityJs$getLivingEntity().level().isClientSide()) {
                CallbackUtils.withAlreadyCalled("onAddedToWorld", null, null, () ->
                        EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onAddedToWorld."));
            }
        }
    }


    @Inject(method = "tick", at = @At("TAIL"))
    public void tick(CallbackInfo ci) {
        entityJs$isMoving = entityJs$movementTracker.isMoving(entityJs$getLivingEntity());
        if (!entityJs$isAddedToWorld && !entityJs$getLivingEntity().isRemoved()) {
            onAddedToWorld();
            entityJs$isAddedToWorld = true;
            entityJs$isRemovedFromWorld = false;
        } else if (entityJs$getLivingEntity().isRemoved() && !entityJs$isRemovedFromWorld) {
            onRemovedFromWorld();
            entityJs$isAddedToWorld = false;
            entityJs$isRemovedFromWorld = true;
        }
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
            Object raw = entityJs$withReturnFallback("myRidingOffset", cir, () -> builder.myRidingOffset.apply(entityJs$getLivingEntity()));
            Object obj = EntityJSHelperClass.convertObjectToDesired(raw, "double");
            if (obj != null) {
                cir.setReturnValue((double) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for myRidingOffset from entity: " + entityJs$entityName() + ". Value: " + raw + ". Must be a double. Defaulting to " + cir.getReturnValue());
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
                if (obj instanceof Boolean result) {
                    cir.setReturnValue(result);
                }
            }
        }
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
            Object raw = entityJs$withReturnFallback("setBlockJumpFactor", cir, () -> builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()));
            Object obj = EntityJSHelperClass.convertObjectToDesired(raw, "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityJs$entityName() + ". Value: " + raw + ". Must be a float. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isPushable", at = @At("RETURN"), remap = true, cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isPushable == null) return;
            cir.setReturnValue(builder.isPushable);
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At("RETURN"), remap = true, cancellable = true)
    protected void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.blockSpeedFactor == null) return;
            Object raw = entityJs$withReturnFallback("blockSpeedFactor", cir, () -> builder.blockSpeedFactor.apply(entityJs$getLivingEntity()));
            Object obj = EntityJSHelperClass.convertObjectToDesired(raw, "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$entityName() + ". Value: " + raw + ". Must be a float, defaulting to " + cir.getReturnValue());
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
            cir.setReturnValue(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.setSwimSplashSound)));

        }
    }


    @Inject(method = "getSwimSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getSwimSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setSwimSound == null) return;
            cir.setReturnValue(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.setSwimSound)));

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


    @Inject(method = "getMaxFallDistance", at = @At("RETURN"), remap = true, cancellable = true)
    public void getMaxFallDistance(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setMaxFallDistance == null) return;
            Object raw = entityJs$withReturnFallback("setMaxFallDistance", cir, () -> builder.setMaxFallDistance.apply(entityJs$getLivingEntity()));
            Object obj = EntityJSHelperClass.convertObjectToDesired(raw, "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityJs$entityName() + ". Value: " + raw + ". Must be an integer. Defaulting to " + cir.getReturnValue());
        }
    }


}
