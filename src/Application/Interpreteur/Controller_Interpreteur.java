package Application.Interpreteur;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import Application.Utils.TableUtils;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static Application.Utils.TableUtils.*;
import static javafx.embed.swing.SwingFXUtils.fromFXImage;

/**
 * Classe qui sert de controller à la fenetre de l'interpreteur
 */
public class Controller_Interpreteur {

    private Double min_vert;

    private Double max_vert;

    private Double min_orange;

    private Double max_orange;

    private Double min_rouge;

    private Double max_rouge;

    private File table_file;

    /**
     * Contenu du fichier TSV
     */
    private List<List<String>> tsv;

    private HashMap<String, Integer> sizeGene;

    private HashMap<String, Object> filtre;

    /**
     * Contenu du fichier metadata
     */
    private ArrayList<List<String>> metadata;

    /**
     * HashMap qui associe a chaque champs du metadata le type de donnée associé
     */
    private HashMap<String, String> typeMetadata;

    @FXML
    private Label label_couleur;

    @FXML
    private Label label_file;

    @FXML
    private Label label_filtre;

    @FXML
    private AnchorPane pane_interpreteur;

    @FXML
    private VBox vbox;

    @FXML
    private VBox vboxMini;

    @FXML
    private Button exportTotal;

    @FXML
    private Button exportPatient;

    @FXML
    private Button exportDixieme;

    @FXML
    private CheckBox checkVisible;

    @FXML
    private ComboBox mutationCombo;


    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            setLabels();

            exportTotal.setOnAction(e -> saveImageTotal());
            exportPatient.setOnAction(e -> saveImagePatient());
            exportDixieme.setOnAction(e -> saveImageMini());

            System.out.println(filtre);

