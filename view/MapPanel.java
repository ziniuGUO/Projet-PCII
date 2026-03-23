package view;

import java.awt.*;
import javax.swing.*;
import model.ActionType;
import model.Map;
import model.Personnage;

public class MapPanel extends JPanel {

    private static final int TILE_SIZE  = 48;
    private static final int BORDER_PAD = 40;
    private static final int TITLE_H    = 50;

    private static final Color COL_HERBE_JOUR = new Color(144, 190, 109);
    private static final Color COL_HERBE_NUIT = new Color( 82,   0, 171);
    private static final Color COL_BATIMENT   = new Color(160, 160, 160);
    private static final Color COL_BOIS       = new Color( 34, 139,  34);
    private static final Color COL_FER        = new Color(105, 105, 105);
    private static final Color COL_OR         = new Color(255, 215,   0);
    private static final Color COL_NOURRITURE = new Color(160, 120,  60);

    // couleurs des batiments construits
    private static final Color COL_HOTEL_VILLE = new Color(180,  60,  60);
    private static final Color COL_MAISON      = new Color(210, 180, 140);
    private static final Color COL_ENTREPOT    = new Color(100, 140, 180);
    private static final Color COL_AUTEL_INVOC = new Color(140,  60, 180);

    private static final Color COL_BG       = new Color( 40,  30,  20);
    private static final Color COL_GRID     = new Color(  0,   0,   0,  40);
    private static final Color COL_SELECTED = new Color(255, 100,  50, 200);

    private static final Font FONT_TITLE     = new Font("Serif",     Font.BOLD,  22);
    private static final Font FONT_RESOURCE  = new Font("SansSerif", Font.BOLD,  11);
    private static final Font FONT_TOOLTIP   = new Font("Serif",     Font.BOLD,  13);
    private static final Font FONT_LEGEND    = new Font("SansSerif", Font.PLAIN, 11);
    private static final Font FONT_INV_TITLE = new Font("Serif",     Font.BOLD,  12);
    private static final Font FONT_INV_VAL   = new Font("SansSerif", Font.BOLD,  12);

    // inventaire du joueur
    private int stockBois       = 0;
    private int stockFer        = 0;
    private int stockOr         = 0;
    private int stockNourriture = 0;

    private final Map gameMap;
    private int selectedX = -1;
    private int selectedY = -1;
    private Personnage hoveredPersonnage  = null;
    private Personnage selectedPersonnage = null;

    public void setSelectedPersonnage(Personnage p) { this.selectedPersonnage = p; repaint(); }
    public Personnage getSelectedPersonnage()        { return selectedPersonnage; }

    // setters inventaire
    public void setStockBois(int v)       { this.stockBois       = v; repaint(); }
    public void setStockFer(int v)        { this.stockFer        = v; repaint(); }
    public void setStockOr(int v)         { this.stockOr         = v; repaint(); }
    public void setStockNourriture(int v) { this.stockNourriture = v; repaint(); }

    public MapPanel(Map gameMap) {
        this.gameMap = gameMap;
        int panelW = BORDER_PAD * 2 + gameMap.getWidth()  * TILE_SIZE + 150;
        int panelH = BORDER_PAD * 2 + gameMap.getHeight() * TILE_SIZE + TITLE_H + 30;
        setPreferredSize(new Dimension(panelW, panelH));
        setBackground(COL_BG);
    }

    public void setSelectedPosition(int x, int y) { this.selectedX = x; this.selectedY = y; repaint(); }
    public void clearSelection()                   { this.selectedX = -1; this.selectedY = -1; repaint(); }
    public void setHoveredPersonnage(Personnage p) { this.hoveredPersonnage = p; repaint(); }
    public int getTileSize()    { return TILE_SIZE;  }
    public int getBorderPad()   { return BORDER_PAD; }
    public int getTitleHeight() { return TITLE_H;    }

    private String getInitiales(String nom) {
        if (nom == null || nom.isEmpty()) return "?";
        if (nom.length() >= 2)
            return String.valueOf(nom.charAt(0)).toUpperCase()
                 + String.valueOf(nom.charAt(1)).toLowerCase();
        return String.valueOf(nom.charAt(0)).toUpperCase();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawTitle(g2);
        drawTiles(g2);
        drawPersonnages(g2);
        drawGrid(g2);
        drawSelectedPersonnage(g2);
        drawSelection(g2);
        drawTooltip(g2);
        drawLegend(g2);
        drawNotification(g2);
    }

