package model;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Classe representant la carte du jeu.
 *
 * Types de terrain :
 *   1 = HERBE       - terrain vide (par defaut)
 *   2 = BATIMENT    - emplacement de batiment (gris, a construire)
 *   3 = EAU         - riviere, lac (pas traversable)
 *   4 = FORET_BOIS  - zone de recolte de BOIS
 *   5 = MINE_FER    - zone de recolte de FER
 *   6 = GISEMENT_OR - zone de recolte d'OR
 *   7 = CUEILLETTE  - zone de recolte de NOURRITURE (baies, champignons...)
 */
public class Map {

    // ── Constantes de type de terrain ─────────────────────────────────────────
    public static final int HERBE       = 1;
    public static final int BATIMENT    = 2;
    public static final int FORET_BOIS  = 3;
    public static final int MINE_FER    = 4;
    public static final int GISEMENT_OR = 5;
    public static final int CUEILLETTE  = 6;
    public JourNuit jourNuit;
    public List<Voleur> voleurs = new ArrayList<>();
    private final int width;
    private final int height;
    private final int[][] terrain;

    // Personnages (version minimale)
    private final List<Personnage> personnages = new ArrayList<>();
    private final Random random = new Random();
    private final AutelInvocation autelInvocation;
    private final int autelX = 13;
    private final int autelY = 4;
    private int stockOr = 500;
    private int stockBoisForet = 200;
    private final int stockBoisForetMax = 200;
    private int stockFer = 0;
    private ActionType actionMiseEnValeur = null;
    private Personnage personnageMiseEnValeur = null;

    private String notificationMessage = "";
    /**
     * Constructeur : cree une map 20x15 par defaut avec
     * quelques zones de ressources pre-placees.
     */
    public Map(int width, int height) {
        this.width   = width;
        this.height  = height;
        this.terrain = new int[height][width];
        this.jourNuit = new JourNuit();
        this.autelInvocation = new AutelInvocation("Autel d'invocation", 300, autelX, autelY, 0, 0, 0);
        jourNuit.start(); // Lancer le thread de simulation jour/nuit
        initializeDefaultMap();
        // Personnage de demo (a remplacer plus tard par ton systeme d'invocation)
        // Place-le sur une case d'herbe proche du centre.
        voleurs.add(new Voleur(10, 0));
        voleurs.add(new Voleur(18, 8));
        for (Voleur v : voleurs) {
            v.start(); // Lancer le thread de chaque voleur
        }
        demarrerThreadDeplacement();
        demarrerThreadRegenerationForet();
    }
    /** Retourne la liste des personnages. */
    public List<Personnage> getPersonnages() {
        return personnages;
    }

    /** Retourne la liste des voleurs. */
    public List<Voleur> getVoleurs() {
        return voleurs;
    }

    public boolean getIsDay() {
        return jourNuit.getIsDay();
    }

