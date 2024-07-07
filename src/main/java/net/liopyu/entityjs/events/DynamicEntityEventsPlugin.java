package net.liopyu.entityjs.events;

public class DynamicEntityEventsPlugin /*extends ProbeJSPlugin*/ {
  /*  private static final Map<String, Class<?>> BUILDER_TYPES = new HashMap<>();
    private static final Map<String, Class<?>> ENTITY_TYPE_MAPPING = new HashMap<>();

    static {
        BUILDER_TYPES.put("entity_builder", ModifyEntityBuilder.class);
        BUILDER_TYPES.put("living_entity_builder", ModifyLivingEntityBuilder.class);

        ENTITY_TYPE_MAPPING.put("minecraft:allay", ModifyLivingEntityBuilder.class);
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType == ScriptType.STARTUP) {
            Wrapped.Namespace groupNamespace = new Wrapped.Namespace("EntityJSEvents");
            MethodDeclaration declaration = Statements.method("modifyEntity")
                    .param("handler", Types.lambda().param("event", Types.type(EntityModificationEventJS.class)).build())
                    .build();
            groupNamespace.addCode(declaration);
            scriptDump.addGlobal("entityjs_events", groupNamespace);
        }
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType == ScriptType.STARTUP) {
            for (Map.Entry<String, Class<?>> entry : BUILDER_TYPES.entrySet()) {
                String builderType = entry.getKey();
                Class<?> builderClass = entry.getValue();
                ClassPath classPath = new ClassPath(builderClass);
                TypeScriptFile tsFile = new TypeScriptFile(classPath);
                ClassDecl classDecl = new ClassDecl(builderType, Types.type(builderClass), List.of(), List.of());
                tsFile.addCode(classDecl);
                globalClasses.put(classPath, tsFile);
            }

            ClassPath eventClassPath = new ClassPath(EntityModificationEventJS.class);
            TypeScriptFile eventClassFile = new TypeScriptFile(eventClassPath);
            ClassDecl eventClass = generateEntityModificationClass();
            eventClassFile.addCode(eventClass);
            globalClasses.put(eventClassPath, eventClassFile);
        }
    }

    private static ClassDecl generateEntityModificationClass() {
        *//*ClassDecl.Builder builder = Statements.clazz("EntityModificationEventJS")
                .superClass(Types.type(EntityModificationEventJS.class));
*//*

        ClassDecl.Builder builder = Statements.clazz("EntityModificationEventJS").superClass(Types.parameterized(Types.type(EntityModificationEventJS.class), new BaseType[]{Types.type(EntityModificationEventJS.class)}));
        for (Map.Entry<String, Class<?>> entry : ENTITY_TYPE_MAPPING.entrySet()) {
            String entityType = entry.getKey();
            Class<?> builderClass = entry.getValue();

            builder.method("modify", method -> {
                method.returnType(Types.typeMaybeGeneric(builderClass))
                        .param("entityType", Types.literal(entityType))
                        .param("consumer", Types.lambda().param("builder", Types.type(builderClass)).build());
            });
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ModifyEntityBuilder.class);
        classes.add(ModifyLivingEntityBuilder.class);
        classes.add(EntityModificationEventJS.class);
        return classes;
    }*/
}
