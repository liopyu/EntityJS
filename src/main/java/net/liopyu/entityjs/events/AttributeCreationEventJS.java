package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.liopyu.entityjs.util.implementation.EACAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class AttributeCreationEventJS extends EventJS {

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
            for (Attribute attr : ForgeRegistries.ATTRIBUTES.getValues()) {
                if (supplier.hasAttribute(attr)) {
                    present.add(attr);
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
        public void add(Attribute attribute, double value) {
            builder.add(attribute, value);
        }

        @Info(value = "Adds the given attribute with default value", params = {
                @Param(name = "attribute", value = "Attribute or resource location string"),
                @Param(name = "value", value = "Default value for the attribute")
        })
        public void add(Object attribute, double value) {
            if (attribute instanceof String s) {
                ResourceLocation rl = new ResourceLocation(s.toLowerCase());
                Attribute att = ForgeRegistries.ATTRIBUTES.getValue(rl);
                if (att != null) {
                    builder.add(att, value);
                } else {
                    EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Unable to add attribute, attribute " + attribute + " does not exist");
                }
            } else if (attribute instanceof Attribute att) {
                builder.add(att, value);
            } else {
                EntityJSHelperClass.logErrorMessageOnce("[EntityJS]: Invalid attribute type: " + attribute);
            }
        }
    }
}