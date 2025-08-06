package net.liopyu.entityjs.client.living.model;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemArmorJSBuilder<T extends LivingEntity & IAnimatableJS> {
    public Function<T, Boolean> addArmorItemLayer = null;
    public final T entity;
    public Map<String, EquipmentSlot> armorBoneToSlotMap = new HashMap<>();
    public transient Consumer<ContextUtils.RenderBoneContext<T>> renderBone;

    public transient Consumer<ContextUtils.VanillaArmorRenderContext<T>> vanillaArmorRenderConsumer;

    public ItemArmorJSBuilder(T entity) {
        this.entity = entity;
    }

    @Info(value = """
            A Consumer controlling custom rendering logic for a vanilla armor piece on a model bone.
            
            Example usage:
            ```javascript
            armorBuilder.renderArmor(context => {
                let {
                    poseStack,
                    bone,
                    slot,
                    armorStack,
                    modelPart,
                    entity,
                    bufferSource,
                    partialTick,
                    packedLight,
                    packedOverlay
                } = context
            
                if (bone.name === "left_leg") {
                    poseStack.translate(0.1, 0.0, 0.0)
                    poseStack.scale(1.2, 1.2, 1.2)
                }
            })
            ```
            """)

    public ItemArmorJSBuilder<T> renderArmor(Consumer<ContextUtils.VanillaArmorRenderContext<T>> consumer) {
        this.vanillaArmorRenderConsumer = consumer;
        return this;
    }

    @Info(value = """
            A Consumer determining custom rendering logic for an armor model bone.
            
            This runs during the armor rendering pass, allowing you to manipulate specific armor bones —
            for example, applying dynamic offsets or transformations per bone before rendering.
            
            Example usage:
            ```javascript
            armorBuilder.renderBone(context => {
                let {
                    poseStack,
                    bone,
                    entity,
                    partialTick,
                    bufferSource,
                    packedLight,
                    packedOverlay
                } = context
            
                if (bone.name == "helmet") {
                    poseStack.translate(0, 0.05, 0)
                    poseStack.scale(1.1, 1.1, 1.1)
                }
            })
            ```
            """)

    public ItemArmorJSBuilder<T> renderBone(Consumer<ContextUtils.RenderBoneContext<T>> renderBone) {
        this.renderBone = renderBone;
        return this;
    }

    public ItemArmorJSBuilder<T> setLeftShoulderArmorBone(Function<T, String> boneName) {
        this.armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.CHEST);
        return this;
    }

    public ItemArmorJSBuilder<T> setRightShoulderArmorBone(Function<T, String> boneName) {
        this.armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.CHEST);
        return this;
    }

    public ItemArmorJSBuilder<T> setLeftLegArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.LEGS);
        return this;
    }

    public ItemArmorJSBuilder<T> setRightLegArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.LEGS);
        return this;
    }

    public ItemArmorJSBuilder<T> setLeftFootArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.FEET);
        return this;
    }

    public ItemArmorJSBuilder<T> setRightFootArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.FEET);
        return this;
    }

    public ItemArmorJSBuilder<T> withArmorItemLayer(Function<T, Boolean> predicate) {
        this.addArmorItemLayer = predicate;
        return this;
    }

    public ItemArmorJSBuilder<T> withArmorBoneMapping(Map<String, EquipmentSlot> mapping) {
        this.armorBoneToSlotMap = mapping;
        return this;
    }

    public ItemArmorJSBuilder<T> setHeadArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.HEAD);
        return this;
    }

    public ItemArmorJSBuilder<T> setChestArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.CHEST);
        return this;
    }

    public ItemArmorJSBuilder<T> setLegArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.LEGS);
        return this;
    }

    public ItemArmorJSBuilder<T> setFeetArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.FEET);
        return this;
    }

    public ItemArmorJSBuilder<T> setMainHandArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.MAINHAND);
        return this;
    }

    public ItemArmorJSBuilder<T> setOffhandArmorBone(Function<T, String> boneName) {
        armorBoneToSlotMap.put(boneName.apply(entity), EquipmentSlot.OFFHAND);
        return this;
    }
}