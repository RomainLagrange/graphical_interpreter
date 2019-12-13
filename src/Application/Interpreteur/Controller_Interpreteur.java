package Application.Interpreteur;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import Application.Utils.TableUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static Application.Utils.TableUtils.*;

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

    @FXML
    private Label label_spinner;

    @FXML
    private Label label_file;

    @FXML
    private AnchorPane pane_interpreteur;

    @FXML
    private VBox vbox;

    @FXML private void initialize() {
        Platform.runLater(() -> {
            this.label_spinner.setText("Valeur de vert : " + this.min_vert + " - " + this.max_vert + "\n" +
                    "Valeur de orange : " + this.min_orange + " - " + this.max_orange + "\n" +
                    "Valeur de rouge : " + this.min_rouge + " - " + this.max_rouge);
            this.label_file.setText(this.table_file.getPath());
            this.tsv = TableUtils.getTSV(this.table_file);

            List<String> listGenes = getListGenes(this.tsv);
            List<Patient> listPatients = getListPatient(this.tsv);

            setMutationsPatients(listPatients,this.tsv);


            for (Patient patient:listPatients) {
                VBox boxPatient = new VBox();
                Label nom_patient = new Label(patient.getIdentifiant());
                nom_patient.setTextFill(Color.RED);
                nom_patient.setFont(Font.font("Cambria", 20));
                boxPatient.getChildren().addAll(nom_patient);
                for (String gene:listGenes) {
                    HBox hbox = new_gene_box(gene,patient.getMutationList());
                    boxPatient.getChildren().add(hbox);
                }
                this.vbox.getChildren().add(boxPatient);
            }

        });

    }

    private HBox new_gene_box(String nom_du_gene, List<Mutation> mutationList) {
        Rectangle rec = rect(this.sizeGene.get(nom_du_gene));
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        Region region1 = new Region();
        HBox.setHgrow(region1, Priority.ALWAYS);

        Label nom_gene = new Label(nom_du_gene);
        nom_gene.setMinWidth(150);
        nom_gene.setMinHeight(50);
        nom_gene.setAlignment(Pos.CENTER);

        AnchorPane gene_pane = new AnchorPane();
        gene_pane.getChildren().add(rec);
        gene_pane.setTopAnchor(rec,40.0);

        for (Mutation mutation:mutationList) {
            if (mutation.getGene().equals(nom_du_gene) && mutation.getTaux()>0){
                createMutationBox(hbox, gene_pane, mutation);
            }
        }

        hbox.getChildren().add(nom_gene);
        hbox.getChildren().add(gene_pane);
        hbox.getChildren().add(region1);

        return hbox;
    }

    private void createMutationBox(HBox hbox, AnchorPane gene_pane, Mutation mutation) {
        Rectangle rec_vert = rec_vert();

        Label mutationLabel = getLabelMutation(mutation.getMutation_nuc() +"\n" + mutation.getPosition_nuc());
        mutationLabel.setMinWidth(30);

        gene_pane.getChildren().add(rec_vert);
        gene_pane.getChildren().add(mutationLabel);

        gene_pane.setTopAnchor(mutationLabel,0.0);
        gene_pane.setLeftAnchor(mutationLabel, Double.valueOf(mutation.getPosition_nuc()) - 10.0);

        gene_pane.setTopAnchor(rec_vert,40.0);
        gene_pane.setLeftAnchor(rec_vert, Double.valueOf(mutation.getPosition_nuc()));

        hbox.setAlignment(Pos.CENTER);
        hbox.setMinHeight(120);
    }

    private Rectangle rect(int x) {
        Rectangle rec =new Rectangle(x,40);
        rec.setFill(Color.WHITE);
        rec.setStroke(Color.BLACK);
        return rec;
    }

    private Rectangle rec_rouge() {
        Rectangle rec_rouge =new Rectangle(20,40);
        rec_rouge.setFill(Color.RED);
        return rec_rouge;
    }

    private Rectangle rec_orange() {
        Rectangle rec_rouge =new Rectangle(20,40);
        rec_rouge.setFill(Color.ORANGE);
        return rec_rouge;
    }

    private Rectangle rec_vert() {
        Rectangle rec_vert =new Rectangle(5,40);
        rec_vert.setFill(Color.GREEN);
        return rec_vert;
    }

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
        this.min_vert=min_vert;
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
}
