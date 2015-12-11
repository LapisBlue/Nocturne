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
package blue.lapis.nocturne.gui.control;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.text.SelectableMember;
import blue.lapis.nocturne.util.MemberType;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.IOException;

/**
 * The code-tab JavaFX component.
 */
public class CodeTab extends Tab {

    public Label memberIdentifierLabel;
    public Label memberInfoLabel;
    public Label memberIdentifier;
    public Label memberInfo;
    public TextFlow code;

    public CodeTab() {
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fx/CodeTab.fxml"));
        loader.setResources(Main.getResourceBundle());

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setCode("public static void main(String[] args) {\n    System.out.println(\"Hello World\");\n}\n");
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
        this.code.getChildren().add(new Text(code));
        this.code.getChildren().add(new SelectableMember(MemberType.CLASS, "test"));
    }

    public enum SelectableMemberType {
        FIELD("codetab.identifier.field", "codetab.identifier.type"),
        METHOD("codetab.identifier.method", "codetab.identifier.signature"),
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
    }
}
