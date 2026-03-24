package model;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Map {

    public static final int HERBE       = 1;
    public static final int BATIMENT    = 2;
    public static final int FORET_BOIS  = 3;
    public static final int MINE_FER    = 4;
    public static final int GISEMENT_OR = 5;
    public static final int CUEILLETTE  = 6;

    // types de batiments
    public static final String TYPE_HOTEL_VILLE    = "HOTEL_VILLE";
    public static final String TYPE_MAISON         = "MAISON";
    public static final String TYPE_ENTREPOT_BOIS  = "ENTREPOT_BOIS";
    public static final String TYPE_ENTREPOT_FER   = "ENTREPOT_FER";
    public static final String TYPE_ENTREPOT_OR    = "ENTREPOT_OR";
    public static final String TYPE_ENTREPOT_NOURR = "ENTREPOT_NOURR";
    public static final String TYPE_AUTEL_INVOC    = "AUTEL_INVOC";

    // Coûts de construction par type de bâtiment {bois, fer, or}
    // Seul le bois est nécessaire pour construire
    public static final int[] COUT_MAISON         = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_BOIS  = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_FER   = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_OR    = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_NOURR = {2000, 0, 0};

    // type de batiment par position "x,y"
    private final HashMap<String, String>  typesBatiments      = new HashMap<>();
    // indique si le batiment est construit ou non
    private final HashMap<String, Boolean> batimentsConstruits = new HashMap<>();

    public JourNuit jourNuit;
    public List<Voleur> voleurs = new ArrayList<>();
    private final int width;
    private final int height;
    private final int[][] terrain;

    private final List<Personnage> personnages = new ArrayList<>();
    private final Random random = new Random();
    private final AutelInvocation autelInvocation;
    private final int autelX = 10;
    private final int autelY = 13;
    private final HotelDeVille hotelDeVille;
    private static final int HDV_X = 10;
    private static final int HDV_Y = 7;
    private Inventaire inventaire;
    private int stockOr = 500;
    private int stockBoisForet = 200;
    private final int stockBoisForetMax = 200;
    private int stockFer = 0;
    private ActionType actionMiseEnValeur = null;
    private Personnage personnageMiseEnValeur = null;
    private String notificationMessage = "";

    // listener de gain de ressource
public interface OnRessourceGagneeListener {
    void onRessourceGagnee(model.Ressource.Type type, int quantite);
}
private OnRessourceGagneeListener ressourceListener;
public void setOnRessourceGagneeListener(OnRessourceGagneeListener l) {
    this.ressourceListener = l;
}

    public Map(int width, int height) {
        this.width   = width;
        this.height  = height;
        this.terrain = new int[height][width];
        this.jourNuit = new JourNuit();
        this.autelInvocation = new AutelInvocation("Autel d'invocation", 300, autelX, autelY, 0, 0, 0);
        this.hotelDeVille = new HotelDeVille("Hotel de Ville", 1000, HDV_X, HDV_Y, 0, 0, 0);
        jourNuit.start();
        initializeDefaultMap();
        initBatiments();

      

        voleurs.add(new Voleur(10, 0));
        voleurs.add(new Voleur(18, 8));
        for (Voleur v : voleurs) {
            v.start();
        }
        demarrerThreadDeplacement();
        demarrerThreadRegenerationForet();
    }

    public List<Personnage> getPersonnages() { return personnages; }
    public List<Voleur> getVoleurs()         { return voleurs; }
    public boolean getIsDay()                { return jourNuit.getIsDay(); }
    public AutelInvocation getAutelInvocation() { return autelInvocation; }
    public boolean estAutelInvocation(int x, int y) { return x == autelX && y == autelY; }
    public int getStockOr()          { return stockOr; }
    public int getStockBoisForet()   { return stockBoisForet; }
    public int getStockBoisForetMax(){ return stockBoisForetMax; }
    public int getStockFer()         { return stockFer; }
    public ActionType getActionMiseEnValeur()    { return actionMiseEnValeur; }
    public Personnage getPersonnageMiseEnValeur(){ return personnageMiseEnValeur; }
    public String getNotificationMessage()       { return notificationMessage; }
    public void clearNotificationMessage()       { notificationMessage = ""; }
    public HotelDeVille getHotelDeVille()         { return hotelDeVille; }
    public Inventaire getInventaire()              { return inventaire; }
    public boolean estHotelDeVille(int x, int y)  { return x == HDV_X && y == HDV_Y; }

    /** Lie l'inventaire à la Map et applique les capacités initiales. */
    public void setInventaire(Inventaire inv) {
        this.inventaire = inv;
        recalculerCapacites();
    }

    /**
     * Recalcule et applique les capacités max de l'inventaire selon :
     * - le niveau de l'HdV
     * - quels entrepôts sont construits
     */
    public void recalculerCapacites() {
        if (inventaire == null) return;
        boolean entrepotBois  = estConstruit(2,  6);
        boolean entrepotFer   = estConstruit(4,  13);
        boolean entrepotOr    = estConstruit(16, 13);
        boolean entrepotNourr = estConstruit(17, 5);
        inventaire.setCapacites(
            hotelDeVille.getCapaciteTotale(entrepotBois),
            hotelDeVille.getCapaciteTotale(entrepotFer),
            hotelDeVille.getCapaciteTotale(entrepotOr),
            hotelDeVille.getCapaciteTotale(entrepotNourr)
        );
    }

    /**
     * Tente d'améliorer l'Hôtel de Ville d'un niveau.
     * Retourne un message résultat.
     */
    public String ameliorerHotelDeVille() {
        if (hotelDeVille.estAuNiveauMax())
            return "L'Hôtel de Ville est déjà au niveau maximum !";
        int cout = hotelDeVille.getCoutAmelioration();
        if (inventaire == null || inventaire.getFer() < cout)
            return "Pas assez de fer ! Il faut " + cout + " fer.";
        inventaire.retirerRessource(Ressource.Type.FER, cout);
        hotelDeVille.monterNiveau();
        recalculerCapacites();
        int cap = hotelDeVille.getCapaciteBase();
        return "Hôtel de Ville amélioré ! Niveau " + hotelDeVille.getNiveau()
             + " — Capacité de base : " + cap;
    }

    /** Construit un bâtiment et recalcule les capacités si c'est un entrepôt. */
    public void construireBatimentEtRecalculer(int x, int y) {
        construireBatiment(x, y);
        recalculerCapacites();
    }

    /** Retourne les coûts {bois, fer, or} pour construire un bâtiment selon son type. */
    public int[] getCoutConstruction(int x, int y) {
        String type = getTypeBatiment(x, y);
        if (type == null) return null;
        return switch (type) {
            case TYPE_MAISON         -> COUT_MAISON;
            case TYPE_ENTREPOT_BOIS  -> COUT_ENTREPOT_BOIS;
            case TYPE_ENTREPOT_FER   -> COUT_ENTREPOT_FER;
            case TYPE_ENTREPOT_OR    -> COUT_ENTREPOT_OR;
            case TYPE_ENTREPOT_NOURR -> COUT_ENTREPOT_NOURR;
            default -> null;
        };
    }

    /** Tente de construire un bâtiment en déduisant les ressources. Retourne null si succès, message d'erreur sinon. */
    public String tenterConstruction(int x, int y) {
        if (inventaire == null) return "Inventaire non initialisé.";
        int[] cout = getCoutConstruction(x, y);
        if (cout == null) return "Ce bâtiment ne peut pas être construit.";
        if (inventaire.getBois() < cout[0]) return "Pas assez de bois ! Il faut " + cout[0] + ".";
        if (inventaire.getFer()  < cout[1]) return "Pas assez de fer ! Il faut "  + cout[1] + ".";
        if (inventaire.getOr()   < cout[2]) return "Pas assez d'or ! Il faut "    + cout[2] + ".";
        if (cout[0] > 0) inventaire.retirerRessource(Ressource.Type.BOIS, cout[0]);
        if (cout[1] > 0) inventaire.retirerRessource(Ressource.Type.FER,  cout[1]);
        if (cout[2] > 0) inventaire.retirerRessource(Ressource.Type.OR,   cout[2]);
        construireBatimentEtRecalculer(x, y);
        return null; // succès
    }

    public boolean peutInvoquer(int coutInvocation) {
        return stockOr >= coutInvocation;
    }

    public Personnage invoquerPersonnage(int coutInvocation) {
        if (!peutInvoquer(coutInvocation)) return null;
        stockOr -= coutInvocation;
        if (ressourceListener != null && coutInvocation > 0)
                ressourceListener.onRessourceGagnee(model.Ressource.Type.OR, -coutInvocation);
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
            case MINER_FER   -> gain = 5;
            case DEFENDRE    -> gain = 0;
        }
        appliquerGain(p.getActionCourante(), gain);
        rappelerPersonnage(p);
        return gain;
    }

   private void appliquerGain(ActionType action, int gain) {
    if (action == null) return;
    switch (action) {
        case COUPER_BOIS -> {
            if (stockBoisForet > 0) stockBoisForet = Math.max(0, stockBoisForet - gain);
            if (ressourceListener != null && gain > 0)
                ressourceListener.onRessourceGagnee(model.Ressource.Type.BOIS, gain);
        }
        case MINER_FER -> {
            stockFer += gain;
            if (ressourceListener != null && gain > 0)
                ressourceListener.onRessourceGagnee(model.Ressource.Type.FER, gain);
        }
        case DEFENDRE -> {}
    }
}

    public boolean caseCorrespondAAction(int terrainType, ActionType action) {
        if (action == null) return false;
        return switch (action) {
            case COUPER_BOIS -> terrainType == FORET_BOIS;
            case MINER_FER   -> terrainType == MINE_FER;
            case DEFENDRE    -> false;
        };
    }

    public boolean personnageOccupe(Personnage p) {
        return p != null && !p.estDisponible();
    }

    public List<Personnage> getPersonnagesEnAttenteDeRecuperation() {
        List<Personnage> finies = new ArrayList<>();
        for (Personnage p : personnages) {
            if (p.isPretARecuperer()) finies.add(p);
        }
        return finies;
    }

    public Personnage getPersonnageAt(int x, int y) {
        for (Personnage p : personnages) {
            if (p.isDeploye() && p.getX() == x && p.getY() == y) return p;
        }
        return null;
    }

    private void initializeDefaultMap() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                terrain[y][x] = HERBE;

        // COIN HAUT-GAUCHE : FORET (BOIS)
        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 7; x++)
                setTerrainAt(x, y, FORET_BOIS);

        // COIN HAUT-DROIT : NOURRITURE
        for (int y = 0; y < 4; y++)
            for (int x = width - 4; x < width; x++)
                setTerrainAt(x, y, CUEILLETTE);

        // COIN BAS-GAUCHE : FER
        for (int y = height - 3; y < height; y++)
            for (int x = 0; x < 3; x++)
                setTerrainAt(x, y, MINE_FER);

        // COIN BAS-DROIT : OR
        for (int y = height - 2; y < height; y++)
            for (int x = width - 2; x < width; x++)
                setTerrainAt(x, y, GISEMENT_OR);

        // BATIMENTS
        setTerrainAt(10, 7, BATIMENT);
        setTerrainAt(10, 3, BATIMENT);
        setTerrainAt(10, 11, BATIMENT);
        setTerrainAt( 6, 7, BATIMENT);
        setTerrainAt(14, 7, BATIMENT);
        setTerrainAt( 7, 4, BATIMENT);
        setTerrainAt(13, 4, BATIMENT);
        setTerrainAt( 7, 10, BATIMENT);
        setTerrainAt(13, 10, BATIMENT);
        setTerrainAt(10, 13, BATIMENT);  // Autel d'invocation
        setTerrainAt( 2,  6, BATIMENT);
        setTerrainAt( 4, 13, BATIMENT);
        setTerrainAt(16, 13, BATIMENT);
        setTerrainAt(17, 5, BATIMENT);
    }

    private void demarrerThreadDeplacement() {
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
                            if (fini) notificationMessage = p.getNom() + " a terminé sa mission. Clique sur lui pour récupérer la récompense.";
                        }
                    }
                }
                try { Thread.sleep(350); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void demarrerThreadRegenerationForet() {
        Thread t = new Thread(() -> {
            while (true) {
                if (stockBoisForet < stockBoisForetMax) stockBoisForet++;
                try { Thread.sleep(2500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public int getTerrainAt(int x, int y) {
        if (isValidPosition(x, y)) return terrain[y][x];
        return -1;
    }

    public void setTerrainAt(int x, int y, int type) {
        if (isValidPosition(x, y)) terrain[y][x] = type;
    }

    public void placerBatiment(int x, int y) { setTerrainAt(x, y, BATIMENT); }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

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
    }

    private java.awt.Point trouverCibleLibrePourAction(ActionType action, Personnage courant) {
        java.util.List<java.awt.Point> cases = getCasesPourAction(action);
        for (java.awt.Point pt : cases) {
            if (!caseOccupeePourAction(pt.x, pt.y, action, courant)) return pt;
        }
        if (!cases.isEmpty()) return cases.get(0);
        return new java.awt.Point(getCentreX(), getCentreY());
    }

    private boolean caseOccupeePourAction(int x, int y, ActionType action, Personnage courant) {
        for (Personnage p : personnages) {
            if (p == courant || !p.isDeploye() || p.getActionCourante() != action) continue;
            if (p.isPretARecuperer()) continue;
            if (p.getCibleX() == x && p.getCibleY() == y) return true;
        }
        return false;
    }

    private java.util.List<java.awt.Point> getCasesPourAction(ActionType action) {
        java.util.List<java.awt.Point> result = new java.util.ArrayList<>();
        int terrainType;
        switch (action) {
            case COUPER_BOIS -> terrainType = FORET_BOIS;
            case MINER_FER   -> terrainType = MINE_FER;
            default -> { result.add(new java.awt.Point(width / 2, height / 2)); return result; }
        }
        for (int x = 0; x < width; x++)
            for (int y = height - 1; y >= 0; y--)
                if (terrain[y][x] == terrainType)
                    result.add(new java.awt.Point(x, y));
        return result;
    }

    private int getCentreX() { return width / 2; }
    private int getCentreY() { return height / 2; }
    public int getWidth()    { return width; }
    public int getHeight()   { return height; }

    // ── Systeme de types de batiments ─────────────────────────────────────────

    private void initBatiments() {
        placerBatimentAvecType(10, 7, TYPE_HOTEL_VILLE, true);

        placerBatimentAvecType(10,  3, TYPE_MAISON, false);
        placerBatimentAvecType(10, 11, TYPE_MAISON, false);
        placerBatimentAvecType( 6,  7, TYPE_MAISON, false);
        placerBatimentAvecType(14,  7, TYPE_MAISON, false);
        placerBatimentAvecType( 7,  4, TYPE_MAISON, false);
        placerBatimentAvecType(13,  4, TYPE_MAISON, false);
        placerBatimentAvecType( 7, 10, TYPE_MAISON, false);
        placerBatimentAvecType(13, 10, TYPE_MAISON, false);

        placerBatimentAvecType( 2,  6, TYPE_ENTREPOT_BOIS,  false);
        placerBatimentAvecType( 4, 13, TYPE_ENTREPOT_FER,   false);
        placerBatimentAvecType(16, 13, TYPE_ENTREPOT_OR,    false);
        placerBatimentAvecType(17,  5, TYPE_ENTREPOT_NOURR, false);

        placerBatimentAvecType(autelX, autelY, TYPE_AUTEL_INVOC, true);
    }

    private void placerBatimentAvecType(int x, int y, String type, boolean construit) {
        String cle = x + "," + y;
        typesBatiments.put(cle, type);
        batimentsConstruits.put(cle, construit);
    }

    public String getTypeBatiment(int x, int y) {
        return typesBatiments.get(x + "," + y);
    }

    public boolean estConstruit(int x, int y) {
        Boolean b = batimentsConstruits.get(x + "," + y);
        return b != null && b;
    }

    public void construireBatiment(int x, int y) {
        String cle = x + "," + y;
        if (batimentsConstruits.containsKey(cle))
            batimentsConstruits.put(cle, true);
    }
}