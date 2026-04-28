package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import model.ActionType;
import model.Batiment;
import model.Map;
import model.Personnage;

public class FenetreInvocation extends JFrame {

    private final Map gameMap;
    private final MapPanel mapPanel;

    private final DefaultListModel<Personnage> listModel = new DefaultListModel<>();
    private final JLabel labelOr = new JLabel();
    private final JLabel labelCout = new JLabel();
    private final JLabel labelResultat = new JLabel(" ");
    private final JList<Personnage> listePersonnages = new JList<>(listModel);

    public FenetreInvocation(Map gameMap, MapPanel mapPanel) {
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;

        setTitle("Autel d'invocation");
        setSize(760, 430);
        setLocationRelativeTo(mapPanel);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(1, 2, 12, 12));

        JPanel panelInvocation = new JPanel(new BorderLayout(8, 8));
        JPanel panelPersonnages = new JPanel(new BorderLayout(8, 8));

        labelOr.setHorizontalAlignment(SwingConstants.CENTER);
        labelCout.setHorizontalAlignment(SwingConstants.CENTER);

        rafraichirInfos();

        JButton boutonInvocation = new JButton();
        rafraichirTexteBouton(boutonInvocation);
        boutonInvocation.addActionListener(e -> invoquer(boutonInvocation));

        labelResultat.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea aideInvocation = new JTextArea(
                "1. Le coût d'invocation augmente à chaque tirage : 10, 20, 40, 80, etc.\n" +
                        "2. Taux d'obtention : 1★ 40 %, 2★ 30 %, 3★ 15 %, 4★ 10 %, 5★ 5 %.\n" +
                        "3. Un 5★ est garanti tous les 10 tirages maximum.\n" +
                        "   Si vous obtenez un 5★ avant, le compteur est remis à zéro.\n" +
                        "4. Les personnages 1★ à 5★ ont une vitesse de 1 à 5\n" +
                        "   et des PV de 10 à 50 respectivement."
        );
        aideInvocation.setEditable(false);
        aideInvocation.setOpaque(false);
        aideInvocation.setLineWrap(true);
        aideInvocation.setWrapStyleWord(true);

        panelInvocation.setBorder(BorderFactory.createTitledBorder("Invocation"));
        JPanel haut = new JPanel(new GridLayout(2, 1));
        haut.add(labelOr);
        haut.add(labelCout);
        panelInvocation.add(haut, BorderLayout.NORTH);
        panelInvocation.add(aideInvocation, BorderLayout.CENTER);

        JPanel basInvocation = new JPanel(new GridLayout(2, 1, 4, 4));
        basInvocation.add(boutonInvocation);
        basInvocation.add(labelResultat);
        panelInvocation.add(basInvocation, BorderLayout.SOUTH);

