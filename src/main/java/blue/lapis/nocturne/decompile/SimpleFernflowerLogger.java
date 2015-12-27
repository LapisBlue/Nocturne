package blue.lapis.nocturne.decompile;

import blue.lapis.nocturne.Main;

import com.google.common.collect.ImmutableBiMap;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.util.logging.Level;

/**
 * Implements {@link IFernflowerLogger}.
 */
public class SimpleFernflowerLogger extends IFernflowerLogger {

    private static SimpleFernflowerLogger INSTANCE;

    public static SimpleFernflowerLogger getInstance() {
        return INSTANCE != null ? INSTANCE : new SimpleFernflowerLogger();
    }

    private SimpleFernflowerLogger() {
        INSTANCE = this;
    }

    private static final ImmutableBiMap<Severity, Level> LEVEL_MAP = ImmutableBiMap.<Severity, Level>builder()
            .put(Severity.TRACE, Level.FINE)
            .put(Severity.INFO, Level.INFO)
            .put(Severity.WARN, Level.WARNING)
            .put(Severity.ERROR, Level.SEVERE)
            .build();

    @Override
    public void writeMessage(String message, Severity severity) {
        Main.getLogger().log(LEVEL_MAP.get(severity), message);
    }

    @Override
    public void writeMessage(String message, Throwable throwable) {
        Main.getLogger().log(Level.SEVERE, message, throwable);
    }

}
