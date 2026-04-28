package model;

public class HotelDeVille extends Batiment {

    protected int cadenceAttaque   = 1;
    protected int puissanceAttaque = 20;
    protected int porteeAttaque    = 2;

    public static final int NIVEAU_MAX = 5;

    // Capacité de base fournie par l'HdV seul (index = niveau-1)
    private static final int[] CAPACITES_HDV = {2000, 3000, 5000, 8000, 12000};

    // Bonus de capacité ajouté par chaque entrepôt construit (index = niveau-1)
    // -1 = illimité
    private static final int[] BONUS_ENTREPOT = {500, 1000, 2000, 4000, -1};

    // Coût en fer pour passer au niveau suivant
    private static final int[] COUTS_AMELIORATION = {1500, 3000, 5000, 8000};

    private int niveau = 1;

    public HotelDeVille(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
        this.estConstruit = true;
    }

    public int getNiveau() { return niveau; }

    public int getCoutAmelioration() {
        if (niveau >= NIVEAU_MAX) return 0;
        return COUTS_AMELIORATION[niveau - 1];
    }

    /** Capacité de base de l'HdV (sans entrepôt). */
    public int getCapaciteBase() {
        return CAPACITES_HDV[niveau - 1];
    }

    /**
     * Capacité totale pour une ressource donnée.
     * @param entrepotConstruit true si le bâtiment de stockage correspondant est construit
     */
    public int getCapaciteTotale(boolean entrepotConstruit) {
        if (!entrepotConstruit) return CAPACITES_HDV[niveau - 1];
        int bonus = BONUS_ENTREPOT[niveau - 1];
        if (bonus < 0) return -1; // illimité
        return CAPACITES_HDV[niveau - 1] + bonus;
    }

    public boolean monterNiveau() {
        if (niveau >= NIVEAU_MAX) return false;
        niveau++;
        return true;
    }

    public boolean estAuNiveauMax() { return niveau >= NIVEAU_MAX; }
}