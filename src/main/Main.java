package main;

import control.ReactionClic;
import java.awt.*;
import javax.swing.*;
import model.CheatCode;
import model.Inventaire;
import model.Map;
import view.MapPanel;

public class Main {

    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("⚔ Jeu Médiéval ⚔");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            demarrerPartie(frame);
            frame.setVisible(true);
        });
    }

    private static void demarrerPartie(JFrame frame) {
        frame.getContentPane().removeAll();

        Map gameMap = new Map(20, 15);

        MapPanel mapPanel = new MapPanel(gameMap);
        Inventaire inventaire = new Inventaire();
        CheatCode cheatCode = new CheatCode(inventaire, gameMap);

        gameMap.setInventaire(inventaire);

        inventaire.setOnInventaireChangeListener((bois, fer, or, nourr) -> {
            mapPanel.setStockBois(bois);
            mapPanel.setStockFer(fer);
            mapPanel.setStockOr(or);
            mapPanel.setStockNourriture(nourr);
        });

        gameMap.setOnRessourceGagneeListener((type, quantite) ->
            inventaire.ajouterRessource(type, quantite)
        );

        Runnable onVictoire = () -> SwingUtilities.invokeLater(() -> demarrerPartie(frame));

        ReactionClic reactionClic = new ReactionClic(gameMap, mapPanel,
            mapPanel.getTileSize(), mapPanel.getBorderPad(), mapPanel.getTitleHeight(), onVictoire);

        mapPanel.addMouseMotionListener(reactionClic);
        mapPanel.addMouseListener(reactionClic);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bottomBar.setBackground(new Color(30, 20, 10));
        JLabel cheatLabel = new JLabel("Code triche :");
        cheatLabel.setForeground(new Color(180, 160, 100));
        cheatLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        JTextField cheatField = new JTextField(16);
        cheatField.setBackground(new Color(50, 40, 25));
        cheatField.setForeground(new Color(255, 220, 100));
        cheatField.setCaretColor(Color.WHITE);
        cheatField.setBorder(BorderFactory.createLineBorder(new Color(120, 100, 50)));
        JButton cheatBtn = new JButton("Activer");
        cheatBtn.setBackground(new Color(80, 60, 30));
        cheatBtn.setForeground(new Color(255, 220, 100));
        JLabel cheatResult = new JLabel("");
        cheatResult.setForeground(new Color(100, 220, 100));
        cheatResult.setFont(new Font("SansSerif", Font.ITALIC, 11));

        Runnable activerCheat = () -> {
            String resultat = cheatCode.appliquer(cheatField.getText());
            cheatResult.setText(resultat);
            cheatField.setText("");
            mapPanel.repaint();
        };

        cheatBtn.addActionListener(e -> activerCheat.run());
        cheatField.addActionListener(e -> activerCheat.run());

        bottomBar.add(cheatLabel);
        bottomBar.add(cheatField);
        bottomBar.add(cheatBtn);
        bottomBar.add(cheatResult);

        Timer timerRafraichissement = new Timer(120, e -> mapPanel.repaint());
        timerRafraichissement.start();

        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(45, 35, 25));

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(bottomBar, BorderLayout.SOUTH);
        frame.pack();
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setLocationRelativeTo(null);
    }
}