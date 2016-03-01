/*
 * Nocturne
 * Copyright (c) 2015-2016, Lapis <https://github.com/LapisBlue>
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

import static blue.lapis.nocturne.util.Constants.Processing.CLASS_REGEX;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.JarClassEntry;

import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;

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
        Matcher matcher = CLASS_REGEX.matcher(internalPath);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Class not found");
        }
        String name = matcher.group(1);
        checkState(Main.getLoadedJar() != null, "JAR is not loaded");
        Optional<JarClassEntry> entry = Main.getLoadedJar().getClass(name);
        checkArgument(entry.isPresent(), "Class not found");
        return entry.get().getContent();
    }

}
