package view;

import java.awt.*;
import javax.swing.*;
import model.Map;
import model.Personnage;
/**
 * Panel d'affichage simplifie de la Map avec code couleur + noms de ressources.
 * 
 * Types de terrain :
 *   1 = Herbe          (vert clair)
 *   2 = Batiment       (gris)
 *   3 = Foret          (vert fonce - BOIS)
 *   4 = Mine           (gris fonce - FER)
 *   5 = Gisement       (jaune - OR)
 *   6 = Cueillette     (brun - NOURRITURE)
 */
public class MapPanel extends JPanel {

    // ── Constantes ─────────────────────────────────────────────────────────────
    private static final int TILE_SIZE  = 48;
    private static final int BORDER_PAD = 40;
    private static final int TITLE_H    = 50;

    // Code couleur
    private static final Color COL_HERBE      = new Color(144, 190, 109);
    private static final Color COL_BATIMENT   = new Color(160, 160, 160);
    private static final Color COL_BOIS       = new Color( 34, 139,  34); // Vert foret
    private static final Color COL_FER        = new Color(105, 105, 105); // Gris fonce
    private static final Color COL_OR         = new Color(255, 215,   0); // Jaune or
    private static final Color COL_NOURRITURE = new Color(160, 120,  60); // Brun-vert

    private static final Color COL_BG       = new Color( 40,  30,  20);
    private static final Color COL_GRID     = new Color(  0,   0,   0,  40);
    private static final Color COL_SELECTED = new Color(255, 100,  50, 200);

    private static final Font FONT_TITLE    = new Font("Serif", Font.BOLD, 22);
    private static final Font FONT_RESOURCE = new Font("SansSerif", Font.BOLD, 11);
    private static final Font FONT_TOOLTIP  = new Font("Serif", Font.BOLD, 13);
    private static final Font FONT_LEGEND   = new Font("SansSerif", Font.PLAIN, 11);

    // ── Etat ───────────────────────────────────────────────────────────────────
    private final Map gameMap;
    private int selectedX = -1;
    private int selectedY = -1;

    // Personnage survole (pour afficher ses infos)
    private Personnage hoveredPersonnage = null;

    // Personnage selectionne (pour deplacement)
    private Personnage selectedPersonnage = null;

    public void setSelectedPersonnage(Personnage p) {
        this.selectedPersonnage = p;
        repaint();
    }

    public Personnage getSelectedPersonnage() {
        return selectedPersonnage;
    }
    // ── Constructeur ───────────────────────────────────────────────────────────
    public MapPanel(Map gameMap) {
        this.gameMap = gameMap;

        int panelW = BORDER_PAD * 2 + gameMap.getWidth()  * TILE_SIZE + 140;
        int panelH = BORDER_PAD * 2 + gameMap.getHeight() * TILE_SIZE + TITLE_H + 30;
        setPreferredSize(new Dimension(panelW, panelH));
        setBackground(COL_BG);
    }

    // ── Methodes publiques pour la selection (utilisees par ReactionClic) ─────
    
    public void setSelectedPosition(int x, int y) {
        this.selectedX = x;
        this.selectedY = y;
        repaint();
    }

    public void clearSelection() {
        this.selectedX = -1;
        this.selectedY = -1;
        repaint();
    }
    public void setHoveredPersonnage(Personnage p) {
        this.hoveredPersonnage = p;
        repaint();
    }
    public int getTileSize() {
        return TILE_SIZE;
    }

    public int getBorderPad() {
        return BORDER_PAD;
    }

    public int getTitleHeight() {
        return TITLE_H;
    }

