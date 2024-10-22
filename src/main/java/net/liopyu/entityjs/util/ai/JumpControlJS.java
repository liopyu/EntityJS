package net.liopyu.entityjs.util.ai;

import net.liopyu.entityjs.builders.misc.JumpControlJSBuilder;
import net.liopyu.entityjs.util.EntityJSHelperClass;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;

public class JumpControlJS extends JumpControl {

    private final JumpControlJSBuilder builder;

    private String entityName() {
        return mob.getType().toString();
    }

    public JumpControlJS(Mob mob, JumpControlJSBuilder builder) {
        super(mob);
        this.builder = builder;
    }

    @Override
    public void jump() {
        if (builder.jump != null) {
            EntityJSHelperClass.consumerCallback(builder.jump, mob, "[EntityJS]: Error in " + entityName() + " Look Control builder for field: jump.");
        } else {
            super.jump();
        }
    }

    @Override
    public void tick() {
        if (builder.tick != null) {
            EntityJSHelperClass.consumerCallback(builder.tick, mob, "[EntityJS]: Error in " + entityName() + " Jump Control builder for field: tick.");
        } else {
            super.tick();
        }
    }
}
