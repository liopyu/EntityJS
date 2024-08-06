package net.liopyu.entityjs.mixin;

import net.liopyu.entityjs.builders.modification.ModifyEntityBuilder;
import net.liopyu.entityjs.builders.modification.ModifyProjectileBuilder;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.EventHandlers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.liopyu.entityjs.events.EntityModificationEventJS.getOrCreate;

@Mixin(value = Projectile.class, remap = true)
public class ProjectileMixin {
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
