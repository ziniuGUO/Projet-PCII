package test.testmini;

import model.Personnage;

import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe sert de "contenant" pour notre grille de jeu.
 * Elle gère les dimensions du terrain et garde une trace de qui se trouve où.
 */
public class MiniMap {
    private final int width;  // Largeur de la carte en nombre de cases
    private final int height; // Hauteur de la carte en nombre de cases

    // La liste de tous les héros ou villageois présents sur la carte
    private final List<Personnage> personnages = new ArrayList<>();

    public MiniMap(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Petite vérification de sécurité pour éviter de cliquer en dehors de la grille
     * ou de déplacer un personnage dans le vide.
     */
    public boolean isValid(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public List<Personnage> getPersonnages() { return personnages; }

    /**
     * Pour faire apparaître un nouveau personnage sur la carte.
     */
    public void addPersonnage(Personnage p) { personnages.add(p); }

    /**
     * On scanne la liste des personnages pour voir si l'un d'eux
     * occupe la case (x, y). Pratique pour savoir sur qui on vient de cliquer.
     */
    public Personnage getPersonnageAt(int x, int y) {
        for (Personnage p : personnages) {
            // Si les coordonnées correspondent, on a trouvé notre cible
            if (p.getX() == x && p.getY() == y) return p;
        }
        // Case vide
        return null;
    }
}