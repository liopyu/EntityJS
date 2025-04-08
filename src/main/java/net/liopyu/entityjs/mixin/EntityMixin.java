package net.liopyu.entityjs.mixin;

import com.mojang.logging.LogUtils;
import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyMobBuilder;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.events.AddGoalSelectorsEventJS;
import net.liopyu.entityjs.events.AddGoalTargetsEventJS;
import net.liopyu.entityjs.events.EntityModificationEventJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.implementation.IEntityJS;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;
import java.util.function.Consumer;

import static net.liopyu.entityjs.events.EntityModificationEventJS.*;

@Mixin(value = Entity.class, remap = true)
public class EntityMixin implements IEntityJS {
    @Unique
    private Object entityJs$builder = getOrCreate(entityJs$getLivingEntity().getType(), ((Entity) (Object) this).getClass());
    ;


    @Unique
    private Object entityJs$entityObject = this;
    @Unique
    private EntityJSHelperClass.EntityMovementTracker entityJs$movementTracker;
    private boolean entityJs$isMoving = false;

    @Unique
    private Entity entityJs$getLivingEntity() {
        return (Entity) (Object) this;
    }


    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    private static final Map<Class<?>, Map<String, EntityDataAccessor<?>>> entityJs$classAccessorMap = new HashMap<>();

    @Unique
    private final Map<String, EntityDataAccessor<?>> entityJs$accessorMap = new HashMap<>();

    @Inject(method = "<init>", at = @At("CTOR_HEAD"), remap = true)
    private void entityjs$predefineAccessors(EntityType<?> type, Level level, CallbackInfo ci) {
        /*var entityType = type;
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, ((Entity) (Object) this).getClass());

            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
            //LogUtils.getLogger().info("Init method initializing builder: " + entityJs$builder);
        }*/
        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
        entityJs$preRegisterCustomAccessors();
    }
    /*@ModifyVariable(
            method = "<init>",
            at = @At(value = "STORE", ordinal = 0), // Store after builder is initialized
            ordinal = 0
    )
    private SynchedEntityData.Builder entityjs$captureBuilder(SynchedEntityData.Builder builder) {
        // Initialize your builder logic here instead of CTOR_HEAD
        var entityType = entityJs$getLivingEntity().getType(); // Or use cached type param
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, (Entity) (Object) this);
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
            LogUtils.getLogger().info("Captured builder: " + entityJs$builder);
        }

        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
        entityJs$preRegisterCustomAccessors();
        return builder;
    }*/


    public void entityJs$defineSynchedData() {
        LogUtils.getLogger().info("Defining synced data");
        if (entityJs$builder instanceof ModifyEntityBuilder builder) {
            LogUtils.getLogger().info("Found synced data builder");
            if (builder.defineSyncedData != null) {
                LogUtils.getLogger().info("Found synced data METHOD");
                builder.defineSyncedData.accept(entityJs$getLivingEntity());
            }
        }
    }

    @Unique
    private static final Map<Class<?>, Set<String>> entityJs$definedKeys = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void entityJs$addSyncedData(EntitySerializerType type, String key, Object value) {
        try {
            Class<?> entityClass = entityJs$getLivingEntity().getClass();

            // Retrieve or initialize the per-class map
            Map<String, EntityDataAccessor<?>> classAccessors = entityJs$classAccessorMap.computeIfAbsent(entityClass, c -> new HashMap<>());

            // Try to reuse the accessor or define a new one if missing
            EntityDataAccessor<Object> accessor;
            if (classAccessors.containsKey(key)) {
                accessor = (EntityDataAccessor<Object>) classAccessors.get(key);
                LogUtils.getLogger().info("[EntityJS] Reusing accessor '{}' with id {}", key, accessor.id());
            } else {
                EntityDataSerializer<?> serializer = type.getSerializer();
                accessor = (EntityDataAccessor<Object>) SynchedEntityData.defineId((Class<? extends SyncedDataHolder>) entityClass, serializer);
                classAccessors.put(key, accessor);
                LogUtils.getLogger().info("[EntityJS] Defined new accessor '{}' with id {}", key, accessor.id());
            }

            // Cast the value properly
            String castHint = switch (type.toString().toLowerCase()) {
                case "byte", "int", "float", "long" -> type.toString().toLowerCase();
                default -> null;
            };
            Object casted = EntitySerializerType.castValue(value, castHint);

            // Set the value
            entityJs$getLivingEntity().getEntityData().set(accessor, casted);
            entityJs$accessorMap.put(key, accessor);
            LogUtils.getLogger().info("[EntityJS] Synced '{}' = {}", key, casted);
        } catch (Exception e) {
            LogUtils.getLogger().error("[EntityJS] Failed to add synced data '{}'", key, e);
        }
    }

