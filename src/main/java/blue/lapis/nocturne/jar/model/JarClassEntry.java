/*
 * Nocturne
 * Copyright (c) 2015-2019, Lapis <https://github.com/LapisBlue>
 *
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package blue.lapis.nocturne.jar.model;

import static blue.lapis.nocturne.processor.index.model.IndexedClass.INDEXED_CLASSES;
import static blue.lapis.nocturne.util.Constants.FF_OPTIONS;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static com.google.common.base.Preconditions.checkArgument;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.decompile.NoopResultSaver;
import blue.lapis.nocturne.decompile.SimpleBytecodeProvider;
import blue.lapis.nocturne.decompile.SimpleFernflowerLogger;
import blue.lapis.nocturne.mapping.model.TopLevelClassMapping;
import blue.lapis.nocturne.processor.index.ClassIndexer;
import blue.lapis.nocturne.processor.transform.ClassTransformer;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.StringHelper;

import com.google.common.base.MoreObjects;
import javafx.scene.control.Dialog;
import org.cadixdev.bombe.type.signature.FieldSignature;
import org.cadixdev.bombe.type.signature.MethodSignature;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents an class entry within a JAR file.
 */
public class JarClassEntry {

    private static Dialog<Boolean> decompileDialog;

    private final String name;
    private byte[] content;
    private boolean deobfuscated;

    private final Map<String, String> classNames = new HashMap<>();
    private final Map<FieldSignature, FieldSignature> fields = new HashMap<>();
    private final Map<MethodSignature, MethodSignature> methods = new HashMap<>();

    static {
        if (!Main.getInstance().testingEnv) {
            decompileDialog = new Dialog<>();
            decompileDialog.setTitle(Main.getResourceBundle().getString("dialog.decompile.title"));
            decompileDialog.setHeaderText(null);
            decompileDialog.setContentText(Main.getResourceBundle().getString("dialog.decompile.content"));
            decompileDialog.setResult(false);
        } else {
            decompileDialog = null;
        }
    }

    /**
     * Constructs a new {@link JarClassEntry} with the given name and byte
     * content.
     *
     * @param name    The name of the {@link JarClassEntry}.
     * @param content A byte array representing the raw content of the class
     */
    public JarClassEntry(String name, byte[] content) {
        this.name = name;
        this.content = new byte[content.length];
        System.arraycopy(content, 0, this.content, 0, content.length);
    }

    public void index() {
        INDEXED_CLASSES.put(getName(), new ClassIndexer(this).index());
    }

    public void process() {
        try {
            content = new ClassTransformer(getName(), getContent()).process();
        } catch (IOException ex) {
            Main.getLogger().severe("Failed to process class " + getName());
            ex.printStackTrace();
        }
    }

    /**
     * Returns the name of this {@link JarClassEntry}.
     *
     * @return The name of this {@link JarClassEntry}
     */
    public String getName() {
        return name;
    }

    public String getDeobfuscatedName() {
        checkArgument(isDeobfuscated(), "Cannot get deobfuscated name from non-deobfuscated class entry");
        TopLevelClassMapping mapping = Main.getMappingContext().getMappings().get(name);
        if (mapping != null) {
            return mapping.getDeobfuscatedName();
        } else {
            return getName();
        }
    }

    /**
     * Returns the raw byte content of this {@link JarClassEntry}.
     *
     * @return The raw byte content of this {@link JarClassEntry}.
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Returns whether this {@link JarClassEntry} is marked as deobfuscated.
     *
     * @return Whether this {@link JarClassEntry} is marked as deobfuscated
     */
    public boolean isDeobfuscated() {
        return deobfuscated;
    }

    /**
     * Sets whether this {@link JarClassEntry} is marked as deobfuscated.
     *
     * @param deobfuscated Whether this {@link JarClassEntry} is marked as
     *                     deobfuscated
     */
    public void setDeobfuscated(boolean deobfuscated) {
        this.deobfuscated = deobfuscated;
    }

    public String decompile() {
        showDecompileDialog();
        Fernflower ff = new Fernflower(
                SimpleBytecodeProvider.getInstance(),
                NoopResultSaver.getInstance(),
                FF_OPTIONS,
                SimpleFernflowerLogger.getInstance()
        );
        try {
            LazyLoader ll = new LazyLoader(SimpleBytecodeProvider.getInstance());
            String procName = StringHelper.getProcessedName(getName(), null, MemberType.CLASS);
            ll.addClassLink(procName, new LazyLoader.Link(LazyLoader.Link.CLASS, null, procName));
            StructClass sc = new StructClass(
                    SimpleBytecodeProvider.getInstance().getBytecode(null, procName),
                    true,
                    ll
            );
            ff.getStructContext().getClasses().put(procName, sc);

            // provide inner classes
            for (JarClassEntry jce : Main.getLoadedJar().getClasses().stream()
                    .filter(entry -> entry.getName().startsWith(getName() + INNER_CLASS_SEPARATOR_CHAR))
                    .collect(Collectors.toList())) {
                String innerProcName = StringHelper.getProcessedName(jce.getName(), null, MemberType.CLASS);
                ll.addClassLink(innerProcName, new LazyLoader.Link(LazyLoader.Link.CLASS, null, innerProcName));
                StructClass innerSc = new StructClass(
                        SimpleBytecodeProvider.getInstance().getBytecode(null, innerProcName),
                        true,
                        ll
                );
                ff.getStructContext().getClasses().put(innerProcName, innerSc);
            }

            ff.decompileContext();
            return ff.getClassContent(sc);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            closeDecompileDialog();
            ff.clearContext();
        }
    }

    public Map<String, String> getCurrentInnerClassNames() {
        return classNames;
    }

    public Map<FieldSignature, FieldSignature> getCurrentFields() {
        return fields;
    }

    public Map<MethodSignature, MethodSignature> getCurrentMethods() {
        return methods;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof JarClassEntry)) {
            return false;
        }
        final JarClassEntry that = (JarClassEntry) obj;
        return Objects.equals(this.name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.name, this.deobfuscated);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", this.name)
                .add("deobfuscated", this.deobfuscated)
                .toString();
    }

    private static void showDecompileDialog() {
        decompileDialog.show();
    }

    private static void closeDecompileDialog() {
        decompileDialog.close();
    }

}
