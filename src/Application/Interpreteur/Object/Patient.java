package Application.Interpreteur.Object;

import java.util.List;

/**
 * Classe permettant de stocker un patient avec sa liste de mutations
 */
public class Patient {

    private String identifiant;

    private List<Mutation> mutationList;

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
}
