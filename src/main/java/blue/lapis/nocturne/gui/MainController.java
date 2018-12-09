/*
 * Nocturne
 * Copyright (c) 2015-2018, Lapis <https://github.com/LapisBlue>
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

package blue.lapis.nocturne.gui;

import static com.google.common.base.Preconditions.checkArgument;

import blue.lapis.nocturne.Main;
import blue.lapis.nocturne.gui.io.JarDialogHelper;
import blue.lapis.nocturne.gui.io.MappingsDialogHelper;
import blue.lapis.nocturne.gui.scene.control.CodeTab;
import blue.lapis.nocturne.gui.scene.control.IdentifiableTreeItem;
import blue.lapis.nocturne.gui.tree.ClassElement;
import blue.lapis.nocturne.gui.tree.PackageElement;
import blue.lapis.nocturne.gui.tree.TreeElement;
import blue.lapis.nocturne.jar.model.JarClassEntry;
import blue.lapis.nocturne.util.Constants;
import blue.lapis.nocturne.util.helper.PropertiesHelper;
import blue.lapis.nocturne.util.helper.SceneHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.cadixdev.lorenz.model.TopLevelClassMapping;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The main JavaFX controller.
 */
public class MainController implements Initializable {

    public static MainController INSTANCE;

    private static final Alert RESTART_ALERT = new Alert(Alert.AlertType.WARNING);

    public MenuItem openJarButton;
    public MenuItem closeJarButton;
    public MenuItem loadMappingsButton;
    public MenuItem mergeMappingsButton;
    public MenuItem saveMappingsButton;
    public MenuItem saveMappingsAsButton;
    public MenuItem closeButton;

    public MenuItem resetMappingsButton;

    public ToggleGroup languageGroup;

    public MenuItem aboutButton;

    public TabPane tabs;

    public TreeView<TreeElement> classes;
    private TreeItem<TreeElement> treeRoot;

    public MainController() {
        INSTANCE = this;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeJarButton.setDisable(Main.getLoadedJar() == null);
        loadMappingsButton.setDisable(Main.getLoadedJar() == null);
        mergeMappingsButton.setDisable(Main.getLoadedJar() == null);
        saveMappingsButton.setDisable(Main.getLoadedJar() == null);
        saveMappingsAsButton.setDisable(Main.getLoadedJar() == null);
        resetMappingsButton.setDisable(Main.getLoadedJar() == null);

        final String langRadioPrefix = "langRadio-";
        for (Toggle toggle : languageGroup.getToggles()) {
            if (((RadioMenuItem) toggle).getId().equals(langRadioPrefix + Main.getCurrentLocale())) {
                toggle.setSelected(true);
                break;
            }
        }

        setAccelerators();

        this.initTreeViews();

        RESTART_ALERT.setTitle(Main.getResourceBundle().getString("dialog.restart.title"));
        RESTART_ALERT.setHeaderText(null);
        RESTART_ALERT.setContentText(Main.getResourceBundle().getString("dialog.restart.content"));
    }

