package model;

// Classe générale des bâtiments dans le jeu
public class Batiment {

    // Attributs communs à tous les bâtiments
    protected String nom;
    
    protected Boolean estConstruit;

    protected int coutBois;
    protected int coutPierre;
    protected int coutOr;

    // Constructeur de la classe Batiment
    public Batiment(String nom, int coutBois, int coutPierre, int coutOr) {
        this.nom = nom;
        this.coutBois = coutBois;
        this.coutPierre = coutPierre;
        this.coutOr = coutOr;
        this.estConstruit = false; // Par défaut, le bâtiment n'est pas construit
    }

}
