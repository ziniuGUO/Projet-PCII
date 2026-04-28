package model;

public class Personnage {

    public static final long DUREE_ACTION = 15000L;
    public static final long DUREE_SOIN = 5000L;

    private final String nom;
    private final int rareteEtoiles;

    private int x;
    private int y;
    private int cibleX;
    private int cibleY;

    private boolean deploye;
    private ActionType actionCourante = null;

    private boolean enExecution = false;
    private boolean pretARecuperer = false;
    private long debutExecution = 0L;

    private final int vitesse;
    private final int hpMax;
    private int hpActuel;

    private boolean enSoin = false;
    private long debutSoin = 0L;

    private Batiment batimentDefenseCible;
    private boolean continuerDefenseApresVol = false;
    private boolean choixApresVolRequis = false;

    public Personnage(String nom, int rareteEtoiles, int x, int y) {
        this.nom = nom;
        this.rareteEtoiles = rareteEtoiles;
        this.x = x;
        this.y = y;
        this.cibleX = x;
        this.cibleY = y;
        this.deploye = false;

        this.vitesse = rareteEtoiles;
        this.hpMax = rareteEtoiles * 10;
        this.hpActuel = hpMax;
    }

    public String getNom() {
        return nom;
    }

    public int getRareteEtoiles() {
        return rareteEtoiles;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCibleX() {
        return cibleX;
    }

    public int getCibleY() {
        return cibleY;
    }

    public void setCible(int x, int y) {
        this.cibleX = x;
        this.cibleY = y;
    }

    public boolean isDeploye() {
        return deploye;
    }

    public void setDeploye(boolean deploye) {
        this.deploye = deploye;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public ActionType getActionCourante() {
        return actionCourante;
    }

    public void setActionCourante(ActionType actionCourante) {
        this.actionCourante = actionCourante;
    }

    public int getVitesse() {
        return vitesse;
    }

    public int getHpMax() {
        return hpMax;
    }

    public int getHpActuel() {
        return hpActuel;
    }

    public void setHpActuel(int hpActuel) {
        this.hpActuel = Math.max(0, Math.min(hpActuel, hpMax));
    }

    public Batiment getBatimentDefenseCible() {
        return batimentDefenseCible;
    }

    public void setBatimentDefenseCible(Batiment batimentDefenseCible) {
        this.batimentDefenseCible = batimentDefenseCible;
    }

    public boolean isContinuerDefenseApresVol() {
        return continuerDefenseApresVol;
    }

    public void setContinuerDefenseApresVol(boolean continuerDefenseApresVol) {
        this.continuerDefenseApresVol = continuerDefenseApresVol;
    }

    public boolean isChoixApresVolRequis() {
        return choixApresVolRequis;
    }

    public void setChoixApresVolRequis(boolean choixApresVolRequis) {
        this.choixApresVolRequis = choixApresVolRequis;
    }

    public boolean estArrive() {
        return x == cibleX && y == cibleY;
    }

    public void avancerVersCible() {
        int pas = vitesse;
        while (pas > 0 && !estArrive()) {
            if (x < cibleX) x++;
            else if (x > cibleX) x--;
            else if (y < cibleY) y++;
            else if (y > cibleY) y--;
            pas--;
        }
    }

    public boolean isEnExecution() {
        return enExecution;
    }

    public boolean isPretARecuperer() {
        return pretARecuperer;
    }

    public void setPretARecuperer(boolean pretARecuperer) {
        this.pretARecuperer = pretARecuperer;
    }

    public void commencerExecution() {
        this.enExecution = true;
        this.pretARecuperer = false;
        this.debutExecution = System.currentTimeMillis();
    }

    public boolean mettreAJourExecution() {
        if (!enExecution) return false;
        if (System.currentTimeMillis() - debutExecution >= DUREE_ACTION) {
            enExecution = false;
            pretARecuperer = true;
            return true;
        }
        return false;
    }

    public void interrompreAction() {
        this.enExecution = false;
        this.pretARecuperer = false;
        this.debutExecution = 0L;
    }

    public boolean isEnSoin() {
        return enSoin;
    }

    public void commencerSoin() {
        this.enSoin = true;
        this.debutSoin = System.currentTimeMillis();
        this.deploye = false;
        this.actionCourante = null;
        this.pretARecuperer = false;
        this.enExecution = false;
        this.batimentDefenseCible = null;
        this.choixApresVolRequis = false;
        this.continuerDefenseApresVol = false;
    }

    public boolean mettreAJourSoin() {
        if (!enSoin) return false;
        if (System.currentTimeMillis() - debutSoin >= DUREE_SOIN) {
            enSoin = false;
            hpActuel = hpMax;
            return true;
        }
        return false;
    }

    public void rappeler() {
        this.deploye = false;
        this.actionCourante = null;
        this.enExecution = false;
        this.pretARecuperer = false;
        this.debutExecution = 0L;
        this.cibleX = x;
        this.cibleY = y;
        this.batimentDefenseCible = null;
        this.choixApresVolRequis = false;
        this.continuerDefenseApresVol = false;
    }
    public boolean estOccupe() {
        return deploye
                || enExecution
                || pretARecuperer
                || enSoin
                || actionCourante != null
                || choixApresVolRequis;
    }

    public boolean estDisponible() {
        return !estOccupe();
    }

    @Override
    public String toString() {
        return nom + " - " + rareteEtoiles + "★";
    }
}