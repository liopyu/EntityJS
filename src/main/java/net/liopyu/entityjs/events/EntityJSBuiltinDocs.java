package net.liopyu.entityjs.events;

/*import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.transpiler.Transpiler;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;*/

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class EntityJSBuiltinDocs /*extends ProbeJSPlugin */ {
   /* public static final Supplier<ProbeJSPlugin> plugin = DynamicEntityEventsPlugin::new;
    public static final List<Supplier<ProbeJSPlugin>> BUILTIN_DOCS = new ArrayList<>(List.of(plugin));

    public EntityJSBuiltinDocs() {
    }

    private static void forEach(Consumer<ProbeJSPlugin> consumer) {
        for (Supplier<ProbeJSPlugin> builtinDoc : BUILTIN_DOCS) {
            consumer.accept(builtinDoc.get());
        }
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.addGlobals(scriptDump));
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        forEach(builtinDoc -> builtinDoc.modifyClasses(scriptDump, globalClasses));
    }

    @Override
    public void assignType(ScriptDump scriptDump) {
        forEach(builtinDoc -> builtinDoc.assignType(scriptDump));
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        forEach(builtinDoc -> builtinDoc.addPredefinedTypes(converter));
    }

    @Override
    public void denyTypes(Transpiler transpiler) {
        forEach(builtinDoc -> builtinDoc.denyTypes(transpiler));
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> allClasses = new HashSet<>();
        forEach(builtinDoc -> allClasses.addAll(builtinDoc.provideJavaClass(scriptDump)));
        return allClasses;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        forEach(builtinDoc -> builtinDoc.addVSCodeSnippets(dump));
    }
*/
}