        listePersonnages.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listePersonnages.setVisibleRowCount(12);
        listePersonnages.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if (value instanceof Personnage personnage) {
                    String statut;
                    if (personnage.isEnSoin()) statut = "EN SOIN";
                    else if (personnage.estOccupe()) statut = "OCCUPE";
                    else statut = "DISPO";

                    label.setText(
                            personnage.getNom()
                                    + " - " + personnage.getRareteEtoiles() + "★"
                                    + " [VIT=" + personnage.getVitesse()
                                    + ", HP=" + personnage.getHpActuel() + "/" + personnage.getHpMax()
                                    + "] [" + statut + "]"
                    );

                    if (!personnage.estDisponible() && !isSelected) {
                        label.setForeground(Color.GRAY);
                    }
                }
                return label;
            }
        });

        listePersonnages.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = listePersonnages.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Personnage personnage = listModel.get(index);
                    String action = personnage.getActionCourante() == null ? "Aucune" : personnage.getActionCourante().getLabel();
                    listePersonnages.setToolTipText(
                            personnage.getNom()
                                    + " | " + personnage.getRareteEtoiles() + "★"
                                    + " | Vitesse : " + personnage.getVitesse()
                                    + " | HP : " + personnage.getHpActuel() + "/" + personnage.getHpMax()
                                    + " | Action : " + action
                    );
                }
            }
        });

        listePersonnages.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    Personnage personnage = listePersonnages.getSelectedValue();
                    if (personnage != null) {
                        ouvrirDialogueActions(personnage);
                    }
                }
            }
        });

        panelPersonnages.setBorder(BorderFactory.createTitledBorder("Personnages obtenus"));
        panelPersonnages.add(new JScrollPane(listePersonnages), BorderLayout.CENTER);
        panelPersonnages.add(new JLabel("Clique sur un personnage pour lui donner une action.", SwingConstants.CENTER), BorderLayout.SOUTH);

        add(panelInvocation);
        add(panelPersonnages);

        rafraichirListe();
    }

    private void invoquer(JButton boutonInvocation) {
        if (gameMap.capacitePersonnagesPleine()) {
            JOptionPane.showMessageDialog(this,
                "Capacité maximale atteinte (" + gameMap.getNombrePersonnages() + "/" + gameMap.getCapacitePersonnages() + ").\nConstruisez des maisons pour augmenter la capacité.");
            return;
        }
        if (!gameMap.orSuffisantPourInvoquer()) {
            JOptionPane.showMessageDialog(this, "Pas assez d'or pour invoquer.");
            return;
        }

        Personnage personnage = gameMap.invoquerPersonnage();
        if (personnage == null) return;

        labelResultat.setText("Obtenu : " + personnage.getNom() + " - " + personnage.getRareteEtoiles() + "★");
        rafraichirInfos();
        rafraichirTexteBouton(boutonInvocation);
        rafraichirListe();
        mapPanel.repaint();
    }

    private void ouvrirDialogueActions(Personnage p) {
        if (p.isEnSoin()) {
            JOptionPane.showMessageDialog(this, "Ce personnage est en soin.");
            return;
        }

        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel(),
                ActionType.CHERCHER_NOURRITURE.getLabel(),
                ActionType.CHERCHER_OR.getLabel()
        };

        int choix = JOptionPane.showOptionDialog(
                this,
                "Choisis une action :",
                "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        ActionType nouvelleAction = null;
        if (choix == 0) nouvelleAction = ActionType.COUPER_BOIS;
        else if (choix == 1) nouvelleAction = ActionType.MINER_FER;
        else if (choix == 2) nouvelleAction = ActionType.DEFENDRE;
        else if (choix == 3) nouvelleAction = ActionType.CHERCHER_NOURRITURE;
        else if (choix == 4) nouvelleAction = ActionType.CHERCHER_OR;
        if (nouvelleAction == null) return;

        if (nouvelleAction == ActionType.DEFENDRE) {
            ouvrirDialogueDefense(p);
        } else {
            gameMap.deployerPersonnage(p, nouvelleAction);
        }

        rafraichirListe();
        mapPanel.repaint();
    }

    private void ouvrirDialogueDefense(Personnage p) {
        List<Batiment> entrepots = gameMap.getBatimentsDefenseDisponibles();
        if (entrepots.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aucun entrepôt construit.");
            return;
        }

        DefaultListModel<Batiment> model = new DefaultListModel<>();
        for (Batiment b : entrepots) model.addElement(b);

        JList<Batiment> list = new JList<>(model);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                Batiment b = (Batiment) value;
                String txt = b.getNom() + " (" + b.getX() + "," + b.getY() + ")";
                if (b.isEnAttaque()) txt += " [ATTAQUE]";
                if (b.isProtege()) txt += " [PROTEGE]";
                label.setText(txt);

                if (b.isEnAttaque() && !isSelected) {
                    label.setForeground(Color.RED);
                } else if (b.isProtege() && !isSelected) {
                    label.setForeground(new Color(0, 128, 0));
                }
                return label;
            }
        });

        int result = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(list),
                "Choisir un entrepôt à défendre",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            Batiment cible = list.getSelectedValue();
            if (cible != null) {
                gameMap.deployerPersonnageDefense(p, cible);
            }
        }
    }

    private void rafraichirInfos() {
        labelOr.setText("Or disponible : " + gameMap.getStockOr());
        int actuel = gameMap.getNombrePersonnages();
        int max    = gameMap.getCapacitePersonnages();
        labelCout.setText("Coût : " + gameMap.getCoutInvocationActuel()
            + " or  |  Personnages : " + actuel + " / " + max);
    }

    private void rafraichirTexteBouton(JButton boutonInvocation) {
        if (gameMap.capacitePersonnagesPleine()) {
            boutonInvocation.setText("Capacité pleine (" + gameMap.getNombrePersonnages() + "/" + gameMap.getCapacitePersonnages() + ")");
            boutonInvocation.setEnabled(false);
        } else {
            boutonInvocation.setText("Invoquer (" + gameMap.getCoutInvocationActuel() + " or)  "
                + gameMap.getNombrePersonnages() + "/" + gameMap.getCapacitePersonnages());
            boutonInvocation.setEnabled(true);
        }
    }

    private void rafraichirListe() {
        listModel.clear();
        for (Personnage personnage : gameMap.getPersonnages()) {
            listModel.addElement(personnage);
        }
        rafraichirInfos();
    }
}