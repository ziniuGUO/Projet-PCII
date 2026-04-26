package model;

/**
 * Actions simples possibles pour un personnage (version minimale).
 */
public enum ActionType {
    COUPER_BOIS("Couper du bois"),
    MINER_FER("Miner du fer"),
    DEFENDRE("Defendre / Veiller"),
    CHERCHER_NOURRITURE("chercher nourriture"),
    CHERCHER_OR("Miner de l'or");
    private final String label;

    ActionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}