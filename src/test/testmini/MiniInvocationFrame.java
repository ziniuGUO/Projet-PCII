package test.testmini;

import model.ActionType;
import model.Personnage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Cette fenêtre représente l'interface de l'Autel d'Invocation.
 * C'est ici que le joueur dépense son or pour obtenir de nouveaux personnages
 * et gère sa liste d'unités obtenues.
 */
public class MiniInvocationFrame extends JFrame {

    // Prix fixe pour une invocation (mécanique de "Gacha" simplifiée)
    private static final int COUT_INVOCATION = 100;

    private final MiniMap map;
    private final MiniMapPanel panel;

    // Gestion dynamique de la liste : le Model permet de mettre à jour la JList sans tout reconstruire
    private final DefaultListModel<Personnage> listModel = new DefaultListModel<>();
    private final JList<Personnage> liste = new JList<>(listModel);

    private final JLabel labelOr = new JLabel();
    private final JLabel labelResultat = new JLabel(" ");

    public MiniInvocationFrame(MiniMap map, MiniMapPanel panel) {
        this.map = map;
        this.panel = panel;

        setTitle("Autel d'invocation");
        setSize(760, 420);
        setLocationRelativeTo(panel); // Centre la fenêtre par rapport au jeu
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // On ferme juste cette fenêtre, pas le jeu
        setLayout(new GridLayout(1, 2, 10, 10)); // On divise l'écran en deux : Invocation à gauche, Liste à droite

        // --- Partie Gauche : Interface de tirage ---
        JPanel gauche = new JPanel(new BorderLayout(8, 8));
        JPanel droite = new JPanel(new BorderLayout(8, 8));

        labelOr.setHorizontalAlignment(SwingConstants.CENTER);
        rafraichirInfos();

        // Petit guide textuel pour aider l'utilisateur
        JTextArea aide = new JTextArea(
                "1. Clique sur Invoquer pour obtenir un personnage.\n" +
                        "2. Chaque invocation coute 100 or.\n" +
                        "3. Clique sur un personnage DISPO pour lui donner une mission.\n" +
                        "4. Clique ensuite sur le personnage sur la carte pour le rappeler ou recuperer."
        );
        aide.setEditable(false);
        aide.setOpaque(false);
        aide.setLineWrap(true);
        aide.setWrapStyleWord(true);

        JButton bouton = new JButton("Invoquer (100 or)");
        bouton.addActionListener(e -> invoquer());

        labelResultat.setHorizontalAlignment(SwingConstants.CENTER);

        gauche.setBorder(BorderFactory.createTitledBorder("Invocation"));
        gauche.add(labelOr, BorderLayout.NORTH);
        gauche.add(aide, BorderLayout.CENTER);

        JPanel bas = new JPanel(new GridLayout(2, 1, 4, 4));
        bas.add(bouton);
        bas.add(labelResultat);
        gauche.add(bas, BorderLayout.SOUTH);

        // --- Partie Droite : Liste des personnages ---
        liste.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Customisation de l'affichage de la liste (on affiche le statut et les étoiles)
        liste.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Personnage p) {
                    String statut = p.estDisponible() ? "DISPO" : "OCCUPE";
                    label.setText(p.getNom() + " - " + p.getRareteEtoiles() + "★ [" + statut + "]");
                    // Si le perso travaille, on le grise pour que ce soit clair visuellement
                    if (!p.estDisponible() && !isSelected) {
                        label.setForeground(Color.GRAY);
                    }
                }
                return label;
            }
        });

        // Tooltip dynamique : affiche les détails quand on survole un élément de la liste
        liste.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = liste.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Personnage p = listModel.get(index);
                    String statut = p.estDisponible() ? "DISPO" : "OCCUPE";
                    String action = p.getActionCourante() == null ? "Aucune" : p.getActionCourante().getLabel();
                    liste.setToolTipText(p.getNom() + " | " + p.getRareteEtoiles() + "★ | " + statut + " | " + action);
                }
            }
        });

        // Un simple clic permet d'ouvrir le menu d'actions pour un perso stocké
        liste.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Personnage p = liste.getSelectedValue();
                    if (p != null) {
                        ouvrirMenuActions(p);
                    }
                }
            }
        });

        droite.setBorder(BorderFactory.createTitledBorder("Personnages obtenus"));
        droite.add(new JScrollPane(liste), BorderLayout.CENTER); // JScrollPane pour pouvoir scroller si on a beaucoup de persos
        droite.add(new JLabel("Clique sur un personnage DISPO pour choisir une action.", SwingConstants.CENTER), BorderLayout.SOUTH);

        add(gauche);
        add(droite);

        rafraichirListe();
    }

    /**
     * Gère la logique d'achat d'un nouveau personnage.
     */
    private void invoquer() {
        Personnage p = map.invoquerPersonnage(COUT_INVOCATION);
        if (p == null) {
            JOptionPane.showMessageDialog(this, "Pas assez d'or.");
            return;
        }

        // Si l'invocation réussit, on met à jour les affichages
        labelResultat.setText("Obtenu : " + p.getNom() + " - " + p.getRareteEtoiles() + "★");
        rafraichirInfos();
        rafraichirListe();
        panel.repaint(); // Pour que le nouveau perso apparaisse sur la carte si nécessaire
    }

    /**
     * Menu contextuel pour envoyer un personnage travailler directement depuis l'inventaire.
     */
    private void ouvrirMenuActions(Personnage p) {
        if (!p.estDisponible()) {
            JOptionPane.showMessageDialog(this, "Ce personnage est deja occupe.");
            return;
        }

        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.DEFENDRE.getLabel()
        };

        int choix = JOptionPane.showOptionDialog(
                this,
                "Choisis une action :",
                "Actions - " + p.getNom(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        ActionType action = null;
        if (choix == 0) action = ActionType.COUPER_BOIS;
        else if (choix == 1) action = ActionType.DEFENDRE;

        if (action == null) return;

        // On déploie le personnage sur la carte avec sa nouvelle mission
        map.deployerPersonnage(p, action);
        rafraichirListe();
        panel.repaint();
    }

    /**
     * Met à jour les labels de ressources (Or et Bois).
     */
    private void rafraichirInfos() {
        labelOr.setText("Or disponible : " + map.getStockOr() + " | Bois joueur : " + map.getStockBoisJoueur());
    }

    /**
     * Synchronise la JList visuelle avec la liste réelle des personnages du modèle.
     */
    private void rafraichirListe() {
        listModel.clear();
        for (Personnage p : map.getPersonnages()) {
            listModel.addElement(p);
        }
    }
}