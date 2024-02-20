package net.liopyu.entityjs.entities;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.liopyu.entityjs.builders.*;
import net.liopyu.entityjs.util.ContextUtils;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ArrowEntityJS extends AbstractArrow implements IArrowEntityJS {

    public final ArrowEntityJSBuilder builder;
    @NotNull
    protected ItemStack pickUpStack;
    private double baseDamage;
    private int knockback;
    private final SoundEvent soundEvent;
    @Nullable
    private IntOpenHashSet piercingIgnoreEntityIds;
    @Nullable
    private List<Entity> piercedAndKilledEntities;

    public ArrowEntityJS(ArrowEntityJSBuilder builder, EntityType<? extends AbstractArrow> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
        this.soundEvent = this.getDefaultHitGroundSoundEvent();
        this.baseDamage = builder.setBaseDamage;
    }

    public ArrowEntityJS(Level level, LivingEntity shooter, ArrowEntityJSBuilder builder) {
        super(builder.get(), shooter, level);
        this.builder = builder;
        pickUpStack = ItemStack.EMPTY;
        this.soundEvent = this.getDefaultHitGroundSoundEvent();
        this.baseDamage = builder.setBaseDamage;
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

    public String entityName() {
        return this.getType().toString();
    }

    //Arrow Overrides
    @Override
    protected void tickDespawn() {
        if (builder.tickDespawn != null) {
            builder.tickDespawn.accept(this);
        } else super.tickDespawn();
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        if (builder != null && builder.defaultHitGroundSoundEvent != null) {
            return Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(builder.defaultHitGroundSoundEvent));
        }
        return super.getDefaultHitGroundSoundEvent();
    }

    @Override
    public void setSoundEvent(SoundEvent pSoundEvent) {
        if (builder.setSoundEvent != null) {
            this.setSoundEvent(Objects.requireNonNull(ForgeRegistries.SOUND_EVENTS.getValue(builder.setSoundEvent)));
        } else {
            super.setSoundEvent(pSoundEvent);
        }
    }


    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        if (builder.doPostHurtEffects != null) {
            final ContextUtils.ArrowLivingEntityContext context = new ContextUtils.ArrowLivingEntityContext(this, target);
            builder.doPostHurtEffects.accept(context);
        } else {
            super.doPostHurtEffects(target);
        }
    }

    @Override
    protected float getWaterInertia() {
        return builder.setWaterInertia != null ? builder.setWaterInertia : super.getWaterInertia();
    }


    @Override
    protected boolean tryPickup(Player player) {
        if (builder.tryPickup == null) return super.tryPickup(player);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.tryPickup.apply(player), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid value for tryPickup from entity: " + entityName() + ". Value: " + builder.tryPickup.apply(player) + ". Must be a boolean. Defaulting to " + super.tryPickup(player));
        return super.tryPickup(player);
    }

    private void resetPiercedEntities() {
        if (this.piercedAndKilledEntities != null) {
            this.piercedAndKilledEntities.clear();
        }

        if (this.piercingIgnoreEntityIds != null) {
            this.piercingIgnoreEntityIds.clear();
        }

    }

    public double setDamageFunction() {
        if (builder.setDamageFunction != null) {
            Object obj = EntityJSHelperClass.convertObjectToDesired(builder.setDamageFunction.apply(this), "double");
            if (obj != null) return (double) obj;
            EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid setDamageFunction for arrow builder: " + builder.setDamageFunction.apply(this) + ". Must be a double. Defaulting to super method: " + 0);
            return 0;
        }
        return 0;
    }

    public void setBaseDamage(double pBaseDamage) {
        this.baseDamage = pBaseDamage + builder.setBaseDamage + setDamageFunction();
    }

    public double getBaseDamage() {
        return this.baseDamage;
    }


    public void setKnockback(int pKnockback) {
        if (builder.setKnockback != null) this.knockback = builder.setKnockback + pKnockback;
        else this.knockback = pKnockback;
    }

    public int getKnockback() {
        return this.knockback;
    }

    @Override
    public void setEnchantmentEffectsFromEntity(LivingEntity pShooter, float pVelocity) {
        int i = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER_ARROWS, pShooter);
        int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH_ARROWS, pShooter);
        this.setBaseDamage((double) (pVelocity * 2.0F) + this.random.triangle((double) this.level.getDifficulty().getId() * 0.11, 0.57425));
        if (i > 0) {
            this.setBaseDamage(this.getBaseDamage() + (double) i * 0.5 + 0.5);
        }

        if (j > 0) {
            this.setKnockback(j);
        }

        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAMING_ARROWS, pShooter) > 0) {
            this.setSecondsOnFire(100);
        }

    }

    //base entity overrides
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        if (builder.shouldRenderAtSqrDistance == null) super.shouldRenderAtSqrDistance(distance);
        final ContextUtils.EntitySqrDistanceContext context = new ContextUtils.EntitySqrDistanceContext(distance, this);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.shouldRenderAtSqrDistance.apply(context), "boolean");
        if (obj != null) return (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid shouldRenderAtSqrDistance for arrow builder: " + builder.shouldRenderAtSqrDistance.apply(context) + ". Must be a boolean. Defaulting to super method: " + super.shouldRenderAtSqrDistance(distance));
        return super.shouldRenderAtSqrDistance(distance);
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
    public void playerTouch(Player player) {
        if (builder != null && builder.playerTouch != null) {
            final ContextUtils.EntityPlayerContext context = new ContextUtils.EntityPlayerContext(player, this);
            builder.playerTouch.accept(context);
        } else {
            super.playerTouch(player);
        }
    }

    @Override
    public boolean isAttackable() {
        return builder.isAttackable != null ? builder.isAttackable : super.isAttackable();
    }

    //Projectile Overrides

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        Entity entity = pResult.getEntity();
        float f = (float) this.getDeltaMovement().length();
        int i = Mth.ceil(Mth.clamp((double) f * this.baseDamage, 0.0, 2.147483647E9));
        if (this.getPierceLevel() > 0) {
            if (this.piercingIgnoreEntityIds == null) {
                this.piercingIgnoreEntityIds = new IntOpenHashSet(5);
            }

            if (this.piercedAndKilledEntities == null) {
                this.piercedAndKilledEntities = Lists.newArrayListWithCapacity(5);
            }

            if (this.piercingIgnoreEntityIds.size() >= this.getPierceLevel() + 1) {
                this.discard();
                return;
            }

            this.piercingIgnoreEntityIds.add(entity.getId());
        }

        if (this.isCritArrow()) {
            long j = (long) this.random.nextInt(i / 2 + 2);
            i = (int) Math.min(j + (long) i, 2147483647L);
        }

        Entity entity1 = this.getOwner();
        DamageSource damagesource;
        if (entity1 == null) {
            damagesource = DamageSource.arrow(this, this);
        } else {
            damagesource = DamageSource.arrow(this, entity1);
            if (entity1 instanceof LivingEntity) {
                ((LivingEntity) entity1).setLastHurtMob(entity);
            }
        }

        boolean flag = entity.getType() == EntityType.ENDERMAN;
        int k = entity.getRemainingFireTicks();
        if (this.isOnFire() && flag != builder.canHitEnderman) {
            entity.setSecondsOnFire(5);
        }

        if (entity.hurt(damagesource, (float) i)) {
            if (!builder.canHitEnderman) {
                if (flag) {
                    return;
                }
            }

            if (entity instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entity;
                if (!this.level.isClientSide && this.getPierceLevel() <= 0) {
                    livingentity.setArrowCount(livingentity.getArrowCount() + 1);
                }

                if (this.knockback > 0) {
                    double d0 = Math.max(0.0, 1.0 - livingentity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
                    Vec3 vec3 = this.getDeltaMovement().multiply(1.0, 0.0, 1.0).normalize().scale((double) this.knockback * 0.6 * d0);
                    if (vec3.lengthSqr() > 0.0) {
                        livingentity.push(vec3.x, 0.1, vec3.z);
                    }
                }

                if (!this.level.isClientSide && entity1 instanceof LivingEntity) {
                    EnchantmentHelper.doPostHurtEffects(livingentity, entity1);
                    EnchantmentHelper.doPostDamageEffects((LivingEntity) entity1, livingentity);
                }

                this.doPostHurtEffects(livingentity);
                if (entity1 != null && livingentity != entity1 && livingentity instanceof Player && entity1 instanceof ServerPlayer && !this.isSilent()) {
                    ((ServerPlayer) entity1).connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.ARROW_HIT_PLAYER, 0.0F));
                }

                if (!entity.isAlive() && this.piercedAndKilledEntities != null) {
                    this.piercedAndKilledEntities.add(livingentity);
                }

                if (!this.level.isClientSide && entity1 instanceof ServerPlayer) {
                    ServerPlayer serverplayer = (ServerPlayer) entity1;
                    if (this.piercedAndKilledEntities != null && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, this.piercedAndKilledEntities);
                    } else if (!entity.isAlive() && this.shotFromCrossbow()) {
                        CriteriaTriggers.KILLED_BY_CROSSBOW.trigger(serverplayer, Arrays.asList(entity));
                    }
                }
            }

            this.playSound(this.soundEvent, 1.0F, 1.2F / (this.random.nextFloat() * 0.2F + 0.9F));
            if (this.getPierceLevel() <= 0) {
                this.discard();
            }
        } else {
            entity.setRemainingFireTicks(k);
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.1));
            this.setYRot(this.getYRot() + 180.0F);
            this.yRotO += 180.0F;
            if (!this.level.isClientSide && this.getDeltaMovement().lengthSqr() < 1.0E-7) {
                if (this.pickup == AbstractArrow.Pickup.ALLOWED) {
                    this.spawnAtLocation(this.getPickupItem(), 0.1F);
                }

                this.discard();
            }
        }
        if (builder.onHitEntity != null) {
            final ContextUtils.ArrowEntityHitContext context = new ContextUtils.ArrowEntityHitContext(pResult, this);
            builder.onHitEntity.accept(context);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putDouble("damage", this.baseDamage);
    }

    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("damage", 99)) {
            this.baseDamage = pCompound.getDouble("damage");
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        this.resetPiercedEntities();
        if (builder.onHitBlock != null) {
            final ContextUtils.ArrowBlockHitContext context = new ContextUtils.ArrowBlockHitContext(result, this);
            builder.onHitBlock.accept(context);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (builder.canHitEntity == null) return super.canHitEntity(entity);
        Object obj = EntityJSHelperClass.convertObjectToDesired(builder.canHitEntity.apply(entity), "boolean");
        if (obj != null) return super.canHitEntity(entity) && (boolean) obj;
        EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid canHitEntity for arrow builder: " + builder.canHitEntity.apply(entity) + ". Must be a boolean. Defaulting to super method: " + super.canHitEntity(entity));
        return super.canHitEntity(entity);
    }
}
