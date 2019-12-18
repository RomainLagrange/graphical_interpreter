package Application.Interpreteur;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import Application.Utils.TableUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
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
import java.util.HashMap;
import java.util.List;

import static Application.Utils.TableUtils.*;
import static javafx.embed.swing.SwingFXUtils.fromFXImage;

public class Controller_Interpreteur {

    private Double min_vert;

    private Double max_vert;

    private Double min_orange;

    private Double max_orange;

    private Double min_rouge;

    private Double max_rouge;

    private File table_file;

    private List<List<String>> tsv;

    private HashMap<String, Integer> sizeGene;

    private HashMap<String, String> filtre;

    @FXML
    private Label label_spinner;

    @FXML
    private Label label_file;

    @FXML
    private Label label_filtre;

    @FXML
    private AnchorPane pane_interpreteur;

    @FXML
    private VBox vbox;


    @FXML
    private void initialize() {
        Platform.runLater(() -> {
            setLabels();

            List<String> listGenes = getListGenes(this.tsv);
            List<Patient> listPatients = getListPatient(this.tsv);

            setMutationsPatients(listPatients, this.tsv);

            generateAnalysis(listGenes, listPatients);
        });
    }

    /**
     * Méthode qui permet d'exporter le contenu de l'anchorpane en fichier png
     */
    @FXML
    private void saveImage() {
        BufferedImage bufferedImage = new BufferedImage(1080, 720, BufferedImage.TYPE_INT_ARGB);

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
        } catch (IOException ex) {
        }
        ;
    }


    /**
     * Méthode qui va générer tous les gènes avec leurs mutations pour chaque cas d'analyse différents
     *
     * @param listGenes
     * @param listPatients
     */
    private void generateAnalysis(List<String> listGenes, List<Patient> listPatients) {
        if (this.filtre.get("analysis").equals("complet")) {
            for (Patient patient : listPatients) {
                VBox boxPatient = getvBox(patient);
                for (String gene : listGenes) {
                    HBox hbox = newGeneBox(gene, patient.getMutationList());
                    HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList());
                    boxPatient.getChildren().addAll(hbox, hbox2);
                }
                this.vbox.getChildren().add(boxPatient);
            }
        }
        if (this.filtre.get("analysis").equals("gene")) {
            for (Patient patient : listPatients) {
                generatePatientForOnePreciseGene(listGenes, patient);
            }
        }
        if (this.filtre.get("analysis").equals("cohort")) {
            if (this.filtre.containsKey("gene")) {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().contains(this.filtre.get("cohort"))) {
                        generatePatientForOnePreciseGene(listGenes, patient);
                    }
                }
            } else {
                for (Patient patient : listPatients) {
                    if (patient.getIdentifiant().contains(this.filtre.get("cohort"))) {
                        VBox boxPatient = getvBox(patient);
                        for (String gene : listGenes) {
                            HBox hbox = newGeneBox(gene, patient.getMutationList());
                            HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList());
                            boxPatient.getChildren().addAll(hbox, hbox2);
                        }
                        this.vbox.getChildren().add(boxPatient);
                    }
                }
            }
        }
    }

    /**
     * Méthode pour générer la liste des analyses génomique dans le cas où l'analyse se concentre sur
     * un seul gène
     *
     * @param listGenes
     * @param patient
     */
    private void generatePatientForOnePreciseGene(List<String> listGenes, Patient patient) {
        VBox boxPatient = getvBox(patient);
        for (String gene : listGenes) {
            if (gene.equals(this.filtre.get("gene"))) {
                HBox hbox = newGeneBox(gene, patient.getMutationList());
                HBox hbox2 = newGeneBoxMini(gene, patient.getMutationList());
                boxPatient.getChildren().addAll(hbox, hbox2);
            }
        }
        this.vbox.getChildren().add(boxPatient);
    }

    /**
     * Création de la box patient avec le nom du patient
     * La VBox se remplira avec les différents gènes par la suite
     *
     * @param patient
     * @return
     */
    private VBox getvBox(Patient patient) {
        VBox boxPatient = new VBox(10);
        Label nom_patient = new Label(patient.getIdentifiant());
        nom_patient.setTextFill(Color.RED);
        nom_patient.setFont(Font.font("Cambria", 20));
        nom_patient.setAlignment(Pos.BASELINE_LEFT);
        boxPatient.getChildren().addAll(nom_patient);
        return boxPatient;
    }

    /**
     * Méthode qui permet de compléter les labels en haut de l'analyse, pour rappeler les valeurs
     * de couleurs saisie ainsi que le filtre d'analyse utilisé
     */
    private void setLabels() {
        this.label_spinner.setText("Valeur de vert : " + this.min_vert + " - " + this.max_vert + "\n" +
                "Valeur de orange : " + this.min_orange + " - " + this.max_orange + "\n" +
                "Valeur de rouge : " + this.min_rouge + " - " + this.max_rouge);
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
     * @param nom_du_gene
     * @param mutationList
     * @return
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
        gene_pane.setTopAnchor(rec, 40.0);

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

    private HBox newGeneBoxMini(String nom_du_gene, List<Mutation> mutationList) {
        Rectangle rec = rect(this.sizeGene.get(nom_du_gene) / 10, 20);

        HBox hbox = new HBox();
        hbox.setMinHeight(20);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        AnchorPane gene_pane = new AnchorPane();
        gene_pane.getChildren().add(rec);
        gene_pane.setLeftAnchor(rec, 150.0);

        for (Mutation mutation : mutationList) {
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux() > this.min_vert) {
                createMutationBoxMini(gene_pane, mutation);
            }
        }

        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle
     *
     * @param gene_pane
     * @param mutation
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

        gene_pane.getChildren().add(rec);
        gene_pane.getChildren().add(mutationLabel);

        gene_pane.setTopAnchor(mutationLabel, 0.0);
        gene_pane.setLeftAnchor(mutationLabel, Double.valueOf(mutation.getPosition_nuc()) - 10.0);

        gene_pane.setTopAnchor(rec, 41.0);
        gene_pane.setLeftAnchor(rec, Double.valueOf(mutation.getPosition_nuc()));

    }

    /**
     * Méthode qui permet de génerer un rectangle mutation qui sera placé sur le gène
     * La mutation passé en paramètre permet de déterminer le choix de la couleur du rectangle
     *
     * @param gene_pane
     * @param mutation
     */
    private void createMutationBoxMini(AnchorPane gene_pane, Mutation mutation) {

        Rectangle rec;

        if (mutation.getTaux() <= this.max_vert && mutation.getTaux() > this.min_vert) {
            rec = rec_vert(19, 2);
        } else if (mutation.getTaux() <= this.max_orange && mutation.getTaux() > this.min_orange) {
            rec = rec_orange(19, 2);
        } else {
            rec = rec_rouge(19, 2);
        }

        gene_pane.getChildren().add(rec);


        gene_pane.setTopAnchor(rec, 1.0);
        gene_pane.setLeftAnchor(rec, 150.0 + Double.valueOf(mutation.getPosition_nuc()) / 10);

    }

    /**
     * Génération du rectangle correspondant au gène
     *
     * @param width
     * @param height
     * @return
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
     * @param height
     * @param width
     * @return
     */
    private Rectangle rec_rouge(int height, int width) {
        Rectangle rec_rouge = new Rectangle(width, height);
        rec_rouge.setFill(Color.rgb(255, 0, 0));
        return rec_rouge;
    }

    /**
     * Génération du rectangle mutation orange
     *
     * @param height
     * @param width
     * @return
     */
    private Rectangle rec_orange(int height, int width) {
        Rectangle rec_rouge = new Rectangle(width, height);
        rec_rouge.setFill(Color.rgb(255, 153, 0));
        return rec_rouge;
    }

    /**
     * Génération du rectangle mutation vert
     *
     * @param height
     * @param width
     * @return
     */
    private Rectangle rec_vert(int height, int width) {
        Rectangle rec_vert = new Rectangle(width, height);
        rec_vert.setFill(Color.GREEN);
        return rec_vert;
    }

    /**
     * Génération du label qui va accueillir nom + position de la mutation
     *
     * @param text
     * @return
     */
    private Label getLabelMutation(String text) {
        Label mutation = new Label(text);
        return mutation;
    }

    public HashMap<String, Integer> getSizeGene() {
        return sizeGene;
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

    public void setFiltre(HashMap<String, String> filtre) {
        this.filtre = filtre;
    }
}
