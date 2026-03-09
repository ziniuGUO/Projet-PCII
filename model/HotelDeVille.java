package model;

// Classe représentant l'Hôtel de Ville, le bâtiment central du village
public class HotelDeVille extends Batiment {

    protected int cadenceAttaque = 1; // Cadence d'attaque de l'hôtel de ville (nombre d'attaques par tour par secondes)
    protected int puissanceAttaque = 20; // Dégats infligés par l'hôtel de ville à chaque attaque
    protected int porteeAttaque = 2; // Portée d'attaque de l'hôtel de ville

    public HotelDeVille(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
    }

}
