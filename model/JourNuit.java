package model;

public class JourNuit extends Thread {
    private boolean isDay;

    public JourNuit() {
        this.isDay = true; // Commence la simulation avec la nuit
    }

    public boolean getIsDay() {
        return this.isDay;
    }

    @Override
    public void run() {
        while (true) {
            isDay = !isDay; // Alterne entre jour et nuit
            if (isDay) {
                System.out.println("Le jour se lève.");
            } else {
                System.out.println("La nuit tombe.");
            }
            try {
                Thread.sleep(30000); // on change toutes les 30s (ajustable)
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
