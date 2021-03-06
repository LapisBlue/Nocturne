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

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_PATTERN;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_CHAR;
import static blue.lapis.nocturne.util.Constants.INNER_CLASS_SEPARATOR_PATTERN;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_PREFIX;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_REGEX;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_SUFFIX;
import static blue.lapis.nocturne.util.Constants.Processing.DELIMITER;
import static blue.lapis.nocturne.util.Constants.Processing.MEMBER_PREFIX;
import static blue.lapis.nocturne.util.Constants.Processing.MEMBER_REGEX;
import static blue.lapis.nocturne.util.Constants.Processing.MEMBER_SUFFIX;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.util.MemberType;

import org.cadixdev.bombe.type.ArrayType;
import org.cadixdev.bombe.type.FieldType;
import org.cadixdev.bombe.type.MethodDescriptor;
import org.cadixdev.bombe.type.ObjectType;
import org.cadixdev.bombe.type.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class for certain string manipulator functions.
 */
public final class StringHelper {

    private static final Pattern LOOKS_DEOBFUSCATED_REGEX = Pattern.compile("(?:.{4,})|(?:[A-Z][a-z]{2})");

    // class format is ^NOCTURNE+name^
    // member format is %NOCTURNE+TYPE-name-descriptor%
    public static String getProcessedName(String qualName, String descriptor, MemberType memberType) {
        if (memberType == MemberType.CLASS) {
            return CLASS_PREFIX + qualName + CLASS_SUFFIX;
        }
        return MEMBER_PREFIX + memberType.name() + DELIMITER + qualName + DELIMITER + descriptor + MEMBER_SUFFIX;
    }

    public static String getProcessedDescriptor(MemberType memberType, String desc) {
        switch (memberType) {
            case FIELD: {
                if (desc.startsWith("L") && desc.endsWith(";")) {
                    String typeClass = desc.substring(1, desc.length() - 1);
                    if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                        return "L" + getProcessedName(typeClass, null, MemberType.CLASS) + ";";
                    }
                }
                break;
            }
            case METHOD: {
                if (!desc.contains(MEMBER_PREFIX)) { // if this condition is true then it's already been processed
                    MethodDescriptor md = MethodDescriptor.of(desc);
                    List<FieldType> newParams = new ArrayList<>();
                    for (FieldType param : md.getParamTypes()) {
                        if (param instanceof ArrayType && ((ArrayType) param).getComponent() instanceof ObjectType) {
                            final ArrayType arr = (ArrayType) param;
                            final ObjectType obj = (ObjectType) arr.getComponent();

                            if (Main.getLoadedJar().getClass(obj.getClassName()).isPresent()) {
                                final ObjectType newObj = new ObjectType(
                                        getProcessedName(obj.getClassName(), null, MemberType.CLASS)
                                );
                                newParams.add(new ArrayType(arr.getDimCount(), newObj));
                            } else {
                                newParams.add(param);
                            }
                        } else if (param instanceof ObjectType) {
                            final ObjectType obj = (ObjectType) param;
                            if (Main.getLoadedJar().getClass(obj.getClassName()).isPresent()) {
                                newParams.add(new ObjectType(
                                        getProcessedName(obj.getClassName(), null, MemberType.CLASS)
                                ));
                            } else {
                                newParams.add(param);
                            }
                        } else {
                            newParams.add(param);
                        }
                    }

                    Type returnType = md.getReturnType();
                    if (returnType instanceof ArrayType
                            && ((ArrayType) returnType).getComponent() instanceof ObjectType) {
                        final ArrayType arr = (ArrayType) returnType;
                        final ObjectType obj = (ObjectType) arr.getComponent();

                        if (Main.getLoadedJar().getClass(obj.getClassName()).isPresent()) {
                            final ObjectType newObj = new ObjectType(
                                    getProcessedName(obj.getClassName(), null, MemberType.CLASS)
                            );
                            returnType = new ArrayType(arr.getDimCount(), newObj);
                        }
                    } else if (returnType instanceof ObjectType) {
                        final ObjectType obj = (ObjectType) returnType;
                        final String typeClass = obj.getClassName();
                        if (Main.getLoadedJar().getClass(typeClass).isPresent()) {
                            returnType = new ObjectType(getProcessedName(obj.getClassName(), null, MemberType.CLASS));
                        }
                    }

                    final MethodDescriptor newMd = new MethodDescriptor(newParams, returnType);
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

    public static String getUnprocessedName(String processed) {
        boolean clazz = processed.startsWith(CLASS_PREFIX);
        Matcher matcher = (clazz ? CLASS_REGEX : MEMBER_REGEX).matcher(processed);
        if (!matcher.find()) {
            throw new IllegalArgumentException("String " + processed + " is not a processed member name");
        }
        return matcher.group(clazz ? 1 : 2);
    }

    public static String resolvePackageName(String qualifiedClassName) {
        return qualifiedClassName.indexOf(CLASS_PATH_SEPARATOR_CHAR) != -1
                ? qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf(CLASS_PATH_SEPARATOR_CHAR))
                : "";
    }

    public static String unqualify(String qualified) {
        String unqual = qualified;
        if (unqual.contains(CLASS_PATH_SEPARATOR_CHAR + "")) {
            String[] arr = CLASS_PATH_SEPARATOR_PATTERN.split(unqual);
            unqual = arr[arr.length - 1];
        }
        if (unqual.contains(INNER_CLASS_SEPARATOR_CHAR + "")) {
            String[] arr = INNER_CLASS_SEPARATOR_PATTERN.split(unqual);
            unqual = arr[arr.length - 1];
        }
        return unqual;
    }

    public static boolean isJavaClassIdentifier(String str) {
        str = CLASS_PATH_SEPARATOR_PATTERN.matcher(str).replaceAll("");
        str = INNER_CLASS_SEPARATOR_PATTERN.matcher(str).replaceAll("");
        return isJavaIdentifier(str);
    }

    public static boolean isJavaIdentifier(String str) {
        if (str.length() == 0 || !Character.isJavaIdentifierStart(str.charAt(0))) {
            return false;
        }

        for (int i = 1; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!Character.isAlphabetic(c) && !Character.isDigit(c) && !Character.isJavaIdentifierPart(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean looksDeobfuscated(String id) {
        return LOOKS_DEOBFUSCATED_REGEX.matcher(id).find();
    }

}
