package Application.Utils;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Classe qui contient différentes méthodes utiles pour la gestion des fichiers TSV
 */
public class TSVUtils {

    /**
     * Méthode qui permet de récupérer le contenu d'un fichier TSV
     *
     * @param tsv le File tsv
     * @return contenu sous forme List<List<String>>
     */
    public static ArrayList<List<String>> getTSV(File tsv) {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");

        TsvParser parser = new TsvParser(settings);

        List<String[]> allRows = parser.parseAll(tsv);
        ArrayList<List<String>> tsvContent = new ArrayList<>();

        for (String[] allRow : allRows) {
            tsvContent.add(Arrays.asList(allRow));
        }
        return tsvContent;

    }

    /**
     * Méthode qui permet de récupérer la liste des mutations du fichier TSV
     *
     * @param tsv File TSV
     * @return List d'objets Mutations
     */
    private static List<Mutation> getMutations(List<List<String>> tsv) {

        List<Mutation> mutationList = new ArrayList<>();
        List<String[]> listInter = new ArrayList<>();
        for (List<String> i : tsv) {
            listInter.add(i.get(0).split("\\|"));
        }
        listInter.remove(0);

        int positionNuc;
        int positionPro;
        String mutationNuc;
        String mutationPro;
        String gene;
        for (String[] mutationString : listInter) {
            if (mutationString[2].equals("CODING")) {
                mutationPro = mutationString[6];
                positionPro = Integer.parseInt(mutationString[5]);
            } else {
                mutationPro = "NOT_CODING";
                positionPro = 0;
            }
            positionNuc = Integer.parseInt(mutationString[3]);
            mutationNuc = mutationString[7];
            gene = mutationString[0];

            Mutation mutation = new Mutation(positionNuc, positionPro, mutationNuc, mutationPro, gene);
            mutationList.add(mutation);
        }
        return mutationList;
    }

    /**
     * Méthode qui permet de récupérer la liste des gènes du fichier TSV
     *
     * @param tsv File TSV
     * @return List des gènes
     */
    public static List<String> getListGenes(List<List<String>> tsv) {
        List<String[]> listInter = new ArrayList<>();
        List<String> listeFinal = new ArrayList<>();

        for (List<String> i : tsv) {
            listInter.add(i.get(0).split("\\|"));
        }
        for (String[] strings : listInter) {
            listeFinal.add(strings[0]);
        }
        listeFinal = listeFinal.stream()
                .distinct()
                .collect(Collectors.toList());
        listeFinal.remove(0);
        return listeFinal;
    }

    /**
     * Méthode qui permet de récupérer la liste des Patients à partir du fichier TSV
     *
     * @param tsv File TSV
     * @return La liste des patients
     */
    public static List<Patient> getListPatient(List<List<String>> tsv) {
        List<Patient> patientList = new ArrayList<>();
        List<String> listInter;

        listInter = tsv.get(0);
        for (String i : listInter) {
            patientList.add(new Patient(i));
        }
        patientList.remove(0);
        return patientList;
    }

    /**
     * Méthode qui permet de récupérer la liste des Patients à partir du fichier TSV ainsi que les
     * metadata associées à ce patient si un fichier a été spécifié
     *
     * @param tsv      File TSV
     * @param metadata File metadata
     * @return La liste des patients
     */
    public static List<Patient> getListPatientMetadata(List<List<String>> tsv, ArrayList<List<String>> metadata, HashMap<String, String> typeMetadata, HashMap<String, Object> infosAccueil) {
        List<Patient> patientList = new ArrayList<>();
        List<String> listInter;

        listInter = tsv.get(0);
        for (String i : listInter) {
            Patient patient = new Patient(i);
            HashMap<String, Object> metadataPatient = new HashMap<>();
            for (List<String> lineMetadata : metadata) {
                if (!lineMetadata.get(0).equals("SAMPLEID")) {
                    if (lineMetadata.get(0).equals(patient.getIdentifiant())) {
                        for (int j = 1; j < lineMetadata.size(); j++) {
                            for (String nameMeta : typeMetadata.keySet()) {
                                if (metadata.get(0).get(j).equals(nameMeta)) {
                                    if (typeMetadata.get(metadata.get(0).get(j)).equals("double")) {
                                        metadataPatient.put(metadata.get(0).get(j), Double.valueOf(lineMetadata.get(j)));
                                    }
                                    if (typeMetadata.get(metadata.get(0).get(j)).equals("integer") || typeMetadata.get(metadata.get(0).get(j)).equals("date")) {
                                        metadataPatient.put(metadata.get(0).get(j), Integer.valueOf(lineMetadata.get(j)));
                                    } else if (typeMetadata.get(metadata.get(0).get(j)).equals("word")) {
                                        metadataPatient.put(metadata.get(0).get(j), String.valueOf(lineMetadata.get(j)));
                                    }
                                }
                            }
                        }
                    }
                }
            }
            patient.setMetadata(metadataPatient);
            if (checkMetadata(patient, infosAccueil)) {
                patientList.add(patient);
            }
        }
        patientList.remove(0);
        return patientList;
    }

