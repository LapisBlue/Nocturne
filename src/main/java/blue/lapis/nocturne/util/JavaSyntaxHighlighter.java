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

package blue.lapis.nocturne.util;

import blue.lapis.nocturne.gui.scene.text.SelectableMember;

import javafx.scene.Node;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class for handling syntax highlighting of Java code.
 */
public final class JavaSyntaxHighlighter {

    private JavaSyntaxHighlighter() {
    }

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "false", "final", "finally",
            "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long",
            "native", "new", "null", "package", "private",
            "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this",
            "throw", "throws", "transient", "true", "try",
            "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String SEMICOLON_PATTERN = ";";
    private static final String STRING_PATTERN = "(\"(?:[^\"\\\\]|\\\\.)*\"|'(?:[^'\\\\]|\\\\.)')";
    private static final String NUMBER_PATTERN = "[^\\w]((?:\\d+(?:\\.\\d+)?)+[DdFfLl]?)";

    private static final String[] PATTERN_NAMES = {"KEYWORD", "SEMICOLON", "STRING", "NUMBER"};

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
    );

    /**
     * Applies syntax highlighting to the given {@link Node} list.
     *
     * <p><em>Note: This method is atomic. As such, if an exception occurs while
     * processing the nodes, the list will remain unmodified.</em></p>
     *
     * @param nodes The {@link Node} list to apply highlighting to
     */
    public static void highlight(List<Node> nodes) {
        List<Node> newNodes = new ArrayList<>();
        nodes.stream()
                .forEach(node -> {
                    if (node.getClass() == SelectableMember.class) {
                        newNodes.add(node);
                        return;
                    }
                    String text = ((Text) node).getText();
                    Matcher matcher = PATTERN.matcher(text);
                    int lastIndex = 0;

                    while (matcher.find()) {
                        String group = null;
                        for (String pattern : PATTERN_NAMES) {
                            if (matcher.group(pattern) != null) {
                                group = pattern;
                                break;
                            }
                        }
                        assert group != null;

                        int start = matcher.start(group);
                        int end = matcher.end(group);
                        if (group.equals("NUMBER") && !Character.isDigit(matcher.group(group).charAt(0))) {
                            //TODO: I am a horrible person
                            start += 1;
                        }
                        newNodes.add(new Text(text.substring(lastIndex, start)));
                        Text syntaxItem = new Text(text.substring(start, end));
                        syntaxItem.getStyleClass().add("syntax");
                        syntaxItem.getStyleClass().add(group.toLowerCase());
                        newNodes.add(syntaxItem);
                        lastIndex = matcher.end();
                    }
                    newNodes.add(new Text(text.substring(lastIndex)));
                });
        nodes.clear();
        nodes.addAll(newNodes);
    }

}
