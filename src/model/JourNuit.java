package model;

public class JourNuit extends Thread {
    private boolean isDay;

    private static final long DUREE_JOUR  = 120000L; // 2 minutes
    private static final long DUREE_NUIT  =  30000L; // 30 secondes

    private long debutPhase = System.currentTimeMillis();
    private long dureePhaseActuelle = DUREE_JOUR;

    public JourNuit() {
        this.isDay = true; // Commence le jeu avec le jour
    }

    public boolean getIsDay() {
        return this.isDay;
    }

    /** Retourne le temps restant (en ms) avant la prochaine transition */
    public long getTempsRestantMs() {
        long ecoule = System.currentTimeMillis() - debutPhase;
        long restant = dureePhaseActuelle - ecoule;
        return Math.max(0, restant);
    }

    @Override
    public void run() {
        while (true) {
            if (isDay) {
                System.out.println("Le jour se lève.");
            } else {
                System.out.println("La nuit tombe.");
            }
            try {
                // Phase jour
                isDay = true;
                debutPhase = System.currentTimeMillis();
                dureePhaseActuelle = DUREE_JOUR;
                Thread.sleep(DUREE_JOUR);

                // Phase nuit
                isDay = false;
                debutPhase = System.currentTimeMillis();
                dureePhaseActuelle = DUREE_NUIT;
                Thread.sleep(DUREE_NUIT);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}