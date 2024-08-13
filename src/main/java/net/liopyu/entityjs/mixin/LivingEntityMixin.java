package net.liopyu.entityjs.mixin;

import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.util.Cast;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.modification.ModifyLivingEntityBuilder;
import net.liopyu.entityjs.entities.living.vanilla.AllayEntityJS;
import net.liopyu.entityjs.events.BuildBrainEventJS;
import net.liopyu.entityjs.events.BuildBrainProviderEventJS;
import net.liopyu.entityjs.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static net.liopyu.entityjs.events.EntityModificationEventJS.*;

@Mixin(value = LivingEntity.class, remap = true)
public abstract class LivingEntityMixin /*implements IModifyEntityJS*/ {

    @Unique
    private Object entityJs$entityObject = this;

    @Unique
    private LivingEntity entityJs$getLivingEntity() {
        return (LivingEntity) entityJs$entityObject;
    }

    @Unique
    private String entityJs$entityName() {
        return entityJs$getLivingEntity().getType().toString();
    }

    @Unique
    public Object entityJs$builder;

    /*@Override
    public ModifyLivingEntityBuilder entityJs$getBuilder() {
        return entityJs$builder instanceof ModifyLivingEntityBuilder ? (ModifyLivingEntityBuilder) entityJs$builder : null;//(ModifyEntityBuilder) entityJs$builder;
    }*/


    @Inject(method = "<init>", at = @At("RETURN"), remap = true)
    private void entityjs$onEntityInit(EntityType<?> pEntityType, Level pLevel, CallbackInfo ci) {
        var entityType = entityJs$getLivingEntity().getType();
        if (EventHandlers.modifyEntity.hasListeners()) {
            var eventJS = getOrCreate(entityType, entityJs$getLivingEntity());
            EventHandlers.modifyEntity.post(eventJS);
            entityJs$builder = eventJS.getBuilder();
        }

    }

