package main;

import control.ReactionClic;
import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import model.CheatCode;
import model.Inventaire;
import model.Map;
import model.SauvegardeJeu;
import view.EcranAccueil;
import view.MapPanel;
import view.PanneauLateral;

public class Main {

    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("⚔ Jeu Médiéval ⚔");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            afficherEcranAccueil();
            frame.setVisible(true);
        });
    }

    private static void afficherEcranAccueil() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        EcranAccueil ecran = new EcranAccueil(new EcranAccueil.EcranAccueilListener() {
            @Override public void onNouvellePartie()                  { demarrerPartie(null); }
            @Override public void onReprendrePartie(SauvegardeJeu s)  { demarrerPartie(s);    }
            @Override public void onQuitter()                         { System.exit(0);        }
        });

        frame.add(ecran, BorderLayout.CENTER);
        frame.setSize(400, 420);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private static void demarrerPartie(SauvegardeJeu save) {
        frame.getContentPane().removeAll();

        Map gameMap = new Map(20, 15);
        MapPanel mapPanel = new MapPanel(gameMap);
        Inventaire inventaire = new Inventaire();
        CheatCode cheatCode = new CheatCode(inventaire, gameMap);

        gameMap.setInventaire(inventaire);

        if (save != null) {
            gameMap.chargerDepuis(save);
        }

        gameMap.setOnRessourceGagneeListener((type, quantite) ->
            inventaire.ajouterRessource(type, quantite)
        );

        Runnable onVictoire = () -> SwingUtilities.invokeLater(Main::afficherEcranAccueil);

        ReactionClic reactionClic = new ReactionClic(gameMap, mapPanel,
            mapPanel.getTileSize(), mapPanel.getBorderPad(), mapPanel.getTitleHeight(), onVictoire);

        mapPanel.addMouseMotionListener(reactionClic);
        mapPanel.addMouseListener(reactionClic);

        JPanel bottomBar = construireBarreBas(gameMap, mapPanel, cheatCode);

        Timer timerRafraichissement = new Timer(120, e -> mapPanel.repaint());
        timerRafraichissement.start();

        PanneauLateral panneauLateral = new PanneauLateral(gameMap, mapPanel);
        panneauLateral.addMouseListener(
            new control.ReactionClicPanneauLateral(gameMap, mapPanel, panneauLateral));

        // Connecter les listeners inventaire au panneau latéral aussi
        inventaire.setOnInventaireChangeListener((bois, fer, or, nourr) -> {
            mapPanel.setStockBois(bois);
            mapPanel.setStockFer(fer);
            mapPanel.setStockOr(or);
            mapPanel.setStockNourriture(nourr);
            panneauLateral.setStockBois(bois);
            panneauLateral.setStockFer(fer);
            panneauLateral.setStockOr(or);
            panneauLateral.setStockNourriture(nourr);
        });

        // Timer rafraichit aussi le panneau latéral
        Timer timerRafraichissement2 = new Timer(500, e -> panneauLateral.repaint());
        timerRafraichissement2.start();

        JScrollPane scrollPane = new JScrollPane(mapPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(new Color(45, 35, 25));

        frame.setLayout(new BorderLayout());
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(panneauLateral, BorderLayout.EAST);
        frame.add(bottomBar, BorderLayout.SOUTH);
        frame.pack();
        frame.setMinimumSize(new Dimension(600, 400));
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel construireBarreBas(Map gameMap, MapPanel mapPanel, CheatCode cheatCode) {
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

        JButton btnSave = new JButton("💾 Sauvegarder");
        btnSave.setBackground(new Color(30, 60, 30));
        btnSave.setForeground(new Color(100, 220, 100));
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSave.addActionListener(e -> {
            try {
                SauvegardeJeu.sauvegarder(gameMap.creerSauvegarde());
                cheatResult.setText("Partie sauvegardée !");
            } catch (IOException ex) {
                cheatResult.setText("Erreur sauvegarde !");
            }
        });

        JButton btnMenu = new JButton("🏠 Menu");
        btnMenu.setBackground(new Color(40, 30, 15));
        btnMenu.setForeground(new Color(200, 170, 80));
        btnMenu.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnMenu.addActionListener(e -> {
            int choix = JOptionPane.showConfirmDialog(frame,
                "Retourner au menu ? La partie en cours ne sera pas sauvegardée automatiquement.",
                "Menu principal", JOptionPane.YES_NO_OPTION);
            if (choix == JOptionPane.YES_OPTION) {
                SwingUtilities.invokeLater(Main::afficherEcranAccueil);
            }
        });

        bottomBar.add(cheatLabel);
        bottomBar.add(cheatField);
        bottomBar.add(cheatBtn);
        bottomBar.add(cheatResult);
        bottomBar.add(Box.createHorizontalStrut(20));
        bottomBar.add(btnSave);
        bottomBar.add(btnMenu);

        return bottomBar;
    }
}