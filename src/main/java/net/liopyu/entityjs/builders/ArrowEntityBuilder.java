package net.liopyu.entityjs.builders;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.IArrowEntityJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;


public abstract class ArrowEntityBuilder<T extends AbstractArrow & IArrowEntityJS> extends BaseEntityBuilder<T> {
    public static final List<ArrowEntityBuilder<?>> thisList = new ArrayList<>();
    public transient Function<T, ResourceLocation> getTextureLocation;
    public transient ResourceLocation setSoundEvent;

    public transient Predicate<Double> shouldRenderAtSqrDistance;

    public transient Consumer<AbstractArrow> tick;
    public transient BiConsumer<MoverType, Vec3> move;
    public transient Consumer<AbstractArrow> tickDespawn;

    public transient Consumer<ContextUtils.ArrowEntityHitContext> onHitEntity;
    public transient Consumer<ContextUtils.ArrowBlockHitContext> onHitBlock;
    public transient ResourceLocation defaultHitGroundSoundEvent;

    public transient Consumer<LivingEntity> doPostHurtEffects;
    public transient BiFunction<Vec3, Vec3, EntityHitResult> findHitEntity;
    public transient Predicate<Entity> canHitEntity;
    public transient Consumer<ContextUtils.ArrowPlayerContext> playerTouch;
    public transient Predicate<Player> tryPickup;
    public transient DoubleConsumer setBaseDamage;
    public transient IntConsumer setKnockback;
    public transient BooleanSupplier isAttackable;
    public transient Supplier<Float> getWaterInertia;

    public ArrowEntityBuilder(ResourceLocation i) {
        super(i);
        thisList.add(this);
        getTextureLocation = t -> t.getArrowBuilder().newID("textures/entity/projectiles/", ".png");
    }

    @Info(value = """
            Sets how the texture of the entity is determined, has access to the entity
            to allow changing the texture based on info about the entity
                        
            Defaults to returning <namespace>:textures/entity/projectiles/<path>.png
            """)
    public BaseEntityBuilder<T> getTextureLocation(Function<T, ResourceLocation> function) {
        getTextureLocation = function;
        return this;
    }

    @Override
    public EntityType<T> createObject() {
        return new EntityTypeBuilder<>(this).get();
    }

    public ArrowEntityBuilder<T> setSoundEvent(ResourceLocation consumer) {
        setSoundEvent = consumer;
        return this;
    }

    @Info(value = "Sets whether the entity should render at a squared distance.")
    public ArrowEntityBuilder<T> shouldRenderAtSqrDistance(Predicate<Double> predicate) {
        shouldRenderAtSqrDistance = predicate;
        return this;
    }


    @Info(value = "Sets the custom tick behavior.")
    public ArrowEntityBuilder<T> tick(Consumer<AbstractArrow> consumer) {
        tick = consumer;
        return this;
    }

    @Info(value = "Sets the custom move behavior with parameters (pType, pPos).")
    public ArrowEntityBuilder<T> move(BiConsumer<MoverType, Vec3> consumer) {
        move = consumer;
        return this;
    }

    @Info(value = "Sets the custom tickDespawn behavior.")
    public ArrowEntityBuilder<T> tickDespawn(Consumer<AbstractArrow> consumer) {
        tickDespawn = consumer;
        return this;
    }

    @Info(value = "Sets the behavior when the arrow hits an entity.")
    public ArrowEntityBuilder<T> onHitEntity(Consumer<ContextUtils.ArrowEntityHitContext> consumer) {
        onHitEntity = consumer;
        return this;
    }

    @Info(value = "Sets the behavior when the arrow hits a block.")
    public ArrowEntityBuilder<T> onHitBlock(Consumer<ContextUtils.ArrowBlockHitContext> consumer) {
        onHitBlock = consumer;
        return this;
    }

    @Info(value = "Sets the default sound event when the arrow hits the ground.")
    public ArrowEntityBuilder<T> defaultHitGroundSoundEvent(ResourceLocation function) {
        defaultHitGroundSoundEvent = function;
        return this;
    }

    @Info(value = "Sets the doPostHurtEffects behavior with parameters (target).")
    public ArrowEntityBuilder<T> doPostHurtEffects(Consumer<LivingEntity> consumer) {
        doPostHurtEffects = consumer;
        return this;
    }

    @Info(value = "Sets the findHitEntity behavior with parameters (startVec, endVec).")
    public ArrowEntityBuilder<T> findHitEntity(BiFunction<Vec3, Vec3, EntityHitResult> function) {
        findHitEntity = function;
        return this;
    }

    @Info(value = "Sets the canHitEntity behavior with parameters (entity).")
    public ArrowEntityBuilder<T> canHitEntity(Predicate<Entity> predicate) {
        canHitEntity = predicate;
        return this;
    }

    @Info(value = "Sets the playerTouch behavior with parameters (player).")
    public ArrowEntityBuilder<T> playerTouch(Consumer<ContextUtils.ArrowPlayerContext> consumer) {
        playerTouch = consumer;
        return this;
    }

    @Info(value = "Sets the tryPickup behavior with parameters (player).")
    public ArrowEntityBuilder<T> tryPickup(Predicate<Player> predicate) {
        tryPickup = predicate;
        return this;
    }

    @Info(value = "Sets the base damage of the arrow entity.")
    public ArrowEntityBuilder<T> setBaseDamage(DoubleConsumer consumer) {
        setBaseDamage = consumer;
        return this;
    }

    @Info(value = "Sets the knockback strength of the arrow entity.")
    public ArrowEntityBuilder<T> setKnockback(IntConsumer consumer) {
        setKnockback = consumer;
        return this;
    }

    @Info(value = "Sets whether the arrow entity can be attacked.")
    public ArrowEntityBuilder<T> isAttackable(BooleanSupplier supplier) {
        isAttackable = supplier;
        return this;
    }

    @Info(value = "Sets the water inertia of the arrow entity.")
    public ArrowEntityBuilder<T> getWaterInertia(Supplier<Float> supplier) {
        getWaterInertia = supplier;
        return this;
    }


}

