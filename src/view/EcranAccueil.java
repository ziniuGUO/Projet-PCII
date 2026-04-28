package view;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;
import model.SauvegardeJeu;

/**
 * Écran d'accueil du jeu : Nouvelle Partie, Reprendre, Quitter.
 */
public class EcranAccueil extends JPanel {

    public interface EcranAccueilListener {
        void onNouvellePartie();
        void onReprendrePartie(SauvegardeJeu save);
        void onQuitter();
    }

    public EcranAccueil(EcranAccueilListener listener) {
        setBackground(new Color(15, 10, 5));
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titre = new JLabel("⚔  Jeu Médiéval  ⚔", SwingConstants.CENTER);
        titre.setFont(new Font("Serif", Font.BOLD, 42));
        titre.setForeground(new Color(255, 200, 30));
        gbc.gridy = 0;
        gbc.insets = new Insets(40, 20, 30, 20);
        add(titre, gbc);

        gbc.insets = new Insets(8, 60, 8, 60);

        // Nouvelle Partie
        JButton btnNouvelle = creerBouton("⚔  Nouvelle Partie");
        gbc.gridy = 1;
        add(btnNouvelle, gbc);
        btnNouvelle.addActionListener(e -> listener.onNouvellePartie());

        // Reprendre
        JButton btnReprendre = creerBouton("↩  Reprendre");
        boolean saveExiste = SauvegardeJeu.sauvegardeExiste();
        btnReprendre.setEnabled(saveExiste);
        if (!saveExiste) {
            btnReprendre.setForeground(new Color(120, 100, 60));
            btnReprendre.setToolTipText("Aucune sauvegarde trouvée");
        }
        gbc.gridy = 2;
        add(btnReprendre, gbc);
        btnReprendre.addActionListener(e -> {
            try {
                SauvegardeJeu save = SauvegardeJeu.charger();
                listener.onReprendrePartie(save);
            } catch (IOException | ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement : " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Quitter
        JButton btnQuitter = creerBouton("✕  Quitter");
        btnQuitter.setForeground(new Color(200, 80, 60));
        gbc.gridy = 3;
        gbc.insets = new Insets(8, 60, 40, 60);
        add(btnQuitter, gbc);
        btnQuitter.addActionListener(e -> listener.onQuitter());
    }

    private JButton creerBouton(String texte) {
        JButton btn = new JButton(texte);
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setBackground(new Color(60, 45, 20));
        btn.setForeground(new Color(255, 220, 100));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(120, 90, 30), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}