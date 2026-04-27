package model;
public class Voleur extends Thread{
    private int x;
    private int y;
    private int pv = 100;
    private final int vitesse = 1;
    private final int atk = 10;
    private Batiment cible;
    private boolean actif = true;
    private boolean voleReussi = false;
    // Nouvel état : le voleur peut rester sur la case pour piller pendant plusieurs ticks
    private boolean enPillage = false;
    private int ticksPillage = 0;
    private static final int TEMPS_PILLAGE_TICKS = 3; // nombre d'itérations de mise à jour
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
    public int getVitesse() {
        return vitesse;
    }

    public int getAtk() {
        return atk;
    }

    public int getPv() {
        return pv;
    }

    public Batiment getCible() {
        return cible;
    }

    public void setCible(Batiment cible) {
        this.cible = cible;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }

    public boolean isVoleReussi() {
        return voleReussi;
    }

    public void setVoleReussi(boolean voleReussi) {
        this.voleReussi = voleReussi;
    }

    public boolean estArrive() {
        return cible != null && x == cible.getX() && y == cible.getY();
    }
    public void avancerVersCible() {
        if (!actif || cible == null || estArrive()) return;

        int pas = vitesse;
        while (pas > 0 && !estArrive()) {
            if (x < cible.getX()) x++;
            else if (x > cible.getX()) x--;
            else if (y < cible.getY()) y++;
            else if (y > cible.getY()) y--;
            pas--;
        }
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
        pv -= degats;
        if (pv < 0) pv = 0;
        if (pv == 0) {
            actif = false;
        }
    }

    public boolean aVoleRessource(int rx, int ry) {
        return x == rx && y == ry;
    }

   /* @Override
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
*/
}