    private void entityJs$preRegisterCustomAccessors() {
        if (true) return;
        Class<?> clazz = entityJs$getLivingEntity().getClass();
        Set<String> definedKeys = entityJs$definedKeys.computeIfAbsent(clazz, k -> new HashSet<>());
        Map<String, EntityDataAccessor<?>> accessors = entityJs$classAccessorMap.computeIfAbsent(clazz, k -> new HashMap<>());
        if (!definedKeys.contains("dummy1")) {
            EntityDataAccessor<String> a = SynchedEntityData.defineId((Class<? extends SyncedDataHolder>) clazz, EntityDataSerializers.STRING);
            accessors.put("dummy1", a);
            definedKeys.add("dummy1");
        }

        if (!definedKeys.contains("dummy2")) {
            EntityDataAccessor<String> b = SynchedEntityData.defineId((Class<? extends SyncedDataHolder>) clazz, EntityDataSerializers.STRING);
            accessors.put("dummy2", b);
            definedKeys.add("dummy2");
        }
    }


    @Unique
    @SuppressWarnings("unchecked")
    public @Nullable <T> T getTheData(String key) {
        EntityDataAccessor<T> accessor = (EntityDataAccessor<T>) entityJs$accessorMap.get(key);
        if (accessor == null) return null;
        try {
            return entityJs$getLivingEntity().getEntityData().get(accessor);
        } catch (Exception e) {
            return null;
        }
    }

