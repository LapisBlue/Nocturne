/*
 * Nocturne
 * Copyright (c) 2015-2018, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.processor.index.model.IndexedClass.INDEXED_CLASSES;
import static blue.lapis.nocturne.util.helper.StringHelper.resolvePackageName;
import static com.google.common.base.Preconditions.checkState;

import blue.lapis.nocturne.processor.index.model.IndexedClass;
import blue.lapis.nocturne.processor.index.model.IndexedMethod;
import org.cadixdev.bombe.type.signature.MethodSignature;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Static utility class for hierarchy-related functionality.
 */
public final class HierarchyHelper {

    public static Set<String> getClassesInHierarchy(String className, MethodSignature sig) {
        checkState(INDEXED_CLASSES.containsKey(className), "Class \"" + className + "\" is not indexed");
        IndexedClass clazz = INDEXED_CLASSES.get(className);

        return clazz.getHierarchy().stream().filter(c -> c.getMethods().containsKey(sig)).map(IndexedClass::getName)
                .collect(Collectors.toSet());
    }

    public static boolean isVisible(String class1, String class2, IndexedMethod.Visibility vis) {
        switch (vis) {
            case PUBLIC:
            case PROTECTED:
                return true;
            case PACKAGE:
                return resolvePackageName(class1).equals(resolvePackageName(class2));
            case PRIVATE:
            default:
                return false;
        }
    }

}
