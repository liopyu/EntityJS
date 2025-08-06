package net.liopyu.entityjs.client.living.model;

import dev.latvian.mods.kubejs.typings.Info;
import net.liopyu.entityjs.entities.living.entityjs.IAnimatableJS;
import net.liopyu.entityjs.util.ContextUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Consumer;

public class ItemModelJSBuilder<T extends LivingEntity & IAnimatableJS> {
    public transient Consumer<ContextUtils.ItemBoneRenderContext<T>> renderItem;

    @Info(value = """
            A Consumer determining custom rendering logic for an item on a model bone.
            
            Example usage:
            ```javascript
            itemBuilder.render(context => {
                let {
                    poseStack,
                    bone,
                    item,
                    entity,
                    bufferSource,
                    partialTick,
                    packedLight,
                    packedOverlay
                } = context
            
                if (bone.name == "right_hand") {
                    poseStack.translate(0.1, 0.2, -0.05)
                    poseStack.scale(1.5, 1.5, 1.5)
                }
            })
            ```
            """)
    public ItemModelJSBuilder<T> renderItem(Consumer<ContextUtils.ItemBoneRenderContext<T>> render) {
        this.renderItem = render;
        return this;
    }

}