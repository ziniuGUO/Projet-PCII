package model;

// Classe générale des bâtiments dans le jeu
public class Batiment {

    // Attributs communs à tous les bâtiments
    protected String nom;
    
    protected Boolean estConstruit;

    protected int pointsVie;

    // Attributs de stockage pour les ressources
    protected int stockageBois;
    protected int stockageFer;
    protected int stockageOr;
    protected int stockageNourriture;

    // Attributs de capacité maximale de stockage pour les ressources
    protected int stockageMaxBois;
    protected int stockageMaxFer;
    protected int stockageMaxOr;
    protected int stockageMaxNourriture;

    // Attributs de coût pour construire le bâtiment
    protected int coutBois;
    protected int coutFer;
    protected int coutOr;
    protected int coutNourriture;

    // Constructeur de la classe Batiment
    public Batiment(String nom, int pointsVie, int coutBois, int coutFer, int coutOr) {
        this.nom = nom;
        this.pointsVie = pointsVie;
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

    // Getters et setters pour les points de vie
    public int getPointsVie() {
        return pointsVie;
    }
    public void setPointsVie(int pointsVie) {
        this.pointsVie = pointsVie;
    }

    // Perdre des points de vie lorsque le bâtiment est attaqué
    public void subirAttaque(int degats) {
        this.pointsVie -= degats;
        if (this.pointsVie <= 0) {
            this.pointsVie = 0; // Le bâtiment ne peut pas avoir de points de vie négatifs
            this.estConstruit = false; // Le bâtiment est détruit
        }
    }
}