            List<String> listGenes = getListGenes(this.tsv);
            List<Patient> listPatients;
            if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
                listPatients = getListPatient(this.tsv);
            }
            else {
                listPatients = getListPatientMetadata(this.tsv, this.metadata, this.typeMetadata);
            }

            performMutationVisible();

            setMutationsPatients(listPatients, this.tsv);

            mutationCombo.setItems(getMutationsList(listPatients));
            mutationCombo.valueProperty().addListener((obs, oldItem, newItem) -> {
                setSpecificMutationVisible(newItem.toString());
            });

            generateAnalysis(listGenes, listPatients);
            generateAnalysisMini(listGenes, listPatients);
        });
    }

    /**
     * Méthode qui permet de rendre les labels des mutations visibles ou invisibles quand la checkbox
     * est cochée ou non
     */
    private void performMutationVisible() {
        checkVisible.setOnAction(event -> {
            if (checkVisible.isSelected()) {
                makeLabelsVisible(true);
            } else {
                makeLabelsVisible(false);
            }
        });
    }

    private void setSpecificMutationVisible(String mutation){
        for (Node child : vbox.getChildren()) {
            if (child instanceof VBox) {
                for (Node childBoxPatient : ((VBox) child).getChildren()) {
                    if (childBoxPatient instanceof HBox){
                        for (Node childHboxGene : ((HBox) childBoxPatient).getChildren()){
                            if (childHboxGene instanceof AnchorPane){
                                for (Node groupMutation : ((AnchorPane) childHboxGene).getChildren()){
                                    if (groupMutation.getId()!=null) {
                                        if (groupMutation.getId().contains(mutation)) {
                                            groupMutation.setVisible(true);
                                        } else {
                                            groupMutation.setVisible(false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Méthode qui permet d'exporter le contenu de l'anchorpane en fichier png
     */
    @FXML
    private void saveImagePatient() {
        FileChooser fileChooser = new FileChooser();

        //Prompt user to select a file
        File file = fileChooser.showSaveDialog(null);
        new File(file.getPath()).mkdir();

        int i = 1;
        for (Node node:this.vbox.getChildren()
        ) {
            if (node instanceof VBox){
                BufferedImage bufferedImage = new BufferedImage(550, 400, BufferedImage.TYPE_INT_ARGB);

                WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);
                ((VBox)node).getChildren().add(new ImageView(snapshot));

                BufferedImage image;
                image = fromFXImage(snapshot, bufferedImage);

                try {
                    Graphics2D gd = (Graphics2D) image.getGraphics();
                    gd.translate(((VBox)node).getWidth(), ((VBox)node).getHeight());
                    ImageIO.write(image, "png", new File(file.getPath() + "/" +
                            ((Label)node.lookup("#NomPatientLabel")).getText()));
                } catch (IOException ignored) {
                }
            }
            i++;
        }
    }

    /**
     * Méthode qui permet d'exporter le contenu de l'anchorpane en fichier png
     */
    @FXML
    private void saveImageTotal() {
        BufferedImage bufferedImage = new BufferedImage(550, 400, BufferedImage.TYPE_INT_ARGB);

        WritableImage snapshot = pane_interpreteur.snapshot(new SnapshotParameters(), null);
        pane_interpreteur.getChildren().add(new ImageView(snapshot));

        BufferedImage image;
        image = fromFXImage(snapshot, bufferedImage);

        try {
            Graphics2D gd = (Graphics2D) image.getGraphics();
            gd.translate(pane_interpreteur.getWidth(), pane_interpreteur.getHeight());
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));

            //Prompt user to select a file
            File file = fileChooser.showSaveDialog(null);
            ImageIO.write(image, "png", file);
        } catch (IOException ignored) {
        }
    }

    /**
     * Méthode qui permet d'exporter le contenu de l'anchorpane en fichier png
     */
    @FXML
    private void saveImageMini() {
        BufferedImage bufferedImage = new BufferedImage(550, 400, BufferedImage.TYPE_INT_ARGB);

        WritableImage snapshot = this.vboxMini.snapshot(new SnapshotParameters(), null);
        this.vboxMini.getChildren().add(new ImageView(snapshot));

        BufferedImage image;
        image = fromFXImage(snapshot, bufferedImage);

        try {
            Graphics2D gd = (Graphics2D) image.getGraphics();
            gd.translate(this.vboxMini.getWidth(), this.vboxMini.getHeight());
            FileChooser fileChooser = new FileChooser();
            //Set extension filter
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png files (*.png)", "*.png"));

            //Prompt user to select a file
            File file = fileChooser.showSaveDialog(null);
            ImageIO.write(image, "png", file);
        } catch (IOException ignored) {
        }
    }

    /**
     * Méthode qui va générer tous les gènes avec leurs mutations pour chaque cas d'analyse différents
     *
     * @param listGenes liste des gènes
     * @param listPatients liste des patients
     */
    private void generateAnalysis(List<String> listGenes, List<Patient> listPatients) {
        if (this.filtre.get("analysis").equals("complet")) {
            for (Patient patient : listPatients) {
                duplicateCodeAnalysis(listGenes, patient);
            }
        }
        if (this.filtre.get("analysis").equals("gene")) {
            for (Patient patient : listPatients) {
                if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
                    generatePatientForOnePreciseGene(listGenes, patient);
                }
                else {
                    if (checkMetadata(patient)){
                        generatePatientForOnePreciseGene(listGenes, patient);
                    }
                }
            }
        }
        if (this.filtre.get("analysis").equals("cohort")) {
            if (this.filtre.containsKey("gene")) {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.filtre.get("cohort")))) {
                        if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
                            generatePatientForOnePreciseGene(listGenes, patient);
                        }
                        else {
                            if (checkMetadata(patient)){
                                generatePatientForOnePreciseGene(listGenes, patient);
                            }
                        }
                    }
                }
            } else {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.filtre.get("cohort")))) {
                        duplicateCodeAnalysis(listGenes, patient);
                    }
                }
            }
        }
    }

    private void duplicateCodeAnalysis(List<String> listGenes, Patient patient) {
        if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
            VBox boxPatient = getvBox(patient);
            for (String gene : listGenes) {
                HBox hbox = newGeneBox(gene, patient.getMutationList());
                HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(),false);
                boxPatient.getChildren().addAll(hbox, hbox2);
            }
            this.vbox.getChildren().add(boxPatient);
        }
        else {
            if (checkMetadata(patient)){
                VBox boxPatient = getvBox(patient);
                for (String gene : listGenes) {
                    HBox hbox = newGeneBox(gene, patient.getMutationList());
                    HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(),false);
                    boxPatient.getChildren().addAll(hbox, hbox2);
                }
                this.vbox.getChildren().add(boxPatient);
            }
        }
    }

    /**
     * Méthode qui permet de checker le contenu metadata de patient et celui du filtre pour savoir s'il faut
     * générer ou non l'analyse du patient
     * @param patient patient
     * @return true ou false selon que le patient doit etre généré ou non
     */
    private boolean checkMetadata(Patient patient){
        for (String metadataPatient: patient.getMetadata().keySet()){
            if (((HashMap<String, Object>)filtre.get("metadata")).containsKey(metadataPatient)){
                if (patient.getMetadata().get(metadataPatient) instanceof String){
                    if (!((HashMap<String, Object>)filtre.get("metadata")).get(metadataPatient).equals(patient.getMetadata().get(metadataPatient))){
                        return false;
                    }
                } else if (patient.getMetadata().get(metadataPatient) instanceof Double) {
                    if ((Double) patient.getMetadata().get(metadataPatient) > ((Double) ((HashMap<String, Object>) ((HashMap<String, Object>) filtre.get("metadata")).get(metadataPatient)).get("max")) ||
                            (Double) patient.getMetadata().get(metadataPatient) < ((Double) ((HashMap<String, Object>) ((HashMap<String, Object>) filtre.get("metadata")).get(metadataPatient)).get("min"))) {
                        return false;
                    }
                } else if (patient.getMetadata().get(metadataPatient) instanceof Integer) {
                    if ((Integer) patient.getMetadata().get(metadataPatient) > ((Integer) ((HashMap<String, Object>) ((HashMap<String, Object>) filtre.get("metadata")).get(metadataPatient)).get("max")) ||
                            (Integer) patient.getMetadata().get(metadataPatient) < ((Integer) ((HashMap<String, Object>) ((HashMap<String, Object>) filtre.get("metadata")).get(metadataPatient)).get("min"))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Méthode pour générer la liste des analyses génomique dans le cas où l'analyse se concentre sur
     * un seul gène
     *
     * @param listGenes liste des gènes
     * @param patient patient en cours
     */
    private void generatePatientForOnePreciseGene(List<String> listGenes, Patient patient) {
        VBox boxPatient = getvBox(patient);
        for (String gene : listGenes) {
            if (gene.equals(this.filtre.get("gene"))) {
                HBox hbox = newGeneBox(gene, patient.getMutationList());
                HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(), false);
                boxPatient.getChildren().addAll(hbox, hbox2);
            }
        }
        this.vbox.getChildren().add(boxPatient);
    }

    /**
     * Création de la box patient avec le nom du patient
     * La VBox se remplira avec les différents gènes par la suite
     *
     * @param patient Patient en cours de création
     * @return VBox du Patient
     */
    private VBox getvBox(Patient patient) {
        VBox boxPatient = new VBox(10);
        Label nom_patient = new Label(patient.getIdentifiant());
        nom_patient.setId("NomPatientLabel");
        nom_patient.setTextFill(Color.RED);
        nom_patient.setFont(Font.font("Cambria", 20));
        nom_patient.setAlignment(Pos.BASELINE_LEFT);
        boxPatient.getChildren().addAll(nom_patient);
        return boxPatient;
    }

    /**
     * Création de la box patient avec le nom du patient dans le cas d'un seul gène par patient
     *
     * @param patient Patient en cours de création
     * @return HBox du Patient
     */
    private HBox getHBoxMini(Patient patient) {
        HBox boxPatient = new HBox();
        Label nom_patient = new Label(patient.getIdentifiant());
        nom_patient.setPrefWidth(100.0);
        nom_patient.setTextFill(Color.RED);
        nom_patient.setFont(Font.font("Cambria", 20));
        nom_patient.setAlignment(Pos.CENTER);

        boxPatient.getChildren().addAll(nom_patient);
        return boxPatient;
    }

    /**
     * Création de la box patient avec le nom du patient dans le cas de plusieurs gènes
     * @param patient
     * @return VBox du patient
     */
    private VBox getVBoxMini(Patient patient) {
        VBox boxPatient = new VBox();
        boxPatient.setSpacing(5.0);
        Label nom_patient = new Label(patient.getIdentifiant());
        nom_patient.setTextFill(Color.RED);
        nom_patient.setFont(Font.font("Cambria", 20));
        nom_patient.setAlignment(Pos.CENTER);

        boxPatient.getChildren().addAll(nom_patient);
        return boxPatient;
    }

    /**
     * Méthode qui permet de compléter les labels en haut de l'analyse, pour rappeler les valeurs
     * de couleurs saisie ainsi que le filtre d'analyse utilisé
     */
    private void setLabels() {
        this.label_couleur.setText("Green value : " + this.min_vert + " - " + this.max_vert + "\n" +
                "Orange value : " + this.min_orange + " - " + this.max_orange + "\n" +
                "Red value : " + this.min_rouge + " - " + this.max_rouge);
        this.label_file.setText(this.table_file.getPath());
        this.tsv = TableUtils.getTSV(this.table_file);
        if (this.filtre.get("analysis").equals("complet")) {
            this.label_filtre.setText("Complet analysis running");
        } else if (this.filtre.get("analysis").equals("gene")) {
            this.label_filtre.setText("Gene analysis running \nGene : " + this.filtre.get("gene"));
        } else {
            if (this.filtre.containsKey("gene")) {
                this.label_filtre.setText("Cohort analysis running \nCohort : " + this.filtre.get("cohort") +
                        "\nGene : " + this.filtre.get("gene"));
            } else {
                this.label_filtre.setText("Cohort analysis running \n Cohort : " + this.filtre.get("cohort"));
            }
        }
    }


    /**
     * Méthode qui permet de génerer un gène avec toutes ses mutations.
     * La liste de mutations en paramètre est celle d'un patient
     *
     * @param nom_du_gene Nom du gène
     * @param mutationList Liste des mutations
     * @return Gène construit avec ses mutations
     */
    private HBox newGeneBox(String nom_du_gene, List<Mutation> mutationList) {
        Rectangle rec = rect(this.sizeGene.get(nom_du_gene), 40);

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.TOP_CENTER);
        hbox.setMinHeight(80);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Label nom_gene = new Label(nom_du_gene);
        nom_gene.setMinWidth(150);
        nom_gene.setMinHeight(90);
        nom_gene.setAlignment(Pos.CENTER);

        AnchorPane gene_pane = new AnchorPane();
        gene_pane.getChildren().add(rec);
        AnchorPane.setTopAnchor(rec, 40.0);

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux() > this.min_vert) {
                createMutationBox(gene_pane, mutation);
            }
        }

        hbox.getChildren().add(nom_gene);
        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un gène avec toutes ses mutations en version 1/10ème.
     * La liste de mutations en paramètre est celle d'un patient
     *
     * @param nom_du_gene
     * @param mutationList
     * @param versionMini
     * @return Gène construit avec ses mutations en version mini
     */
    private HBox newGeneBoxMini(String nom_du_gene, List<Mutation> mutationList, Boolean versionMini) {
        Rectangle rec = rect(this.sizeGene.get(nom_du_gene) / 10, 20);

        HBox hbox = new HBox();
        hbox.setMinHeight(20);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        AnchorPane gene_pane = new AnchorPane();
        gene_pane.getChildren().add(rec);

        if (!versionMini) {
            AnchorPane.setLeftAnchor(rec, 150.0);
        }

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux() > this.min_vert) {
                createMutationBoxMini(gene_pane, mutation, versionMini);
            }
        }

        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un gène avec toutes ses mutations en version 1/10ème.
     * La liste de mutations en paramètre est celle d'un patient
     *
     * @param nom_du_gene
     * @param mutationList
     * @param versionMini
     * @return Gène construit avec ses mutations en version mini
     */
    private HBox newGeneBoxMiniAvecGene(String nom_du_gene, List<Mutation> mutationList, Boolean versionMini) {
        Rectangle rec = rect(this.sizeGene.get(nom_du_gene) / 10, 20);

        HBox hbox = new HBox();
        hbox.setMinHeight(20);

        Label nom_gene = new Label(nom_du_gene);
        nom_gene.setMinWidth(150);
        nom_gene.setAlignment(Pos.CENTER);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        AnchorPane gene_pane = new AnchorPane();
        gene_pane.getChildren().add(rec);

        if (!versionMini) {
            AnchorPane.setLeftAnchor(rec, 150.0);
        }

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux() > this.min_vert) {
                createMutationBoxMini(gene_pane, mutation, versionMini);
            }
        }

        hbox.getChildren().add(nom_gene);
        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle
     *
     * @param gene_pane pane du gène
     * @param mutation mutation à ajouter
     */
    private void createMutationBox(AnchorPane gene_pane, Mutation mutation) {

        Rectangle rec;

        if (mutation.getTaux() <= this.max_vert && mutation.getTaux() > this.min_vert) {
            rec = rec_vert(39, 5);
        } else if (mutation.getTaux() <= this.max_orange && mutation.getTaux() > this.min_orange) {
            rec = rec_orange(39, 5);
        } else {
            rec = rec_rouge(39, 5);
        }

        Label mutationLabel;
        if (mutation.getMutation_nuc().equals("COMPLEX")) {
            mutationLabel = getLabelMutation("CX" + "\n" + mutation.getPosition_nuc());
        } else {
            mutationLabel = getLabelMutation(mutation.getMutation_nuc() + "\n" + mutation.getPosition_nuc());
        }

        mutationLabel.setMinWidth(30);
        mutationLabel.setVisible(false);
        mutationLabel.setId("MutationLabel" + mutationLabel.getText());

        rec.setId(mutationLabel.getText());

        rec.setOnMouseClicked(event -> {
            if (mutationLabel.isVisible()) {
                mutationLabel.setVisible(false);
            } else {
                mutationLabel.setVisible(true);
            }
        });

        gene_pane.getChildren().add(rec);
        gene_pane.getChildren().add(mutationLabel);

        AnchorPane.setTopAnchor(mutationLabel, 0.0);
        AnchorPane.setLeftAnchor(mutationLabel, Double.valueOf(mutation.getPosition_nuc()) - 10.0);

        AnchorPane.setTopAnchor(rec, 41.0);
        AnchorPane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_nuc()));

    }

    /**
     * Méthode qui parcourt tous les gènes présents dans la VBox et qui setVisible les labels des mutations
     * a true ou false
     * @param visible true or false
     */
    private void makeLabelsVisible(boolean visible) {
        for (Node child : vbox.getChildren()) {
            if (child instanceof VBox) {
                for (Node childBoxPatient : ((VBox) child).getChildren()) {
                    if (childBoxPatient instanceof HBox){
                        for (Node childHboxGene : ((HBox) childBoxPatient).getChildren()){
                            if (childHboxGene instanceof AnchorPane){
                                for (Node groupMutation : ((AnchorPane) childHboxGene).getChildren()){
                                    if (groupMutation.getId()!=null) {
                                        if (groupMutation.getId().contains("MutationLabel")) {
                                            groupMutation.setVisible(visible);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle
     *
     * @param gene_pane Pane du gène
     * @param mutation Mutation en cours de création
     */
    private void createMutationBoxMini(AnchorPane gene_pane, Mutation mutation, Boolean versionMini) {

        Rectangle rec;

        if (mutation.getTaux() <= this.max_vert && mutation.getTaux() > this.min_vert) {
            rec = rec_vert(19, 2);
        } else if (mutation.getTaux() <= this.max_orange && mutation.getTaux() > this.min_orange) {
            rec = rec_orange(19, 2);
        } else {
            rec = rec_rouge(19, 2);
        }

        gene_pane.getChildren().add(rec);


        AnchorPane.setTopAnchor(rec, 1.0);
        if (versionMini){
            AnchorPane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_nuc()) / 10);
        }
        else {
            AnchorPane.setLeftAnchor(rec, 150.0 + Double.valueOf(mutation.getPosition_nuc()) / 10);
        }
    }

    /**
     * Méthode qui va générer tous les gènes avec leurs mutations pour chaque cas d'analyse différents
     * en version mini
     *
     * @param listGenes liste des gènes
     * @param listPatients liste des patients
     */
    private void generateAnalysisMini(List<String> listGenes, List<Patient> listPatients) {
        Label titreMini = new Label("Compacted Version");
        titreMini.setFont(Font.font("Cambria", 20));
        this.vboxMini.getChildren().add(titreMini);

        if (this.filtre.get("analysis").equals("complet")) {
            for (Patient patient : listPatients) {
                if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
                    VBox boxPatient = getvBox(patient);
                    for (String gene : listGenes) {
                        HBox hbox2 = newGeneBoxMiniAvecGene(gene, patient.getMutationList(),true);
                        boxPatient.getChildren().addAll(hbox2);
                    }
                    this.vboxMini.getChildren().add(boxPatient);
                }
                else {
                    if (checkMetadata(patient)){
                        VBox boxPatient = getvBox(patient);
                        for (String gene : listGenes) {
                            HBox hbox2 = newGeneBoxMiniAvecGene(gene, patient.getMutationList(),true);
                            boxPatient.getChildren().addAll(hbox2);
                        }
                        this.vboxMini.getChildren().add(boxPatient);
                    }
                }
            }
        }
        if (this.filtre.get("analysis").equals("gene")) {
            for (Patient patient : listPatients) {
                duplicateCodeAnalysisMini(listGenes, patient);
            }
        }
        if (this.filtre.get("analysis").equals("cohort")) {
            if (this.filtre.containsKey("gene")) {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.filtre.get("cohort")))) {
                        duplicateCodeAnalysisMini(listGenes, patient);
                    }
                }
            } else {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.filtre.get("cohort")))) {
                        if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
                            VBox boxPatient = getVBoxMini(patient);
                            for (String gene : listGenes) {
                                HBox hbox2 = newGeneBoxMiniAvecGene(gene, patient.getMutationList(),true);
                                boxPatient.getChildren().addAll(hbox2);
                            }
                            this.vboxMini.getChildren().add(boxPatient);
                        }
                        else{
                            if (checkMetadata(patient)) {
                                VBox boxPatient = getVBoxMini(patient);
                                for (String gene : listGenes) {
                                    HBox hbox2 = newGeneBoxMiniAvecGene(gene, patient.getMutationList(),true);
                                    boxPatient.getChildren().addAll(hbox2);
                                }
                                this.vboxMini.getChildren().add(boxPatient);
                            }
                        }
                    }
                }
            }
        }
    }

    private void duplicateCodeAnalysisMini(List<String> listGenes, Patient patient) {
        if (((HashMap<String,Object>)filtre.get("metadata")).isEmpty()){
            HBox boxPatient = getHBoxMini(patient);
            for (String gene : listGenes) {
                if (gene.equals(this.filtre.get("gene"))) {
                    HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(), true);
                    boxPatient.getChildren().addAll(hbox2);
                }
            }
            this.vboxMini.getChildren().add(boxPatient);
        }
        else {
            if (checkMetadata(patient)) {
                HBox boxPatient = getHBoxMini(patient);
                for (String gene : listGenes) {
                    if (gene.equals(this.filtre.get("gene"))) {
                        HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(), true);
                        boxPatient.getChildren().addAll(hbox2);
                    }
                }
                this.vboxMini.getChildren().add(boxPatient);
            }
        }
    }

    /**
     * Génération du rectangle correspondant au gène
     *
     * @param width Largeur
     * @param height Hauteur
     * @return Rectangle blanc
     */
    private Rectangle rect(int width, int height) {
        Rectangle rec = new Rectangle(width, height);
        rec.setFill(Color.WHITE);
        rec.setStroke(Color.BLACK);
        return rec;
    }

    /**
     * Génération du rectangle mutation rouge
     *
     * @param height Hauteur
     * @param width Largeur
     * @return Rectangle rouge
     */
    private Rectangle rec_rouge(int height, int width) {
        Rectangle rec_rouge = new Rectangle(width, height);
        rec_rouge.setFill(Color.rgb(255, 0, 0));
        return rec_rouge;
    }

    /**
     * Génération du rectangle mutation orange
     *
     * @param height Hauteur
     * @param width Largeur
     * @return Rectangle orange
     */
    private Rectangle rec_orange(int height, int width) {
        Rectangle rec_rouge = new Rectangle(width, height);
        rec_rouge.setFill(Color.rgb(255, 153, 0));
        return rec_rouge;
    }

    /**
     * Génération du rectangle mutation vert
     *
     * @param height Hauteur
     * @param width Largeur
     * @return Rectangle vert
     */
    private Rectangle rec_vert(int height, int width) {
        Rectangle rec_vert = new Rectangle(width, height);
        rec_vert.setFill(Color.GREEN);
        return rec_vert;
    }

    /**
     * Génération du label qui va accueillir nom + position de la mutation
     *
     * @param text Texte qui sera dans le label
     * @return label remplis
     */
    private Label getLabelMutation(String text) {
        return new Label(text);
    }

    public void setSizeGene(HashMap<String, Integer> sizeGene) {
        this.sizeGene = sizeGene;
    }

    public void setMin_vert(Double min_vert) {
        this.min_vert = min_vert;
    }

    public void setMax_vert(Double max_vert) {
        this.max_vert = max_vert;
    }

    public void setMin_orange(Double min_orange) {
        this.min_orange = min_orange;
    }

    public void setMax_orange(Double max_orange) {
        this.max_orange = max_orange;
    }

    public void setMin_rouge(Double min_rouge) {
        this.min_rouge = min_rouge;
    }

    public void setMax_rouge(Double max_rouge) {
        this.max_rouge = max_rouge;
    }

    public void setTable_file(File table_file) {
        this.table_file = table_file;
    }

    public void setFiltre(HashMap<String, Object> filtre) {
        this.filtre = filtre;
    }

    public ArrayList<List<String>> getMetadata() {
        return metadata;
    }

    public void setMetadata(ArrayList<List<String>> metadata) {
        this.metadata = metadata;
    }

    public HashMap<String, String> getTypeMetadata() {
        return typeMetadata;
    }

    public void setTypeMetadata(HashMap<String, String> typeMetadata) {
        this.typeMetadata = typeMetadata;
    }
}
