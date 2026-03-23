package model;

public class Inventaire {

    private int bois       = 300;
    private int fer        = 200;
    private int or         = 500;
    private int nourriture = 100;

    public interface OnInventaireChangeListener {
        void onInventaireChange(int bois, int fer, int or, int nourriture);
    }

    private OnInventaireChangeListener listener;

    public void setOnInventaireChangeListener(OnInventaireChangeListener listener) {
        this.listener = listener;
        notifierListener();
    }

    public void ajouterRessource(Ressource.Type type, int quantite) {
        switch (type) {
            case BOIS       -> bois       += quantite;
            case FER        -> fer        += quantite;
            case OR         -> or         += quantite;
            case NOURRITURE -> nourriture += quantite;
        }
        notifierListener();
    }

    public boolean retirerRessource(Ressource.Type type, int quantite) {
        switch (type) {
            case BOIS -> {
                if (bois < quantite) return false;
                bois -= quantite;
            }
            case FER -> {
                if (fer < quantite) return false;
                fer -= quantite;
            }
            case OR -> {
                if (or < quantite) return false;
                or -= quantite;
            }
            case NOURRITURE -> {
                if (nourriture < quantite) return false;
                nourriture -= quantite;
            }
        }
        notifierListener();
        return true;
    }

    private void notifierListener() {
        if (listener != null)
            listener.onInventaireChange(bois, fer, or, nourriture);
    }

    public int getBois()       { return bois;       }
    public int getFer()        { return fer;        }
    public int getOr()         { return or;         }
    public int getNourriture() { return nourriture; }
}