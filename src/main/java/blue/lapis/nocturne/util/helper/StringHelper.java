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
package blue.lapis.nocturne.util.helper;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.MEMBER_DELIMITER;
import static blue.lapis.nocturne.util.Constants.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.Constants.MEMBER_SUFFIX;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.jar.model.attribute.MethodDescriptor;
import blue.lapis.nocturne.jar.model.attribute.Type;
import blue.lapis.nocturne.util.MemberType;

import java.util.ArrayList;
import java.util.List;

/**
 * Static utility class for certain string manipulator functions.
 */
public final class StringHelper {

    // current format is %NOCTURNE+TYPE-name-descriptor% (descriptor is optional)
    public static String getProcessedName(String qualifiedMemberName, String descriptor, MemberType memberType) {
        return MEMBER_PREFIX + memberType.name() + MEMBER_DELIMITER + qualifiedMemberName
                + (descriptor != null ? MEMBER_DELIMITER + descriptor : "") + MEMBER_SUFFIX;
    }

    public static String getProcessedDescriptor(MemberType memberType, String desc) {
        switch (memberType) {
            case FIELD: {
                if (desc.startsWith("L") && desc.endsWith(";")) {
                    String typeClass = desc.substring(1, desc.length());
                    if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                        return "L" + getProcessedName(typeClass, null, MemberType.CLASS) + ";";
                    }
                }
                break;
            }
            case METHOD: {
                if (!desc.contains(MEMBER_PREFIX)) { // if this condition is true then it's already been processed
                    MethodDescriptor md = MethodDescriptor.fromString(desc);
                    List<Type> newParams = new ArrayList<>();
                    for (Type param : md.getParamTypes()) {
                        if (param.isPrimitive()) {
                            newParams.add(param);
                        } else {
                            String typeClass = param.getClassName();
                            if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                                newParams.add(new Type(getProcessedName(typeClass, null, MemberType.CLASS),
                                        param.getArrayDimensions()));
                            } else {
                                newParams.add(param);
                            }
                        }
                    }
                    Type returnType = md.getReturnType();
                    if (!returnType.isPrimitive()) {
                        String typeClass = returnType.getClassName();
                        if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                            returnType = new Type(getProcessedName(typeClass, null, MemberType.CLASS),
                                    returnType.getArrayDimensions());
                        }
                    }

                    Type[] newParamArr = new Type[newParams.size()];
                    newParams.toArray(newParamArr);
                    MethodDescriptor newMd = new MethodDescriptor(returnType, newParamArr);
                    return newMd.toString();
                }
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        return desc;
    }

    public static String resolvePackageName(String qualifiedClassName) {
        return qualifiedClassName.indexOf(CLASS_PATH_SEPARATOR_CHAR) != -1
                ? qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(CLASS_PATH_SEPARATOR_CHAR))
                : "";
    }

}