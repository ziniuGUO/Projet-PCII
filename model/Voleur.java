package model;
public class Voleur extends Thread{
    private int x;
    private int y;
    private int pv;

    public Voleur(int x, int y) {
        this.x = x;
        this.y = y;
        this.pv = 100; // Points de vie par défaut
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
    // Fuit l'écran en se déplaçant vers le bord le plus proche (ex: si x<10, fuir vers x=0, sinon vers x=19)
    public void fuir() {
        synchronized (this) {
            int targetX = (x < 10) ? 0 : 19;
            int targetY = (y < 7) ? 0 : 14;
            avancerVers(targetX, targetY);
        }
    }

    /* Subit une attaque et perd des points de vie */
    public void subirAttaque(int degats) {
        synchronized (this) {
            pv -= degats;
            if (pv < 0) pv = 0;
            System.out.println("Le voleur subit " + degats + " points de dégâts. PV restants : " + pv);
        }
    }

    public boolean aVoleRessource(int rx, int ry) {
        return x == rx && y == ry;
    }

    @Override
    public void run() {
        while (true) {
            // Le voleur doit avancer vers une ressource la nuit, et fuir quand le jour se lève
            // on les fait avancer vers le coin le plus proche
            int targetX = (x < 10) ? 0 : 19;
            int targetY = (y < 7) ? 0 : 14;
            avancerVers(targetX, targetY);
            try {
                Thread.sleep(1000); // Attendre 1 seconde entre chaque mouvement
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

}
