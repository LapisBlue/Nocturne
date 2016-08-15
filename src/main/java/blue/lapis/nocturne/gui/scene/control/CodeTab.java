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

package blue.lapis.nocturne.gui.scene.control;

import static blue.lapis.nocturne.util.Constants.CLASS_PATH_SEPARATOR_PATTERN;
import static blue.lapis.nocturne.util.Constants.Processing.CLASS_REGEX;
import static blue.lapis.nocturne.util.Constants.Processing.MEMBER_REGEX;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.scene.text.SelectableMember;
import blue.lapis.nocturne.util.JavaSyntaxHighlighter;
import blue.lapis.nocturne.util.MemberType;
import blue.lapis.nocturne.util.helper.StringHelper;

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
        this.setText(CLASS_PATH_SEPARATOR_PATTERN.matcher(displayName).replaceAll("."));

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
        this.setText(CLASS_PATH_SEPARATOR_PATTERN.matcher(className).replaceAll("."));
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

        List<Node> nodes = Lists.newArrayList(new Text(code));

        parseItems(nodes, CLASS_REGEX, 1);
        parseItems(nodes, MEMBER_REGEX, 2);

        JavaSyntaxHighlighter.highlight(nodes);

        nodes.forEach(node -> ((Text) node).setFont(Font.font("monospace", ((Text) node).getFont().getSize())));

        Node[] nodeArr = new Node[nodes.size()];
        nodes.toArray(nodeArr);
        TextFlow flow = new TextFlow(nodeArr);

        this.code.getChildren().add(flow);
    }

    public enum SelectableMemberType {
        FIELD("codetab.identifier.field", "codetab.identifier.type"),
        METHOD("codetab.identifier.method", "codetab.identifier.descriptor"),
        ARG("codetab.identifier.arg", "codetab.identifier.type"),
        CLASS("codetab.identifier.class"),
        ;

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
                case ARG:
                    return SelectableMemberType.ARG;
                default:
                    throw new AssertionError();
            }
        }
    }

    public void parseItems(List<Node> nodes, Pattern pattern, int defaultGroup) {
        List<Node> newNodes = new ArrayList<>();

        for (Node node : nodes) {
            if (node.getClass() != Text.class) {
                newNodes.add(node);
                continue;
            }

            String str = ((Text) node).getText();
            Matcher matcher = pattern.matcher(str);
            int lastIndex = 0;
            while (matcher.find()) {
                newNodes.add(new Text(str.substring(lastIndex, matcher.start())));
                SelectableMember sm = SelectableMember.fromMatcher(this, matcher);
                if (sm != null) {
                    newNodes.add(sm);
                } else {
                    newNodes.add(new Text(StringHelper.unqualify(matcher.group(defaultGroup))));
                }
                lastIndex = matcher.end();
            }
            newNodes.add(new Text(str.substring(lastIndex)));
        }

        nodes.clear();
        nodes.addAll(newNodes);
    }

}
