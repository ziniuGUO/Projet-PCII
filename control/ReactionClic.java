package control;

import java.awt.event.*;
import javax.swing.*;
import model.Map;
import model.Personnage;
import model.ActionType;
import view.MapPanel;
import view.FenetreInvocation;
/**
 * Gestion des interactions souris sur la map.
 * Gere la selection de tuiles au survol et au clic.
 */
public class ReactionClic implements MouseMotionListener, MouseListener {

    private final Map gameMap;
    private final MapPanel mapPanel;
    private final int tileSize;
    private final int borderPad;
    private final int titleHeight;

    public ReactionClic(Map gameMap, MapPanel mapPanel, int tileSize, int borderPad, int titleHeight) {
        this.gameMap = gameMap;
        this.mapPanel = mapPanel;
        this.tileSize = tileSize;
        this.borderPad = borderPad;
        this.titleHeight = titleHeight;
    }

    // ── MouseMotionListener ────────────────────────────────────────────────────

    @Override
    public void mouseMoved(MouseEvent e) {
        // Convertir position pixel en coordonnees tuile
        int tileX = (e.getX() - borderPad) / tileSize;
        int tileY = (e.getY() - borderPad - titleHeight) / tileSize;

        // Verifier si la position est valide sur la map
        if (gameMap.isValidPosition(tileX, tileY)) {
            mapPanel.setSelectedPosition(tileX, tileY);
            // Survol personnage ?
            Personnage p = gameMap.getPersonnageAt(tileX, tileY);
            mapPanel.setHoveredPersonnage(p);
        } else {
            mapPanel.clearSelection();
            mapPanel.setHoveredPersonnage(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Pas d'action pour le drag pour l'instant
    }

    // ── MouseListener ──────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        int tileX = (e.getX() - borderPad) / tileSize;
        int tileY = (e.getY() - borderPad - titleHeight) / tileSize;

        if (!gameMap.isValidPosition(tileX, tileY)) return;

        if (gameMap.estAutelInvocation(tileX, tileY) && SwingUtilities.isLeftMouseButton(e)) {
            FenetreInvocation fenetreInvocation = new FenetreInvocation(gameMap, mapPanel);
            fenetreInvocation.setVisible(true);
            return;
        }

        Personnage clicked = gameMap.getPersonnageAt(tileX, tileY);
        if (clicked != null) {
            mapPanel.setSelectedPersonnage(clicked);

            if (clicked.isPretARecuperer()) {
                int gain = gameMap.recupererRecompenseEtRappeler(clicked);
                String msg;
                if (clicked.getActionCourante() == ActionType.DEFENDRE || gain == 0) {
                    msg = "Mission terminée. Le personnage est retourné à l'autel.";
                } else {
                    msg = "Récompense récupérée : +" + gain + ". Le personnage retourne à l'autel.";
                }
                JOptionPane.showMessageDialog(mapPanel, msg);
                mapPanel.setSelectedPersonnage(null);
                mapPanel.repaint();
                return;
            }

            if (SwingUtilities.isLeftMouseButton(e)) {
                ouvrirDialogueActions(clicked);
            }
            mapPanel.repaint();
            return;
        }
    }

    private void ouvrirDialogueActions(Personnage p) {
        String titre = "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)";

        // 3 boutons = 3 actions (version minimale)
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel(),
                "Rappeler"
        };

        int choix = JOptionPane.showOptionDialog(
                mapPanel,
                "Choisis une action :\n\nAction actuelle : " + p.getActionCourante().getLabel(),
                titre,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choix == 3) {
            gameMap.rappelerPersonnage(p);
            mapPanel.setSelectedPersonnage(null);
            mapPanel.repaint();
            return;
        }

        ActionType nouvelleAction = null;
        if (choix == 0) p.setActionCourante(ActionType.COUPER_BOIS);
        else if (choix == 1) p.setActionCourante(ActionType.MINER_FER);
        else if (choix == 2) p.setActionCourante(ActionType.DEFENDRE);

        if (nouvelleAction == null) return;

        if (p.getActionCourante() == nouvelleAction && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {
            JOptionPane.showMessageDialog(mapPanel, "Ce personnage effectue déjà cette action.");
            return;
        }

        if (p.getActionCourante() != null && p.getActionCourante() != nouvelleAction
                && (p.isEnExecution() || !p.estArrive() || p.isPretARecuperer())) {

            int confirmation = JOptionPane.showConfirmDialog(
                    mapPanel,
                    "Ce personnage est déjà occupé.\nInterrompre l'action en cours ?\nSi tu interromps, aucune ressource ne sera obtenue.",
                    "Confirmation",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            p.interrompreAction();
            p.setPretARecuperer(false);
        }

        gameMap.deployerPersonnage(p, nouvelleAction);
        mapPanel.repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Pas d'action pour l'instant
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Efface la selection quand la souris sort de la map
        mapPanel.clearSelection();
        mapPanel.setHoveredPersonnage(null);
    }
}