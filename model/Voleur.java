package model;
public class Voleur {
    private int x;
    private int y;

    public Voleur(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int nx, int ny) {
        this.x = nx;
        this.y = ny;
    }

    // Avance d'un seul pas vers (tx, ty) : +/-1 en x et y par appel
    public void avancerVers(int tx, int ty) {
        synchronized (this) {
            if (x == tx && y == ty) {
                return;
            }
            if (x < tx) x++; else if (x > tx) x--;
            if (y < ty) y++; else if (y > ty) y--;
            System.out.println("Coordonnées du voleur : (" + x + ", " + y + ")");
        }
    }

    // Fuit d'un seul pas (appelé par la boucle principale à chaque seconde)
    public void fuir() {
        synchronized (this) {
            x -= 1;
            y -= 1;
        }
    }

    public boolean aVoleRessource(int rx, int ry) {
        return x == rx && y == ry;
    }
}
