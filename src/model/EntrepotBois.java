package model;

// Classe représentant le bâtiment de stockage du bois
public class EntrepotBois extends Batiment {

    public EntrepotBois(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
        setTypeStocke(Ressource.Type.BOIS);
    }
}

