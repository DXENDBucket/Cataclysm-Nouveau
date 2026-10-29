import com.google.gson.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

/** Read-only production-name audit. Does not load Minecraft or execute native classes. */
class ProductionMixinAudit {
    static final Map<String, ClassNode> classes = new HashMap<>();
    static final List<ZipFile> inputs = new ArrayList<>();
    static final List<String> failures = new ArrayList<>();
    static final Map<String, Integer> optionalMissCallbacks = new TreeMap<>();
    static int mixins, selectors, sites, members, optionalMisses;

    static ClassNode read(String name) throws Exception {
        if (classes.containsKey(name)) return classes.get(name);
        for (ZipFile zip : inputs) {
            var entry = zip.getEntry(name + ".class");
            if (entry == null) continue;
            ClassNode node = new ClassNode();
            try (var stream = zip.getInputStream(entry)) {
                new ClassReader(stream).accept(node, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            }
            classes.put(name, node);
            return node;
        }
        return null;
    }

    static Object value(AnnotationNode a, String key, Object fallback) {
        if (a != null && a.values != null) for (int i = 0; i < a.values.size(); i += 2)
            if (a.values.get(i).equals(key)) return a.values.get(i + 1);
        return fallback;
    }
    static List<?> list(Object value) { return value instanceof List<?> l ? l : List.of(value); }
    static List<AnnotationNode> annotations(List<AnnotationNode> a, List<AnnotationNode> b) {
        var all = new ArrayList<AnnotationNode>();
        if (a != null) all.addAll(a);
        if (b != null) all.addAll(b);
        return all;
    }
    static String remap(JsonObject refs, String reference, boolean enabled) {
        return enabled && refs != null && refs.has(reference) ? refs.get(reference).getAsString() : reference;
    }
    static String member(String reference) {
        return reference.startsWith("L") && reference.indexOf(';') < reference.indexOf('(')
                ? reference.substring(reference.indexOf(';') + 1) : reference;
    }
    static boolean selected(MethodNode method, String reference) {
        reference = member(reference);
        int desc = reference.indexOf('(');
        String name = desc < 0 ? reference : reference.substring(0, desc);
        String pattern = Arrays.stream(name.split("\\*", -1)).map(java.util.regex.Pattern::quote)
                .collect(java.util.stream.Collectors.joining(".*"));
        return method.name.matches(pattern) && (desc < 0 || method.desc.equals(reference.substring(desc)));
    }
    static boolean matches(AbstractInsnNode instruction, String reference) {
        if (instruction instanceof MethodInsnNode m)
            return reference.equals("L" + m.owner + ";" + m.name + m.desc);
        if (instruction instanceof FieldInsnNode f)
            return reference.equals("L" + f.owner + ";" + f.name + ":" + f.desc);
        return false;
    }
    static boolean hasField(ClassNode owner, String reference, String descriptor) throws Exception {
        int ownerEnd = reference.indexOf(';'), descriptorStart = reference.indexOf(':');
        if (reference.startsWith("L") && ownerEnd >= 0 && (descriptorStart < 0 || ownerEnd < descriptorStart))
            reference = reference.substring(ownerEnd + 1);
        String name = reference.contains(":") ? reference.substring(0, reference.indexOf(':')) : reference;
        while (owner != null) {
            for (FieldNode field : owner.fields) if (field.name.equals(name) && field.desc.equals(descriptor)) return true;
            owner = owner.superName == null ? null : read(owner.superName);
        }
        return false;
    }
    static void missing(String message, boolean optional) {
        if (optional) {
            optionalMisses++;
            optionalMissCallbacks.merge(message.split(" -> ")[0], 1, Integer::sum);
        }
        else failures.add(message);
    }

    static void audit(String name, JsonObject mappings, int defaultRequire) throws Exception {
        ClassNode mixin = Objects.requireNonNull(read(name), "Missing Mixin class " + name);
        AnnotationNode declaration = annotations(mixin.visibleAnnotations, mixin.invisibleAnnotations).stream()
                .filter(a -> a.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")).findFirst().orElseThrow();
        boolean defaultRemap = (boolean) value(declaration, "remap", true);
        JsonObject refs = mappings.has(name) ? mappings.getAsJsonObject(name) : null;
        var targets = new ArrayList<String>();
        for (Object type : list(value(declaration, "value", List.of()))) targets.add(((Type) type).getInternalName());
        for (Object type : list(value(declaration, "targets", List.of()))) targets.add(type.toString().replace('.', '/'));
        mixins++;
        for (String targetName : targets) {
            ClassNode target = read(targetName);
            if (target == null) { failures.add(name + ": missing production target " + targetName); continue; }
            for (FieldNode field : mixin.fields) for (AnnotationNode shadow : annotations(field.visibleAnnotations, field.invisibleAnnotations)) {
                if (!shadow.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) continue;
                members++;
                String reference = remap(refs, field.name, (boolean) value(shadow, "remap", defaultRemap));
                if (!hasField(target, reference, field.desc)) failures.add(name + ": missing shadow field " + reference + " in " + targetName);
            }
            for (MethodNode callback : mixin.methods) {
                for (AnnotationNode injection : annotations(callback.visibleAnnotations, callback.invisibleAnnotations)) {
                    if (injection.desc.equals("Lorg/spongepowered/asm/mixin/gen/Invoker;")) {
                        members++;
                        String reference = value(injection, "value", "").toString();
                        if (reference.isEmpty()) throw new IllegalStateException("Unspecified invoker target: " + name + "/" + callback.name);
                        reference = remap(refs, reference, (boolean) value(injection, "remap", defaultRemap));
                        if (!reference.contains("(")) reference += callback.desc;
                        final String resolved = reference;
                        if (target.methods.stream().noneMatch(m -> selected(m, resolved)))
                            failures.add(name + ": missing invoker method " + reference + " in " + targetName);
                        continue;
                    }
                    if (injection.desc.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;")) {
                        members++;
                        String reference = value(injection, "value", "").toString();
                        if (reference.isEmpty()) throw new IllegalStateException("Unspecified accessor target: " + name + "/" + callback.name);
                        reference = remap(refs, reference, (boolean) value(injection, "remap", defaultRemap));
                        Type[] args = Type.getArgumentTypes(callback.desc);
                        String desc = (args.length == 0 ? Type.getReturnType(callback.desc) : args[0]).getDescriptor();
                        if (!hasField(target, reference, desc)) failures.add(name + ": missing accessor field " + reference + " in " + targetName);
                        continue;
                    }
                    Object rawSelectors = value(injection, "method", null);
                    if (rawSelectors == null) continue;
                    boolean enabled = (boolean) value(injection, "remap", defaultRemap);
                    boolean optional = (int) value(injection, "require", defaultRequire) == 0;
                    var resolved = new ArrayList<String>();
                    for (Object raw : list(rawSelectors)) resolved.add(remap(refs, raw.toString(), enabled));
                    var methods = target.methods.stream().filter(m -> resolved.stream().anyMatch(s -> selected(m, s))).toList();
                    selectors++;
                    String context = name + "/" + callback.name + " -> " + targetName;
                    if (methods.isEmpty()) { missing(context + ": no selected method " + resolved, optional); continue; }
                    for (Object raw : list(value(injection, "at", List.of()))) {
                        if (!(raw instanceof AnnotationNode at)) continue;
                        String kind = value(at, "value", "").toString();
                        if (!Set.of("INVOKE", "INVOKE_ASSIGN", "FIELD").contains(kind)) continue;
                        String reference = value(at, "target", "").toString();
                        reference = remap(refs, reference, (boolean) value(at, "remap", enabled));
                        boolean found = false;
                        for (MethodNode method : methods) for (AbstractInsnNode insn : method.instructions)
                            if (matches(insn, reference)) found = true;
                        sites++;
                        if (!found) missing(context + ": no " + kind + " site " + reference + " in " + resolved, optional);
                    }
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) throw new IllegalArgumentException("mod.jar report.json production-input.jar ...");
        try {
            for (int i = 2; i < args.length; i++) inputs.add(new ZipFile(args[i]));
            var addon = new ZipFile(args[0]);
            inputs.add(addon);
            JsonObject config, refmap;
            try (var reader = new java.io.InputStreamReader(addon.getInputStream(addon.getEntry("cataclysm_nouveau.mixins.json")), java.nio.charset.StandardCharsets.UTF_8)) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            }
            try (var reader = new java.io.InputStreamReader(addon.getInputStream(addon.getEntry(config.get("refmap").getAsString())), java.nio.charset.StandardCharsets.UTF_8)) {
                refmap = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonObject("mappings");
            }
            int require = config.getAsJsonObject("injectors").get("defaultRequire").getAsInt();
            for (String group : List.of("mixins", "client")) for (JsonElement name : config.getAsJsonArray(group))
                audit((config.get("package").getAsString() + "." + name.getAsString()).replace('.', '/'), refmap, require);
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("binary", args[0]); report.put("mixins", mixins); report.put("method_selectors", selectors);
            report.put("invoke_or_field_sites", sites); report.put("optional_misses", optionalMisses); report.put("failures", failures);
            report.put("accessors_and_shadow_fields", members);
            report.put("optional_miss_callbacks", optionalMissCallbacks);
            report.put("scope", "Production bytecode names and call sites; not a Mixin runtime or client visual test");
            String json = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(report);
            Files.writeString(Path.of(args[1]), json + "\n");
            System.out.println(json);
        } finally { for (ZipFile zip : inputs) zip.close(); }
        if (!failures.isEmpty()) System.exit(1);
    }
}
