package model;

/**
 * C'est la classe maîtresse pour nos unités.
 * Elle ne se contente pas de stocker des infos, elle gère aussi toute la logique
 * de déplacement "case par case" et le compte à rebours des actions (le timer).
 */
public class Personnage {

    private final String nom;
    private final int rareteEtoiles; // Pour le côté collection / RPG

    // Position actuelle sur la grille
    private int x;
    private int y;

    // Là où le personnage doit se rendre.
    // Si cibleX/Y != x/y, alors le personnage est considéré "en mouvement".
    private int cibleX;
    private int cibleY;

    private boolean deploye; // Est-ce qu'il est sur la carte ou dans la réserve ?
    private ActionType actionCourante = null;

    // --- Gestion du temps de travail (Action en cours) ---
    private boolean enExecution = false;      // Est-ce qu'il est en train de bosser ?
    private boolean pretARecuperer = false;  // L'action est finie, on attend que le joueur valide
    private long debutExecution = 0L;        // Timestamp pour calculer le temps restant

    // On a fixé la durée des tâches à 5 secondes pour le moment.
    public static final long DUREE_ACTION = 5000;

    public Personnage(String nom, int rareteEtoiles, int x, int y) {
        this.nom = nom;
        this.rareteEtoiles = rareteEtoiles;
        this.x = x;
        this.y = y;
        this.cibleX = x;
        this.cibleY = y;
        this.deploye = false;
    }

    /* --- Accesseurs classiques --- */

    public String getNom() { return nom; }
    public int getRareteEtoiles() { return rareteEtoiles; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getCibleX() { return cibleX; }
    public int getCibleY() { return cibleY; }

    /**
     * Force la position sans passer par un déplacement fluide.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Définit une nouvelle destination.
     * Le personnage s'y dirigera lors des prochains appels à avancerVersCible().
     */
    public void setCible(int x, int y) {
        this.cibleX = x;
        this.cibleY = y;
    }

    public boolean isDeploye() { return deploye; }
    public void setDeploye(boolean deploye) { this.deploye = deploye; }

    public ActionType getActionCourante() { return actionCourante; }
    public void setActionCourante(ActionType actionCourante) { this.actionCourante = actionCourante; }

    /**
     * Vérifie si le personnage a atteint sa destination.
     */
    public boolean estArrive() {
        return x == cibleX && y == cibleY;
    }

    /**
     * Logique de déplacement simplifiée : on se rapproche de la cible
     * d'une case à la fois, en horizontal et en vertical.
     */
    public void avancerVersCible() {
        if (x < cibleX) x++;
        else if (x > cibleX) x--;

        if (y < cibleY) y++;
        else if (y > cibleY) y--;
    }

    /* --- Gestion des états d'action --- */

    public boolean isEnExecution() { return enExecution; }
    public boolean isPretARecuperer() { return pretARecuperer; }

    public void setPretARecuperer(boolean pretARecuperer) {
        this.pretARecuperer = pretARecuperer;
    }

    /**
     * Lance le chrono pour l'action choisie (bûcheronnage, etc.).
     */
    public void commencerExecution() {
        this.enExecution = true;
        this.pretARecuperer = false;
        this.debutExecution = System.currentTimeMillis();
    }

    /**
     * Si on déplace le perso ou qu'on annule, on remet les compteurs à zéro.
     */
    public void interrompreAction() {
        this.enExecution = false;
        this.pretARecuperer = false;
        this.debutExecution = 0L;
    }

    /**
     * À appeler dans la boucle de jeu.
     * Vérifie si le temps de travail est écoulé.
     * @return true si l'action vient de se terminer.
     */
    public boolean mettreAJourExecution() {
        if (!enExecution) return false;

        // On checke si on a dépassé les 5 secondes
        if (System.currentTimeMillis() - debutExecution >= DUREE_ACTION) {
            enExecution = false;
            pretARecuperer = true;
            return true;
        }
        return false;
    }

    /**
     * Un perso est dispo s'il n'est ni sur la carte, ni occupé à bosser.
     */
    public boolean estDisponible() {
        return !deploye && !enExecution && !pretARecuperer && actionCourante == null;
    }

    /**
     * Remet tout à plat : on retire le personnage de la carte et on stoppe ses tâches.
     */
    public void rappeler() {
        this.deploye = false;
        this.actionCourante = null;
        this.enExecution = false;
        this.pretARecuperer = false;
        this.debutExecution = 0L;
    }

    @Override
    public String toString() {
        return nom + " - " + rareteEtoiles + "★";
    }
}