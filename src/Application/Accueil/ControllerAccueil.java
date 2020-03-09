package Application.Accueil;

import Application.Interpreteur.ControllerInterpreteur;
import Application.Utils.TSVUtils;
import javafx.application.Platform;
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
import javafx.util.converter.IntegerStringConverter;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * Controleur de la fenetre d'accueil
 */
public class ControllerAccueil {

    /**
     * Fenetre d'accueil
     */
    private Stage myStage;

    /**
     * ComboBox contenant la liste des différentes analyses possibles
     */
    private ComboBox<String> comboBoxAnalyse;

    /**
     * TextField pour saisir le pattern pour l'analyse de cohorte
     */
    private TextField textFieldCohort;

    /**
     * Checkbox utilisé pour préciser si on veut ajouter l'analyse de cohorte sur un gène particulier
     */
    private CheckBox checkBoxGeneFilter;

    /**
     * Combobox contenant la liste des différents gènes présents dans le fichier TSV
     */
    private ComboBox<String> comboGene;

    /**
     * File du TSV utilisé pour l'analyse
     */
    private File fileTSV;

    /**
     * HashMap qui associe à chaque gène sa longueur et qui sera transmis au controller interpreteur
     */
    private HashMap<String, Integer> sizeGene;

    /**
     * HashMap qui contient les informations de la page d'accueil
     * Cette HashMap est transmise au controller interpreteur pour traitement
     */
    private HashMap<String, Object> infosAccueil;

    /**
     * File qui correspond aux données metadata qui permettent de filtrer les patients
     */
    private File fileMetadata;

    /**
     * HashMap qui associe a chaque champs du metadata le type de donnée associé
     */
    private HashMap<String, String> typeMetadata;

    /**
     * Contenu du fichier metadata
     */
    private ArrayList<List<String>> metadata;

    private VBox boxMetadata;


