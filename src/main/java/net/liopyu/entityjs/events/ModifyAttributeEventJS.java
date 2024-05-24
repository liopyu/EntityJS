package net.liopyu.entityjs.events;

import com.google.common.collect.ImmutableList;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
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
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ModifyAttributeEventJS extends EventJS {
    private final List<EntityType<? extends LivingEntity>> entityTypes;

    public ModifyAttributeEventJS() {
        this.entityTypes = ImmutableList.copyOf(
                Registry.ENTITY_TYPE.stream()
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
    }

    @Info(value = "Returns a list of all entity types that can have their attributes modified by this event")
    public List<EntityType<? extends LivingEntity>> getAllTypes() {
        return entityTypes;
    }

    @Info(value = "Returns a list of all attributes the given entity type has by default")
    public List<Attribute> getAttributes(EntityType<? extends LivingEntity> entityType) {
        AttributeSupplier defaultAttributeSupplier = DefaultAttributes.getSupplier(entityType);
        List<Attribute> defaultAttributes = new ArrayList<>();
        for (Attribute attribute : Registry.ATTRIBUTE) {
            if (defaultAttributeSupplier.hasAttribute(attribute)) {
                defaultAttributes.add(attribute);
            }
        }
        return defaultAttributes;
    }

    public record AttributeModificationHelper(@HideFromJS EntityType<? extends LivingEntity> type) {

        @Info(value = """
                Adds the given attribute to the entity type, using its default value
                                
                It is safe to add an attribute that an entity type already has
                """)
        public void add(Attribute attribute) {
            EntityAttributeRegistry.register(() -> type, () -> AttributeSupplier.builder().add(attribute));
        }

        @Info(value = """
                Adds the given attribute to the entity type, using the provided default value
                                
                It is safe to add an attribute that an entity type already has
                """, params = {
                @Param(name = "attribute", value = "The attribute to add"),
                @Param(name = "defaultValue", value = "The default value of the attribute")
        })
        public void add(Object attribute, double defaultValue) {
            if (attribute instanceof String string) {
                ResourceLocation stringLocation = new ResourceLocation(string.toLowerCase());
                Attribute att = Registry.ATTRIBUTE.get(stringLocation);
                if (att != null) {
                    EntityAttributeRegistry.register(() -> type, () -> AttributeSupplier.builder().add(att, defaultValue));
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Unable to add attribute, attribute " + attribute + " does not exist");
                }
            } else if (attribute instanceof Attribute att) {
                EntityAttributeRegistry.register(() -> type, () -> AttributeSupplier.builder().add(att, defaultValue));
            } else
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Unable to add attribute, attribute: " + attribute + ". Must be of type Attribute or resource location. Example: \"minecraft:generic.max_health\"");
        }
    }
}