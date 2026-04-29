package model;

public class CheatCode {

    private final Inventaire inventaire;
    private final Map gameMap;

    public CheatCode(Inventaire inventaire, Map gameMap) {
        this.inventaire = inventaire;
        this.gameMap    = gameMap;
    }

    public String appliquer(String code) {
        if (code == null) return "Code invalide.";
        switch (code.trim().toLowerCase()) {

            case "jesuisriche" -> {
                // Remplit chaque ressource à son max (ou 50 000 si illimité)
                int maxBois  = inventaire.getMaxBois();
                int maxFer   = inventaire.getMaxFer();
                int maxOr    = inventaire.getMaxOr();
                int maxNourr = inventaire.getMaxNourriture();
                inventaire.ajouterRessource(Ressource.Type.BOIS,       maxBois  < 0 ? 50000 : maxBois);
                inventaire.ajouterRessource(Ressource.Type.FER,        maxFer   < 0 ? 50000 : maxFer);
                inventaire.ajouterRessource(Ressource.Type.OR,         maxOr    < 0 ? 50000 : maxOr);
                inventaire.ajouterRessource(Ressource.Type.NOURRITURE, maxNourr < 0 ? 50000 : maxNourr);
                return " Ressources remplies au maximum !";
            }

            case "construittout" -> {
                // Construit tous les bâtiments sauf la statue du dragon
                for (int x = 0; x < 20; x++) {
                    for (int y = 0; y < 15; y++) {
                        String type = gameMap.getTypeBatiment(x, y);
                        if (type == null) continue;
                        if (type.equals(Map.TYPE_STATUE_DRAGON)) continue;
                        if (type.equals(Map.TYPE_HOTEL_VILLE))   continue;
                        if (type.equals(Map.TYPE_AUTEL_INVOC))   continue;
                        if (!gameMap.estConstruit(x, y)) {
                            gameMap.construireBatimentEtRecalculer(x, y);
                        }
                    }
                }
                return " Tous les bâtiments sont construits !";
            }

            case "niveaumax" -> {
                // Monte l'Hôtel de Ville au niveau 5 directement
                model.HotelDeVille hdv = gameMap.getHotelDeVille();
                while (!hdv.estAuNiveauMax()) {
                    hdv.monterNiveau();
                }
                gameMap.recalculerCapacites();
                return "✅ Hôtel de Ville monté au niveau " + hdv.getNiveau() + " !";
            }

            case "jaifaim" -> {
                // Réduit la nourriture à 30
                int nourr = inventaire.getNourriture();
                if (nourr > 30) inventaire.retirerRessource(Ressource.Type.NOURRITURE, nourr - 30);
                return " Nourriture réduite à 30 !";
            }

            case "plusletime" -> {
                // Met le timer à 10 secondes
                gameMap.setTempsRestantMs(10_000L);
                return " Timer mis à 10 secondes !";
            }

            default -> { return " Code inconnu."; }
        }
    }
}