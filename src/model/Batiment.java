package model;

// Classe générale des bâtiments dans le jeu
public class Batiment {

    // Attributs communs à tous les bâtiments
    protected String nom;
    
    // Indique si le bâtiment est construit ou non
    protected Boolean estConstruit;

    // Points de vie du bâtiment, indiquant sa résistance aux attaques
    protected int pointsVie;

    // Position du bâtiment sur la carte (coordonnées x et y)
    protected int x;
    protected int y;

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
/* 
    // Attributs d'attaque, spécifiques à certains bâtiments comme la tour de défense
    public static int puissanceAttaque = 10; // Dégats infligés par la tour de défense à chaque attaque
    public static int porteeAttaque = 3; // Portée d'attaque de la tour de défense
    public static int cadenceAttaque = 1; // Cadence d'attaque de la tour de défense (nombre d'attaques par tour par secondes)
    */

    private Ressource.Type typeStocke;
    private boolean enAttaque = false;
    private boolean protege = false;

    // Constructeur de la classe Batiment
    public Batiment(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        this.nom = nom;
        this.pointsVie = pointsVie;
        this.x = x;
        this.y = y;
        this.coutBois = coutBois;
        this.coutFer = coutFer;
        this.coutOr = coutOr;
        this.estConstruit = false; // Par défaut, le bâtiment n'est pas construit
    }

    // Méthode pour construire le bâtiment
    public void construire() {
        this.estConstruit = true;
    }
    public Boolean isConstruit() {
        return estConstruit;
    }
    // Getters et setters pour le nom du bâtiment
    public String getNom() {
        return nom;
    }

    // Getters pour la position du bâtiment
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    // Getters et setters pour les points de vie
    public int getPointsVie() {
        return pointsVie;
    }
    public void setPointsVie(int pointsVie) {
        this.pointsVie = pointsVie;
    }

    public Ressource.Type getTypeStocke() {
        return typeStocke;
    }
    public void setTypeStocke(Ressource.Type typeStocke) {
        this.typeStocke = typeStocke;
    }
    public boolean isEnAttaque() {
        return enAttaque;
    }

    public void setEnAttaque(boolean enAttaque) {
        this.enAttaque = enAttaque;
    }

    public boolean isProtege() {
        return protege;
    }

    public void setProtege(boolean protege) {
        this.protege = protege;
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
