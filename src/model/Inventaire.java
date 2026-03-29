package model;

public class Inventaire {

    private int bois       = 1000;
    private int fer        = 750;
    private int or         = 500;
    private int nourriture = 750;

    // Capacités max par ressource (-1 = illimité)
    private int maxBois       = 2000;
    private int maxFer        = 750;
    private int maxOr         = 500;
    private int maxNourriture = 750;

    public interface OnInventaireChangeListener {
        void onInventaireChange(int bois, int fer, int or, int nourriture);
    }

    private OnInventaireChangeListener listener;

    public void setOnInventaireChangeListener(OnInventaireChangeListener listener) {
        this.listener = listener;
        notifierListener();
    }

    public void setCapacites(int maxBois, int maxFer, int maxOr, int maxNourriture) {
        this.maxBois       = maxBois;
        this.maxFer        = maxFer;
        this.maxOr         = maxOr;
        this.maxNourriture = maxNourriture;
        if (maxBois       >= 0) this.bois       = Math.min(this.bois,       maxBois);
        if (maxFer        >= 0) this.fer        = Math.min(this.fer,        maxFer);
        if (maxOr         >= 0) this.or         = Math.min(this.or,         maxOr);
        if (maxNourriture >= 0) this.nourriture = Math.min(this.nourriture, maxNourriture);
        notifierListener();
    }

    public void ajouterRessource(Ressource.Type type, int quantite) {
        switch (type) {
            case BOIS       -> bois       = (maxBois < 0)       ? bois + quantite       : Math.min(bois + quantite,       maxBois);
            case FER        -> fer        = (maxFer < 0)        ? fer + quantite        : Math.min(fer + quantite,        maxFer);
            case OR         -> or         = (maxOr < 0)         ? or + quantite         : Math.min(or + quantite,         maxOr);
            case NOURRITURE -> nourriture = (maxNourriture < 0) ? nourriture + quantite : Math.min(nourriture + quantite, maxNourriture);
        }
        notifierListener();
    }

    public boolean retirerRessource(Ressource.Type type, int quantite) {
        switch (type) {
            case BOIS       -> { if (bois       < quantite) return false; bois       -= quantite; }
            case FER        -> { if (fer        < quantite) return false; fer        -= quantite; }
            case OR         -> { if (or         < quantite) return false; or         -= quantite; }
            case NOURRITURE -> { if (nourriture < quantite) return false; nourriture -= quantite; }
        }
        notifierListener();
        return true;
    }

    private void notifierListener() {
        if (listener != null)
            listener.onInventaireChange(bois, fer, or, nourriture);
    }

    public int getBois()          { return bois;          }
    public int getFer()           { return fer;           }
    public int getOr()            { return or;            }
    public int getNourriture()    { return nourriture;    }
    public int getMaxBois()       { return maxBois;       }
    public int getMaxFer()        { return maxFer;        }
    public int getMaxOr()         { return maxOr;         }
    public int getMaxNourriture() { return maxNourriture; }
}