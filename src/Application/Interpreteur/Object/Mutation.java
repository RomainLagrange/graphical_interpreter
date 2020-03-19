package Application.Interpreteur.Object;

/**
 * Classe qu permet de stocker une mutation
 */
public class Mutation {

    /**
     * Position nucléotidique de la mutation
     */
    private Integer positionNuc;

    /**
     * Position protéique de la mutation
     */
    private Integer positionPro;

    /**
     * Type de la mutation en nucléotide
     */
    private String mutationNuc;

    /**
     * Type de la mutation en acide aminé
     */
    private String mutationPro;

    /**
     * Nom du gène sur lequel se trouve la mutation
     */
    private String gene;

    /**
     * Taux de la mutation pour le patient
     */
    private Double taux;

    public Mutation(Integer position_nuc, Integer position_pro, String mutation_nuc, String mutation_pro, String gene) {
        this.positionNuc = position_nuc;
        this.positionPro = position_pro;
        this.mutationNuc = mutation_nuc;
        this.mutationPro = mutation_pro;
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

    public Integer getPositionNuc() {
        return positionNuc;
    }

    public void setPositionNuc(Integer positionNuc) {
        this.positionNuc = positionNuc;
    }

    public Integer getPositionPro() {
        return positionPro;
    }

    public void setPositionPro(Integer positionPro) {
        this.positionPro = positionPro;
    }

    public String getMutationNuc() {
        return mutationNuc;
    }

    public void setMutationNuc(String mutationNuc) {
        this.mutationNuc = mutationNuc;
    }

    public String getMutationPro() {
        return mutationPro;
    }

    public void setMutationPro(String mutationPro) {
        this.mutationPro = mutationPro;
    }
}
