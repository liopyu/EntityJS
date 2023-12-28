package net.liopyu.entityjs.entities;

import net.liopyu.entityjs.builders.BaseEntityJSBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Collection;

public class BaseEntityJS extends Entity implements IAnimatableJS {

    protected final BaseEntityJSBuilder builder;

    public BaseEntityJS(BaseEntityJSBuilder builder, EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.builder = builder;
        this.getattribute(Attributes.MAX_HEALTH).setBaseValue(builder.getMaxHealth());
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(builder.getAttackDamage());
        this.getAttribute(Attributes.ATTACK_SPEED).setBaseValue(builder.getAttackSpeed());
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(builder.getMovementSpeed());
    }


    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {

    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return null;
    }

    @Override
    public void registerControllers(AnimationData data) {

    }


    @Override
    public AnimationFactory getFactory() {
        return null;
    }

    public static AttributeSupplier createMobAttributes() {
        return AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.MOVEMENT_SPEED)
                .build();
    }
}