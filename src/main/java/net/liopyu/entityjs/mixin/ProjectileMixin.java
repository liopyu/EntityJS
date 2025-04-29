package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyProjectileBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EntitySerializerType;
import net.liopyu.entityjs.util.EventHandlers;
import net.liopyu.entityjs.util.implementation.IProjectilsJs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = Projectile.class, remap = true)
public class ProjectileMixin implements IProjectilsJs {
    @Unique
    private Object entityJs$builder;

    @Unique
    private Object entityJs$entityObject = this;


    @Unique
    private Projectile entityJs$getLivingEntity() {
        return (Projectile) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }
        entityJs$defineSynchedData();
    }

    @Unique
    private static final Map<Class<?>, Map<String, EntityDataAccessor<?>>> entityJs$classAccessorMap = new HashMap<>();
    @Unique
    private final Map<String, EntityDataAccessor<?>> entityJs$accessorMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void entityJs$addSyncedData(EntitySerializerType type, String key, Object value) {
        try {
            String castHint = switch (type.toString().toLowerCase()) {
                case "byte", "int", "float", "long" -> type.toString().toLowerCase();
                default -> null;
            };
            Class<?> entityClass = entityJs$getLivingEntity().getClass();
            Map<String, EntityDataAccessor<?>> classMap = entityJs$classAccessorMap.computeIfAbsent(entityClass, k -> new HashMap<>());
            EntityDataAccessor<Object> accessor;
            if (classMap.containsKey(key)) {
                accessor = (EntityDataAccessor<Object>) classMap.get(key);
            } else {
                EntityDataSerializer<?> serializer = type.getSerializer();
                accessor = (EntityDataAccessor<Object>) SynchedEntityData.defineId((Class<? extends Entity>) entityClass, serializer);
                classMap.put(key, accessor);
            }

            Object finalValue = EntitySerializerType.castValue(value, castHint);

            if (!entityJs$getLivingEntity().getEntityData().hasItem(accessor)) {
                entityJs$getLivingEntity().getEntityData().define(accessor, finalValue);
            } else {
                entityJs$getLivingEntity().getEntityData().set(accessor, finalValue);
            }
            entityJs$accessorMap.put(key, accessor);
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error adding synched data", e);
        }
    }

    @Unique
    public void entityJs$addSyncedData(String identifier, Object value) {
        try {
            EntitySerializerType type;
            if (value instanceof Number num) {
                double d = num.doubleValue();
                if (num instanceof Float) {
                    type = EntitySerializerType.FLOAT;
                } else if (num instanceof Long || (d % 1 == 0 && d > Integer.MAX_VALUE && d <= Long.MAX_VALUE)) {
                    type = EntitySerializerType.LONG;
                } else if (num instanceof Integer || (d % 1 == 0 && d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE)) {
                    type = EntitySerializerType.INT;
                } else {
                    type = EntitySerializerType.FLOAT;
                }
            } else {
                type = EntitySerializerType.fromObject(value);
            }
            entityJs$addSyncedData(type, identifier, value);
        } catch (Exception e) {
            EntityJSHelperClass.logErrorMessageOnceCatchable("[EntityJS]: Error adding synched data", e);
        }
    }

    @SuppressWarnings("unchecked")
    @Unique
    public void entityJs$setSyncedData(String key, Object value) {
        EntityDataAccessor<Object> accessor = (EntityDataAccessor<Object>) entityJs$accessorMap.get(key);
        if (accessor == null) {
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Tried to set undefined synced data key: " + key);
            return;
        }
        EntityDataSerializer<?> serializer = accessor.getSerializer();
        EntitySerializerType type = EntitySerializerType.fromSerializer(serializer);
        String castHint = switch (type.toString().toLowerCase()) {
            case "byte", "int", "float", "long" -> type.toString().toLowerCase();
            default -> null;
        };
        Object casted = EntitySerializerType.castValue(value, castHint);
        entityJs$getLivingEntity().getEntityData().set(accessor, casted);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T entityJs$getSyncedData(String identifier) {
        EntityDataAccessor<T> accessor = (EntityDataAccessor<T>) entityJs$accessorMap.get(identifier);
        if (accessor == null) return null;
        if (!entityJs$getLivingEntity().getEntityData().hasItem(accessor)) return null;
        return entityJs$getLivingEntity().getEntityData().get(accessor);
    }

    public void entityJs$defineSynchedData() {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyProjectileBuilder builder) {
            if (builder.defineSyncedData != null) {
                builder.defineSyncedData.accept(entityJs$getLivingEntity());
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void entityjs$writeSyncedData(CompoundTag tag, CallbackInfo ci) {
        CompoundTag jsData = new CompoundTag();
        for (Map.Entry<String, EntityDataAccessor<?>> entry : entityJs$accessorMap.entrySet()) {
            String key = entry.getKey();
            EntityDataAccessor<?> accessor = entry.getValue();
            Object value = entityJs$getLivingEntity().getEntityData().get((EntityDataAccessor<Object>) accessor);
            EntityDataSerializer<?> serializer = accessor.getSerializer();
            EntitySerializerType type = EntitySerializerType.fromSerializer(serializer);
            switch (type) {
                case UUID -> jsData.putUUID(key, (UUID) value);
                case BYTE -> jsData.putByte(key, (Byte) value);
                case INT -> jsData.putInt(key, (Integer) value);
                case LONG -> jsData.putLong(key, (Long) value);
                case FLOAT -> jsData.putFloat(key, (Float) value);
                case STRING -> jsData.putString(key, (String) value);
                case BOOLEAN -> jsData.putBoolean(key, (Boolean) value);
                case COMPOUND_TAG -> jsData.put(key, ((net.minecraft.nbt.CompoundTag) value).copy());
                case VECTOR3 -> {
                    CompoundTag vecTag = new CompoundTag();
                    var v = (org.joml.Vector3f) value;
                    vecTag.putFloat("x", v.x());
                    vecTag.putFloat("y", v.y());
                    vecTag.putFloat("z", v.z());
                    jsData.put(key, vecTag);
                }
                case QUATERNION -> {
                    CompoundTag quatTag = new CompoundTag();
                    var q = (org.joml.Quaternionf) value;
                    quatTag.putFloat("x", q.x());
                    quatTag.putFloat("y", q.y());
                    quatTag.putFloat("z", q.z());
                    quatTag.putFloat("w", q.w());
                    jsData.put(key, quatTag);
                }
            }
        }
        tag.put("EntityJSData", jsData);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void entityjs$readSyncedData(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains("EntityJSData", 10)) return;
        CompoundTag jsData = tag.getCompound("EntityJSData");
        for (String key : jsData.getAllKeys()) {
            if (!entityJs$accessorMap.containsKey(key)) continue;
            EntityDataAccessor<?> accessor = entityJs$accessorMap.get(key);
            EntityDataSerializer<?> serializer = accessor.getSerializer();
            EntitySerializerType type = EntitySerializerType.fromSerializer(serializer);
            Object value = switch (type) {
                case UUID -> jsData.getUUID(key);
                case BYTE -> jsData.getByte(key);
                case INT -> jsData.getInt(key);
                case LONG -> jsData.getLong(key);
                case FLOAT -> jsData.getFloat(key);
                case STRING -> jsData.getString(key);
                case BOOLEAN -> jsData.getBoolean(key);
                case COMPOUND_TAG -> jsData.getCompound(key);
                case VECTOR3 -> {
                    CompoundTag vecTag = jsData.getCompound(key);
                    float x = vecTag.getFloat("x");
                    float y = vecTag.getFloat("y");
                    float z = vecTag.getFloat("z");
                    yield new org.joml.Vector3f(x, y, z);
                }
                case QUATERNION -> {
                    CompoundTag quatTag = jsData.getCompound(key);
                    float x = quatTag.getFloat("x");
                    float y = quatTag.getFloat("y");
                    float z = quatTag.getFloat("z");
                    float w = quatTag.getFloat("w");
                    yield new org.joml.Quaternionf(x, y, z, w);
                }
            };
            entityJs$setSyncedData(key, value);
        }
    }

    @Inject(method = "onHitEntity", at = @At("HEAD"), remap = true, cancellable = true)
    protected void onHitEntity(EntityHitResult pResult, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyProjectileBuilder builder) {
            if (builder != null && builder.onHitEntity != null) {
                final ContextUtils.ProjectileEntityHitContext context = new ContextUtils.ProjectileEntityHitContext(pResult, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onHitEntity, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHitEntity.");
            }
        }

    }

    @Inject(method = "onHitBlock", at = @At("HEAD"), remap = true, cancellable = true)
    protected void onHitBlock(BlockHitResult pResult, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyProjectileBuilder builder) {
            if (builder != null && builder.onHitBlock != null) {
                final ContextUtils.ProjectileBlockHitContext context = new ContextUtils.ProjectileBlockHitContext(pResult, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onHitBlock, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHitBlock.");
            }
        }

    }

    @Inject(method = "canHitEntity", at = @At("HEAD"), remap = true, cancellable = true)
    protected void canHitEntity(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyProjectileBuilder builder) {
            if (builder != null && builder.canHitEntity != null) {
                Object obj = builder.canHitEntity.apply(pTarget);
                if (obj instanceof Boolean b) {
                    boolean bool = cir.getReturnValue() && b;
                    cir.setReturnValue(bool);
                    return;
                }
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid canHitEntity for arrow builder: " + obj + ". Must be a boolean. Defaulting to super method: " + cir.getReturnValue());
            }
        }
    }
}
