package main;

import control.ReactionClic;
import java.awt.*;
import javax.swing.*;
import model.Inventaire;
import model.Map;
import view.MapPanel;

public class Main {
    public static void main(String[] args) {
        Map gameMap = new Map(20, 15);
        gameMap.displayMap();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("⚔ Jeu Médiéval ⚔");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            MapPanel mapPanel = new MapPanel(gameMap);

            Inventaire inventaire = new Inventaire();

            // sync affichage inventaire -> MapPanel
            inventaire.setOnInventaireChangeListener((bois, fer, or, nourr) -> {
                mapPanel.setStockBois(bois);
                mapPanel.setStockFer(fer);
                mapPanel.setStockOr(or);
                mapPanel.setStockNourriture(nourr);
            });

            // sync gain de ressource -> Inventaire
            gameMap.setOnRessourceGagneeListener((type, quantite) ->
                inventaire.ajouterRessource(type, quantite)
            );

            ReactionClic reactionClic = new ReactionClic(
                gameMap,
                mapPanel,
                mapPanel.getTileSize(),
                mapPanel.getBorderPad(),
                mapPanel.getTitleHeight()
            );

            mapPanel.addMouseMotionListener(reactionClic);
            mapPanel.addMouseListener(reactionClic);

            Timer timerRafraichissement = new Timer(120, e -> mapPanel.repaint());
            timerRafraichissement.start();

            JScrollPane scrollPane = new JScrollPane(mapPanel);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            scrollPane.getViewport().setBackground(new Color(45, 35, 25));

            frame.add(scrollPane);
            frame.pack();
            frame.setMinimumSize(new Dimension(600, 400));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}