package model;

public class JourNuit extends Thread {
    private boolean isDay;

    public JourNuit() {
        this.isDay = true; // Commence le jeu avec le jour
    }

    public boolean getIsDay() {
        return this.isDay;
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
                Thread.sleep(120000); // 2 minutes de jour
                isDay = !isDay;
                Thread.sleep(30000); // 30 secondes de nuit
                isDay = !isDay;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
