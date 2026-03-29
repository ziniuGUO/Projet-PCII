package model;

// Classe représentant l'Autel d'Invocation, un bâtiment spécifique dans le jeu
public class AutelInvocation extends Batiment {

    public AutelInvocation(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
    }
    @Override
    public String getNom() {
        return "Autel d'invocation";
    }
}
