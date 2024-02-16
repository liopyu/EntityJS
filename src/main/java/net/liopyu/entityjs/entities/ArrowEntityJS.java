package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.*;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {

    public final ArrowEntityJSBuilder builder;
    @NotNull
    protected ItemStack pickUpStack;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }

    public ArrowEntityJS(Level level, LivingEntity shooter, ArrowEntityJSBuilder builder) {
        super(builder.get(), shooter, level);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
    }

    @Override
    public ArrowEntityBuilder<?> getArrowBuilder() {
        return builder;
    }

    @Override
    public void setPickUpItem(ItemStack stack) {
        pickUpStack = stack;
    }

    @Override
    protected ItemStack getPickupItem() {
        return pickUpStack;
    }

    //Beginning of Base Overrides


    @Override
    public void setSoundEvent(SoundEvent pSoundEvent) {
        if (builder.setSoundEvent != null) {
            this.setSoundEvent(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(builder.setSoundEvent)));
        } else {
            super.setSoundEvent(pSoundEvent);
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(distance, this);
        return builder.shouldRenderAtSqrDistance != null ? builder.shouldRenderAtSqrDistance.test(context) : super.shouldRenderAtSqrDistance(distance);
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {

        if (builder.lerpTo != null) {
            final ContextUtils.LerpToContext context = new ContextUtils.LerpToContext(x, y, z, yaw, pitch, posRotationIncrements, teleport, this);
            builder.lerpTo.accept(context);
        } else super.lerpTo(x, y, z, yaw, pitch, posRotationIncrements, teleport);
    }


    @Override
    public void tick() {
        super.tick();
        if (builder.tick != null) {
            builder.tick.accept(this);
        }
    }

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
        if (builder.move != null) {
            final ContextUtils.MovementContext context = new ContextUtils.MovementContext(pType, pPos, this);
            builder.move.accept(context);
        }
    }

    @Override
    protected void tickDespawn() {
        super.tickDespawn();
        if (builder.tickDespawn != null) {
            builder.tickDespawn.accept(this);
        }
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (builder.onHitEntity != null) {
            final ContextUtils.ArrowEntityHitContext context = new ContextUtils.ArrowEntityHitContext(result, this);
            builder.onHitEntity.accept(context);
        } else {
            super.onHitEntity(result);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (builder.onHitBlock != null) {
            final ContextUtils.ArrowBlockHitContext context = new ContextUtils.ArrowBlockHitContext(result, this);
            builder.onHitBlock.accept(context);
        } else {
            super.onHitBlock(result);
        }
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        if (builder.defaultHitGroundSoundEvent != null) {
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(builder.defaultHitGroundSoundEvent));
        }
        return super.getDefaultHitGroundSoundEvent();
    }


    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (builder.doPostHurtEffects != null) {
            builder.doPostHurtEffects.accept(target);
        } else {
            super.doPostHurtEffects(target);
        }
    }

    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startVec, Vec3 endVec) {
        final ContextUtils.ArrowVec3Context context = new ContextUtils.ArrowVec3Context(startVec, endVec, this);
        return builder.findHitEntity != null ? builder.findHitEntity.apply(context) : super.findHitEntity(startVec, endVec);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return builder.canHitEntity != null ? builder.canHitEntity.test(entity) : super.canHitEntity(entity);
    }


    @Override
    public void playerTouch(Player player) {
        if (builder.playerTouch != null) {
            final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(player, this);
            builder.playerTouch.accept(context);
        } else {
            super.playerTouch(player);
        }
    }

    @Override
    protected boolean tryPickup(Player player) {
        return builder.tryPickup != null ? builder.tryPickup.test(player) : super.tryPickup(player);
    }


    @Override
    public void setBaseDamage(double damage) {
        if (builder.setBaseDamage != null) {
            builder.setBaseDamage.accept(damage);
        } else {
            super.setBaseDamage(damage);
        }
    }

    @Override
    public void setKnockback(int knockback) {
        if (builder.setKnockback != null) {
            builder.setKnockback.accept(knockback);
        } else {
            super.setKnockback(knockback);
        }
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable : super.isAttackable();
    }

    @Override
    protected float getWaterInertia() {
        return builder.setWaterInertia != null ? builder.setWaterInertia : super.getWaterInertia();
    }
}
