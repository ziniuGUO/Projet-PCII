package model;

public class Voleur extends Thread {

    private volatile int x;
    private volatile int y;
    private int pv = 60;
    private final int vitesse = 1;
    private final int atk = 20;
    private Batiment cible;
    private volatile boolean actif = true;
    private boolean voleReussi = false;

    // Durée du pillage sur place (en ms)
    private static final long DUREE_PILLAGE_MS = 3500L;
    // Délai entre chaque pas de déplacement (ms)
    private static final long DELAI_DEPLACEMENT_MS = 800L;

    // Callback appelé quand le pillage est terminé (fourni par Map)
    private Runnable onPillageTermine;

    public Voleur(int x, int y) {
        this.x = x;
        this.y = y;
        setDaemon(true);
    }

    public void setOnPillageTermine(Runnable callback) {
        this.onPillageTermine = callback;
    }

    @Override
    public void run() {
        // Phase 1 : avancer vers la cible
        while (actif && !estArrive()) {
            avancerVersCible();
            try {
                Thread.sleep(DELAI_DEPLACEMENT_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (!actif) return;

        // Phase 2 : pillage sur place
        try {
            Thread.sleep(DUREE_PILLAGE_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (!actif) return;

        // Phase 3 : déclencher la résolution vol/défense
        voleReussi = true;
        if (onPillageTermine != null) {
            onPillageTermine.run();
        }
        actif = false;
    }

    // ── Déplacement ───────────────────────────────────────────────────────────

    public boolean estArrive() {
        return cible != null && x == cible.getX() && y == cible.getY();
    }

    public synchronized void avancerVersCible() {
        if (!actif || cible == null || estArrive()) return;
        int pas = vitesse;
        while (pas > 0 && !estArrive()) {
            if      (x < cible.getX()) x++;
            else if (x > cible.getX()) x--;
            else if (y < cible.getY()) y++;
            else if (y > cible.getY()) y--;
            pas--;
        }
    }

    public synchronized void fuir() {
        int targetX = (x < 10) ? 0 : 19;
        int targetY = (y < 7)  ? 0 : 14;
        if (x < targetX) x++;
        else if (x > targetX) x--;
        if (y < targetY) y++;
        else if (y > targetY) y--;
    }

    // ── Combat ────────────────────────────────────────────────────────────────

    public void subirAttaque(int degats) {
        pv -= degats;
        if (pv <= 0) {
            pv = 0;
            actif = false;
            interrupt();
        }
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public synchronized int getX()  { return x; }
    public synchronized int getY()  { return y; }

    public void setPosition(int nx, int ny) { this.x = nx; this.y = ny; }

    public int  getVitesse()    { return vitesse; }
    public int  getAtk()        { return atk; }
    public int  getPv()         { return pv; }

    public Batiment getCible()              { return cible; }
    public void     setCible(Batiment c)    { this.cible = c; }

    public boolean isActif()                { return actif; }
    public void    setActif(boolean actif)  { this.actif = actif; if (!actif) interrupt(); }

    public boolean isVoleReussi()                   { return voleReussi; }
    public void    setVoleReussi(boolean v)         { this.voleReussi = v; }

    public boolean aVoleRessource(int rx, int ry)   { return x == rx && y == ry; }
}