    @Unique
    public ResourceKey<EntityType<?>> entityJs$getTypeId() {
        return Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getResourceKey(entityJs$getLivingEntity().getType())).get();
    }

    @Inject(method = "brainProvider", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void brainProvider(CallbackInfoReturnable<Brain.Provider<?>> cir) {
        if (EventHandlers.buildBrainProvider.hasListeners()) {
            final BuildBrainProviderEventJS<?> event = new BuildBrainProviderEventJS<>();
            EventHandlers.buildBrainProvider.post(event, entityJs$getTypeId());
            cir.setReturnValue(event.provide());
        }
    }

    @Inject(method = "makeBrain", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    public void makeBrain(Dynamic<?> pDynamic, CallbackInfoReturnable<Brain<?>> cir) {
        if (EventHandlers.buildBrain.hasListeners()) {
            final Brain<?> brain = Cast.to(entityJs$getLivingEntity().brainProvider().makeBrain(pDynamic));
            EventHandlers.buildBrain.post(new BuildBrainEventJS<>(brain), entityJs$getTypeId());
            cir.setReturnValue(brain);
        }
    }


    @Inject(method = "tickDeath", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    protected void tickDeath(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.tickDeath != null) {
                EntityJSHelperClass.consumerCallback(builder.tickDeath, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tickDeath.");
            }
        }
    }

    @Inject(method = "aiStep", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$aiStep(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.aiStep != null) {
                builder.aiStep.accept(entityJs$getLivingEntity());
            }
        }
    }

    //(Base LivingEntity/Entity Overrides)
    @Inject(method = "doHurtTarget", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$isAlliedTo(Entity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (entityJs$builder != null && builder.onHurtTarget != null) {
                final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(entityJs$getLivingEntity(), entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onHurtTarget, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurtTarget.");
            }
        }
    }

    @Inject(method = "travel", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$travel(Vec3 pTravelVector, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.travel != null) {
                final ContextUtils.Vec3Context context = new ContextUtils.Vec3Context(pTravelVector, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.travel, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: travel.");

            }
        }
    }

    /*@Inject(method = "tick", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$tick(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (entityJs$builder != null && builder.tick != null) {
                if (!entityJs$getLivingEntity().level().isClientSide()) {
                    EntityJSHelperClass.consumerCallback(builder.tick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: tick.");
                }
            }
        }
    }*/

    @Inject(method = "doAutoAttackOnTouch", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$doAutoAttackOnTouch(LivingEntity pTarget, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.doAutoAttackOnTouch != null) {
                final ContextUtils.AutoAttackContext context = new ContextUtils.AutoAttackContext(entityJs$getLivingEntity(), pTarget);
                EntityJSHelperClass.consumerCallback(builder.doAutoAttackOnTouch, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: doAutoAttackOnTouch.");
            }
        }
    }

    @Inject(method = "decreaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$decreaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onDecreaseAirSupply != null) {
                EntityJSHelperClass.consumerCallback(builder.onDecreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
            }
        }
    }

    @Inject(method = "increaseAirSupply", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$increaseAirSupply(int pCurrentAir, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onIncreaseAirSupply != null) {
                EntityJSHelperClass.consumerCallback(builder.onIncreaseAirSupply, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onIncreaseAirSupply.");

            }
        }
    }

    @Inject(method = "blockedByShield", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$blockedByShield(LivingEntity pDefender, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onBlockedByShield != null) {
                var context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pDefender);
                EntityJSHelperClass.consumerCallback(builder.onBlockedByShield, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDecreaseAirSupply.");
            }
        }
    }

    @Inject(method = "onEquipItem", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$onEquipItem(EquipmentSlot pSlot, ItemStack pOldItem, ItemStack pNewItem, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onEquipItem != null) {
                final ContextUtils.EntityEquipmentContext context = new ContextUtils.EntityEquipmentContext(pSlot, pOldItem, pNewItem, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onEquipItem, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEquipItem.");

            }
        }
    }

    @Inject(method = "onEffectAdded", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$onEffectAdded(MobEffectInstance pEffectInstance, Entity pEntity, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onEffectAdded != null) {
                final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onEffectAdded, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectAdded.");

            }
        }
    }

    @Inject(method = "onEffectRemoved", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$onEffectRemoved(MobEffectInstance pEffectInstance, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onEffectRemoved != null) {
                final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onEffectRemoved, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEffectRemoved.");
            }
        }
    }

    @Inject(method = "heal", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$heal(float pHealAmount, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onLivingHeal != null) {
                final ContextUtils.EntityHealContext context = new ContextUtils.EntityHealContext(entityJs$getLivingEntity(), pHealAmount);
                EntityJSHelperClass.consumerCallback(builder.onLivingHeal, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingHeal.");

            }
        }
    }

    @Inject(method = "die", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$die(DamageSource pDamageSource, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onDeath != null) {
                final ContextUtils.DeathContext context = new ContextUtils.DeathContext(entityJs$getLivingEntity(), pDamageSource);
                EntityJSHelperClass.consumerCallback(builder.onDeath, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onDeath.");
            }
        }
    }

    @Inject(method = "dropCustomDeathLoot", at = @At(value = "HEAD", ordinal = 0), remap = true)
    private void entityjs$dropCustomDeathLoot(ServerLevel serverLevel, DamageSource pDamageSource, boolean pHitByPlayer, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.dropCustomDeathLoot != null) {
                final ContextUtils.EntityLootContext context = new ContextUtils.EntityLootContext(serverLevel, pDamageSource, pHitByPlayer, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.dropCustomDeathLoot, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: dropCustomDeathLoot.");

            }
        }
    }


    @Inject(method = "getSoundVolume", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getSoundVolume(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setSoundVolume != null) {
                cir.setReturnValue(builder.setSoundVolume);
            }
        }
    }

    @Inject(method = "getWaterSlowDown", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getWaterSlowDown(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setWaterSlowDown != null) {
                cir.setReturnValue(builder.setWaterSlowDown);
            }
        }
    }


    @Inject(method = "isPushable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isPushable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isPushable != null) {
                cir.setReturnValue(builder.isPushable);
            }
        }
    }

    @Inject(method = "getBlockSpeedFactor", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getBlockSpeedFactor(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.blockSpeedFactor == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.blockSpeedFactor.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for blockSpeedFactor from entity: " + entityJs$getLivingEntity().getType() + ". Value: " + builder.blockSpeedFactor.apply(entityJs$getLivingEntity()) + ". Must be a float, defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "shouldDropLoot", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$shouldDropLoot(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.shouldDropLoot != null) {
                Object obj = builder.shouldDropLoot.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropLoot from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean, defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isAffectedByFluids", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isAffectedByFluids(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isAffectedByFluids != null) {
                Object obj = builder.isAffectedByFluids.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByFluids from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isAlwaysExperienceDropper", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isAlwaysExperienceDropper(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isAlwaysExperienceDropper != null) {
                cir.setReturnValue(builder.isAlwaysExperienceDropper);
            }
        }
    }

    @Inject(method = "isImmobile", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isImmobile(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isImmobile != null) {
                Object obj = builder.isImmobile.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isImmobile from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "calculateFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$calculateFallDamage(float pFallDistance, float pDamageMultiplier, CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.calculateFallDamage == null) return;
            final ContextUtils.CalculateFallDamageContext context = new ContextUtils.CalculateFallDamageContext(pFallDistance, pDamageMultiplier, entityJs$getLivingEntity());
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.calculateFallDamage.apply(context), "integer");
            if (obj != null) {
                cir.setReturnValue((int) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for calculateFallDamage from entity: " + entityJs$entityName() + ". Value: " + builder.calculateFallDamage.apply(context) + ". Must be an int, defaulting to " + cir.getReturnValue());

        }
    }


    @Inject(method = "getHurtSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getHurtSound(DamageSource pDamageSource, CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setHurtSound == null) return;
            final ContextUtils.HurtContext context = new ContextUtils.HurtContext(entityJs$getLivingEntity(), pDamageSource);
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setHurtSound.apply(context), "resourcelocation");
            if (obj != null)
                cir.setReturnValue(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) obj));
            else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for setHurtSound from entity: " + entityJs$entityName() + ". Value: " + builder.setHurtSound.apply(context) + ". Must be a ResourceLocation or String. Defaulting to \"minecraft:entity.generic.hurt\"");

        }
    }


    @Inject(method = "canAttackType", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canAttackType(EntityType<?> pEntityType, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canAttackType != null) {
                final ContextUtils.EntityTypeEntityContext context = new ContextUtils.EntityTypeEntityContext(entityJs$getLivingEntity(), pEntityType);
                Object obj = builder.canAttackType.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttackType from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "getScale", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getScale(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.scale == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.scale.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) {
                cir.setReturnValue((float) obj);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for scale from entity: " + entityJs$entityName() + ". Value: " + builder.scale.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "shouldDropExperience", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$shouldDropExperience(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.shouldDropExperience != null) {
                Object obj = builder.shouldDropExperience.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldDropExperience from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "getVisibilityPercent", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getVisibilityPercent(Entity pLookingEntity, CallbackInfoReturnable<Double> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.visibilityPercent != null) {
                final ContextUtils.VisualContext context = new ContextUtils.VisualContext(pLookingEntity, entityJs$getLivingEntity());
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.visibilityPercent.apply(context), "double");
                if (obj != null) {
                    cir.setReturnValue((double) obj);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for visibilityPercent from entity: " + entityJs$entityName() + ". Value: " + builder.visibilityPercent.apply(context) + ". Must be a double. Defaulting to " + cir.getReturnValue());
                }
            }
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canAttack(LivingEntity pTarget, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canAttack != null) {
                final ContextUtils.LivingEntityContext context = new ContextUtils.LivingEntityContext(entityJs$getLivingEntity(), pTarget);
                Object obj = builder.canAttack.apply(context);
                if (obj instanceof Boolean b) {
                    boolean bool = b && cir.getReturnValue();
                    cir.setReturnValue(bool);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canAttack from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canBeAffected", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canBeAffected(MobEffectInstance pEffectInstance, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canBeAffected == null) {
                return;
            }
            final ContextUtils.OnEffectContext context = new ContextUtils.OnEffectContext(pEffectInstance, entityJs$getLivingEntity());
            Object result = builder.canBeAffected.apply(context);
            if (result instanceof Boolean) {
                cir.setReturnValue((boolean) result);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canBeAffected from entity: " + entityJs$entityName() + ". Value: " + result + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "isInvertedHealAndHarm", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isInvertedHealAndHarm(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.invertedHealAndHarm == null) {
                return;
            }
            Object obj = builder.invertedHealAndHarm.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for invertedHealAndHarm from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "getDeathSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getDeathSound(CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.setDeathSound == null) return;
            cir.setReturnValue(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.setDeathSound)));

        }
    }

    @Inject(method = "getFallSounds", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getFallSounds(CallbackInfoReturnable<LivingEntity.Fallsounds> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.fallSounds != null)
                cir.setReturnValue(new LivingEntity.Fallsounds(
                        Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.smallFallSound)),
                        Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.largeFallSound))
                ));
        }
    }

    @Inject(method = "getEatingSound", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getEatingSound(ItemStack pStack, CallbackInfoReturnable<SoundEvent> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.eatingSound != null)
                cir.setReturnValue(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.get((ResourceLocation) builder.eatingSound)));

        }
    }

    @Inject(method = "onClimbable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$onClimbable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onClimbable == null) {
                return;
            }
            Object obj = builder.onClimbable.apply(entityJs$getLivingEntity());
            if (obj instanceof Boolean) {
                cir.setReturnValue((boolean) obj);
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for onClimbable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to super.onClimbable(): " + cir.getReturnValue());

        }
    }


    @Inject(method = "canBreatheUnderwater", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canBreatheUnderwater(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canBreatheUnderwater != null) {
                cir.setReturnValue(builder.canBreatheUnderwater);
            }
        }
    }

    /*@Inject(method = "causeFallDamage", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onLivingFall != null) {
                final ContextUtils.EntityFallDamageContext context = new ContextUtils.EntityFallDamageContext(entityJs$getLivingEntity(), pMultiplier, pFallDistance, pSource);
                EntityJSHelperClass.consumerCallback(builder.onLivingFall, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLivingFall.");
            }
        }
    }*/

    /*@Inject(method = "setSprinting", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$setSprinting(boolean pSprinting, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onSprint != null) {
                EntityJSHelperClass.consumerCallback(builder.onSprint, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onSprint.");
            }
        }
    }*/

    @Inject(method = "getJumpBoostPower", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getJumpBoostPower(CallbackInfoReturnable<Float> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.jumpBoostPower == null) return;
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.jumpBoostPower.apply(entityJs$getLivingEntity()), "float");
            if (obj != null) cir.setReturnValue((float) obj);
            else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for jumpBoostPower from entity: " + entityJs$entityName() + ". Value: " + builder.jumpBoostPower.apply(entityJs$getLivingEntity()) + ". Must be a float. Defaulting to " + cir.getReturnValue());

        }
    }

    @Inject(method = "canStandOnFluid", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canStandOnFluid(FluidState pFluidState, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canStandOnFluid != null) {
                final ContextUtils.EntityFluidStateContext context = new ContextUtils.EntityFluidStateContext(entityJs$getLivingEntity(), pFluidState);
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canStandOnFluid.apply(context), "boolean");
                if (obj != null) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canStandOnFluid from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "isSensitiveToWater", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isSensitiveToWater(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isSensitiveToWater != null) {
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.isSensitiveToWater.apply(entityJs$getLivingEntity()), "boolean");
                if (obj != null) {
                    cir.setReturnValue((boolean) obj);
                    return;
                }
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSensitiveToWater from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    /*@Inject(method = "stopRiding", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$stopRiding(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onStopRiding != null) {
                EntityJSHelperClass.consumerCallback(builder.onStopRiding, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopRiding.");
            }
        }
    }*/

   /* @Inject(method = "rideTick", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$rideTick(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.rideTick != null) {
                EntityJSHelperClass.consumerCallback(builder.rideTick, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: rideTick.");

            }
        }
    }*/

    @Inject(method = "onItemPickup", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$onItemPickup(ItemEntity pItemEntity, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onItemPickup != null) {
                final ContextUtils.EntityItemEntityContext context = new ContextUtils.EntityItemEntityContext(entityJs$getLivingEntity(), pItemEntity);
                EntityJSHelperClass.consumerCallback(builder.onItemPickup, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onItemPickup.");
            }
        }
    }

    @Inject(method = "hasLineOfSight", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$hasLineOfSight(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.hasLineOfSight != null) {
                final ContextUtils.LineOfSightContext context = new ContextUtils.LineOfSightContext(pEntity, entityJs$getLivingEntity());
                Object obj = builder.hasLineOfSight.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for hasLineOfSight from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "onEnterCombat", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$onEnterCombat(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onEnterCombat != null) {
                EntityJSHelperClass.consumerCallback(builder.onEnterCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onEnterCombat.");
            }
        }
    }

    @Inject(method = "onLeaveCombat", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$onLeaveCombat(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onLeaveCombat != null) {
                EntityJSHelperClass.consumerCallback(builder.onLeaveCombat, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onLeaveCombat.");

            }
        }
    }

    @Inject(method = "isAffectedByPotions", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isAffectedByPotions(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isAffectedByPotions != null) {
                Object obj = builder.isAffectedByPotions.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAffectedByPotions from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "attackable", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$attackable(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.isAttackableFunction != null) {
                Object obj = builder.isAttackableFunction.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isAttackable from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canTakeItem", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canTakeItem(ItemStack pStack, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canTakeItem != null) {
                final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pStack, entityJs$getLivingEntity().level());
                Object obj = builder.canTakeItem.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canTakeItem from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "isSleeping", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isSleeping(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (entityJs$builder != null && builder.isSleeping != null) {
                Object obj = builder.isSleeping.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isSleeping from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "startSleeping", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$startSleeping(BlockPos pPos, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onStartSleeping != null) {
                final ContextUtils.EntityBlockPosContext context = new ContextUtils.EntityBlockPosContext(entityJs$getLivingEntity(), pPos);
                EntityJSHelperClass.consumerCallback(builder.onStartSleeping, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStartSleeping.");
            }
        }
    }

    @Inject(method = "stopSleeping", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$stopSleeping(CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onStopSleeping != null) {
                EntityJSHelperClass.consumerCallback(builder.onStopSleeping, entityJs$getLivingEntity(), "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onStopSleeping.");
            }
        }
    }

    @Inject(method = "eat", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$eat(Level pLevel, ItemStack pFood, CallbackInfoReturnable<ItemStack> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.eat != null) {
                final ContextUtils.EntityItemLevelContext context = new ContextUtils.EntityItemLevelContext(entityJs$getLivingEntity(), pFood, pLevel);
                EntityJSHelperClass.consumerCallback(builder.eat, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: eat.");
            }
        }
    }

    @Inject(method = "shouldRiderFaceForward", at = @At(value = "HEAD", ordinal = 0), remap = false, cancellable = true)
    private void entityjs$shouldRiderFaceForward(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.shouldRiderFaceForward != null) {
                final ContextUtils.PlayerEntityContext context = new ContextUtils.PlayerEntityContext(player, entityJs$getLivingEntity());
                Object obj = builder.shouldRiderFaceForward.apply(context);
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for shouldRiderFaceForward from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canFreeze", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canFreeze(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canFreeze != null) {
                Object obj = builder.canFreeze.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canFreeze from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "isCurrentlyGlowing", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$isCurrentlyGlowing(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (entityJs$builder != null && builder.isCurrentlyGlowing != null && !entityJs$getLivingEntity().level().isClientSide()) {
                Object obj = builder.isCurrentlyGlowing.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for isCurrentlyGlowing from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "canDisableShield", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$canDisableShield(CallbackInfoReturnable<Boolean> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.canDisableShield != null) {
                Object obj = builder.canDisableShield.apply(entityJs$getLivingEntity());
                if (obj instanceof Boolean) {
                    cir.setReturnValue((boolean) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for canDisableShield from entity: " + entityJs$entityName() + ". Value: " + obj + ". Must be a boolean. Defaulting to " + cir.getReturnValue());
            }
        }
    }

    @Inject(method = "actuallyHurt", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$actuallyHurt(DamageSource pDamageSource, float pDamageAmount, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.onHurt != null) {
                final ContextUtils.EntityDamageContext context = new ContextUtils.EntityDamageContext(pDamageSource, pDamageAmount, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.onHurt, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: onHurt.");
            }
        }
    }

    @Inject(method = "getExperienceReward", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$getExperienceReward(CallbackInfoReturnable<Integer> cir) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.experienceReward != null) {
                Object obj = EntityJSHelperClass.convertObjectToDesired(builder.experienceReward.apply(entityJs$getLivingEntity()), "integer");
                if (obj != null) {
                    cir.setReturnValue((int) obj);
                } else
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid return value for experienceReward from entity: " + entityJs$entityName() + ". Value: " + builder.experienceReward.apply(entityJs$getLivingEntity()) + ". Must be an integer. Defaulting to " + cir.getReturnValue());
            }
        }
    }


    @Inject(method = "lerpTo", at = @At(value = "HEAD", ordinal = 0), remap = true, cancellable = true)
    private void entityjs$lerpTo(double pX, double pY, double pZ, float pYaw, float pPitch, int pPosRotationIncrements, CallbackInfo ci) {
        if (entityJs$builder != null && entityJs$builder instanceof ModifyLivingEntityBuilder builder) {
            if (builder.lerpTo != null) {
                final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(pX, pY, pZ, pYaw, pPitch, pPosRotationIncrements, entityJs$getLivingEntity());
                EntityJSHelperClass.consumerCallback(builder.lerpTo, context, "[EntityJS]: Error in " + entityJs$entityName() + "builder for field: lerpTo.");
            }
        }
    }
}