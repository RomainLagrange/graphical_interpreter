package Application.Interpreteur.Object;

/**
 * Classe qu permet de stocker une mutation
 */
public class Mutation {

    /**
     * Position nucléotidique de la mutation
     */
    private Integer position_nuc;

    /**
     * Position protéique de la mutation
     */
    private Integer position_pro;

    /**
     * Type de la mutation en nucléotide
     */
    private String mutation_nuc;

    /**
     * Type de la mutation en acide aminé
     */
    private String mutation_pro;

    /**
     * Nom du gène sur lequel se trouve la mutation
     */
    private String gene;

    /**
     * Taux de la mutation pour le patient
     */
    private Double taux;

    public Mutation(Integer position_nuc, Integer position_pro, String mutation_nuc, String mutation_pro, String gene) {
        this.position_nuc = position_nuc;
        this.position_pro = position_pro;
        this.mutation_nuc = mutation_nuc;
        this.mutation_pro = mutation_pro;
        this.gene = gene;
        this.taux = 0.0;
    }

    public Double getTaux() {
        return taux;
    }

    public void setTaux(Double taux) {
        this.taux = taux;
    }

    public String getGene() {
        return gene;
    }

    public void setGene(String gene) {
        this.gene = gene;
    }

    public Integer getPosition_nuc() {
        return position_nuc;
    }

    public void setPosition_nuc(Integer position_nuc) {
        this.position_nuc = position_nuc;
    }

    public Integer getPosition_pro() {
        return position_pro;
    }

    public void setPosition_pro(Integer position_pro) {
        this.position_pro = position_pro;
    }

    public String getMutation_nuc() {
        return mutation_nuc;
    }

    public void setMutation_nuc(String mutation_nuc) {
        this.mutation_nuc = mutation_nuc;
    }

    public String getMutation_pro() {
        return mutation_pro;
    }

    public void setMutation_pro(String mutation_pro) {
        this.mutation_pro = mutation_pro;
    }
}