    @FXML
    private TextField minVert;
    @FXML
    private TextField maxVert;
    @FXML
    private TextField minOrange;
    @FXML
    private TextField maxOrange;
    @FXML
    private TextField minRouge;
    @FXML
    private TextField maxRouge;
    @FXML
    private Button buttonValider;
    @FXML
    private Button buttonFileTSV;
    @FXML
    private Button buttonFileMetadata;
    @FXML
    private Label filePathTSV;
    @FXML
    private Label filePathMetadata;
    @FXML
    private Button buttonOkMetadata;
    @FXML
    private GridPane gridGene;
    @FXML
    private ScrollPane scrollMeta;
    @FXML
    private VBox filtreVBox;
    @FXML
    private Label geneName;
    @FXML
    private Label geneSize;
    @FXML
    private ScrollPane scrollGene;
    @FXML
    private RadioButton dnaCheck;
    @FXML
    private RadioButton proteinCheck;
    @FXML
    private Label labelAnalyse;

    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            ToggleGroup group = new ToggleGroup();
            dnaCheck.setToggleGroup(group);
            dnaCheck.setSelected(true);
            proteinCheck.setToggleGroup(group);
        });
    }

    /**
     * Methode qui permet de formater les textfields de saisie des valeurs de couleurs
     * pour n'accepter que des décimaux. Génère aussi les valeurs par défaut
     */
    public void setFieldFomat() {
        this.minVert.setTextFormatter(getDoubleTextFormatter(0.5));
        this.minOrange.setTextFormatter(getDoubleTextFormatter(2.0));
        this.minRouge.setTextFormatter(getDoubleTextFormatter(20.0));
        this.maxVert.setTextFormatter(getDoubleTextFormatter(2.0));
        this.maxOrange.setTextFormatter(getDoubleTextFormatter(20.0));
        this.maxRouge.setTextFormatter(getDoubleTextFormatter(100.0));
        this.buttonValider.setStyle("-fx-border-color: black; -fx-text-fill: mediumblue");
    }

    /**
     * Fonction qui permet de générer un formater de décimaux en donnant en paramètre la valeur par défaut
     * Les décimaux sont arrondis 3 chiffres après la virgule et utilisent le '.' comme séparateur
     *
     * @param x valeur par défaut
     * @return textformateur de double
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
        this.buttonFileTSV.setOnMouseClicked((event -> {
            List<String> list_gene = TSVUtils.getListGenes(TSVUtils.getTSV(this.fileTSV));
            this.gridGene.setPadding(new Insets(10, 10, 10, 10));
            this.sizeGene = new HashMap<>();
            for (String gene : list_gene) {
                this.sizeGene.put(gene, 0);
            }
            createLabelTailleGene();
            generateFiltre();
            makeChildrenVisible();
        }));
    }

    /**
     * Méthode qui permet de rendre les différents champs visibles après selection du fichier TSV
     * des mutations
     */
    private void makeChildrenVisible() {
        this.geneName.setVisible(true);
        this.geneName.setStyle("-fx-font-weight: bold;");
        this.geneSize.setVisible(true);
        this.geneSize.setStyle("-fx-font-weight: bold;");
        this.scrollGene.setVisible(true);
        this.buttonFileMetadata.setVisible(true);
        this.buttonOkMetadata.setVisible(true);
        this.scrollMeta.setVisible(true);
        this.buttonValider.setVisible(true);
        this.labelAnalyse.setVisible(true);
        this.labelAnalyse.setStyle("-fx-font-weight: bold;");
        this.dnaCheck.setVisible(true);
        this.proteinCheck.setVisible(true);
    }

    /**
     * Méthode qui permet de générer la box contenant les différents types d'analyses
     * Le contenu change selon le choix de la comboBox
     */
    private void generateFiltre() {
        Label annonceFiltre = new Label("Type of research");
        annonceFiltre.setStyle("-fx-font-weight: bold;");
        annonceFiltre.setPadding(new Insets(10, 10, 10, 10));
        comboBoxAnalyse = new ComboBox<>();
        comboBoxAnalyse.getItems().setAll("Complet Analysis", "Cohort Analysis", "Gene Analysis");
        comboBoxAnalyse.getSelectionModel().select(0);

        VBox vBoxChoix = new VBox(5);
        vBoxChoix.setPadding(new Insets(10, 10, 10, 10));
        vBoxChoix.setStyle("-fx-border-color: black");
        infosAccueil = new HashMap<>();

        comboBoxAnalyse.valueProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case "Cohort Analysis":
                    vBoxChoix.getChildren().clear();
                    boxCohortAnalysis(vBoxChoix);
                    break;
                case "Complet Analysis":
                    vBoxChoix.getChildren().clear();
                    break;
                case "Gene Analysis":
                    vBoxChoix.getChildren().clear();
                    boxGeneAnalysis(vBoxChoix);
                    break;
            }
        });

        filtreVBox.getChildren().addAll(annonceFiltre, comboBoxAnalyse, vBoxChoix);
    }

    /**
     * Methode qui permet de genererer la box metadata pour filtrer par la suite
     */
    @FXML
    private void generateMetadata() {
        generateBoxMetadata();
        scrollMeta.setContent(boxMetadata);
    }

    /**
     * Méthode qui génère la box pour le choix d'une analyse d'un gène particulier
     *
     * @param vBoxChoix vbox du filtre
     */
    private void boxGeneAnalysis(VBox vBoxChoix) {
        HBox ligne3 = new HBox(10);
        ligne3.setPadding(new Insets(20, 10, 10, 10));
        ligne3.setAlignment(Pos.CENTER);
        Label label3 = new Label("Gene name");
        comboGene = new ComboBox<>();
        for (String gene : sizeGene.keySet()) {
            comboGene.getItems().add(gene);
        }
        comboGene.getSelectionModel().select(0);
        ligne3.getChildren().addAll(label3, comboGene);
        vBoxChoix.getChildren().addAll(ligne3);
    }

    /**
     * Méthode qui génère la box pour le choix d'une analyse de cohorte
     * Si on coche la checkbox, affiche le choix d'un gène spécifique pour l'analyse
     *
     * @param vBoxChoix vbox du filtre
     */
    private void boxCohortAnalysis(VBox vBoxChoix) {
        HBox ligne1 = new HBox(5);
        ligne1.setAlignment(Pos.CENTER);
        Label label1 = new Label("First letters");
        textFieldCohort = new TextField("");
        textFieldCohort.setPromptText("ex : DA");
        ligne1.getChildren().addAll(label1, textFieldCohort);

        HBox ligne2 = new HBox();
        ligne2.setAlignment(Pos.CENTER);
        checkBoxGeneFilter = new CheckBox();
        Label label2 = new Label("Add gene filter ");
        ligne2.getChildren().addAll(label2, checkBoxGeneFilter);

        vBoxChoix.getChildren().addAll(ligne1, ligne2);

        HBox ligne3 = new HBox(5);
        ligne3.setAlignment(Pos.CENTER);
        Label label3 = new Label("Gene name");
        comboGene = new ComboBox<>();
        for (String gene : sizeGene.keySet()) {
            comboGene.getItems().add(gene);
        }
        comboGene.getSelectionModel().select(0);
        ligne3.getChildren().addAll(label3, comboGene);

        checkBoxGeneFilter.setOnAction(event -> {
            if (checkBoxGeneFilter.isSelected()) {
                vBoxChoix.getChildren().add(ligne3);
            } else {
                vBoxChoix.getChildren().remove(ligne3);
            }
        });
    }

    /**
     * Méthode qui permet de récupérer le node d'un gridpane en donnant ses coordonées x/ y dans celui-ci
     *
     * @param gridPane gridpane dont on veut récupérer le node
     * @param row      ligne
     * @return node
     */
    private Node getNodeFromGridPane(GridPane gridPane, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == 1 && GridPane.getRowIndex(node) == row) {
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
            TextField taille_gene = new TextField("3000");
            taille_gene.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("\\d*")) {
                    taille_gene.setText(newValue.replaceAll("[^\\d]", ""));
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

        setGeneSize();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Application/Interpreteur/Interpreteur.fxml"));
        loader.setControllerFactory((Class<?> controllerType) -> {
            if (controllerType == ControllerInterpreteur.class) {
                ControllerInterpreteur controller = new ControllerInterpreteur();
                if (dnaCheck.isSelected()){
                    controller.setDnaAnalysis(true);
                }
                else {
                    controller.setDnaAnalysis(false);
                }
                controller.setMinVert(Double.valueOf(this.minVert.getText()));
                controller.setMaxVert(Double.valueOf(this.maxVert.getText()));
                controller.setMinOrange(Double.valueOf(this.minOrange.getText()));
                controller.setMaxOrange(Double.valueOf(this.maxOrange.getText()));
                controller.setMinRouge(Double.valueOf(this.minRouge.getText()));
                controller.setMaxRouge(Double.valueOf(this.maxRouge.getText()));
                controller.setMetadata(this.metadata);
                controller.setTypeMetadata(this.typeMetadata);
                controller.setSizeGene(this.sizeGene);
                controller.setFileTSV(this.fileTSV);
                controller.setFiltre(this.infosAccueil);
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
        infosAccueil = new HashMap<>();
        if (comboBoxAnalyse.getSelectionModel().getSelectedItem().equals("Gene Analysis")) {
            infosAccueil.put("analysis", "gene");
            infosAccueil.put("gene", comboGene.getValue());
        } else if (comboBoxAnalyse.getSelectionModel().getSelectedItem().equals("Cohort Analysis")) {
            infosAccueil.put("analysis", "cohort");
            infosAccueil.put("cohort", textFieldCohort.getText());
            if (checkBoxGeneFilter.isSelected()) {
                infosAccueil.put("gene", comboGene.getValue());
            }
        } else {
            infosAccueil.put("analysis", "complet");
        }
        HashMap<String,Object> metadataFiltre = new HashMap<>();
        if (!filePathMetadata.getText().isEmpty()){
            for (Node lineFilter : boxMetadata.getChildren()){
                if (((CheckBox)lineFilter.lookup("#checkbox")).isSelected()){
                    String key = ((Label)lineFilter.lookup("#nameMetadata")).getText();
                    for (String nameMeta : typeMetadata.keySet()){
                        if (key.equals(nameMeta)){
                            if (typeMetadata.get(key).equals("double")){
                                HashMap<String, Double> valuesDouble = new HashMap<>();
                                Double min = Double.valueOf(((TextField)lineFilter.lookup("#"+key+"min")).getText());
                                Double max = Double.valueOf(((TextField)lineFilter.lookup("#"+key+"max")).getText());
                                valuesDouble.put("min",min);
                                valuesDouble.put("max",max);
                                metadataFiltre.put(key,valuesDouble);
                            }
                            if (typeMetadata.get(key).equals("integer") || typeMetadata.get(key).equals("date")){
                                HashMap<String, Integer> valuesInteger = new HashMap<>();
                                Integer min = Integer.valueOf(((TextField)lineFilter.lookup("#"+key+"min")).getText());
                                Integer max = Integer.valueOf(((TextField)lineFilter.lookup("#"+key+"max")).getText());
                                valuesInteger.put("min",min);
                                valuesInteger.put("max",max);
                                metadataFiltre.put(key,valuesInteger);
                            }
                            else if (typeMetadata.get(key).equals("word")){
                                String input = ((TextField)lineFilter.lookup("#input")).getText();
                                metadataFiltre.put(key,input);
                            }
                        }
                    }
                }
            }
        }
        infosAccueil.put("metadata", metadataFiltre);
    }

    /**
     * Permet de générer remplir la HashMap Gène - Taille avec la valeur saisie
     */
    private void setGeneSize() {
        int i = 0;
        for (String gene : this.sizeGene.keySet()) {
            this.sizeGene.put(gene, Integer.valueOf(((TextField) Objects.requireNonNull(getNodeFromGridPane(this.gridGene, i))).getText()));
            i++;
        }
    }

    /**
     * Permet d'ouvrir un explorateur pour choisir le fichier tsv qui devra être analysé
     */
    @FXML
    private void loadTSV() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.myStage);
        if (!(file ==null)){
            this.fileTSV = file;
            this.filePathTSV.setText(file.getName());
        }
    }

    /**
     * Méthode qui permet de charger le fichier de metadata
     */
    @FXML
    private void loadTSVFiltre() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(this.myStage);
        if (!(file==null)){
            this.fileMetadata = file;
            this.filePathMetadata.setText(file.getName());
        }
    }

    /**
     * Methode qui permet de genererer la Vbox qui contiendra les différentes options de filtre par rapport
     * au fichier de metadata
     */
    private void generateBoxMetadata() {
        boxMetadata = new VBox(15);
        boxMetadata.setPadding(new Insets(10, 10, 10, 10));
        metadata = TSVUtils.getTSV(this.fileMetadata);

        List<String> listMetadataFilter = new ArrayList<>();
        for (String enTete : metadata.get(0)) {
            if (!enTete.equals("SAMPLEID")) {
                listMetadataFilter.add(enTete);
            }
        }

        typeMetadata = createMetadataMap(metadata, listMetadataFilter);

        for (String key : typeMetadata.keySet()) {
            switch (typeMetadata.get(key)) {
                case "word": {
                    HBox lineMetadata = hBoxWord(key);
                    boxMetadata.getChildren().add(lineMetadata);
                    break;
                }
                case "integer": {
                    HBox lineMetadata = hBoxInt(key);
                    boxMetadata.getChildren().add(lineMetadata);
                    break;
                }
                case "double": {
                    HBox lineMetadata = hBoxDouble(key);
                    boxMetadata.getChildren().add(lineMetadata);
                    break;
                }
                case "date": {
                    HBox lineMetadata = hBoxDate(key);
                    boxMetadata.getChildren().add(lineMetadata);
                    break;
                }
            }
        }
    }

    private HBox hBoxWord(String key) {
        HBox lineMetadata = new HBox(10);
        lineMetadata.setAlignment(Pos.CENTER_LEFT);
        Label nameMetadata = new Label(key);
        nameMetadata.setId("nameMetadata");

        CheckBox checkMeta = new CheckBox();
        checkMeta.setId("checkbox");

        lineMetadata.getChildren().addAll(nameMetadata, checkMeta);

        TextField inputMot = new TextField();
        inputMot.setId("input");

        checkMeta.setOnAction(event -> {
            if (checkMeta.isSelected()) {
                lineMetadata.getChildren().add(inputMot);
            } else {
                lineMetadata.getChildren().remove(inputMot);
            }
        });
        return lineMetadata;
    }

    private HBox hBoxInt(String key) {
        HBox lineMetadata = new HBox(10);
        lineMetadata.setAlignment(Pos.CENTER_LEFT);
        Label nameMetadata = new Label(key);
        nameMetadata.setId("nameMetadata");

        CheckBox checkMeta = new CheckBox();
        checkMeta.setId("checkbox");

        lineMetadata.getChildren().addAll(nameMetadata, checkMeta);

        HBox lineMetadataInt = new HBox(5);

        TextField inputIntMin = getTextFieldIntFormated();
        inputIntMin.setId(key+"min");
        TextField inputIntMax = getTextFieldIntFormated();
        inputIntMax.setId(key+"max");
        Label separator = new Label("< Value <");

        lineMetadataInt.getChildren().addAll(inputIntMin, separator, inputIntMax);
        lineMetadataInt.setAlignment(Pos.CENTER_LEFT);

        checkMeta.setOnAction(event -> {
            if (checkMeta.isSelected()) {
                lineMetadata.getChildren().add(lineMetadataInt);
            } else {
                lineMetadata.getChildren().remove(lineMetadataInt);
            }
        });
        return lineMetadata;
    }

    private HBox hBoxDate(String key) {
        HBox lineMetadata = new HBox(10);
        lineMetadata.setAlignment(Pos.CENTER_LEFT);
        Label nameMetadata = new Label(key);
        nameMetadata.setId("nameMetadata");

        CheckBox checkMeta = new CheckBox();
        checkMeta.setId("checkbox");

        lineMetadata.getChildren().addAll(nameMetadata, checkMeta);

        HBox lineMetadataDate = new HBox(5);

        TextField inputIntMin = getTextFieldIntFormated();
        inputIntMin.setPrefWidth(80);
        inputIntMin.setId(key+"min");
        TextField inputIntMax = getTextFieldIntFormated();
        inputIntMax.setPrefWidth(80);
        inputIntMax.setId(key+"max");
        Label from = new Label("From");
        Label to = new Label("To");

        lineMetadataDate.getChildren().addAll(from, inputIntMin, to, inputIntMax);
        lineMetadataDate.setAlignment(Pos.CENTER_LEFT);

        checkMeta.setOnAction(event -> {
            if (checkMeta.isSelected()) {
                lineMetadata.getChildren().add(lineMetadataDate);
            } else {
                lineMetadata.getChildren().remove(lineMetadataDate);
            }
        });
        return lineMetadata;
    }

    private HBox hBoxDouble(String key) {
        HBox lineMetadata = new HBox(10);
        lineMetadata.setAlignment(Pos.CENTER_LEFT);
        Label nameMetadata = new Label(key);
        nameMetadata.setId("nameMetadata");

        CheckBox checkMeta = new CheckBox();
        checkMeta.setId("checkbox");

        lineMetadata.getChildren().addAll(nameMetadata, checkMeta);

        HBox lineMetadataDouble = new HBox(5);

        TextField inputDoubleMin = new TextField();
        inputDoubleMin.setTextFormatter(getDoubleTextFormatter(0.0));
        inputDoubleMin.setPrefWidth(80);
        inputDoubleMin.setId(key+"min");
        TextField inputDoubleMax = new TextField();
        inputDoubleMax.setTextFormatter(getDoubleTextFormatter(100.0));
        inputDoubleMax.setPrefWidth(80);
        inputDoubleMax.setId(key+"max");
        Label value = new Label("< Value <");

        lineMetadataDouble.getChildren().addAll(inputDoubleMin, value, inputDoubleMax);
        lineMetadataDouble.setAlignment(Pos.CENTER_LEFT);

        checkMeta.setOnAction(event -> {
            if (checkMeta.isSelected()) {
                lineMetadata.getChildren().add(lineMetadataDouble);
            } else {
                lineMetadata.getChildren().remove(lineMetadataDouble);
            }
        });
        return lineMetadata;
    }

    /**
     * Méthode qui permet de remplir la hashmap metadata reférence pour savoir quel est le bon type de donnée
     * pour chaque donnée du fichier metadata
     *
     * @param metadata     Hashmap metadata
     * @param listMetadata Liste des valeurs dans le metadata
     * @return HashMap remplie
     */
    private HashMap<String, String> createMetadataMap(ArrayList<List<String>> metadata, List<String> listMetadata) {
        HashMap<String, String> typeMetadata = new HashMap<>();
        String patternWord = "[a-zA-Z]+";
        String patternDouble = "[-+]?[0-9]*\\.[0-9]+([eE][-+]?[0-9]+)?";
        String patternInteger = "\\d+";
        String patternDate = "\\d{6}";

        for (int i = 1; i < metadata.get(1).size(); i++) {
            if (metadata.get(1).get(i).matches(patternDouble)) {
                typeMetadata.put(listMetadata.get(i - 1), "double");
            } else if (metadata.get(1).get(i).matches(patternDate)) {
                typeMetadata.put(listMetadata.get(i - 1), "date");
            } else if (metadata.get(1).get(i).matches(patternWord)) {
                typeMetadata.put(listMetadata.get(i - 1), "word");
            } else if (metadata.get(1).get(i).matches(patternInteger)) {
                typeMetadata.put(listMetadata.get(i - 1), "integer");
            }
        }
        return typeMetadata;
    }

    /**
     * Méthode qui permet de retourner un texfield formaté pour n'entrer que des chiffres
     *
     * @return TextField
     */
    private TextField getTextFieldIntFormated() {
        TextField inputInt = new TextField();
        UnaryOperator<TextFormatter.Change> integerOnlyKeyboardInput = change -> change.getText().matches("[0-9]*") ? change : null;
        StringConverter<Integer> integerValueConverter = new IntegerStringConverter();
        TextFormatter<Integer> integerOnlyFormatter = new TextFormatter<>(integerValueConverter, 0, integerOnlyKeyboardInput);
        inputInt.setTextFormatter(integerOnlyFormatter);
        return inputInt;
    }

    public void setStage(Stage stage) {
        myStage = stage;
    }

}