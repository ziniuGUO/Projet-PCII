package model;

/**
 * Personnage minimal (villageois) : position + infos + action courante.
 */
public class Personnage {

    private final String nom;
    private final int rareteEtoiles; // 1..5
    private int x;
    private int y;
    private ActionType actionCourante = ActionType.DEFENDRE;

    public Personnage(String nom, int rareteEtoiles, int x, int y) {
        this.nom = nom;
        this.rareteEtoiles = rareteEtoiles;
        this.x = x;
        this.y = y;
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
}