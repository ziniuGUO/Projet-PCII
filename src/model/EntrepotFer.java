package model;

// Classe représentant le bâtiment de stockage du fer
public class EntrepotFer extends Batiment {

    public EntrepotFer(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
        setTypeStocke(Ressource.Type.FER);
    }
}