    private void initTreeViews() {
        // Event handlers
        this.classes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                final TreeItem<TreeElement> item = this.classes.getSelectionModel().getSelectedItems().get(0);
                if (item == null) return;
                item.getValue().activate();
            }
        });
        // TODO: keyboard selection

        // Root element
        this.treeRoot = new TreeItem<>(new PackageElement("root"));
        this.treeRoot.setExpanded(true);
        this.classes.setRoot(this.treeRoot);
    }

    private void setAccelerators() {
        openJarButton.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
        loadMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN));
        mergeMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN,
                KeyCombination.ALT_DOWN));
        saveMappingsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
        saveMappingsAsButton.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN,
                KeyCombination.ALT_DOWN));
        aboutButton.setAccelerator(new KeyCodeCombination(KeyCode.F1));
    }

    public void openJar(ActionEvent actionEvent) throws IOException {
        if (Main.getLoadedJar() != null && !deinitializeCurrentJar()) {
            return;
        }
        JarDialogHelper.openJar(this);
        refreshClasses();

    }

    public void closeJar(ActionEvent actionEvent) throws IOException {
        if (!deinitializeCurrentJar()) {
            return;
        }

        closeJarButton.setDisable(true);
        loadMappingsButton.setDisable(true);
        mergeMappingsButton.setDisable(true);
        saveMappingsButton.setDisable(true);
        saveMappingsAsButton.setDisable(true);
        resetMappingsButton.setDisable(true);

        Main.clearMappings();

        refreshClasses();
    }

    public void loadMappings(ActionEvent actionEvent) throws IOException {
        // TODO: save mappings if needed
        MappingsDialogHelper.loadMappings(Main.getMainStage().getOwner(), Main.getMappings());
        refreshClasses();
    }

    public void mergeMappings(ActionEvent actionEvent) throws IOException {
        MappingsDialogHelper.loadMappings(Main.getMainStage().getOwner(), Main.getMappings());
        refreshClasses();
    }

    public void resetMappings(ActionEvent actionEvent) {
        // TODO: save mappings if needed
        /*
        Main.getMappingContext().getMappings().values().forEach(cm -> {
            Main.getLoadedJar().getCurrentNames().put(cm.getObfuscatedName(), cm.getObfuscatedName());
            JarClassEntry jce = Main.getLoadedJar().getClass(cm.getObfuscatedName()).orElse(null);
            if (jce == null) {
                return;
            }
            cm.getInnerClassMappings().values()
                    .forEach(im -> jce.getCurrentInnerClassNames().put(im.getObfuscatedName(), im.getObfuscatedName()));
            cm.getFieldMappings().values()
                    .forEach(fm -> jce.getCurrentFields().put(fm.getSignature(), fm.getSignature()));
            cm.getMethodMappings().values()
                    .forEach(mm -> jce.getCurrentMethods().put(mm.getSignature(), mm.getSignature()));
        });
        Main.getMappingContext().clear();
        Main.getLoadedJar().getClasses().forEach(jce -> jce.setDeobfuscated(false));
        CodeTab.CODE_TABS.values().forEach(CodeTab::resetClassName);
        SelectableMember.MEMBERS.values()
                .forEach(list -> list.forEach(member -> {
                    member.setAndProcessText(member.getName());
                    member.setDeobfuscated(false);
                }));
        refreshClasses();
        */
    }

    public void saveMappings(ActionEvent actionEvent) throws IOException {
        // TODO: reimplement old mappings save stuff
        MappingsDialogHelper.saveMappingsAs(Main.getMainStage().getOwner(), Main.getMappings());
    }

    public void saveMappingsAs(ActionEvent actionEvent) throws IOException {
        MappingsDialogHelper.saveMappingsAs(Main.getMainStage().getOwner(), Main.getMappings());
    }

    public void onClose(ActionEvent actionEvent) {
        // TODO: save mappings if needed
        System.exit(0);
    }

    public void showAbout(ActionEvent actionEvent) throws IOException {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(Main.getResourceBundle().getString("about.title"));
        alert.setHeaderText("Nocturne v" + Constants.VERSION);

        alert.getDialogPane().getStyleClass().add("about");
        SceneHelper.addStdStylesheet(alert.getDialogPane());

        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("fxml/about.fxml"));
        loader.setResources(Main.getResourceBundle());
        Node content = loader.load();
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    public void onLanguageSelect(ActionEvent actionEvent) throws IOException {
        RadioMenuItem radioItem = (RadioMenuItem) actionEvent.getSource();
        final String langPrefix = "langRadio-";
        String langId = radioItem.getId().substring(langPrefix.length());

        if (!langId.equals(Main.getCurrentLocale())) {
            Main.getPropertiesHelper().setProperty(PropertiesHelper.Key.LOCALE, langId);

            RESTART_ALERT.showAndWait();
        }
    }

    public void refreshClasses() {
        final List<String> expanded = this.getExpandedPackages(new ArrayList<>(), this.treeRoot);
        this.treeRoot.getChildren().clear();

        final Map<String, TreeItem<TreeElement>> packageCache = new HashMap<>();
        Main.getLoadedJar().getClasses().forEach(entry -> {
            final TopLevelClassMapping klass = Main.getMappings().getOrCreateTopLevelClassMapping(entry.getName());
            this.getPackageItem(packageCache, klass.getDeobfuscatedPackage()).getChildren()
                    .add(new TreeItem<>(new ClassElement(klass)));
        });

        // sort
        packageCache.values().forEach(item -> {
            item.getChildren().setAll(item.getChildren().sorted(Comparator.comparing(TreeItem::getValue)));
        });
        this.treeRoot.getChildren().setAll(this.treeRoot.getChildren().sorted(Comparator.comparing(TreeItem::getValue)));

        // reopen packages
        expanded.forEach(pkg -> {
            final TreeItem<TreeElement> packageItem = packageCache.get(pkg);
            if (packageItem == null) return;
            packageItem.setExpanded(true);
        });
    }

    private TreeItem<TreeElement> getPackageItem(final Map<String, TreeItem<TreeElement>> cache, final String packageName) {
        if (packageName.isEmpty()) return this.treeRoot;
        return cache.computeIfAbsent(packageName, name -> {
            final TreeItem<TreeElement> parent;
            if (name.lastIndexOf('/') != -1) {
                parent = this.getPackageItem(cache, name.substring(0, name.lastIndexOf('/')));
            }
            else {
                parent = this.treeRoot;
            }
            final TreeItem<TreeElement> packageItem = new TreeItem<>(new PackageElement(name));
            parent.getChildren().add(packageItem);
            return packageItem;
        });
    }

    private List<String> getExpandedPackages(final List<String> packages, final TreeItem<TreeElement> item) {
        item.getChildren().filtered(TreeItem::isExpanded).forEach(pkg -> {
            this.getExpandedPackages(packages, pkg);
            if (pkg.getValue() instanceof PackageElement) {
                packages.add(((PackageElement) pkg.getValue()).getName());
            }
        });
        return packages;
    }

    private boolean deinitializeCurrentJar() throws IOException {
        // TODO: save mappings if needed
        Main.clearMappings();
        this.closeAllTabs();
        Main.setLoadedJar(null);
        return true;
    }

    /**
     * Closes all currently opened tabs.
     */
    public void closeAllTabs() {
        tabs.getTabs().forEach(tab -> tab.getOnClosed().handle(null));
        tabs.getTabs().clear();
        CodeTab.CODE_TABS.clear();
    }

    public void openTab(String className) {
        if (CodeTab.CODE_TABS.containsKey(className)) {
            tabs.getSelectionModel().select(CodeTab.CODE_TABS.get(className));
        } else {
            CodeTab tab = new CodeTab(tabs, Main.getMappings().getOrCreateTopLevelClassMapping(className));

            Optional<JarClassEntry> clazz = Main.getLoadedJar().getClass(className);
            checkArgument(clazz.isPresent(), "Cannot find class entry for " + className);
            tab.setCode(clazz.get().decompile());
        }
    }

    public static boolean isInitialized() {
        return INSTANCE != null;
    }

    private static Map<String, IdentifiableTreeItem> flatten(IdentifiableTreeItem tree) {
        Map<String, IdentifiableTreeItem> map = new HashMap<>();
        map.put(tree.getId(), tree);
        if (tree.getChildren().isEmpty()) {
            return map;
        }

        for (TreeItem<String> child : tree.getChildren()) {
            map.putAll(flatten((IdentifiableTreeItem) child));
        }
        return map;
    }

    private static Set<String> getExpandedIds(IdentifiableTreeItem tree) {
        if (tree == null) {
            return Collections.emptySet();
        }

        return flatten(tree).entrySet().stream().filter(e -> e.getValue().isExpanded()).map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

}
