package control;

import java.awt.event.*;
import model.Map;
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
        } else {
            mapPanel.clearSelection();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Pas d'action pour le drag pour l'instant
    }

    // ── MouseListener ──────────────────────────────────────────────────────────

    @Override
    public void mouseClicked(MouseEvent e) {
        // Recuperer la tuile cliquee
        int tileX = (e.getX() - borderPad) / tileSize;
        int tileY = (e.getY() - borderPad - titleHeight) / tileSize;

        if (gameMap.isValidPosition(tileX, tileY)) {
            int terrainType = gameMap.getTerrainAt(tileX, tileY);
            System.out.println("Clic sur (" + tileX + ", " + tileY + ") - Type: " + Map.getNomTerrain(terrainType));
            
            // Ici tu pourras ajouter d'autres actions selon le type de tuile
            // Par exemple: ouvrir un menu de construction, selectionner un villageois, etc.
        }
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
    }
}