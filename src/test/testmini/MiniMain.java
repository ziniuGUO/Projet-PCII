package test.testmini;

import javax.swing.*;

/**
 * C'est le point de départ tout simple de notre petit module de test.
 */
public class MiniMain {

    public static void main(String[] args) {
        /* * On utilise invokeLater pour être sûr que l'interface Swing se lance
         * proprement dans son propre thread. C'est la règle d'or pour éviter
         * les freezes au démarrage.
         */
        SwingUtilities.invokeLater(MiniView::new);
    }
}