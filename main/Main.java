package main;

import control.ReactionClic;
import java.awt.*;
import javax.swing.*;
import model.Map;
import view.MapPanel;

public class Main {
    public static void main(String[] args) {
        // Crée la map 20x15
        Map gameMap = new Map(20, 15);

        // Affichage console (debug)
        gameMap.displayMap();

        // Lancement de l'interface graphique dans le thread Swing
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("⚔ Jeu Médiéval ⚔");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Panel de la map
            MapPanel mapPanel = new MapPanel(gameMap);

            // Controleur pour gerer les clics souris
            ReactionClic reactionClic = new ReactionClic(
                gameMap, 
                mapPanel,
                mapPanel.getTileSize(),
                mapPanel.getBorderPad(),
                mapPanel.getTitleHeight()
            );
            
            // Attacher les listeners au panel
            mapPanel.addMouseMotionListener(reactionClic);
            mapPanel.addMouseListener(reactionClic);

            // ScrollPane au cas où la map est grande
            JScrollPane scrollPane = new JScrollPane(mapPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(45, 35, 25));

            frame.add(scrollPane);
            frame.pack();
            frame.setMinimumSize(new Dimension(600, 400));
            frame.setLocationRelativeTo(null); // Centrer la fenêtre
            frame.setVisible(true);
        });
    }
}