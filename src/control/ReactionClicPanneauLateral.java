package control;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import model.ActionType;
import model.Batiment;
import model.Map;
import model.Personnage;
import view.MapPanel;
import view.PanneauLateral;

/**
 * Contrôleur des clics sur le PanneauLateral.
 * Gère la sélection d'un personnage et l'ouverture des dialogues d'action.
 */
public class ReactionClicPanneauLateral extends MouseAdapter {

    private final Map gameMap;
    private final MapPanel mapPanel;
    private final PanneauLateral panneauLateral;

    public ReactionClicPanneauLateral(Map gameMap, MapPanel mapPanel, PanneauLateral panneauLateral) {
        this.gameMap        = gameMap;
        this.mapPanel       = mapPanel;
        this.panneauLateral = panneauLateral;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Clics uniquement dans la colonne droite (personnages)
        if (e.getX() < panneauLateral.getWidth() / 2) return;

        Personnage p = panneauLateral.trouverPersonnageAuClic(e.getY());
        if (p != null) ouvrirDialogueActions(p);
    }

    private void ouvrirDialogueActions(Personnage p) {
        if (p.isEnSoin()) {
            JOptionPane.showMessageDialog(panneauLateral, p.getNom() + " est en soin.");
            return;
        }

        if (p.isPretARecuperer()) {
            int gain = gameMap.recupererRecompenseEtRappeler(p);
            JOptionPane.showMessageDialog(panneauLateral,
                gain > 0 ? "Récompense récupérée : +" + gain : "Mission terminée.");
            mapPanel.repaint();
            panneauLateral.repaint();
            return;
        }

        Object[] options = {
            ActionType.COUPER_BOIS.getLabel(),
            ActionType.MINER_FER.getLabel(),
            ActionType.DEFENDRE.getLabel(),
            ActionType.CHERCHER_NOURRITURE.getLabel(),
            ActionType.CHERCHER_OR.getLabel(),
            "Rappeler"
        };

        int choix = JOptionPane.showOptionDialog(panneauLateral,
            "Action pour " + p.getNom() + " (" + p.getRareteEtoiles() + "★)",
            "Actions - " + p.getNom(),
            JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
            null, options, options[0]);

        if (choix == 5) {
            gameMap.rappelerPersonnage(p);
        } else if (choix == 2) {
            ouvrirDialogueDefense(p);
        } else if (choix >= 0) {
            ActionType[] actions = {
                ActionType.COUPER_BOIS, ActionType.MINER_FER,
                ActionType.DEFENDRE, ActionType.CHERCHER_NOURRITURE, ActionType.CHERCHER_OR
            };
            gameMap.deployerPersonnage(p, actions[choix]);
        }

        mapPanel.repaint();
        panneauLateral.repaint();
    }

    private void ouvrirDialogueDefense(Personnage p) {
        List<Batiment> entrepots = gameMap.getBatimentsDefenseDisponibles();
        if (entrepots.isEmpty()) {
            JOptionPane.showMessageDialog(panneauLateral, "Aucun entrepôt construit.");
            return;
        }

        DefaultListModel<Batiment> model = new DefaultListModel<>();
        for (Batiment b : entrepots) model.addElement(b);

        JList<Batiment> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                Batiment b = (Batiment) value;
                String txt = b.getNom() + " (" + b.getX() + "," + b.getY() + ")";
                if (b.isEnAttaque()) { txt += " [ATTAQUE]"; if (!isSelected) label.setForeground(Color.RED); }
                if (b.isProtege())   { txt += " [PROTEGE]"; if (!isSelected) label.setForeground(new Color(0, 128, 0)); }
                label.setText(txt);
                return label;
            }
        });

        int result = JOptionPane.showConfirmDialog(panneauLateral,
            new JScrollPane(list), "Bâtiment à défendre", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION && list.getSelectedValue() != null) {
            gameMap.deployerPersonnageDefense(p, list.getSelectedValue());
        }
    }
}