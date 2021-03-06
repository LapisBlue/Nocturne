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

package blue.lapis.nocturne.decompile;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.util.helper.collections.MapBuilder;

import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

import java.util.Map;
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

    private static final Map<Severity, Level> LEVEL_MAP = new MapBuilder<Severity, Level>()
            .put(Severity.TRACE, Level.FINE)
            .put(Severity.INFO, Level.INFO)
            .put(Severity.WARN, Level.WARNING)
            .put(Severity.ERROR, Level.SEVERE)
            .buildUnmodifiable();

    @Override
    public void writeMessage(String message, Severity severity) {
        Main.getFernFlowerLogger().log(LEVEL_MAP.get(severity), message);
    }

    @Override
    public void writeMessage(String message, Throwable throwable) {
        Main.getFernFlowerLogger().log(Level.SEVERE, message, throwable);
    }

}
