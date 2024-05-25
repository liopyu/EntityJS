package net.liopyu.entityjs.events;

import com.google.common.collect.ImmutableList;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModifyAttributeEventJS extends EventJS {
    public static final Map<EntityType<? extends LivingEntity>, List<Attribute>> modifiedAttributesMap = new HashMap<>();
    private final List<EntityType<? extends LivingEntity>> entityTypes;

    public ModifyAttributeEventJS() {
        this.entityTypes = ImmutableList.copyOf(
                BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(DefaultAttributes::hasSupplier)
                        .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                        .collect(Collectors.toList())
        );
    }

    @Info(value = "Modifies the given entity type's attributes", params = {
            @Param(name = "entityType", value = "The entity type whose default attributes are to be modified"),
            @Param(name = "attributes", value = "A consumer for setting the default attributes and their values")
    })
    public void modify(EntityType<? extends LivingEntity> entityType, Consumer<AttributeModificationHelper> attributes) {
        final AttributeModificationHelper helper = new AttributeModificationHelper(entityType);
        attributes.accept(helper);
        AttributeSupplier defaultAttributeSupplier = DefaultAttributes.getSupplier(entityType);
        List<Attribute> existingAttributes = new ArrayList<>();
        Map<Attribute, Double> defaultValues = new HashMap<>();
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            if (defaultAttributeSupplier.hasAttribute(attribute)) {
                existingAttributes.add(attribute);
                defaultValues.put(attribute, defaultAttributeSupplier.getValue(attribute));
            }
        }
        List<Attribute> newAttributes = helper.getNewAttributes();
        Map<Attribute, Double> newAttributeDefaultValues = helper.getDefaultValues();
        List<Attribute> mergedAttributes = new ArrayList<>(existingAttributes);
        mergedAttributes.addAll(newAttributes);
        EntityAttributeRegistry.register(() -> entityType, () -> {
            AttributeSupplier.Builder builder = AttributeSupplier.builder();
            for (Attribute attribute : mergedAttributes) {
                if (newAttributeDefaultValues.containsKey(attribute)) {
                    builder.add(attribute, newAttributeDefaultValues.get(attribute));
                } else if (defaultValues.containsKey(attribute)) {
                    builder.add(attribute, defaultValues.get(attribute));
                } else {
                    builder.add(attribute);
                }
            }
            return builder;
        });
        modifiedAttributesMap.put(entityType, ImmutableList.copyOf(mergedAttributes));

    }

    @Info(value = "Returns a list of all entity types that can have their attributes modified by this event")
    public List<EntityType<? extends LivingEntity>> getAllTypes() {
        return entityTypes;
    }

    @Info(value = "Returns a list of all attributes the given entity type has by default")
    public List<Attribute> getAttributes(EntityType<? extends LivingEntity> entityType) {
        AttributeSupplier defaultAttributeSupplier = DefaultAttributes.getSupplier(entityType);
        List<Attribute> defaultAttributes = new ArrayList<>();
        for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
            if (defaultAttributeSupplier.hasAttribute(attribute)) {
                defaultAttributes.add(attribute);
            }
        }
        return defaultAttributes;
    }

    public static class AttributeModificationHelper {

        private final EntityType<? extends LivingEntity> entityType;
        private final List<Attribute> newAttributes = new ArrayList<>();
        private final Map<Attribute, Double> defaultValues = new HashMap<>();

        public AttributeModificationHelper(EntityType<? extends LivingEntity> entityType) {
            this.entityType = entityType;
        }

        @Info(value = """
                Adds the given attribute to the entity type, using its default value
                                
                It is safe to add an attribute that an entity type already has
                """)
        public void add(Attribute attribute) {
            newAttributes.add(attribute);
        }

        @Info(value = """
                Adds the given attribute to the entity type, using the provided default value
                                
                It is safe to add an attribute that an entity type already has
                """, params = {
                @Param(name = "attribute", value = "The attribute to add"),
                @Param(name = "defaultValue", value = "The default value of the attribute")
        })
        public void add(Attribute attribute, double defaultValue) {
            newAttributes.add(attribute);
            defaultValues.put(attribute, defaultValue);
        }

        @Info(value = """
                Gets a list of all attributes post-modification
                """)
        public List<Attribute> getNewAttributes() {
            return newAttributes;
        }

        @Info(value = """
                Gets a list of all attributes pre-modification
                """)
        public Map<Attribute, Double> getDefaultValues() {
            return defaultValues;
        }
    }
}