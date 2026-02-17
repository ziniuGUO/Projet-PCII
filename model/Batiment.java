package model;

// Classe générale des bâtiments dans le jeu
public class Batiment {

    // Attributs communs à tous les bâtiments
    protected String nom;
    
    protected Boolean estConstruit;

    protected int stockageBois;
    protected int stockageFer;
    protected int stockageOr;
    protected int stockageNourriture;

    protected int stockageMaxBois;
    protected int stockageMaxFer;
    protected int stockageMaxOr;
    protected int stockageMaxNourriture;

    protected int coutBois;
    protected int coutFer;
    protected int coutOr;
    protected int coutNourriture;

    // Constructeur de la classe Batiment
    public Batiment(String nom, int coutBois, int coutFer, int coutOr) {
        this.nom = nom;
        this.coutBois = coutBois;
        this.coutFer = coutFer;
        this.coutOr = coutOr;
        this.estConstruit = false; // Par défaut, le bâtiment n'est pas construit
    }

    // Méthode pour construire le bâtiment
    public void construire() {
        this.estConstruit = true;
    }

    // Getters et setters pour les attributs
    public String getNom() {
        return nom;
    }


}
