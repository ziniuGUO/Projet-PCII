package model;

// Thread pour attaquer automatiquement les voleurs à partir d'un bâtiment conçu pour cela
public class BatimentAttaque extends Thread {
    private Batiment batiment;
    private Map map;

    public BatimentAttaque(Batiment batiment, Map map, Voleur voleur) {
        this.map = map;
        this.batiment = batiment;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(1000/batiment.cadenceAttaque); // Cadence d'attaque
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // Logique pour attaquer les voleurs et infliger des dégâts
            System.out.println(batiment.getNom() + " attaque les voleurs et inflige " + TourDefense.puissanceAttaque + " dégâts !");
            // Infliger des dégats aux voleurs dans la portée d'attaque du bâtiment
            if (batiment.estConstruit) {
            for (Voleur voleur : map.getVoleurs()) {
                if (voleur.getX() >= batiment.getX() - Batiment.porteeAttaque && voleur.getX() <= batiment.getX() + Batiment.porteeAttaque &&
                    voleur.getY() >= batiment.getY() - Batiment.porteeAttaque && voleur.getY() <= batiment.getY() + Batiment.porteeAttaque) {
                    voleur.subirAttaque(batiment.puissanceAttaque);
                }
            }
            }
        }
    }
}
