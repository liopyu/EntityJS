package net.liopyu.entityjs.builders;


import net.liopyu.entityjs.entities.BaseEntityJS;
import net.liopyu.entityjs.entities.BaseLivingEntityJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;

public class BaseEntityJSBuilder extends BaseEntityBuilder<BaseEntityJS> {

    public BaseEntityJSBuilder(ResourceLocation i) {
        super(i);
    }

    @Override
    public EntityType.EntityFactory<BaseEntityJS> factory() {
        return (type, level) -> new BaseEntityJS(this, type, level);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        final AttributeSupplier.Builder builder = BaseLivingEntityJS.createLivingAttributes();
        builder.add(Attributes.ATTACK_DAMAGE);
        builder.add(Attributes.ATTACK_SPEED);
        builder.add(Attributes.ATTACK_KNOCKBACK);
        return BaseLivingEntityJS.createLivingAttributes();
    }
}