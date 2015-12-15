/*
 * Nocturne
 * Copyright (c) 2015, Lapis <https://github.com/LapisBlue>
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

import blue.lapis.nocturne.mapping.MappingContext;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

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
     *     {@link MethodDescriptor}
     * @param paramTypes The parameter {@link Type}s of the new
     *     {@link MethodDescriptor}
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
     * @param descriptor The {@link CharSequence} representing the signature
     * @throws IllegalArgumentException If the provided {@link CharSequence} is
     *     not a valid method signature
     */
    public MethodDescriptor(String descriptor) throws IllegalArgumentException {
        final String errMsg = "Not a valid method descriptor: " + descriptor;
        Preconditions.checkArgument(descriptor.charAt(0) == '(', errMsg);

        List<Type> paramTypeList = new ArrayList<>();

        char[] chars = descriptor.toCharArray();
        int arrayDims = 0;
        outer:
        for (int i = 1; i < descriptor.length(); i++) {
            switch (chars[i]) {
                case ')': {
                    break outer;
                }
                case '[': {
                    arrayDims++;
                    break;
                }
                case 'L': {
                    i++;
                    StringBuilder sb = new StringBuilder();
                    try {
                        while (chars[i] != ';') {
                            sb.append(chars[i]);
                            i++;
                        }
                    } catch (IndexOutOfBoundsException ex) {
                        throw new IllegalArgumentException(errMsg);
                    }
                    paramTypeList.add(new Type(sb.toString(), arrayDims));
                    arrayDims = 0;
                    break;
                }
                case 'V': {
                    throw new IllegalArgumentException("VOID cannot be used as a method parameter type");
                }
                default: {
                    try {
                        paramTypeList.add(new Type(Primitive.getFromKey(chars[i]), arrayDims));
                        arrayDims = 0;
                    } catch (IllegalArgumentException ex) {
                        throw new IllegalArgumentException(errMsg, ex);
                    }
                }
            }
        }
        this.paramTypes = new Type[paramTypeList.size()];
        paramTypeList.toArray(this.paramTypes);

        String returnTypeStr = descriptor.substring(descriptor.indexOf(')') + 1);

        arrayDims = 0;
        if (returnTypeStr.startsWith("[")) {
            for (char c : returnTypeStr.toCharArray()) {
                if (c == '[') {
                    arrayDims++;
                }
            }
            returnTypeStr = returnTypeStr.substring(arrayDims);
        }

        if (returnTypeStr.length() == 1) {
            this.returnType = new Type(Primitive.getFromKey(returnTypeStr.charAt(0)), arrayDims);
        } else if (returnTypeStr.startsWith("L") && returnTypeStr.endsWith(";")) {
            this.returnType = new Type(returnTypeStr.substring(1, returnTypeStr.length() - 1), arrayDims);
        } else {
            throw new IllegalArgumentException(errMsg);
        }
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
     *     deobfuscation mappings
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

}
