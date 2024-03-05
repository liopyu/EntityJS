package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.PartEntityJSBuilder;
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
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class PartEntityJS extends PartEntity<AnimalEntityJS> implements IPartEntityJS {
    private final PartEntityJSBuilder builder;
    public final AnimalEntityJS parentMob;
    public final String name;
    private final EntityDimensions size;

    public PartEntityJS(PartEntityJSBuilder builder, AnimalEntityJS parentMob, String name, EntityDimensions size) {
        super(parentMob);
        this.builder = builder;
        this.parentMob = parentMob;
        this.name = name;
        this.size = size;
    }

    @Override
    public PartEntityJSBuilder getPartEntityBuilder() {
        return builder;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Nullable
    public ItemStack getPickResult() {
        return this.parentMob.getPickResult();
    }

    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        return !this.isInvulnerableTo(pSource) && this.parentMob.hurt(pSource, pAmount);
    }

    public boolean is(@NotNull Entity pEntity) {
        return this == pEntity || this.parentMob == pEntity;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    public @NotNull EntityDimensions getDimensions(Pose pPose) {
        return this.size;
    }

    public boolean shouldBeSaved() {
        return false;
    }
}