    @Inject(method = "<init>", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/network/syncher/SynchedEntityData$Builder;build()Lnet/minecraft/network/syncher/SynchedEntityData;",
            shift = At.Shift.BEFORE
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    private void entityjs$beforeBuild(EntityType<?> type, Level level, CallbackInfo ci, SynchedEntityData.Builder builder) {
        // Run this BEFORE defineSynchedData
        /*if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(type, ((Entity) (Object) this).getClass());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
            LogUtils.getLogger().info("[EntityJS] Captured builder inline: " + entityJs$builder);
        }*/

        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();

        // Then now it's safe to define
        entityJs$defineSynchedData();

        var accessors = entityJs$classAccessorMap.get(entityJs$getLivingEntity().getClass());
        if (accessors != null) {
            var items = ((SynchedEntityDataBuilderAccessor) builder).entityJs$getItemsById();
            for (var entry : accessors.entrySet()) {
                var key = entry.getKey();
                var accessor = (EntityDataAccessor<Object>) entry.getValue();
                int id = accessor.id();

                if (id < items.length && items[id] == null) {
                    LogUtils.getLogger().info("[EntityJS] Defining synced accessor: " + key);
                    builder.define(accessor, "default_" + key);
                    entityJs$accessorMap.put(key, accessor);
                } else {
                    LogUtils.getLogger().info("[EntityJS] Skipping already defined accessor: " + key);
                }
            }
        }
    }


    // Getter for testing
    public @Nullable String entityJs$getDummyData(String key) {
        EntityDataAccessor<String> accessor = (EntityDataAccessor<String>) entityJs$accessorMap.get(key);
        if (accessor == null) return null;
        return ((Entity) (Object) this).getEntityData().get(accessor);
    }
    /*@Inject(method = "<init>", at = @At("CTOR_HEAD"), remap = true)
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }
        entityJs$movementTracker = new EntityJSHelperClass.EntityMovementTracker();
        entityJs$preRegisterCustomAccessors();
    }*/

    @Override
    public boolean entityJs$isMoving() {
        return this.entityJs$isMoving;
    }

    //@Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), cancellable = true)
    @Inject(method = "tick", at = @At("HEAD"), remap = true, cancellable = true)
    public void entityJs$tick(CallbackInfo ci) {
        entityJs$isMoving = entityJs$movementTracker.isMoving(entityJs$getLivingEntity());
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.tick != null) {
                EntityJSHelperClass.consumerCallback(builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick.");
            }
        }

    }

    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.lerpTo != null) {
                final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo.");
            }
        }
    }


    @Inject(method = "move", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void move(MoverType pType, Vec3 pPos, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.move != null) {
                final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.move, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: move.");
            }
        }
    }

    @Inject(method = "playerTouch", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void playerTouch(Player pPlayer, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (entityJs$builder != null && builder.playerTouch != null) {
                final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(pPlayer, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.playerTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: playerTouch.");
            }
        }
    }

    @Inject(method = "onRemovedFromLevel", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onRemovedFromWorld(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onRemovedFromWorld != null) {
                EntityJSHelperClass.consumerCallback(builder.onRemovedFromWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onRemovedFromWorld.");
            }
        }
    }

    @Inject(method = "thunderHit", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void thunderHit(ServerLevel pLevel, LightningBolt pLightning, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.thunderHit != null) {
                final ContextUtils.EThunderHitContext context = new ContextUtils.EThunderHitContext(pLevel, pLightning, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.thunderHit, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: thunderHit.");
            }
        }
    }

    @Inject(method = "causeFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onFall != null) {
                final ContextUtils.EEntityFallDamageContext context = new ContextUtils.EEntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
                EntityJSHelperClass.consumerCallback(builder.onFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
            }
        }
    }

    @Inject(method = "onAddedToLevel", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void onAddedToWorld(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onAddedToWorld != null && !entityJs$getLivingEntity().level().isClientSide()) {
                EntityJSHelperClass.consumerCallback(builder.onAddedToWorld, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onAddedToWorld.");
            }
        }
        if (!(entityJs$getLivingEntity() instanceof IAnimatableJS)) {
            if (entityJs$getLivingEntity() instanceof Mob m) {
                if (EventHandlers.addGoalTargets.hasListeners()) {
                    EventHandlers.addGoalTargets.post(new AddGoalSelectorsEventJS<>(m, m.goalSelector), entityJs$getTypeId());
                }
                if (EventHandlers.addGoalSelectors.hasListeners()) {
                    EventHandlers.addGoalSelectors.post(new AddGoalSelectorsEventJS<>(m, m.goalSelector), entityJs$getTypeId());
                }
            }
        }
    }


    @Unique
    public ResourceKey<EntityType<?>> entityJs$getTypeId() {
        return Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityJs$getLivingEntity().getType())).get();
    }

    @Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onSprint != null) {
                EntityJSHelperClass.consumerCallback(builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint.");
            }
        }
    }


    @Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void stopRiding(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onStopRiding != null) {
                EntityJSHelperClass.consumerCallback(builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding.");
            }
        }
    }


    @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void rideTick(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.rideTick != null) {
                EntityJSHelperClass.consumerCallback(builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick.");
            }
        }
    }

    @Inject(method = "onClientRemoval", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void onClientRemoval(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onClientRemoval != null) {
                EntityJSHelperClass.consumerCallback(builder.onClientRemoval, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onClientRemoval.");
            }
        }
    }


    @Inject(method = "lavaHurt", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void lavaHurt(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.lavaHurt != null) {
                EntityJSHelperClass.consumerCallback(builder.lavaHurt, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lavaHurt.");
            }
        }
    }


    @Inject(method = "onFlap", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void onFlap(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onFlap != null) {
                EntityJSHelperClass.consumerCallback(builder.onFlap, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onFlap.");
            }
        }
    }


    @Inject(method = "shouldRenderAtSqrDistance", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void shouldRenderAtSqrDistance(double pDistance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.shouldRenderAtSqrDistance != null) {
                final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(pDistance, entityJs$getLivingEntity());
                Object obj = builder.shouldRenderAtSqrDistance.apply(context);
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isAttackable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isAttackable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isAttackable == null) return;
            cir.setReturnValue(builder.isAttackable);
        }
    }


    @Inject(method = "getControllingPassenger", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
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

    @Inject(method = "canCollideWith", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void canCollideWith(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canCollideWith != null) {
                final ContextUtils.ECollidingEntityContext context = new ContextUtils.ECollidingEntityContext(entityJs$getLivingEntity(), pEntity);
                Object obj = builder.canCollideWith.apply(context);
                if (obj instanceof Boolean b) {
                    cir.setReturnValue(b);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canCollideWith from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "getBlockJumpFactor", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getBlockJumpFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setBlockJumpFactor == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setBlockJumpFactor from entity: " + entityJs$entityName() + ". Value: " + builder.setBlockJumpFactor.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isPickable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isPickable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isPickable == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isPickable.apply(entityJs$getLivingEntity()), "boolean");
            if (obj != null) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isPickable from entity: " + entityJs$entityName() + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
        }
    }

    @Inject(method = "isPushable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isPushable == null) return;
            cir.setReturnValue(builder.isPushable);
        }
    }

    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), remap = true, cancellable = true)
    public void push(Entity pEntity, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onEntityCollision != null) {
                final ContextUtils.CollidingProjectileEntityContext context = new ContextUtils.CollidingProjectileEntityContext(entityJs$getLivingEntity(), pEntity);
                EntityJSHelperClass.consumerCallback(builder.onEntityCollision, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEntityCollision.");
            }
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.blockSpeedFactor == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$entityName() + ". Value: " + builder.blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void positionRider(Entity pPassenger, Entity.MoveFunction pCallback, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.positionRider != null) {
                final ContextUtils.PositionRiderContext context = new ContextUtils.PositionRiderContext(entityJs$getLivingEntity(), pPassenger, pCallback);
                EntityJSHelperClass.consumerCallback(builder.positionRider, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: positionRider.");
                ci.cancel();
            }
        }
    }

    @Inject(method = "canAddPassenger", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void canAddPassenger(Entity pPassenger, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canAddPassenger == null) {
                return;
            }
            final ContextUtils.EPassengerEntityContext context = new ContextUtils.EPassengerEntityContext(pPassenger, entityJs$getLivingEntity());
            Object obj = builder.canAddPassenger.apply(context);
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAddPassenger from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());

        }
    }


    @Inject(method = "isFlapping", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void isFlapping(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isFlapping != null) {
                Object obj = builder.isFlapping.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFlapping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "repositionEntityAfterLoad", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
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


    @Inject(method = "canFreeze", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canFreeze != null) {
                Object obj = builder.canFreeze.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canChangeDimensions", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canChangeDimensions != null) {
                Object obj = builder.canChangeDimensions.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isFreezing", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isFreezing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isFreezing != null) {
                Object obj = builder.isFreezing.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isFreezing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (entityJs$builder != null && builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
                Object obj = builder.isCurrentlyGlowing.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "dampensVibrations", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void dampensVibrations(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.dampensVibrations != null) {
                Object obj = builder.dampensVibrations.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for dampensVibrations from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "showVehicleHealth", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void showVehicleHealth(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.showVehicleHealth != null) {
                Object obj = builder.showVehicleHealth.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for showVehicleHealth from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isInvulnerableTo", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void isInvulnerableTo(DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.isInvulnerableTo != null) {
                final ContextUtils.EDamageContext context = new ContextUtils.EDamageContext(entityJs$getLivingEntity(), pSource);
                Object obj = builder.isInvulnerableTo.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isInvulnerableTo from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "canChangeDimensions", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void canChangeDimensions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canChangeDimensions != null) {
                Object obj = builder.canChangeDimensions.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canChangeDimensions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "interact", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void onInteract(Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResult> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.onInteract != null) {
                final ContextUtils.EntityInteractContext context = new ContextUtils.EntityInteractContext(entityJs$getLivingEntity(), pPlayer, pHand);
                EntityJSHelperClass.consumerCallback(builder.onInteract, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onInteract.");
            }
        }

    }

    @Inject(method = "mayInteract", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void mayInteract(Level pLevel, BlockPos pPos, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.mayInteract != null) {
                final ContextUtils.EMayInteractContext context = new ContextUtils.EMayInteractContext(pLevel, pPos, entityJs$getLivingEntity());
                Object obj = builder.mayInteract.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for mayInteract from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "canTrample", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    public void canTrample(BlockState state, BlockPos pos, float fallDistance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.canTrample != null) {
                final ContextUtils.ECanTrampleContext context = new ContextUtils.ECanTrampleContext(state, pos, fallDistance, entityJs$getLivingEntity());
                Object obj = builder.canTrample.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTrample from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "getMaxFallDistance", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void getMaxFallDistance(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyEntityBuilder builder) {
            if (builder.setMaxFallDistance == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setMaxFallDistance.apply(entityJs$getLivingEntity()), "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setMaxFallDistance from entity: " + entityJs$entityName() + ". Value: " + builder.setMaxFallDistance.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + cir.getReturnValue());
        }
    }
}