package view;

import model.ActionType;
import model.Map;
import model.Personnage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FenetreInvocation extends JFrame {

    private static final int COUT_INVOCATION = 100;

    private final Map gameMap;
    private final MapPanel mapPanel;

    private final DefaultListModel<Personnage> listModel = new DefaultListModel<>();
    private final JLabel labelOr = new JLabel();
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
        rafraichirOr();

        JButton boutonInvocation = new JButton("Invoquer (100 or)");
        boutonInvocation.addActionListener(e -> invoquer());

        labelResultat.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea aideInvocation = new JTextArea(
                "1. Clique sur Invoquer pour obtenir un personnage aléatoire.\n" +
                        "2. Chaque invocation coûte 100 or.\n" +
                        "3. Un personnage OCCUPÉ ne peut pas être renvoyé depuis l'autel.\n" +
                        "4. Quand une mission est finie, clique sur le personnage sur la carte."
        );
        aideInvocation.setEditable(false);
        aideInvocation.setOpaque(false);
        aideInvocation.setLineWrap(true);
        aideInvocation.setWrapStyleWord(true);

        panelInvocation.setBorder(BorderFactory.createTitledBorder("Invocation"));
        panelInvocation.add(labelOr, BorderLayout.NORTH);
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
                    String statut = personnage.estDisponible() ? "DISPO" : "OCCUPE";
                    label.setText(personnage.getNom() + " - " + personnage.getRareteEtoiles() + "★ [" + statut + "]");

                    if (!personnage.estDisponible() && !isSelected) {
                        label.setForeground(Color.GRAY);
                    }
                }
                return label;
            }
        });

        listePersonnages.setToolTipText("");
        ToolTipManager.sharedInstance().registerComponent(listePersonnages);

        listePersonnages.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = listePersonnages.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Personnage personnage = listModel.get(index);
                    String statut = personnage.estDisponible() ? "DISPO" : "OCCUPE";
                    listePersonnages.setToolTipText(
                            personnage.getNom()
                                    + " | " + personnage.getRareteEtoiles() + "★"
                                    + " | Etat : " + statut
                                    + " | Action : " + (personnage.getActionCourante() == null ? "Aucune" : personnage.getActionCourante().getLabel())
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
        panelPersonnages.add(new JLabel("Clique sur un personnage DISPO pour choisir une action.", SwingConstants.CENTER), BorderLayout.SOUTH);

        add(panelInvocation);
        add(panelPersonnages);

        rafraichirListe();
    }

    private void invoquer() {
        Personnage personnage = gameMap.invoquerPersonnage(COUT_INVOCATION);
        if (personnage == null) {
            JOptionPane.showMessageDialog(this, "Pas assez d'or pour invoquer.");
            return;
        }

        labelResultat.setText("Obtenu : " + personnage.getNom() + " - " + personnage.getRareteEtoiles() + "★");
        rafraichirOr();
        rafraichirListe();
        mapPanel.repaint();
    }

    private void ouvrirDialogueActions(Personnage p) {
        if (!p.estDisponible()) {
            JOptionPane.showMessageDialog(this, "Ce personnage est déjà occupé.");
            return;
        }

        String titre = "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)";
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel()
        };

        int choix = JOptionPane.showOptionDialog(
                this,
                "Choisis une action :",
                titre,
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

        if (nouvelleAction == null) return;

        gameMap.deployerPersonnage(p, nouvelleAction);
        rafraichirListe();
        mapPanel.repaint();
    }

    private void rafraichirOr() {
        labelOr.setText("Or disponible : " + gameMap.getStockOr());
    }

    private void rafraichirListe() {
        listModel.clear();
        for (Personnage personnage : gameMap.getPersonnages()) {
            listModel.addElement(personnage);
        }
    }
}