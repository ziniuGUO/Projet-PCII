package test.testmini;

import javax.swing.*;
import java.awt.*;

/**
 * C'est la fenêtre principale de mon prototype.
 * Son rôle est d'assembler les briques du MVC : le Modèle (MiniMap),
 * la Vue (MiniMapPanel) et le Contrôleur (MiniController).
 */
public class MiniView extends JFrame {

    public MiniView() {
        // Paramètres de base de la fenêtre
        setTitle("PCII - Mini Test Autel");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 1. Initialisation du modèle (une grille de 12x10)
        MiniMap map = new MiniMap(12, 10);

        // 2. Initialisation de l'affichage
        MiniMapPanel panel = new MiniMapPanel(map);

        // 3. Initialisation du contrôleur pour gérer les clics et les mouvements
        MiniController controller = new MiniController(map, panel);

        // Liaison entre la vue et le contrôleur
        panel.addMouseListener(controller);
        panel.addMouseMotionListener(controller);

        /**
         * Mise en place d'un Timer pour le rafraîchissement graphique.
         * Toutes les 100ms, on force le panneau à se redessiner (repaint).
         * C'est indispensable pour voir les personnages bouger et les ressources évoluer.
         */
        Timer timer = new Timer(100, e -> panel.repaint());
        timer.start();

        // Utilisation d'un JScrollPane pour permettre de scroller si la grille est trop grande
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Ajuste la taille de la fenêtre au contenu et centre l'affichage sur l'écran
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}