    // ── Rendu principal ────────────────────────────────────────────────────────
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTitle(g2);
        drawTiles(g2);
        drawPersonnages(g2);
        drawGrid(g2);
        drawSelectedPersonnage(g2);
        drawSelection(g2);
        drawTooltip(g2);
        drawLegend(g2);
    }

    // ── Personnages ───────────────────────────────────────────────────────────
    private void drawPersonnages(Graphics2D g2) {
        for (Personnage p : gameMap.getPersonnages()) {
            int px = BORDER_PAD + p.getX() * TILE_SIZE;
            int py = BORDER_PAD + TITLE_H + p.getY() * TILE_SIZE;

            int margin = 8;
            int size = TILE_SIZE - margin * 2;
            int cx = px + margin;
            int cy = py + margin;

            // Ombre
            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(cx + 2, cy + 3, size, size);

            // Corps (cercle)
            g2.setColor(new Color(60, 120, 200));
            g2.fillOval(cx, cy, size, size);

            // Contour
            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, size, size);
            g2.setStroke(new BasicStroke(1f));

            // Lettre "V"
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            String letter = "V";
            int tx = px + (TILE_SIZE - fm.stringWidth(letter)) / 2;
            int ty = py + (TILE_SIZE + fm.getAscent()) / 2 - 4;
            g2.drawString(letter, tx, ty);
        }
    }

    private void drawSelectedPersonnage(Graphics2D g2) {
        if (selectedPersonnage == null) return;

        int px = BORDER_PAD + selectedPersonnage.getX() * TILE_SIZE;
        int py = BORDER_PAD + TITLE_H + selectedPersonnage.getY() * TILE_SIZE;

        g2.setColor(new Color(255, 215, 0, 180));
        g2.setStroke(new BasicStroke(4f));
        g2.drawOval(px + 4, py + 4, TILE_SIZE - 8, TILE_SIZE - 8);
        g2.setStroke(new BasicStroke(1f));
    }
    // ── Titre ──────────────────────────────────────────────────────────────────
    private void drawTitle(Graphics2D g2) {
        String title = "Carte du Royaume";
        g2.setFont(FONT_TITLE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - 140 - fm.stringWidth(title)) / 2;

        g2.setColor(new Color(0, 0, 0, 130));
        g2.drawString(title, tx + 2, 34);

        GradientPaint gold = new GradientPaint(0, 12, new Color(255, 220, 60),
                                               0, 34, new Color(190, 140,  0));
        g2.setPaint(gold);
        g2.drawString(title, tx, 34);
    }

    // ── Tuiles simplifiees ─────────────────────────────────────────────────────
    private void drawTiles(Graphics2D g2) {
        for (int y = 0; y < gameMap.getHeight(); y++) {
            for (int x = 0; x < gameMap.getWidth(); x++) {
                int px = BORDER_PAD + x * TILE_SIZE;
                int py = BORDER_PAD + TITLE_H + y * TILE_SIZE;
                int type = gameMap.getTerrainAt(x, y);
                
                drawTile(g2, px, py, type);
            }
        }
    }

    private void drawTile(Graphics2D g2, int px, int py, int type) {
        Color color;
        String label = null;
        Color textColor = Color.WHITE;

        switch (type) {
            case 1: // Herbe
                color = COL_HERBE;
                break;
            case 2: // Batiment
                color = COL_BATIMENT;
                label = "CONSTRUIRE";
                textColor = new Color(60, 60, 60);
                break;
            case 3: // Foret / Bois
                color = COL_BOIS;
                label = "BOIS";
                break;
            case 4: // Mine / Fer
                color = COL_FER;
                label = "FER";
                break;
            case 5: // Gisement / Or
                color = COL_OR;
                label = "OR";
                textColor = new Color(100, 70, 0);
                break;
            case 6: // Cueillette / Nourriture
                color = COL_NOURRITURE;
                label = "NOURRITURE";
                break;
            default:
                color = COL_HERBE;
        }

        // Fond de couleur
        g2.setColor(color);
        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

        // Bordure legere
        g2.setColor(new Color(0, 0, 0, 30));
        g2.drawRect(px, py, TILE_SIZE - 1, TILE_SIZE - 1);

        // Texte du label si necessaire
        if (label != null) {
            g2.setFont(FONT_RESOURCE);
            g2.setColor(textColor);
            FontMetrics fm = g2.getFontMetrics();
            
            // Texte centre
            int textWidth = fm.stringWidth(label);
            int textX = px + (TILE_SIZE - textWidth) / 2;
            int textY = py + (TILE_SIZE + fm.getAscent()) / 2 - 2;
            
            // Ombre pour lisibilite
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(label, textX + 1, textY + 1);
            g2.setColor(textColor);
            g2.drawString(label, textX, textY);
        }
    }

    // ── Grille ─────────────────────────────────────────────────────────────────
    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        int x0 = BORDER_PAD, y0 = BORDER_PAD + TITLE_H;
        int x1 = x0 + gameMap.getWidth()  * TILE_SIZE;
        int y1 = y0 + gameMap.getHeight() * TILE_SIZE;
        
        for (int x = 0; x <= gameMap.getWidth();  x++) 
            g2.drawLine(x0 + x * TILE_SIZE, y0, x0 + x * TILE_SIZE, y1);
        for (int y = 0; y <= gameMap.getHeight(); y++) 
            g2.drawLine(x0, y0 + y * TILE_SIZE, x1, y0 + y * TILE_SIZE);
        
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Selection ──────────────────────────────────────────────────────────────
    private void drawSelection(Graphics2D g2) {
        if (selectedX < 0) return;
        int px = BORDER_PAD + selectedX * TILE_SIZE;
        int py = BORDER_PAD + TITLE_H + selectedY * TILE_SIZE;
        g2.setColor(COL_SELECTED);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(px + 1, py + 1, TILE_SIZE - 3, TILE_SIZE - 3);
        g2.setStroke(new BasicStroke(1f));
    }

    // ── Tooltip ────────────────────────────────────────────────────────────────
    private void drawTooltip(Graphics2D g2) {
        if (selectedX < 0) return;
        String text;
        if (hoveredPersonnage != null) {
            text = String.format(
                    "%s  |  %d★  |  Action: %s  |  (%d, %d)",
                    hoveredPersonnage.getNom(),
                    hoveredPersonnage.getRareteEtoiles(),
                    hoveredPersonnage.getActionCourante().getLabel(),
                    hoveredPersonnage.getX(),
                    hoveredPersonnage.getY()
            );
        } else {
            int type = gameMap.getTerrainAt(selectedX, selectedY);
            String typeName = switch (type) {
                case 1 -> "Herbe";
                case 2 -> "Batiment";
                case 3 -> "Foret -> Bois";
                case 4 -> "Mine -> Fer";
                case 5 -> "Gisement -> Or";
                case 6 -> "Cueillette -> Nourriture";
                default -> "Inconnu";
            };
            text = String.format("(%d, %d)  -  %s", selectedX, selectedY, typeName);
        }

        g2.setFont(FONT_TOOLTIP);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 16;
        int th = fm.getHeight() + 6;
        int bx = BORDER_PAD;
        int by = getHeight() - BORDER_PAD / 2 - th;

        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx - 4, by - 2, tw, th, 8, 8);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(text, bx + 4, by + fm.getAscent() - 1);
    }

    // ── Legende ────────────────────────────────────────────────────────────────
    private void drawLegend(Graphics2D g2) {
        int lx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE + 16;
        int ly = BORDER_PAD + TITLE_H;

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(lx - 6, ly - 6, 130, 6 * 22 + 30, 8, 8);

        g2.setFont(new Font("Serif", Font.BOLD, 12));
        g2.setColor(new Color(255, 215, 60));
        g2.drawString("RESSOURCES", lx, ly + 13);

        String[] labels = {"Herbe", "Batiment", "Foret / Bois", "Mine / Fer", "Gisement / Or", "Cueillette / Nourr."};
        Color[]  colors = {COL_HERBE, COL_BATIMENT, COL_BOIS, COL_FER, COL_OR, COL_NOURRITURE};

        g2.setFont(FONT_LEGEND);
        for (int i = 0; i < labels.length; i++) {
            int iy = ly + 28 + i * 22;
            g2.setColor(colors[i]);
            g2.fillRect(lx, iy, 14, 14);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawRect(lx, iy, 14, 14);
            g2.setColor(new Color(220, 205, 170));
            g2.drawString(labels[i], lx + 18, iy + 11);
        }
    }
}