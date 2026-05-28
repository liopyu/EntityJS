package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.EACAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AttributeCreationEventJS implements KubeEvent {

    private final EntityAttributeCreationEvent event;

    public AttributeCreationEventJS(EntityAttributeCreationEvent event) {
        this.event = event;
    }

    public Map<EntityType<? extends LivingEntity>, AttributeSupplier> getMap() {
        return ((EACAccess) event).entityJs$getMap();
    }

    @Info(value = "Modifies the given entity type's default attributes", params = {
            @Param(name = "entityType", value = "The entity type whose default attributes are to be modified"),
            @Param(name = "attributes", value = "A consumer for modifying the default attributes and their values")
    })
    public void create(EntityType<? extends LivingEntity> entityType, Consumer<AttributeCreationHelper> attributes) {
        Map<EntityType<? extends LivingEntity>, AttributeSupplier> internalMap = ((EACAccess) event).entityJs$getMap();

        AttributeSupplier existing = internalMap.get(entityType);
        AttributeSupplier.Builder builder = (existing == null)
                ? new AttributeSupplier.Builder()
                : new AttributeSupplier.Builder(existing);

        AttributeCreationHelper helper = new AttributeCreationHelper(builder);
        attributes.accept(helper);

        internalMap.put(entityType, builder.build());
    }

    @Info(value = "Returns a list of all entity types available in the attribute map")
    public List<EntityType<? extends LivingEntity>> getAllTypes() {
        return new ArrayList<>(((EACAccess) event).entityJs$getMap().keySet());
    }

    @Info(value = "Returns a list of all attributes the given entity type has by default")
    public List<Attribute> getAttributes(EntityType<? extends LivingEntity> entityType) {
        List<Attribute> present = new ArrayList<>();
        AttributeSupplier supplier = ((EACAccess) event).entityJs$getMap().get(entityType);
        if (supplier != null) {
            for (Holder<Attribute> attr : BuiltInRegistries.ATTRIBUTE.holders().toList()) {
                if (supplier.hasAttribute(attr)) {
                    present.add(attr.value());
                }
            }
        }
        return present;
    }

    public static class AttributeCreationHelper {
        @HideFromJS
        private final AttributeSupplier.Builder builder;

        public AttributeCreationHelper(AttributeSupplier.Builder builder) {
            this.builder = builder;
        }

        @Info(value = "Adds the given attribute with default value")
        public void add(Attribute attribute) {
            Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if (id != null) {
                Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id);
                if (holder != null) {
                    builder.add(holder.get());
                } else {
                    builder.add(Holder.direct(attribute)); // fallback if holder not found
                }
            } else {
                builder.add(Holder.direct(attribute)); // not registered at all
            }
        }

        @Info(value = "Adds the given attribute with default value", params = {
                @Param(name = "attribute", value = "Attribute or resource location string"),
                @Param(name = "value", value = "Default value for the attribute")
        })
        public void add(Attribute attribute, int value) {
            Identifier id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if (id != null) {
                Optional<Holder.Reference<Attribute>> holder = BuiltInRegistries.ATTRIBUTE.getHolder(id);
                if (holder.isPresent()) {
                    builder.add(holder.get(), value);
                } else {
                    builder.add(Holder.direct(attribute), value);
                }
            } else {
                builder.add(Holder.direct(attribute));
            }
        }
    }
}