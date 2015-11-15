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
package blue.lapis.nocturne;

import blue.lapis.nocturne.gui.io.mappings.MappingsSaveDialogHelper;
import blue.lapis.nocturne.mapping.MappingContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ResourceBundle;

public class Main extends Application {

    private static Main instance;

    public static String locale = "en_US";
    public static ResourceBundle resourceBundle = ResourceBundle.getBundle("lang." + locale);

    public static Stage mainStage;

    public static MappingContext mappings = new MappingContext();
    public static Path currentMappingsPath;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;
        mainStage = primaryStage;

        loadView("en_US");

        Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
            throwable.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(Main.resourceBundle.getString("exception.title"));
            alert.setHeaderText(Main.resourceBundle.getString("exception.header"));

            Text description = new Text();
            description.setText(
                    Main.resourceBundle.getString("exception.dialog1") + "\n"
                    + Main.resourceBundle.getString("exception.dialog2") + "\n\n"
                    + Main.resourceBundle.getString("exception.dialog3")
            );
            description.setLayoutX(20);
            description.setLayoutY(25);

            TextArea exceptionText = new TextArea();
            StringWriter exceptionWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(exceptionWriter));
            exceptionText.setText(exceptionWriter.toString());
            exceptionText.setLayoutX(20);
            exceptionText.setLayoutY(85);

            Pane contentPane = new Pane(description, exceptionText);
            alert.getDialogPane().setContent(contentPane);

            alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CLOSE);

            alert.showAndWait();

            if (alert.getResult() == ButtonType.CLOSE) {
                System.exit(0);
            }
        });
    }

    public static Main getInstance() {
        return instance;
    }

    public void loadView(String lang) throws IOException {
        locale = lang;
        resourceBundle = ResourceBundle.getBundle("lang." + lang);
        FXMLLoader loader = new FXMLLoader(ClassLoader.getSystemResource("main.fxml"));
        loader.setResources(resourceBundle);
        Parent root = loader.load();

        Scene scene = new Scene(root);
        mainStage.setTitle("Nocturne");
        mainStage.setScene(scene);
        mainStage.setOnCloseRequest(event -> {
            try {
                if (MappingsSaveDialogHelper.doDirtyConfirmation()) {
                    event.consume();
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        mainStage.show();
    }

}
