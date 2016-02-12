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
package blue.lapis.nocturne.gui.scene.control;

import static blue.lapis.nocturne.util.Constants.CHAR_LITERAL_REGEX;
import static blue.lapis.nocturne.util.Constants.KEYWORD_REGEX;
import static blue.lapis.nocturne.util.Constants.MEMBER_REGEX;
import static blue.lapis.nocturne.util.Constants.STRING_LITERAL_REGEX;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.scene.text.syntax.Keyword;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.gui.scene.text.syntax.StringLiteral;
import blue.lapis.nocturne.util.MemberType;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The code-tab JavaFX component.
 */
public class CodeTab extends Tab {

    public static final Map<String, CodeTab> CODE_TABS = Maps.newHashMap();

    private final String className;

    public Label memberIdentifierLabel;
    public Label memberInfoLabel;
    public Label memberIdentifier;
    public Label memberInfo;
    public TextFlow code;

    public CodeTab(TabPane pane, String className, String displayName) {
        this.className = className;
        this.setText(displayName);

        pane.getTabs().add(this);

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fxml/CodeTab.fxml"));
        loader.setResources(Main.getResourceBundle());
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CODE_TABS.put(className, this);
        getTabPane().getSelectionModel().select(this);

        this.setOnClosed(event -> CODE_TABS.remove(this.getClassName()));
    }

    public String getClassName() {
        return className;
    }

    public void resetClassName() {
        this.setText(className);
    }

    /**
     * Sets the member type of the selected member.
     *
     * @param type the member type.
     */
    public void setMemberType(SelectableMemberType type) {
        this.memberIdentifierLabel.setText(String.format("%s: ", type.getIdentifierLabel()));
        if (type.isInfoEnabled()) {
            this.memberInfoLabel.setText(String.format("%s: ", type.getInfoLabel()));
            this.memberInfo.setVisible(true);
            this.memberInfoLabel.setVisible(true);
        } else {
            this.memberInfo.setVisible(false);
            this.memberInfoLabel.setVisible(false);
        }
    }

    /**
     * Sets the member identifier of the selected member.
     *
     * @param identifier The member identifier.
     */
    public void setMemberIdentifier(String identifier) {
        this.memberIdentifier.setText(identifier);
    }

    /**
     * Sets the member info of the selected member.
     *
     * @param info The member info.
     */
    public void setMemberInfo(String info) {
        this.memberInfo.setText(info);
    }

    /**
     * Sets the open source file's code.
     *
     * @param code The code.
     */
    public void setCode(String code) {
        this.code.getChildren().clear();

        List<Node> nodes = Lists.newArrayList();

        Matcher matcher = MEMBER_REGEX.matcher(code);
        int lastIndex = 0;
        while (matcher.find()) {
            nodes.add(new Text(code.substring(lastIndex, matcher.start())));
            nodes.add(SelectableMember.fromMatcher(this, matcher));
            lastIndex = matcher.end();
        }
        nodes.add(new Text(code.substring(lastIndex)));

        nodes = applyAllSyntaxHighlighting(nodes);

        nodes.forEach(node -> ((Text) node).setFont(Font.font("monospace", 12)));

        Node[] nodeArr = new Node[nodes.size()];
        nodes.toArray(nodeArr);
        TextFlow flow = new TextFlow(nodeArr);

        this.code.getChildren().add(flow);
    }

    private List<Node> applyAllSyntaxHighlighting(List<Node> nodes) {
        try {
            nodes = applySyntaxHighlighting(nodes, STRING_LITERAL_REGEX,
                    StringLiteral.class.getConstructor(String.class));
            nodes = applySyntaxHighlighting(nodes, CHAR_LITERAL_REGEX,
                    StringLiteral.class.getConstructor(String.class));
            nodes = applySyntaxHighlighting(nodes, KEYWORD_REGEX, Keyword.class.getConstructor(String.class));
            return nodes;
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        }
    }

    private List<Node> applySyntaxHighlighting(List<Node> nodes, Pattern regex, Constructor<? extends Text> ctor) {
        List<Node> newNodes = new ArrayList<Node>();
        nodes.stream()
                .forEach(node -> {
                    if (node.getClass() != Text.class) {
                        newNodes.add(node);
                        return;
                    }
                    String text = ((Text) node).getText();
                    Matcher matcher = regex.matcher(text);
                    int lastIndex = 0;
                    while (matcher.find()) {
                        newNodes.add(new Text(text.substring(lastIndex, matcher.start())));
                        try {
                            newNodes.add(ctor.newInstance(matcher.group(0)));
                        } catch ( IllegalAccessException | IllegalArgumentException | InstantiationException
                                | InvocationTargetException ex) {
                            throw new RuntimeException(ex);
                        }
                        lastIndex = matcher.end();
                    }
                    newNodes.add(new Text(text.substring(lastIndex)));
                });
        return newNodes;
    }

    public enum SelectableMemberType {
        FIELD("codetab.identifier.field", "codetab.identifier.type"),
        METHOD("codetab.identifier.method", "codetab.identifier.descriptor"),
        CLASS("codetab.identifier.class");

        private final String identifierLabel;
        private final String infoLabel;
        private final boolean infoEnabled;

        SelectableMemberType(String identifierLabel, String infoLabel, boolean infoEnabled) {
            this.identifierLabel = identifierLabel;
            this.infoLabel = infoLabel;
            this.infoEnabled = infoEnabled;
        }

        SelectableMemberType(String identifierLabel) {
            this(identifierLabel, "", false);
        }

        SelectableMemberType(String identifierLabel, String infoLabel) {
            this(identifierLabel, infoLabel, true);
        }

        /**
         * Gets the localised identifier label for this member type.
         *
         * @return The identifier label.
         */
        public String getIdentifierLabel() {
            return Main.getResourceBundle().getString(this.identifierLabel);
        }

        /**
         * Gets the localised info label for this member type.
         *
         * @return The info label.
         */
        public String getInfoLabel() {
            return Main.getResourceBundle().getString(this.infoLabel);
        }

        /**
         * Gets if the info label should be displayed or not.
         *
         * @return {@code True} if the label should be displayed.
         */
        public boolean isInfoEnabled() {
            return infoEnabled;
        }

        public static SelectableMemberType fromMemberType(MemberType type) {
            switch (type) {
                case CLASS:
                    return SelectableMemberType.CLASS;
                case FIELD:
                    return SelectableMemberType.FIELD;
                case METHOD:
                    return SelectableMemberType.METHOD;
                default:
                    throw new AssertionError();
            }
        }
    }
}
