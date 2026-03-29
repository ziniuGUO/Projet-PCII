package model;

public class CheatCode {

    private final Inventaire inventaire;

    public CheatCode(Inventaire inventaire, Map gameMap) {
        this.inventaire = inventaire;
    }

    public String appliquer(String code) {
        if (code == null) return "Code invalide.";
        switch (code.trim().toLowerCase()) {

            case "jesuisriche" -> {
                // Remplit chaque ressource à son max actuel
                int maxBois  = inventaire.getMaxBois();
                int maxFer   = inventaire.getMaxFer();
                int maxOr    = inventaire.getMaxOr();
                int maxNourr = inventaire.getMaxNourriture();
                if (maxBois  > 0) inventaire.ajouterRessource(Ressource.Type.BOIS,       maxBois);
                if (maxFer   > 0) inventaire.ajouterRessource(Ressource.Type.FER,        maxFer);
                if (maxOr    > 0) inventaire.ajouterRessource(Ressource.Type.OR,         maxOr);
                if (maxNourr > 0) inventaire.ajouterRessource(Ressource.Type.NOURRITURE, maxNourr);
                return "✅ Ressources remplies au maximum !";
            }

            default -> { return "❌ Code inconnu."; }
        }
    }
}