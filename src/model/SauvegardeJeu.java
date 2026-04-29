package model;

import java.io.*;
import java.util.HashMap;
import java.util.List;

/**
 * Données sérialisables représentant l'état complet d'une partie.
 */
public class SauvegardeJeu implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String FICHIER = "sauvegarde.dat";

    // ── État de la partie ─────────────────────────────────────────────────────
    public boolean partieEnCours = true;
    public long tempsRestantMs = 3 * 60 * 60 * 1000L;
    public long dureeTotaleMs  = 3 * 60 * 60 * 1000L;

    // Inventaire
    public int bois;
    public int fer;
    public int or;
    public int nourriture;

    // HotelDeVille
    public int niveauHdV;

    // Bâtiments construits (clé = "x,y")
    public HashMap<String, Boolean> batimentsConstruits;

    // Personnages
    public List<DonneesPersonnage> personnages;

    // Compteurs invocation
    public int nombreInvocations;
    public int compteurSansCinqEtoiles;

    // Stock forêt
    public int stockBoisForet;

    // ── Données personnage ────────────────────────────────────────────────────

    public static class DonneesPersonnage implements Serializable {
        private static final long serialVersionUID = 1L;
        public String nom;
        public int rareteEtoiles;
        public int hpActuel;
        public int x;
        public int y;
    }

    // ── Sauvegarde / Chargement ───────────────────────────────────────────────

    public static void sauvegarder(SauvegardeJeu save) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(FICHIER))) {
            oos.writeObject(save);
        }
    }

    public static SauvegardeJeu charger() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(FICHIER))) {
            return (SauvegardeJeu) ois.readObject();
        }
    }

    public static boolean sauvegardeExiste() {
        return new File(FICHIER).exists();
    }
}