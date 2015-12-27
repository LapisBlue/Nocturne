package blue.lapis.nocturne.decompile;

import static com.google.common.base.Preconditions.*;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.JarClassEntry;

import com.google.common.base.Preconditions;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.IOException;
import java.util.Optional;

/**
 * Implements {@link IBytecodeProvider}.
 */
public class SimpleBytecodeProvider implements IBytecodeProvider {

    private static SimpleBytecodeProvider INSTANCE;

    public static SimpleBytecodeProvider getInstance() {
        return INSTANCE != null ? INSTANCE : new SimpleBytecodeProvider();
    }

    private SimpleBytecodeProvider() {
        INSTANCE = this;
    }

    @Override
    public byte[] getBytecode(String externalPath, String internalPath) throws IOException {
        checkState(Main.getLoadedJar() != null, "JAR is not loaded");
        Optional<JarClassEntry> entry = Main.getLoadedJar().getClass(internalPath);
        checkArgument(entry.isPresent(), "Class not found");
        return entry.get().getContent();
    }

}
