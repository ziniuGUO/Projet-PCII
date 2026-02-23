package test.testmini;

import model.Personnage;

import javax.swing.*;

/**
 * C'est le point de lancement pour tester notre "Mini-Map".
 * L'idée est de vérifier que le personnage s'affiche bien et réagit correctement.
 */
public class MiniView {

    public static void main(String[] args) {
        /* On lance l'interface dans le thread Swing pour éviter les bugs graphiques */
        SwingUtilities.invokeLater(() -> {

            /* 1. On prépare le terrain : une grille de 12 par 8 cases */
            MiniMap map = new MiniMap(12, 8);

            /* 2. On pose un petit personnage sur la carte (un villageois en (2,3)) */
            map.addPersonnage(new Personnage("Villageois", 2, 3, 3));

            /* 3. On crée le panneau qui va dessiner tout ça */
            MiniMapPanel panel = new MiniMapPanel(map);

            /* 4. On branche le contrôleur pour que la souris puisse interagir avec la carte */
            MiniController controller = new MiniController(map, panel);

            /* On écoute les mouvements et les clics de la souris */
            panel.addMouseMotionListener(controller);
            panel.addMouseListener(controller);

            /* 5. Configuration de la fenêtre Windows/macOS */
            JFrame frame = new JFrame("Mini Test Personnage");

            /* Pour que le programme s'arrête vraiment quand on ferme la fenêtre */
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            /* On met notre panneau de jeu à l'intérieur */
            frame.setContentPane(panel);

            /* On ajuste la taille de la fenêtre au contenu */
            frame.pack();

            /* On centre la fenêtre à l'écran, c'est plus propre */
            frame.setLocationRelativeTo(null);

            /* Et enfin, on l'affiche ! */
            frame.setVisible(true);
        });
    }
}