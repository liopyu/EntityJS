package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModifyAttributeEventJS extends EventJS {

    public static final List<Consumer<ModifyAttributeEventJS>> HANDLERS = new ArrayList<>();

    public ModifyAttributeEventJS() {
    }

    @Info(value = "Modifies the given entity type's attributes", params = {
            @Param(name = "entityType", value = "The entity type whose default attributes are to be modified"),
            @Param(name = "attributes", value = "A consumer for setting the default attributes and their values")
    })
    public void modify(EntityType<? extends LivingEntity> entityType, Consumer<AttributeModificationHelper> attributes) {
        final AttributeModificationHelper helper = new AttributeModificationHelper(entityType);
        attributes.accept(helper);
    }

    @Info(value = "Returns a list of all entity types that can have their attributes modified by this event")
    public List<EntityType<? extends LivingEntity>> getAllTypes() {
        return Registry.ENTITY_TYPE.stream()
                .filter(DefaultAttributes::hasSupplier)
                .map(entityType -> (EntityType<? extends LivingEntity>) entityType)
                .collect(Collectors.toList());
    }

    @Info(value = "Returns a list of all attributes the given entity type has by default")
    public List<Attribute> getAttributes(EntityType<? extends LivingEntity> entityType) {
        final List<Attribute> present = new ArrayList<>();
        LivingEntity entity = entityType.create(null);
        if (entity != null) {
            entity.getAttributes().getSyncableAttributes().forEach(attributeInstance -> present.add(attributeInstance.getAttribute()));
        }
        return present;
    }

    public static class AttributeModificationHelper {
        private final EntityType<? extends LivingEntity> type;

        public AttributeModificationHelper(EntityType<? extends LivingEntity> type) {
            this.type = type;
        }

        @Info(value = "Adds the given attribute to the entity type, using its default value")
        public void add(Attribute attribute) {
            modifyAttribute(attribute, attribute.getDefaultValue());
        }

        @Info(value = "Adds the given attribute to the entity type, using the provided default value", params = {
                @Param(name = "attribute", value = "The attribute to add"),
                @Param(name = "defaultValue", value = "The default value of the attribute")
        })
        public void add(Object attribute, double defaultValue) {
            if (attribute instanceof String string) {
                ResourceLocation id = new ResourceLocation(string.toLowerCase());
                Attribute att = Registry.ATTRIBUTE.get(id);
                if (att != null) {
                    modifyAttribute(att, defaultValue);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Unable to add attribute, attribute " + attribute + " does not exist");
                }
            } else if (attribute instanceof Attribute att) {
                modifyAttribute(att, defaultValue);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Unable to add attribute, attribute: " + attribute + ". Must be of type EntityAttribute or resource location. Example: \"minecraft:generic.max_health\"");
            }
        }

        private void modifyAttribute(Attribute attribute, double defaultValue) {
            LivingEntity entity = type.create(null);
            if (entity != null) {
                AttributeInstance instance = entity.getAttributes().getInstance(attribute);
                if (instance != null) {
                    instance.setBaseValue(defaultValue);
                }
            }
        }
    }
}
