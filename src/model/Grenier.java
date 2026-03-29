package model;

// Classe représentant le bâtiment de stockage de nourriture
public class Grenier extends Batiment {

    public Grenier(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
        setTypeStocke(Ressource.Type.NOURRITURE);
    }
    
}
