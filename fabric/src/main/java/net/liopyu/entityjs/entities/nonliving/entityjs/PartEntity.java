package net.liopyu.entityjs.entities.nonliving.entityjs;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public abstract class PartEntity<T extends Entity> extends Entity {
    private final T parent;

    public PartEntity(T parent) {
        super(parent.getType(), parent.level());
        this.parent = parent;
    }

    public T getParent() {
        return parent;
    }


    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundTag compoundTag) {
    }

    protected void addAdditionalSaveData(CompoundTag compoundTag) {
    }

    @Nullable
    public ItemStack getPickResult() {
        return parent.getPickResult();
    }

    public boolean hurt(DamageSource damageSource, float f) {
        return !this.isInvulnerableTo(damageSource) && this.parent.hurt(damageSource, f);
    }

    public boolean is(Entity entity) {
        return this == entity;
    }

   /* public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }*/

    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public void baseTick() {
        if (parent.isRemoved()) {
            this.setRemoved(parent.getRemovalReason());
        }
        super.baseTick();
    }
}
