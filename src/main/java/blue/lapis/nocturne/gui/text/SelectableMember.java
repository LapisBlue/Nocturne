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
package blue.lapis.nocturne.gui.text;

import blue.lapis.nocturne.Main;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Represents a selectable member in code.
 */
public class SelectableMember extends Text {

    private final StringProperty typeProperty = new SimpleStringProperty(this, "type");
    private final StringProperty nameProperty = new SimpleStringProperty(this, "name");
    private final StringProperty parentClassProperty = new SimpleStringProperty(this, "parentClass");

    public SelectableMember(String type, String name) {
        super(name);
        this.typeProperty.set(type);
        this.nameProperty.set(name);

        this.setFill(Color.web("orange"));

        ContextMenu contextMenu = new ContextMenu();

        MenuItem renameItem = new MenuItem(Main.getResourceBundle().getString("member.contextmenu.rename"));
        renameItem.setOnAction(event -> {
            TextInputDialog textInputDialog = new TextInputDialog(this.getText());
            textInputDialog.setHeaderText(Main.getResourceBundle().getString("member.contextmenu.rename"));
            textInputDialog.showAndWait();
            if (textInputDialog.getResult() != null && !textInputDialog.getResult().equals("")) {
                this.setText(textInputDialog.getResult());
            }
        });

        contextMenu.getItems().add(renameItem);

        this.setOnContextMenuRequested(event ->
                contextMenu.show(SelectableMember.this, event.getScreenX(), event.getScreenY()));
    }

    public StringProperty getTypeProperty() {
        return typeProperty;
    }

    public StringProperty getNameProperty() {
        return nameProperty;
    }

    public StringProperty getParentClassProperty() {
        return parentClassProperty;
    }

    public String getType() {
        return getTypeProperty().get();
    }

    public String getName() {
        return getNameProperty().get();
    }

    public String getParentClass() {
        return getParentClassProperty().get();
    }

    public void updateText() {
        String newText = getText();
        //TODO
        setText(newText);
    }

}