    private void drawPersonnages(Graphics2D g2) {
        for (Personnage p : gameMap.getPersonnages()) {
            if (!p.isDeploye()) continue;
            int px = BORDER_PAD + p.getX() * TILE_SIZE;
            int py = BORDER_PAD + TITLE_H  + p.getY() * TILE_SIZE;
            int margin = 8, size = TILE_SIZE - margin * 2;
            int cx = px + margin, cy = py + margin;

            g2.setColor(new Color(0, 0, 0, 90));
            g2.fillOval(cx + 2, cy + 3, size, size);

            if (p.isPretARecuperer())       g2.setColor(new Color(255, 180,  60));
            else if (p.isEnExecution())     g2.setColor(new Color(220, 100, 100));
            else                            g2.setColor(new Color( 60, 120, 200));
            g2.fillOval(cx, cy, size, size);

            g2.setColor(new Color(255, 255, 255, 180));
            g2.setStroke(new BasicStroke(2f));
            g2.drawOval(cx, cy, size, size);
            g2.setStroke(new BasicStroke(1f));

            String initiales = getInitiales(p.getNom());
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int tx = px + (TILE_SIZE - fm.stringWidth(initiales)) / 2;
            int ty = py + (TILE_SIZE + fm.getAscent()) / 2 - 4;
            g2.drawString(initiales, tx, ty);
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

    private void drawTitle(Graphics2D g2) {
        String title = "Carte du Royaume";
        g2.setFont(FONT_TITLE);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (getWidth() - 170 - fm.stringWidth(title)) / 2;
        g2.setColor(new Color(0, 0, 0, 130));
        g2.drawString(title, tx + 2, 34);
        GradientPaint gold = new GradientPaint(0, 12, new Color(255, 220, 60), 0, 34, new Color(190, 140, 0));
        g2.setPaint(gold);
        g2.drawString(title, tx, 34);
    }

    private void drawTiles(Graphics2D g2) {
        for (int y = 0; y < gameMap.getHeight(); y++)
            for (int x = 0; x < gameMap.getWidth(); x++)
                drawTile(g2, BORDER_PAD + x * TILE_SIZE, BORDER_PAD + TITLE_H + y * TILE_SIZE, gameMap.getTerrainAt(x, y), x, y);
    }

    private void drawTile(Graphics2D g2, int px, int py, int type, int tileX, int tileY) {
        Color color;
        String label    = null;
        Color textColor = Color.WHITE;

        switch (type) {
            case 1 -> color = gameMap.getIsDay() ? COL_HERBE_JOUR : COL_HERBE_NUIT;
            case 2 -> {
                String typeBat = gameMap.getTypeBatiment(tileX, tileY);
                if (typeBat != null && gameMap.estConstruit(tileX, tileY)) {
                    switch (typeBat) {
                        case Map.TYPE_HOTEL_VILLE:
                            color = COL_HOTEL_VILLE; label = "HOTEL"; break;
                        case Map.TYPE_MAISON:
                            color = COL_MAISON; label = "MAISON";
                            textColor = new Color(60, 40, 20); break;
                        case Map.TYPE_ENTREPOT_BOIS:
                            color = COL_ENTREPOT; label = "ENT.BOIS"; break;
                        case Map.TYPE_ENTREPOT_FER:
                            color = COL_ENTREPOT; label = "ENT.FER"; break;
                        case Map.TYPE_ENTREPOT_OR:
                            color = COL_ENTREPOT; label = "ENT.OR"; break;
                        case Map.TYPE_ENTREPOT_NOURR:
                            color = COL_ENTREPOT; label = "ENT.NOURR"; break;
                        case Map.TYPE_AUTEL_INVOC:
                            color = COL_AUTEL_INVOC; label = "AUTEL"; break;
                        default:
                            color = COL_BATIMENT; label = "BATIMENT";
                    }
                } else {
                    color = COL_BATIMENT; label = "CONSTRUIRE";
                    textColor = new Color(60, 60, 60);
                }
            }
            case 3 -> {
                float tauxBois = (float) gameMap.getStockBoisForet() / gameMap.getStockBoisForetMax();
                color = new Color(20, 90 + (int)(120 * tauxBois), 20);
                label = "BOIS " + gameMap.getStockBoisForet();
            }
            case 4 -> { color = COL_FER;        label = "FER"; }
            case 5 -> { color = COL_OR;          label = "OR";  textColor = new Color(100, 70, 0); }
            case 6 -> { color = COL_NOURRITURE;  label = "NOURR."; }
            default -> color = gameMap.getIsDay() ? COL_HERBE_JOUR : COL_HERBE_NUIT;
        }

        // surbrillance de la zone d'action du personnage selectionne
        ActionType actionAffichee = null;
        if (selectedPersonnage != null && selectedPersonnage.isDeploye())
            actionAffichee = selectedPersonnage.getActionCourante();
        else if (gameMap.getPersonnageMiseEnValeur() != null)
            actionAffichee = gameMap.getActionMiseEnValeur();

        if (gameMap.caseCorrespondAAction(type, actionAffichee)) {
            if (actionAffichee == ActionType.COUPER_BOIS)   color = new Color( 40, 170,  40);
            else if (actionAffichee == ActionType.MINER_FER) color = new Color(120, 120, 210);
        }

        g2.setColor(color);
        g2.fillRect(px, py, TILE_SIZE, TILE_SIZE);

        // bordure plus visible pour les batiments construits
        if (type == 2 && gameMap.getTypeBatiment(tileX, tileY) != null && gameMap.estConstruit(tileX, tileY)) {
            g2.setColor(new Color(0, 0, 0, 80));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRect(px + 1, py + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            g2.setStroke(new BasicStroke(1f));
        } else {
            g2.setColor(new Color(0, 0, 0, 30));
            g2.drawRect(px, py, TILE_SIZE - 1, TILE_SIZE - 1);
        }

        if (label != null) {
            g2.setFont(FONT_RESOURCE);
            FontMetrics fm = g2.getFontMetrics();
            int textX = px + (TILE_SIZE - fm.stringWidth(label)) / 2;
            int textY = py + (TILE_SIZE + fm.getAscent()) / 2 - 2;
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawString(label, textX + 1, textY + 1);
            g2.setColor(textColor);
            g2.drawString(label, textX, textY);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(COL_GRID);
        g2.setStroke(new BasicStroke(0.5f));
        int x0 = BORDER_PAD, y0 = BORDER_PAD + TITLE_H;
        int x1 = x0 + gameMap.getWidth()  * TILE_SIZE;
        int y1 = y0 + gameMap.getHeight() * TILE_SIZE;
        for (int x = 0; x <= gameMap.getWidth();  x++) g2.drawLine(x0 + x * TILE_SIZE, y0, x0 + x * TILE_SIZE, y1);
        for (int y = 0; y <= gameMap.getHeight(); y++) g2.drawLine(x0, y0 + y * TILE_SIZE, x1, y0 + y * TILE_SIZE);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawSelection(Graphics2D g2) {
        if (selectedX < 0) return;
        int px = BORDER_PAD + selectedX * TILE_SIZE;
        int py = BORDER_PAD + TITLE_H   + selectedY * TILE_SIZE;
        g2.setColor(COL_SELECTED);
        g2.setStroke(new BasicStroke(3f));
        g2.drawRect(px + 1, py + 1, TILE_SIZE - 3, TILE_SIZE - 3);
        g2.setStroke(new BasicStroke(1f));
    }

    private void drawTooltip(Graphics2D g2) {
        if (selectedX < 0) return;
        String text;
        if (hoveredPersonnage != null) {
            String etat;
            if (hoveredPersonnage.isPretARecuperer())  etat = "Mission terminée - clique pour récupérer";
            else if (hoveredPersonnage.isEnExecution()) etat = "En exécution";
            else if (hoveredPersonnage.isDeploye())     etat = "En déplacement";
            else                                        etat = "Réserve";
            text = String.format("%s  |  %d★  |  Action: %s  |  (%d, %d)",
                    hoveredPersonnage.getNom(),
                    hoveredPersonnage.getRareteEtoiles(),
                    etat,
                    hoveredPersonnage.getX(), hoveredPersonnage.getY());
        } else {
            int type = gameMap.getTerrainAt(selectedX, selectedY);
            String typeName = switch (type) {
                case 1 -> "Herbe";          case 2 -> "Batiment";
                case 3 -> "Foret -> Bois";  case 4 -> "Mine -> Fer";
                case 5 -> "Gisement -> Or"; case 6 -> "Cueillette -> Nourriture";
                default -> "Inconnu";
            };
            text = String.format("(%d, %d) - %s", selectedX, selectedY, typeName);
        }
        g2.setFont(FONT_TOOLTIP);
        FontMetrics fm = g2.getFontMetrics();
        int tw = fm.stringWidth(text) + 16, th = fm.getHeight() + 6;
        int bx = BORDER_PAD, by = getHeight() - BORDER_PAD / 2 - th;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRoundRect(bx - 4, by - 2, tw, th, 8, 8);
        g2.setColor(new Color(255, 215, 0));
        g2.drawString(text, bx + 4, by + fm.getAscent() - 1);
    }

    private void drawNotification(Graphics2D g2) {
        String message = gameMap.getNotificationMessage();
        if (message == null || message.isBlank()) return;
        int boxW = 300, boxH = 60;
        int bx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE - boxW, by = 8;
        g2.setColor(new Color(20, 20, 20, 210));
        g2.fillRoundRect(bx, by, boxW, boxH, 12, 12);
        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.drawString("Notification", bx + 12, by + 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String ligne1 = message.length() > 42 ? message.substring(0, 42) + "..." : message;
        g2.drawString(ligne1, bx + 12, by + 38);
        g2.drawString("Clique sur le personnage pour recuperer.", bx + 12, by + 53);
    }

    private void drawLegend(Graphics2D g2) {
        int lx = BORDER_PAD + gameMap.getWidth() * TILE_SIZE + 16;
        int ly = BORDER_PAD + TITLE_H;

        String[] terrainLabels = {"Herbe", "Batiment", "Foret / Bois", "Mine / Fer", "Gisement / Or", "Cueillette / Nourr."};
        Color[]  terrainColors = {COL_HERBE_JOUR, COL_BATIMENT, COL_BOIS, COL_FER, COL_OR, COL_NOURRITURE};
        if (!gameMap.getIsDay()) terrainColors[0] = COL_HERBE_NUIT;

        int terrainBlockH = terrainLabels.length * 22 + 30;
        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRoundRect(lx - 6, ly - 6, 135, terrainBlockH, 8, 8);

        g2.setFont(FONT_INV_TITLE);
        g2.setColor(new Color(255, 215, 60));
        g2.drawString("TERRAINS", lx, ly + 13);

        g2.setFont(FONT_LEGEND);
        for (int i = 0; i < terrainLabels.length; i++) {
            int iy = ly + 28 + i * 22;
            g2.setColor(terrainColors[i]);
            g2.fillRect(lx, iy, 14, 14);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawRect(lx, iy, 14, 14);
            g2.setColor(new Color(220, 205, 170));
            g2.drawString(terrainLabels[i], lx + 18, iy + 11);
        }

        // bloc inventaire
        int invY = ly + terrainBlockH + 14;
        String[] resLabels = {"Bois", "Fer", "Or", "Nourriture"};
        Color[]  resColors = {COL_BOIS, COL_FER, COL_OR, COL_NOURRITURE};
        int[]    resValues = {stockBois, stockFer, stockOr, stockNourriture};

        int invBlockH = resLabels.length * 26 + 34;
        g2.setColor(new Color(0, 0, 0, 130));
        g2.fillRoundRect(lx - 6, invY - 6, 135, invBlockH, 8, 8);

        g2.setFont(FONT_INV_TITLE);
        g2.setColor(new Color(255, 215, 60));
        g2.drawString("RESSOURCES", lx, invY + 13);
        g2.setColor(new Color(255, 215, 60, 70));
        g2.drawLine(lx, invY + 18, lx + 122, invY + 18);

        for (int i = 0; i < resLabels.length; i++) {
            int iy = invY + 30 + i * 26;
            g2.setColor(resColors[i]);
            g2.fillRect(lx, iy, 14, 14);
            g2.setColor(new Color(0, 0, 0, 100));
            g2.drawRect(lx, iy, 14, 14);
            g2.setFont(FONT_LEGEND);
            g2.setColor(new Color(220, 205, 170));
            g2.drawString(resLabels[i], lx + 18, iy + 11);
            g2.setFont(FONT_INV_VAL);
            String val = String.valueOf(resValues[i]);
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(resValues[i] > 0 ? new Color(255, 240, 160) : new Color(130, 120, 100));
            g2.drawString(val, lx + 126 - fm.stringWidth(val), iy + 11);
        }
    }
}