    public AutelInvocation getAutelInvocation() {
        return autelInvocation;
    }
    public boolean estAutelInvocation(int x, int y) {
        return x == autelX && y == autelY;
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
    public int getStockFer() {
        return stockFer;
    }

    public ActionType getActionMiseEnValeur() {
        return actionMiseEnValeur;
    }

    public Personnage getPersonnageMiseEnValeur() {
        return personnageMiseEnValeur;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void clearNotificationMessage() {
        notificationMessage = "";
    }
    public boolean peutInvoquer(int coutInvocation) {
        return stockOr >= coutInvocation;
    }
    public Personnage invoquerPersonnage(int coutInvocation) {
        if (!peutInvoquer(coutInvocation)) return null;

        stockOr -= coutInvocation;
        String[] noms = {"Arthur", "Lina", "Kael", "Mira", "Thorne", "Sylva", "Ronan", "Elya", "Darius", "Nyx"};
        String nom = noms[random.nextInt(noms.length)];
        int etoiles = tirerRarete();
        Personnage personnage = new Personnage(nom, etoiles, getCentreX(), getCentreY());
        personnages.add(personnage);
        return personnage;
    }
    private int tirerRarete() {
        double r = random.nextDouble();
        if (r < 0.40) return 1;
        if (r < 0.70) return 2;
        if (r < 0.85) return 3;
        if (r < 0.95) return 4;
        return 5;
    }
    public void deployerPersonnage(Personnage p, ActionType action) {
        if (p == null) return;

        if (!p.isDeploye()) {
            p.setPosition(getCentreX(), getCentreY());
            p.setDeploye(true);
        }

        p.interrompreAction();
        p.setPretARecuperer(false);
        p.setActionCourante(action);

        java.awt.Point cible = trouverCibleLibrePourAction(action, p);
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
        switch (p.getActionCourante()) {
            case COUPER_BOIS -> gain = 10;
            case MINER_FER -> gain = 5;
            case DEFENDRE -> gain = 0;
        }

        appliquerGain(p.getActionCourante(), gain);
        rappelerPersonnage(p);
        return gain;
    }

    private void appliquerGain(ActionType action, int gain) {
        if (action == null) return;

        switch (action) {
            case COUPER_BOIS -> {
                // le gain va au joueur, et la forêt perd du stock
                if (stockBoisForet > 0) {
                    stockBoisForet = Math.max(0, stockBoisForet - gain);
                }
            }
            case MINER_FER -> stockFer += gain;
            case DEFENDRE -> {
                // pas de récompense pour l'instant
            }
        }
    }

    public boolean caseCorrespondAAction(int terrainType, ActionType action) {
        if (action == null) return false;

        return switch (action) {
            case COUPER_BOIS -> terrainType == FORET_BOIS;
            case MINER_FER -> terrainType == MINE_FER;
            case DEFENDRE -> false;
        };
    }

    public boolean personnageOccupe(Personnage p) {
        return p != null && !p.estDisponible();
    }

    public List<Personnage> getPersonnagesEnAttenteDeRecuperation() {
        List<Personnage> finies = new ArrayList<>();
        for (Personnage p : personnages) {
            if (p.isPretARecuperer()) {
                finies.add(p);
            }
        }
        return finies;
    }

    private int getCentreX() {
        return width / 2;
    }

    private int getCentreY() {
        return height / 2;
    }

    /** Retourne le personnage situe sur la case (x,y), ou null s'il n'y en a pas. */
    public Personnage getPersonnageAt(int x, int y) {
        for (Personnage p : personnages) {
            if (p.isDeploye() && p.getX() == x && p.getY() == y) return p;
        }
        return null;
    }
    /**
     * Map par defaut avec :
     * - Herbe partout
     * - Une riviere (eau)
     * - Une foret a bois
     * - Une mine de fer
     * - Un gisement d'or
     * - Une zone de cueillette (nourriture)
     * - Des emplacements de batiments
     */
    private void initializeDefaultMap() {
        // Herbe partout par defaut
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                terrain[y][x] = HERBE;

        // ── RESSOURCES - une par coin ─────────────────────────────────────────

        // COIN HAUT-GAUCHE : FORET (BOIS) - la plus grande (7x5)
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 7; x++)
                setTerrainAt(x, y, FORET_BOIS);

        // COIN HAUT-DROIT : NOURRITURE - carre 4x4
        for (int y = 0; y < 4; y++)
            for (int x = width - 4; x < width; x++)
                setTerrainAt(x, y, CUEILLETTE);

        // COIN BAS-GAUCHE : FER - carre 3x3
        for (int y = height - 3; y < height; y++)
            for (int x = 0; x < 3; x++)
                setTerrainAt(x, y, MINE_FER);

        // COIN BAS-DROIT : OR - carre 2x2
        for (int y = height - 2; y < height; y++)
            for (int x = width - 2; x < width; x++)
                setTerrainAt(x, y, GISEMENT_OR);

        // ── BATIMENTS (gris, a construire) ─────────────────────────────────────

        // Centre (10,7) :   pour l'hotel de ville
        setTerrainAt(10, 7, BATIMENT);

        // Cercle de batiments autour de (10,7) - rayon 4
        // Les 8 positions formant le cercle:
        setTerrainAt(10, 3, BATIMENT);  // Haut (distance 4)
        setTerrainAt(10, 11, BATIMENT); // Bas (distance 4)
        setTerrainAt( 6, 7, BATIMENT);  // Gauche (distance 4)
        setTerrainAt(14, 7, BATIMENT);  // Droite (distance 4)
        setTerrainAt( 7, 4, BATIMENT);  // Haut-gauche
        setTerrainAt(13, 4, BATIMENT);  // Haut-droit
        setTerrainAt( 7, 10, BATIMENT); // Bas-gauche
        setTerrainAt(13, 10, BATIMENT); // Bas-droit

        // Quelques entrepots supplementaires pour les batiments de stockage
        setTerrainAt(2, 6, BATIMENT);
        setTerrainAt(4, 13, BATIMENT);
        setTerrainAt(16, 13, BATIMENT);
        setTerrainAt(17, 5, BATIMENT);
    }
    private void demarrerThreadDeplacement() {
        Thread threadDeplacement = new Thread(() -> {
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
                                notificationMessage = p.getNom() + " a terminé sa mission. Clique sur lui pour récupérer la récompense.";
                            }
                        }
                    }
                }

