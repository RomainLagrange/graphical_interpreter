package Application.Interpreteur.Object;

import java.util.HashMap;
import java.util.List;

/**
 * Classe permettant de stocker un patient avec sa liste de mutations
 */
public class Patient {

    private String identifiant;

    private List<Mutation> mutationList;

    private HashMap<String,Object> metadata;

    public Patient(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public List<Mutation> getMutationList() {
        return mutationList;
    }

    public void setMutationList(List<Mutation> mutationList) {
        this.mutationList = mutationList;
    }

    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(HashMap<String, Object> metadata) {
        this.metadata = metadata;
    }
}
