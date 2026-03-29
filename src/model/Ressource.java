package model;

/**
 * Represente une source de ressource sur la map (foret, mine, gisement, zone de cueillette).
 * Chaque source a une quantite disponible que les personnages peuvent recolter.
 */
public class Ressource {

    // ── Types de ressources ────────────────────────────────────────────────────
    public enum Type {
        BOIS,        // Foret
        FER,         // Mine
        OR,          // Gisement
        NOURRITURE   // Zone de cueillette
    }

    // ── Attributs ──────────────────────────────────────────────────────────────
    private final Type type;
    private int quantiteActuelle;
    private final int quantiteMax;
    private final int tauxRegeneration; // Quantite regeneree par cycle (pour plus tard)

    // ── Constructeur ───────────────────────────────────────────────────────────
    
    /**
     * Cree une ressource avec quantite illimitee (pour le moment).
     */
    public Ressource(Type type) {
        this.type = type;
        this.quantiteActuelle = Integer.MAX_VALUE; // Illimite pour le moment
        this.quantiteMax = Integer.MAX_VALUE;
        this.tauxRegeneration = 0; // Pas de regen pour le moment
    }

    /**
     * Cree une ressource avec quantites definies (pour plus tard).
     */
    public Ressource(Type type, int quantiteMax, int tauxRegeneration) {
        this.type = type;
        this.quantiteActuelle = quantiteMax;
        this.quantiteMax = quantiteMax;
        this.tauxRegeneration = tauxRegeneration;
    }

    // ── Methodes de recolte ────────────────────────────────────────────────────

    /**
     * Recolte une certaine quantite de cette ressource.
     * @param quantite Quantite a recolter
     * @return Quantite reellement recoltee (peut etre moins si pas assez dispo)
     */
    public int recolter(int quantite) {
        if (quantiteActuelle == Integer.MAX_VALUE) {
            // Illimite : retourne toujours la quantite demandee
            return quantite;
        }
        
        int recoltee = Math.min(quantite, quantiteActuelle);
        quantiteActuelle -= recoltee;
        return recoltee;
    }

    /**
     * Verifie si la ressource est epuisee.
     */
    public boolean estVide() {
        return quantiteActuelle == 0;
    }

    /**
     * Verifie si la ressource est disponible (au moins 1 unite).
     */
    public boolean estDisponible() {
        return quantiteActuelle > 0;
    }

    // ── Regeneration (pour thread plus tard) ──────────────────────────────────

    /**
     * Regenere la ressource selon le taux de regeneration.
     * Appelee periodiquement par un thread.
     */
    public void regenerer() {
        if (quantiteActuelle < quantiteMax) {
            quantiteActuelle = Math.min(quantiteActuelle + tauxRegeneration, quantiteMax);
        }
    }

    /**
     * Recharge completement la ressource a son maximum.
     */
    public void recharger() {
        quantiteActuelle = quantiteMax;
    }

    // ── Getters ────────────────────────────────────────────────────────────────

    public Type getType() {
        return type;
    }

    public int getQuantiteActuelle() {
        return quantiteActuelle;
    }

    public int getQuantiteMax() {
        return quantiteMax;
    }

    public int getTauxRegeneration() {
        return tauxRegeneration;
    }

    /**
     * Retourne le pourcentage de ressource restante (0-100).
     * Utile pour afficher une barre de progression.
     */
    public int getPourcentageRestant() {
        if (quantiteMax == Integer.MAX_VALUE) return 100; // Illimite
        return (int) ((quantiteActuelle * 100.0) / quantiteMax);
    }

    // ── Debug ──────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        if (quantiteActuelle == Integer.MAX_VALUE) {
            return type + " (illimite)";
        }
        return type + " (" + quantiteActuelle + "/" + quantiteMax + ")";
    }
}