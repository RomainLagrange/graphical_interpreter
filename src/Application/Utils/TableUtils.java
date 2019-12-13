package Application.Utils;

import Application.Interpreteur.Object.Mutation;
import Application.Interpreteur.Object.Patient;
import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableUtils {

    public static List<List<String>> getTSV(File tsv) {
        TsvParserSettings settings = new TsvParserSettings();
        settings.getFormat().setLineSeparator("\n");

        TsvParser parser = new TsvParser(settings);

        List<String[]> allRows = parser.parseAll(tsv);
        List<List<String>> tsvContent = new ArrayList();

        for (int i = 0; i < allRows.size(); i++){

            tsvContent.add(Arrays.asList(allRows.get(i)));
        }
        return tsvContent;

    }

    public static List<Mutation> getMutations(List<List<String>> tsv){

        List<Mutation> list_mutations = new ArrayList<>();
        List<String[]> list_inter = new ArrayList<>();
        Integer position_nuc = 0;
        Integer position_pro = 0;
        String mutation_nuc = "";
        String mutation_pro = "";
        String gene = "";
        for (List<String> i : tsv) {
            list_inter.add(i.get(0).split("\\|"));
        }
        list_inter.remove(0);
        for (int i = 0; i < list_inter.size(); i++){
            if(list_inter.get(i)[2].equals("CODING")) {
                mutation_pro = list_inter.get(i)[6];
                position_pro = Integer.valueOf(list_inter.get(i)[5]);
            }
            else {
                mutation_pro = "";
                position_pro = 0;
            }
            position_nuc = Integer.valueOf(list_inter.get(i)[3]);
            mutation_nuc = list_inter.get(i)[7];
            gene = list_inter.get(i)[0];

            Mutation mutation = new Mutation(position_nuc,position_pro,mutation_nuc,mutation_pro,gene);
            list_mutations.add(mutation);
        }
        return list_mutations;
    }

    public static List<String> getListGenes(List<List<String>> tsv) {
        List<String[]> list_inter = new ArrayList<>();
        List<String> liste_final = new ArrayList<>();

        for (List<String> i : tsv) {
            list_inter.add(i.get(0).split("\\|"));
        }
        for (int i = 0; i < list_inter.size(); i++){
            liste_final.add(list_inter.get(i)[0]);
        }
        liste_final = liste_final.stream()
                .distinct()
                .collect(Collectors.toList());
        liste_final.remove(0);
        return liste_final;
    }

    public static List<Patient> getListPatient(List<List<String>> tsv){
        List<Patient> patientList = new ArrayList<>();
        List<String> listInter = new ArrayList<>();

        listInter = tsv.get(0);
        for (String i:listInter) {
            patientList.add(new Patient(i));
        }
        return patientList;
    }

    public static List<Patient> setMutationsPatients(List<Patient> patientList, List<List<String>> tsv){
        for (int i = 1; i < tsv.get(1).size(); i++){
            List<Mutation> mutationList = getMutations(tsv);
            for (int j = 1; j < tsv.size(); j++){
                mutationList.get(j-1).setTaux(Double.valueOf(tsv.get(j).get(i)));
            }
            patientList.get(i).setMutationList(mutationList);
        }
        System.out.println("Patient " + patientList.get(4).getIdentifiant() + " Mutation sur gene " +
                patientList.get(4).getMutationList().get(0).getGene() + " Remplacement : " +
                patientList.get(4).getMutationList().get(0).getMutation_nuc() + " Emplacement : " +
                patientList.get(4).getMutationList().get(0).getPosition_nuc() + " Taux : " +
                patientList.get(4).getMutationList().get(0).getTaux() );
        patientList.remove(0);
        return patientList;
    }
}
