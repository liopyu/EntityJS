package net.liopyu.entityjs.entities;

import dev.latvian.mods.kubejs.util.UtilsJS;
import net.liopyu.entityjs.builders.BaseLivingEntityBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.network.GeckoLibNetwork;
import software.bernie.geckolib.network.packet.AnimTriggerPacket;
import software.bernie.geckolib.network.packet.EntityAnimTriggerPacket;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * This provides a default implementation of {@link GeoAnimatable#registerControllers(AnimatableManager.ControllerRegistrar)}}
 * which delegates to the entity builder. Unfortunately, {@link #getAnimatableInstanceCache()} cannot have a default
 * implementation due to the {@link AnimatableInstanceCache} returned needing to be cached in the entity.<br><br>
 * <p>
 * See the comments on the methods here and in {@link AnimalEntityJS} for further reading.
 */
public interface IAnimatableJS extends GeoAnimatable, GeoEntity {

    /**
     * Used to retrieve the builder this entity(type) was built with. Used to retrieve information
     * about animations and anything the builder defines
     */
    BaseLivingEntityBuilder<?> getBuilder();


    /**
     * Note for implementors: by default this casts {@code this} to {@code <E extends LivingEntity & IAnimatableJS>},
     * if your implementation is not a subclass of {@link net.minecraft.world.entity.LivingEntity} this will cause
     * problems and this will necessitate overriding this method with custom implementations
     */
    default void registerControllers(AnimatableManager.ControllerRegistrar data) {
        for (BaseLivingEntityBuilder.AnimationControllerSupplier<?> supplier : getBuilder().animationSuppliers) {
            data.add(supplier.get(UtilsJS.cast(this)));
        }
    }

    /**
     * Trigger an animation for this Entity, based on the controller name and animation name.<br>
     * <b><u>DO NOT OVERRIDE</u></b>
     *
     * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
     * @param animName       The name of animation to trigger. This needs to have been registered with the controller via {@link software.bernie.geckolib.core.animation.AnimationController#triggerableAnim AnimationController.triggerableAnim}
     */
    default void triggerAnim(@Nullable String controllerName, String animName) {
        Entity entity = (Entity) this;

        if (entity.level().isClientSide()) {
            getAnimatableInstanceCache().getManagerForId(entity.getId()).tryTriggerAnimation(controllerName, animName);
        } else {
            GeckoLibNetwork.send(new EntityAnimTriggerPacket<>(entity.getId(), controllerName, animName), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity));
        }
    }

    /**
     * This cannot be implemented here, the returned value should be a cached value that is initialized in the entity's constructor. See {@link BaseLivingEntityJS} for an example.<br><br>
     * <p>
     * If the value is not cached, some of the values available through query in the animation json file will not 'progress'
     *
     * @return The entity's {@link AnimatableInstanceCache}
     */
    AnimatableInstanceCache getAnimatableInstanceCache();

    /**
     * Trigger a client-side animation for this GeoAnimatable for the given controller name and animation name.<br>
     * This can be fired from either the client or the server, but optimally you would call it from the server.<br>
     * <b><u>DO NOT OVERRIDE</u></b>
     *
     * @param relatedEntity  An entity related to the animatable to trigger the animation for (E.G. The player holding the item)
     * @param instanceId     The unique id that identifies the specific animatable instance
     * @param controllerName The name of the controller name the animation belongs to, or null to do an inefficient lazy search
     * @param animName       The name of animation to trigger. This needs to have been registered with the controller via {@link software.bernie.geckolib.core.animation.AnimationController#triggerableAnim AnimationController.triggerableAnim}
     */
    default <D> void triggerAnim(Entity relatedEntity, long instanceId, @Nullable String controllerName, String animName) {
        if (relatedEntity.level().isClientSide()) {
            getAnimatableInstanceCache().getManagerForId(instanceId).tryTriggerAnimation(controllerName, animName);
        } else {
            GeckoLibNetwork.send(new AnimTriggerPacket<>(getClass().toString(), instanceId, controllerName, animName), PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> relatedEntity));
        }
    }

    default double getTick(Object entity) {
        return ((Entity) entity).tickCount;
    }

    /**
     * Gets the id of the entity's entity type
     */
    default String getTypeId() {
        return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(getType())).toString();
    }

    EntityType<?> getType();
}
