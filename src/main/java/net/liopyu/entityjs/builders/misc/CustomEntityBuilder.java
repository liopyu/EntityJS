package net.liopyu.entityjs.builders.misc;

import dev.latvian.mods.kubejs.registry.AdditionalObjectRegistry;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.liopyu.entityjs.item.SpawnEggItemBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;


public class CustomEntityBuilder extends CustomEntityJSBuilder {

    private final Class<?> entityClass;
    public transient SpawnEggItemBuilder eggItem;
    public transient boolean noEggItem = false;

    public CustomEntityBuilder(Identifier i, Class<? extends LivingEntity> entityClass) {
        super(i);
        this.entityClass = EntityReflection.createEntityClass(entityClass);
        if (Mob.class.isAssignableFrom(entityClass)) {
            this.eggItem = new SpawnEggItemBuilder(id, this)
                    .backgroundColor(0)
                    .highlightColor(0);
        }
    }

    @Info(value = "Indicates that no egg item should be created for this entity type")
    public CustomEntityBuilder noEggItem() {
        this.noEggItem = true;
        return this;
    }

    @Info(value = "Creates a spawn egg item for this entity type")
    public CustomEntityBuilder eggItem(Consumer<SpawnEggItemBuilder> eggItem) {
        this.eggItem = new SpawnEggItemBuilder(id, this);
        eggItem.accept(this.eggItem);
        return this;
    }

    @HideFromJS
    @Override
    public void createAdditionalObjects(AdditionalObjectRegistry registry) {
        if (noEggItem || !Mob.class.isAssignableFrom(entityClass)) return;
        registry.add(Registries.ITEM, eggItem);
    }

    @Override
    public AttributeSupplier.Builder getAttributeBuilder() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.SPAWN_REINFORCEMENTS_CHANCE)
                .add(Attributes.ATTACK_KNOCKBACK)
                .add(Attributes.FLYING_SPEED)
                .add(Attributes.JUMP_STRENGTH)
                .add(Attributes.LUCK)
                .add(Attributes.ATTACK_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.ARMOR);
    }


    @Override
    public EntityType.EntityFactory<? extends LivingEntity> factory() {
        return (type, world) -> {
            if (entityClass == null) {
                throw new IllegalStateException("Entity class not set! Call .set(Class<T>) before using this builder.");
            }
            try {
                return (LivingEntity) entityClass.getDeclaredConstructor(EntityType.class, Level.class).newInstance(type, world);
            } catch (Exception e) {
                throw new RuntimeException("Failed to dynamically instantiate entity: " + id, e);
            }
        };
    }
}