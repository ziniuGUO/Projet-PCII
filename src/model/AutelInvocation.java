package model;

/**
 * Cette classe représente l'Autel d'invocation sur la carte.
 * Pour l'instant, c'est un objet assez simple qui sert de point de repère
 * ou de zone interactive pour faire apparaître nos personnages.
 */
public class AutelInvocation {
    // Les coordonnées x et y sont fixées à la création (final) car l'autel ne bouge pas.
    private final int x;
    private final int y;

    public AutelInvocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Retourne le nom de l'édifice.
     * Pratique pour l'affichage des infos quand on passe la souris dessus.
     */
    public String getNom() {
        return "Autel d'invocation";
    }

    /* Accesseurs classiques pour la position */

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}