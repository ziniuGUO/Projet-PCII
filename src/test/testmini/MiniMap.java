package test.testmini;

import model.ActionType;
import model.AutelInvocation;
import model.Personnage;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * C'est le moteur logique de notre carte.
 * Elle gère les ressources, l'invocation des héros et le cycle de vie des missions.
 */
public class MiniMap {

    public static final int HERBE = 0;
    public static final int BOIS = 1;
    public static final int AUTEL = 2;

    private final int width;
    private final int height;
    private final int[][] terrain;

    private final AutelInvocation autel;
    private final List<Personnage> personnages = new ArrayList<>();
    private final Random random = new Random();

    // --- Ressources ---
    private int stockOr = 500;
    private int stockBoisForet = 200;
    private final int stockBoisForetMax = 200;
    private int stockBoisJoueur = 0;

    // --- Feedback UI ---
    private String notificationMessage = "";
    private ActionType actionMiseEnValeur = null;
    private Personnage personnageMiseEnValeur = null;

    public MiniMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrain = new int[height][width];

        initialiserTerrain();

        int centreX = width / 2;
        int centreY = height / 2;
        this.autel = new AutelInvocation(centreX, centreY);
        terrain[centreY][centreX] = AUTEL;

        demarrerThreadBois();
        demarrerThreadMissions();
    }

    private void initialiserTerrain() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                terrain[y][x] = HERBE;
            }
        }

        // Zone bois en haut à gauche (4x4)
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                terrain[y][x] = BOIS;
            }
        }
    }

    /* --- Accesseurs (Getters) --- */

    public List<Personnage> getPersonnages() {
        return personnages;
    }

    public int getTerrainAt(int x, int y) {
        if (!isValidPosition(x, y)) return -1;
        return terrain[y][x];
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public AutelInvocation getAutel() {
        return autel;
    }

    public boolean estAutel(int x, int y) {
        return x == autel.getX() && y == autel.getY();
    }

    public int getStockOr() {
        return stockOr;
    }

    public int getStockBoisForet() {
        return stockBoisForet;
    }

    public int getStockBoisForetMax() {
        return stockBoisForetMax;
    }

    public int getStockBoisJoueur() {
        return stockBoisJoueur;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public ActionType getActionMiseEnValeur() {
        return actionMiseEnValeur;
    }

    public Personnage getPersonnageMiseEnValeur() {
        return personnageMiseEnValeur;
    }

    // --- Logique métier ---

    public boolean peutInvoquer(int cout) {
        return stockOr >= cout;
    }

    /**
     * Gère l'invocation d'un nouveau personnage.
     */
    public Personnage invoquerPersonnage(int cout) {
        if (!peutInvoquer(cout)) return null;

        stockOr -= cout;

        String[] noms = {
                "Audrey", "Angel", "Lina", "Mira", "Arthur",
                "Sylva", "Ronan", "Elya", "Kael", "Nyx"
        };
        String nom = noms[random.nextInt(noms.length)];
        int etoiles = tirerRarete();

        Personnage p = new Personnage(nom, etoiles, autel.getX(), autel.getY());
        personnages.add(p);
        return p;
    }

    private int tirerRarete() {
        double r = random.nextDouble();
        if (r < 0.40) return 1;
        if (r < 0.70) return 2;
        if (r < 0.85) return 3;
        if (r < 0.95) return 4;
        return 5;
    }

    /**
     * Place un personnage sur le terrain pour une mission.
     */
    public void deployerPersonnage(Personnage p, ActionType action) {
        if (p == null) return;

        if (!p.isDeploye()) {
            p.setPosition(autel.getX(), autel.getY());
            p.setDeploye(true);
        }

        p.interrompreAction();
        p.setPretARecuperer(false);
        p.setActionCourante(action);

        Point cible = trouverCibleLibrePourAction(action, p);
        p.setCible(cible.x, cible.y);

        this.actionMiseEnValeur = action;
        this.personnageMiseEnValeur = p;
        this.notificationMessage = "";
    }

    public void rappelerPersonnage(Personnage p) {
        if (p == null) return;

        p.rappeler();

        if (personnageMiseEnValeur == p) {
            personnageMiseEnValeur = null;
            actionMiseEnValeur = null;
        }

        notificationMessage = "";
    }

    public int recupererRecompenseEtRappeler(Personnage p) {
        if (p == null || !p.isPretARecuperer() || p.getActionCourante() == null) return 0;

        int gain = 0;
        if (p.getActionCourante() == ActionType.COUPER_BOIS) {
            gain = Math.min(10, stockBoisForet);
            stockBoisForet -= gain;
            stockBoisJoueur += gain;
        }

        rappelerPersonnage(p);
        return gain;
    }

    public Personnage getPersonnageAt(int x, int y) {
        for (Personnage p : personnages) {
            if (p.isDeploye() && p.getX() == x && p.getY() == y) {
                return p;
            }
        }
        return null;
    }

    public boolean caseCorrespondAAction(int terrainType, ActionType action) {
        if (action == null) return false;
        return action == ActionType.COUPER_BOIS && terrainType == BOIS;
    }

    private Point trouverCibleLibrePourAction(ActionType action, Personnage courant) {
        List<Point> cases = getCasesPourAction(action);

        for (Point pt : cases) {
            if (!caseOccupeePourAction(pt.x, pt.y, action, courant)) {
                return pt;
            }
        }

        if (!cases.isEmpty()) {
            return cases.get(0);
        }

        return new Point(autel.getX(), autel.getY());
    }

    private boolean caseOccupeePourAction(int x, int y, ActionType action, Personnage courant) {
        for (Personnage p : personnages) {
            if (p == courant) continue;
            if (!p.isDeploye()) continue;
            if (p.getActionCourante() != action) continue;

            if (p.isPretARecuperer()) continue;

            if (p.getCibleX() == x && p.getCibleY() == y) {
                return true;
            }
        }
        return false;
    }

    private List<Point> getCasesPourAction(ActionType action) {
        List<Point> result = new ArrayList<>();

        if (action == ActionType.DEFENDRE) {
            result.add(new Point(autel.getX(), autel.getY() - 1));
            return result;
        }

        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                if (terrain[y][x] == BOIS) {
                    result.add(new Point(x, y));
                }
            }
        }

        return result;
    }

    /**
     * Thread gérant la repousse passive du bois.
     */
    private void demarrerThreadBois() {
        Thread t = new Thread(() -> {
            while (true) {
                if (stockBoisForet < stockBoisForetMax) {
                    stockBoisForet++;
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Thread gérant les déplacements et l'exécution des missions des personnages.
     */
    private void demarrerThreadMissions() {
        Thread t = new Thread(() -> {
            while (true) {
                for (Personnage p : personnages) {
                    if (!p.isDeploye() || p.getActionCourante() == null) continue;

                    if (!p.estArrive()) {
                        p.avancerVersCible();
                    } else {
                        if (!p.isEnExecution() && !p.isPretARecuperer()) {
                            p.commencerExecution();
                        } else if (p.isEnExecution()) {
                            boolean fini = p.mettreAJourExecution();
                            if (fini) {
                                notificationMessage = p.getNom()
                                        + " a terminé sa mission. Clique sur lui pour récupérer.";
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    return;
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }
}