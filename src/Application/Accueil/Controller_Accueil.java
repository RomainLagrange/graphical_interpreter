package Application.Accueil;

import Application.Interpreteur.Controller_Interpreteur;
import Application.Utils.TableUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    private ComboBox<String> comboBox;
    private TextField textFieldCohort;
    private CheckBox checkBoxGeneFilter;
    private ComboBox<String> comboGene;
    private ComboBox<String> comboGeneAnalysis;

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

    @FXML
    private HBox hBoxMain;

    private File table_file;

    private HashMap<String, Integer> sizeGene;

    private HashMap<String, String> filtre;


    /**
     * Methode qui permet de formater les textfields de saisie des valeurs de couleurs
     * pour n'accepter que des décimaux. Génère aussi les valeurs par défaut
     */
    public void setFieldFomat() {
        this.min_vert.setTextFormatter(getDoubleTextFormatter(0.5));
        this.min_orange.setTextFormatter(getDoubleTextFormatter(2.0));
        this.min_rouge.setTextFormatter(getDoubleTextFormatter(20.0));
        this.max_vert.setTextFormatter(getDoubleTextFormatter(2.0));
        this.max_orange.setTextFormatter(getDoubleTextFormatter(20.0));
        this.max_rouge.setTextFormatter(getDoubleTextFormatter(100.0));
        this.button_valider.setStyle("-fx-border-color: black; -fx-text-fill: mediumblue");
    }

    /**
     * Fonction qui permet de générer un formater de décimaux en donnant en paramètre la valeur par défaut
     * Les décimaux sont arrondis 3 chiffres après la virgule et utilisent le '.' comme séparateur
     * @param x
     * @return
     */
    private TextFormatter<Double> getDoubleTextFormatter(Double x) {
        DecimalFormat decimalFormat = new DecimalFormat();
        String patter = decimalFormat.toPattern();
        DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
        symbols.setDecimalSeparator('.');

        DecimalFormat decimalFormatCustom = new DecimalFormat(patter, symbols);
        decimalFormatCustom.setMinimumFractionDigits(1);

        char decimalSep = '.';

        UnaryOperator<TextFormatter.Change> filter = change -> {
            for (char c : change.getText().toCharArray()) {
                if ((!Character.isDigit(c)) && c != decimalSep) {
                    return null;
                }
            }
            return change;
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
                    return x;
                }
            }

        };
        return new TextFormatter<>(converter, x, filter);
    }


    /**
     * Méthode associée au click de validation du choix du tableau utilisé pour l'analyse
     * Elle permet de générer la liste des gènes puis créer le label qui va demander de saisir la taille des gènes
     */
    @FXML
    private void acceptTable() {
        this.button_file.setOnMouseClicked((event -> {
            List<String> list_gene = TableUtils.getListGenes(TableUtils.getTSV(this.table_file));
            this.gridGene.setPadding(new Insets(10, 10, 10, 10));
            this.sizeGene = new HashMap<>();
            for (String gene : list_gene) {
                this.sizeGene.put(gene, 0);
            }
            createLabelTailleGene();
            generateFiltreBox();
        }));
    }

    /**
     * Méthode qui permet de générer la box contenant les différents types d'analyses
     * Le contenu change selon le choix de la comboBox
     */
    private void generateFiltreBox() {
        VBox vBox = new VBox(10);
        Label annonceFiltre = new Label("Type of research");
        annonceFiltre.setStyle("-fx-font-weight: bold;");
        annonceFiltre.setPadding(new Insets(10, 10, 10, 10));
        comboBox = new ComboBox();
        comboBox.getItems().setAll("Complet Analysis", "Cohort Analysis", "Gene Analysis");
        comboBox.getSelectionModel().select(0);

        vBox.setPadding(new Insets(20, 10, 10, 10));
        VBox vBoxChoix = new VBox(10);
        vBoxChoix.setPrefSize(300,200);
        vBoxChoix.setStyle("-fx-border-color: black");
        filtre = new HashMap<>();

        comboBox.valueProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.equals("Cohort Analysis")) {
                    vBoxChoix.getChildren().clear();
                    boxCohortAnalysis(vBoxChoix);
                }
                else if (newValue.equals("Complet Analysis")){
                    vBoxChoix.getChildren().clear();
                }
                else if (newValue.equals("Gene Analysis")){
                    vBoxChoix.getChildren().clear();
                    boxGeneAnalysis(vBoxChoix);
                }
            }
        });


        vBox.getChildren().addAll(annonceFiltre, comboBox,vBoxChoix);
        this.hBoxMain.getChildren().add(vBox);
    }

    /**
     * Méthode qui génère la box pour le choix d'une analyse d'un gène particulier
     * @param vBoxChoix
     */
    private void boxGeneAnalysis(VBox vBoxChoix){
        HBox ligne3 = new HBox(10);
        ligne3.setPadding(new Insets(20, 10, 10, 10));
        ligne3.setAlignment(Pos.CENTER);
        Label label3 = new Label("Gene name");
        comboGeneAnalysis = new ComboBox<>();
        for (String gene :sizeGene.keySet() ) {
            comboGeneAnalysis.getItems().add(gene);
        }
        comboGeneAnalysis.getSelectionModel().select(0);
        ligne3.getChildren().addAll(label3, comboGeneAnalysis);
        vBoxChoix.getChildren().addAll(ligne3);
    }

    /**
     * Méthode qui génère la box pour le choix d'une analyse de cohorte
     * Si on coche la checkbox, affiche le choix d'un gène spécifique pour l'analyse
     * @param vBoxChoix
     */
    private void boxCohortAnalysis(VBox vBoxChoix) {
        HBox ligne1 = new HBox(10);
        ligne1.setAlignment(Pos.CENTER);
        ligne1.setPadding(new Insets(20, 10, 10, 10));
        Label label1 = new Label("First letters");
        textFieldCohort = new TextField("");
        textFieldCohort.setPromptText("ex : DA");
        ligne1.getChildren().addAll(label1, textFieldCohort);

        HBox ligne2 = new HBox(10);
        ligne2.setPadding(new Insets(20, 10, 10, 10));
        ligne2.setAlignment(Pos.CENTER);
        checkBoxGeneFilter = new CheckBox();
        Label label2 = new Label("Add gene filter ");
        ligne2.getChildren().addAll(label2, checkBoxGeneFilter);

        vBoxChoix.getChildren().addAll(ligne1,ligne2);

        checkBoxGeneFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    HBox ligne3 = new HBox(10);
                    ligne3.setPadding(new Insets(20, 10, 10, 10));
                    ligne3.setAlignment(Pos.CENTER);
                    Label label3 = new Label("Gene name");
                    comboGene = new ComboBox<>();
                    for (String gene :sizeGene.keySet() ) {
                        comboGene.getItems().add(gene);
                    }
                    comboGene.getSelectionModel().select(0);
                    ligne3.getChildren().addAll(label3, comboGene);
                    vBoxChoix.getChildren().addAll(ligne3);
                }
            }
        });
    }

    /**
     * Méthode qui permet de récupérer le node d'un gridpane en donnant ses coordonées x/ y dans celui-ci
     * @param gridPane
     * @param col
     * @param row
     * @return
     */
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    /**
     * Méthode qui va créer le label pour saisir la longueur des gènes
     * Pour chaque gène un label et textfield de saisie est créé
     * Un formater est utilisé pour ne saisir que des integers
     */
    private void createLabelTailleGene() {
        int i = 0;
        for (String gene : this.sizeGene.keySet()) {
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
            this.gridGene.addRow(i, nom, taille_gene);
            this.gridGene.setVgap(5);
            i++;
        }
    }

    /**
     * Méthode qui permet de générer la page de résultats d'analyse
     * Elle donne au controleur de résultats les différents paramètres qui ont été saisis sur la page d'accueil
     */
    @FXML
    private void nextWindow() {
        generateFiltreMap();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Interpreteur/Interpreteur.fxml"));
            setGeneSize();
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
                    controller.setFiltre(this.filtre);
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
                scene = new Scene(loader.load(), 1000, 600);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = new Stage();
            stage.setTitle("Analyse");
            stage.setScene(scene);
            stage.show();
    }

    /**
     * Méthode qui génère la hashmap filtre a envoyer au second controleur
     */
    private void generateFiltreMap() {
        filtre = new HashMap<>();
        if (comboBox.getSelectionModel().getSelectedItem().equals("Gene Analysis")){
            filtre.put("analysis","gene");
            filtre.put("gene",comboGeneAnalysis.getValue());
        }
        else if (comboBox.getSelectionModel().getSelectedItem().equals("Cohort Analysis")){
            filtre.put("analysis","cohort");
            filtre.put("cohort",textFieldCohort.getText());
            if (checkBoxGeneFilter.isSelected()){
                filtre.put("gene",comboGene.getValue());
            }
        }
        else {
            filtre.put("analysis","complet");
        }
    }

    /**
     * Permet de générer les noms des gènes dans la hashmap qui va accueillir nom - taille
     */
    private void setGeneSize() {
        int i = 0;
        for (String gene : this.sizeGene.keySet()) {
            this.sizeGene.put(gene, Integer.valueOf(((TextField) getNodeFromGridPane(this.gridGene, 1, i)).getText()));
            i++;
        }
    }

    /**
     * Permet d'ouvrir un explorateur pour choisir le fichier tsv qui devra être analysé
     */
    private void loadTSV() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.myStage);
        this.table_file = file;
        this.file_path.setText(file.getName());
    }

    @FXML
    private void choisirFichier() {
        button_table.setOnMouseClicked((event) -> {
            loadTSV();
        });
    }
}