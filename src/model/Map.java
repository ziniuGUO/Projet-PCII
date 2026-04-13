package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Map {

    /* attributs */
    public static final int HERBE       = 1;
    public static final int BATIMENT    = 2;
    public static final int FORET_BOIS  = 3;
    public static final int MINE_FER    = 4;
    public static final int GISEMENT_OR = 5;
    public static final int CUEILLETTE  = 6;

    public static final String TYPE_HOTEL_VILLE    = "HOTEL_VILLE";
    public static final String TYPE_MAISON         = "MAISON";
    public static final String TYPE_ENTREPOT_BOIS  = "ENTREPOT_BOIS";
    public static final String TYPE_ENTREPOT_FER   = "ENTREPOT_FER";
    public static final String TYPE_ENTREPOT_OR    = "ENTREPOT_OR";
    public static final String TYPE_ENTREPOT_NOURR = "ENTREPOT_NOURR";
    public static final String TYPE_AUTEL_INVOC    = "AUTEL_INVOC";
    public static final String TYPE_STATUE_DRAGON  = "STATUE_DRAGON";
    public static final String TYPE_TOUR_DEFENSE   = "TOUR_DEFENSE";

    public static final int[] COUT_MAISON         = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_BOIS  = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_FER   = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_OR    = {2000, 0, 0};
    public static final int[] COUT_ENTREPOT_NOURR = {2000, 0, 0};
    public static final int[] COUT_TOUR_DEFENSE   = {1500, 500, 0};
    public static final int[] COUT_STATUE_DRAGON  = {25000, 0, 0};

    private final HashMap<String, String>  typesBatiments      = new HashMap<>();
    private final HashMap<String, Boolean> batimentsConstruits = new HashMap<>();
    private final HashMap<String, Batiment> batimentsObjets    = new HashMap<>();

    public JourNuit jourNuit;
    public List<Voleur> voleurs = new ArrayList<>();

    private final int width;
    private final int height;
    private final int[][] terrain;

    private final List<Personnage> personnages = new ArrayList<>();
    private final Random random = new Random();

    private final AutelInvocation autelInvocation;
    private final HotelDeVille hotelDeVille;

    private final int autelX = 10;
    private final int autelY = 13;
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

    private int nombreInvocations = 0;
    private int compteurSansCinqEtoiles = 0;

    private long dernierSpawnVoleur = 0L;
    private static final long INTERVALLE_SPAWN_VOLEUR = 15000L;

    public interface OnRessourceGagneeListener {
        void onRessourceGagnee(Ressource.Type type, int quantite);
    }

    private OnRessourceGagneeListener ressourceListener;

    public void setOnRessourceGagneeListener(OnRessourceGagneeListener l) {
        this.ressourceListener = l;
    }

    /* constructeur */
    public Map(int width, int height) {
        this.width = width;
        this.height = height;
        this.terrain = new int[height][width];
        this.jourNuit = new JourNuit();
        this.autelInvocation = new AutelInvocation("Autel d'invocation", 300, autelX, autelY, 0, 0, 0);
        this.hotelDeVille = new HotelDeVille("Hotel de Ville", 1000, HDV_X, HDV_Y, 0, 0, 0);

        initializeDefaultMap();
        initBatiments();

        jourNuit.start();
        demarrerThreadJeu();
        demarrerThreadRegenerationForet();
    }

    /* getters */
    public List<Personnage> getPersonnages() {
        return personnages;
    }

    public List<Voleur> getVoleurs() {
        return voleurs;
    }

    public boolean getIsDay() {
        return jourNuit.getIsDay();
    }

    public AutelInvocation getAutelInvocation() {
        return autelInvocation;
    }

    public HotelDeVille getHotelDeVille() {
        return hotelDeVille;
    }

    public Inventaire getInventaire() {
        return inventaire;
    }

    public boolean estAutelInvocation(int x, int y) {
        return x == autelX && y == autelY;
    }

    public boolean estHotelDeVille(int x, int y) {
        return x == HDV_X && y == HDV_Y;
    }

    public int getStockOr() {
        return inventaire != null ? inventaire.getOr() : stockOr;
    }

    public int getStockBoisForet() {
        return stockBoisForet;
    }

    public int getStockBoisForetMax() {
        return stockBoisForetMax;
    }

    public int getStockFer() {
        return inventaire != null ? inventaire.getFer() : stockFer;
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

    private int getCentreX() {
        return width / 2;
    }

    private int getCentreY() {
        return height / 2;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Personnage getPersonnageAt(int x, int y) {
        for (Personnage p : personnages) {
            if (p.isDeploye() && p.getX() == x && p.getY() == y) return p;
        }
        return null;
    }

    public Batiment getBatimentAt(int x, int y) {
        return batimentsObjets.get(cle(x, y));
    }

    public List<Batiment> getBatimentsDefenseDisponibles() {
        List<Batiment> list = new ArrayList<>();
        for (Batiment b : batimentsObjets.values()) {
            if (b == null) continue;
            if (!Boolean.TRUE.equals(b.isConstruit())) continue;
            if (b == hotelDeVille) continue;
            if (b.getTypeStocke() == null) continue;
            list.add(b);
        }
        if (list.isEmpty()) {
            list.add(hotelDeVille);
        }

        return list;
    }

    public int getTerrainAt(int x, int y) {
        if (isValidPosition(x, y)) return terrain[y][x];
        return -1;
    }

    private List<Point> getCasesPourAction(ActionType action) {
        List<Point> result = new ArrayList<>();
        int terrainType;

        switch (action) {
            case COUPER_BOIS -> terrainType = FORET_BOIS;
            case MINER_FER   -> terrainType = MINE_FER;
            default -> {
                result.add(new Point(getCentreX(), getCentreY()));
                return result;
            }
        }

        for (int x = 0; x < width; x++) {
            for (int y = height - 1; y >= 0; y--) {
                if (terrain[y][x] == terrainType) result.add(new Point(x, y));
            }
        }
        return result;
    }

    /* setters */
    public void clearNotificationMessage() {
        notificationMessage = "";
    }

    public void setInventaire(Inventaire inv) {
        this.inventaire = inv;
        if (inventaire != null) {
            stockOr = inventaire.getOr();
            stockFer = inventaire.getFer();
        }
        recalculerCapacites();
    }

    public void setTerrainAt(int x, int y, int type) {
        if (isValidPosition(x, y)) terrain[y][x] = type;
    }

    /* fonctions */

    public void recalculerCapacites() {
        if (inventaire == null) return;

        boolean entrepotBois  = estConstruit(2,  6);
        boolean entrepotFer   = estConstruit(4, 13);
        boolean entrepotOr    = estConstruit(16, 13);
        boolean entrepotNourr = estConstruit(17, 5);

        inventaire.setCapacites(
                hotelDeVille.getCapaciteTotale(entrepotBois),
                hotelDeVille.getCapaciteTotale(entrepotFer),
                hotelDeVille.getCapaciteTotale(entrepotOr),
                hotelDeVille.getCapaciteTotale(entrepotNourr)
        );
    }

    /** Vérifie que tous les bâtiments (hors HdV, Autel et Statue) sont construits. */
    public boolean tousBatimentsConstructs() {
        for (java.util.Map.Entry<String, String> entry : typesBatiments.entrySet()) {
            String t = entry.getValue();
            if (TYPE_HOTEL_VILLE.equals(t))   continue;
            if (TYPE_AUTEL_INVOC.equals(t))   continue;
            if (TYPE_STATUE_DRAGON.equals(t)) continue;
            Boolean construit = batimentsConstruits.get(entry.getKey());
            if (construit == null || !construit) return false;
        }
        return true;
    }

    public boolean estStatueDragon(int x, int y) {
        return TYPE_STATUE_DRAGON.equals(getTypeBatiment(x, y));
    }

    public String ameliorerHotelDeVille() {
        if (hotelDeVille.estAuNiveauMax())
            return "L'Hôtel de Ville est déjà au niveau maximum !";

        int cout = hotelDeVille.getCoutAmelioration();
        if (inventaire == null || inventaire.getFer() < cout)
            return "Pas assez de fer ! Il faut " + cout + " fer.";

        inventaire.retirerRessource(Ressource.Type.FER, cout);
        hotelDeVille.monterNiveau();
        recalculerCapacites();

        return "Hôtel de Ville amélioré ! Niveau " + hotelDeVille.getNiveau();
    }

    public void construireBatimentEtRecalculer(int x, int y) {
        construireBatiment(x, y);
        recalculerCapacites();
    }

    public int[] getCoutConstruction(int x, int y) {
        String type = getTypeBatiment(x, y);
        if (type == null) return null;

        return switch (type) {
            case TYPE_MAISON         -> COUT_MAISON;
            case TYPE_TOUR_DEFENSE   -> COUT_TOUR_DEFENSE;
            case TYPE_ENTREPOT_BOIS  -> COUT_ENTREPOT_BOIS;
            case TYPE_ENTREPOT_FER   -> COUT_ENTREPOT_FER;
            case TYPE_ENTREPOT_OR    -> COUT_ENTREPOT_OR;
            case TYPE_ENTREPOT_NOURR -> COUT_ENTREPOT_NOURR;
            case TYPE_STATUE_DRAGON  -> COUT_STATUE_DRAGON;
            default -> null;
        };
    }

    public String tenterConstruction(int x, int y) {
        if (inventaire == null) return "Inventaire non initialisé.";

        String type = getTypeBatiment(x, y);

        // Conditions spéciales pour la Grande Statue du Dragon
        if (TYPE_STATUE_DRAGON.equals(type)) {
            if (hotelDeVille.getNiveau() < HotelDeVille.NIVEAU_MAX)
                return "L'Hôtel de Ville doit être au niveau " + HotelDeVille.NIVEAU_MAX + " (actuel : " + hotelDeVille.getNiveau() + ") !";
            if (!tousBatimentsConstructs())
                return "Tous les bâtiments doivent être construits avant d'ériger la statue !";
        }

        int[] cout = getCoutConstruction(x, y);
        if (cout == null) return "Ce bâtiment ne peut pas être construit.";

        if (inventaire.getBois() < cout[0]) return "Pas assez de bois !";
        if (inventaire.getFer()  < cout[1]) return "Pas assez de fer !";
        if (inventaire.getOr()   < cout[2]) return "Pas assez d'or !";

        if (cout[0] > 0) inventaire.retirerRessource(Ressource.Type.BOIS, cout[0]);
        if (cout[1] > 0) inventaire.retirerRessource(Ressource.Type.FER, cout[1]);
        if (cout[2] > 0) inventaire.retirerRessource(Ressource.Type.OR, cout[2]);

        construireBatimentEtRecalculer(x, y);
        return null;
    }

    public int getCoutInvocationActuel() {
        long cout = 10L << nombreInvocations;
        if (cout > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        return (int) cout;
    }

    public boolean peutInvoquer() {
        return getStockOr() >= getCoutInvocationActuel();
    }

    public boolean peutInvoquer(int ignored) {
        return peutInvoquer();
    }

    public Personnage invoquerPersonnage() {
        int cout = getCoutInvocationActuel();

        if (!peutInvoquer()) return null;

        if (inventaire != null) {
            boolean ok = inventaire.retirerRessource(Ressource.Type.OR, cout);
            if (!ok) return null;
        } else {
            if (stockOr < cout) return null;
            stockOr -= cout;
        }

        String[] noms = {"Arthur", "Lina", "Kael", "Mira", "Thorne", "Sylva", "Ronan", "Elya", "Darius", "Nyx"};
        String nom = noms[random.nextInt(noms.length)];
        int etoiles = tirerRarete();

        Personnage personnage = new Personnage(nom, etoiles, getCentreX(), getCentreY());
        personnages.add(personnage);

        nombreInvocations++;
        return personnage;
    }

    public Personnage invoquerPersonnage(int ignored) {
        return invoquerPersonnage();
    }

    private int tirerRarete() {
        if (compteurSansCinqEtoiles >= 9) {
            compteurSansCinqEtoiles = 0;
            return 5;
        }

        double r = random.nextDouble();
        int etoiles;
        if (r < 0.40) etoiles = 1;
        else if (r < 0.70) etoiles = 2;
        else if (r < 0.85) etoiles = 3;
        else if (r < 0.95) etoiles = 4;
        else etoiles = 5;

        if (etoiles == 5) compteurSansCinqEtoiles = 0;
        else compteurSansCinqEtoiles++;

        return etoiles;
    }

    public void deployerPersonnage(Personnage p, ActionType action) {
        if (p == null) return;

        if (!p.isDeploye()) {
            p.setPosition(hotelDeVille.getX(), hotelDeVille.getY());
            p.setDeploye(true);
        }

        p.interrompreAction();
        p.setPretARecuperer(false);
        p.setActionCourante(action);

        Point cible = trouverCibleLibrePourAction(action, p);
        p.setCible(cible.x, cible.y);

        this.actionMiseEnValeur = action;
        this.personnageMiseEnValeur = p;
    }

    public void deployerPersonnageDefense(Personnage p, Batiment cible) {
        if (p == null || cible == null) return;

        p.interrompreAction();
        p.setPretARecuperer(false);
        p.setActionCourante(ActionType.DEFENDRE);
        p.setBatimentDefenseCible(cible);
        p.setPosition(hotelDeVille.getX(), hotelDeVille.getY());
        p.setDeploye(true);
        p.setCible(cible.getX(), cible.getY());

        this.actionMiseEnValeur = ActionType.DEFENDRE;
        this.personnageMiseEnValeur = p;

        if (cible == hotelDeVille) {
            cible.setProtege(true);
            p.setDeploye(false);
            this.notificationMessage = p.getNom() + " protège immédiatement l'Hôtel de Ville.";
        } else {
            this.notificationMessage = "Défense envoyée vers " + cible.getNom()
                    + " (" + cible.getX() + "," + cible.getY() + ")";
        }
    }
    public void rappelerPersonnage(Personnage p) {
        if (p == null) return;

        p.rappeler();

        if (personnageMiseEnValeur == p) {
            personnageMiseEnValeur = null;
            actionMiseEnValeur = null;
        }
    }

    public int recupererRecompenseEtRappeler(Personnage p) {
        if (p == null || !p.isPretARecuperer() || p.getActionCourante() == null) return 0;

        int gain = 0;
        switch (p.getActionCourante()) {
            case COUPER_BOIS -> gain = calculerGainRessource(p, Ressource.Type.BOIS);
            case MINER_FER   -> gain = calculerGainRessource(p, Ressource.Type.FER);
            case DEFENDRE    -> gain = 0;
        }

        appliquerGain(p.getActionCourante(), gain);
        rappelerPersonnage(p);
        return gain;
    }

    private int calculerGainRessource(Personnage p, Ressource.Type type) {
        int e = p.getRareteEtoiles();
        return switch (type) {
            case OR -> 10 * e;
            case FER -> 20 * e;
            case NOURRITURE -> 30 * e;
            case BOIS -> 40 * e;
        };
    }

    private void appliquerGain(ActionType action, int gain) {
        if (action == null || gain <= 0) return;

        switch (action) {
            case COUPER_BOIS -> {
                stockBoisForet = Math.max(0, stockBoisForet - gain);
                if (inventaire != null) inventaire.ajouterRessource(Ressource.Type.BOIS, gain);
                else if (ressourceListener != null) ressourceListener.onRessourceGagnee(Ressource.Type.BOIS, gain);
            }
            case MINER_FER -> {
                if (inventaire != null) inventaire.ajouterRessource(Ressource.Type.FER, gain);
                else if (ressourceListener != null) ressourceListener.onRessourceGagnee(Ressource.Type.FER, gain);
            }
            case DEFENDRE -> {
            }
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

    public void placerBatiment(int x, int y) {
        setTerrainAt(x, y, BATIMENT);
    }

    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
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

    private Point trouverCibleLibrePourAction(ActionType action, Personnage courant) {
        List<Point> cases = getCasesPourAction(action);
        for (Point pt : cases) {
            if (!caseOccupeePourAction(pt.x, pt.y, action, courant)) return pt;
        }
        if (!cases.isEmpty()) return cases.get(0);
        return new Point(getCentreX(), getCentreY());
    }

    private boolean caseOccupeePourAction(int x, int y, ActionType action, Personnage courant) {
        for (Personnage p : personnages) {
            if (p == courant || !p.isDeploye() || p.getActionCourante() != action) continue;
            if (p.isPretARecuperer()) continue;
            if (p.getCibleX() == x && p.getCibleY() == y) return true;
        }
        return false;
    }

    private void initializeDefaultMap() {
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                terrain[y][x] = HERBE;

        for (int y = 0; y < 5; y++)
            for (int x = 0; x < 7; x++)
                setTerrainAt(x, y, FORET_BOIS);

        for (int y = 0; y < 4; y++)
            for (int x = width - 4; x < width; x++)
                setTerrainAt(x, y, CUEILLETTE);

        for (int y = height - 3; y < height; y++)
            for (int x = 0; x < 3; x++)
                setTerrainAt(x, y, MINE_FER);

        for (int y = height - 2; y < height; y++)
            for (int x = width - 2; x < width; x++)
                setTerrainAt(x, y, GISEMENT_OR);

        setTerrainAt(10, 7, BATIMENT);
        setTerrainAt(10, 3, BATIMENT);
        setTerrainAt(10, 11, BATIMENT);
        setTerrainAt(6, 7, BATIMENT);
        setTerrainAt(14, 7, BATIMENT);
        setTerrainAt(7, 4, BATIMENT);
        setTerrainAt(13, 4, BATIMENT);
        setTerrainAt(7, 10, BATIMENT);
        setTerrainAt(13, 10, BATIMENT);
        setTerrainAt(10, 13, BATIMENT);

        setTerrainAt(10, 5, BATIMENT);
        setTerrainAt(2, 8, BATIMENT);
        setTerrainAt(6, 13, BATIMENT);
        setTerrainAt(14, 13, BATIMENT);
        setTerrainAt(17, 7, BATIMENT);

        setTerrainAt(2, 6, BATIMENT);
        setTerrainAt(4, 13, BATIMENT);
        setTerrainAt(16, 13, BATIMENT);
        setTerrainAt(17, 5, BATIMENT);

        setTerrainAt(10, 9, BATIMENT); // Grande Statue du Dragon
    }

    private void initBatiments() {
        placerBatimentAvecType(10, 7, TYPE_HOTEL_VILLE, true, hotelDeVille);

        placerBatimentAvecType(10, 3, TYPE_MAISON, false, new Maison("Maison", 200, 10, 3, 0, 0, 0));
        placerBatimentAvecType(10, 11, TYPE_MAISON, false, new Maison("Maison", 200, 10, 11, 0, 0, 0));
        placerBatimentAvecType(6, 7, TYPE_MAISON, false, new Maison("Maison", 200, 6, 7, 0, 0, 0));
        placerBatimentAvecType(14, 7, TYPE_MAISON, false, new Maison("Maison", 200, 14, 7, 0, 0, 0));
        placerBatimentAvecType(7, 4, TYPE_MAISON, false, new Maison("Maison", 200, 7, 4, 0, 0, 0));
        placerBatimentAvecType(13, 4, TYPE_MAISON, false, new Maison("Maison", 200, 13, 4, 0, 0, 0));
        placerBatimentAvecType(7, 10, TYPE_MAISON, false, new Maison("Maison", 200, 7, 10, 0, 0, 0));
        placerBatimentAvecType(13, 10, TYPE_MAISON, false, new Maison("Maison", 200, 13, 10, 0, 0, 0));

        placerBatimentAvecType(10, 5, TYPE_TOUR_DEFENSE, false, new TourDefense("Tour de Défense", 200, 10, 5, 0, 0, 0));
        placerBatimentAvecType(2, 8, TYPE_TOUR_DEFENSE, false, new TourDefense("Tour de Défense", 200, 2, 8, 0, 0, 0));
        placerBatimentAvecType(6, 13, TYPE_TOUR_DEFENSE, false, new TourDefense("Tour de Défense", 200, 6, 13, 0, 0, 0));
        placerBatimentAvecType(14, 13, TYPE_TOUR_DEFENSE, false, new TourDefense("Tour de Défense", 200, 14, 13, 0, 0, 0));
        placerBatimentAvecType(17, 7, TYPE_TOUR_DEFENSE, false, new TourDefense("Tour de Défense", 200, 17, 7, 0, 0, 0));

        placerBatimentAvecType(2, 6, TYPE_ENTREPOT_BOIS, false, new EntrepotBois("Entrepôt Bois", 300, 2, 6, 0, 0, 0));
        placerBatimentAvecType(4, 13, TYPE_ENTREPOT_FER, false, new EntrepotFer("Entrepôt Fer", 300, 4, 13, 0, 0, 0));
        placerBatimentAvecType(16, 13, TYPE_ENTREPOT_OR, false, new Tresorerie("Trésorerie", 300, 16, 13, 0, 0, 0));
        placerBatimentAvecType(17, 5, TYPE_ENTREPOT_NOURR, false, new Grenier("Grenier", 300, 17, 5, 0, 0, 0));

        placerBatimentAvecType(autelX, autelY, TYPE_AUTEL_INVOC, true, autelInvocation);

        placerBatimentAvecType(10, 9, TYPE_STATUE_DRAGON, false,
                new GrandeStatueDragon("Grande Statue du Dragon", 9999, 10, 9, 25000, 0, 0));
    }

    private void placerBatimentAvecType(int x, int y, String type, boolean construit, Batiment batiment) {
        String cle = cle(x, y);
        typesBatiments.put(cle, type);
        batimentsConstruits.put(cle, construit);
        batimentsObjets.put(cle, batiment);

        if (construit && batiment != null) {
            batiment.construire();
        }
    }

    public String getTypeBatiment(int x, int y) {
        return typesBatiments.get(cle(x, y));
    }

    public boolean estConstruit(int x, int y) {
        Boolean b = batimentsConstruits.get(cle(x, y));
        return b != null && b;
    }

    public void construireBatiment(int x, int y) {
        String c = cle(x, y);
        if (batimentsConstruits.containsKey(c)) {
            batimentsConstruits.put(c, true);
            Batiment b = batimentsObjets.get(c);
            if (b != null) b.construire();
        }
    }

    private String cle(int x, int y) {
        return x + "," + y;
    }

    private void demarrerThreadJeu() {
        Thread t = new Thread(() -> {
            while (true) {
                mettreAJourPersonnages();
                mettreAJourVoleurs();
                tenterSpawnVoleur();

                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    private void mettreAJourPersonnages() {
        for (Personnage p : personnages) {
            if (p.isEnSoin()) {
                p.mettreAJourSoin();
                continue;
            }

            if (!p.isDeploye() || p.getActionCourante() == null) continue;

            if (p.getActionCourante() == ActionType.DEFENDRE) {
                mettreAJourDefense(p);
                continue;
            }

            if (!p.estArrive()) {
                p.avancerVersCible();
            } else {
                if (!p.isEnExecution() && !p.isPretARecuperer()) {
                    p.commencerExecution();
                } else if (p.isEnExecution()) {
                    boolean fini = p.mettreAJourExecution();
                    if (fini) {
                        notificationMessage = p.getNom() + " a terminé sa mission. Clique sur lui pour récupérer.";
                    }
                }
            }
        }
    }

    private void mettreAJourDefense(Personnage p) {
        Batiment cible = p.getBatimentDefenseCible();
        if (cible == null) return;

        if (!p.estArrive()) {
            p.avancerVersCible();

            if (cible.isEnAttaque()) {
                for (Voleur v : voleurs) {
                    if (!v.isActif() || v.getCible() != cible) continue;

                    if (v.estArrive()) {
                        p.setChoixApresVolRequis(true);
                        p.setDeploye(false);
                        notificationMessage = p.getNom()
                                + " est arrivé trop tard. Clique sur lui : retour ou continuer défendre.";
                        break;
                    }
                }
            }
            return;
        }

        cible.setProtege(true);
        p.setDeploye(false);
        notificationMessage = cible.getNom() + " est maintenant protégé par " + p.getNom() + ".";
    }

    private void demarrerThreadRegenerationForet() {
        Thread t = new Thread(() -> {
            while (true) {
                if (stockBoisForet < stockBoisForetMax) stockBoisForet++;
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });

        t.setDaemon(true);
        t.start();
    }

    /* fonction en lien avec les voleurs */
    private void mettreAJourVoleurs() {
        List<Voleur> aSupprimer = new ArrayList<>();

        for (Voleur v : voleurs) {
            if (!v.isActif()) {
                if (v.getCible() != null) {
                    v.getCible().setEnAttaque(false);
                }
                aSupprimer.add(v);
                continue;
            }

            if (!v.estArrive()) {
                v.avancerVersCible();
            } else {
                resoudreVolOuDefense(v);
                aSupprimer.add(v);
            }
        }

        voleurs.removeAll(aSupprimer);
    }

    private void tenterSpawnVoleur() {
        long now = System.currentTimeMillis();
        if (now - dernierSpawnVoleur < INTERVALLE_SPAWN_VOLEUR) return;

        List<Batiment> cibles = getBatimentsDefenseDisponibles();
        if (cibles.isEmpty()) return;

        // Appliquer la probabilité d'apparition selon jour/nuit : 20% jour, 80% nuit
        double probabilite = jourNuit.getIsDay() ? 0.20 : 0.80;
        if (random.nextDouble() > probabilite) return;

        dernierSpawnVoleur = now;
        spawnVoleur();
    }

    private void spawnVoleur() {
        List<Batiment> cibles = getBatimentsDefenseDisponibles();
        if (cibles.isEmpty()) return;

        Batiment cible = cibles.get(random.nextInt(cibles.size()));

        // 如果目标是 Hotel de Ville，可以直接从边缘刷一个小偷过去
        int side = random.nextInt(4);
        int x, y;

        switch (side) {
            case 0 -> { x = 0; y = random.nextInt(height); }
            case 1 -> { x = width - 1; y = random.nextInt(height); }
            case 2 -> { x = random.nextInt(width); y = 0; }
            default -> { x = random.nextInt(width); y = height - 1; }
        }

        Voleur v = new Voleur(x, y);
        v.setCible(cible);
        cible.setEnAttaque(true);
        voleurs.add(v);

        notificationMessage = "Alerte ! Un voleur va attaquer " + cible.getNom()
                + " en (" + cible.getX() + "," + cible.getY() + ")";
    }
    private void resoudreVolOuDefense(Voleur v) {
        Batiment cible = v.getCible();
        if (cible == null) return;

        Personnage defenseur = trouverDefenseurPourBatiment(cible);

        if (defenseur == null) {
            appliquerVol(v, cible, v.getAtk());
            return;
        }

        cible.setProtege(true);

        if (defenseur.getHpActuel() >= v.getAtk()) {
            defenseur.setHpActuel(defenseur.getHpActuel() - v.getAtk());
            notificationMessage = "Défense réussie sur " + cible.getNom() + " par " + defenseur.getNom() + ".";
        } else {
            int reste = v.getAtk() - defenseur.getHpActuel();
            defenseur.setHpActuel(0);
            appliquerVol(v, cible, reste);
            notificationMessage = "Défense échouée sur " + cible.getNom() + ". Ressources perdues.";
        }

        cible.setEnAttaque(false);
        cible.setProtege(false);

        defenseur.setPosition(hotelDeVille.getX(), hotelDeVille.getY());
        defenseur.commencerSoin();

        v.setActif(false);
    }

    private Personnage trouverDefenseurPourBatiment(Batiment cible) {
        for (Personnage p : personnages) {
            if (p.getBatimentDefenseCible() != cible) continue;
            if (p.getActionCourante() != ActionType.DEFENDRE) continue;

            if (cible == hotelDeVille) {
                return p;
            }

            if ((p.getX() == cible.getX() && p.getY() == cible.getY()) || cible.isProtege()) {
                return p;
            }

            if (p.isContinuerDefenseApresVol()) {
                return p;
            }
        }
        return null;
    }

    private void appliquerVol(Voleur v, Batiment cible, int atkEffectif) {
        if (inventaire == null) {
            cible.setEnAttaque(false);
            v.setActif(false);
            return;
        }

        Ressource.Type type = cible.getTypeStocke();

        if (cible == hotelDeVille) {
            Ressource.Type[] types = {
                    Ressource.Type.OR,
                    Ressource.Type.FER,
                    Ressource.Type.NOURRITURE,
                    Ressource.Type.BOIS
            };
            type = types[random.nextInt(types.length)];
        }

        if (type == null) {
            cible.setEnAttaque(false);
            v.setActif(false);
            return;
        }

        int multiplicateur = switch (type) {
            case OR -> 1;
            case FER -> 2;
            case NOURRITURE -> 3;
            case BOIS -> 4;
        };

        int perte = atkEffectif * multiplicateur;
        inventaire.retirerRessource(type, perte);

        cible.setEnAttaque(false);
        cible.setProtege(false);
        v.setVoleReussi(true);
        v.setActif(false);

        notificationMessage = "Vol réussi sur " + cible.getNom() + " : -" + perte + " " + type;
    }

    public void choisirRetourApresVol(Personnage p, boolean continuer) {
        if (p == null) return;

        p.setChoixApresVolRequis(false);
        p.setContinuerDefenseApresVol(continuer);

        Batiment cible = p.getBatimentDefenseCible();

        if (!continuer || cible == null) {
            rappelerPersonnage(p);
            notificationMessage = p.getNom() + " retourne à l'hôtel de ville.";
            return;
        }

        p.setPosition(cible.getX(), cible.getY());
        cible.setProtege(true);
        notificationMessage = p.getNom() + " continue à protéger " + cible.getNom() + ".";
    }

    public Voleur getVoleurAt(int x, int y) {
        for (Voleur v : voleurs) {
            if (v != null && v.isActif() && v.getX() == x && v.getY() == y) {
                return v;
            }
        }
        return null;
    }
}
