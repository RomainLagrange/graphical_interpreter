package Application.Accueil;

import Application.Interpreteur.Controller_Interpreteur;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.function.UnaryOperator;

public class Controller_Accueil {

    private Stage myStage;

    public void setStage(Stage stage) {
        myStage = stage;
    }

    @FXML
    private Spinner<Integer> spinner_min_vert;

    @FXML
    private Spinner<Integer> spinner_max_vert;

    @FXML
    private Spinner<Integer> spinner_min_orange;

    @FXML
    private Spinner<Integer> spinner_max_orange;

    @FXML
    private Spinner<Integer> spinner_min_rouge;

    @FXML
    private Spinner<Integer> spinner_max_rouge;

    @FXML
    private Button button_valider;

    @FXML
    private Button button_table;

    private File table_file;

    @FXML private void initialize() {
        this.spinner_min_vert.setEditable(true);
        this.spinner_min_orange.setEditable(true);
        this.spinner_min_rouge.setEditable(true);
        this.spinner_max_vert.setEditable(true);
        this.spinner_max_orange.setEditable(true);
        this.spinner_max_rouge.setEditable(true);
    }

    @FXML
    private void NextWindow() {
        button_valider.setOnMouseClicked((event) -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Interpreteur/Interpreteur.fxml"));
            loader.setControllerFactory((Class<?> controllerType) -> {
                if (controllerType == Controller_Interpreteur.class) {
                    Controller_Interpreteur controller = new Controller_Interpreteur();
                    controller.setMin_vert(spinner_min_vert.getValue());
                    controller.setMax_vert(spinner_max_vert.getValue());
                    controller.setMin_orange(spinner_min_orange.getValue());
                    controller.setMax_orange(spinner_max_orange.getValue());
                    controller.setMin_rouge(spinner_min_rouge.getValue());
                    controller.setMax_rouge(spinner_max_rouge.getValue());
                    controller.setTable_file(table_file);
                    return controller;
                } else {
                    try {
                        return controllerType.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            Scene scene = null;
            try {
                scene = new Scene(loader.load(), 600, 400);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = new Stage();
            stage.setTitle("New Window");
            stage.setScene(scene);
            stage.show();
        });
    }

    private void loadTSV() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.myStage);
        System.out.println(file);
        table_file=file;
    }

    @FXML
    private void choisirFichier() {
        button_table.setOnMouseClicked((event) -> {
            loadTSV();
        });
    }
}