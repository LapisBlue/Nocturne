package blue.lapis.shroud.mapping.io;

import blue.lapis.shroud.mapping.MappingSet;
import blue.lapis.shroud.mapping.model.ClassMapping;
import blue.lapis.shroud.mapping.model.FieldMapping;
import blue.lapis.shroud.mapping.model.MethodMapping;

import java.io.PrintWriter;

/**
 * The mappings writer, for the SRG format.
 *
 * @author Jamie Mansfield
 */
public class SrgWriter {

    public static void write(PrintWriter out, MappingSet mappings) {
        mappings.getMappings().values().forEach(m -> writeClass(out, m));
    }

    private static void writeClass(PrintWriter out, ClassMapping classMapping) {
        out.format("CL: %s %s\n",
                classMapping.getObfuscatedName(), classMapping.getDeobfuscatedName());
        // TODO: handle inner classes

        classMapping.getFieldMappings().values().forEach(m -> writeField(out, m));
        classMapping.getMethodMappings().values().forEach(m -> writeMethod(out, m));
        classMapping.getInnerClassMappings().values().forEach(m -> writeClass(out, m));
    }

    private static void writeField(PrintWriter out, FieldMapping fieldMapping) {
        out.format("FD: %s/%s %s/%s\n",
                fieldMapping.getParent().getObfuscatedName(), fieldMapping.getObfuscatedName(),
                fieldMapping.getParent().getDeobfuscatedName(), fieldMapping.getDeobfuscatedName());
    }

    private static void writeMethod(PrintWriter out, MethodMapping mapping) {
        out.format("MD: %s/%s %s %s/%s %s\n",
                mapping.getParent().getObfuscatedName(), mapping.getObfuscatedName(), mapping.getSignature(),
                mapping.getParent().getDeobfuscatedName(), mapping.getDeobfuscatedName(), mapping.getDeobfuscatedSignature());
    }
}
