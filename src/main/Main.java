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

    private static void afficherEcranGameOver(model.Map.RaisonGameOver raison) {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(new Color(10, 5, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(180, 30, 30), 4));

        String titre, sousTitre;
        if (raison == model.Map.RaisonGameOver.FAMINE) {
            titre     = "💀  DÉFAITE — FAMINE  💀";
            sousTitre = "<html><center>Vos villageois sont morts de faim.<br>Le village s'est effondré dans le silence.</center></html>";
        } else {
            titre     = "🔥  DÉFAITE — LE DRAGON  🔥";
            sousTitre = "<html><center>Le dragon a brûlé votre village.<br>La Grande Statue n'a jamais été érigée.</center></html>";
        }

        JLabel lblTitre = new JLabel(titre, SwingConstants.CENTER);
        lblTitre.setFont(new Font("Serif", Font.BOLD, 30));
        lblTitre.setForeground(new Color(220, 60, 60));
        lblTitre.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        JLabel lblSous = new JLabel(sousTitre, SwingConstants.CENTER);
        lblSous.setFont(new Font("Serif", Font.ITALIC, 15));
        lblSous.setForeground(new Color(200, 150, 150));
        lblSous.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JButton btnRejouer = new JButton("⚔  Rejouer (3h)");
        btnRejouer.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnRejouer.setBackground(new Color(120, 30, 30));
        btnRejouer.setForeground(Color.WHITE);
        btnRejouer.setFocusPainted(false);
        btnRejouer.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        btnRejouer.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRejouer.addActionListener(e -> demarrerPartie(null, 3 * 60 * 60 * 1000L));

        JButton btnMenu = new JButton("🏠  Menu principal");
        btnMenu.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnMenu.setBackground(new Color(40, 30, 15));
        btnMenu.setForeground(new Color(200, 170, 80));
        btnMenu.setFocusPainted(false);
        btnMenu.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnMenu.addActionListener(e -> afficherEcranAccueil());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(new Color(10, 5, 5));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        btnPanel.add(btnRejouer);
        btnPanel.add(btnMenu);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(10, 5, 5));
        centre.add(lblSous);

        panel.add(lblTitre, BorderLayout.NORTH);
        panel.add(centre,   BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static void afficherEcranVictoireAvecDifficulte(long dureeActuelle) {
        // Calculer la prochaine difficulté
        long prochaineDuree;
        if      (dureeActuelle >= 3 * 60 * 60 * 1000L)        prochaineDuree = (long)(2.5 * 60 * 60 * 1000L);
        else if (dureeActuelle >= (long)(2.5 * 60 * 60 * 1000L)) prochaineDuree = 2 * 60 * 60 * 1000L;
        else if (dureeActuelle >= 2 * 60 * 60 * 1000L)        prochaineDuree = (long)(1.5 * 60 * 60 * 1000L);
        else                                                   prochaineDuree = (long)(1.5 * 60 * 60 * 1000L); // déjà au max

        boolean estNiveauMax = dureeActuelle <= (long)(1.5 * 60 * 60 * 1000L);

        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(new Color(15, 10, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(220, 130, 0), 4));

        JLabel lblTitre = new JLabel("🐉  VICTOIRE !  🐉", SwingConstants.CENTER);
        lblTitre.setFont(new Font("Serif", Font.BOLD, 36));
        lblTitre.setForeground(new Color(255, 200, 30));
        lblTitre.setBorder(BorderFactory.createEmptyBorder(30, 20, 0, 20));

        JLabel lblSous = new JLabel(
            "<html><center>La Grande Statue du Dragon s'élève sur votre cité !<br>Votre règne est légendaire !</center></html>",
            SwingConstants.CENTER);
        lblSous.setFont(new Font("Serif", Font.ITALIC, 15));
        lblSous.setForeground(new Color(210, 180, 100));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnPanel.setBackground(new Color(15, 10, 5));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        // Bouton "Même difficulté"
        String labelMeme = formaterDuree(dureeActuelle);
        JButton btnMeme = new JButton("⚔  Rejouer (" + labelMeme + ")");
        styliserBoutonVictoire(btnMeme, new Color(80, 60, 20));
        btnMeme.addActionListener(e -> demarrerPartie(null, dureeActuelle));
        btnPanel.add(btnMeme);

        // Bouton "Plus difficile" (si pas au niveau max)
        if (!estNiveauMax) {
            String labelPlus = formaterDuree(prochaineDuree);
            JButton btnPlus = new JButton("🔥  Plus difficile (" + labelPlus + ")");
            styliserBoutonVictoire(btnPlus, new Color(140, 40, 0));
            btnPlus.addActionListener(e -> demarrerPartie(null, prochaineDuree));
            btnPanel.add(btnPlus);
        } else {
            JLabel lblMax = new JLabel("★ Niveau maximum atteint !", SwingConstants.CENTER);
            lblMax.setFont(new Font("SansSerif", Font.BOLD, 13));
            lblMax.setForeground(new Color(255, 180, 0));
            btnPanel.add(lblMax);
        }

        // Bouton menu
        JButton btnMenu = new JButton("🏠  Menu");
        styliserBoutonVictoire(btnMenu, new Color(40, 30, 15));
        btnMenu.setForeground(new Color(200, 170, 80));
        btnMenu.addActionListener(e -> afficherEcranAccueil());
        btnPanel.add(btnMenu);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(new Color(15, 10, 5));
        centre.add(lblSous);

        panel.add(lblTitre, BorderLayout.NORTH);
        panel.add(centre,   BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);

        frame.add(panel, BorderLayout.CENTER);
        frame.revalidate();
        frame.repaint();
    }

    private static String formaterDuree(long ms) {
        long h = ms / 3600000;
        long m = (ms % 3600000) / 60000;
        if (m == 0) return h + "h";
        return h + "h" + m + "m";
    }

    private static void styliserBoutonVictoire(JButton btn, Color bg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static void afficherEcranAccueil() {
        frame.getContentPane().removeAll();
        frame.setLayout(new BorderLayout());

        EcranAccueil ecran = new EcranAccueil(new EcranAccueil.EcranAccueilListener() {
            @Override public void onNouvellePartie()                  { demarrerPartie(null); }
            @Override public void onReprendrePartie(SauvegardeJeu s)  {
                if (!s.partieEnCours) {
                    // Victoire sauvegardée → afficher l'écran victoire avec choix difficulté
                    afficherEcranVictoireAvecDifficulte(s.dureeTotaleMs);
                } else {
                    demarrerPartie(s);
                }
            }
            @Override public void onQuitter()                         { System.exit(0);        }
        });

        frame.add(ecran, BorderLayout.CENTER);
        frame.setSize(400, 420);
        frame.setLocationRelativeTo(null);
        frame.revalidate();
        frame.repaint();
    }

    private static void demarrerPartie(SauvegardeJeu save) {
        demarrerPartie(save, 3 * 60 * 60 * 1000L);
    }

    private static void demarrerPartie(SauvegardeJeu save, long dureeTotaleMs) {
        frame.getContentPane().removeAll();

        Map gameMap = new Map(20, 15);
        MapPanel mapPanel = new MapPanel(gameMap);
        Inventaire inventaire = new Inventaire();
        CheatCode cheatCode = new CheatCode(inventaire, gameMap);

        gameMap.setInventaire(inventaire);

        // Appliquer la durée (sauvegarde ou nouvelle partie)
        if (save != null) {
            gameMap.chargerDepuis(save);
        } else {
            gameMap.setDureeTotaleMs(dureeTotaleMs);
            gameMap.setTempsRestantMs(dureeTotaleMs);
        }

        gameMap.setOnRessourceGagneeListener((type, quantite) ->
            inventaire.ajouterRessource(type, quantite)
        );

        // ── Game over listener ────────────────────────────────────────────────
        gameMap.setOnGameOverListener(raison -> afficherEcranGameOver(raison));

        Runnable onVictoire = () -> SwingUtilities.invokeLater(() -> {
            long dureeActuelle = gameMap.getDureeTotaleMs();
            // Sauvegarder l'état victoire pour que "Reprendre" affiche le bon écran
            gameMap.arreterTimer();
            try {
                SauvegardeJeu saveVictoire = gameMap.creerSauvegarde();
                saveVictoire.partieEnCours = false;
                SauvegardeJeu.sauvegarder(saveVictoire);
            } catch (java.io.IOException ignored) {}
            afficherEcranVictoireAvecDifficulte(dureeActuelle);
        });

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

        // Démarrer le timer de partie
        gameMap.demarrerTimer();

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
                gameMap.arreterTimer();
                SauvegardeJeu.sauvegarder(gameMap.creerSauvegarde());
                gameMap.demarrerTimer();
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