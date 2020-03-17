package Application.Interpreteur;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import Application.Utils.TSVUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

import static Application.Utils.TSVUtils.*;
import static javafx.embed.swing.SwingFXUtils.fromFXImage;

/**
 * Classe qui sert de controller à la fenetre de l'interpreteur
 */
public class ControllerInterpreteur {

    private Double minVert;

    private Double maxVert;

    private Double minOrange;

    private Double maxOrange;

    private Double minRouge;

    private Double maxRouge;

    private File fileTSV;

    /**
     * Contenu du fichier TSV
     */
    private List<List<String>> tsv;

    /**
     * Longueur des différents gènes
     */
    private HashMap<String, Integer> sizeGene;

    /**
     * Filtre indiquant le type d'analyse
     */
    private HashMap<String, Object> infosAccueil;

    /**
     * Contenu du fichier metadata
     */
    private ArrayList<List<String>> metadata;

    /**
     * HashMap qui associe a chaque champs du metadata le type de donnée associé
     */
    private HashMap<String, String> typeMetadata;

    private Boolean dnaAnalysis;

    @FXML
    private Label labelCouleur;

    @FXML
    private Label labelFileTSV;

    @FXML
    private Label labelFiltre;

    @FXML
    private AnchorPane paneInterpreteur;

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
    private Label labelEchelleGrande;

    @FXML
    private Label labelEchelleMini;

    @FXML
    private Label labelEchelleGrandeText;

    @FXML
    private Label labelEchelleMiniText;

    /**
     * Méthode qui se lance au chargement de la fenêtre
     */
    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            setLabels();
            setLabelsBold();

            exportTotal.setOnAction(e -> saveImageTotal());
            exportPatient.setOnAction(e -> saveImagePatient());
            exportDixieme.setOnAction(e -> saveImageMini());

            List<String> listGenes = getListGenes(this.tsv);
            List<Patient> listPatients;
            if (((HashMap<String, Object>) infosAccueil.get("metadata")).isEmpty()) {
                listPatients = getListPatient(this.tsv);
            } else {
                listPatients = getListPatientMetadata(this.tsv, this.metadata, this.typeMetadata, this.infosAccueil);
            }

            performMutationVisible();

            setMutationsPatients(listPatients, this.tsv);

            if (this.dnaAnalysis) {
                mutationCombo.setItems(getMutationsListDNA(listPatients, this.infosAccueil));
            } else {
                mutationCombo.setItems(getMutationsListProtein(listPatients, this.infosAccueil));
                labelEchelleGrande.setText("33 aa");
                labelEchelleMini.setText("33 aa");
            }

            mutationCombo.valueProperty().addListener((obs, oldItem, newItem) -> {
                setSpecificMutationVisible(newItem.toString());
            });
            mutationCombo.getSelectionModel().select(0);

            checkVisible.setSelected(false);

            generateAnalysis(listGenes, listPatients);
            generateAnalysisMini(listGenes, listPatients);
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Labels                                                   /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Méthode qui permet de compléter les labels en haut de l'analyse, pour rappeler les valeurs
     * de couleurs saisie ainsi que le filtre d'analyse utilisé
     */
    private void setLabels() {
        this.labelCouleur.setText("Green value : " + this.minVert + " - " + this.maxVert + "\n" +
                "Orange value : " + this.minOrange + " - " + this.maxOrange + "\n" +
                "Red value : " + this.minRouge + " - " + this.maxRouge);
        this.labelFileTSV.setText(this.fileTSV.getPath());
        this.tsv = TSVUtils.getTSV(this.fileTSV);
        if (this.infosAccueil.get("analysis").equals("complet")) {
            this.labelFiltre.setText("Complet analysis running");
        } else if (this.infosAccueil.get("analysis").equals("gene")) {
            this.labelFiltre.setText("Gene analysis running \nGene : " + this.infosAccueil.get("gene"));
        } else {
            if (this.infosAccueil.containsKey("gene")) {
                this.labelFiltre.setText("Cohort analysis running \nCohort : " + this.infosAccueil.get("cohort") +
                        "\nGene : " + this.infosAccueil.get("gene"));
            } else {
                this.labelFiltre.setText("Cohort analysis running \n Cohort : " + this.infosAccueil.get("cohort"));
            }
        }
    }

