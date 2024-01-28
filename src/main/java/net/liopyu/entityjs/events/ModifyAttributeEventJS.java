package net.liopyu.entityjs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.kubejs.typings.Param;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ModifyAttributeEventJS extends EventJS {

    private final EntityAttributeModificationEvent event;

    public ModifyAttributeEventJS(EntityAttributeModificationEvent event) {
        this.event = event;
    }

    @Info(value = "Modifies the given entity type's attributes", params = {
            @Param(name = "entityType", value = "The entity type whose default attributes are to be modified"),
            @Param(name = "attributes", value = "A consumer for setting the default attributes and their values")
    })
    public void modify(EntityType<? extends LivingEntity> entityType, Consumer<AttributeModificationHelper> attributes) {
        final AttributeModificationHelper helper = new AttributeModificationHelper(entityType, event);
        attributes.accept(helper);
    }

    @Info(value = "Returns a list of all entity types that can have their attributes modified by this event")
    public List<EntityType<? extends LivingEntity>> getAllTypes() {
        return event.getTypes();
    }

    @Info(value = "Returns a list of all attributes the given entity type has by default")
    public List<Attribute> getAttributes(EntityType<? extends LivingEntity> entityType) {
        final List<Attribute> present = new ArrayList<>();
        for (Attribute attribute : ForgeRegistries.ATTRIBUTES.getValues()) {
            if (event.has(entityType, attribute)) {
                present.add(attribute);
            }
        }
        return present;
    }

    public record AttributeModificationHelper(@HideFromJS EntityType<? extends LivingEntity> type, @HideFromJS EntityAttributeModificationEvent event) {

        @Info(value = """
                Adds the given attribute to the entity type, using its default value
                
                It is safe to add an attribute that an entity type already has
                """)
        public void add(Attribute attribute) {
            event.add(type, attribute);
        }

        @Info(value = """
                Adds the given attribute to the entity type, using the provided default value
                
                It is safe to add an attribute that an entity type already has
                """, params = {
                @Param(name = "attribute", value = "The attribute to add"),
                @Param(name = "defaultValue", value = "The default value of the attribute")
        })
        public void add(Attribute attribute, double defaultValue) {
            event.add(type, attribute, defaultValue);
        }
    }
}
