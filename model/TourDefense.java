package model;

// Classe représentant une tour de défense, un bâtiment défensif pour protéger le village des voleurs
public class TourDefense extends Batiment{
    public TourDefense(String nom, int pointsVie, int x, int y, int coutBois, int coutFer, int coutOr) {
        super(nom, pointsVie, x, y, coutBois, coutFer, coutOr);
    }
    
    /* points de vie de la tour */
    protected int pointsVie = 100; // Attribut spécifique à la tour de défense pour représenter sa santé

    /* Getter pour la puissance d'attaque de la tour de défense */
    public int getPuissanceAttaque() {
        return puissanceAttaque;
    }

    /* Setter pour la puissance d'attaque de la tour de défense */
    public void setPuissanceAttaque(int puissanceAttaque) {
        this.puissanceAttaque = puissanceAttaque;
    }

    /* Méthode pour attaquer un voleur */
    public void attaquerVoleur(Voleur voleur) {
        voleur.subirAttaque(this.puissanceAttaque);
    }

    /* Méthode pour subir une attaque */
    public void subirAttaque(int degats) {
        super.subirAttaque(degats);
        // Teste si la tour de défense est détruite après avoir subi des dégâts
        if (!estConstruit) {
            System.out.println("La tour de défense " + this.nom + " a été détruite !");
        }
    }
}
