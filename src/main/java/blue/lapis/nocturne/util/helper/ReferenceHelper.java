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

package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.util.helper.Preconditions.checkArgument;

import blue.lapis.nocturne.util.tuple.Pair;

import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.reference.ClassReference;
import org.cadixdev.bombe.type.reference.InnerClassReference;
import org.cadixdev.bombe.type.reference.MemberReference;
import org.cadixdev.bombe.type.reference.MethodParameterReference;
import org.cadixdev.bombe.type.reference.QualifiedReference;
import org.cadixdev.bombe.type.reference.TopLevelClassReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.annotation.Nullable;

public class ReferenceHelper {

    public static Pair<TopLevelClassReference, List<InnerClassReference>> explodeScopedClasses(ClassReference ref) {
        // top-level classes are really simple
        if (ref.getType() == QualifiedReference.Type.TOP_LEVEL_CLASS) {
            assert ref instanceof TopLevelClassReference;
            return Pair.of((TopLevelClassReference) ref, Collections.emptyList());
        }

        // otherwise, we employ a simple algorithm to traverse the inner class hierarchy
        assert ref instanceof InnerClassReference;

        // first we ascend up the hierarchy until we reach the top,
        // adding the inner classes to a stack in reverse order (lowest scope to highest)
        Stack<InnerClassReference> innerStack = new Stack<>();
        ClassReference parent = ref;
        while (parent instanceof InnerClassReference) {
            innerStack.push((InnerClassReference) parent);
            parent = ((InnerClassReference) parent).getParentClass();
        }
        assert parent instanceof TopLevelClassReference;

        // finally, pop the stack into a list so that the list is in descending hierarchical order
        List<InnerClassReference> innerRefs = new ArrayList<>();
        while (!innerStack.empty()) {
            innerRefs.add(innerStack.pop());
        }

        return Pair.of((TopLevelClassReference) parent, Collections.unmodifiableList(innerRefs));
    }

    public static ClassReference createClassReference(String className) {
        String[] split = className.split("\\$");

        ClassReference curRef = new TopLevelClassReference(split[0]);

        for (int i = 1; i < split.length; i++) {
            curRef = curRef.getInnerClass(split[i]);
        }

        return curRef;
    }

    public static ClassReference createClassReference(ObjectType classType) {
        return createClassReference(classType.getClassName());
    }

    public static String getDisplayName(QualifiedReference reference, @Nullable String paramName) {
        String res = getName(reference, paramName);
        if (reference.getType() == QualifiedReference.Type.TOP_LEVEL_CLASS) {
            res = StringHelper.unqualify(res);
        }
        return res;
    }

    public static String getDisplayName(QualifiedReference reference) {
        return getDisplayName(reference, null);
    }

    public static String getName(QualifiedReference reference, @Nullable String paramName) {
        switch (reference.getType()) {
            case TOP_LEVEL_CLASS:
                return ((ClassReference) reference).getClassType().getClassName();
            case INNER_CLASS:
                return StringHelper.unqualify(((ClassReference) reference).getClassType().getClassName());
            case FIELD:
            case METHOD:
                return ((MemberReference<?>) reference).getSignature().getName();
            case METHOD_PARAMETER:
                checkArgument(paramName != null, "Cannot infer name from method parameter reference");
                return paramName;
            default:
                throw new AssertionError("Unhandled case " + reference.getType());
        }
    }

    public static String getName(QualifiedReference reference) {
        return getDisplayName(reference, null);
    }

    public static TopLevelClassReference getRootClass(QualifiedReference ref) {
        switch (ref.getType()) {
            case TOP_LEVEL_CLASS:
                return (TopLevelClassReference) ref;
            case INNER_CLASS: {
                ClassReference parent = (InnerClassReference) ref;
                while (parent instanceof InnerClassReference) {
                    parent = ((InnerClassReference) parent).getParentClass();
                }
                return (TopLevelClassReference) parent;
            }
            case FIELD:
            case METHOD:
                return getRootClass(((MemberReference<?>) ref).getOwningClass());
            case METHOD_PARAMETER:
                return getRootClass(((MethodParameterReference) ref).getParentMethod());
            default:
                throw new AssertionError("Unhandled case " + ref.getType().name());
        }
    }

}