    /**
     * Méthode qui permet de checker le contenu metadata de patient et celui des infos accueil pour savoir s'il faut
     * générer ou non l'analyse du patient et l'ajouter à la liste des patients
     *
     * @param patient patient
     * @param infosAccueil hashmap contenant les infos de la fenêtre d'accueil
     * @return true ou false selon que le patient doit etre ajouté ou non
     */
    private static boolean checkMetadata(Patient patient, HashMap<String, Object> infosAccueil) {
        for (String metadataPatient : patient.getMetadata().keySet()) {
            if (((HashMap<String, Object>) infosAccueil.get("metadata")).containsKey(metadataPatient)) {
                if (patient.getMetadata().get(metadataPatient) instanceof String) {
                    if (!((HashMap<String, Object>) infosAccueil.get("metadata")).get(metadataPatient).equals(patient.getMetadata().get(metadataPatient))) {
                        return false;
                    }
                } else if (patient.getMetadata().get(metadataPatient) instanceof Double) {
                    if ((Double) patient.getMetadata().get(metadataPatient) > ((Double) ((HashMap<String, Object>) ((HashMap<String, Object>) infosAccueil.get("metadata")).get(metadataPatient)).get("max")) ||
                            (Double) patient.getMetadata().get(metadataPatient) < ((Double) ((HashMap<String, Object>) ((HashMap<String, Object>) infosAccueil.get("metadata")).get(metadataPatient)).get("min"))) {
                        return false;
                    }
                } else if (patient.getMetadata().get(metadataPatient) instanceof Integer) {
                    if ((Integer) patient.getMetadata().get(metadataPatient) > ((Integer) ((HashMap<String, Object>) ((HashMap<String, Object>) infosAccueil.get("metadata")).get(metadataPatient)).get("max")) ||
                            (Integer) patient.getMetadata().get(metadataPatient) < ((Integer) ((HashMap<String, Object>) ((HashMap<String, Object>) infosAccueil.get("metadata")).get(metadataPatient)).get("min"))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Méthode qui permet d'attribuer à chaque patient la liste des différentes Mutations
     *
     * @param patientList Liste des patients
     * @param tsv         File TSV
     */
    public static void setMutationsPatients(List<Patient> patientList, List<List<String>> tsv) {
        int colonne = 0;
        for (Patient patient : patientList) {
            for (int i = 1; i < tsv.get(0).size(); i++) {
                if (tsv.get(0).get(i).equals(patient.getIdentifiant())) {
                    colonne = i;
                    break;
                }
            }

            List<Mutation> mutationList = getMutations(tsv);
            for (int ligne = 1; ligne < tsv.size(); ligne++) {
                mutationList.get(ligne - 1).setTaux(Double.valueOf(tsv.get(ligne).get(colonne)));
            }
            patient.setMutationList(mutationList);
        }
    }

    /**
     * Méthode qui permet de récuperer la liste de toutes les mutations ADN existantes pour les patients
     *
     * @param patientList liste des patients
     * @param filtre      filtre de recherche dans le cas d'une analyse sur un gène particulier
     * @return liste des mutations
     */
    public static ObservableList<String> getMutationsListDNA(List<Patient> patientList, HashMap<String, Object> filtre) {
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("All mutations");
        for (Patient patient : patientList) {
            if (!(patient.getMutationList() == null)) {
                for (Mutation mutationPatient : patient.getMutationList()) {
                    if (filtre.get("analysis").equals("gene")) {
                        if (!options.contains(mutationPatient.getMutationNuc()) && mutationPatient.getGene().equals(filtre.get("gene")) && mutationPatient.getTaux() > 0) {
                            options.add(mutationPatient.getMutationNuc());
                        }
                    } else {
                        if (!options.contains(mutationPatient.getMutationNuc()) && mutationPatient.getTaux() > 0) {
                            options.add(mutationPatient.getMutationNuc());
                        }
                    }
                }
            }
        }

        return options;
    }

    /**
     * Méthode qui permet de récuperer la liste de toutes les mutations protéines existantes pour les patients
     *
     * @param patientList liste des patients
     * @param filtre      filtre de recherche dans le cas d'une analyse sur un gène particulier
     * @return liste des mutations
     */
    public static ObservableList<String> getMutationsListProtein(List<Patient> patientList, HashMap<String, Object> filtre) {
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("All mutations");
        for (Patient patient : patientList) {
            if (!(patient.getMutationList() == null)) {
                for (Mutation mutationPatient : patient.getMutationList()) {
                    if (filtre.get("analysis").equals("gene")) {
                        if (!options.contains(mutationPatient.getMutationPro()) && !mutationPatient.getMutationPro().equals("NOT_CODING") && mutationPatient.getGene().equals(filtre.get("gene")) && mutationPatient.getTaux() > 0) {
                            options.add(mutationPatient.getMutationPro());
                        }
                    } else {
                        if (!options.contains(mutationPatient.getMutationPro()) && !mutationPatient.getMutationPro().equals("NOT_CODING") && mutationPatient.getTaux() > 0) {
                            options.add(mutationPatient.getMutationPro());
                        }
                    }
                }
            }
        }
        return options;
    }

}