    /**
     * Méthode qui permet de mettre les labels en gras
     */
    private void setLabelsBold() {
        this.labelEchelleGrandeText.setStyle("-fx-font-weight: bold;");
        this.labelEchelleMiniText.setStyle("-fx-font-weight: bold;");
        this.labelCouleur.setStyle("-fx-font-weight: bold;");
        this.labelFiltre.setStyle("-fx-font-weight: bold;");
    }



    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Export                                                   /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
        for (Node node : this.vbox.getChildren()
        ) {
            if (node instanceof VBox) {
                BufferedImage bufferedImage = new BufferedImage(550, 400, BufferedImage.TYPE_INT_ARGB);

                WritableImage snapshot = node.snapshot(new SnapshotParameters(), null);
                ((VBox) node).getChildren().add(new ImageView(snapshot));

                BufferedImage image;
                image = fromFXImage(snapshot, bufferedImage);

                try {
                    Graphics2D gd = (Graphics2D) image.getGraphics();
                    gd.translate(((VBox) node).getWidth(), ((VBox) node).getHeight());
                    ImageIO.write(image, "png", new File(file.getPath() + "/" +
                            ((Label) node.lookup("#NomPatientLabel")).getText()));
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

        WritableImage snapshot = paneInterpreteur.snapshot(new SnapshotParameters(), null);
        paneInterpreteur.getChildren().add(new ImageView(snapshot));

        BufferedImage image;
        image = fromFXImage(snapshot, bufferedImage);

        try {
            Graphics2D gd = (Graphics2D) image.getGraphics();
            gd.translate(paneInterpreteur.getWidth(), paneInterpreteur.getHeight());
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Analyse Complète                                         /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Méthode qui va générer tous les gènes avec leurs mutations pour chaque cas d'analyse différents
     *
     * @param listGenes    liste des gènes
     * @param listPatients liste des patients
     */
    private void generateAnalysis(List<String> listGenes, List<Patient> listPatients) {
        if (this.infosAccueil.get("analysis").equals("complet")) {
            for (Patient patient : listPatients) {
                generatePatientAllGene(listGenes, patient);
            }
        }
        if (this.infosAccueil.get("analysis").equals("gene")) {
            for (Patient patient : listPatients) {
                generatePatientForOnePreciseGene(listGenes, patient);
            }
        }
        if (this.infosAccueil.get("analysis").equals("cohort")) {
            if (this.infosAccueil.containsKey("gene")) {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.infosAccueil.get("cohort")))) {
                        generatePatientForOnePreciseGene(listGenes, patient);
                    }
                }
            } else {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.infosAccueil.get("cohort")))) {
                        generatePatientAllGene(listGenes, patient);
                    }
                }
            }
        }
    }

    /**
     * Méthode pour générer le résultat d'analyse pour tous les gènes du patient en cours
     * @param listGenes liste des gènes
     * @param patient patient en cours
     */
    private void generatePatientAllGene(List<String> listGenes, Patient patient) {
        VBox boxPatient = getvBox(patient);
        for (String gene : listGenes) {
            HBox hbox = newGeneBox(gene, patient.getMutationList());
            HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(), false);
            boxPatient.getChildren().addAll(hbox, hbox2);
        }
        this.vbox.getChildren().add(boxPatient);
    }

    /**
     * Méthode pour générer le résultat d'analyse pour un gène spécifique du patient en cours
     * @param listGenes liste des gènes
     * @param patient   patient en cours
     */
    private void generatePatientForOnePreciseGene(List<String> listGenes, Patient patient) {
        VBox boxPatient = getvBox(patient);
        for (String gene : listGenes) {
            if (gene.equals(this.infosAccueil.get("gene"))) {
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
        Label nomPatient = new Label(patient.getIdentifiant());
        nomPatient.setId("NomPatientLabel");
        nomPatient.setTextFill(Color.RED);
        nomPatient.setFont(Font.font("Cambria", 20));
        nomPatient.setAlignment(Pos.BASELINE_LEFT);
        boxPatient.getChildren().addAll(nomPatient);
        return boxPatient;
    }


    /**
     * Méthode qui permet de génerer un gène avec toutes ses mutations.
     * La liste de mutations en paramètre est celle d'un patient
     *
     * @param nom_du_gene  Nom du gène
     * @param mutationList Liste des mutations
     * @return Gène construit avec ses mutations
     */
    private HBox newGeneBox(String nom_du_gene, List<Mutation> mutationList) {
        Rectangle rec;
        if (this.dnaAnalysis) {
            rec = rect(this.sizeGene.get(nom_du_gene), 40);
        } else {
            rec = rect(this.sizeGene.get(nom_du_gene), 40);
        }

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
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux() > this.minVert) {
                if (!dnaAnalysis && !mutation.getMutation_pro().equals("NOT_CODING")) {
                    createMutationBox(gene_pane, mutation);
                } else if (dnaAnalysis) {
                    createMutationBox(gene_pane, mutation);
                }
            }
        }

        hbox.getChildren().add(nom_gene);
        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle et sa position
     * ainsi que le label
     *
     * @param genePane pane du gène
     * @param mutation  mutation à ajouter
     * @return
     */
    private Label createMutationBox(AnchorPane genePane, Mutation mutation) {

        Rectangle rec;

        if (mutation.getTaux() <= this.maxVert && mutation.getTaux() > this.minVert) {
            rec = recVert(39, 5);
        } else if (mutation.getTaux() <= this.maxOrange && mutation.getTaux() > this.minOrange) {
            rec = recOrange(39, 5);
        } else {
            rec = recRouge(39, 5);
        }

        Label mutationLabel;
        if (this.dnaAnalysis) {
            mutationLabel = getLabelMutation(mutation.getMutation_nuc() + "\n" + mutation.getPosition_nuc());
        } else {
            mutationLabel = getLabelMutation(mutation.getMutation_pro() + "\n" + mutation.getPosition_pro());
        }

        mutationLabel.setMinWidth(30);
        mutationLabel.setVisible(false);
        mutationLabel.setId("MutationLabel" + mutationLabel.getText());

        rec.setOnMouseClicked(event -> {
            if (mutationLabel.isVisible()) {
                mutationLabel.setVisible(false);
            } else {
                mutationLabel.setVisible(true);
            }
        });

        genePane.getChildren().add(rec);
        genePane.getChildren().add(mutationLabel);

        AnchorPane.setTopAnchor(mutationLabel, 0.0);
        AnchorPane.setTopAnchor(rec, 41.0);


        if (this.dnaAnalysis) {
            AnchorPane.setLeftAnchor(mutationLabel, Double.valueOf(mutation.getPosition_nuc()) - 10.0);
            AnchorPane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_nuc()));
            rec.setId(mutation.getMutation_nuc());
        } else {
            AnchorPane.setLeftAnchor(mutationLabel, (Double.valueOf(mutation.getPosition_pro()) * 3.0 - 10.0));
            AnchorPane.setLeftAnchor(rec, (Double.valueOf(mutation.getPosition_pro())) * 3.0);
            rec.setId(mutation.getMutation_pro());
        }

        return mutationLabel;

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


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Analyse Mini                                             /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    /**
     * Méthode qui va générer tous les gènes avec leurs mutations pour chaque cas d'analyse différents
     * en version mini
     *
     * @param listGenes    liste des gènes
     * @param listPatients liste des patients
     */
    private void generateAnalysisMini(List<String> listGenes, List<Patient> listPatients) {
        Label titreMini = new Label("Compacted Version");
        titreMini.setFont(Font.font("Cambria", 20));
        this.vboxMini.getChildren().add(titreMini);

        if (this.infosAccueil.get("analysis").equals("complet")) {
            for (Patient patient : listPatients) {
                VBox boxPatient = getvBox(patient);
                for (String gene : listGenes) {
                    HBox hbox2 = newGeneBoxMiniAvecGeneAffiche(gene, patient.getMutationList(), true);
                    boxPatient.getChildren().addAll(hbox2);
                }
                this.vboxMini.getChildren().add(boxPatient);
            }
        }
        if (this.infosAccueil.get("analysis").equals("gene")) {
            for (Patient patient : listPatients) {
                analyseMiniGeneSpecifique(listGenes, patient);
            }
        }
        if (this.infosAccueil.get("analysis").equals("cohort")) {
            if (this.infosAccueil.containsKey("gene")) {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.infosAccueil.get("cohort")))) {
                        analyseMiniGeneSpecifique(listGenes, patient);
                    }
                }
            } else {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().startsWith(String.valueOf(this.infosAccueil.get("cohort")))) {
                        VBox boxPatient = getVBoxMini(patient);
                        for (String gene : listGenes) {
                            HBox hbox2 = newGeneBoxMiniAvecGeneAffiche(gene, patient.getMutationList(), true);
                            boxPatient.getChildren().addAll(hbox2);
                        }
                        this.vboxMini.getChildren().add(boxPatient);
                    }
                }
            }
        }
    }

    /**
     * Génération de la version mini pour un gène spécifique
     * @param listGenes
     * @param patient
     */
    private void analyseMiniGeneSpecifique(List<String> listGenes, Patient patient) {
        HBox boxPatient = getHBoxMini(patient);
        for (String gene : listGenes) {
            if (gene.equals(this.infosAccueil.get("gene"))) {
                HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList(), true);
                boxPatient.getChildren().addAll(hbox2);
            }
        }
        this.vboxMini.getChildren().add(boxPatient);
    }

    /**
     * Création de la box patient avec le nom du patient dans le cas d'un seul gène par patient pour la version
     * mini
     *
     * @param patient Patient en cours de création
     * @return HBox du Patient
     */
    private HBox getHBoxMini(Patient patient) {
        HBox boxPatient = new HBox();
        Label nomPatient = new Label(patient.getIdentifiant());
        nomPatient.setPrefWidth(100.0);
        nomPatient.setTextFill(Color.RED);
        nomPatient.setFont(Font.font("Cambria", 20));
        nomPatient.setAlignment(Pos.CENTER);

        boxPatient.getChildren().addAll(nomPatient);
        return boxPatient;
    }

    /**
     * Création de la box patient avec le nom du patient dans le cas de plusieurs gènes par patient
     * pour la version mini
     *
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
     * Méthode qui permet de génerer un gène avec toutes ses mutations en version 1/10ème.
     * La liste de mutations en paramètre est celle d'un patient
     *
     * @param nomGene
     * @param mutationList
     * @param versionMini
     * @return Gène construit avec ses mutations en version mini
     */
    private HBox newGeneBoxMini(String nomGene, List<Mutation> mutationList, Boolean versionMini) {
        Rectangle rec = rect(this.sizeGene.get(nomGene) / 10, 20);

        HBox hbox = new HBox();
        hbox.setMinHeight(20);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        AnchorPane genePane = new AnchorPane();
        genePane.getChildren().add(rec);

        if (!versionMini) {
            AnchorPane.setLeftAnchor(rec, 150.0);
        }

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nomGene) && mutation.getTaux() > this.minVert) {
                if (!dnaAnalysis && !mutation.getMutation_pro().equals("NOT_CODING")) {
                    createMutationBoxMini(genePane, mutation, versionMini);
                } else if (dnaAnalysis) {
                    createMutationBoxMini(genePane, mutation, versionMini);
                }
            }
        }

        hbox.getChildren().add(genePane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un gène avec toutes ses mutations en version 1/10ème.
     * La liste de mutations en paramètre est celle d'un patient
     * Dans le cas de cette méthode, le nom du gène est affiché
     *
     * @param nomGene
     * @param mutationList
     * @param versionMini
     * @return Gène construit avec ses mutations en version mini
     */
    private HBox newGeneBoxMiniAvecGeneAffiche(String nomGene, List<Mutation> mutationList, Boolean versionMini) {
        Rectangle rec = rect(this.sizeGene.get(nomGene) / 10, 20);

        HBox hbox = new HBox();
        hbox.setMinHeight(20);

        Label labelGene = new Label(nomGene);
        labelGene.setMinWidth(150);
        labelGene.setAlignment(Pos.CENTER);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        AnchorPane genePane = new AnchorPane();
        genePane.getChildren().add(rec);

        if (!versionMini) {
            AnchorPane.setLeftAnchor(rec, 150.0);
        }

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nomGene) && mutation.getTaux() > this.minVert) {
                createMutationBoxMini(genePane, mutation, versionMini);
            }
        }

        hbox.getChildren().add(labelGene);
        hbox.getChildren().add(genePane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle
     *
     * @param gene_pane Pane du gène
     * @param mutation  Mutation en cours de création
     */
    private void createMutationBoxMini(AnchorPane gene_pane, Mutation mutation, Boolean versionMini) {

        Rectangle rec;

        if (mutation.getTaux() <= this.maxVert && mutation.getTaux() > this.minVert) {
            rec = recVert(19, 2);
        } else if (mutation.getTaux() <= this.maxOrange && mutation.getTaux() > this.minOrange) {
            rec = recOrange(19, 2);
        } else {
            rec = recRouge(19, 2);
        }

        gene_pane.getChildren().add(rec);

        AnchorPane.setTopAnchor(rec, 1.0);

        if (versionMini) {
            if (this.dnaAnalysis) {
                AnchorPane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_nuc()) / 10.0);
                rec.setId(mutation.getMutation_nuc());
            } else {
                AnchorPane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_pro()) * 3.0 / 10.0);
                rec.setId(mutation.getMutation_pro());
            }
        } else {
            if (this.dnaAnalysis) {
                AnchorPane.setLeftAnchor(rec, 150.0 + Double.valueOf(mutation.getPosition_nuc()) / 10.0);
                rec.setId(mutation.getMutation_nuc());
            } else {
                AnchorPane.setLeftAnchor(rec, 150.0 + Double.valueOf(mutation.getPosition_pro()) * 3.0 / 10.0);
                rec.setId(mutation.getMutation_pro());
            }
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                           Intéraction avec Mutations                                      /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

    /**
     * Méthode qui parcourt tous les gènes présents dans la VBox et qui setVisible les labels des mutations
     * a true ou false
     *
     * @param visible true or false
     */
    private void makeLabelsVisible(boolean visible) {
        for (Node child : vbox.getChildren()) {
            if (child instanceof VBox) {
                for (Node childBoxPatient : ((VBox) child).getChildren()) {
                    if (childBoxPatient instanceof HBox) {
                        for (Node childHboxGene : ((HBox) childBoxPatient).getChildren()) {
                            if (childHboxGene instanceof AnchorPane) {
                                for (Node groupMutation : ((AnchorPane) childHboxGene).getChildren()) {
                                    if (groupMutation.getId() != null) {
                                        if (groupMutation.getId().contains("MutationLabel")) {
                                            if (mutationCombo.getValue().equals("All mutations") || groupMutation.getId().contains(mutationCombo.getValue().toString())) {
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
    }

    /**
     * Méthode qui permet de rendre invisible toutes les mutations qui ne correspondent pas a la mutation
     * selectionnée dans la combobox
     *
     * @param mutation
     */
    private void setSpecificMutationVisible(String mutation) {
        for (Node child : vbox.getChildren()) {
            childInstanceVBox(mutation, child);
        }
        for (Node child : vboxMini.getChildren()) {
            if (child instanceof HBox) {
                for (Node childBoxPatient : ((HBox) child).getChildren()) {
                    if (childBoxPatient instanceof HBox) {
                        for (Node childHboxGene : ((HBox) childBoxPatient).getChildren()) {
                            if (childHboxGene instanceof AnchorPane) {
                                for (Node groupMutation : ((AnchorPane) childHboxGene).getChildren()) {
                                    if (mutation.equals("All mutations")) {
                                        groupMutation.setVisible(true);
                                    } else if (groupMutation.getId() != null) {
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
            childInstanceVBox(mutation, child);
        }
        checkVisible.setSelected(true);
    }

    /**
     * Méthode idem à la précédente mais cette fois ci se sont les VBox qui sont vérifées
     * @param mutation
     * @param child
     */
    private void childInstanceVBox(String mutation, Node child) {
        if (child instanceof VBox) {
            for (Node childBoxPatient : ((VBox) child).getChildren()) {
                if (childBoxPatient instanceof HBox) {
                    for (Node childHboxGene : ((HBox) childBoxPatient).getChildren()) {
                        if (childHboxGene instanceof AnchorPane) {
                            for (Node groupMutation : ((AnchorPane) childHboxGene).getChildren()) {
                                if (mutation.equals("All mutations")) {
                                    groupMutation.setVisible(true);
                                } else if (groupMutation.getId() != null) {
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Rectangles                                               /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Génération du rectangle correspondant au gène
     *
     * @param width  Largeur
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
     * @param width  Largeur
     * @return Rectangle rouge
     */
    private Rectangle recRouge(int height, int width) {
        Rectangle recRouge = new Rectangle(width, height);
        recRouge.setFill(Color.rgb(255, 0, 0));
        return recRouge;
    }

    /**
     * Génération du rectangle mutation orange
     *
     * @param height Hauteur
     * @param width  Largeur
     * @return Rectangle orange
     */
    private Rectangle recOrange(int height, int width) {
        Rectangle recOrange = new Rectangle(width, height);
        recOrange.setFill(Color.rgb(255, 153, 0));
        return recOrange;
    }

    /**
     * Génération du rectangle mutation vert
     *
     * @param height Hauteur
     * @param width  Largeur
     * @return Rectangle vert
     */
    private Rectangle recVert(int height, int width) {
        Rectangle recVert = new Rectangle(width, height);
        recVert.setFill(Color.GREEN);
        return recVert;
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                                           /////////
    ////////                                  Setters                                                  /////////
    ////////                                                                                           /////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void setSizeGene(HashMap<String, Integer> sizeGene) {
        this.sizeGene = sizeGene;
    }

    public void setMinVert(Double minVert) {
        this.minVert = minVert;
    }

    public void setMaxVert(Double maxVert) {
        this.maxVert = maxVert;
    }

    public void setMinOrange(Double minOrange) {
        this.minOrange = minOrange;
    }

    public void setMaxOrange(Double maxOrange) {
        this.maxOrange = maxOrange;
    }

    public void setMinRouge(Double minRouge) {
        this.minRouge = minRouge;
    }

    public void setMaxRouge(Double maxRouge) {
        this.maxRouge = maxRouge;
    }

    public void setFileTSV(File fileTSV) {
        this.fileTSV = fileTSV;
    }

    public void setInfosAccueil(HashMap<String, Object> infosAccueil) {
        this.infosAccueil = infosAccueil;
    }

    public void setMetadata(ArrayList<List<String>> metadata) {
        this.metadata = metadata;
    }

    public void setTypeMetadata(HashMap<String, String> typeMetadata) {
        this.typeMetadata = typeMetadata;
    }

    public void setDnaAnalysis(Boolean dnaAnalysis) {
        this.dnaAnalysis = dnaAnalysis;
    }
}
