/*
 * Nocturne
 * Copyright (c) 2015-2017, Lapis <https://github.com/LapisBlue>
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

import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.util.jar.Manifest;

/**
 * No-op implementation of {@link IResultSaver}.
 */
public class NoopResultSaver implements IResultSaver {

    private static NoopResultSaver INSTANCE;

    public static NoopResultSaver getInstance() {
        return INSTANCE != null ? INSTANCE : new NoopResultSaver();
    }

    private NoopResultSaver() {
        INSTANCE = this;
    }

    @Override
    public void saveFolder(String s) {
    }

    @Override
    public void copyFile(String s, String s1, String s2) {
    }

    @Override
    public void saveClassFile(String s, String s1, String s2, String s3, int[] ints) {
    }

    @Override
    public void createArchive(String s, String s1, Manifest manifest) {
    }

    @Override
    public void saveDirEntry(String s, String s1, String s2) {
    }

    @Override
    public void copyEntry(String s, String s1, String s2, String s3) {
    }

    @Override
    public void saveClassEntry(String s, String s1, String s2, String s3, String s4) {
    }

    @Override
    public void closeArchive(String s, String s1) {
    }

}
