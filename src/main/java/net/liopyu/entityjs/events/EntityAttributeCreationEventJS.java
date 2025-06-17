package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public class EntityAttributeCreationEventJS implements KubeEvent {
    /*private final EntityAttributeCreationEvent event;

    public EntityAttributeCreationEventJS(EntityAttributeCreationEvent event) {
        this.event = event;
    }

    public void addOrModify(EntityType<? extends LivingEntity> type, java.util.function.Consumer<Builder> modify) {
        AttributeSupplier existing = event.map.get(type);
        Builder builder;

        if (existing != null) {
            builder = AttributeSupplier.builder();
            existing.save().forEach(builder::add);
        } else {
            builder = AttributeSupplier.builder();
        }

        modify.accept(builder);

        if (!DefaultAttributes.hasSupplier(type)) {
            event.put(type, builder.build());
        } else {
            event.map.put(type, builder.build());
        }
    }*/
}
