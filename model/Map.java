package model;

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

    private final int width;
    private final int height;
    private final int[][] terrain;

    /**
     * Constructeur : cree une map 20x15 par defaut avec
     * quelques zones de ressources pre-placees.
     */
    public Map(int width, int height) {
        this.width   = width;
        this.height  = height;
        this.terrain = new int[height][width];
        initializeDefaultMap();
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

    // ── Getters ───────────────────────────────────────────────────────────────
    public int getWidth()  { return width;  }
    public int getHeight() { return height; }
}