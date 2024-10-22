package net.liopyu.entityjs.builders.misc;

import net.minecraft.world.entity.Mob;

import java.util.function.Consumer;

public class JumpControlJSBuilder {

    public transient Consumer<Mob> jump;
    public transient Consumer<Mob> tick;

    public JumpControlJSBuilder jump(Consumer<Mob> jump) {
        this.jump = jump;
        return this;
    }

    public JumpControlJSBuilder tick(Consumer<Mob> tick) {
        this.tick = tick;
        return this;
    }
}
