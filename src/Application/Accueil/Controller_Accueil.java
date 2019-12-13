package Application.Accueil;

import Application.Interpreteur.Controller_Interpreteur;
import Application.Utils.TableUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.HashMap;
import java.util.List;
import java.util.function.UnaryOperator;

public class Controller_Accueil {

    private Stage myStage;

    public void setStage(Stage stage) {
        myStage = stage;
    }

    @FXML
    private TextField min_vert;

    @FXML
    private TextField max_vert;

    @FXML
    private TextField min_orange;

    @FXML
    private TextField max_orange;

    @FXML
    private TextField min_rouge;

    @FXML
    private TextField max_rouge;

    @FXML
    private Button button_valider;

    @FXML
    private Button button_table;

    @FXML
    private Button button_file;

    @FXML
    private Label file_path;
    
    @FXML
    private GridPane gridGene;

    private File table_file;

    private HashMap<String, Integer> sizeGene;

    @FXML private void initialize() {

        setSpinnerFomat();
    }

    private void setSpinnerFomat() {
        this.min_vert.setTextFormatter(getDoubleTextFormatter(0.5));
        this.min_orange.setTextFormatter(getDoubleTextFormatter(2.0));
        this.min_rouge.setTextFormatter(getDoubleTextFormatter(20.0));
        this.max_vert.setTextFormatter(getDoubleTextFormatter(2.0));
        this.max_orange.setTextFormatter(getDoubleTextFormatter(20.0));
        this.max_rouge.setTextFormatter(getDoubleTextFormatter(100.0));
    }

    private TextFormatter<Double> getDoubleTextFormatter(Double x) {
        DecimalFormat decimalFormat = new DecimalFormat();
        String patter = decimalFormat.toPattern();
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols() ;
        symbols.setDecimalSeparator('.');

        DecimalFormat decimalFormatCustom = new DecimalFormat(patter,symbols);
        decimalFormatCustom.setMinimumFractionDigits(1);

        char decimalSep = '.' ;

        UnaryOperator<TextFormatter.Change> filter = change -> {
            for (char c : change.getText().toCharArray()) {
                if ( (! Character.isDigit(c)) && c != decimalSep) {
                    return null ;
                }
            }
            return change ;
        };

        StringConverter<Double> converter = new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return object == null ? "" : decimalFormatCustom.format(object);
            }

            @Override
            public Double fromString(String string) {
                try {
                    return string.isEmpty() ? x : decimalFormatCustom.parse(string).doubleValue();
                } catch (ParseException e) {
                    return x ;
                }
            }

        };
        return new TextFormatter<>(converter, x, filter);
    }


    @FXML
    private void AcceptTabel() {
        this.button_file.setOnMouseClicked((event -> {
            List<String> list_gene = TableUtils.getListGenes(TableUtils.getTSV(this.table_file));
            this.gridGene.setPadding(new Insets(10, 10, 10, 10));
            this.sizeGene = new HashMap<>();
            for (String gene:list_gene) {
                this.sizeGene.put(gene, 0);
            }
            createLabelTailleGene();
        }));
    }

    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void createLabelTailleGene() {
        int i = 0;
        for (String gene:this.sizeGene.keySet()) {
            Label nom = new Label(gene);
            nom.setId(gene);
            nom.setPrefWidth(150);
            TextField taille_gene = new TextField();
            taille_gene.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue,
                                    String newValue) {
                    if (!newValue.matches("\\d*")) {
                        taille_gene.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                }
            });
            taille_gene.setPrefWidth(70);
            taille_gene.setId("taille"+gene);
            this.gridGene.addRow(i,nom,taille_gene);
            this.gridGene.setVgap(5);
            i++;
        }
    }

    @FXML
    private void NextWindow() {
        button_valider.setOnMouseClicked((event) -> {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Interpreteur/Interpreteur.fxml"));
            setGeneSize();
            System.out.println(this.sizeGene);
            loader.setControllerFactory((Class<?> controllerType) -> {
                if (controllerType == Controller_Interpreteur.class) {
                    Controller_Interpreteur controller = new Controller_Interpreteur();
                    controller.setMin_vert(Double.valueOf(this.min_vert.getText()));
                    controller.setMax_vert(Double.valueOf(this.max_vert.getText()));
                    controller.setMin_orange(Double.valueOf(this.min_orange.getText()));
                    controller.setMax_orange(Double.valueOf(this.max_orange.getText()));
                    controller.setMin_rouge(Double.valueOf(this.min_rouge.getText()));
                    controller.setMax_rouge(Double.valueOf(this.max_rouge.getText()));
                    controller.setSizeGene(this.sizeGene);
                    controller.setTable_file(this.table_file);
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

    private void setGeneSize() {
        int i = 0;
        for (String gene:this.sizeGene.keySet()) {
            this.sizeGene.put(gene, Integer.valueOf(((TextField)getNodeFromGridPane(this.gridGene,1, i)).getText()));
            i++;
        }
    }

    private void loadTSV() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.myStage);
        this.table_file=file;
        this.file_path.setText(file.getName());
    }

    @FXML
    private void choisirFichier() {
        button_table.setOnMouseClicked((event) -> {
            loadTSV();
        });
    }
}