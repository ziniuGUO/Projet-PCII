public class Voleur {

    //attributs
    private int x;
    private int y;
    private boolean enFuite;

    //constructeur
    public Voleur(int x, int y) {
        this.x = x;
        this.y = y;
        this.enFuite = false;
    }

    //méthodes
    //on souhaite créer une méthode qui prend en argument les coordonnées d'une ressource et qui fait avancer le voleur vers cette ressource
    public void avancerVers(int xRessource, int yRessource) {
        while (x != xRessource || y != yRessource) {
            if (x < xRessource) {
                x++;
            } else if (x > xRessource) {
                x--;
            }

            if (y < yRessource) {
                y++;
            } else if (y > yRessource) {
                y--;
            }
        }
    }

    //cette méthode permet au voleur de "fuir" en quittant l'écran progressivement, quand le jour se lève
    public void fuir() {
        this.enFuite = true;
        while (x < 100 && y < 100) { // supposons que l'écran fait 100x100
            x++;
            y++;
        }
    }

    /* 
    //on veut faire en sorte que le voleur soit arrêté lorsqu'un villageois le touche, ce qui se produit lorsque le voleur est à une distance de 1 ou moins d'un villageois
    public boolean estArretePar(Villageois villageois) {
        double distance = Math.sqrt(Math.pow(x - villageois.getX(), 2) + Math.pow(y - villageois.getY(), 2));
        return distance <= 1;
    */

    //on veut faire en sorte qu'une ressource soit volée et disparaisse lorsque le voleur atteint les coordonnées de la ressource
    public boolean aVoleRessource(int xRessource, int yRessource) {
        return x == xRessource && y == yRessource;
    }
    
}