// MobType class
package net.liopyu.entityjs;

import java.util.HashMap;
import java.util.Map;

public class MobType {
    private static final Map<String, MobType> BY_NAME = new HashMap<>();

    public static final MobType CREEPER = new MobType("CREEPER");
    public static final MobType ZOMBIE = new MobType("ZOMBIE");
    public static final MobType SKELETON = new MobType("SKELETON");

    private final String name;

    private MobType(String name) {
        this.name = name;
        BY_NAME.put(name, this);
    }

    public String getName() {
        return name;
    }

    public static MobType fromName(String name) {
        return BY_NAME.get(name);
    }
}
