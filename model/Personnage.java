package model;

/**
 * Personnage minimal (villageois) : position + infos + action courante.
 */
public class Personnage {

    private final String nom;
    private final int rareteEtoiles; // 1..5
    private int x;
    private int y;
    private int cibleX;
    private int cibleY;
    private boolean deploye;
    private ActionType actionCourante = null;

    private boolean enExecution = false;
    private boolean pretARecuperer = false;
    private long debutExecution = 0L;

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

    public String getNom() {
        return nom;
    }

    public int getRareteEtoiles() {
        return rareteEtoiles;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public int getCibleX() {
        return cibleX;
    }

    public int getCibleY() {
        return cibleY;
    }
    public void setCible(int x, int y) {
        this.cibleX = x;
        this.cibleY = y;
    }

    public boolean isDeploye() {
        return deploye;
    }

    public void setDeploye(boolean deploye) {
        this.deploye = deploye;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ActionType getActionCourante() {
        return actionCourante;
    }

    public void setActionCourante(ActionType actionCourante) {
        this.actionCourante = actionCourante;
    }
    public boolean estArrive() {
        return x == cibleX && y == cibleY;
    }

    public void avancerVersCible() {
        if (x < cibleX) x++;
        else if (x > cibleX) x--;

        if (y < cibleY) y++;
        else if (y > cibleY) y--;
    }
    public boolean isEnExecution() {
        return enExecution;
    }

    public boolean isPretARecuperer() {
        return pretARecuperer;
    }

    public void setPretARecuperer(boolean pretARecuperer) {
        this.pretARecuperer = pretARecuperer;
    }

    public void commencerExecution() {
        this.enExecution = true;
        this.pretARecuperer = false;
        this.debutExecution = System.currentTimeMillis();
    }

    public void interrompreAction() {
        this.enExecution = false;
        this.pretARecuperer = false;
        this.debutExecution = 0L;
    }

    public boolean mettreAJourExecution() {
        if (!enExecution) return false;

        if (System.currentTimeMillis() - debutExecution >= DUREE_ACTION) {
            enExecution = false;
            pretARecuperer = true;
            return true;
        }
        return false;
    }

    public boolean estDisponible() {
        return !deploye && !enExecution && !pretARecuperer && actionCourante == null;
    }

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