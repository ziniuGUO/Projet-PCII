package control;

import java.awt.event.*;
import javax.swing.*;
import model.Map;
import model.Personnage;
import model.ActionType;
import view.MapPanel;

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

        Personnage selected = mapPanel.getSelectedPersonnage();

        // Pointage sur un personnage : selection + action
        Personnage clicked = gameMap.getPersonnageAt(tileX, tileY);
        if (clicked != null) {
            // Si on clique sur le meme personnage deja selectionne => deselection
            if (selected == clicked) {
                mapPanel.setSelectedPersonnage(null);
                mapPanel.repaint();
                return;
            }

            // Sinon, selection du personnage clique
            mapPanel.setSelectedPersonnage(clicked);

            // Si clic gauche => ouvrir dialogue d'actions
            if (SwingUtilities.isLeftMouseButton(e)) {
                ouvrirDialogueActions(clicked);
            }
            mapPanel.repaint();
            return;
        }

        //Clic sur une tuile vide : deplacer le personnage selectionne (debug)
        if (selected != null) {
            selected.setPosition(tileX, tileY);

            // Apres deplacement, on deselectionne le personnage
            mapPanel.setSelectedPersonnage(null);

            mapPanel.repaint();
            return;
        }

        // Clic sur une tuile vide sans personnage selectionne : afficher le type de terrain (debug)
        int terrainType = gameMap.getTerrainAt(tileX, tileY);
        System.out.println("Clic sur (" + tileX + ", " + tileY + ") - Type: " + Map.getNomTerrain(terrainType));
    }

    private void ouvrirDialogueActions(Personnage p) {
        String titre = "Actions - " + p.getNom() + " (" + p.getRareteEtoiles() + "★)";

        // 3 boutons = 3 actions (version minimale)
        Object[] options = {
                ActionType.COUPER_BOIS.getLabel(),
                ActionType.MINER_FER.getLabel(),
                ActionType.DEFENDRE.getLabel()
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

        if (choix == 0) p.setActionCourante(ActionType.COUPER_BOIS);
        else if (choix == 1) p.setActionCourante(ActionType.MINER_FER);
        else if (choix == 2) p.setActionCourante(ActionType.DEFENDRE);
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