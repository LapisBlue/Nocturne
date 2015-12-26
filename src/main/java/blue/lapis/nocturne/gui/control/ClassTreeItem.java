package blue.lapis.nocturne.gui.control;

import javafx.scene.control.TreeItem;

public class ClassTreeItem extends TreeItem<String> {

    private String qualName;

    public ClassTreeItem(String qualifiedName, String label) {
        this.setValue(label);
        this.qualName = qualifiedName;
    }

    public String getQualifiedName() {
        return qualName;
    }

}
