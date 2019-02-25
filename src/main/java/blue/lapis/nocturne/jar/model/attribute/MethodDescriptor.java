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

package blue.lapis.nocturne.jar.model.attribute;

import static blue.lapis.nocturne.util.Constants.TYPE_SEQUENCE_REGEX;

import blue.lapis.nocturne.mapping.MappingContext;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;

/**
 * Represents a method descriptor consisting of parameter {@link Type}s and a
 * return {@link Type}.
 */
public class MethodDescriptor {

    private final Type returnType;
    private final Type[] paramTypes;

    /**
     * Constructs a new {@link MethodDescriptor} with the given return and
     * parameter {@link Type}s.
     *
     * @param returnType The return {@link Type} of the new
     *                   {@link MethodDescriptor}
     * @param paramTypes The parameter {@link Type}s of the new
     *                   {@link MethodDescriptor}
     */
    public MethodDescriptor(Type returnType, Type... paramTypes) {
        this.returnType = returnType;
        this.paramTypes = paramTypes;
        for (Type type : paramTypes) {
            if (type.isPrimitive() && type.asPrimitive() == Primitive.VOID) {
                throw new IllegalArgumentException("VOID cannot be used as a method parameter type");
            }
        }
    }

    /**
     * Parses and constructs a new {@link MethodDescriptor} from the given
     * {@link CharSequence}.
     *
     * @param descriptor The {@link CharSequence} representing the descriptor
     * @throws IllegalArgumentException If the provided {@link CharSequence} is
     *                                  not a valid method descriptor
     */
    public static MethodDescriptor fromString(String descriptor) throws IllegalArgumentException {
        Preconditions.checkArgument(descriptor.charAt(0) == '(', "Not a valid method descriptor: " + descriptor);

        int returnTypeIndex = descriptor.indexOf(')');

        Matcher matcher = TYPE_SEQUENCE_REGEX.matcher(descriptor.substring(1, returnTypeIndex));
        List<Type> paramTypeList = new ArrayList<>();
        while (matcher.find()) {
            paramTypeList.add(Type.fromString(matcher.group()));
        }
        Type[] paramTypes = new Type[paramTypeList.size()];
        paramTypeList.toArray(paramTypes);

        String returnTypeStr = descriptor.substring(returnTypeIndex + 1);
        Type returnType = Type.fromString(returnTypeStr);

        return new MethodDescriptor(returnType, paramTypes);
    }

    /**
     * Gets the return {@link Type} of this {@link MethodDescriptor}.
     *
     * @return The return {@link Type} of this {@link MethodDescriptor}
     */
    public Type getReturnType() {
        return returnType;
    }

    /**
     * Gets the parameter {@link Type}s of this {@link MethodDescriptor}.
     *
     * @return The parameter {@link Type}s of this {@link MethodDescriptor}
     */
    public Type[] getParamTypes() {
        return paramTypes;
    }

    /**
     * Attempts to deobfuscate this {@link MethodDescriptor}.
     *
     * @param context The {@link MappingContext} to use for obtaining
     *                deobfuscation mappings
     * @return The deobfuscated {@link MethodDescriptor}
     */
    public MethodDescriptor deobfuscate(MappingContext context) {
        Type[] deobfParams = new Type[getParamTypes().length];
        for (int i = 0; i < getParamTypes().length; i++) {
            deobfParams[i] = getParamTypes()[i].deobfuscate(context);
        }

        return new MethodDescriptor(getReturnType().deobfuscate(context), deobfParams);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (Type paramType : getParamTypes()) {
            sb.append(paramType.toString());
        }
        sb.append(")");
        sb.append(getReturnType().toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (!(otherObj instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor md = (MethodDescriptor) otherObj;
        return md.getReturnType().equals(getReturnType()) && Arrays.equals(md.getParamTypes(), getParamTypes());
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnType, Arrays.hashCode(paramTypes));
    }

}
