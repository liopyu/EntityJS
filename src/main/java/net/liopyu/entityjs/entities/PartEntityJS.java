package net.liopyu.entityjs.entities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.entity.PartEntity;

import javax.annotation.Nullable;

public class PartEntityJS extends PartEntity<AnimalEntityJS> {
    public final AnimalEntityJS parentMob;
    public final String name;
    private final EntityDimensions size;
    public float width;
    public float height;

    public PartEntityJS(AnimalEntityJS pParentMob, String pName, float pWidth, float pHeight) {
        super(pParentMob);
        this.size = EntityDimensions.scalable(pWidth, pHeight);
        this.refreshDimensions();
        this.parentMob = pParentMob;
        this.name = pName;
        this.width = pWidth;
        this.height = pHeight;
    }

    protected void defineSynchedData() {
    }

    protected void readAdditionalSaveData(CompoundTag pCompound) {
    }

    protected void addAdditionalSaveData(CompoundTag pCompound) {
    }

    public boolean isPickable() {
        return true;
    }

    @Nullable
    public ItemStack getPickResult() {
        return this.parentMob.getPickResult();
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            return super.hurt(pSource, pAmount);
        }
    }

    public boolean is(Entity pEntity) {
        return this == pEntity || this.parentMob == pEntity;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    public EntityDimensions getDimensions(Pose pPose) {
        return this.size;
    }

    public boolean shouldBeSaved() {
        return false;
    }
}