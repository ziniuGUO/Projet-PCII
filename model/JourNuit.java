public class JourNuit extends Thread {
    private boolean isDay;

    public JourNuit() {
        this.isDay = true; // Commence avec le jour
    }

    public boolean getIsDay() {
        return this.isDay;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(10000); // Change de jour/nuit toutes les 2 minutes (120000 ms)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            isDay = !isDay; // Alterne entre jour et nuit
            if (isDay) {
                System.out.println("Le jour se lève.");
            } else {
                System.out.println("La nuit tombe.");
            }
        }
    }

    public boolean isDay() {
        return isDay;
    }
}