package model;

/**
 * Ici, on liste les actions que nos personnages peuvent effectuer.
 * L'idée d'utiliser un Enum, c'est d'avoir un code propre et d'éviter les erreurs
 * de frappe qu'on aurait si on utilisait de simples chaînes de caractères (Strings).
 */
public enum ActionType {
    /* Action pour envoyer le villageois à la forêt */
    COUPER_BOIS("Couper du bois"),

    /* Action pour mettre le personnage en mode garde/protection */
    DEFENDRE("Defendre");

    // Le "label" sert à afficher un texte sympa dans l'interface graphique (boutons, tooltips)
    private final String label;

    ActionType(String label) {
        this.label = label;
    }

    /**
     * Petite méthode pour récupérer le nom de l'action proprement.
     * C'est ce qu'on appelle dans le JOptionPane du contrôleur.
     */
    public String getLabel() {
        return label;
    }
}