                try {
                    Thread.sleep(350);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        threadDeplacement.setDaemon(true);
        threadDeplacement.start();
    }

    private void demarrerThreadRegenerationForet() {
        Thread threadForet = new Thread(() -> {
            while (true) {
                if (stockBoisForet < stockBoisForetMax) {
                    stockBoisForet++;
                }
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        threadForet.setDaemon(true);
        threadForet.start();
    }

    // ── Acces terrain ──────────────────────────────────────────────────────────

    /** Retourne le type de terrain en (x,y), ou -1 si hors limites */
    public int getTerrainAt(int x, int y) {
        if (isValidPosition(x, y)) return terrain[y][x];
        return -1;
    }

    /** Modifie le type de terrain en (x,y) */
    public void setTerrainAt(int x, int y, int type) {
        if (isValidPosition(x, y)) terrain[y][x] = type;
    }

    /** Place un emplacement de batiment en (x,y) */
    public void placerBatiment(int x, int y) {
        setTerrainAt(x, y, BATIMENT);
    }

    /** Retourne true si (x,y) est dans les limites de la map */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    /** Retourne le nom lisible du type de terrain */
    public static String getNomTerrain(int type) {
        return switch (type) {
            case HERBE       -> "Herbe";
            case BATIMENT    -> "Batiment";
            case FORET_BOIS  -> "Foret (Bois)";
            case MINE_FER    -> "Mine (Fer)";
            case GISEMENT_OR -> "Gisement (Or)";
            case CUEILLETTE  -> "Cueillette (Nourriture)";
            default          -> "Inconnu";
        };
    }

    /** Affichage console (debug) */
    public void displayMap() {
        String[] sym = {"?", ".", "B", "T", "#", "$", "M"};
        System.out.println("=== MAP " + width + "x" + height + " ===");
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int t = terrain[y][x];
                System.out.print((t >= 0 && t < sym.length ? sym[t] : "?") + " ");
            }
            System.out.println();
        }
        System.out.println("Legende: . herbe  B batiment  T bois  # fer  $ or  M nourriture");
    }
    private java.awt.Point trouverCibleLibrePourAction(ActionType action, Personnage courant) {
        java.util.List<java.awt.Point> cases = getCasesPourAction(action);

        for (java.awt.Point pt : cases) {
            if (!caseOccupeePourAction(pt.x, pt.y, action, courant)) {
                return pt;
            }
        }

        // Si toutes les cases sont occupées, retourner le centre par défaut
        if (!cases.isEmpty()) {
            return cases.get(0);
        }

        return new java.awt.Point(getCentreX(), getCentreY());
    }
    private boolean caseOccupeePourAction(int x, int y, ActionType action, Personnage courant) {
        for (Personnage p : personnages) {
            if (p == courant) continue;
            if (!p.isDeploye()) continue;
            if (p.getActionCourante() != action) continue;

            // Si le personnage est prêt à récupérer, il n'occupe plus la case
            if (p.isPretARecuperer()) continue;

            // Si un autre personnage a la même cible, la case est considérée comme occupée
            if (p.getCibleX() == x && p.getCibleY() == y) {
                return true;
            }
        }
        return false;
    }
    private java.util.List<java.awt.Point> getCasesPourAction(ActionType action) {
        java.util.List<java.awt.Point> result = new java.util.ArrayList<>();

        int terrainType;
        switch (action) {
            case COUPER_BOIS -> terrainType = FORET_BOIS;
            case MINER_FER -> terrainType = MINE_FER;
            case DEFENDRE -> {
                result.add(new java.awt.Point(width / 2, height / 2));
                return result;
            }
            default -> {
                result.add(new java.awt.Point(width / 2, height / 2));
                return result;
            }
        }

        // Parcourir la map de bas en haut pour trouver les cases du type de terrain correspondant
        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                if (terrain[y][x] == terrainType) {
                    result.add(new java.awt.Point(x, y));
                }
            }
        }

        return result;
    }
    // ── Getters ───────────────────────────────────────────────────────────────
    public int getWidth()  { return width;  }
    public int getHeight() { return height; }
}
